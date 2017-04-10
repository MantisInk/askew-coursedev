package askew;/*
 * askew.InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;

import askew.util.*;
import lombok.Getter;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
	// Sensitivity for moving crosshair with gameplay
	private static final float GP_ACCELERATE = 1.0f;
	private static final float GP_MAX_SPEED  = 10.0f;
	private static final float GP_THRESHOLD  = 0.01f;

	/** The singleton instance of the input controller */
	private static InputController theController = null;

	/**
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputController getInstance() {
		if (theController == null) {
			theController = new InputController();
		}
		return theController;
	}

	@Getter
	private boolean rightClickPressed;
	@Getter
	private boolean leftClickPressed;

	// Keyboard keys for the level editor
	@Getter
	private boolean sKeyPressed;
	@Getter
	private boolean nKeyPressed;
	@Getter
	private boolean lKeyPressed;
	@Getter
	private boolean leftKeyPressed;
	@Getter
	private boolean rightKeyPressed;
	@Getter
	private boolean enterKeyPressed;
	@Getter
	private boolean tKeyPressed;
	@Getter
	private boolean eKeyPressed;
	@Getter
	private boolean gKeyPressed;
	@Getter
	private boolean hKeyPressed;
	@Getter
	private boolean bKeyPressed;


	// Fields to manage buttons
	private boolean startButtonPressed;
	private boolean startButtonPrevious;

	private boolean leftButtonPressed;
	private boolean leftButtonPrevious;

	private boolean rightButtonPressed;
	private boolean rightButtonPrevious;

	private boolean bottomButtonPressed;
	private boolean bottomButtonPrevious;

	private boolean topButtonPressed;
	private boolean topButtonPrevious;

	private boolean backButtonPressed;
	private boolean backButtonPrevious;

	//Fields to manage DPad

	private boolean topDPadPressed;
	private boolean topDPadPrevious;

	private boolean rightDPadPressed;
	private boolean rightDPadPrevious;

	private boolean leftDPadPressed;
	private boolean leftDPadPrevious;

	private boolean bottomDPadPressed;
	private boolean bottomDPadPrevious;




	/** Whether the right hand is grabbing. */
	private boolean leftGrabPressed;
	/** Whether the left hand is grabbing. */
	private boolean rightGrabPressed;

	private boolean leftStickPressed;
	private boolean rightStickPressed;

	/** How much did left arm move horizontally? */
	private float leftHorizontal;
	/** How much did left arm move move vertically? */
	private float leftVertical;
	/** How much did right arm move horizontally? */
	private float rightHorizontal;
	/** How much did right arm move move vertically? */
	private float rightVertical;

	/** The crosshair position (for raddoll) */
	private Vector2 crosshair;
	/** The crosshair cache (for using as a return value) */
	private Vector2 crosscache;
	/** For the gamepad crosshair control */
	private float momentum;

	/** An X-Box controller (if it is connected) */
	XBox360Controller xbox;

	/**
	 * Returns the amount of sideways movement for the left arm.
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement.
	 */
	public float getLeftHorizontal() {
		return leftHorizontal;
	}

	/**
	 * Returns the amount of vertical movement for the left arm.
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement.
	 */
	public float getLeftVertical() {
		return leftVertical;
	}

	/**
	 * Returns the amount of sideways movement for the left arm.
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement.
	 */
	public float getRightHorizontal() {
		return rightHorizontal;
	}

	/**
	 * Returns the amount of vertical movement for the left arm.
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement.
	 */
	public float getRightVertical() {
		return rightVertical;
	}

	/**
	 * Returns how much the left trigger button was pressed.
	 *
	 * This is a value between 0 and 1, where 0 is no pressure.
	 *
	 * This is a sustained button. It will returns true as long as the player
	 * holds it down.
	 *
	 * @return how much the left trigger button was pressed.
	 */
	public boolean getLeftGrab() {
		return leftGrabPressed;
	}

	/**
	 * Returns how much the right trigger button was pressed.
	 *
	 * This is a value between 0 and 1, where 0 is no pressure.
	 *
	 * This is a sustained button. It will returns true as long as the player
	 * holds it down.
	 *
	 * @return how much the right trigger button was pressed.
	 */
	public boolean getRightGrab() {
		return rightGrabPressed;
	}

	public boolean getLeftStickPressed() {return leftStickPressed;}
	public boolean getRightStickPressed() {return  rightStickPressed;}

	/**
	 * Returns the current position of the crosshairs on the screen.
	 *
	 * This value does not return the actual reference to the crosshairs position.
	 * That way this method can be called multiple times without any fair that
	 * the position has been corrupted.  However, it does return the same object
	 * each time.  So if you modify the object, the object will be reset in a
	 * subsequent call to this getter.
	 *
	 * @return the current position of the crosshairs on the screen.
	 */
	public Vector2 getCrossHair() {
		return crosscache.set(crosshair);
	}


	/**
	 * Returns true if the corresponding button was pressed.
	 *
	 * @return true if the corresponding button was pressed.
	 */
	public boolean didBottomButtonPress() {
		return bottomButtonPressed && !bottomButtonPrevious;
	}

	public boolean didStartPress() {
		return startButtonPressed && !startButtonPrevious;
	}

	public boolean didLeftButtonPress() {
		return leftButtonPressed && !leftButtonPrevious;
	}

	public boolean didRightButtonPress() {
		return rightButtonPressed && !rightButtonPrevious;
	}

	public boolean didTopButtonPress() {
		return topButtonPressed && !topButtonPrevious;
	}

	public boolean didBackPressed() {
		return backButtonPressed && !backButtonPrevious;
	}

	/**
	 * Returns true if the corresponding dpad was pressed.
	 *
	 * @return true if the corresponding dpad was pressed.
	 */
	public boolean didTopDPadPress() { return topDPadPressed && !topDPadPrevious; }

	public boolean didRightDPadPress() { return rightDPadPressed && !rightDPadPrevious; }

	public boolean didLeftDPadPress() { return leftDPadPressed && !leftDPadPrevious; }

	public boolean didBottomDPadPress() { return bottomDPadPressed && !bottomDPadPrevious; }



	/**
	 * Creates a new input controller
	 *
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() {
		// If we have a game-pad for id, then use it.
		xbox = new XBox360Controller(0);
		crosshair = new Vector2();
		crosscache = new Vector2();
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.
	 * @param scale  The drawing scale
	 */
	public void readInput(Rectangle bounds, Vector2 scale) {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		bottomButtonPrevious = bottomButtonPressed;
		startButtonPrevious = startButtonPressed;
		topButtonPrevious = topButtonPressed;
		backButtonPrevious = backButtonPressed;
		leftButtonPrevious = leftButtonPressed;
		rightButtonPrevious = rightButtonPressed;

		topDPadPrevious = topDPadPressed;
		rightDPadPrevious = rightDPadPressed;
		leftDPadPrevious = leftDPadPressed;
		bottomDPadPrevious = bottomDPadPressed;


		// Check to see if a GamePad is connected
		if (xbox.isConnected()) {
			readGamepad(bounds, scale);
			readKeyboard(bounds, scale, true); // Read as a back-up
		} else {
			System.err.println("no 360 controller connected");
			readKeyboard(bounds, scale, false);
		}
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.
	 * @param scale  The drawing scale
	 */
	private void readGamepad(Rectangle bounds, Vector2 scale) {
		startButtonPressed = xbox.getStart();
		backButtonPressed = xbox.getBack();
		leftButtonPressed = xbox.getX();
		rightButtonPressed = xbox.getB();
		bottomButtonPressed = xbox.getA();
		topButtonPressed = xbox.getY();
		topDPadPressed = xbox.getDPadUp();
		rightDPadPressed = xbox.getDPadRight();
		leftDPadPressed = xbox.getDPadLeft();
		bottomDPadPressed = xbox.getDPadDown();

		//Check if hands are grabbing
		//leftGrabPressed = xbox.getLeftTrigger();
		leftGrabPressed = xbox.getLB();
		//rightGrabPressed = xbox.getRightTrigger();
		rightGrabPressed = xbox.getRB();

		leftStickPressed = xbox.getL3();
		rightStickPressed = xbox.getR3();


		//Get positions of joysticks/arms
		leftHorizontal = xbox.getLeftX();
		leftVertical   = xbox.getLeftY();
		rightHorizontal = xbox.getRightX();
		rightVertical   = xbox.getRightY();


		crosscache.set(xbox.getLeftX(), xbox.getLeftY());
		if (crosscache.len2() > GP_THRESHOLD) {
			momentum += GP_ACCELERATE;
			momentum = Math.min(momentum, GP_MAX_SPEED);
			crosscache.scl(momentum);
			crosscache.scl(1/scale.x,1/scale.y);
			crosshair.add(crosscache);
		} else {
			momentum = 0;
		}
		clampPosition(bounds);
	}

	// only for forcing release on reset
	public void releaseGrabs(){
		leftGrabPressed = false;
		rightGrabPressed = false;
	}

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */
	private void readKeyboard(Rectangle bounds, Vector2 scale, boolean secondary) {
		// Give priority to gamepad results
		startButtonPressed = (secondary && startButtonPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
		topButtonPressed = (secondary && topButtonPressed) || (Gdx.input.isKeyPressed(Input.Keys.D));
		bottomButtonPressed = (secondary && bottomButtonPressed) || (Gdx.input.isKeyPressed(Input.Keys.UP));
		rightButtonPressed = (secondary && rightButtonPressed) || (Gdx.input.isKeyPressed(Input.Keys.P));
		leftButtonPressed = (secondary && leftButtonPressed) ;//(Gdx.input.isKeyPressed(Input.Keys.N));
		backButtonPressed = (secondary && backButtonPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));

		// Directional controls
		leftHorizontal = (secondary ? leftHorizontal : 0.0f);
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			leftHorizontal += 1.0f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			leftHorizontal -= 1.0f;
		}

		leftVertical = (secondary ? leftVertical : 0.0f);
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			leftVertical += 1.0f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			leftVertical -= 1.0f;
		}

		// Mouse results
		leftClickPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		rightClickPressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);

		// Keypresses for level editor hotkeys
		sKeyPressed =  Gdx.input.isKeyPressed(Input.Keys.S);
		nKeyPressed =  Gdx.input.isKeyPressed(Input.Keys.N);
		lKeyPressed =  Gdx.input.isKeyPressed(Input.Keys.L);
		eKeyPressed =  Gdx.input.isKeyPressed(Input.Keys.E);
		gKeyPressed = Gdx.input.isKeyPressed(Input.Keys.G);
		hKeyPressed = Gdx.input.isKeyPressed(Input.Keys.H);

		leftKeyPressed =  Gdx.input.isKeyPressed(Input.Keys.LEFT);
		rightKeyPressed =  Gdx.input.isKeyPressed(Input.Keys.RIGHT);
		enterKeyPressed =  Gdx.input.isKeyPressed(Input.Keys.ENTER);
		tKeyPressed =  Gdx.input.isKeyPressed(Input.Keys.T);
		bKeyPressed = Gdx.input.isKeyPressed(Input.Keys.B);
		crosshair.set(Gdx.input.getX(), Gdx.input.getY());
		crosshair.scl(1/scale.x,-1/scale.y);
		crosshair.y += bounds.height;
		clampPosition(bounds);
	}

	/**
	 * Clamp the cursor position so that it does not go outside the window
	 *
	 * While this is not usually a problem with mouse control, this is critical 
	 * for the gamepad controls.
	 */
	private void clampPosition(Rectangle bounds) {
		crosshair.x = Math.max(bounds.x, Math.min(bounds.x+bounds.width, crosshair.x));
		crosshair.y = Math.max(bounds.y, Math.min(bounds.y+bounds.height, crosshair.y));
	}

}