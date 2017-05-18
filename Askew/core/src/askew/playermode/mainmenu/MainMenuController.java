package askew.playermode.mainmenu;

import askew.GlobalConfiguration;
import askew.InputController;
import askew.InputControllerManager;
import askew.MantisAssetManager;
import askew.playermode.WorldController;
import askew.playermode.gamemode.GameModeController;
import askew.util.SoundController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("FieldCanBeLocal")
public class MainMenuController extends WorldController {
    private static final String FERN_TEXTURE = "texture/background/fern.png";
    private static final String MENU_BACKGROUND1_TEXTURE = "texture/background/menu1.png";
    private static final String MENU_BACKGROUND2_TEXTURE = "texture/background/menu2.png";
    private static final String MENU_BACKGROUND_TEXTURE = "texture/background/menu_background.png";
    private static final String MENU_MUSIC = "sound/music/levelselect.ogg";
    private final int MAX_LEVEL;
    private final Color fontcolor = new Color(.984f, .545f, .384f, 1);
    // main menu modes
    private final int HOME_SCREEN = 0;
    private final int LEVEL_SELECT = 1;
    private final int SETTINGS = 2;
    // home mode options
    private final int PLAY_BUTTON = 0;
    private final int TUTORIAL_BUTTON = 1;
    private final int LEVEL_SELECT_BUTTON = 2;
    private final int SETTINGS_BUTTON = 3;
    private final int QUIT_BUTTON = 4;
    private final String[] home_text = {"Play", "Tutorial", "Level Select", "Settings", "Quit"};
    private final Vector2[] home_text_locs = {
            new Vector2(0.6f, 0.54f),
            new Vector2(0.6f, 0.47f),
            new Vector2(0.6f, 0.40f),
            new Vector2(0.6f, 0.33f),
            new Vector2(0.6f, 0.26f)};
    private final Vector2[] home_button_locs = {
            new Vector2(0.65f, 0.53f),
            new Vector2(0.65f, 0.46f),
            new Vector2(0.65f, 0.39f),
            new Vector2(0.65f, 0.32f),
            new Vector2(0.65f, 0.25f)};
    // level select mode options
    private final int CHOOSE_LEVEL = 0;
    private final int RETURN_HOME = 1;
    // settings mode options
    private final int CONTROL_SCHEME = 0;
    private final int GRAB_CONTROL = 1;
    private final int GRAPHICS_QUALITY = 2;
    private final int SETTINGS_RETURN_HOME = 3;
    private final int NUM_SETTINGS = 3;
    private final Vector2[] settings_button_locs = {
            new Vector2(0.43f, 0.53f), new Vector2(0.68f, 0.53f),
            new Vector2(0.43f, 0.43f), new Vector2(0.68f, 0.43f),
            new Vector2(0.43f, 0.33f), new Vector2(0.68f, 0.33f),
            new Vector2(0.43f, 0.23f)
    };
    private final FileHandle fontFile = Gdx.files.internal("font/ReginaFree.ttf");
    private Vector2[] select_button_locs = {
            new Vector2(0.65f, 0.45f),
            new Vector2(0.65f, 0.35f)
    };
    private boolean graphics = false;       // false means low graphics
    private String[] settings_text = {"Control Scheme", "One Arm", "Two Arm", "Grab Scheme", "Hold to Grab", "Release to Grab", "Graphics", "Low", "High", "Main Menu"};
    private Vector2[] settings_text_locs = {
            new Vector2(0.4f, 0.54f), new Vector2(0.45f, 0.54f), new Vector2(0.7f, 0.54f),
            new Vector2(0.4f, 0.44f), new Vector2(0.45f, 0.44f), new Vector2(0.7f, 0.44f),
            new Vector2(0.4f, 0.34f), new Vector2(0.45f, 0.34f), new Vector2(0.7f, 0.34f),
            new Vector2(0.4f, 0.24f)
    };
    @Getter
    @Setter
    private int selected = 1;
    @Getter
    @Setter
    private int minLevel = 1;
    @Getter
    @Setter
    private int maxLevel = 10;
    private int mode;
    private int prevMode;
    private int home_button = PLAY_BUTTON;
    private int select_button = CHOOSE_LEVEL;
    private int settings_button = CONTROL_SCHEME;
    private boolean control = false;        // false means one arm control scheme
    private boolean grab = false;           // false means hold to grab
    private boolean music = false;
    private boolean prevLeftUp, prevLeftDown, prevLeftLeft, prevLeftRight;        // keep track of previous joystick positions
    private boolean leftUp, leftDown, leftLeft, leftRight;                        // track current joystick positions
    @Getter
    private String nextCon = "";
    private BitmapFont regina;
    private BitmapFont regina1;
    private BitmapFont regina2;
    private MantisAssetManager manager;

