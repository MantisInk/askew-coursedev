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
    private boolean prevLeftUp, prevLeftDown, prevLeftLeft, prevLeftRight;        // keep track of previous joystick positions
    private boolean leftUp, leftDown, leftLeft, leftRight;                        // track current joystick positions
    @Getter
    private String nextCon = "";
    private BitmapFont regina;
    private BitmapFont regina1;
    private BitmapFont regina2;

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
        if (mode == HOME_SCREEN) {
            for (int i = 0; i < home_text_locs.length; i++) {
                canvas.drawTextAlignedRight(home_text[i], regina, home_text_locs[i].x * canvas.getWidth(), home_text_locs[i].y * canvas.getHeight(), fontcolor);
            }
            canvas.draw(fern, Color.WHITE, fern.getWidth() / 2, fern.getHeight() / 2,
                    home_button_locs[home_button].x * canvas.getWidth(), home_button_locs[home_button].y * canvas.getHeight(),
                    0, worldScale.x / fern.getWidth(), worldScale.y / fern.getHeight());
        }
        // TODO: new level select screen
        else if (mode == LEVEL_SELECT) {
            canvas.draw(menu2);
            canvas.drawText("         " + selected, displayFont, 0.42f * canvas.getWidth(), 0.45f * canvas.getHeight());
            canvas.draw(fern, Color.WHITE, fern.getWidth() / 2, fern.getHeight() / 2,
                    select_button_locs[select_button].x * canvas.getWidth(), select_button_locs[select_button].y * canvas.getHeight(),
                    0, worldScale.x / fern.getWidth(), worldScale.y / fern.getHeight());
        } else if (mode == SETTINGS) {
            for (int i = 0; i < settings_text_locs.length; i++) {
                if (i == 0 || i == 3 || i == 6 || i == 9)
                    canvas.drawTextAlignedRight(settings_text[i], regina, settings_text_locs[i].x * canvas.getWidth(), settings_text_locs[i].y * canvas.getHeight(), fontcolor);
            }
            if (control) {
                canvas.drawText(settings_text[2], regina2, settings_text_locs[2].x * canvas.getWidth(), settings_text_locs[2].y * canvas.getHeight());
                canvas.drawText(settings_text[1], regina1, settings_text_locs[1].x * canvas.getWidth(), settings_text_locs[1].y * canvas.getHeight());
            } else {
                canvas.drawText(settings_text[1], regina2, settings_text_locs[1].x * canvas.getWidth(), settings_text_locs[1].y * canvas.getHeight());
                canvas.drawText(settings_text[2], regina1, settings_text_locs[2].x * canvas.getWidth(), settings_text_locs[2].y * canvas.getHeight());
            }
            if (grab) {
                canvas.drawText(settings_text[4], regina2, settings_text_locs[4].x * canvas.getWidth(), settings_text_locs[4].y * canvas.getHeight());
                canvas.drawText(settings_text[5], regina1, settings_text_locs[5].x * canvas.getWidth(), settings_text_locs[5].y * canvas.getHeight());
            } else {
                canvas.drawText(settings_text[5], regina2, settings_text_locs[5].x * canvas.getWidth(), settings_text_locs[5].y * canvas.getHeight());
                canvas.drawText(settings_text[4], regina1, settings_text_locs[4].x * canvas.getWidth(), settings_text_locs[4].y * canvas.getHeight());
            }
            if (graphics) {
                canvas.drawText(settings_text[8], regina2, settings_text_locs[8].x * canvas.getWidth(), settings_text_locs[8].y * canvas.getHeight());
                canvas.drawText(settings_text[7], regina1, settings_text_locs[7].x * canvas.getWidth(), settings_text_locs[7].y * canvas.getHeight());
            } else {
                canvas.drawText(settings_text[7], regina2, settings_text_locs[7].x * canvas.getWidth(), settings_text_locs[7].y * canvas.getHeight());
                canvas.drawText(settings_text[8], regina1, settings_text_locs[8].x * canvas.getWidth(), settings_text_locs[8].y * canvas.getHeight());
            }
            int swtch;
            switch (settings_button) {
                case 0:
                    swtch = (control) ? 1 : 0;
                    canvas.draw(fern, Color.WHITE, fern.getWidth() / 2, fern.getHeight() / 2,
                            settings_button_locs[swtch].x * canvas.getWidth(), settings_button_locs[swtch].y * canvas.getHeight(),
                            0, worldScale.x / fern.getWidth(), worldScale.y / fern.getHeight());
                    break;
                case 1:
                    swtch = (grab) ? 2 : 3;
                    canvas.draw(fern, Color.WHITE, fern.getWidth() / 2, fern.getHeight() / 2,
                            settings_button_locs[swtch].x * canvas.getWidth(), settings_button_locs[swtch].y * canvas.getHeight(),
                            0, worldScale.x / fern.getWidth(), worldScale.y / fern.getHeight());
                    break;
                case 2:
                    swtch = (graphics) ? 5 : 4;
                    canvas.draw(fern, Color.WHITE, fern.getWidth() / 2, fern.getHeight() / 2,
                            settings_button_locs[swtch].x * canvas.getWidth(), settings_button_locs[swtch].y * canvas.getHeight(),
                            0, worldScale.x / fern.getWidth(), worldScale.y / fern.getHeight());
                    break;
                default:
                    swtch = 6;
                    canvas.draw(fern, Color.WHITE, fern.getWidth() / 2, fern.getHeight() / 2,
                            settings_button_locs[swtch].x * canvas.getWidth(), settings_button_locs[swtch].y * canvas.getHeight(),
                            0, worldScale.x / fern.getWidth(), worldScale.y / fern.getHeight());
            }
        }
        canvas.end();
    }

    @Override
    public void reset() {
        bounds = new Rectangle(0, 0, 16.0f, 9.0f);
        Gdx.input.setCursorCatched(false);
        nextCon = "";
        SoundController instance = SoundController.getInstance();
        if (playingMusic) {
            if (instance.isActive("bgmusic")) instance.stop("bgmusic");
            if (!instance.isActive("menumusic"))
                instance.play("menumusic", MENU_MUSIC, true, GameModeController.MAX_MUSIC_VOLUME);
        }
    }

    @Override
    public void update(float dt) {
        InputController input = InputControllerManager.getInstance().getController(0);
        if (mode == HOME_SCREEN) {
            if (mode != prevMode) {
                return;
            }
//            System.out.println(mode+" "+home_button);
            if ((input.didBottomButtonPress() || input.didEnterKeyPress()) && home_button == PLAY_BUTTON) {
                selected = GlobalConfiguration.getInstance().getCurrentLevel();
                if (selected > MAX_LEVEL) {
                    selected = 1;
                    GlobalConfiguration.getInstance().setCurrentLevel(selected);
                }
                System.out.println("selected " + selected);
                nextCon = "GM";
                return;
            } else if ((input.didBottomButtonPress() || input.didEnterKeyPress()) && home_button == TUTORIAL_BUTTON) {
                nextCon = "TL";
                return;
            } else if ((input.didBottomButtonPress() || input.didEnterKeyPress()) && home_button == LEVEL_SELECT_BUTTON) {
                mode = LEVEL_SELECT;
                select_button = CHOOSE_LEVEL;
                selected = 1;
            } else if ((input.didBottomButtonPress() || input.didEnterKeyPress()) && home_button == SETTINGS_BUTTON) {
                mode = SETTINGS;
                settings_button = CONTROL_SCHEME;
            } else if ((input.didBottomButtonPress() || input.didEnterKeyPress()) && home_button == QUIT_BUTTON) {
                listener.exitScreen(this, EXIT_QUIT);
            }

            if (input.didTopDPadPress() || input.didUpArrowPress() || (!prevLeftUp && leftUp)) {
                if (home_button > 0) {
                    home_button--;
                }
            } else if (input.didBottomDPadPress() || input.didDownArrowPress() || (!prevLeftDown && leftDown)) {
                if (home_button < home_button_locs.length - 1) {
                    home_button++;
                }
            }
        }
        if (mode == LEVEL_SELECT) {
            if (mode != prevMode)
                return;

            if ((input.didBottomButtonPress() || input.didEnterKeyPress()) && select_button == RETURN_HOME) {
                System.out.println("return home");
                mode = HOME_SCREEN;
                home_button = PLAY_BUTTON;
            } else if ((input.didBottomButtonPress() || input.didEnterKeyPress()) && select_button == CHOOSE_LEVEL) {
                GlobalConfiguration.getInstance().setCurrentLevel(selected);
                System.out.println("selected level");
                nextCon = "GM";
                return;
            }

            if ((input.didLeftDPadPress() || (leftLeft && !prevLeftLeft) || input.didLeftArrowPress()) && selected > minLevel && select_button == CHOOSE_LEVEL) {
                selected--;
            } else if ((input.didRightDPadPress() || (leftRight && !prevLeftRight) || input.didRightArrowPress()) && selected < maxLevel && select_button == CHOOSE_LEVEL) {
                selected++;
            }

            if (input.didTopDPadPress() || input.didUpArrowPress() || (leftUp && !prevLeftUp)) {
                select_button = CHOOSE_LEVEL;
            } else if (input.didBottomDPadPress() || input.didDownArrowPress() || (leftDown && !prevLeftDown)) {
                select_button = RETURN_HOME;
            }
        }
        if (mode == SETTINGS) {
            if (mode != prevMode)
                return;

            if ((input.didBottomButtonPress() || input.didEnterKeyPress()) && settings_button == SETTINGS_RETURN_HOME) {
                System.out.println("return home");
                mode = HOME_SCREEN;
                home_button = PLAY_BUTTON;
                GlobalConfiguration.getInstance().setFlowControlMode(control);
                GlobalConfiguration.getInstance().setFlowMovementMode(grab);
                GlobalConfiguration.getInstance().setGraphicsQuality(graphics);
            }

            if (input.didTopDPadPress() || input.didUpArrowPress() || (!prevLeftUp && leftUp)) {
                if (settings_button > 0) {
                    settings_button--;
                }
            } else if (input.didBottomDPadPress() || input.didDownArrowPress() || (!prevLeftDown && leftDown)) {
                if (settings_button < NUM_SETTINGS) {
                    settings_button++;
                }
            } else if (input.didRightDPadPress() || input.didRightArrowPress() || (!prevLeftRight && leftRight)) {
                if (settings_button == CONTROL_SCHEME) {
                    control = !control;
                }
                if (settings_button == GRAB_CONTROL) {
                    grab = !grab;
                }
                if (settings_button == GRAPHICS_QUALITY) {
                    graphics = !graphics;
                }
            } else if (input.didLeftDPadPress() || input.didLeftArrowPress() || (!prevLeftLeft && leftLeft)) {
                if (settings_button == CONTROL_SCHEME) {
                    control = !control;
                }
                if (settings_button == GRAB_CONTROL) {
                    grab = !grab;
                }
                if (settings_button == GRAPHICS_QUALITY) {
                    graphics = !graphics;
                }
            }
        }

    }
}
