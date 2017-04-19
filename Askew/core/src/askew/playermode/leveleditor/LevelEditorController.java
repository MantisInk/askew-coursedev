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
import com.badlogic.gdx.physics.box2d.World;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

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

	public static final int BUFFER = 5;
	public static final int TEXT_HEIGHT = 20;
	public static final int FIELD_TEXT_WIDTH = 150;
	public static final int FIELD_BOX_WIDTH = 150;
	public static final int BUTTON_WIDTH = 75;
	public static final int BUTTON_HEIGHT = 30;
	public static final int TEXT_LENGTH = 175;
	public static final int TEXT_HEIGHT1 = 20;
	/** Track asset loading from all instances and subclasses */
	private AssetState levelEditorAssetState = AssetState.EMPTY;

	private JSONLoaderSaver jsonLoaderSaver;
	@Getter @Setter
	private MantisAssetManager mantisAssetManager;

	private LevelModel levelModel;

	private static ShapeRenderer gridLineRenderer = new ShapeRenderer();

	private Texture background;

	Affine2 camTrans;
	float cxCamera;
	float cyCamera;
	float adjustedMouseX;
	float adjustedMouseY;

	protected Vector2 oneScale;

	private boolean pressedL, prevPressedL;

	@Getter
	private String currentLevel;

	private String createClass;

	JFrame editorWindow;

	/** A decrementing int that helps prevent accidental repeats of actions through an arbitrary countdown */
	private int inputRateLimiter = 0;

	private int tentativeEntityIndex = 0;
	private int entityIndex = 0;

	public static final int UI_WAIT_SHORT = 2;
	public static final int UI_WAIT_LONG = 15;
	public static final int UI_WAIT_ETERNAL = 120;


	public static final String[] creationOptions = {
			".SlothModel",
			".Vine",
			".PoleVault",
			".Trunk",
			".StiffBranch",
			".OwlModel",
			".WallModel",
			//".Tree",
			".OwlModel",
			".GhostModel",
			".BackgroundEntity"
	};

	private boolean prompting;
	private boolean guiPrompt;
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

	@Getter
	@Setter
	private boolean vimMode;

	@Getter
	@Setter
	private boolean selectedEntity;

	@Getter
	@Setter
	private boolean scrollEnabled;

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
		currentLevel = "test_save_obstacle";
		createClass = ".SlothModel";
		showHelp = true;
		shouldDrawGrid = true;
		camTrans = new Affine2();
		vimMode = false;
		selectedEntity = false;
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
		addQueue.clear();
		world.dispose();

		world = new World(gravity,false);
		setComplete(false);
		setFailure(false);
		populateLevel();
		if (editorWindow != null) {
			editorWindow.dispose();
		}
		guiPrompt = false;

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
			addObject( o);
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
	private void createXY(float x, float y) {
		float xorig = x;
		float yorig = y;
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
			case ".PoleVault":
				PoleVault pvTemplate = new PoleVault(x,y, 5.0f, 0.25f, 1.0f, oneScale, 0);
				promptTemplate(pvTemplate);
				break;
			case ".StiffBranch":
				StiffBranch sb = new StiffBranch(x,y, 3.0f, 0.25f, 1.0f,oneScale, 0f);
				promptTemplate(sb);
				break;
//			case ".Tree":
//				Tree tr = new Tree(x,y,5f, 3f, 0.25f, 1.0f, oneScale);
//				promptTemplate(tr);
//				break;
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
			case ".BackgroundEntity":
				BackgroundEntity bge = new BackgroundEntity(xorig,yorig);
				promptTemplate(bge);
				break;

			default:
				System.err.println("UNKNOWN ENT");
				break;
		}
		inputRateLimiter = UI_WAIT_SHORT;
	}

	private int generateSwingPropertiesForEntity(JsonObject e, JFrame jPanel, int rowNum) {
		for(Map.Entry<String,JsonElement> entry : e.entrySet()) {
			// Add key
			String key = entry.getKey();
			JLabel paramText = new JLabel(key + ":");

			JComponent valueComponent;
			// Add value based on type
			JsonElement value = entry.getValue();
			if (value.isJsonArray()) {
				// TODO (hard), recurse
				System.err.println("TODO: JSON Array. Use Henry's click and drag!");
				continue;
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

		return rowNum;
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
				// TODO (hard), recurse
				System.err.println("TODO: JSON Array. Use Henry's click and drag!");
				continue;
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

			paramText.setBounds((2 * BUFFER), (rowNum * TEXT_HEIGHT) + ((rowNum + 1) * BUFFER), FIELD_TEXT_WIDTH, TEXT_HEIGHT);
			valueComponent.setBounds((3 * BUFFER) + FIELD_TEXT_WIDTH, (rowNum * TEXT_HEIGHT) + ((rowNum + 1) * BUFFER), FIELD_BOX_WIDTH, TEXT_HEIGHT);

			// Update panel with key, value
			jPanel.add(paramText);
			jPanel.add(valueComponent);
			rowNum++;
		}

		return rowNum;
	}

	private void grabUpdatedObjectValuesFromGUI(JsonObject entityProp, Container p) {
		for(Map.Entry<String,JsonElement> entry : entityProp.entrySet()) {
			// Add key
			String key = entry.getKey();

			JsonElement value = entry.getValue();
			if (value.isJsonArray()) {
				// TODO (hard), recurse
				System.err.println("TODO: JSON Array");
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

	/** Returns the pop-out window for editing parameters of individual entities as a JPanel
	 *
	 * @param template The entity to be edited
	 * @param parentWindow The parent window for the window being created (used for button events)
	 * @returns A JPanel specifically for the level editor GUI
	 * */
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

	private void deleteEntity(){
		Entity select = entityQuery();
		if (select != null) objects.remove(select);
		inputRateLimiter = UI_WAIT_SHORT;
	}

	private void deleteEntity(Entity target){
		objects.remove(target);
	}

	private void editEntity(){
		Entity select = entityQuery();
		if (select != null) {
			if(isVimMode()) promptTemplate(select);
			else changeEntityParam(select);
		}
		inputRateLimiter = UI_WAIT_SHORT;
	}

	private void saveLevel(){
		System.out.println("Saving...");
		LevelModel timeToSave = new LevelModel();
		if (!vimMode) {
			// Grab params from gui
			JsonObject levelJson = jsonLoaderSaver.gsonToJsonObject(levelModel);
			grabUpdatedObjectValuesFromGUI(levelJson,editorWindow.getRootPane().getContentPane());
			timeToSave = jsonLoaderSaver.levelFromJson(levelJson);
			timeToSave.entities.clear();
		}
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

	private void loadLevel(){
		if (!loadingLevelPrompt) {
			loadingLevelPrompt = true;
			loadLevel(showInputDialog("What level do you want to load?"));
			loadingLevelPrompt = false;
		}
		inputRateLimiter = UI_WAIT_LONG;
	}


	private void loadLevel(String toLoad){
		currentLevel = toLoad;
		reset();
	}

	private void setLevelName(){
		String prevLevel = currentLevel;
		currentLevel = showInputDialog("What should we call this level?");

		//If action cancelled or entry is empty
		if(currentLevel.isEmpty()) currentLevel = prevLevel; // TODO Check if currentLevel == null

		inputRateLimiter = UI_WAIT_LONG;
	}

	private void promptTemplate(Entity template) {
		if (!vimMode) {
			changeEntityParam(template);
			return;
		}
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
			JButton okButton = new JButton("OK");
			okButton.addActionListener(e -> {
				objects.remove(template);
				promptTemplateCallback(commentTextArea.getText());
				mainFrame.setVisible(false);
				mainFrame.dispose();
			});
			panel.add(okButton);
			mainFrame.setVisible(true);
		}
	}

	private void changeEntityParam(Entity template) {
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

		adjustedMouseX = mouseX - (cxCamera + canvas.getWidth() / 2) / worldScale.x;
		adjustedMouseY = mouseY - (cyCamera + canvas.getHeight() / 2) / worldScale.y;

		//Toggle scrolling flag
		setScrollEnabled(InputController.getInstance().isRShiftKeyPressed() ||
				InputController.getInstance().isLShiftKeyPressed());

		//Toggle "VIM" mode
		if (InputController.getInstance().isVKeyPressed()) {
			setVimMode(!isVimMode());
			inputRateLimiter = UI_WAIT_LONG;
		}

		//Allows user to move the camera/view of the level
		if (isScrollEnabled()) {
			if (mouseX < 1) {
				// Pan left
				cxCamera += 10;
			}
			if (mouseY < 1) {
				// down
				cyCamera += 10;
			}
			if (mouseX > (canvas.getWidth() / worldScale.x) - 1) {
				cxCamera -= 10;
			}
			if (mouseY > (canvas.getHeight() / worldScale.y) - 1) {
				cyCamera -= 10;
			}
		}


		//If "VIM" mode is enabled
		if (isVimMode()) {
			//Dispose of GUI because VIM
			//editor_window.setVisible(false);
			//editor_window.dispose();
			//guiPrompt = false;

			guiPrompt = false;

			// Create
			if (InputController.getInstance().isLeftClickPressed()) {
				createXY(adjustedMouseX, adjustedMouseY);
			}

			// Delete
			if (InputController.getInstance().isRightClickPressed()) {
				deleteEntity();
			}

			// Edit
			if (InputController.getInstance().isEKeyPressed()) {
				editEntity();
			}

			// Save
			if (InputController.getInstance().isSKeyPressed()) {
				saveLevel();
			}

			// Name level
			if (InputController.getInstance().isNKeyPressed()) {
				setLevelName();
			}

			// Load level
			if (InputController.getInstance().isLKeyPressed()) {
				loadLevel();
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

			// Global Config
			if (InputController.getInstance().isGKeyPressed()) {
				promptGlobalConfig();
			}

			// Background
			if (InputController.getInstance().isBKeyPressed()) {
				levelModel.setBackground(showInputDialog("What texture should the background be set to?"));
				// TODO: Update the drawn background (after henry implements the engine)
				background = getMantisAssetManager().get(levelModel.getBackground());
			}
		} else {
			if (!guiPrompt) {
				makeGuiWindow();
			}

			if (InputController.getInstance().isLeftClickPressed()) {
				try {
					editEntity();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void makeGuiWindow() {
		//GUI Mode Enabled
			//Prevent multiple windows from being created
			guiPrompt = true;
			//Window Settings
			editorWindow = new JFrame();
			GridLayout gridLayout = new GridLayout(12,2);
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

			//Adding Entities
			JLabel addEntityHeader = new JLabel("Choose Entity to Add");
			JComboBox entityTypes = new JComboBox(creationOptions);
			JButton entityButton = new JButton("Add Entity");

			entityButton.addActionListener(e -> {
				entityIndex = entityTypes.getSelectedIndex();
				createXY(-cxCamera / worldScale.x, -cyCamera / worldScale.y);
			});

			editorWindow.add(addEntityHeader);
			editorWindow.add(entityTypes);
			editorWindow.add(entityButton);

			//TODO Add ability to edit entity parameters (on click/selecting only?)

			JLabel editEntityHeader = new JLabel("Click a Stage Entity to Edit It");

			editEntityHeader.setSize(250, TEXT_HEIGHT1);

			editorWindow.add(editEntityHeader);

			//Display Everything
			editorWindow.setVisible(true);
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

		//draw background
		canvas.begin();
		canvas.draw(background);
		canvas.end();

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
		float xPos = canvas.getWidth()-425;
		float yPos = canvas.getHeight() - 15;

		//TODO Figure out why this causes NullPointer Exception
		//displayFont.setColor(Color.GREEN);

		canvas.drawTextStandard("HOLD SHIFT + MOVE CURSOR TO ADJUST THE CAMERA", xPos, yPos);

		if (isVimMode()) {
			//float xPos = (canvas.getWidth()/2)-100;
			canvas.drawTextStandard("VIM MODE ENABLED (Press V to toggle)", 5, canvas.getHeight() - 15);

			if (showHelp) {
				String[] splitHelp = HELP_TEXT.split("\\R");
				float beginY = 500.0f;
				for (int i = 0; i < splitHelp.length; i++) {
					canvas.drawTextStandard(splitHelp[i], 90.0f, beginY);
					beginY -= 20;
				}
			}


			canvas.drawTextStandard("Creating: " + creationOptions[tentativeEntityIndex], 10.0f, 80.0f);
			if (tentativeEntityIndex != entityIndex) {
				canvas.drawTextStandard("Hit Enter to Select New Object Type.", 10.0f, 60.0f);
			}

		}
		canvas.drawTextStandard("MOUSE: " + adjustedMouseX + " , " + adjustedMouseY, 10.0f, 140.0f);
		canvas.drawTextStandard("Camera: " + (-cxCamera / worldScale.x) + "," + (-cyCamera / worldScale.y), 10.0f, 120.0f);
		canvas.drawTextStandard("Level: " + currentLevel, 10.0f, 100.0f);

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
			}
		}
	}

	@Override
	public void setCanvas(GameCanvas canvas) {
		// unscale
		this.canvas = canvas;
		this.worldScale.x = 1.0f * (float)canvas.getWidth()/(float)bounds.getWidth();
		this.worldScale.y = 1.0f * (float)canvas.getHeight()/(float)bounds.getHeight();
		jsonLoaderSaver.setScale(this.worldScale);
	}
}