    private Texture fern, menu1, menu2, menu;

    public MainMenuController() {
        mode = PLAY_BUTTON;
        prevLeftUp = false;
        prevLeftDown = false;
        prevLeftLeft = false;
        prevLeftRight = false;
        leftUp = false;
        leftDown = false;
        leftLeft = false;
        leftRight = false;
        MAX_LEVEL = GlobalConfiguration.getInstance().getAsInt("maxLevel");
        control = GlobalConfiguration.getInstance().getAsInt("flowControlMode") != 1;
        grab = GlobalConfiguration.getInstance().getAsInt("flowMovementMode") != 1;
        graphics = GlobalConfiguration.getInstance().getAsInt("graphics") == 1;
    }

    // player selected another mode
    public void preLoadContent(MantisAssetManager manager) {
        manager.load(MENU_MUSIC, Sound.class);
        super.preLoadContent(manager);
    }

    @Override
    public void loadContent(MantisAssetManager manager) {
        super.loadContent(manager);
        fern = manager.get(FERN_TEXTURE);
        menu1 = manager.get(MENU_BACKGROUND1_TEXTURE);
        menu2 = manager.get(MENU_BACKGROUND2_TEXTURE);
        menu = manager.get(MENU_BACKGROUND_TEXTURE);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        int textscale = 1;
        param.size = 56 * textscale;
        regina = generator.generateFont(param);
        param.color = fontcolor;
        param.size = 32 * textscale;
//        param.color = Color.GREEN;
        regina1 = generator.generateFont(param);
        param.size = 44 * textscale;
        param.shadowColor = Color.BLACK;
        param.shadowOffsetX = 1;
        param.shadowOffsetY = 1;
        regina2 = generator.generateFont(param);
        generator.dispose();
        SoundController.getInstance().allocate(manager, MENU_MUSIC);
        this.manager = manager;
    }

