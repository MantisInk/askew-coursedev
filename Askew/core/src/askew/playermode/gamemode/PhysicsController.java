package askew.playermode.gamemode;

import askew.GlobalConfiguration;
import askew.InputController;
import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.entity.owl.OwlModel;
import askew.playermode.WorldController;
import askew.playermode.leveleditor.LevelModel;
import askew.util.SoundController;
import askew.util.json.JSONLoaderSaver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ObjectSet;
import lombok.Getter;
import lombok.Setter;
import askew.entity.obstacle.BoxObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.obstacle.PolygonObstacle;
import askew.entity.sloth.SlothModel;

import java.io.FileNotFoundException;


public class PhysicsController implements ContactListener {
    @Getter
    private Body leftBody;
    @Getter
    private Body rightBody;
    @Getter
    private boolean isFlowKill;
    @Getter @Setter
    private SlothModel sloth;
    @Getter @Setter
    private BoxObstacle goalDoor;
    @Getter
    private boolean isFlowWin;

    /**
     * This function deals with collisions.
     * Fixture collision mask groups:
     * 0x0001 ->
     *
     *
     */
    public PhysicsController() {
        reset();
    }

    public void reset(){
        sloth = null;
        goalDoor = null;
        isFlowKill = false;
        isFlowWin = false;
        clearGrab();
    }



    @Override
    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            if (fd1 != null && fd1.equals("sloth left hand")  && (!sloth.badBodies().contains(bd2)) && (!(bd2 instanceof PolygonObstacle))) {
                leftBody = body2;
            }
            if (fd1 != null && fd1.equals("sloth right hand")  && bd2 != sloth && (!sloth.badBodies().contains(bd2))&& (!(bd2 instanceof PolygonObstacle))) {
                rightBody = body2;
            }

            if (fd2 != null && fd2.equals("sloth left hand")  && bd1 != sloth && (!sloth.badBodies().contains(bd1))&& (!(bd1 instanceof PolygonObstacle))) {
                leftBody = body1;
            }
            if (fd2 != null && fd2.equals("sloth right hand")  && bd1 != sloth && (!sloth.badBodies().contains(bd1))&& (!(bd1 instanceof PolygonObstacle))) {
                rightBody = body1;
            }

