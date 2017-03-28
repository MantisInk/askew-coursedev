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
import askew.util.json.JSONLoaderSaver;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.google.gson.JsonObject;
import askew.playermode.WorldController;
import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.Trunk;
import askew.entity.StiffBranch;
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
public class LevelEditorController extends WorldController implements ContactListener {

	/** Track asset loading from all instances and subclasses */
	private AssetState levelEditorAssetState = AssetState.EMPTY;

	private AssetTraversalController fat;

	private JSONLoaderSaver jls;

	private LevelModel lm;

	private String currentLevel;

	private String createClass;

	int inputThresher = 0;
	int tentativeIndex = 0;
	int actualindex = 0;

	public static final int THRESHER_RESET = 2;
	public static final int THRESHER_RESET_LONG = 15;


	public static final String[] creationOptions = {
			".SlothModel",
			".Vine",
			".Platform",
			".Trunk",
			".StiffBranch"
	};

	@Getter
	private String trialLevelName;
	private boolean prompting;

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
		jls.setManager(manager);
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
		world.setContactListener(this);
		jls = new JSONLoaderSaver();
		currentLevel = "test_save_obstacle";
		createClass = ".SlothModel";
		trialLevelName = "";
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
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		try {
			lm = jls.loadLevel(currentLevel);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (lm == null) {
			lm = new LevelModel();
		}

		for (Obstacle o : lm.getEntities()) {
			addObject(o);
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
		switch (creationOptions[actualindex]) {
			case ".SlothModel":
				SlothModel sTemplate = new SlothModel(x,y);
				promptTemplate(sTemplate);
				break;
			case ".Vine":
				Vine vTemplate = new Vine(x,y,5.0f,0.25f,1.0f,scale);
				promptTemplate(vTemplate);
				break;
			case ".Trunk":
				Trunk tTemplate = new Trunk(x,y, 5.0f, 0.25f, 1.0f, 3.0f,scale);
				promptTemplate(tTemplate);
				break;
			case ".StiffBranch":
				StiffBranch sb = new StiffBranch(x,y, 3.0f, 0.25f, 1.0f,scale);
				promptTemplate(sb);
		}
		inputThresher = THRESHER_RESET;
	}

	private void promptTemplate(Obstacle template) {
		if (!prompting) {
			prompting = true;
			String jsonOfTemplate = jls.gsonToJson(template);
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
		Obstacle toAdd = jls.obstacleFromJson(json);
		addObject(toAdd);
		prompting = false;
	}

	private void promptGlobalConfig() {
		if (!prompting) {
			prompting = true;
			String jsonOfConfig = jls.prettyJson(JSONLoaderSaver.loadArbitrary("./config.json").orElseGet(JsonObject::new));
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
				JSONLoaderSaver.saveArbitrary("./config.json",commentTextArea.getText());
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

		if (inputThresher > 0) {
			inputThresher--;
			return;
		}

		if (InputController.getInstance().isLeftClickPressed()) {
			float mx = InputController.getInstance().getCrossHair().x;
			float my = InputController.getInstance().getCrossHair().y;
			createXY(mx,my);
		}

		if (InputController.getInstance().isRightClickPressed()) {
			float mx = InputController.getInstance().getCrossHair().x;
			float my = InputController.getInstance().getCrossHair().y;

			QueryCallback qc = new QueryCallback() {
				@Override
				public boolean reportFixture(Fixture fixture) {
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
				}
			};

			world.QueryAABB(qc,mx,my,mx,my);
			inputThresher = THRESHER_RESET;
		}

		if (InputController.getInstance().isEKeyPressed()) {
			float mx = InputController.getInstance().getCrossHair().x;
			float my = InputController.getInstance().getCrossHair().y;

			QueryCallback qc = new QueryCallback() {
				@Override
				public boolean reportFixture(Fixture fixture) {
					Object userData = fixture.getBody().getUserData();
					for (Obstacle o : objects) {
						if (o == userData) {
							promptTemplate(o);
							objects.remove(o);
							return false;
						}

						if (o instanceof ComplexObstacle) {
							for (Obstacle oo : ((ComplexObstacle) o).getBodies()) {
								if (oo == userData) {
									promptTemplate(o);
									objects.remove(o);
									return false;
								}
							}
						}
					}
					return true;
				}
			};

			world.QueryAABB(qc,mx,my,mx,my);
			inputThresher = THRESHER_RESET;
		}

		if (InputController.getInstance().isNKeyPressed()) {
			String name = showInputDialog("what do u wanna call this mess?");
			currentLevel = name;
			inputThresher = THRESHER_RESET_LONG;
		}

		if (InputController.getInstance().isLKeyPressed()) {
			currentLevel = showInputDialog("what do u wanna load?");
			reset();
		}

		if (InputController.getInstance().isSKeyPressed()) {
			System.out.println("Saving...");
			LevelModel timeToSave = new LevelModel();
			timeToSave.setTitle(currentLevel);
			for (Obstacle o : objects) {
				timeToSave.addEntity(o);
			}
			if (jls.saveLevel(timeToSave, currentLevel)) {
				System.out.println("Saved!");
			} else {
				System.err.println("ERROR IN SAVE");
			}
			inputThresher = THRESHER_RESET;
		}

		if (InputController.getInstance().isLeftKeyPressed()) {
			tentativeIndex = (tentativeIndex + 1 + creationOptions.length) % creationOptions.length;
			inputThresher = THRESHER_RESET_LONG;
		}
		if (InputController.getInstance().isRightKeyPressed()) {
			tentativeIndex = (tentativeIndex - 1 + creationOptions.length) % creationOptions.length;
			inputThresher = THRESHER_RESET_LONG;
		}
		if (InputController.getInstance().isEnterKeyPressed()) {
			actualindex = tentativeIndex;
			inputThresher = THRESHER_RESET_LONG;
		}
		if (InputController.getInstance().isTKeyPressed()) {
			trialLevelName = currentLevel;
		}

		if (InputController.getInstance().isGKeyPressed()) {
			promptGlobalConfig();
		}
	}

	@Override
	public void draw(float delta) {
		canvas.clear();

		canvas.begin();
		for(Obstacle obj : objects) {
			obj.draw(canvas);
		}
		canvas.end();


		// Final message
		canvas.begin(); // DO NOT SCALE
		canvas.drawTextStandard("Level: " + currentLevel, 10.0f, 100.0f);
		canvas.drawTextStandard("Creating: " + creationOptions[tentativeIndex], 10.0f, 80.0f);
		if (tentativeIndex != actualindex) {
			canvas.drawTextStandard("Hit Enter to Select New Object Type.", 10.0f, 60.0f);
		}
		canvas.end();
	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * But trevor uses it for something else
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {

	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */
	public void endContact(Contact contact) {

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
		jls.setScale(this.scale);
	}
}