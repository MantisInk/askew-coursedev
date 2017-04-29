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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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

	private final int DID_NOTHING = 0;
	private final int MOVED_LEFT = 1;
	private final int MOVED_RIGHT = 2;
	private final int GRABBED_LEFT = 3;
	private final int GRABBED_RIGHT = 4;
	private final int SWING_LEFT = 5;
	private final int REGRABBED_LEFT = 6;
	private int stepsDone = DID_NOTHING;

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

		return true;
	}

	public void drawInstructions() {
		joystickNeutralTexture = joystickAnimation.getKeyFrame(0);
		joystickTexture = joystickAnimation.getKeyFrame(elapseTime, true);
		bumperLTexture = bumperLAnimation.getKeyFrame(elapseTime,true);
		bumperRTexture = bumperRAnimation.getKeyFrame(elapseTime, true);

		canvas.draw(container, Color.WHITE, container.getWidth() / 2, 0, 425, 300, 0, worldScale.x * 5 / container.getWidth(), worldScale.y * 5 / container.getHeight());
		if (stepsDone == DID_NOTHING) {
			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
		} else if (stepsDone == MOVED_LEFT) {
			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
		} else if (stepsDone == MOVED_RIGHT) {
			canvas.draw(bumperLTexture, Color.WHITE, bumperLTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperLTexture.getRegionWidth(), worldScale.y * 3 / bumperLTexture.getRegionHeight());
			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
		} else if (stepsDone == GRABBED_LEFT) {
			canvas.draw(bumperRTexture, Color.WHITE, bumperRTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperRTexture.getRegionWidth(), worldScale.y * 3 / bumperRTexture.getRegionHeight());
			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
		} else if (stepsDone >= GRABBED_RIGHT) {
			if(sloth.isRightGrab()) {
				canvas.draw(bumperRTexture, Color.WHITE, bumperLTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperLTexture.getRegionWidth(), worldScale.y * 3 / bumperLTexture.getRegionHeight());
				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
			}
			else {
				canvas.draw(bumperLTexture, Color.WHITE, bumperRTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperRTexture.getRegionWidth(), worldScale.y * 3 / bumperRTexture.getRegionHeight());
				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
			}
		}
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
//			float angleL = 0;//(lAngle - prevLAngle);
//			float angleR = 0;//(rAngle - prevRAngle);
//			if (Math.abs(angleL) > CONTROLLER_DEADZONE) {
//				dLAngle += angleL;
//				prevLAngle = angleL;
//			}
//			if (Math.abs(angleR) > CONTROLLER_DEADZONE) {
//				dRAngle += angleR;
//				prevRAngle = angleR;
//			}

			//System.out.println("before case ("+rAngle+","+cw+","+ccw+")");
			// TODO: move sloth movement in slothmodel
			switch (stepsDone){
				case DID_NOTHING:
					//System.out.println("during case0 ("+rAngle+","+cw+","+ccw+")");
					sloth.setRightHori(0);
					sloth.setRightVert(0);
					break;
				case MOVED_LEFT:
					sloth.setLeftHori(0);
					sloth.setLeftVert(0);
					//System.out.println("during case1 ("+rAngle+","+cw+","+ccw+")");
//					sloth.setLeftGrab(false);
					break;
				case MOVED_RIGHT:
					if (sloth.controlMode == 0)
						sloth.setRightGrab(false);
					break;
				case GRABBED_LEFT:
					//sloth.setLeftGrab(true);
					break;
				case GRABBED_RIGHT:
					//sloth.setRightGrab(true);
					//Set right arm to be 0 too?
					break;
				case SWING_LEFT:
					//Let go of left grab
					//sloth.setRightGrab(true);
					break;
				case REGRABBED_LEFT:
					break;
				default:
					System.err.println(stepsDone);

			}

//			boolean didSafe = InputController.getInstance().getRightGrab();

			//Increment Steps
			System.out.println(stepsDone);
			InputController input = InputController.getInstance();
			if(stepsDone==DID_NOTHING){
				//Check for left joystick movement
				if(Math.abs(input.getLeftHorizontal())>CONTROLLER_DEADZONE || Math.abs(input.getLeftVertical())>CONTROLLER_DEADZONE){
					if(Math.abs(lAngle) > 4*Math.PI) {
						cw = false;
						ccw = false;
						stepsDone++;
					}
				}
			}
			else if(stepsDone==MOVED_LEFT){
				//Check for right joystick movement
				if(Math.abs(input.getRightHorizontal())>CONTROLLER_DEADZONE || Math.abs(input.getRightVertical())>CONTROLLER_DEADZONE){
					if (Math.abs(2*Math.PI+rAngle) > 3*Math.PI) {
						cw = false;
						ccw = false;
						stepsDone++;
					}
				}
			}
			else if(stepsDone==MOVED_RIGHT){
				if (sloth.isActualRightGrab()) {
						stepsDone++;
				}

				//Check for left grab
				if(sloth.isActualLeftGrab()){
					stepsDone++;
				}
			}
			else if(stepsDone==GRABBED_LEFT){
				if(sloth.isActualRightGrab()){
					stepsDone++;
				}
			}
			else if(stepsDone==GRABBED_RIGHT){
				//Check for left joystick movement
				if(Math.abs(input.getLeftHorizontal())>CONTROLLER_DEADZONE || Math.abs(input.getLeftVertical())>CONTROLLER_DEADZONE){
					stepsDone++;
				}
			}
			else if(stepsDone==SWING_LEFT){
				if(sloth.isActualLeftGrab()){ //TODO Check for left hand crossing right hand and grabbing
					stepsDone++;
				}
			}
			else if(stepsDone==REGRABBED_LEFT){
				//End of scripted checks, else if statement left here for sanity's sake
				//stepsDone++;
			}
		}
	}

	public void draw(float delta){
		super.draw(delta);
		// draw instructional animations
		canvas.begin();
		drawInstructions();
		canvas.end();

	}

}