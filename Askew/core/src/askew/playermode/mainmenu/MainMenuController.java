package askew.playermode.mainmenu;

import askew.GlobalConfiguration;
import askew.InputController;
import askew.InputControllerManager;
import askew.MantisAssetManager;
import askew.playermode.WorldController;
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

public class MainMenuController extends WorldController {
    @Getter @Setter
    private int selected = 1;
    @Getter @Setter
    private int minLevel = 1;
    @Getter @Setter
    private int maxLevel = 10;

    private int MAX_LEVEL;

    // main menu modes
    private final int HOME_SCREEN = 0;
    private final int LEVEL_SELECT = 1;
    private int mode;
    private int prevMode;

    // home mode options
    private final int PLAY_BUTTON = 0;
    private final int TUTORIAL_BUTTON = 1;
    private final int LEVEL_SELECT_BUTTON = 2;
    private final int QUIT_BUTTON = 3;
    private int home_button = PLAY_BUTTON;
    private String[] home_text = {"Play", "Tutorial", "Level Select", "Quit"};
    private Vector2[] home_text_locs = {new Vector2(0.6f, 0.54f), new Vector2(0.6f, 0.47f), new Vector2(0.6f, 0.40f), new Vector2(0.6f,0.33f)};
    private Vector2[] home_button_locs = {new Vector2(0.65f,0.53f), new Vector2(0.65f,0.46f), new Vector2(0.65f,0.39f), new Vector2(0.65f, 0.32f)};

    // level select mode options
    private final int CHOOSE_LEVEL = 0;
    private final int RETURN_HOME = 1;
    private int select_button = CHOOSE_LEVEL;
    private Vector2[] select_button_locs = {new Vector2(10f, 3.8f), new Vector2(10f, 2.8f)};

    private static final String FERN_TEXTURE = "texture/background/fern.png";
    private static final String MENU_BACKGROUND1_TEXTURE = "texture/background/menu1.png";
    private static final String MENU_BACKGROUND2_TEXTURE = "texture/background/menu2.png";
    private static final String MENU_BACKGROUND_TEXTURE = "texture/background/menu_background.png";
    private boolean prevLeftUp, prevLeftDown,prevLeftLeft,prevLeftRight;        // keep track of previous joystick positions
    private boolean leftUp, leftDown,leftLeft,leftRight;                        // track current joystick positions
    public static final String MENU_MUSIC = "sound/music/levelselect.ogg";

    FileHandle fontFile = Gdx.files.internal("font/ReginaFree.ttf");
    @Getter
    BitmapFont regina;
    private Texture fern, menu1, menu2, menu;

    // player selected another mode

    public void preLoadContent(MantisAssetManager manager) {
        manager.load(MENU_MUSIC, Sound.class);
        super.preLoadContent(manager);
    }
    private String nextCon = "";