    @Override
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }

        InputController input = InputControllerManager.getInstance().getController(0);

        if (((input.didRightDPadPress() && mode == HOME_SCREEN && prevMode == mode) || nextCon.equals("GM"))) {
            System.out.println("GM");
            listener.exitScreen(this, EXIT_MM_GM);
            return false;
        } else if (input.didLeftDPadPress() && mode == HOME_SCREEN && prevMode == mode) {
            System.out.println("LE");
            listener.exitScreen(this, EXIT_MM_LE);
            return false;
        } else if (nextCon.equals("TL") && prevMode == mode) {
            listener.exitScreen(this, EXIT_MM_TL);
            return false;
        } else if (input.didBottomDPadPress()) {
            reset();
        }

        prevLeftUp = leftUp;
        prevLeftDown = leftDown;
        prevLeftLeft = leftLeft;
        prevLeftRight = leftRight;

        leftUp = input.getLeftVertical() < -0.5;
        leftDown = input.getLeftVertical() > 0.5;
        leftLeft = input.getLeftHorizontal() > 0.5;
        leftRight = input.getLeftHorizontal() < -0.5;

        prevMode = mode;
        return true;
    }

    @Override
    public void draw(float delta) {
        canvas.clear();
        displayFont.setColor(fontcolor);

        canvas.begin(); // DO NOT SCALE
        canvas.draw(menu);
        canvas.end();
        canvas.begin();
        manager.getMenuManager().draw();
        canvas.end();
    }

    @Override
    public void reset() {
        super.reset();
        bounds = new Rectangle(0, 0, 16.0f, 9.0f);
        Gdx.input.setCursorCatched(false);
        nextCon = "";
        SoundController instance = SoundController.getInstance();
        if (playingMusic) {
            if (instance.isActive("bgmusic")) instance.stop("bgmusic");
            if (!instance.isActive("menumusic"))
                instance.play("menumusic", MENU_MUSIC, true, GameModeController.MAX_MUSIC_VOLUME);
        }
        manager.getMenuManager().setupMainMenu();
        mode = HOME_SCREEN;
    }

    @Override
    public void update(float dt) {
        InputController input = InputControllerManager.getInstance().getController(0);
        if (mode == HOME_SCREEN) {
            if (mode != prevMode) {
                return;
            }
            String updateString = manager.getMenuManager().update().orElse("");
            if (!updateString.contains
                    ("ACTION")) {
                if (updateString.contains("Play")) {
                    selected = GlobalConfiguration.getInstance().getCurrentLevel();
                    if (selected > MAX_LEVEL) {
                        selected = 1;
                        GlobalConfiguration.getInstance().setCurrentLevel(selected);
                    }
                    nextCon = "GM";
                } else if (updateString.contains("Tutorial")) {
                    nextCon = "TL";
                } else if (updateString.contains("Level Select")) {
                    mode = LEVEL_SELECT;
                    select_button = CHOOSE_LEVEL;
                    selected = 1;
                    manager.getMenuManager().setupLevelSelectMenu(selected);
                } else if (updateString.contains("Settings")) {
                    mode = SETTINGS;
                    settings_button = CONTROL_SCHEME;
                    manager.getMenuManager().setupSettingsMenu(control,grab,
                            graphics,music);
                } else if (updateString.contains("Exit")) {
                    listener.exitScreen(this, EXIT_QUIT);
                }
            }
        } else if (mode == LEVEL_SELECT) {
            if (mode != prevMode)
                return;

            String updateString = manager.getMenuManager().update().orElse("");
            if (updateString.contains("Main Menu") && (!updateString.contains
                    ("ACTION"))) {
                mode = HOME_SCREEN;
                manager.getMenuManager().setupMainMenu();
            } else if (updateString.contains("Level")) {
                if (updateString.contains("ACTION_RIGHT")) {
                    if (selected > minLevel) selected--;
                    manager.getMenuManager().updateButtonContainingText("Level",
                            "Level: " + selected);
                } else if (updateString.contains("ACTION_LEFT")) {
                    if (selected < maxLevel) selected++;
                    manager.getMenuManager().updateButtonContainingText("Level",
                            "Level: " + selected);
                } else {
                    GlobalConfiguration.getInstance().setCurrentLevel(selected);
                    nextCon = "GM";
                }
            }
        } else if (mode == SETTINGS) {
            if (mode != prevMode)
                return;

            String updateString = manager.getMenuManager().update().orElse("");
            if (updateString.contains("Main Menu") && (!updateString.contains
                    ("ACTION"))) {
                mode = HOME_SCREEN;
                manager.getMenuManager().setupMainMenu();
                mode = HOME_SCREEN;
                home_button = PLAY_BUTTON;
                GlobalConfiguration.getInstance().setFlowControlMode(control);
                GlobalConfiguration.getInstance().setFlowMovementMode(grab);
                GlobalConfiguration.getInstance().setGraphicsQuality(graphics);
                GlobalConfiguration.getInstance().setMusic(music);
            } else if (updateString.contains("Control Scheme")) {
                if (updateString.contains("ACTION_RIGHT") || updateString
                        .contains("ACTION_LEFT")) {
                    control = !control;
                    manager.getMenuManager().updateButtonContainingText
                            ("Control", "Control Scheme: " +
                                    (control ? "One Arm" : "Two Arm"));
                }
            } else if (updateString.contains("Grab Scheme")) {
                if (updateString.contains("ACTION_RIGHT") || updateString
                        .contains("ACTION_LEFT")) {
                    grab = !grab;
                    manager.getMenuManager().updateButtonContainingText
                            ("Grab", "Grab Scheme: " +
                                    (grab ? "Normal" : "Reverse"));
                }
            } else if (updateString.contains("Graphics Quality")) {
                if (updateString.contains("ACTION_RIGHT") || updateString
                        .contains("ACTION_LEFT")) {
                    graphics = !graphics;
                    manager.getMenuManager().updateButtonContainingText
                            ("Graphics", "Graphics Quality: " +
                                    (graphics ? "LOW" : "HIGH"));
                }
            } else if (updateString.contains("Music")) {
                if (updateString.contains("ACTION_RIGHT") || updateString
                        .contains("ACTION_LEFT")) {
                    music = !music;
                    manager.getMenuManager().updateButtonContainingText
                            ("Music", "Music: " + (music ? "ON" : "OFF"));
                }
            }
        }

    }
}
