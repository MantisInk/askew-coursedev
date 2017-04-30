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
package askew.playermode.gamemode;

import askew.InputController;
import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.entity.obstacle.WheelObstacle;
import askew.entity.tree.Trunk;
import askew.entity.vine.Vine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

/**
 * Gameplay specific controller for the platformer game.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class TutorialModeController extends GameModeController {

	private final int STAGE_PINNED = 0;
	private final int STAGE_GRAB = 1;
	private final int STAGE_SHIMMY = 2;
	private final int STAGE_FLING = 3;
	private final int STAGE_VINE = 4;
	private int stepsDone = STAGE_PINNED;

	private final float CONTROLLER_DEADZONE = 0.15f;

	private float lAngle;
	private float rAngle;
	private boolean cw;
	private boolean ccw;

	private Animation joystickAnimation;
	private Animation bumperLAnimation;
	private Animation bumperRAnimation;
	private float elapseTime;

	// selected animation textures to be drawn
	TextureRegion joystickNeutralTexture;
	TextureRegion joystickTexture;
	TextureRegion bumperLTexture;
	TextureRegion bumperRTexture;
	Texture container;

	// list of objects for stage of tutorial
	protected ArrayList<Entity> tutorialEntities = new ArrayList<Entity>();

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
		container = manager.get("texture/tutorial/infoContainer.png");
		// reset animation frames
		if(joystickAnimation == null) {
			joystickAnimation = new Animation(0.15f, manager.getTextureAtlas().findRegions("joy"), Animation.PlayMode.LOOP);
			bumperLAnimation = new Animation(0.20f, manager.getTextureAtlas().findRegions("bumperL"), Animation.PlayMode.LOOP);
			bumperRAnimation = new Animation(0.20f, manager.getTextureAtlas().findRegions("bumperR"), Animation.PlayMode.LOOP);
		}
		DEFAULT_LEVEL = "tutorial0";
		loadLevel = DEFAULT_LEVEL;
	}

	// Physics objects for the game
	/** Reference to the character avatar */

	public TutorialModeController() {
		super();
		stepsDone = 0;
		tutorialEntities.clear();
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		super.reset();
		stepsDone=0;
		joystickTexture = joystickAnimation.getKeyFrame(0);
		bumperLTexture = bumperLAnimation.getKeyFrame(0);
		bumperRTexture = bumperRAnimation.getKeyFrame(0);
	}

	/**
	 * Lays out the game geography.
	 */
	@Override
	protected void populateLevel() {
		super.populateLevel();
		for(Entity e: objects) {
			if(e instanceof Trunk) {
				if (stepsDone >= STAGE_GRAB) {
					tutorialEntities.add(e);
				}
			} else if (e instanceof Vine) {
				if (stepsDone >= STAGE_VINE) {
					tutorialEntities.add(e);
				}
			} else {
				tutorialEntities.add(e);
			}
		}
		float sloth_x = sloth.getX();
		float sloth_y = sloth.getY();
		WheelObstacle pin = new WheelObstacle(sloth_x,sloth_y,1);
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
		if(stepsDone>STAGE_VINE) {
			System.out.println("finished tutorial");
			listener.exitScreen(this, EXIT_TL_GM);
			return false;
		}
		return true;
	}

	public void drawInstructions() {
		joystickNeutralTexture = joystickAnimation.getKeyFrame(0);
		joystickTexture = joystickAnimation.getKeyFrame(elapseTime, true);
		bumperLTexture = bumperLAnimation.getKeyFrame(elapseTime,true);
		bumperRTexture = bumperRAnimation.getKeyFrame(elapseTime, true);

		canvas.draw(container, Color.WHITE, container.getWidth() / 2, 0, 425, 300, 0, worldScale.x * 5 / container.getWidth(), worldScale.y * 5 / container.getHeight());
//		if (stepsDone == DID_NOTHING) {
//			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//		} else if (stepsDone == MOVED_LEFT) {
//			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//		} else if (stepsDone == MOVED_RIGHT) {
//			canvas.draw(bumperLTexture, Color.WHITE, bumperLTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperLTexture.getRegionWidth(), worldScale.y * 3 / bumperLTexture.getRegionHeight());
//			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//		} else if (stepsDone == GRABBED_LEFT) {
//			canvas.draw(bumperRTexture, Color.WHITE, bumperRTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperRTexture.getRegionWidth(), worldScale.y * 3 / bumperRTexture.getRegionHeight());
//			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//		} else if (stepsDone >= GRABBED_RIGHT) {
//			if(sloth.isRightGrab()) {
//				canvas.draw(bumperRTexture, Color.WHITE, bumperLTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperLTexture.getRegionWidth(), worldScale.y * 3 / bumperLTexture.getRegionHeight());
//				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//			}
//			else {
//				canvas.draw(bumperLTexture, Color.WHITE, bumperRTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperRTexture.getRegionWidth(), worldScale.y * 3 / bumperRTexture.getRegionHeight());
//				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//			}
//		}
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class askew.playermode.WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		super.update(dt);
		if (!paused) {
			// Process actions in object model
			lAngle = (float) ((sloth.getLeftArm().getAngle()) - 1.5*Math.PI);//+3.14)%3.14);
			rAngle = (float) ((sloth.getRightArm().getAngle()) - 1.5*Math.PI);//+ 3.14)%3.14);
			System.out.print(lAngle+" ");
			System.out.println(rAngle);

			// TODO: move sloth movement in slothmodel

			//Increment Steps
			System.out.println(stepsDone);
			InputController input = InputController.getInstance();

			switch(stepsDone) {
				case STAGE_PINNED:
					if(checkPinned())
						stepsDone++;
					break;
				case STAGE_GRAB:
					if(checkGrabbed())
						stepsDone++;
					break;
				case STAGE_SHIMMY:
					if(checkShimmied())
						stepsDone++;
					break;
				case STAGE_FLING:
					if(checkFlinged())
						stepsDone++;
					break;
				case STAGE_VINE:
					if(checkVined())
						stepsDone++;
					break;
				default:
					System.err.println(stepsDone);
			}
		}
	}

	public boolean checkPinned(){
		return false;
	}
	public boolean checkGrabbed() {
		return false;
	}
	public boolean checkShimmied() {
		return false;
	}
	public boolean checkFlinged() {
		return false;
	}
	public boolean checkVined() {
		return false;
	}

	public void draw(float delta){
		super.draw(delta);
		// draw instructional animations
		canvas.begin();
		drawInstructions();
		canvas.end();

	}

}