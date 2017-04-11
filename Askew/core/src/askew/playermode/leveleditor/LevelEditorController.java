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
import askew.entity.Entity;
import askew.entity.ghost.GhostModel;
import askew.entity.owl.OwlModel;
import askew.entity.wall.WallModel;
import askew.util.json.JSONLoaderSaver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.google.gson.JsonObject;
import askew.playermode.WorldController;
import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.tree.Trunk;
import askew.entity.tree.StiffBranch;
import askew.entity.tree.Tree;
import askew.entity.vine.Vine;
import askew.entity.sloth.SlothModel;
import askew.util.PooledList;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
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

	private LevelModel levelModel;

	private static ShapeRenderer gridLineRenderer = new ShapeRenderer();

	Affine2 camTrans;
	float cxCamera;
	float cyCamera;
	float adjustedMouseX;
	float adjustedMouseY;

	protected Vector2 oneScale;


	@Getter
	private String currentLevel;

	private String createClass;

	/** A decrementing int that helps prevent accidental repeats of actions through an arbitrary countdown */
	private int inputRateLimiter = 0;

	private int tentativeEntityIndex = 0;
	private int entityIndex = 0;

	public static final int UI_WAIT_SHORT = 2;
	public static final int UI_WAIT_LONG = 15;


	public static final String[] creationOptions = {
			".SlothModel",
			".Vine",
			".Platform",
			".Trunk",
			".StiffBranch",
			".OwlModel",
			".WallModel",
			".Tree",
			".OwlModel",
			".GhostModel"
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
		if (levelEditorAssetState != AssetState.LOADING) {
			return;
		}

		super.loadContent(manager);
		levelEditorAssetState = AssetState.COMPLETE;
	}

	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public LevelEditorController() {
		super(36,18,0);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		jsonLoaderSaver = new JSONLoaderSaver();
		currentLevel = "test_save_obstacle";
		createClass = ".SlothModel";
		showHelp = true;
		shouldDrawGrid = true;
		camTrans = new Affine2();
		oneScale = new Vector2(1,1);

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
		addQueue.clear();
		world.dispose();

		world = new World(gravity,false);
		setComplete(false);
		setFailure(false);
		populateLevel();
		cxCamera = -canvas.getWidth() / 2;
		cyCamera = -canvas.getHeight() / 2;
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
			if (o instanceof Obstacle) {
				addObject((Obstacle) o);
			} else {
				System.err.println("UNSUPPORTED: Adding non obstacle entity");
			}
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
	private void createXY(float x, float y) {
		x = Math.round(x);
		y = Math.round(y);
		switch (creationOptions[entityIndex]) {
			case ".SlothModel":
				SlothModel sTemplate = new SlothModel(x,y);
				promptTemplate(sTemplate);
				break;
			case ".Vine":
				Vine vTemplate = new Vine(x,y,5.0f,0.25f,1.0f,oneScale, 5f, -400f);
				promptTemplate(vTemplate);
				break;
			case ".Trunk":
				Trunk tTemplate = new Trunk(x,y, 5.0f, 0.25f, 1.0f, 3.0f,oneScale, 0);
				promptTemplate(tTemplate);
				break;
			case ".StiffBranch":
				StiffBranch sb = new StiffBranch(x,y, 3.0f, 0.25f, 1.0f,oneScale);
				promptTemplate(sb);
				break;
			case ".Tree":
				Tree tr = new Tree(x,y,5f, 3f, 0.25f, 1.0f, oneScale);
				promptTemplate(tr);
				break;
			case ".OwlModel":
				OwlModel owl = new OwlModel(x,y);
				promptTemplate(owl);
				break;
			case ".WallModel":
				WallModel wall = new WallModel(x,y,new float[] {0,0,0f,1f,1f,1f,1f,0f}, false);
				promptTemplate(wall);
				break;
			case ".GhostModel":
				GhostModel ghost = new GhostModel(x,y,x+2,y+2);
				promptTemplate(ghost);
				break;
			default:
				System.err.println("UNKNOWN ENT");
				break;
		}
		inputRateLimiter = UI_WAIT_SHORT;
	}

	private void promptTemplate(Entity template) {
		if (!prompting) {
			prompting = true;
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
		if (toAdd instanceof Obstacle) {
			addObject((Obstacle) toAdd);
		} else {
			System.err.println(toAdd);
			System.err.println("Unsupported nonobstacle entity");
		}
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
		float MAX_DISTANCE = 2f;
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

		if (minDistance < MAX_DISTANCE) {
			return found;
		}
		return null;
	}

	public void update(float dt) {

		// Decrement rate limiter to allow new input
		if (inputRateLimiter > 0) {
			inputRateLimiter--;
			return;
		}

		// Allow access to mouse coordinates for multiple inputs
		float mouseX = InputController.getInstance().getCrossHair().x;
		float mouseY = InputController.getInstance().getCrossHair().y;

		adjustedMouseX = mouseX - (cxCamera + canvas.getWidth()/2) / worldScale.x;
		adjustedMouseY = mouseY - (cyCamera + canvas.getHeight()/2) / worldScale.y;


		// Check for pan
		if (mouseX < 1) {
			// Pan left
			cxCamera+= 10;
		}
		if (mouseY < 1) {
			// down
			cyCamera+= 10;
		}
		if (mouseX > (canvas.getWidth() / worldScale.x) - 1) {
			cxCamera-= 10;
		}
		if (mouseY > (canvas.getHeight() / worldScale.y) - 1) {
			cyCamera-= 10;
		}

		// Create
		if (InputController.getInstance().isLeftClickPressed()) {
			createXY(adjustedMouseX,adjustedMouseY);
		}

		// Delete
		if (InputController.getInstance().isRightClickPressed()) {
			Entity select = entityQuery();
			if (select != null) objects.remove(select);
			inputRateLimiter = UI_WAIT_SHORT;
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
		if (InputController.getInstance().isLKeyPressed()) {
			if (!loadingLevelPrompt) {
				loadingLevelPrompt = true;
				currentLevel = showInputDialog("What level do you want to load?");
				reset();
				loadingLevelPrompt = false;
			}
			inputRateLimiter = UI_WAIT_LONG;
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

		for (float i = ((int)cxCamera % dpsW - dpsW); i < canvas.getWidth(); i += dpsW) {
			gridLineRenderer.begin(ShapeRenderer.ShapeType.Line);
			gridLineRenderer.setColor(Color.FOREST);
			gridLineRenderer.line(i, 0,i,canvas.getHeight());
			gridLineRenderer.end();
		}

		// horizontal
		for (float i = ((int)cyCamera % dpsH - dpsH); i < canvas.getHeight(); i += dpsH) {
			gridLineRenderer.begin(ShapeRenderer.ShapeType.Line);
			gridLineRenderer.setColor(Color.FOREST);
			gridLineRenderer.line(0, i,canvas.getWidth(),i);
			gridLineRenderer.end();
		}
	}

	@Override
	public void draw(float delta) {
		canvas.clear();

		// Translate camera to cx, cy
		camTrans.setToTranslation(cxCamera, cyCamera);
		camTrans.translate(canvas.getWidth()/2, canvas.getHeight()/2);

		canvas.begin(camTrans);
		for(Entity obj : objects) {
			obj.draw(canvas);
		}
		canvas.end();

		canvas.begin(camTrans);
		if (shouldDrawGrid) {
			drawGridLines();
		}
		canvas.end();


		// Text- independent of where you scroll
		canvas.begin(); // DO NOT SCALE
		if (showHelp) {
			String[] splitHelp = HELP_TEXT.split("\\R");
			float beginY = 500.0f;
			for (int i = 0; i < splitHelp.length; i++) {
				canvas.drawTextStandard(splitHelp[i], 90.0f, beginY);
				beginY -= 20;
			}
		}


		canvas.drawTextStandard("MOUSE: " + adjustedMouseX + " , " + adjustedMouseY, 10.0f, 140.0f);
		canvas.drawTextStandard(-cxCamera / worldScale.x + "," + -cyCamera / worldScale.y , 10.0f, 120.0f);
		canvas.drawTextStandard("Level: " + currentLevel, 10.0f, 100.0f);
		canvas.drawTextStandard("Creating: " + creationOptions[tentativeEntityIndex], 10.0f, 80.0f);
		if (tentativeEntityIndex != entityIndex) {
			canvas.drawTextStandard("Hit Enter to Select New Object Type.", 10.0f, 60.0f);
		}
		canvas.end();
	}

	@Override
	public void postUpdate(float dt) {
		// Add any objects created by actions
		while (!addQueue.isEmpty()) {
			addObject(addQueue.poll());
		}

		// Turn the physics engine crank.
		//world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		Iterator<PooledList<Entity>.Entry> iterator = objects.entryIterator();
		while (iterator.hasNext()) {
			PooledList<Entity>.Entry entry = iterator.next();
			Entity ent = entry.getValue();
			if (ent instanceof Obstacle) {
				Obstacle obj = (Obstacle) ent;
				if (obj.isRemoved()) {
					obj.deactivatePhysics(world);
					entry.remove();
				}
			} else {
				// Note that update is called last!
				ent.update(dt);
			}
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