            // Check for thorns
            if (bd1 != null && bd2 != null && ((bd1.getName() != null && bd1.getName().equals("slothpart")) ||( bd2.getName() != null && bd2.getName().equals("slothpart")))) {
                Obstacle slothy;
                Obstacle other;
                if (bd1.getName() != null && bd1.getName().equals("slothpart")) {
                    slothy = bd1;
                    other = bd2;
                } else {
                    slothy = bd2;
                    other = bd1;
                }

                if (other.getName() != null && (other.getName().equals("thorns") || other.getName().equals("ghost") )) {
                    System.out.println("GG TODO KILL FLOW");
                    isFlowKill = true;
                }

                if (other.getName() != null && other.getName().equals("owl") ) {
                    System.out.println("Hoot hoot");
                    isFlowWin = true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();    
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if (fd1 != null && fd1.equals("sloth left hand") && body2 == leftBody) {
            leftBody = null;

        }
        if (fd2 != null && fd2.equals("sloth left hand") && body1 == leftBody) {
            leftBody = null;
        }
        if (fd1 != null && fd1.equals("sloth right hand") && body2 == rightBody) {
            rightBody = null;
        }
        if (fd2 != null && fd2.equals("sloth right hand") && body1 == rightBody) {
            rightBody = null;
        }
    }

    // only for forcing release on reset
    public void clearGrab(){
        leftBody = null;
        rightBody = null;
    }
    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

    /**
     * Gameplay specific controller for the platformer game.
     *
     * You will notice that asset loading is not done with static methods this time.
     * Instance asset loading makes it easier to process our game modes in a loop, which
     * is much more scalable. However, we still want the assets themselves to be static.
     * This is the purpose of our AssetState variable; it ensures that multiple instances
     * place nicely with the static assets.
     */
    public static class TutorialController extends WorldController {


        Affine2 camTrans = new Affine2();

        /** Track asset loading from all instances and subclasses */
        private AssetState platformAssetState = AssetState.EMPTY;

        /** Track asset loading from all instances and subclasses */
        @Getter
        private static boolean playerIsReady = false;
        private boolean paused = false;
        private boolean prevPaused = false;
        // fern selection indicator locations for pause menu options
        private Vector2[] pause_locs = {new Vector2(11f,4.8f), new Vector2(9f,3.9f), new Vector2(11f,3f)};

        @Setter
        private String loadLevel, DEFAULT_LEVEL;
        private LevelModel lm; 				// LevelModel for the level the player is currently on
        private int numLevel, MAX_LEVEL; 	// track int val of lvl #

        private float currentTime, recordTime;	// track current and record time to complete level

        private PhysicsController collisions;

        private JSONLoaderSaver jsonLoaderSaver;
        private float initFlowX;
        private float initFlowY;
        private int PAUSE_RESUME = 0;
        private int PAUSE_RESTART = 1;
        private int PAUSE_MAINMENU = 2;
        private int pause_mode = PAUSE_RESUME;
        private Texture background;
        private Texture pauseTexture;
        private Texture fern;

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
            if (platformAssetState != AssetState.EMPTY) {
                return;
            }
            manager.load("sound/music/askew.wav", Sound.class);
            platformAssetState = AssetState.LOADING;
            jsonLoaderSaver.setManager(manager);
            super.preLoadContent(manager);
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
            if (platformAssetState != AssetState.LOADING) {
                return;
            }

            //SoundController sounds = SoundController.getInstance();
            SoundController.getInstance().allocate(manager, "sound/music/askew.wav");
            background = manager.get("texture/background/background1.png", Texture.class);
            pauseTexture = manager.get("texture/background/pause.png", Texture.class);
            fern = manager.get("texture/background/fern.png");

            super.loadContent(manager);
            platformAssetState = AssetState.COMPLETE;
        }


        // Physics constants for initialization
        /** The new heavier gravity for this world (so it is not so floaty) */
        private static final float  DEFAULT_GRAVITY = -10.7f;



        // Physics objects for the game
        /** Reference to the character avatar */

        private static SlothModel sloth;
        private static OwlModel owl;

        /** Reference to the goalDoor (for collision detection) */
        private BoxObstacle goalDoor;

        /** Mark set to handle more sophisticated collision callbacks */
        protected ObjectSet<Fixture> sensorFixtures;

        /**
         * Creates and initialize a new instance of the platformer game
         *
         * The game has default gravity and other settings
         */
        public TutorialController() {
            super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
            setDebug(false);
            setComplete(false);
            setFailure(false);
            collisions = new PhysicsController();
            world.setContactListener(collisions);
            sensorFixtures = new ObjectSet<Fixture>();
            DEFAULT_LEVEL = GlobalConfiguration.getInstance().getAsString("defaultLevel");
            MAX_LEVEL = GlobalConfiguration.getInstance().getAsInt("maxLevel");
            loadLevel = DEFAULT_LEVEL;
            jsonLoaderSaver = new JSONLoaderSaver();
        }

        public void setLevel() {
            //numLevel = lvl;
            int lvl = GlobalConfiguration.getInstance().getCurrentLevel();
            if (lvl == 0) {
                loadLevel = DEFAULT_LEVEL;
            } else if (lvl > MAX_LEVEL) {
                loadLevel = "level"+MAX_LEVEL;
                System.out.println("MM");
                listener.exitScreen(this, EXIT_GM_MM);
            } else
                loadLevel = "level"+lvl;
        }

        public void pause(){
            if (!paused) {
                paused = true;
                pause_mode = PAUSE_RESUME;
            }
            else {
                paused = false;
            }
            playerIsReady = false;
        }

        /**
         * Resets the status of the game so that we can play again.
         *
         * This method disposes of the world and creates a new one.
         */
        public void reset() {

            playerIsReady = false;
            paused = false;
            collisions.clearGrab();
            Vector2 gravity = new Vector2(world.getGravity() );

            InputController.getInstance().releaseGrabs();
            for(Entity obj : objects) {
                if( (obj instanceof Obstacle && !(obj instanceof SlothModel)))
                    ((Obstacle)obj).deactivatePhysics(world);
            }

            objects.clear();
            addQueue.clear();
            world.dispose();

            world = new World(gravity,false);
            if(collisions == null){
                collisions = new PhysicsController();
            }
            collisions.reset();

            world.setContactListener(collisions);
            setComplete(false);
            setFailure(false);
            setLevel();
            populateLevel();
            if (!SoundController.getInstance().isActive("bgmusic"))
                SoundController.getInstance().play("bgmusic","sound/music/askew.wav",true);
        }

        /**
         * Lays out the game geography.
         */
        private void populateLevel() {
                jsonLoaderSaver.setScale(this.worldScale);
                try {
                    lm = jsonLoaderSaver.loadLevel(loadLevel);
                    System.out.println(loadLevel);
                    recordTime = lm.getRecordTime();
                    if (lm == null) {
                        lm = new LevelModel();
                    }

                    for (Entity o : lm.getEntities()) {
                        // drawing

                        addObject( o);
                        if (o instanceof SlothModel) {
                            sloth = (SlothModel) o;
                            sloth.activateSlothPhysics(world);
                            collisions.setSloth(sloth);
                            initFlowX = sloth.getX();
                            initFlowY = sloth.getY();
                        }
                        if (o instanceof OwlModel) {
                            owl = (OwlModel) o;
                        }

                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                currentTime = 0f;

        }

        /**For drawing force lines*/
        public static SlothModel getSloth(){return sloth;}

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

            if (input.didLeftButtonPress() || input.isLKeyPressed()) {
                System.out.println("LE");
                listener.exitScreen(this, EXIT_GM_LE);
                return false;
            } else if (input.didTopButtonPress()) {
                System.out.println("MM");
                listener.exitScreen(this, EXIT_GM_MM);
                return false;
            }

            if (paused) {
                //InputController input = InputController.getInstance();
                if (input.didBottomButtonPress() && pause_mode == PAUSE_RESUME) {
                    paused = false;
                    playerIsReady = false;
                } else if (input.didBottomButtonPress() && pause_mode == PAUSE_RESTART) {
                    reset();
                } else if (input.didBottomButtonPress() && pause_mode == PAUSE_MAINMENU) {
                    System.out.println("MM");
                    listener.exitScreen(this, EXIT_GM_MM);
                }

                if (input.didTopDPadPress() && pause_mode > 0) {
                    pause_mode--;
                }
                if (input.didBottomDPadPress() && pause_mode < 2) {
                    pause_mode++;
                }
            }

            //Checks to see if player has selected the button on the starting screen
            if(!playerIsReady){
                if(checkReady()){
                    playerIsReady = true;
                }
                else{
                    return false;
                }
            }

            return true;
        }

        /**
         * Checks to see if the player has pressed any button. This is used to
         * indicate that the player is ready to start the level.
         *
         * @return whether the player has pressed a button
         */
        public boolean checkReady(){
            InputController theController = InputController.getInstance();

            if (paused)
                return false;

            //If the player pressed "RB"
            if(theController.getRightGrab()){
                return true;
            }
            //If the player pressed "LB"
            else if(theController.getLeftGrab()){
                return true;
            }

            return false;
        }

        public void printHelp(){
            //Display waiting text if not ready
            displayFont.setColor(Color.YELLOW);
            canvas.drawText("Hold RB/LB \n to start!", displayFont, initFlowX * worldScale.x, initFlowY * worldScale.y + 200f);
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
            if (!paused) {
                // Process actions in object model
                sloth.setLeftHori(InputController.getInstance().getLeftHorizontal());
                sloth.setLeftVert(InputController.getInstance().getLeftVertical());
                sloth.setRightHori(InputController.getInstance().getRightHorizontal());
                sloth.setRightVert(InputController.getInstance().getRightVertical());
                sloth.setLeftGrab(InputController.getInstance().getLeftGrab());
                sloth.setRightGrab(InputController.getInstance().getRightGrab());
                sloth.setLeftStickPressed(InputController.getInstance().getLeftStickPressed());
                sloth.setRightStickPressed(InputController.getInstance().getRightStickPressed());
                currentTime += dt;

                //#TODO Collision states check
                setFailure(collisions.isFlowKill());

                Body leftCollisionBody = collisions.getLeftBody();
                Body rightCollisionBody = collisions.getRightBody();

                if (collisions.isFlowWin()) {
                    System.out.println("VICTORY");
                    setComplete(true);
                }

                // Physics tiem
                // Gribby grab
                if (sloth.isLeftGrab()) {
                    sloth.grab(world, leftCollisionBody, true);
                } else {
                    sloth.releaseLeft(world);
                }

                if (sloth.isRightGrab()) {
                    sloth.grab(world, rightCollisionBody, false);
                } else {
                    sloth.releaseRight(world);
                }

                // Normal physics
                sloth.doThePhysics();

                // If we use sound, we must remember this.
                SoundController.getInstance().update();

                if (isComplete()) {
                    float record = currentTime;
                    if (record < lm.getRecordTime()) {
                        lm.setRecordTime(record);
                        if (jsonLoaderSaver.saveLevel(lm, loadLevel))
                            System.out.println("New record time for this level!");
                    }
                    int current = GlobalConfiguration.getInstance().getCurrentLevel();
                    GlobalConfiguration.getInstance().setCurrentLevel(current + 1);
                    System.out.println("GG");
                    listener.exitScreen(this, EXIT_GM_GM);
                }

                if (isFailure()) {
                    System.out.println("Fail");
                    reset();
                }
            }
            prevPaused = paused;
        }

        public void draw(float delta){
            canvas.clear();

            camTrans.setToTranslation(-1 * sloth.getBody().getPosition().x * worldScale.x
                    , -1 * sloth.getBody().getPosition().y * worldScale.y);

            camTrans.translate(canvas.getWidth()/2,canvas.getHeight()/2);

            canvas.begin();
            canvas.draw(background);
            canvas.drawTextStandard("current time:    "+currentTime, 10f, 70f);
            canvas.drawTextStandard("record time:     "+recordTime,10f,50f);
            canvas.end();

            canvas.begin(camTrans);
            //canvas.draw(background, Color.WHITE, .25f*background.getWidth(),.75f * background.getHeight(),initFlowX*worldScale.x,initFlowY*worldScale.y,background.getWidth(), background.getHeight());

            for(Entity obj : objects) {
                obj.setDrawScale(worldScale);
                obj.draw(canvas);
            }

            if (!playerIsReady && !paused)
                printHelp();
            canvas.end();
            sloth.drawGrab(canvas, camTrans);


            if (debug) {
                canvas.beginDebug(camTrans);
                for(Entity obj : objects) {
                    if( obj instanceof  Obstacle){
                        ((Obstacle)obj).drawDebug(canvas);
                    }

                }

                canvas.endDebug();
                canvas.begin();
                // text
                canvas.drawTextStandard("FPS: " + 1f/delta, 10.0f, 100.0f);
                canvas.end();
                sloth.drawForces(canvas, camTrans);
            }

            // draw pause menu stuff over everything
            if (paused) {
                canvas.begin();
                canvas.draw(pauseTexture);

                canvas.draw(fern, Color.WHITE,fern.getWidth()/2, fern.getHeight()/2,
                        pause_locs[pause_mode].x * worldScale.x, pause_locs[pause_mode].y* worldScale.y,
                        0,worldScale.x/fern.getWidth(),worldScale.y/fern.getHeight());

                canvas.end();
            }

            canvas.begin();
            if (prevPaused && !paused && !playerIsReady)
                printHelp();
            canvas.end();
        }

    }
}
