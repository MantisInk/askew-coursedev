/*
 * PlatformController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package askew.playermode.leveleditor;

import askew.*;
import askew.entity.BackgroundEntity;
import askew.entity.Entity;
import askew.entity.ghost.GhostModel;
import askew.entity.obstacle.Obstacle;
import askew.entity.owl.OwlModel;
import askew.entity.sloth.SlothModel;
import askew.entity.thorn.ThornModel;
import askew.entity.tree.PoleVault;
import askew.entity.tree.StiffBranch;
import askew.entity.tree.Trunk;
import askew.entity.vine.Vine;
import askew.entity.wall.WallModel;
import askew.playermode.WorldController;
import askew.playermode.gamemode.GameModeController;
import askew.playermode.leveleditor.button.Button;
import askew.playermode.leveleditor.button.ButtonList;
import askew.playermode.leveleditor.button.ToggleButton;
import askew.util.RecordBook;
import askew.util.json.JSONLoaderSaver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static javax.swing.JOptionPane.showInputDialog;

public class LevelEditorController extends WorldController {

	/** Track asset loading from all instances and subclasses */
	private AssetState levelEditorAssetState = AssetState.EMPTY;

	public static final int BUFFER = 5;
	public static final int TEXT_HEIGHT = 20;
	public static final int FIELD_TEXT_WIDTH = 150;
	public static final int FIELD_BOX_WIDTH = 150;
	public static final int BUTTON_WIDTH = 75;
	public static final int BUTTON_HEIGHT = 30;
	public static final int TEXT_LENGTH = 175;
	public static final int TEXT_HEIGHT1 = 20;

	private JSONLoaderSaver jsonLoaderSaver;
	@Getter @Setter
	private MantisAssetManager mantisAssetManager;

	private LevelModel levelModel;

	private static ShapeRenderer gridLineRenderer = new ShapeRenderer();

	private Texture background;
	private Texture grey;
	private Texture upFolder;
	private Texture folder;
	private Texture placeholder;
	private Texture yellowbox;


	JFrame editorWindow;

	//Camera Variables
	Affine2 camTrans;
	float cxCamera;				//lower left corner position
	float cyCamera;
	float adjustedCxCamera;		//center of le window position
	float adjustedCyCamera;
	float mouseX;				//mouse position in window
	float mouseY;
	float adjustedMouseX;		//mouse position adjusted to camera
	float adjustedMouseY;



	protected Vector2 oneScale;
	private transient CircleShape circleShape = new CircleShape();

	private boolean pressedL, prevPressedL;


	@Getter
	private String currentLevel;

	/** A decrementing int that helps prevent accidental repeats of actions through an arbitrary countdown */
	private int inputRateLimiter = 0;

	public static final int UI_WAIT_SHORT = 2;
	public static final int UI_WAIT_LONG = 15;
	public static final int UI_WAIT_ETERNAL = 120;

	//In Pixels, divide by world scale for box2d.
	public static final float GUI_LOWER_BAR_HEIGHT = 200.f;
	public static final float GUI_LEFT_BAR_WIDTH = 200.f;
	public static final float GUI_LEFT_BAR_MARGIN = 16f;

	public float MAX_SNAP_DISTANCE = 1f;
	public float CAMERA_PAN_SPEED = 20f;

	private EntityTree entityTree;
	private Entity selected;
	private Button manip;
	private boolean dragging = false;
	private boolean creating = false;
	private boolean snapping = false;
	private boolean movefar = false;
	private GameModeController gmc;

	private ButtonList buttons;
	private boolean didLoad;
	private boolean released;


	private boolean prompting;
	private boolean showHelp;
	private static final String HELP_TEXT = "Welcome to the help screen. You \n" +
			"can hit H at any time to toggle this screen. Remember to save \n" +
			"often!\n" +
			"\n" +
			"The controls are as follows:\n" +
			"Left Click: Place currently selected entity\n" +
			"Right Click: Delete entity under mouse\n" +
			"Left Arrow Key: Cycle left on selected entity\n" +
			"Right Arrow Key: Cycle right on selected entity\n" +
			"Enter: Select entity for placement\n" +
			"E: Edit entity under mouse\n" +
			"N: Name level (can be used to make a new level)\n" +
			"L: Load level (do not include .json in the level name!)\n" +
			"S: Save\n" +
			"B: Set background texture\n" +
			"T: Draw grid lines\n" +
			"X: (xbox controller) switch to playing the level\n" +
			"H: Toggle this help text";
	private boolean loadingLevelPrompt;
	private boolean shouldDrawGrid;

	private Vector2 temp;

	/**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(MantisAssetManager manager) {
		super.preLoadContent(manager);
		jsonLoaderSaver.setManager(manager);
		setMantisAssetManager(manager);
	}

	@Override
	public void setCanvas(GameCanvas canvas) {
		this.canvas = canvas;
		this.worldScale.x = 1.0f * (float)canvas.getWidth()/(float)bounds.getWidth();
		this.worldScale.y = 1.0f * (float)canvas.getHeight()/(float)bounds.getHeight();
	}

	/**
	 * Load the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(MantisAssetManager manager) {
		super.loadContent(manager);
		background = manager.get("texture/background/background1.png");
		grey = manager.get("texture/leveleditor/grey.png");
		upFolder = manager.get("texture/leveleditor/up.png");
		folder = manager.get("texture/leveleditor/folder.png");
		placeholder = manager.get("texture/leveleditor/placeholder.png");
		yellowbox = manager.get("texture/leveleditor/yellowbox.png");
		entityTree.setTextures(manager);
		buttons.setManager(manager);
		buttons.setTextures(manager);
		levelEditorAssetState = AssetState.COMPLETE;
	}


	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public LevelEditorController(GameModeController gmc) {
//		super(36,18,0); I want this scale but for the sake of alpha:
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,0);
		jsonLoaderSaver = new JSONLoaderSaver(false);
		entityTree = new EntityTree();
		buttons = new ButtonList();
		currentLevel = "test_save_obstacle";
		showHelp = true;
		shouldDrawGrid = true;
		camTrans = new Affine2();
		oneScale = new Vector2(1,1);
		pressedL = false;
		prevPressedL = false;
		this.gmc = gmc;
	}

	public void setLevel(String levelName) {
		currentLevel = levelName;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Gdx.input.setCursorCatched(false);
		Vector2 gravity = new Vector2(world.getGravity());

		for(Entity obj : objects) {
			if( (obj instanceof Obstacle))
				((Obstacle)obj).deactivatePhysics(world);
		}

		objects.clear();
		buttons.clear();
		world.dispose();

		world = new World(gravity,false);
		setComplete(false);
		setFailure(false);
		populateLevel();
		populateButtons();

		adjustedCxCamera = 0;
		adjustedCyCamera = 0;
		camUpdate();

		pressedL = false;
		prevPressedL = false;
		released = true;
		if (didLoad) makeGuiWindow();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		try {
			levelModel = jsonLoaderSaver.loadLevel(currentLevel);
			if (levelModel != null)
				background = mantisAssetManager.get(levelModel.getBackground(), Texture.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (levelModel == null) {
			levelModel = new LevelModel();
		}

		for (Entity o : levelModel.getEntities()) {
			objects.add(o);
		}
	}

	private void populateButtons(){
		buttons.add(new Button(GUI_LEFT_BAR_MARGIN, GUI_LEFT_BAR_MARGIN,
				GUI_LEFT_BAR_WIDTH- (2*GUI_LEFT_BAR_MARGIN), GUI_LEFT_BAR_MARGIN,
				"JSON", 0, "levelgui"));

		buttons.add(new Button(GUI_LEFT_BAR_MARGIN, 3 * GUI_LEFT_BAR_MARGIN,
				GUI_LEFT_BAR_WIDTH- (2*GUI_LEFT_BAR_MARGIN), GUI_LEFT_BAR_MARGIN,
				"JSON", 1, "globalconfig"));
		buttons.add(new ToggleButton(GUI_LEFT_BAR_MARGIN, 5 * GUI_LEFT_BAR_MARGIN,
				GUI_LEFT_BAR_WIDTH- (2*GUI_LEFT_BAR_MARGIN), GUI_LEFT_BAR_MARGIN,
				"LEOptions", 0, "snapping"));

		buttons.add(new ToggleButton(GUI_LEFT_BAR_MARGIN, 7 * GUI_LEFT_BAR_MARGIN,
				GUI_LEFT_BAR_WIDTH- (2*GUI_LEFT_BAR_MARGIN), GUI_LEFT_BAR_MARGIN,
				"LEOptions", 1, "move far"));

		buttons.add(new Button(GUI_LEFT_BAR_MARGIN, 11 * GUI_LEFT_BAR_MARGIN,
				GUI_LEFT_BAR_WIDTH- (2*GUI_LEFT_BAR_MARGIN), GUI_LEFT_BAR_MARGIN,
				"Entity", 0, "edit"));

		buttons.add(new Button(GUI_LEFT_BAR_MARGIN, 13 * GUI_LEFT_BAR_MARGIN,
				GUI_LEFT_BAR_WIDTH- (2*GUI_LEFT_BAR_MARGIN), GUI_LEFT_BAR_MARGIN,
				"Entity", 1, "delete"));

		buttons.add(new Button(GUI_LEFT_BAR_MARGIN, 15 * GUI_LEFT_BAR_MARGIN,
				GUI_LEFT_BAR_WIDTH- (2*GUI_LEFT_BAR_MARGIN), GUI_LEFT_BAR_MARGIN,
				"Entity", 2, "copy"));

		buttons.add(new Button(GUI_LEFT_BAR_MARGIN, 17 * GUI_LEFT_BAR_MARGIN,
				GUI_LEFT_BAR_WIDTH- (2*GUI_LEFT_BAR_MARGIN), GUI_LEFT_BAR_MARGIN,
				"Entity", 3, "deselect"));

	}

	//region Utility Helpers
	/**
	 * Type safety is overrated [trevor]
	 * @param x
	 * @param y
	 */
	private Entity createXY(EntityTree.ETNode node, float x, float y) {
		float xorig = x;
		float yorig = y;
		x = Math.round(x);
		y = Math.round(y);

		Entity entity = null;
		String name = node.name;


		switch (name) {
			case "SlothModel":
				entity = new SlothModel(x,y);
				break;
			case "Vine":
				entity = new Vine(x,y,5.0f, 5f, -400f);
				break;
			case "Trunk":
				entity = new Trunk(x,y, 5.0f, 0);
				break;
			case "PoleVault":
				entity = new PoleVault(x,y, 5.0f, oneScale, 0);
				break;
			case "StiffBranch":
				entity = new StiffBranch(x,y, 3.0f,oneScale, 0);
				break;
			case "OwlModel":
				entity = new OwlModel(x,y);
				break;
			case "WallModel":
				entity = new WallModel(x,y,new float[] {0,0,0f,1f,1f,1f,1f,0f});
				break;
			case "ThornModel":
				entity = new ThornModel(x,y,1,0);
				break;
			case "GhostModel":
				entity = new GhostModel(x,y,x+2,y+2);
				break;
			case "BackgroundEntity":
				entity = new BackgroundEntity(xorig,yorig);
				break;

			default:
				//System.err.println("UNKNOWN ENT");
				break;
		}


		inputRateLimiter = UI_WAIT_SHORT;
		if(entityTree.isBackground){
			entity = new BackgroundEntity(x,y,node.texturePath);
		}
		if(entity == null){
			System.err.println("UNKNOWN ENT");
		}
		return entity;

	}

	private void deleteEntity(Entity target){
		objects.remove(target);
	}

	public Entity entityQuery() {

		Entity found = null;
		Vector2 mouse = new Vector2(adjustedMouseX, adjustedMouseY);
		float minDistance = Float.MAX_VALUE;
		for (Entity e : objects) {
			Vector2 pos = e.getPosition();
			if(movefar){
				pos = e.getModifiedPosition(adjustedCxCamera,adjustedCyCamera);
			}

			float curDist = pos.dst(mouse);
			if (curDist < minDistance) {
				found = e;
				minDistance = curDist;
			}
		}

		if (minDistance < MAX_SNAP_DISTANCE) {
			return found;
		}
		return null;
	}

	public void camUpdate(){
		cxCamera = adjustedCxCamera - (((bounds.getWidth()-(GUI_LEFT_BAR_WIDTH/worldScale.x) )/2f)+(GUI_LEFT_BAR_WIDTH / worldScale.x) );
		cyCamera = adjustedCyCamera - (((bounds.getHeight()-(GUI_LOWER_BAR_HEIGHT/worldScale.y))/2f) + (GUI_LOWER_BAR_HEIGHT / worldScale.y));

	}

	private boolean processButtons(Button b){
		if(b != null){
			switch (b.getGroup()){
				case("JSON"):
					switch (b.getName()){
						case("levelgui"):
							makeGuiWindow();
							break;
						case("globalconfig"):
							promptGlobalConfig();
							break;
						default:
							break;
					}
					break;
				case("LEOptions"):
					switch (b.getName()){
						case("snapping"):
							ToggleButton t = ((ToggleButton)b);
							t.setOn(!t.isOn());
							snapping = t.isOn();
							break;
						case("move far"):
							t = ((ToggleButton)b);
							t.setOn(!t.isOn());
							movefar = t.isOn();
							break;
						default:
							break;
					}
					break;
				case("Entity"):
					switch(b.getName()){
						case("edit"):
							if (selected != null) {
								promptTemplate(selected);
								objects.remove(selected);
								selected = null;
							}
							dragging = false;
							creating = false;
							break;
						case("delete"):
							if (selected != null) {
								objects.remove(selected);
								selected = null;
							}
							dragging = false;
							creating = false;
							break;
						case("copy"):

							break;
						case("deselect"):
							selected = null;
							dragging = false;
							creating = false;
							break;
						default:
							break;
					}
				default:
					break;
			}
			return true;
		}
		else{
			return false;
		}
	}
	//endregion

	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt Number of seconds since last animation frame
	 *
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		if (!super.preUpdate(dt)) {
			return false;
		}

		InputController input = InputControllerManager.getInstance().getController(0);
		prevPressedL = pressedL;
		pressedL = input.isLKeyPressed();
		if (input.didRightDPadPress()) {
			System.out.println("GM");
			listener.exitScreen(this, EXIT_LE_GM);
			return false;
		} else if (input.didTopDPadPress()) {
			System.out.println("MM");
			listener.exitScreen(this, EXIT_LE_MM);
			return false;
		}else if (input.didBottomDPadPress()) {
			reset();
		}

		return true;
	}

	public void update(float dt) {

		// Decrement rate limiter to allow new input
		if (inputRateLimiter > 0) {
			inputRateLimiter--;
			return;
		}

		// Allow access to mouse coordinates for multiple inputs
		mouseX = InputControllerManager.getInstance().getController(0).getCrossHair().x;
		mouseY = InputControllerManager.getInstance().getController(0).getCrossHair().y;



		if(InputControllerManager.getInstance().getController(0).isShiftKeyPressed()) {
			// Check for pan
			if (mouseX < GUI_LEFT_BAR_WIDTH / worldScale.x ) {
				// Pan left
				adjustedCxCamera -= CAMERA_PAN_SPEED/worldScale.x;
			}
			if (mouseY < GUI_LOWER_BAR_HEIGHT / worldScale.y ) {
				// down
				adjustedCyCamera -= CAMERA_PAN_SPEED/worldScale.y;
			}
			if (mouseX > (bounds.getWidth() ) - 1) {
				adjustedCxCamera += CAMERA_PAN_SPEED/worldScale.x;
			}
			if (mouseY > (bounds.getHeight() ) - 1) {
				adjustedCyCamera += CAMERA_PAN_SPEED/worldScale.y;
			}
			if(InputControllerManager.getInstance().getController(0).isSpaceKeyPressed()){
				adjustedCxCamera = 0;
				adjustedCyCamera = 0;
			}
			camUpdate();
		}

		adjustedMouseX = mouseX + cxCamera ;
		adjustedMouseY = mouseY + cyCamera ;
		if(snapping){
			adjustedMouseX = Math.round(adjustedMouseX);
			adjustedMouseY = Math.round(adjustedMouseY);

		}


		// Left Click
		if (InputControllerManager.getInstance().getController(0).didLeftClick()) {
			if(!processButtons(buttons.findButton(mouseX * worldScale.x, mouseY * worldScale.y))) {

				if (mouseX * worldScale.x <= GUI_LEFT_BAR_WIDTH) {

				} else if (mouseY * worldScale.y <= GUI_LOWER_BAR_HEIGHT) {

					int button = getEntityMenuButton(mouseX * worldScale.x, mouseY * worldScale.y);
					if (button == -2) {
						//do nothing
					} else if (button == -1) {
						if (entityTree.current.parent != null) {
							entityTree.upFolder();
							creating = false;
							selected = null;
						}
					} else {
						if (!entityTree.current.children.get(button).isLeaf) {
							entityTree.setCurrent(entityTree.current.children.get(button));
							creating = false;
							selected = null;
						} else {
							selected = createXY(entityTree.current.children.get(button), adjustedMouseX, adjustedMouseY);
							if (selected != null) {
								selected.setTextures(getMantisAssetManager());
								creating = true;
							}
						}
					}
				} else {
					creating = false;
					dragging = false;
					selected = entityQuery();
					if (selected != null) {
						//createXY(creationOptions[entityIndex], adjustedMouseX,adjustedMouseY);
					}
				}

			}
		}

		if(InputControllerManager.getInstance().getController(0).didLeftDrag()){
			if(mouseX* worldScale.x <= GUI_LEFT_BAR_WIDTH ){

			}else if(mouseY * worldScale.y <= GUI_LOWER_BAR_HEIGHT){

			} else {
				dragging = true;
				if(selected != null){
					selected.setPosition(adjustedMouseX, adjustedMouseY);
					if(movefar){
						selected.setModifiedPosition( adjustedMouseX, adjustedMouseY, adjustedCxCamera, adjustedCyCamera);
					}
					selected.setTextures(getMantisAssetManager());
				}else{
					// find nearest wall, custom entity query
					float dist = Float.MAX_VALUE;
					WallModel wm = null;
					float bdx = 0;
					float bdy = 0;
					for (Entity e : objects) {
						if (e instanceof WallModel) {
							WallModel eWall = (WallModel) e;
							float dx = (eWall.getModelX() - adjustedMouseX);
							float dy = (eWall.getModelY() - adjustedMouseY);
							float newDst = (float) Math.sqrt(dx*dx + dy*dy);
							if (newDst < dist) {
								dist = newDst;
								wm = eWall;
								bdx = dx;
								bdy = dy;
							}
						}
					}

					bdx = -bdx;
					bdy = -bdy;
					if (wm != null && released) {
						if (InputControllerManager.getInstance().getController(0).isAltKeyPressed()) {
							// pinch move
							wm.pinchMove(bdx,bdy);
//							released = false;
						} else if (InputControllerManager.getInstance().getController(0).isDotKeyPressed()) {
							// delete
							wm.pinchDelete(bdx,bdy);
							released = false;

						} else if (InputControllerManager.getInstance().getController(0).isRShiftKeyPressed()) {
							// pinch create
							wm.pinchCreate(bdx,bdy);
							released = false;
						}
					}
				}
			}
		}
		if(InputControllerManager.getInstance().getController(0).didLeftRelease()){
			released= true;
			if(mouseX* worldScale.x <= GUI_LEFT_BAR_WIDTH ){

			}else if(mouseY * worldScale.y <= GUI_LOWER_BAR_HEIGHT){

			}else{
				if(selected != null){
					if(dragging) {
						dragging = false;
						if (selected != null) {
							selected.setPosition(adjustedMouseX, adjustedMouseY);
							if(movefar){
								selected.setModifiedPosition( adjustedMouseX, adjustedMouseY, adjustedCxCamera, adjustedCyCamera);
							}
							selected.setTextures(getMantisAssetManager());
						}
						if (creating) {
							promptTemplate(selected);
						}
					}
				}else {

				}
			}
			creating = false;
		}

		// Help
		if (InputControllerManager.getInstance().getController(0).isHKeyPressed()) {
			showHelp = !showHelp;
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Grid
		if (InputControllerManager.getInstance().getController(0).isTKeyPressed()) {
			shouldDrawGrid = !shouldDrawGrid;
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Background
		if (InputControllerManager.getInstance().getController(0).isBKeyPressed()) {
			levelModel.setBackground(showInputDialog("What texture should the background be set to?"));
			// TODO: Update the drawn background (after henry implements the engine)
			background = getMantisAssetManager().get("texture/background/background1.png");

		}

		// Playtest
		if (InputControllerManager.getInstance().getController(0).isEKeyPressed()) {
			gmc.setLevel(currentLevel);
			saveLevel();
			listener.exitScreen(this, EXIT_LE_GM);
		}
	}

	@Override
	public void postUpdate(float dt) {

		// Turn the physics engine crank.
		//world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		for (Entity ent :objects){

			if(ent instanceof Obstacle){
				Obstacle obj  = (Obstacle)ent;
				if (obj.isRemoved()) {
					obj.deactivatePhysics(world);
					objects.remove(ent);
					continue;
				}
			}
			ent.update(dt); // called last!
		}
	}


	//region Draw and Helpers
	private void drawGridLines() {
		// debug lines
		if (!shouldDrawGrid)
			return;
		canvas.begin(camTrans);
		Gdx.gl.glLineWidth(1);
		// vertical
		float dpsW = ((canvas.getWidth()) / bounds.width);
		float dpsH = ((canvas.getHeight()) / bounds.height);

		gridLineRenderer.begin(ShapeRenderer.ShapeType.Line);
		for (float i = ((int)(-cxCamera * worldScale.x) % dpsW - dpsW); i < canvas.getWidth(); i += dpsW) {
			gridLineRenderer.setColor(Color.FOREST);
			gridLineRenderer.line(i, 0,i,canvas.getHeight());
		}

		// horizontal
		for (float i = ((int)(-cyCamera * worldScale.y) % dpsH - dpsH); i < canvas.getHeight(); i += dpsH) {
			gridLineRenderer.setColor(Color.FOREST);
			gridLineRenderer.line(0, i,canvas.getWidth(),i);
		}
		gridLineRenderer.end();
		// While we're at it, just draw the level bounds.
		gridLineRenderer.setColor(Color.RED);
		Gdx.gl.glLineWidth(4);
		gridLineRenderer.begin(ShapeRenderer.ShapeType.Line);
		float minPixelsX = levelModel.getMinX() * worldScale.x - cxCamera *
				worldScale.x;
		float minPixelsY = levelModel.getMinY() * worldScale.y - cyCamera *
				worldScale.y;
		float maxPixelsX = levelModel.getMaxX() * worldScale.x - cxCamera *
				worldScale.x;
		float maxPixelsY = levelModel.getMaxY() * worldScale.y - cyCamera *
				worldScale.y;

		// left line
		gridLineRenderer.line(minPixelsX, maxPixelsY,minPixelsX,minPixelsY);
		// top line
		gridLineRenderer.line(minPixelsX, maxPixelsY,maxPixelsX,maxPixelsY);
		// right line
		gridLineRenderer.line(maxPixelsX, maxPixelsY,maxPixelsX,minPixelsY);
		// bottom line
		gridLineRenderer.line(minPixelsX, minPixelsY,maxPixelsX,minPixelsY);


		gridLineRenderer.end();
		canvas.end();
	}

	private void drawEntitySelector(){
		circleShape.setRadius(MAX_SNAP_DISTANCE);
		Gdx.gl.glLineWidth(5);
		canvas.beginDebug(camTrans);
		Entity ent = entityQuery();


		if(ent!= null) {
			temp = ent.getPosition();
			if(movefar){
				temp = ent.getModifiedPosition(adjustedCxCamera,adjustedCyCamera);
			}
			canvas.drawPhysics(circleShape, new Color(0xcfcf000f), temp.x, temp.y, worldScale.x, worldScale.y);
		}

		circleShape.setRadius(.05f);
		for(Entity e : objects){
			temp = e.getPosition();
			if(movefar){
				temp = e.getModifiedPosition(adjustedCxCamera,adjustedCyCamera);
			}
			//if(e.getPosition().x * worldScale.x  > GUI_LEFT_BAR_WIDTH && )
			canvas.drawPhysics(circleShape, new Color(0xcfcf000f),temp.x , temp.y ,worldScale.x,worldScale.y );
			if(e instanceof BackgroundEntity){
				float offsetx = ((e.getPosition().x - adjustedCxCamera) * worldScale.x) / ((BackgroundEntity) e).getDepth();
				float offsety = ((e.getPosition().y - adjustedCyCamera) * worldScale.y) / ((BackgroundEntity) e).getDepth();
				canvas.drawLine(temp.x *worldScale.x, temp.y *worldScale.y , adjustedCxCamera*worldScale.x  + offsetx, adjustedCyCamera*worldScale.y + offsety, Color.YELLOW, Color.CHARTREUSE);
			}
		}

		canvas.endDebug();

	}


	//ox and oy are bottom left corner
	public boolean inBounds(float x ,float y, float ox ,float oy, float width, float height){
		return  x >= ox && x <= ox + width && y >= oy && y <= oy + height;
	}

	private int getEntityMenuButton(float mousex, float mousey){
		float margin = 18f;
		float startx = GUI_LEFT_BAR_WIDTH + margin;
		float starty = GUI_LOWER_BAR_HEIGHT - margin;
		float sizex = 64f;
		float sizey = 64f;

		if(inBounds(mousex,mousey,startx,starty-sizey,sizex,sizey)) {
			return -1;
		}

		for(int i = 0; i < entityTree.current.children.size(); i++) {
			float x = startx + ((i + 1) * (sizex + margin));
			float y = starty - sizey;
			if (inBounds(mousex, mousey, x, y, sizex, sizey)) {
				return i;
			}

		}

		return  -2;

	}

	private void drawEntityMenu(){
		int numChildren = entityTree.current.children.size();

		float margin = 18f;
		float startx = GUI_LEFT_BAR_WIDTH + margin;
		float starty = GUI_LOWER_BAR_HEIGHT - margin;
		float sizex = 64f;
		float sizey = 64f;

		float oneUnit = sizex + margin;
		float totalWidth = canvas.getWidth() - GUI_LEFT_BAR_WIDTH;
		float widthMinusArrows = totalWidth - margin * 2;


		float mousex = mouseX * worldScale.x;
		float mousey = mouseY * worldScale.y;

		Texture tex = upFolder;

		if(entityTree.current.parent == null){
			tex = placeholder;
		}
		if(inBounds(mousex,mousey,startx,starty-sizey,sizex,sizey)){
			canvas.draw(yellowbox ,Color.WHITE,0,0,startx - 3f ,starty - sizey -3f ,0,(sizex+6f) /yellowbox.getWidth(), (sizey + 6f)/yellowbox.getHeight());
		}
		canvas.draw(tex ,Color.WHITE,0,tex.getHeight(),startx ,starty,0,sizex /tex.getWidth(), sizey/tex.getHeight());

		for(int i = 0; i < entityTree.current.children.size(); i++){

			tex = entityTree.current.children.get(i).texture;
			if (!entityTree.current.children.get(i).isLeaf){
				tex = folder;
			}
			float x = startx + ((i + 1) * (sizex + margin));
			float y = starty - sizey;
			if(inBounds(mousex,mousey,x,y,sizex,sizey)){
				canvas.draw(yellowbox ,Color.WHITE,0,0,x - 3f ,y-3f ,0,(sizex+6f) /yellowbox.getWidth(), (sizey + 6f)/yellowbox.getHeight());
			}
			canvas.draw(tex ,Color.WHITE,0,0,x ,y,0,sizex /tex.getWidth(), sizey/tex.getHeight());
			canvas.drawTextStandard(entityTree.current.children.get(i).name, x, y - 10f);

		}


	}

	private void drawSelected(){

		circleShape.setRadius(MAX_SNAP_DISTANCE);
		Gdx.gl.glLineWidth(5);
		canvas.beginDebug(camTrans);
		Entity ent = selected;
		if(ent!= null) {
			temp = ent.getPosition();
			if (movefar)
				temp = ent.getModifiedPosition(adjustedCxCamera,adjustedCyCamera);
			canvas.drawPhysics(circleShape, new Color(Color.FIREBRICK), temp.x, temp.y, worldScale.x, worldScale.y);
		}
		canvas.endDebug();
	}

	private void drawButtons(){
		buttons.setTextures(mantisAssetManager);
		buttons.draw(canvas, mouseX * worldScale.x, mouseY * worldScale.y );
	}

	private void drawGUI(){
		canvas.begin();
		canvas.draw(grey,Color.WHITE,0,0,0,0,0,GUI_LEFT_BAR_WIDTH /grey.getWidth(), ((float)canvas.getHeight())/grey.getHeight());
		canvas.draw(grey,Color.WHITE,0,0,GUI_LEFT_BAR_WIDTH,0,0,((float)canvas.getWidth() - GUI_LEFT_BAR_WIDTH) /grey.getWidth(), GUI_LOWER_BAR_HEIGHT/grey.getHeight());
		//canvas.draw(grey,Color.WHITE,0,0,0,0,0,2.0f * worldScale.x /grey.getWidth(), 9.0f * worldScale.y/grey.getHeight());

		drawEntityMenu();
		drawButtons();
		canvas.end();
	}

	private void drawHelp(){
		// Text- independent of where you scroll
		canvas.begin(); // DO NOT SCALE
		if (showHelp) {
			String[] splitHelp = HELP_TEXT.split("\\R");
			float beginY = GUI_LOWER_BAR_HEIGHT + (9f * worldScale.y);
			for (int i = 0; i < splitHelp.length; i++) {
				canvas.drawTextStandard(splitHelp[i], GUI_LEFT_BAR_WIDTH + 10, beginY);
				beginY -= .3 * worldScale.y;
			}
		}


		canvas.drawTextStandard("MOUSE: " + adjustedMouseX + " , " + adjustedMouseY, GUI_LEFT_BAR_WIDTH + 10, GUI_LOWER_BAR_HEIGHT + (1.4f * worldScale.y));
		canvas.drawTextStandard(cxCamera + "," + cyCamera , GUI_LEFT_BAR_WIDTH + 10, GUI_LOWER_BAR_HEIGHT + (1.1f * worldScale.y));
		canvas.drawTextStandard("Level: " + currentLevel, GUI_LEFT_BAR_WIDTH + 10, GUI_LOWER_BAR_HEIGHT + (.8f * worldScale.y));

		canvas.end();
	}

	@Override
	public void draw(float delta) {
		canvas.clear();

		//draw background
		canvas.begin();
		canvas.draw(background);
		canvas.end();

		// Translate camera to cx, cy
		camTrans.setToTranslation(0,0);
		camTrans.setToTranslation(-cxCamera * worldScale.x, -cyCamera* worldScale.y);

		Vector2 pos = canvas.getCampos();
		pos.set(adjustedCxCamera * worldScale.x ,adjustedCyCamera * worldScale.y);
		canvas.begin(camTrans);
		Collections.sort(objects);
		int s = objects.size();
		for (int i = 0; i < s; i++) {
			if (i < objects.size()) {
				Entity obj = objects.get(i);
				if (obj != null) {
					obj.setDrawScale(worldScale);
					obj.draw(canvas);
				}
			}
		}
		canvas.end();


		canvas.font.setColor(Color.GOLDENROD);
		drawGridLines();
		drawEntitySelector();
		drawGUI();
		drawSelected();
		drawHelp();

	}
	//endregion

	//region JSON Stuff
	private void makeGuiWindow() {
		didLoad = true;
		if (editorWindow != null) {
			editorWindow.dispose();
		}
		//GUI Mode Enabled
		//Prevent multiple windows from being created
		//Window Settings
		editorWindow = new JFrame();
		GridLayout gridLayout = new GridLayout(18,2);
		gridLayout.setVgap(2);
		gridLayout.setHgap(10);

		editorWindow.setLayout(gridLayout);

		JsonObject levelJson = jsonLoaderSaver.gsonToJsonObject(levelModel);

		//Load/Save/LevelName
		JButton loadButton = new JButton("Load");
		JButton saveButton = new JButton("Save");
		JLabel fileLabel = new JLabel("File Name");
		JTextField fileName = new JTextField(currentLevel);

		loadButton.setSize(BUTTON_WIDTH,BUTTON_HEIGHT);
		saveButton.setSize(BUTTON_WIDTH,BUTTON_HEIGHT);

		loadButton.addActionListener(e -> {
			loadLevel(fileName.getText());
		});

		saveButton.addActionListener(e -> {
			currentLevel = fileName.getText();
			saveLevel();
		});

		editorWindow.add(fileLabel);
		editorWindow.add(fileName);
		editorWindow.add(loadButton);
		editorWindow.add(saveButton);

		generateSwingPropertiesForEntity(levelJson,editorWindow,0);

		editorWindow.setSize(canvas.getWidth() * 3 / 5, canvas.getHeight() * 2 / 3);

		//TODO Add ability to edit entity parameters (on click/selecting only?)

		JLabel editEntityHeader = new JLabel("Click a Stage Entity to Edit It");

		editEntityHeader.setSize(250, TEXT_HEIGHT1);

		editorWindow.add(editEntityHeader);

		//Display Everything
		editorWindow.setVisible(true);
	}

	private void generateSwingPropertiesForEntity(JsonObject e, JFrame jPanel, int rowNum) {
		for(Map.Entry<String,JsonElement> entry : e.entrySet()) {
			// Add key
			String key = entry.getKey();
			JLabel paramText = new JLabel(key + ":");

			JComponent valueComponent;
			// Add value based on type
			JsonElement value = entry.getValue();
			StringBuilder theArray = new StringBuilder();
			if (value.isJsonArray()) {
				if (key.equals("entities")) continue;
				boolean newLineTime = false;
				for (JsonElement x : value.getAsJsonArray()) {
					theArray.append(x.getAsString());
					theArray.append(", ");
					if (newLineTime) theArray.append("\n");
					newLineTime = !newLineTime;
				}
				valueComponent = new JTextArea(theArray.toString());
			} else if (value.isJsonPrimitive()) {
				JsonPrimitive primitive = value.getAsJsonPrimitive();
				if (primitive.isBoolean()) {
					valueComponent = new JCheckBox("",primitive.getAsBoolean());
				} else if (primitive.isNumber()) {
					valueComponent = new JTextField(primitive.getAsNumber().toString());
				} else if (primitive.isString()) {
					valueComponent = new JTextField(primitive.getAsString());
				} else {
					System.err.println("Unknown primitive type: " + entry);
					continue;
				}
			} else if (value.isJsonObject()) {
				System.err.println("Unknown object type: " + entry);
				continue;
			} else {
				System.err.println("Unknown type: " + entry);
				continue;
			}

			paramText.setSize(FIELD_TEXT_WIDTH, TEXT_HEIGHT);
			valueComponent.setSize(FIELD_BOX_WIDTH, TEXT_HEIGHT);

			// Update panel with key, value
			jPanel.add(paramText);
			jPanel.add(valueComponent);
			rowNum++;
		}

	}

	private void loadLevel(String toLoad){
		currentLevel = toLoad;
		try {
			Thread.sleep(250);
			reset();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void saveLevel(){
		System.out.println("Saving...");
		LevelModel timeToSave;
//		if (!vimMode) {
		// Grab params from gui
		JsonObject levelJson = jsonLoaderSaver.gsonToJsonObject(levelModel);
		if (editorWindow != null)
			grabUpdatedObjectValuesFromGUI(levelJson,editorWindow.getRootPane().getContentPane());

		timeToSave = jsonLoaderSaver.levelFromJson(levelJson);

		if (timeToSave.entities != null)
			timeToSave.entities.clear();
		else
			timeToSave.entities = new ArrayList<>();

		// copy to avoid concurrent modification
		ArrayList<Entity> copy = new ArrayList<>(objects);
		for (Entity o : copy) {
			timeToSave.addEntity(o);
		}
		if (jsonLoaderSaver.saveLevel(timeToSave, currentLevel)) {
			RecordBook.getInstance().resetRecord(currentLevel);
			System.out.println("Saved!");
		} else {
			System.err.println("ERROR IN SAVE");
		}
		inputRateLimiter = UI_WAIT_LONG;
	}

	private void grabUpdatedObjectValuesFromGUI(JsonObject entityProp, Container p) {
		for(Map.Entry<String,JsonElement> entry : entityProp.entrySet()) {
			// Add key
			String key = entry.getKey();

			JsonElement value = entry.getValue();
			if (value.isJsonArray()) {
				entityProp.add(key,findInPanel(key,p));
			} else if (value.isJsonPrimitive()) {
				entityProp.add(key,findInPanel(key,p));
			} else if (value.isJsonObject()) {
				System.err.println("Unknown object type: " + entry);
			} else {
				System.err.println("Unknown type: " + entry);
			}
		}
	}

	private JsonElement findInPanel(String key, Container panel) {
		boolean grabNext = false;
		for (Component c : panel.getComponents()) {
			if (grabNext) {
				if (c instanceof JCheckBox) {
					return new JsonPrimitive(((JCheckBox)c).isSelected());
				} else if (c instanceof JTextField) {
					return new JsonPrimitive((((JTextField)c).getText()));
				} else if (c instanceof JTextArea) {
					String text = ((JTextArea)c).getText();
					JsonArray jarr = new JsonArray();
					String[] split = text.split(",");
					for (String s : split) {
						jarr.add(new JsonPrimitive(Float.parseFloat(s)));
					}
					return jarr;
				} else {
					System.err.println("UNKNOWN FOR " + key);
					return null;
				}
			}
			if (c instanceof JLabel) {
				if (((JLabel) c).getText().equals(key+":")) {
					grabNext = true;
				}
			}
		}
		System.err.println("CANT FIND " + key);
		return null;
	}

	private void promptTemplate(Entity template) {
		if (!prompting) {
			prompting = true; //Use different constant? Can just use the same one?

			JDialog entityDisplay = new JDialog();
			entityDisplay.setUndecorated(true);
			entityDisplay.setSize(600,600);
//			entityDisplay.setLocationRelativeTo(null);
			entityDisplay.toFront();
			JPanel panel = makeEntityWindow(template,entityDisplay);

			entityDisplay.add(panel);
			entityDisplay.setVisible(true);
		}
	}

	private JPanel makeEntityWindow(Entity template, JDialog parentWindow){
		JsonObject entityObject = jsonLoaderSaver.gsonToJsonObject(template);

		JsonObject entityProp = entityObject.get("INSTANCE").getAsJsonObject();
		String entityName = entityObject.get("CLASSNAME").getAsString();
		entityName = entityName.substring(entityName.lastIndexOf("."));

		JPanel panel = new JPanel();
		panel.setLayout(null);

		int rowNum = 0;

		JLabel header = new JLabel(entityName+" Properties (Please hit OK instead of X to closeout window)");
		JButton okButton = new JButton("OK");
		JButton deleteButton = new JButton("Delete Entity");

		//Define top elements
		header.setBounds(BUFFER, BUFFER, 500, TEXT_HEIGHT);
		rowNum++;
		deleteButton.setBounds(BUFFER, (rowNum*TEXT_HEIGHT)+((rowNum+1)* BUFFER), 150, TEXT_HEIGHT);
		rowNum++;

		// Add properties
		rowNum = generateSwingPropertiesForEntity(entityProp,panel,rowNum);

		//Add okay button
		okButton.addActionListener(e -> {
			grabUpdatedObjectValuesFromGUI(entityProp,panel);
			entityObject.add("INSTANCE", entityProp);
			objects.remove(template);

			//Get the string form of the entityObject
			String stringJson = jsonLoaderSaver.stringFromJson(entityObject);
			promptTemplateCallback(stringJson);

			parentWindow.setVisible(false);
			parentWindow.dispose();
			prompting = false;
		});

		okButton.setBounds(125, ((rowNum+1)*TEXT_HEIGHT)+((rowNum+1)* BUFFER), 100, TEXT_HEIGHT);

		deleteButton.addActionListener(e -> {
			deleteEntity(template);
			parentWindow.setVisible(false);
			parentWindow.dispose();
			prompting = false;
		});

		panel.add(header);
		panel.add(okButton);
		panel.add(deleteButton);

		return panel;
	}

	private int generateSwingPropertiesForEntity(JsonObject e, JPanel jPanel, int rowNum) {
		for(Map.Entry<String,JsonElement> entry : e.entrySet()) {
			// Add key
			String key = entry.getKey();
			JLabel paramText = new JLabel(key + ":");

			JComponent valueComponent;
			// Add value based on type
			JsonElement value = entry.getValue();
			if (value.isJsonArray()) {
				StringBuilder theArray = new StringBuilder();
				boolean newLineTime = false;
				for (JsonElement x : value.getAsJsonArray()) {
					theArray.append(x.getAsString());
					theArray.append(", ");
					if (newLineTime) theArray.append(System.lineSeparator());
					newLineTime = !newLineTime;
				}
				String done = theArray.toString();
				int doneLength = done.length();
				if (doneLength > 0) {
					done = done.substring(0,doneLength-2-System.lineSeparator().length());
				}
				valueComponent = new JTextArea(done, 40,16);
				valueComponent.setBounds((3 * BUFFER) + FIELD_TEXT_WIDTH, (rowNum * TEXT_HEIGHT) + ((rowNum + 1) * BUFFER), FIELD_BOX_WIDTH, TEXT_HEIGHT * 16);
			} else if (value.isJsonPrimitive()) {
				JsonPrimitive primitive = value.getAsJsonPrimitive();
				if (primitive.isBoolean()) {
					valueComponent = new JCheckBox("",primitive.getAsBoolean());
				} else if (primitive.isNumber()) {
					valueComponent = new JTextField(primitive.getAsNumber().toString());
				} else if (primitive.isString()) {
					valueComponent = new JTextField(primitive.getAsString());
				} else {
					System.err.println("Unknown primitive type: " + entry);
					continue;
				}
				valueComponent.setBounds((3 * BUFFER) + FIELD_TEXT_WIDTH, (rowNum * TEXT_HEIGHT) + ((rowNum + 1) * BUFFER), FIELD_BOX_WIDTH, TEXT_HEIGHT);
			} else if (value.isJsonObject()) {
				System.err.println("Unknown object type: " + entry);
				continue;
			} else {
				System.err.println("Unknown type: " + entry);
				continue;
			}

			paramText.setBounds((2 * BUFFER), (rowNum * TEXT_HEIGHT) + ((rowNum + 1) * BUFFER), FIELD_TEXT_WIDTH, TEXT_HEIGHT);

			// Update panel with key, value
			jPanel.add(paramText);
			jPanel.add(valueComponent);
			rowNum++;
		}

		return rowNum;
	}

	private void promptTemplateCallback(String json) {
		Entity toAdd = jsonLoaderSaver.entityFromJson(json);
		addObject( toAdd);
		prompting = false;
	}

	private void promptGlobalConfig() {
		if (!prompting) {
			prompting = true;
			String jsonOfConfig = jsonLoaderSaver.prettyJson(JSONLoaderSaver
					.loadArbitrary("data/config.json").orElseGet
							(JsonObject::new));
			JDialog mainFrame = new JDialog();
			mainFrame.setSize(600,600);
			mainFrame.setLocationRelativeTo(null);
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout());
			final JTextArea commentTextArea =
					new JTextArea(jsonOfConfig,20,30);
			panel.add(commentTextArea);
			mainFrame.add(panel);
			JButton okButton = new JButton("OK");
			okButton.addActionListener(e -> {
				JSONLoaderSaver.saveArbitrary("data/config.json",commentTextArea
						.getText());
				GlobalConfiguration.update();
				mainFrame.setVisible(false);
				mainFrame.dispose();
				prompting = false;
			});
			panel.add(okButton);
			mainFrame.setVisible(true);
		}
	}
	//endregion
}