    @Override
    public void loadContent(MantisAssetManager manager) {
        super.loadContent(manager);
        fern = manager.get(FERN_TEXTURE);
        menu1 = manager.get(MENU_BACKGROUND1_TEXTURE);
        menu2 = manager.get(MENU_BACKGROUND2_TEXTURE);
        menu = manager.get(MENU_BACKGROUND_TEXTURE);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 56;
        regina = generator.generateFont(param);
        generator.dispose();
        SoundController.getInstance().allocate(manager, MENU_MUSIC);
    }

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
    }

    @Override
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }

        InputController input = InputControllerManager.getInstance().getController(0);

        if (((input.didRightDPadPress() && mode == HOME_SCREEN) || nextCon.equals("GM"))) {
            System.out.println("GM");
            listener.exitScreen(this, EXIT_MM_GM);
            return false;
        } else if (input.didLeftDPadPress() && mode != LEVEL_SELECT) {
            System.out.println("LE");
            listener.exitScreen(this, EXIT_MM_LE);
            return false;
        } else if (nextCon.equals("TL")){
            listener.exitScreen(this, EXIT_MM_TL);
            return false;
        } else if (input.didBottomDPadPress()) {
            reset();
        }

        prevLeftUp = leftUp;
        prevLeftDown = leftDown;
        prevLeftLeft = leftLeft;
        prevLeftRight = leftRight;

        leftUp = input.getLeftVertical()<-0.5;
        leftDown = input.getLeftVertical()>0.5;
        leftLeft = input.getLeftHorizontal()>0.5;
        leftRight = input.getLeftHorizontal()<-0.5;

        prevMode = mode;
        return true;
    }

    @Override
    public void draw(float delta) {
        canvas.clear();
        displayFont.setColor(Color.GREEN);

        canvas.begin(); // DO NOT SCALE
        canvas.draw(menu);
        if(mode == HOME_SCREEN) {
            for(int i = 0; i < home_text_locs.length; i++) {
                canvas.drawTextAlignedRight(home_text[i], regina, home_text_locs[i].x * canvas.getWidth(), home_text_locs[i].y * canvas.getHeight(), Color.GREEN);
            }
            canvas.draw(fern, Color.WHITE,fern.getWidth()/2, fern.getHeight()/2,
                    home_button_locs[home_button].x * canvas.getWidth(), home_button_locs[home_button].y* canvas.getHeight(),
                    0, worldScale.x/fern.getWidth(),worldScale.y/fern.getHeight());
        }
        // TODO: new level select screen
        else if(mode == LEVEL_SELECT) {
            canvas.draw(menu2);
            canvas.drawText("         " + selected, displayFont, 6.5f*worldScale.x, 4.1f*worldScale.y);
            canvas.draw(fern, Color.WHITE,fern.getWidth()/2, fern.getHeight()/2,
                    select_button_locs[select_button].x * worldScale.x, select_button_locs[select_button].y* worldScale.y,
                    0,worldScale.x/fern.getWidth(),worldScale.y/fern.getHeight());
        }
        canvas.end();
    }

    @Override
    public void reset() {
        bounds = new Rectangle(0,0,16.0f, 9.0f);
        Gdx.input.setCursorCatched(false);
        nextCon = "";
        SoundController instance = SoundController.getInstance();
        if (playingMusic) {
            if (instance.isActive("bgmusic")) instance.stop("bgmusic");
            if (!instance.isActive("menumusic")) instance.play("menumusic", MENU_MUSIC, true);
        }
    }

    @Override
    public void update(float dt) {
        InputController input = InputControllerManager.getInstance().getController(0);
        if(mode == HOME_SCREEN) {
            if(mode!=prevMode) {
                return;
            }

            if((input.didBottomButtonPress() || input.didEnterKeyPress()) && home_button == PLAY_BUTTON) {
                selected = GlobalConfiguration.getInstance().getCurrentLevel();
                if (selected > MAX_LEVEL) {
                    selected = 1;
                    GlobalConfiguration.getInstance().setCurrentLevel(selected);
                }
                System.out.println("selected "+selected);
                nextCon = "GM";
                return;
            }
            else if((input.didBottomButtonPress() || input.didEnterKeyPress()) && home_button == TUTORIAL_BUTTON) {
                nextCon = "TL";
                return;
            }
            else if((input.didBottomButtonPress() || input.didEnterKeyPress()) && home_button == LEVEL_SELECT_BUTTON) {
                mode = LEVEL_SELECT;
                select_button = CHOOSE_LEVEL;
                selected = 1;
            }
            else if((input.didBottomButtonPress() || input.didEnterKeyPress()) && home_button == QUIT_BUTTON) {
                listener.exitScreen(this, EXIT_QUIT);
            }

            if (input.didTopDPadPress() || input.didUpArrowPress() || (!prevLeftUp && leftUp)) {
                if (home_button > 0) {
                    home_button--;
                }
            }
            else if (input.didBottomDPadPress() || input.didDownArrowPress() || (!prevLeftDown && leftDown)) {
                if (home_button < home_button_locs.length - 1) {
                    home_button++;
                }
            }
        }
        if(mode == LEVEL_SELECT) {
            if(mode!=prevMode)
                return;

            if((input.didBottomButtonPress() || input.didEnterKeyPress()) && select_button == RETURN_HOME) {
                System.out.println("return home");
                mode = HOME_SCREEN;
                home_button = PLAY_BUTTON;
            }
            else if((input.didBottomButtonPress() || input.didEnterKeyPress()) && select_button == CHOOSE_LEVEL){
                GlobalConfiguration.getInstance().setCurrentLevel(selected);
                System.out.println("selected level");
                nextCon = "GM";
                return;
            }

            if((input.didLeftDPadPress() || (leftLeft && !prevLeftLeft) || input.didLeftArrowPress()) && selected > minLevel && select_button == CHOOSE_LEVEL) {
                selected--;
            } else if((input.didRightDPadPress() || (leftRight && !prevLeftRight) || input.didRightArrowPress()) && selected < maxLevel && select_button == CHOOSE_LEVEL) {
                selected++;
            }

            if(input.didTopDPadPress() || input.didUpArrowPress() || (leftUp && !prevLeftUp)) {
                select_button = CHOOSE_LEVEL;
            }
            else if(input.didBottomDPadPress() || input.didDownArrowPress() || (leftDown && !prevLeftDown)){
                select_button = RETURN_HOME;
            }
        }

    }
}