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
import askew.entity.owl.OwlModel;
import askew.util.json.JSONLoaderSaver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import askew.playermode.WorldController;
import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.trunk.Trunk;
import askew.entity.stiffbranch.StiffBranch;
import askew.entity.vine.Vine;
import askew.entity.sloth.SlothModel;
import askew.util.PooledList;
import lombok.Getter;
import lombok.Setter;

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
			".OwlModel"
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
	public void preLoadContent(AssetManager manager) {
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
	public void loadContent(AssetManager manager) {
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
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );

		for(Obstacle obj : objects) {
			if(! (obj instanceof SlothModel))
				obj.deactivatePhysics(world);
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
			System.out.println("GE");
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
		switch (creationOptions[entityIndex]) {
			case ".SlothModel":
				SlothModel sTemplate = new SlothModel(x,y);
				promptTemplate(sTemplate);
				break;
			case ".Vine":
				Vine vTemplate = new Vine(x,y,5.0f,0.25f,1.0f,scale);
				promptTemplate(vTemplate);
				break;
			case ".trunk":
				Trunk tTemplate = new Trunk(x,y, 5.0f, 0.25f, 1.0f, 3.0f,scale);
				promptTemplate(tTemplate);
				break;
			case ".StiffBranch":
				StiffBranch sb = new StiffBranch(x,y, 3.0f, 0.25f, 1.0f,scale);
				promptTemplate(sb);
				break;
			case ".OwlModel":
				OwlModel owl = new OwlModel(x,y);
				promptTemplate(owl);
				break;
			default:
				System.err.println("UNKNOWN ENT");
				break;
		}
		inputRateLimiter = UI_WAIT_SHORT;
	}

	private void deleteEntity(float adjustedMouseX, float adjustedMouseY){
		QueryCallback qc = fixture -> {
			Object userData = fixture.getBody().getUserData();
			for (Obstacle o : objects) {
				if (o == userData) {
					objects.remove(o);
					return false;
				}

				if (o instanceof ComplexObstacle) {
					for (Obstacle oo : ((ComplexObstacle) o).getBodies()) {
						if (oo == userData) {
							objects.remove(o);
							return false;
						}
					}
				}
			}
			return true;
		};

		world.QueryAABB(qc,adjustedMouseX,adjustedMouseY,adjustedMouseX,adjustedMouseY);
		inputRateLimiter = UI_WAIT_SHORT;
	}

	private void editEntity(float adjustedMouseX, float adjustedMouseY){
		QueryCallback qc = fixture -> {
			Object userData = fixture.getBody().getUserData();
			for (Obstacle o : objects) {
				if (o == userData) {
					//promptTemplate(o);
					//System.out.println(o.getClass().getName());
					if(isVimMode()) promptTemplate(o); //changeEntityParam(o, o.getClass().getName());
					else changeEntityParam(o, o.getClass().getName()); //Allow user to edit parameters on GUI

					objects.remove(o);
					return false;
				}

				if (o instanceof ComplexObstacle) {
					for (Obstacle oo : ((ComplexObstacle) o).getBodies()) {
						if (oo == userData) {
							//promptTemplate(o);
							if(isVimMode()) promptTemplate(o); //changeEntityParam(o, o.getClass().getName());
							else changeEntityParam(o, o.getClass().getName()); //Allow user to edit parameters on GUI

							objects.remove(o);
							return false;
						}
					}
				}
			}
			return true;
		};

		world.QueryAABB(qc,adjustedMouseX,adjustedMouseY,adjustedMouseX,adjustedMouseY);
		inputRateLimiter = UI_WAIT_SHORT;
	}

	private void saveLevel(){
		System.out.println("Saving...");
		LevelModel timeToSave = new LevelModel();
		timeToSave.setTitle(currentLevel);
		for (Obstacle o : objects) {
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
			System.out.println(jsonOfTemplate); //TEST
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

	private void changeEntityParam(Entity template, String className) {
		if (!prompting) {
			prompting = true; //Use different constant? Can just use the same one?
			JsonObject entityObject = jsonLoaderSaver.gsonToJsonObject(template);

			//System.out.println(entityObject); //FIX EVERYTHING

			// flipping swing
			JDialog mainFrame = new JDialog();
			mainFrame.setSize(600,600);
			mainFrame.setLocationRelativeTo(null);
			JPanel panel = new JPanel();
			//JFrame panel = new JFrame();
			panel.setLayout(new FlowLayout());
//			final JTextArea commentTextArea =
//					new JTextArea(jsonOfTemplate,20,30);
			//panel.add(commentTextArea);

			//System.out.println(entityObject);
			//System.out.println(Entity.class);

			JsonObject entity_prop = entityObject.get("INSTANCE").getAsJsonObject();
			String entity_name = entityObject.get("CLASSNAME").getAsString();
			//String entity_name = className;
			int field_num = 2;
			int buffer = 5;
			int text_height = 15;

			//float x = entityObject.get("x").getAsFloat();
			//float y = entityObject.get("y").getAsFloat();

			float x = entity_prop.get("x").getAsFloat();
			float y = entity_prop.get("y").getAsFloat();

			JButton delete_thing = new JButton("Delete Entity");

			//JLabel header = new JLabel("Entity Properties");
			JLabel header = new JLabel("Entity Properties (Please hit OK instead of X)");
			JLabel x_pos_text = new JLabel("X:");
			JTextField x_pos_val = new JTextField(""+x);
			JLabel y_pos_text = new JLabel("Y:");
			JTextField y_pos_val = new JTextField(""+y);

			header.setBounds(buffer, buffer, 100, text_height);
			delete_thing.setBounds((2*buffer)+100, buffer, 300, text_height);
			x_pos_text.setBounds((2*buffer), text_height+(2*buffer), 25, text_height);
			x_pos_val.setBounds((3*buffer)+25, text_height+(2*buffer), 50, text_height);
			y_pos_text.setBounds((2*buffer), (2*text_height)+(3*buffer), 25, text_height);
			y_pos_val.setBounds((3*buffer)+25, (2*text_height)+(3*buffer), 50, text_height);

			delete_thing.addActionListener(e -> {
				deleteEntity(x,y);
				//promptTemplateCallback(commentTextArea.getText());
				mainFrame.setVisible(false);
				mainFrame.dispose();
			});

			panel.add(header);
			panel.add(x_pos_text);
			panel.add(x_pos_val);
			panel.add(y_pos_text);
			panel.add(y_pos_val);
			panel.add(delete_thing);

			//Trim class name
			entity_name = entity_name.substring(entity_name.lastIndexOf("."));

			JButton okButton = new JButton("OK");
			okButton.setBounds(275,500,100,text_height);
			//JLabel please_text = new JLabel("Please hit OK instead of X");
//			okButton.addActionListener(e -> {
//				//promptTemplateCallback(commentTextArea.getText());
//				mainFrame.setVisible(false);
//				mainFrame.dispose();
//			});

			switch(entity_name){
				case ".SlothModel":
//					SlothModel sTemplate = new SlothModel(x, y);
//					promptTemplate(sTemplate);
					//field_num = 2;
					okButton.addActionListener(e -> {
						entity_prop.remove("x");
						entity_prop.addProperty("x", x_pos_val.getText());

						entity_prop.remove("y");
						entity_prop.addProperty("y", y_pos_val.getText());

						entityObject.remove("INSTANCE");
						entityObject.add("INSTANCE", entity_prop);

						String temp2 = jsonLoaderSaver.stringFromJson(entityObject);
						//System.out.println(temp2);

						promptTemplateCallback(temp2);

						mainFrame.setVisible(false);
						mainFrame.dispose();
					});
					break;
				case ".Vine":
					float current_vines = entity_prop.get("numLinks").getAsFloat();
					//float current_vines = entityObject.get("numLinks").getAsFloat();
					JLabel vine_text = new JLabel("Number of links");
					JTextField vine_links = new JTextField(""+current_vines);

					vine_text.setBounds((2*buffer), (3*text_height)+(4*buffer), 25, text_height);
					vine_links.setBounds((3*buffer)+25, (3*text_height)+(4*buffer), 50, text_height);

					panel.add(vine_text);
					panel.add(vine_links);

					okButton.addActionListener(e -> {
						//STILL WORKING
						//promptTemplateCallback(commentTextArea.getText());
						//entityObject.remove("numLinks");
						//entityObject.addProperty("numLinks", vine_links.getText());

						entity_prop.remove("x");
						entity_prop.addProperty("x", x_pos_val.getText());

						entity_prop.remove("y");
						entity_prop.addProperty("y", y_pos_val.getText());

						entity_prop.remove("numLinks");
						entity_prop.addProperty("numLinks", vine_links.getText());

						entityObject.remove("INSTANCE");
						entityObject.add("INSTANCE", entity_prop);

						//Entity temp = jsonLoaderSaver.entityFromJson(entityObject);
						//String jsonOfObject = jsonLoaderSaver.gsonToJson(temp);

						//String jsonOfObject = jsonLoaderSaver.stringFromJson(entityObject);
//						Entity temp = jsonLoaderSaver.entityFromJson(entityObject, entity_name);
//						String jsonOfObject = jsonLoaderSaver.gsonToJson(temp);
//						promptTemplateCallback(jsonOfObject);

						String temp2 = jsonLoaderSaver.stringFromJson(entityObject);
						System.out.println(temp2);
						//guiCallback(temp2, entity_name);

						promptTemplateCallback(temp2);

						mainFrame.setVisible(false);
						mainFrame.dispose();
					});

					field_num++;
					break;
				case ".Platform":
					break;
				case ".Trunk":
					float current_links = entity_prop.get("numLinks").getAsFloat();
					//float current_links = entityObject.get("numLinks").getAsFloat();
					JLabel link_text = new JLabel("Number of links");
					JTextField link_val = new JTextField(""+current_links);
					//float current_stiff = entity_prop.get("stiffLen").getAsFloat();
					float current_stiff = entityObject.get("stiffLen").getAsFloat();
					JLabel stiff_text = new JLabel("Stiffness");
					JTextField stiff_val = new JTextField(""+current_stiff);

					link_text.setBounds((2*buffer), (3*text_height)+(4*buffer), 25, text_height);
					link_val.setBounds((3*buffer)+25, (3*text_height)+(4*buffer), 50, text_height);
					stiff_text.setBounds((2*buffer), (4*text_height)+(5*buffer), 25, text_height);
					stiff_val.setBounds((3*buffer)+25, (4*text_height)+(5*buffer), 50, text_height);

					panel.add(link_text);
					panel.add(link_val);
					panel.add(stiff_text);
					panel.add(stiff_val);

					okButton.addActionListener(e -> {
						entity_prop.remove("x");
						entity_prop.addProperty("x", x_pos_val.getText());

						entity_prop.remove("y");
						entity_prop.addProperty("y", y_pos_val.getText());

						entity_prop.remove("numLinks");
						entity_prop.addProperty("numLinks", link_val.getText());

						entity_prop.remove("stiffLen");
						entity_prop.addProperty("stiffLen", stiff_val.getText());

						entityObject.remove("INSTANCE");
						entityObject.add("INSTANCE", entity_prop);

						String temp2 = jsonLoaderSaver.stringFromJson(entityObject);
						promptTemplateCallback(temp2);

						mainFrame.setVisible(false);
						mainFrame.dispose();
					});

					field_num += 2;
					break;
				case ".StiffBranch":
					float current_branch = entity_prop.get("stiffLen").getAsFloat();
					//float current_branch = entityObject.get("stiffLen").getAsFloat();
					JLabel branch_text = new JLabel("Stiffness");
					JTextField branch_val = new JTextField(""+current_branch);

					branch_text.setBounds((2*buffer), (3*text_height)+(4*buffer), 25, text_height);
					branch_val.setBounds((3*buffer)+25, (3*text_height)+(4*buffer), 50, text_height);

					panel.add(branch_text);
					panel.add(branch_val);

					okButton.addActionListener(e -> {
						entity_prop.remove("x");
						entity_prop.addProperty("x", x_pos_val.getText());

						entity_prop.remove("y");
						entity_prop.addProperty("y", y_pos_val.getText());

						entity_prop.remove("stiffLen");
						entity_prop.addProperty("stiffLen", branch_val.getText());

						entityObject.remove("INSTANCE");
						entityObject.add("INSTANCE", entity_prop);

						String temp2 = jsonLoaderSaver.stringFromJson(entityObject);
						promptTemplateCallback(temp2);

						mainFrame.setVisible(false);
						mainFrame.dispose();
					});

					field_num++;
					break;
				case ".OwlModel":
					okButton.addActionListener(e -> {
						entity_prop.remove("x");
						entity_prop.addProperty("x", x_pos_val.getText());

						entity_prop.remove("y");
						entity_prop.addProperty("y", y_pos_val.getText());

						entityObject.remove("INSTANCE");
						entityObject.add("INSTANCE", entity_prop);

						String temp2 = jsonLoaderSaver.stringFromJson(entityObject);
						//System.out.println(temp2);

						promptTemplateCallback(temp2);

						mainFrame.setVisible(false);
						mainFrame.dispose();
					});

					break;
				default:

					break;
			}


			mainFrame.add(panel);
//			JButton okButton = new JButton("OK");
//			JLabel please_text = new JLabel("Please hit OK instead of X");
//			okButton.addActionListener(e -> {
//				//promptTemplateCallback(commentTextArea.getText());
//				mainFrame.setVisible(false);
//				mainFrame.dispose();
//			});
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

	public void update(float dt) {

		// Decrement rate limiter to allow new input
		if (inputRateLimiter > 0) {
			inputRateLimiter--;
			return;
		}

		// Allow access to mouse coordinates for multiple inputs
		float mouseX = InputController.getInstance().getCrossHair().x;
		float mouseY = InputController.getInstance().getCrossHair().y;
		float adjustedMouseX = mouseX - (cxCamera + canvas.getWidth()/2) / scale.x;
		float adjustedMouseY = mouseY - (cyCamera + canvas.getHeight()/2) / scale.y;

		//Toggle scrolling flag
		setScrollEnabled(InputController.getInstance().isRShiftKeyPressed() ||
				InputController.getInstance().isLShiftKeyPressed());

		//Toggle "VIM" mode
		if(InputController.getInstance().isVKeyPressed()){
			if(isVimMode()) setVimMode(false);
			else setVimMode(true);
			inputRateLimiter = UI_WAIT_LONG;
		}

		//Allows user to move the camera/view of the level
		if(isScrollEnabled()){
			// Check for pan
			if (mouseX < 1) {
				// Pan left
				cxCamera += 10;
			}
			if (mouseY < 1) {
				// down
				cyCamera += 10;
			}
			if (mouseX > (canvas.getWidth() / scale.x) - 1) {
				cxCamera -= 10;
			}
			if (mouseY > (canvas.getHeight() / scale.y) - 1) {
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

			// Name level
			if (InputController.getInstance().isNKeyPressed()) {
				setLevelName();
//				currentLevel = showInputDialog("What should we call this level?");
//				inputRateLimiter = UI_WAIT_LONG;
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
				JFrame editor_window = new JFrame();
				//JFrame entity_window = new JFrame();

				//TODO Add scaling
				int button_width = 75;
				int button_height = 30;
				int buffer = 6;
				int text_length = 175;
				int text_height = 20;
				//int field_length = 150;
				//int field_height= text_height;

				//editor_window.setSize(canvas.getWidth()*3/5, canvas.getHeight() + 100);
				editor_window.setSize(canvas.getWidth()*3/5, canvas.getHeight()*2/3);
//				JButton okButton = new JButton("ok");
//				okButton.addActionListener(e -> {
//					editor_window.setVisible(false);
//					editor_window.dispose();
//					guiPrompt = false;
//				});

				//System.out.println("X: "+editor_window.getWidth());
				//System.out.println("Y: "+editor_window.getHeight());


				//"File Properties" Stuff
				//JLabel file_text = new JLabel("File Name: "+"Temp");
				JLabel file_text = new JLabel("File Name: ");
				//JLabel level_text = new JLabel("Level Name: "+currentLevel);
				JLabel level_text = new JLabel("Level Name: ");
				//JButton file_button = new JButton("Edit");
				//JButton level_button = new JButton("Edit");

				JTextField file_name = new JTextField("Temp");
				JTextField level_name = new JTextField(currentLevel);
				//file_button.setBounds(100*scale.x,100*scale.y,100*scale.x,100*scale.y);

				//file_text.setBounds(buffer, buffer, text_length, text_height);
				file_text.setBounds(buffer, buffer, 65, text_height);
				//file_button.setBounds(text_length+buffer, buffer, button_width-20, button_height);
				file_name.setBounds(65+buffer, buffer, text_length, text_height);
				//level_text.setBounds(text_length+button_width+(buffer*5), buffer, text_length, text_height);
				level_text.setBounds(65+text_length+(buffer*2), buffer, 75, text_height);
				level_name.setBounds(65+75+text_length+(buffer*2), buffer, text_length, text_height);
				//level_button.setBounds(button_width +(text_length*2)+(buffer*5), buffer, button_width-20, button_height);

				//Load/Save Button
				JButton load_button = new JButton("Load");
				JButton save_button = new JButton("Save");

				load_button.setBounds(65+75+(2*text_length)+(buffer*3), buffer, button_width, button_height);
				save_button.setBounds(65+75+(2*text_length)+(buffer*3), (2*buffer)+button_height, button_width, button_height);

				load_button.addActionListener(e -> {
					loadLevel();
					//System.out.println("BAP");
				});

				save_button.addActionListener(e -> {
					//editor_window.setVisible(false);
					//editor_window.dispose();
					//guiPrompt = false;

					//currentFile = file_name.getText(); ?!?!?!?
					currentLevel = level_name.getText();
					saveLevel();
					//System.out.println("BOOP");
				});

//				file_button.addActionListener(e -> {
//					//editor_window.setVisible(false);
//					//editor_window.dispose();
//					//guiPrompt = false;
//					System.out.println("BOOP");
//				});
//
//				level_button.addActionListener(e -> {
//					//editor_window.setVisible(false);
//					//editor_window.dispose();
//					//guiPrompt = false;
//					setLevelName();
//				});

				//Add all file properties to the editor window
				editor_window.add(file_text);
				editor_window.add(file_name);
				//editor_window.add(file_button);
				editor_window.add(level_text);
				editor_window.add(level_name);
				//editor_window.add(level_button);
				editor_window.add(load_button);
				editor_window.add(save_button);

				//Stage Dimensions & Background

				//STUFF
				JLabel bg_text = new JLabel("Current BG: ");
				//JTextField bg_name = new JTextField();
				JLabel bg_name = new JLabel("BACKGROUND_NAME");
				JButton bg_button = new JButton("Edit");
				JLabel size_text = new JLabel("Stage Size: ");
				JButton size_button = new JButton("Apply");
				JLabel width_text = new JLabel("Width: ");
				JLabel height_text = new JLabel("Height: ");
				JTextField width_val = new JTextField();
				JTextField height_val = new JTextField();

				//

				int col_buffer = 125; //New location
				int col2_buffer = 175; //New location

				bg_text.setBounds(buffer, text_height+(3*buffer), 75, text_height);
				bg_name.setBounds(buffer+25, (text_height*2)+(5*buffer), 200, text_height);
				bg_button.setBounds((2*buffer)+75, text_height+(3*buffer), 60, text_height);
//				start_x_text.setBounds((3*buffer), (text_height*3)+(4*buffer), 25, text_height);
//				start_x_pos.setBounds((3*buffer)+25, (text_height*3)+(4*buffer), 50, text_height);
//				start_y_text.setBounds((3*buffer), (text_height*4)+(5*buffer), 25, text_height);
//				start_y_pos.setBounds((3*buffer)+25, (text_height*4)+(5*buffer), 50, text_height);
				size_text.setBounds(buffer+col_buffer+col2_buffer, text_height+(3*buffer), 75, text_height);
				size_button.setBounds(buffer+col_buffer+col2_buffer+75, text_height+(3*buffer), 75, text_height);
				//rank_text.setBounds(buffer+col_buffer+col2_buffer , (text_height*2)+(3*buffer), 100, text_height);
				width_text.setBounds((3*buffer)+col_buffer+col2_buffer , (text_height*2)+(4*buffer), 50, text_height);
				width_val.setBounds((3*buffer)+50+col_buffer+col2_buffer , (text_height*2)+(4*buffer), 50, text_height);
				height_text.setBounds((3*buffer)+col_buffer+col2_buffer , (text_height*3)+(5*buffer), 50, text_height);
				height_val.setBounds((3*buffer)+50+col_buffer+col2_buffer , (text_height*3)+(5*buffer), 50, text_height);
				//rank_b_text.setBounds((3*buffer)+col_buffer+col2_buffer , (text_height*4)+(5*buffer), 50, text_height);
				//rank_b_time.setBounds((3*buffer)+50+col_buffer+col2_buffer , (text_height*4)+(5*buffer), 50, text_height);

				bg_button.addActionListener(e -> {
					//TODO Get image file name and apply background
					bg_name.setText("Not implemented yet!"); //Assign new file name to display
					//loadLevel();
				});

				size_button.addActionListener(e -> {
					//TODO Apply change to level size
					//Change level size
					//width_val.setText("Not implemented yet!");
					width_val.setText("Nah!");
					height_val.setText("Nope!");
					//loadLevel();
				});

				int oops_buffer = (2*text_height)+(7*buffer); //Apply to everything below here \/

//				bg_text.setBounds();
//				bg_name.setBounds();
//				size_text.setBounds();
//				width_text.setBounds();
//				height_text.setBounds();
//				width_val.setBounds();
//				height_val.setBounds();

				editor_window.add(bg_text);
				editor_window.add(bg_name);
				editor_window.add(bg_button);
				editor_window.add(size_text);
				editor_window.add(size_button);
				editor_window.add(width_text);
				editor_window.add(height_text);
				editor_window.add(width_val);
				editor_window.add(height_val);

				//Starting/Ending Fields

				JLabel sg_header_text = new JLabel("Start and Goal Positions");
				JLabel start_text = new JLabel("Start:");
				JLabel start_x_text = new JLabel("X: ");
				JLabel start_y_text = new JLabel("Y: ");
				JLabel goal_text = new JLabel("Goal:");
				JLabel goal_x_text = new JLabel("X: ");
				JLabel goal_y_text = new JLabel("Y: ");

				JTextField start_x_pos = new JTextField();
				JTextField start_y_pos = new JTextField();
				JTextField goal_x_pos = new JTextField();
				JTextField goal_y_pos = new JTextField();

				JButton start_button = new JButton("Edit");
				JButton goal_button = new JButton("Edit");

				sg_header_text.setBounds(buffer, text_height+(3*buffer)+oops_buffer, 175, text_height);
				start_text.setBounds(buffer, (text_height*2)+(3*buffer+oops_buffer), 50, text_height);
				start_button.setBounds((2*buffer)+35, (text_height*2)+(3*buffer)+oops_buffer, 60, text_height);
				start_x_text.setBounds((3*buffer), (text_height*3)+(4*buffer)+oops_buffer, 25, text_height);
				start_x_pos.setBounds((3*buffer)+25, (text_height*3)+(4*buffer)+oops_buffer, 50, text_height);
				start_y_text.setBounds((3*buffer), (text_height*4)+(5*buffer)+oops_buffer, 25, text_height);
				start_y_pos.setBounds((3*buffer)+25, (text_height*4)+(5*buffer)+oops_buffer, 50, text_height);

				//int col_buffer = 125;
				goal_text.setBounds(buffer+col_buffer , (text_height*2)+(3*buffer)+oops_buffer, 50, text_height);
				goal_button.setBounds((2*buffer)+35+col_buffer, (text_height*2)+(3*buffer)+oops_buffer, 60, text_height);
				goal_x_text.setBounds((3*buffer)+col_buffer , (text_height*3)+(4*buffer)+oops_buffer, 25, text_height);
				goal_x_pos.setBounds((3*buffer)+25+col_buffer , (text_height*3)+(4*buffer)+oops_buffer, 50, text_height);
				goal_y_text.setBounds((3*buffer)+col_buffer , (text_height*4)+(5*buffer)+oops_buffer, 25, text_height);
				goal_y_pos.setBounds((3*buffer)+25+col_buffer , (text_height*4)+(5*buffer)+oops_buffer, 50, text_height);

				start_button.addActionListener(e -> {
					//TODO Enable manual selection of start position
					System.out.println("BAP");
				});

				goal_button.addActionListener(e -> {
					//TODO Enable manual selection of goal position
					System.out.println("BLAH");
				});

				//Add all Starting/Ending Fields to the editor window
				editor_window.add(sg_header_text);
				editor_window.add(start_text);
				editor_window.add(start_x_text);
				editor_window.add(start_y_text);
				editor_window.add(goal_text);
				editor_window.add(goal_x_text);
				editor_window.add(goal_y_text);

				editor_window.add(start_x_pos);
				editor_window.add(start_y_pos);
				editor_window.add(goal_x_pos);
				editor_window.add(goal_y_pos);

				editor_window.add(start_button);
				editor_window.add(goal_button);

				//Goal Time Properties

				JLabel rank_header_text = new JLabel("Ranking Thresholds (Seconds)");
				//JLabel rank_text = new JLabel("Time ");
				JLabel rank_g_text = new JLabel("Gold: ");
				JLabel rank_s_text = new JLabel("Silver: ");
				JLabel rank_b_text = new JLabel("Bronze: ");

				JTextField rank_g_time = new JTextField();
				JTextField rank_s_time = new JTextField();
				JTextField rank_b_time = new JTextField();

				//int col2_buffer = 175;
				rank_header_text.setBounds(buffer+col_buffer+col2_buffer, text_height+(3*buffer)+oops_buffer, 200, text_height);
				//rank_text.setBounds(buffer+col_buffer+col2_buffer , (text_height*2)+(3*buffer), 100, text_height);
				rank_g_text.setBounds((3*buffer)+col_buffer+col2_buffer , (text_height*2)+(3*buffer)+oops_buffer, 50, text_height);
				rank_g_time.setBounds((3*buffer)+50+col_buffer+col2_buffer , (text_height*2)+(3*buffer)+oops_buffer, 50, text_height);
				rank_s_text.setBounds((3*buffer)+col_buffer+col2_buffer , (text_height*3)+(4*buffer)+oops_buffer, 50, text_height);
				rank_s_time.setBounds((3*buffer)+50+col_buffer+col2_buffer , (text_height*3)+(4*buffer)+oops_buffer, 50, text_height);
				rank_b_text.setBounds((3*buffer)+col_buffer+col2_buffer , (text_height*4)+(5*buffer)+oops_buffer, 50, text_height);
				rank_b_time.setBounds((3*buffer)+50+col_buffer+col2_buffer , (text_height*4)+(5*buffer)+oops_buffer, 50, text_height);
				//rank_b_text.setBounds((3*buffer)+col_buffer+col2_buffer , (text_height*5)+(6*buffer), 50, text_height);
				//rank_b_time.setBounds((3*buffer)+50+col_buffer+col2_buffer , (text_height*5)+(6*buffer), 50, text_height);

				editor_window.add(rank_header_text);
				//editor_window.add(rank_text);
				editor_window.add(rank_g_text);
				editor_window.add(rank_s_text);
				editor_window.add(rank_b_text);

				editor_window.add(rank_g_time);
				editor_window.add(rank_s_time);
				editor_window.add(rank_b_time);

				//Adding Entities

				JLabel add_entity_header = new JLabel("Choose Entity to Add");
				JComboBox entity_types = new JComboBox(creationOptions);
				JButton entity_button = new JButton("Add Entity");
//				JLabel entity_x_text = new JLabel("X: ");
//				JLabel entity_y_text = new JLabel("Y: ");
//				JTextField entity_x_val = new JTextField();
//				JTextField entity_y_val = new JTextField();

				add_entity_header.setBounds(buffer, (text_height*5)+(7*buffer)+oops_buffer, 175, text_height);
				entity_types.setBounds(buffer, (text_height*6)+(8*buffer)+oops_buffer, 100, text_height);
				entity_button.setBounds(100+(2*buffer), (text_height*6)+(8*buffer)+oops_buffer, 100, text_height);

				entity_button.addActionListener(e -> {
					//TODO Add object to center of screen

					//getEntityParam();
					//editEntity(adjustedMouseX,adjustedMouseY); //TESTING

					entityIndex = entity_types.getSelectedIndex();
					createXY(cxCamera,cyCamera);
					System.out.println("BAP");
				});

				editor_window.add(add_entity_header);
				editor_window.add(entity_types);
				editor_window.add(entity_button);

				//TODO Add ability to edit entity parameters (on click/selecting only?)

				JLabel edit_entity_header = new JLabel("Click a Stage Entity to Edit It");
				//JComboBox entity_types = new JComboBox(creationOptions);
				//JButton entity_button = new JButton("Add Entity");

				edit_entity_header.setBounds(100+(2*buffer), (text_height*7)+(10*buffer)+oops_buffer, 250, text_height);

				editor_window.add(edit_entity_header);

				//On left click = get entity at coordinate via same looping method used in promptTemplate
				//

				//promptTemplate

				//Display Everything
				editor_window.setLayout(null);
				editor_window.setVisible(true);



			}
			//TODO Add mouse interactions with level

			if (InputController.getInstance().isLeftClickPressed()) {
				try {
					editEntity(adjustedMouseX, adjustedMouseY);
				}
				catch (Exception e){
					System.out.println(e);
					System.out.println("Derp?");
				}
			}
		}
	}

	private void drawGridLines() {
		// debug lines
		// vertical
		for (int i = ((int)cxCamera % 32 - 32); i < 1024; i += 32) {
			Gdx.gl.glLineWidth(1);
			gridLineRenderer.begin(ShapeRenderer.ShapeType.Line);
			gridLineRenderer.setColor(Color.FOREST);
			gridLineRenderer.line(i, 0,i,768);
			gridLineRenderer.end();
			Gdx.gl.glLineWidth(1);
		}

		// horizontal
		for (int i = ((int)cyCamera % 32 - 32); i < 768; i += 32) {
			Gdx.gl.glLineWidth(1);
			gridLineRenderer.begin(ShapeRenderer.ShapeType.Line);
			gridLineRenderer.setColor(Color.FOREST);
			gridLineRenderer.line(0, i,1024,i);
			gridLineRenderer.end();
			Gdx.gl.glLineWidth(1);
		}
	}

	@Override
	public void draw(float delta) {
		canvas.clear();

		// Translate camera to cx, cy
		camTrans.setToTranslation(cxCamera, cyCamera);
		camTrans.translate(canvas.getWidth()/2, canvas.getHeight()/2);

		canvas.begin(camTrans);
		for(Obstacle obj : objects) {
			obj.draw(canvas);
		}

		if (shouldDrawGrid) {
			drawGridLines();
		}

		canvas.end();


		// Text- independent of where you scroll
		canvas.begin(); // DO NOT SCALE
		float x_pos = canvas.getWidth()-425;
		float y_pos = canvas.getHeight() - 15;

		canvas.drawTextStandard("HOLD SHIFT + MOVE CURSOR TO ADJUST THE CAMERA", x_pos, y_pos);

		if (isVimMode()) {
			//float x_pos = (canvas.getWidth()/2)-100;
			canvas.drawTextStandard("VIM MODE ENABLED (Press V to toggle)", 5, canvas.getHeight() - 15);

			if (showHelp) {
				String[] splitHelp = HELP_TEXT.split("\\R");
				float beginY = 500.0f;
				for (int i = 0; i < splitHelp.length; i++) {
					canvas.drawTextStandard(splitHelp[i], 90.0f, beginY);
					beginY -= 20;
				}
			}

			canvas.drawTextStandard(cxCamera / scale.x + "," + cyCamera / scale.y, 10.0f, 120.0f);
			canvas.drawTextStandard("Level: " + currentLevel, 10.0f, 100.0f);
			canvas.drawTextStandard("Creating: " + creationOptions[tentativeEntityIndex], 10.0f, 80.0f);
			if (tentativeEntityIndex != entityIndex) {
				canvas.drawTextStandard("Hit Enter to Select New Object Type.", 10.0f, 60.0f);
			}
		}
		//else canvas.drawTextStandard("VIM MODE DISABLED (Press V to toggle)", x_pos, y_pos);
		canvas.end();
	}

	@Override
	public void postUpdate(float dt) {
		// Add any objects created by actions
		// Add any objects created by actions
		while (!addQueue.isEmpty()) {
			addObject(addQueue.poll());
		}

		// Turn the physics engine crank.
		//world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
		while (iterator.hasNext()) {
			PooledList<Obstacle>.Entry entry = iterator.next();
			Obstacle obj = entry.getValue();
			if (obj.isRemoved()) {
				obj.deactivatePhysics(world);
				entry.remove();
			} else {
				// Note that update is called last!
				obj.update(dt);
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
		this.scale.x = 1.0f * canvas.getWidth()/bounds.getWidth();
		this.scale.y = 1.0f * canvas.getHeight()/bounds.getHeight();
		jsonLoaderSaver.setScale(this.scale);
	}
}