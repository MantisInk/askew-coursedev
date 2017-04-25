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

import askew.GameCanvas;
import askew.GlobalConfiguration;
import askew.InputController;
import askew.MantisAssetManager;
import askew.entity.BackgroundEntity;
import askew.entity.Entity;
import askew.entity.ghost.GhostModel;
import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.owl.OwlModel;
import askew.entity.sloth.SlothModel;
import askew.entity.tree.PoleVault;
import askew.entity.tree.StiffBranch;
import askew.entity.tree.Trunk;
import askew.entity.vine.Vine;
import askew.entity.wall.WallModel;
import askew.playermode.WorldController;
import askew.util.PooledList;
import askew.util.json.JSONLoaderSaver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;

import static javax.swing.JOptionPane.showInputDialog;


/**
 * Gameplay specific controller for the platformer game.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class LevelEditorController extends WorldController {

	/** Track asset loading from all instances and subclasses */
	private AssetState levelEditorAssetState = AssetState.EMPTY;

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

	Affine2 camTrans;
	float cxCamera;
	float cyCamera;
	float adjustedCxCamera;
	float adjustedCyCamera;
	float mouseX;
	float mouseY;
	float adjustedMouseX;
	float adjustedMouseY;



	protected Vector2 oneScale;
	private transient CircleShape circleShape = new CircleShape();

	private boolean pressedL, prevPressedL;


	@Getter
	private String currentLevel;

	private String createClass;

	/** A decrementing int that helps prevent accidental repeats of actions through an arbitrary countdown */
	private int inputRateLimiter = 0;

	private int tentativeEntityIndex = 0;
	private int entityIndex = 0;

	public static final int UI_WAIT_SHORT = 2;
	public static final int UI_WAIT_LONG = 15;
	public static final int UI_WAIT_ETERNAL = 120;

	//In Pixels, divide by world scale for box2d.
	public static final float GUI_LOWER_BAR_HEIGHT = 200.f;
	public static final float GUI_LEFT_BAR_WIDTH = 400.f;

	public float MAX_SNAP_DISTANCE = 1f;
	public float CAMERA_PAN_SPEED = 20f;

	private EntityTree entityTree;
	private Entity selected;
	private boolean dragging = false;
	private boolean creating = false;



	public static final String[] creationOptions = {
			".SlothModel",
			".Vine",
			".PoleVault",
			".Trunk",
			".StiffBranch",
			".OwlModel",
			".WallModel",
			".OwlModel",
			".GhostModel",
			".BackgroundEntity"
	};

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
		levelEditorAssetState = AssetState.COMPLETE;
	}


	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public LevelEditorController() {
//		super(36,18,0); I want this scale but for the sake of alpha:
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,0);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		jsonLoaderSaver = new JSONLoaderSaver();
		entityTree = new EntityTree();
		currentLevel = "test_save_obstacle";
		createClass = ".SlothModel";
		showHelp = true;
		shouldDrawGrid = true;
		camTrans = new Affine2();
		oneScale = new Vector2(1,1);
		pressedL = false;
		prevPressedL = false;
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
		Vector2 gravity = new Vector2(world.getGravity() );

		for(Entity obj : objects) {
			if( (obj instanceof Obstacle))
				((Obstacle)obj).deactivatePhysics(world);
		}

		objects.clear();
		world.dispose();

		world = new World(gravity,false);
		setComplete(false);
		setFailure(false);
		populateLevel();

		adjustedCxCamera = 0;
		adjustedCyCamera = 0;
		camUpdate();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		try {
			levelModel = jsonLoaderSaver.loadLevel(currentLevel);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (levelModel == null) {
			levelModel = new LevelModel();
		}

		for (Entity o : levelModel.getEntities()) {

			addObject( o);

			//System.err.println("UNSUPPORTED: Adding non obstacle entity");

		}
	}

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

		InputController input = InputController.getInstance();
		prevPressedL = pressedL;
		pressedL = input.isLKeyPressed();
		if (input.didLeftButtonPress()) {
			System.out.println("GM");
			listener.exitScreen(this, EXIT_LE_GM);
			return false;
		} else if (input.didTopButtonPress()) {
			System.out.println("MM");
			listener.exitScreen(this, EXIT_LE_MM);
			return false;
		}

		return true;
	}

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


		//creationOptions[entityIndex]
		switch (name) {
			case "SlothModel":
				entity = new SlothModel(x,y);
				break;
			case "Vine":
				entity = new Vine(x,y,5.0f, 5f, -400f);
				break;
			case "Trunk":
				entity = new Trunk(x,y, 5.0f, 0.25f, 1.0f, 3.0f,oneScale, 0);
				break;
			case "PoleVault":
				entity = new PoleVault(x,y, 5.0f, 0.25f, 1.0f, oneScale, 0);
				break;
			case "StiffBranch":
				entity = new StiffBranch(x,y, 3.0f, 0.25f, 1.0f,oneScale);
				break;
			case "OwlModel":
				entity = new OwlModel(x,y);
				break;
			case "WallModel":
				entity = new WallModel(x,y,new float[] {0,0,0f,1f,1f,1f,1f,0f}, false);
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

	private void promptTemplate(Entity template) {
		if (!prompting) {
			prompting = true;
			template.fillJSON();
			String jsonOfTemplate = jsonLoaderSaver.gsonToJson(template);
			// flipping swing
			JDialog mainFrame = new JDialog();
			mainFrame.setSize(600,600);
			mainFrame.setLocationRelativeTo(null);
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout());
			final JTextArea commentTextArea =
					new JTextArea(jsonOfTemplate,20,30);
			panel.add(commentTextArea);
			mainFrame.add(panel);
			JButton okButton = new JButton("ok");
			okButton.addActionListener(e -> {
				promptTemplateCallback(commentTextArea.getText());
				mainFrame.setVisible(false);
				mainFrame.dispose();
			});
			panel.add(okButton);
			mainFrame.setVisible(true);
		}
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
			JButton okButton = new JButton("ok");
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

	public Entity entityQuery() {

		Entity found = null;
		Vector2 mouse = new Vector2(adjustedMouseX, adjustedMouseY);
		float minDistance = Float.MAX_VALUE;
		for (Entity e : objects) {
			float curDist = e.getPosition().dst(mouse);
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
		cxCamera = adjustedCxCamera + (((bounds.getWidth()-(GUI_LEFT_BAR_WIDTH/worldScale.x) )/2f)+(GUI_LEFT_BAR_WIDTH / worldScale.x) );
		cyCamera = adjustedCyCamera + (((bounds.getHeight()-(GUI_LOWER_BAR_HEIGHT/worldScale.y))/2f) + (GUI_LOWER_BAR_HEIGHT / worldScale.y));
		System.out.println(cxCamera + " : " +cyCamera);
	}

	public void update(float dt) {

		// Decrement rate limiter to allow new input
		if (inputRateLimiter > 0) {
			inputRateLimiter--;
			return;
		}

		// Allow access to mouse coordinates for multiple inputs
		mouseX = InputController.getInstance().getCrossHair().x;
		mouseY = InputController.getInstance().getCrossHair().y;

		adjustedMouseX = mouseX - cxCamera ;
		adjustedMouseY = mouseY - cyCamera ;

		if(InputController.getInstance().isShiftKeyPressed()) {
			// Check for pan
			if (mouseX < GUI_LEFT_BAR_WIDTH / worldScale.x ) {
				// Pan left
				adjustedCxCamera += CAMERA_PAN_SPEED/worldScale.x;
			}
			if (mouseY < GUI_LOWER_BAR_HEIGHT / worldScale.y ) {
				// down
				adjustedCyCamera += CAMERA_PAN_SPEED/worldScale.y;
			}
			if (mouseX > (16f ) - 1) {
				adjustedCxCamera -= CAMERA_PAN_SPEED/worldScale.x;
			}
			if (mouseY > (9f ) - 1) {
				adjustedCyCamera -= CAMERA_PAN_SPEED/worldScale.y;
			}
			if(InputController.getInstance().isSpaceKeyPressed()){
				adjustedCxCamera = 0;
				adjustedCyCamera = 0;
			}
			camUpdate();
		}


		// Left Click
		if (InputController.getInstance().didLeftClick()) {
			if(mouseX* worldScale.x <= GUI_LEFT_BAR_WIDTH ){

			}else if(mouseY * worldScale.y <= GUI_LOWER_BAR_HEIGHT){

				int button = getEntityMenuButton(mouseX * worldScale.x, mouseY * worldScale.y);
				if(button == -2){
					//do nothing
				} else if(button == -1){
					if(entityTree.current.parent != null){
						entityTree.upFolder();
					}
				} else{
					if(!entityTree.current.children.get(button).isLeaf){
						entityTree.setCurrent(entityTree.current.children.get(button));
					}else{
						selected = createXY(entityTree.current.children.get(button),adjustedMouseX, adjustedMouseY);
						if(selected != null) {
							selected.setTextures(getMantisAssetManager());
							creating = true;
						}
					}
				}


			}else{
				creating = false;
				dragging = false;
				selected = entityQuery();
				if(selected == null){
					//createXY(creationOptions[entityIndex], adjustedMouseX,adjustedMouseY);
				}
				/*
				if(selected instanceof BackgroundEntity){
					adjustedCxCamera = -adjustedMouseX;
					adjustedCyCamera = -adjustedMouseY;
					camUpdate();
				}*/

			}



		}

		if(InputController.getInstance().didLeftDrag()){
			if(mouseX* worldScale.x <= GUI_LEFT_BAR_WIDTH ){

			}else if(mouseY * worldScale.y <= GUI_LOWER_BAR_HEIGHT){

			}else{
				dragging = true;

				if(selected != null) {
					/*
					if(false && selected instanceof BackgroundEntity){
						if (mouseX < -adjustedCxCamera + 3 ) {
							// Pan left
							adjustedCxCamera += CAMERA_PAN_SPEED/worldScale.x;
						}
						if (mouseY < -adjustedCyCamera + 3 ) {
							// down
							adjustedCyCamera += CAMERA_PAN_SPEED/worldScale.y;
						}
						if (mouseX > -adjustedCxCamera - 3) {
							adjustedCxCamera -= CAMERA_PAN_SPEED/worldScale.x;
						}
						if (mouseY > -adjustedCyCamera - 3) {
							adjustedCyCamera -= CAMERA_PAN_SPEED/worldScale.y;
						}
						camUpdate();

						selected.setPosition(adjustedCxCamera, adjustedCyCamera);



					}
					else {*/
						selected.setPosition(adjustedMouseX, adjustedMouseY);
						if (selected instanceof ComplexObstacle) {
							((ComplexObstacle) selected).rebuild(adjustedMouseX, adjustedMouseY);
							selected.setTextures(getMantisAssetManager());
						//}
					}


				}
				//get offset
				//display stuff related to it

			}

		}

		if(InputController.getInstance().didLeftRelease()){
			if(mouseX* worldScale.x <= GUI_LEFT_BAR_WIDTH ){

			}else if(mouseY * worldScale.y <= GUI_LOWER_BAR_HEIGHT){

			}else{

				if(dragging) {
					dragging = false;
					if (selected != null) {
						if(selected instanceof BackgroundEntity){
							selected.setPosition(adjustedMouseX, adjustedMouseY);
						}
						else {
							selected.setPosition(adjustedMouseX, adjustedMouseY);
							if (selected instanceof ComplexObstacle) {

								System.out.println("move complex");
								((ComplexObstacle) selected).rebuild(adjustedMouseX, adjustedMouseY);
								selected.setTextures(getMantisAssetManager());
							}
						}
					}
					if (creating) {
						promptTemplate(selected);
					}
					selected = null;
				}
				else{


				}

			}

		}

		// Delete
		if (InputController.getInstance().isRightClickPressed()) {
			Entity select = entityQuery();
			if (select != null) objects.remove(select);
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Edit
		if (InputController.getInstance().isEKeyPressed()) {
			Entity select = entityQuery();
			if (select != null) {
				promptTemplate(select);
				objects.remove(select);
			}
			inputRateLimiter = UI_WAIT_SHORT;
		}

		// Name level
		if (InputController.getInstance().isNKeyPressed()) {
			currentLevel = showInputDialog("What should we call this level?");
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Load level
		if (pressedL && !prevPressedL) {
			if (!loadingLevelPrompt) {
				loadingLevelPrompt = true;
				currentLevel = showInputDialog("What level do you want to load?");
				loadingLevelPrompt = false;
				reset();
			}
			inputRateLimiter = UI_WAIT_ETERNAL;
		}

		// Save
		if (InputController.getInstance().isSKeyPressed()) {
			System.out.println("Saving...");
			LevelModel timeToSave = new LevelModel();
			timeToSave.setTitle(currentLevel);
			for (Entity o : objects) {
				timeToSave.addEntity(o);
			}
			if (jsonLoaderSaver.saveLevel(timeToSave, currentLevel)) {
				System.out.println("Saved!");
			} else {
				System.err.println("ERROR IN SAVE");
			}
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Scroll backward ent
		if (InputController.getInstance().isLeftKeyPressed()) {
			tentativeEntityIndex = (tentativeEntityIndex + 1 + creationOptions.length) % creationOptions.length;
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Scroll forward ent
		if (InputController.getInstance().isRightKeyPressed()) {
			tentativeEntityIndex = (tentativeEntityIndex - 1 + creationOptions.length) % creationOptions.length;
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Select ent
		if (InputController.getInstance().isEnterKeyPressed()) {
			entityIndex = tentativeEntityIndex;
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Help
		if (InputController.getInstance().isHKeyPressed()) {
			showHelp = !showHelp;
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Grid
		if (InputController.getInstance().isTKeyPressed()) {
			shouldDrawGrid = !shouldDrawGrid;
			inputRateLimiter = UI_WAIT_LONG;
		}

		// Background
		if (InputController.getInstance().isBKeyPressed()) {
			levelModel.setBackground(showInputDialog("What texture should the background be set to?"));
			// TODO: Update the drawn background (after henry implements the engine)
			background = getMantisAssetManager().get("texture/background/background1.png");

		}

		if (InputController.getInstance().isGKeyPressed()) {
			promptGlobalConfig();
		}
	}

	private void drawGridLines() {
		// debug lines
		Gdx.gl.glLineWidth(1);
		// vertical
		float dpsW = ((canvas.getWidth()) / bounds.width);
		float dpsH = ((canvas.getHeight()) / bounds.height);

		for (float i = ((int)(cxCamera * worldScale.x) % dpsW - dpsW); i < canvas.getWidth(); i += dpsW) {
			gridLineRenderer.begin(ShapeRenderer.ShapeType.Line);
			gridLineRenderer.setColor(Color.FOREST);
			gridLineRenderer.line(i, 0,i,canvas.getHeight());
			gridLineRenderer.end();
		}

		// horizontal
		for (float i = ((int)(cyCamera * worldScale.x) % dpsH - dpsH); i < canvas.getHeight(); i += dpsH) {
			gridLineRenderer.begin(ShapeRenderer.ShapeType.Line);
			gridLineRenderer.setColor(Color.FOREST);
			gridLineRenderer.line(0, i,canvas.getWidth(),i);
			gridLineRenderer.end();
		}
	}

	private void drawEntitySelector(){
		circleShape.setRadius(MAX_SNAP_DISTANCE);
		Gdx.gl.glLineWidth(5);
		canvas.beginDebug(camTrans);
		Entity ent = entityQuery();
		if(ent!= null)
			canvas.drawPhysics(circleShape, new Color(0xcfcf000f),ent.getPosition().x , ent.getPosition().y ,worldScale.x,worldScale.y );

		circleShape.setRadius(.05f);
		for(Entity e : objects){
			canvas.drawPhysics(circleShape, new Color(0xcfcf000f),e.getPosition().x , e.getPosition().y ,worldScale.x,worldScale.y );
			if(e instanceof BackgroundEntity){
				float offsetx = ((e.getPosition().x + adjustedCxCamera) * worldScale.x) / ((BackgroundEntity) e).getDepth();
				float offsety = ((e.getPosition().y + adjustedCyCamera) * worldScale.y) / ((BackgroundEntity) e).getDepth();
				canvas.drawLine(e.getPosition().x *worldScale.x, e.getPosition().y *worldScale.y , -adjustedCxCamera*worldScale.x  + offsetx, -adjustedCyCamera*worldScale.y + offsety, Color.YELLOW, Color.CHARTREUSE);
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
		float margin = 18f;
		float startx = GUI_LEFT_BAR_WIDTH + margin;
		float starty = GUI_LOWER_BAR_HEIGHT - margin;
		float sizex = 64f;
		float sizey = 64f;

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

	private void drawGUI(){
		canvas.begin();
		canvas.draw(grey,Color.WHITE,0,0,0,0,0,GUI_LEFT_BAR_WIDTH /grey.getWidth(), ((float)canvas.getHeight())/grey.getHeight());
		canvas.draw(grey,Color.WHITE,0,0,GUI_LEFT_BAR_WIDTH,0,0,((float)canvas.getWidth() - GUI_LEFT_BAR_WIDTH) /grey.getWidth(), GUI_LOWER_BAR_HEIGHT/grey.getHeight());
		//canvas.draw(grey,Color.WHITE,0,0,0,0,0,2.0f * worldScale.x /grey.getWidth(), 9.0f * worldScale.y/grey.getHeight());

		drawEntityMenu();
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
		camTrans.setToTranslation(cxCamera * worldScale.x, cyCamera* worldScale.y);

		Vector2 pos = canvas.getCampos();
		pos.set(-adjustedCxCamera * worldScale.x ,-adjustedCyCamera * worldScale.y);
		canvas.begin(camTrans);
		Collections.sort(objects);
		for(Entity obj : objects) {
			obj.setDrawScale(worldScale);
			obj.draw(canvas);
		}
		canvas.end();

		canvas.begin(camTrans);
		if (shouldDrawGrid) {
			drawGridLines();
		}
		canvas.end();
		drawGUI();

		// Text- independent of where you scroll
		canvas.begin(); // DO NOT SCALE
		if (showHelp) {
			String[] splitHelp = HELP_TEXT.split("\\R");
			float beginY = 7f * worldScale.y;
			for (int i = 0; i < splitHelp.length; i++) {
				canvas.drawTextStandard(splitHelp[i], GUI_LEFT_BAR_WIDTH + 10, beginY);
				beginY -= .2 * worldScale.y;
			}
		}


		canvas.drawTextStandard("MOUSE: " + adjustedMouseX + " , " + adjustedMouseY, GUI_LEFT_BAR_WIDTH + 10, 2.8f * worldScale.y);
		canvas.drawTextStandard(-adjustedCxCamera + "," + -adjustedCyCamera , GUI_LEFT_BAR_WIDTH + 10, 2.6f * worldScale.y);
		canvas.drawTextStandard("Level: " + currentLevel, GUI_LEFT_BAR_WIDTH + 10, 2.4f * worldScale.y);
		canvas.drawTextStandard("Creating: " + creationOptions[tentativeEntityIndex], GUI_LEFT_BAR_WIDTH + 10, 2.2f * worldScale.y);
		if (tentativeEntityIndex != entityIndex) {
			canvas.drawTextStandard("Hit Enter to Select New Object Type.", GUI_LEFT_BAR_WIDTH + 10, 2f * worldScale.y);
		}
		canvas.end();

		drawEntitySelector();



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

	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}

	@Override
	public void setCanvas(GameCanvas canvas) {
		// unscale
		this.canvas = canvas;
		this.worldScale.x = 1.0f * canvas.getWidth()/bounds.getWidth();
		this.worldScale.y = 1.0f * canvas.getHeight()/bounds.getHeight();
		jsonLoaderSaver.setScale(this.worldScale);
	}
}
