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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import askew.playermode.WorldController;
import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.tree.Trunk;
import askew.entity.tree.PoleVault;
import askew.entity.tree.StiffBranch;
import askew.entity.tree.Tree;
import askew.entity.vine.Vine;
import askew.entity.sloth.SlothModel;
import askew.util.PooledList;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
			".PoleVault",
			".Trunk",
			".StiffBranch",
			".OwlModel",
			".WallModel",
			".Tree",
			".OwlModel",
			".GhostModel"
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
		vimMode = false;
		selectedEntity = false;
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
			case ".PoleVault":
				PoleVault pvTemplate = new PoleVault(x,y, 5.0f, 0.25f, 1.0f, oneScale, 0);
				promptTemplate(pvTemplate);
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

	/** Returns and ArrayList of parameters needed create a given game entity
	 *
	 * @param entityName The name of the entity as a string, ex ".SlothModel"
	 * */
	private ArrayList<String> getEntityParam(String entityName){
		ArrayList<String> fieldNames = new ArrayList<String>();
		fieldNames.add("x");
		fieldNames.add("y");

		switch (entityName) {
			case ".SlothModel":
				//No additional fields needed
				break;
			case ".Vine":
				fieldNames.add("numLinks");
				fieldNames.add("angle");
				fieldNames.add("omega");
				break;
			case ".Trunk":
				fieldNames.add("angle");
				fieldNames.add("numLinks");
				fieldNames.add("linksize");
				fieldNames.add("stiffLen");
				break;
			case ".PoleVault":
				fieldNames.add("angle");
				fieldNames.add("numLinks");
				fieldNames.add("linksize");
				break;
			case ".StiffBranch":
				fieldNames.add("stiffLen");
				break;
			case ".Tree":
				fieldNames = getEntityParam(".Trunk");
				ArrayList<String> branchList = getEntityParam(".StiffBranch");

				for(int i=0;i<branchList.size();i++){
					fieldNames.add(branchList.get(i));
				}
				break;
			case ".OwlModel":
				//No additional fields needed
				break;
			case ".WallModel":
				fieldNames.add("thorn");
				fieldNames.add("points");
				break;
			case ".GhostModel":
				fieldNames.add("patroldx");
				fieldNames.add("patroldy");
				break;
			default:
				System.err.println("UNKNOWN ENT");
				break;
		}

		return fieldNames;
	}

	private void createGuiWindow(){
		//TODO Create clean way of making GUI window

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

		ArrayList<String> params = getEntityParam(entityName);
		ArrayList<JTextField> fieldObjects = new ArrayList<>();
		//ArrayList<JComponent> fieldObjects = new ArrayList<>();

		JPanel panel = new JPanel();
		panel.setLayout(null);

		int buffer = 5;
		int textHeight = 20;
		int rowNum = 0;
		int fieldTextWidth = 75;
		int fieldBoxWidth = 50;

		JLabel header = new JLabel(entityName+" Properties (Please hit OK instead of X to closeout window)");
		JButton okButton = new JButton("OK");
		JButton deleteButton = new JButton("Delete Entity");

		//TODO Still figure out how to add Trees

		//Hard-coded for lambda purposes in the delete button ;w;
		float x = entityProp.get("x").getAsFloat();
		float y = entityProp.get("y").getAsFloat();

		//TODO Add back in for .Tree
//			if (entityName == ".Tree"){
//				trunkObject = entityProp.get("treeTrunk").getAsJsonObject();
//				branchObject = entityProp.get("treeBranch").getAsJsonObject();
//
//				//Extract X and Y for each entity
//			}
//			else {
//				//More assignments to make Java not complain
//				trunkObject = entityProp;
//				branchObject = entityProp;
//
//				x = entityProp.get("x").getAsFloat();
//				y = entityProp.get("y").getAsFloat();
//			}

		//Define top elements
		header.setBounds(buffer, buffer, 500, textHeight);
		rowNum++;
		deleteButton.setBounds(buffer, (rowNum*textHeight)+((rowNum+1)*buffer), 150, textHeight);
		rowNum++;

		//Define field elements

		if(entityName.equals(".WallModel")){
			//Hard-coded since otherwise I don't know how to make it work because JRadioButton isn't a JTextField OTL

			//Do X & Y components
			for (int i = 0; i < 2; i++) {
				String paramName = params.get(i);

				float paramVal = entityProp.get(paramName).getAsFloat();
				JLabel paramText = new JLabel(paramName + ":");
				JTextField paramField = new JTextField("" + paramVal);

				paramText.setBounds((2 * buffer), (rowNum * textHeight) + ((rowNum + 1) * buffer), fieldTextWidth, textHeight);
				paramField.setBounds((3 * buffer) + fieldTextWidth, (rowNum * textHeight) + ((rowNum + 1) * buffer), fieldBoxWidth, textHeight);

				panel.add(paramText);
				panel.add(paramField);
				fieldObjects.add(paramField);

				rowNum++;
			}

			//Get everything else
			boolean thornsFlag = entityProp.get("thorn").getAsBoolean();
			JRadioButton yesThorn = new JRadioButton("Thorns");
			JRadioButton noThorn = new JRadioButton("No Thorns");
			ButtonGroup thornButtons = new ButtonGroup();

			JsonArray thornsPoints = entityProp.get("points").getAsJsonArray();

			//Holds the dimensions for the wall
			int width = 0;
			int height = 0;

			//TODO Change from assuming shape is always a rectangle
			try {
				for (int k = 0; k < thornsPoints.size(); k++) {
					int value = thornsPoints.get(k).getAsInt();
					if (value > 0) {
						if (k % 2 == 0) width = value;
						else height = value;
					}
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}

			//Define wall parameter boxes
			JLabel boxWidthText = new JLabel("Width: ");
			JTextField boxWidthVal = new JTextField(""+width);
			JLabel boxHeightText = new JLabel("Height: ");
			JTextField boxHeightVal = new JTextField(""+height);

			//Set value of radio buttons
			if (thornsFlag) yesThorn.setSelected(true);
			else noThorn.setSelected(true);

			//Place radio buttons
			yesThorn.setBounds((2*buffer), (rowNum*textHeight)+((rowNum+1)*buffer), 75, textHeight);
			noThorn.setBounds((3*buffer)+75, (rowNum*textHeight)+((rowNum+1)*buffer), 100, textHeight);
			rowNum++;

			//Place wall parameter boxes
			boxWidthText.setBounds((2*buffer), (rowNum*textHeight)+((rowNum+1)*buffer), fieldTextWidth, textHeight);
			boxWidthVal.setBounds((3*buffer)+fieldTextWidth, (rowNum*textHeight)+((rowNum+1)*buffer), fieldBoxWidth, textHeight);
			rowNum++;
			boxHeightText.setBounds((2*buffer), (rowNum*textHeight)+((rowNum+1)*buffer), fieldTextWidth, textHeight);
			boxHeightVal.setBounds((3*buffer)+fieldTextWidth, (rowNum*textHeight)+((rowNum+1)*buffer), fieldBoxWidth, textHeight);
			rowNum++;

			//Add radio buttons to button group
			thornButtons.add(yesThorn);
			thornButtons.add(noThorn);

			//Add all elements to panel
			panel.add(yesThorn);
			panel.add(noThorn);
			panel.add(boxWidthText);
			panel.add(boxWidthVal);
			panel.add(boxHeightText);
			panel.add(boxHeightVal);

			okButton.addActionListener((ActionEvent e) -> {

				//Do X & Y components
				for (int i = 0; i < 2; i++) {
					String paramName = params.get(i);
					JTextField fieldObject = fieldObjects.get(i);

					entityProp.remove(paramName);
					entityProp.addProperty(paramName, fieldObject.getText());
				}

				//Change thorn flag
				entityProp.remove("thorn");
				if (yesThorn.isSelected()) entityProp.addProperty("thorn", true);
				else entityProp.addProperty("thorn", false);

				int boxWidth = Integer.parseInt(boxWidthVal.getText());
				int boxHeight = Integer.parseInt(boxHeightVal.getText());

				//Create box coordinates
				for(int k=0;k<thornsPoints.size();k++){
					thornsPoints.remove(0);
					if (k == 2 || k == 4) thornsPoints.add(boxWidth);
					else if (k == 7 || k == 5) thornsPoints.add(boxHeight);
					else thornsPoints.add(0);
				}

				//Change box parameters
				entityProp.remove("points");
				entityProp.add("points", thornsPoints);

				//Reassign object
				entityObject.remove("INSTANCE");
				entityObject.add("INSTANCE", entityProp);

				//Get the string form of the entityObject
				String stringJson = jsonLoaderSaver.stringFromJson(entityObject);
				promptTemplateCallback(stringJson);

				parentWindow.setVisible(false);
				parentWindow.dispose();
				//panel.setVisible(false);
				//panel.dispose();
			});
			//End of sad times
		}
		else if(entityName.equals(".Tree")){
			System.err.println("Unimplemented");
//					//TODO Add back in for .Tree
//					//entityProp
//					//trunkObject, branchObject
//
//					//Edit Trunk
////					float trunk_links = trunkObject.get("numLinks").getAsFloat();
////					//float current_links = trunkObject.get("numLinks").getAsFloat();
////					JLabel trunk_link_text = new JLabel("Number of links");
////					JTextField trunk_link_val = new JTextField(""+trunk_links);
////					//float current_stiff = entityProp.get("stiffLen").getAsFloat();
////					float trunk_stiff = trunkObject.get("stiffLen").getAsFloat();
////					JLabel trunk_stiff_text = new JLabel("Trunk Stiffness");
////					JTextField trunk_stiff_val = new JTextField(""+trunk_stiff);
////
////					trunk_link_text.setBounds((2*buffer), (3*textHeight)+(4*buffer), 100, textHeight);
////					trunk_link_val.setBounds((3*buffer)+100, (3*textHeight)+(4*buffer), 50, textHeight);
////					trunk_stiff_text.setBounds((2*buffer), (4*textHeight)+(5*buffer), 100, textHeight);
////					trunk_stiff_val.setBounds((3*buffer)+100, (4*textHeight)+(5*buffer), 50, textHeight);
////
////					panel.add(trunk_link_text);
////					panel.add(trunk_link_val);
////					panel.add(trunk_stiff_text);
////					panel.add(trunk_stiff_val);
////
////					//Edit Branch
////
////					float branch_stiff = branchObject.get("stiffLen").getAsFloat();
////					//float current_branch = entityObject.get("stiffLen").getAsFloat();
////					JLabel branch_stiff_text = new JLabel("Branch Stiffness");
////					JTextField branch_stiff_val = new JTextField(""+branch_stiff);
////
////					branch_stiff_text.setBounds((2*buffer), (5*textHeight)+(6*buffer), 75, textHeight);
////					branch_stiff_val.setBounds((3*buffer)+25, (5*textHeight)+(6*buffer), 50, textHeight);
////
////					panel.add(branch_stiff_text);
////					panel.add(branch_stiff_val);
////
////					okButton.addActionListener(e -> {
////						//Assign trunk parts
////						trunkObject.remove("x");
////						trunkObject.addProperty("x", x_pos_val.getText());
////
////						trunkObject.remove("y");
////						trunkObject.addProperty("y", y_pos_val.getText());
////
////						trunkObject.remove("numLinks");
////						trunkObject.addProperty("numLinks", link_val.getText());
////
////						trunkObject.remove("stiffLen");
////						trunkObject.addProperty("stiffLen", stiff_val.getText());
////
////						entityProp.remove("treeTrunk");
////						entityProp.addProperty("treeTrunk", stiff_val.getText());
////
////						//Assign branch parts
////
////						branchObject.remove("stiffLen");
////						branchObject.addProperty("stiffLen", branch_val.getText());
////
////						entityProp.remove("treeBranch");
////						entityProp.addProperty("treeBranch", stiff_val.getText());
////
////						//Put Everything Together
////
////						entityObject.remove("INSTANCE");
////						entityObject.add("INSTANCE", entityProp);
////
////						String temp2 = jsonLoaderSaver.stringFromJson(entityObject);
////						promptTemplateCallback(temp2);
////
////						mainFrame.setVisible(false);
////						mainFrame.dispose();
////						//panel.setVisible(false);
////						//panel.dispose();
////					});
//
//
//					break;
		}
		//Anything that isn't a wall
		else {

			for (int i = 0; i < params.size(); i++) {
				String paramName = params.get(i);
				float paramVal = entityProp.get(paramName).getAsFloat();
				JLabel paramText = new JLabel(paramName + ":");
				JTextField paramField = new JTextField("" + paramVal);

				paramText.setBounds((2 * buffer), (rowNum * textHeight) + ((rowNum + 1) * buffer), fieldTextWidth, textHeight);
				paramField.setBounds((3 * buffer) + fieldTextWidth, (rowNum * textHeight) + ((rowNum + 1) * buffer), fieldBoxWidth, textHeight);

				panel.add(paramText);
				panel.add(paramField);
				fieldObjects.add(paramField);
				//}
				rowNum++;
			}

			//Add okay button
			//okButton.setBounds(125, ((rowNum + 1) * textHeight) + ((rowNum + 1) * buffer), 100, textHeight);

			okButton.addActionListener(e -> {
				for (int i = 0; i < params.size(); i++) {
					String paramName = params.get(i);

					JTextField fieldObject = fieldObjects.get(i);

					entityProp.remove(paramName);
					entityProp.addProperty(paramName, fieldObject.getText());

				}

				entityObject.remove("INSTANCE");
				entityObject.add("INSTANCE", entityProp);

				//Get the string form of the entityObject
				String stringJson = jsonLoaderSaver.stringFromJson(entityObject);
				promptTemplateCallback(stringJson);

				parentWindow.setVisible(false);
				parentWindow.dispose();
				//panel.setVisible(false);
				//panel.dispose();
			});
		}

		okButton.setBounds(125, ((rowNum+1)*textHeight)+((rowNum+1)*buffer), 100, textHeight);

		deleteButton.addActionListener(e -> {
			deleteEntity(x,y);
			parentWindow.setVisible(false);
			parentWindow.dispose();
			//panel.setVisible(false);
			//panel.dispose();
		});

		panel.add(header);
		panel.add(okButton);
		panel.add(deleteButton);

		return panel;

	}

	private void deleteEntity(float adjustedMouseX, float adjustedMouseY){
		Entity select = entityQuery();
		if (select != null) objects.remove(select);
		inputRateLimiter = UI_WAIT_SHORT;
	}

	private void editEntity(float adjustedMouseX, float adjustedMouseY){
		Entity select = entityQuery();
		if (select != null) {
			if(isVimMode()) promptTemplate(select);
			else changeEntityParam(select);
			objects.remove(select);
		}
		inputRateLimiter = UI_WAIT_SHORT;
	}

	private void saveLevel(){
		System.out.println("Saving...");
		LevelModel timeToSave = new LevelModel();
		timeToSave.setTitle(currentLevel);
		for (Entity o : objects) {
			timeToSave.addEntity(o);
		}
	}

	private void loadLevel(){
		if (!loadingLevelPrompt) {
			loadingLevelPrompt = true;
			currentLevel = showInputDialog("What level do you want to load?");
			reset();
			loadingLevelPrompt = false;
		}
		inputRateLimiter = UI_WAIT_LONG;
	}

	private void setLevelName(){
		String prevLevel = currentLevel;
		currentLevel = showInputDialog("What should we call this level?");

		//If action cancelled or entry is empty
		if(currentLevel.isEmpty()) currentLevel = prevLevel; // TODO Check if currentLevel == null

		inputRateLimiter = UI_WAIT_LONG;
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
			JButton okButton = new JButton("OK");
			okButton.addActionListener(e -> {
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
			entityDisplay.setSize(600,600);
			entityDisplay.setLocationRelativeTo(null);
			JPanel panel = makeEntityWindow(template,entityDisplay);

			entityDisplay.add(panel);
			entityDisplay.setVisible(true);
			//panel.setVisible(true);
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

		adjustedMouseX = mouseX - (cxCamera + canvas.getWidth()/2) / worldScale.x;
		adjustedMouseY = mouseY - (cyCamera + canvas.getHeight()/2) / worldScale.y;

		//Toggle scrolling flag
		setScrollEnabled(InputController.getInstance().isRShiftKeyPressed() ||
				InputController.getInstance().isLShiftKeyPressed());

		//Toggle "VIM" mode
		if(InputController.getInstance().isVKeyPressed()) {
			setVimMode(!isVimMode());
			inputRateLimiter = UI_WAIT_LONG;
		}

		//Allows user to move the camera/view of the level
		if(isScrollEnabled()) {
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
		if(isVimMode()) {
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
				deleteEntity(adjustedMouseX, adjustedMouseY);
			}

			// Edit
			if (InputController.getInstance().isEKeyPressed()) {
				editEntity(adjustedMouseX, adjustedMouseY);
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

			// Save
			if (InputController.getInstance().isSKeyPressed()) {
				saveLevel();
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
		//GUI Mode Enabled
		else{
			if(!guiPrompt) {
				//Prevent multiple windows from being created
				guiPrompt = true;
				//Window Settings
				JFrame editorWindow = new JFrame();;

				//TODO Add scaling
				int buttonWidth = 75;
				int buttonHeight = 30;
				int buffer = 6;
				int textLength = 175;
				int textHeight = 20;
				//int field_length = 150;
				//int field_height= textHeight;

				editorWindow.setSize(canvas.getWidth()*3/5, canvas.getHeight()*2/3);

				//"File Properties" Stuff
				JLabel fileText = new JLabel("File Name: ");
				JLabel levelText = new JLabel("Level Name: ");

				JTextField fileName = new JTextField("Temp");
				JTextField levelName = new JTextField(currentLevel);

				//fileText.setBounds(buffer, buffer, textLength, textHeight);
				fileText.setBounds(buffer, buffer, 65, textHeight);
				//file_button.setBounds(textLength+buffer, buffer, buttonWidth-20, buttonHeight);
				fileName.setBounds(65+buffer, buffer, textLength, textHeight);
				//levelText.setBounds(textLength+buttonWidth+(buffer*5), buffer, textLength, textHeight);
				levelText.setBounds(65+textLength+(buffer*2), buffer, 75, textHeight);
				levelName.setBounds(65+75+textLength+(buffer*2), buffer, textLength, textHeight);
				//level_button.setBounds(buttonWidth +(textLength*2)+(buffer*5), buffer, buttonWidth-20, buttonHeight);

				//Load/Save Button
				JButton loadButton = new JButton("Load");
				JButton saveButton = new JButton("Save");

				loadButton.setBounds(65+75+(2*textLength)+(buffer*3), buffer, buttonWidth, buttonHeight);
				saveButton.setBounds(65+75+(2*textLength)+(buffer*3), (2*buffer)+buttonHeight, buttonWidth, buttonHeight);

				loadButton.addActionListener(e -> {
					loadLevel();
				});

				saveButton.addActionListener(e -> {
					//editorWindow.setVisible(false);
					//editorWindow.dispose();
					//guiPrompt = false;

					currentLevel = levelName.getText();
					saveLevel();
					//System.out.println("BOOP");
				});

//				file_button.addActionListener(e -> {
//					//editorWindow.setVisible(false);
//					//editorWindow.dispose();
//					//guiPrompt = false;
//					System.out.println("BOOP");
//				});
//
//				level_button.addActionListener(e -> {
//					//editorWindow.setVisible(false);
//					//editorWindow.dispose();
//					//guiPrompt = false;
//					setLevelName();
//				});

				//Add all file properties to the editor window
				editorWindow.add(fileText);
				editorWindow.add(fileName);
				//editorWindow.add(file_button);
				editorWindow.add(levelText);
				editorWindow.add(levelName);
				//editorWindow.add(level_button);
				editorWindow.add(loadButton);
				editorWindow.add(saveButton);

				//Stage Dimensions & Background

				//STUFF
				JLabel bgText = new JLabel("Current BG: ");
				//JTextField bgName = new JTextField();
				JLabel bgName = new JLabel("BACKGROUND_NAME");
				JButton bgButton = new JButton("Edit");
				JLabel sizeText = new JLabel("Stage Size: ");
				JButton sizeButton = new JButton("Apply");
				JLabel widthText = new JLabel("Width: ");
				JLabel heightText = new JLabel("Height: ");
				JTextField widthVal = new JTextField();
				JTextField heightVal = new JTextField();

				//

				int colBuffer = 125; //New location
				int col2Buffer = 175; //New location

				bgText.setBounds(buffer, textHeight+(3*buffer), 75, textHeight);
				bgName.setBounds(buffer+25, (textHeight*2)+(5*buffer), 200, textHeight);
				bgButton.setBounds((2*buffer)+75, textHeight+(3*buffer), 60, textHeight);
//				startXText.setBounds((3*buffer), (textHeight*3)+(4*buffer), 25, textHeight);
//				startXPos.setBounds((3*buffer)+25, (textHeight*3)+(4*buffer), 50, textHeight);
//				startYText.setBounds((3*buffer), (textHeight*4)+(5*buffer), 25, textHeight);
//				startYPos.setBounds((3*buffer)+25, (textHeight*4)+(5*buffer), 50, textHeight);
				sizeText.setBounds(buffer+colBuffer+col2Buffer, textHeight+(3*buffer), 75, textHeight);
				sizeButton.setBounds(buffer+colBuffer+col2Buffer+75, textHeight+(3*buffer), 75, textHeight);
				//rank_text.setBounds(buffer+colBuffer+col2Buffer , (textHeight*2)+(3*buffer), 100, textHeight);
				widthText.setBounds((3*buffer)+colBuffer+col2Buffer , (textHeight*2)+(4*buffer), 50, textHeight);
				widthVal.setBounds((3*buffer)+50+colBuffer+col2Buffer , (textHeight*2)+(4*buffer), 50, textHeight);
				heightText.setBounds((3*buffer)+colBuffer+col2Buffer , (textHeight*3)+(5*buffer), 50, textHeight);
				heightVal.setBounds((3*buffer)+50+colBuffer+col2Buffer , (textHeight*3)+(5*buffer), 50, textHeight);
				//rankBText.setBounds((3*buffer)+colBuffer+col2Buffer , (textHeight*4)+(5*buffer), 50, textHeight);
				//rankBTime.setBounds((3*buffer)+50+colBuffer+col2Buffer , (textHeight*4)+(5*buffer), 50, textHeight);

				bgButton.addActionListener(e -> {
					//TODO Get image file name and apply background
					bgName.setText("Not implemented yet!"); //Assign new file name to display
					//loadLevel();
				});

				sizeButton.addActionListener(e -> {
					//TODO Apply change to level size
					//Change level size
					//widthVal.setText("Not implemented yet!");
					widthVal.setText("Nah!");
					heightVal.setText("Nope!");
					//loadLevel();
				});

				int oopsBuffer = (2*textHeight)+(7*buffer); //Apply to everything below here \/

//				bgText.setBounds();
//				bgName.setBounds();
//				sizeText.setBounds();
//				widthText.setBounds();
//				heightText.setBounds();
//				widthVal.setBounds();
//				heightVal.setBounds();

				editorWindow.add(bgText);
				editorWindow.add(bgName);
				editorWindow.add(bgButton);
				editorWindow.add(sizeText);
				editorWindow.add(sizeButton);
				editorWindow.add(widthText);
				editorWindow.add(heightText);
				editorWindow.add(widthVal);
				editorWindow.add(heightVal);

				//Starting/Ending Fields

				JLabel sgHeaderText = new JLabel("Start and Goal Positions");
				JLabel startText = new JLabel("Start:");
				JLabel startXText = new JLabel("X: ");
				JLabel startYText = new JLabel("Y: ");
				JLabel goalText = new JLabel("Goal:");
				JLabel goalXText = new JLabel("X: ");
				JLabel goalYText = new JLabel("Y: ");

				JTextField startXPos = new JTextField();
				JTextField startYPos = new JTextField();
				JTextField goalXPos = new JTextField();
				JTextField goalYPos = new JTextField();

				JButton startButton = new JButton("Edit");
				JButton goalButton = new JButton("Edit");

				sgHeaderText.setBounds(buffer, textHeight+(3*buffer)+oopsBuffer, 175, textHeight);
				startText.setBounds(buffer, (textHeight*2)+(3*buffer+oopsBuffer), 50, textHeight);
				startButton.setBounds((2*buffer)+35, (textHeight*2)+(3*buffer)+oopsBuffer, 60, textHeight);
				startXText.setBounds((3*buffer), (textHeight*3)+(4*buffer)+oopsBuffer, 25, textHeight);
				startXPos.setBounds((3*buffer)+25, (textHeight*3)+(4*buffer)+oopsBuffer, 50, textHeight);
				startYText.setBounds((3*buffer), (textHeight*4)+(5*buffer)+oopsBuffer, 25, textHeight);
				startYPos.setBounds((3*buffer)+25, (textHeight*4)+(5*buffer)+oopsBuffer, 50, textHeight);

				//int colBuffer = 125;
				goalText.setBounds(buffer+colBuffer , (textHeight*2)+(3*buffer)+oopsBuffer, 50, textHeight);
				goalButton.setBounds((2*buffer)+35+colBuffer, (textHeight*2)+(3*buffer)+oopsBuffer, 60, textHeight);
				goalXText.setBounds((3*buffer)+colBuffer , (textHeight*3)+(4*buffer)+oopsBuffer, 25, textHeight);
				goalXPos.setBounds((3*buffer)+25+colBuffer , (textHeight*3)+(4*buffer)+oopsBuffer, 50, textHeight);
				goalYText.setBounds((3*buffer)+colBuffer , (textHeight*4)+(5*buffer)+oopsBuffer, 25, textHeight);
				goalYPos.setBounds((3*buffer)+25+colBuffer , (textHeight*4)+(5*buffer)+oopsBuffer, 50, textHeight);

				startButton.addActionListener(e -> {
					//TODO Enable manual selection of start position
					System.err.println("Unimplemented");
				});

				goalButton.addActionListener(e -> {
					//TODO Enable manual selection of goal position
					System.err.println("Unimplemented");
				});

				//Add all Starting/Ending Fields to the editor window
				//Taken out because may not need

//				editorWindow.add(sgHeaderText);
//				editorWindow.add(startText);
//				editorWindow.add(startXText);
//				editorWindow.add(startYText);
//				editorWindow.add(goalText);
//				editorWindow.add(goalXText);
//				editorWindow.add(goalYText);
//
//				editorWindow.add(startXPos);
//				editorWindow.add(startYPos);
//				editorWindow.add(goalXPos);
//				editorWindow.add(goalYPos);
//
//				editorWindow.add(startButton);
//				editorWindow.add(goalButton);

				//Goal Time Properties

				JLabel rankHeaderText = new JLabel("Ranking Thresholds (Seconds)");
				//JLabel rank_text = new JLabel("Time ");
				JLabel rankGText = new JLabel("Gold: ");
				JLabel rankSText = new JLabel("Silver: ");
				JLabel rankBText = new JLabel("Bronze: ");

				JTextField rankGTime = new JTextField();
				JTextField rankSTime = new JTextField();
				JTextField rankBTime = new JTextField();

				//int col2Buffer = 175;
				rankHeaderText.setBounds(buffer+colBuffer+col2Buffer, textHeight+(3*buffer)+oopsBuffer, 200, textHeight);
				//rank_text.setBounds(buffer+colBuffer+col2Buffer , (textHeight*2)+(3*buffer), 100, textHeight);
				rankGText.setBounds((3*buffer)+colBuffer+col2Buffer , (textHeight*2)+(3*buffer)+oopsBuffer, 50, textHeight);
				rankGTime.setBounds((3*buffer)+50+colBuffer+col2Buffer , (textHeight*2)+(3*buffer)+oopsBuffer, 50, textHeight);
				rankSText.setBounds((3*buffer)+colBuffer+col2Buffer , (textHeight*3)+(4*buffer)+oopsBuffer, 50, textHeight);
				rankSTime.setBounds((3*buffer)+50+colBuffer+col2Buffer , (textHeight*3)+(4*buffer)+oopsBuffer, 50, textHeight);
				rankBText.setBounds((3*buffer)+colBuffer+col2Buffer , (textHeight*4)+(5*buffer)+oopsBuffer, 50, textHeight);
				rankBTime.setBounds((3*buffer)+50+colBuffer+col2Buffer , (textHeight*4)+(5*buffer)+oopsBuffer, 50, textHeight);
				//rankBText.setBounds((3*buffer)+colBuffer+col2Buffer , (textHeight*5)+(6*buffer), 50, textHeight);
				//rankBTime.setBounds((3*buffer)+50+colBuffer+col2Buffer , (textHeight*5)+(6*buffer), 50, textHeight);

				editorWindow.add(rankHeaderText);
				//editorWindow.add(rank_text);
				editorWindow.add(rankGText);
				editorWindow.add(rankSText);
				editorWindow.add(rankBText);

				editorWindow.add(rankGTime);
				editorWindow.add(rankSTime);
				editorWindow.add(rankBTime);

				//Adding Entities

				JLabel addEntityHeader = new JLabel("Choose Entity to Add");
				JComboBox entityTypes = new JComboBox(creationOptions);
				JButton entityButton = new JButton("Add Entity");
//				JLabel entity_x_text = new JLabel("X: ");
//				JLabel entity_y_text = new JLabel("Y: ");
//				JTextField entity_x_val = new JTextField();
//				JTextField entity_y_val = new JTextField();

				addEntityHeader.setBounds(buffer, (textHeight*5)+(7*buffer)+oopsBuffer, 175, textHeight);
				entityTypes.setBounds(buffer, (textHeight*6)+(8*buffer)+oopsBuffer, 100, textHeight);
				entityButton.setBounds(100+(2*buffer), (textHeight*6)+(8*buffer)+oopsBuffer, 100, textHeight);

				entityButton.addActionListener(e -> {
					//TODO FIgure out why can't add object to center of screen

					//getEntityParam();
					//editEntity(adjustedMouseX,adjustedMouseY); //TESTING

					entityIndex = entityTypes.getSelectedIndex();
					createXY(-cxCamera,-cyCamera);
					//System.out.println("BAP");
				});

				editorWindow.add(addEntityHeader);
				editorWindow.add(entityTypes);
				editorWindow.add(entityButton);

				//TODO Add ability to edit entity parameters (on click/selecting only?)

				JLabel editEntityHeader = new JLabel("Click a Stage Entity to Edit It");
				//JComboBox entityTypes = new JComboBox(creationOptions);
				//JButton entityButton = new JButton("Add Entity");

				editEntityHeader.setBounds(100+(2*buffer), (textHeight*7)+(10*buffer)+oopsBuffer, 250, textHeight);

				editorWindow.add(editEntityHeader);

				//On left click = get entity at coordinate via same looping method used in promptTemplate
				//

				//promptTemplate

				//Display Everything
				editorWindow.setLayout(null);
				editorWindow.setVisible(true);



			}
			//TODO Add mouse interactions with level

			if (InputController.getInstance().isLeftClickPressed()) {
				try {
					editEntity(adjustedMouseX, adjustedMouseY);
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
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