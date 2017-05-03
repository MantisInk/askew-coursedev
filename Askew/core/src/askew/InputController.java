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

	@Getter
	private boolean rightClickPressed;
	private boolean rightClickPrevious;
	@Getter
	private boolean leftClickPressed;
	private boolean leftClickPrevious;

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
	@Getter
	private boolean shiftKeyPressed;
	@Getter
	private boolean spaceKeyPressed;

	@Getter
	private boolean vKeyPressed;
	@Getter
	private boolean lShiftKeyPressed;
	@Getter
	private boolean rShiftKeyPressed;
	@Getter
	private boolean oneKeyPressed;
	@Getter
	private boolean twoKeyPressed;
	@Getter
	private boolean threeKeyPressed;
	@Getter
	private boolean zKeyPressed;
	@Getter
	private boolean xKeyPressed;


	private boolean sKeyPrevious;
	private boolean nKeyPrevious;
	private boolean lKeyPrevious;
	private boolean leftKeyPrevious;
	private boolean rightKeyPrevious;
	private boolean enterKeyPrevious;
	private boolean tKeyPrevious;
	private boolean eKeyPrevious;
	private boolean gKeyPrevious;
	private boolean hKeyPrevious;
	private boolean bKeyPrevious;
	private boolean vKeyPrevious;
	private boolean lShiftKeyPrevious;
	private boolean rShiftKeyPrevious;


	// Fields to manage buttons
	private boolean startButtonPressed;
	private boolean startButtonPrevious;

	@Getter
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

	@Getter
	private boolean upKeyPressed;
	private boolean upKeyPrevious;

	@Getter
	private boolean downKeyPressed;
	private boolean downKeyPrevious;




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
	@Getter
	private XBox360Controller xbox;
	@Getter
	private boolean altKeyPressed;
	@Getter
	private boolean dotKeyPressed;

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

	public boolean isBottomButtonPressed() {
		return bottomButtonPressed;
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

	public boolean didLeftDPadPress() { return rightDPadPressed && !rightDPadPrevious; }

	public boolean didRightDPadPress() { return leftDPadPressed && !leftDPadPrevious; }

	public boolean didBottomDPadPress() { return bottomDPadPressed && !bottomDPadPrevious; }

	public boolean didLeftClick(){ return leftClickPressed && !leftClickPrevious; }

	public boolean didLeftDrag(){ return leftClickPressed && leftClickPrevious; }

	public boolean didLeftRelease(){ return !leftClickPressed && leftClickPrevious; }

	public boolean didRightClick(){ return rightClickPressed && !leftClickPrevious; }

	public boolean didRightDrag(){ return rightClickPressed && rightClickPrevious; }

	public boolean didRightRelease(){ return !rightClickPressed && rightClickPrevious; }

	public boolean didUpArrowPress() {return upKeyPressed && !upKeyPrevious;}

	public boolean didRightArrowPress() {
		// for some reason left/right is flipped
		return leftKeyPressed && !leftKeyPrevious;
	}

	public boolean didLeftArrowPress() {
		// for some reason left/right is flipped
		return rightKeyPressed&& !rightKeyPrevious;
	}

	public boolean didDownArrowPress() {return downKeyPressed && !downKeyPrevious;}

	public boolean didEnterKeyPress() {return enterKeyPressed && !enterKeyPrevious;}




	/**
	 * Creates a new input controller
	 *
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController(int id) {
		// If we have a game-pad for id, then use it.
		xbox = new XBox360Controller(id);
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

		leftClickPrevious = leftClickPressed;
		rightClickPrevious = rightClickPressed;

		sKeyPrevious = isSKeyPressed();
		nKeyPrevious = isNKeyPressed();
		lKeyPrevious = isLKeyPressed();
		leftKeyPrevious = isLeftKeyPressed();
		rightKeyPrevious = isRightKeyPressed();
		enterKeyPrevious = isEnterKeyPressed();
		tKeyPrevious = isTKeyPressed();
		eKeyPrevious = isEKeyPressed();
		gKeyPrevious = isGKeyPressed();
		hKeyPrevious = isHKeyPressed();
		bKeyPrevious = isBKeyPressed();
		vKeyPrevious = isVKeyPressed();
		lShiftKeyPrevious = isLShiftKeyPressed();
		rShiftKeyPrevious = isRShiftKeyPressed();
		upKeyPrevious = isUpKeyPressed();
		downKeyPrevious = isDownKeyPressed();

		// Check to see if a GamePad is connected
		if (xbox.isConnected()) {
			readGamepad(bounds, scale);
			readKeyboard(bounds, scale, true); // Read as a back-up
		} else {
//			System.err.println("no 360 controller connected");
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
		topButtonPressed = (secondary && topButtonPressed) || Gdx.input.isKeyPressed(Input.Keys.U);
		bottomButtonPressed = (secondary && bottomButtonPressed) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.K);;
		rightButtonPressed = (secondary && rightButtonPressed) || (Gdx.input.isKeyPressed(Input.Keys.P));
		leftButtonPressed = (secondary && leftButtonPressed) ;//(Gdx.input.isKeyPressed(Input.Keys.N));
		backButtonPressed = (secondary && backButtonPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
		rightGrabPressed = (secondary && rightGrabPressed) || ((Gdx.input.isKeyPressed(Input.Keys.SPACE))) || Gdx.input.isKeyPressed(Input.Keys.J);

		// for some reason left/right arrows are switched
//		upArrowPressed = (secondary && upArrowPressed) || (Gdx.input.isKeyPressed(Input.Keys.UP));
//		rightArrowPressed = (secondary && rightArrowPressed) || (Gdx.input.isKeyPressed(Input.Keys.LEFT));
//		leftArrowPressed = (secondary && leftArrowPressed) || (Gdx.input.isKeyPressed(Input.Keys.RIGHT));
//		downArrowPressed = (secondary && downArrowPressed) || (Gdx.input.isKeyPressed(Input.Keys.DOWN));

		// Directional controls
		leftHorizontal = (secondary ? leftHorizontal : 0.0f);
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			leftHorizontal += 1.0f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			leftHorizontal -= 1.0f;
		}

		leftVertical = (secondary ? leftVertical : 0.0f);
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			leftVertical -= 1.0f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			leftVertical += 1.0f;
		}


		// Mouse results
		leftClickPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		rightClickPressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);


		// Keypresses for level editor hotkeys

		sKeyPressed = Gdx.input.isKeyPressed(Input.Keys.S) && !sKeyPrevious;
		nKeyPressed = Gdx.input.isKeyPressed(Input.Keys.N) && !nKeyPrevious;
		lKeyPressed = Gdx.input.isKeyPressed(Input.Keys.L) && !lKeyPrevious;
		eKeyPressed = Gdx.input.isKeyPressed(Input.Keys.E) && !eKeyPrevious;
		gKeyPressed = Gdx.input.isKeyPressed(Input.Keys.G) && !gKeyPrevious;
		hKeyPressed = Gdx.input.isKeyPressed(Input.Keys.H) && !hKeyPrevious;
		altKeyPressed = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT);
		dotKeyPressed = Gdx.input.isKeyPressed(Input.Keys.PERIOD);

		//Keypresses for toggling control schemes
		oneKeyPressed = Gdx.input.isKeyPressed(Input.Keys.NUM_1);
		twoKeyPressed = Gdx.input.isKeyPressed(Input.Keys.NUM_2);
		threeKeyPressed = Gdx.input.isKeyPressed(Input.Keys.NUM_3);
		zKeyPressed = Gdx.input.isKeyPressed(Input.Keys.Z);
		xKeyPressed = Gdx.input.isKeyPressed(Input.Keys.X);


		leftKeyPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) && !leftKeyPrevious;
		rightKeyPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !rightKeyPrevious;
		enterKeyPressed = Gdx.input.isKeyPressed(Input.Keys.ENTER) && !enterKeyPrevious;
		tKeyPressed = Gdx.input.isKeyPressed(Input.Keys.T) && !tKeyPrevious;
		bKeyPressed = Gdx.input.isKeyPressed(Input.Keys.B) && !bKeyPrevious;
		vKeyPressed = Gdx.input.isKeyPressed(Input.Keys.V) && !vKeyPrevious;

		lShiftKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
		rShiftKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
		shiftKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
		spaceKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

		lShiftKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !lShiftKeyPressed;
		rShiftKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
		upKeyPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
		downKeyPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);
		rightDPadPressed |= Gdx.input.isKeyPressed(Input.Keys.P);

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
