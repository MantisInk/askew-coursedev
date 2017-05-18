package askew.playermode.gamemode;

import askew.InputController;
import askew.InputControllerManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuManager {

    private Stage stage;
    private Table table;
    private VerticalGroup rightVerticalGroup;
    private VerticalGroup midVerticalGroup;
    private VerticalGroup leftVerticalGroup;
    private BitmapFont font;
    private TextButtonStyle textButtonStyle;
    private LabelStyle labelStyle;
    private boolean didUpdate;
    private ChangeListener changeListener;
    private List<TextButton> mainButtons;
    private int mainButtonIndex;
    private boolean prevLeftUp;
    private boolean prevLeftDown;
    private boolean prevLeftRight;
    private boolean prevLeftLeft;
    private boolean leftUp;
    private boolean leftRight;
    private boolean leftLeft;
    private boolean leftDown;

    public MenuManager(BitmapFont regina, BitmapFont beckyIsBack) {
        font = regina;
        stage = new Stage();
        table = new Table();
        table.setFillParent(true);
        rightVerticalGroup = new VerticalGroup();
        midVerticalGroup = new VerticalGroup();
        leftVerticalGroup = new VerticalGroup();
        rightVerticalGroup.pad(100f);
        midVerticalGroup.pad(100f);
        leftVerticalGroup.pad(100f);
        stage.addActor(table);
        table.add(rightVerticalGroup).expand().center().left().padLeft(100f);
//        table.add(midVerticalGroup).center().padTop(100f);
//        table.add(rightVerticalGroup).center().right().padLeft(100f);

        textButtonStyle = new TextButtonStyle();
        textButtonStyle.font = regina;
        textButtonStyle.overFontColor = new Color(0xbd1d30ff);
        textButtonStyle.downFontColor = new Color(0x59330fff);
        textButtonStyle.fontColor = new Color(0xff932eff);
        textButtonStyle.checkedFontColor = new Color(0xabcdeff);

        labelStyle = new LabelStyle();
        labelStyle.font = beckyIsBack;
        labelStyle.fontColor = new Color(0xff932eff);

        setupLevelCompleteMenu();
    }

    private void clear() {
        rightVerticalGroup.clear();
        midVerticalGroup.clear();
        leftVerticalGroup.clear();
        mainButtonIndex = 0;
        mainButtons = new ArrayList<>();
        changeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
//                System.out.println(event);
//                System.out.println(actor);
            }
        };
        prevLeftDown = prevLeftRight = prevLeftUp = prevLeftDown = false;
    }

    public void setupLevelCompleteMenu() {
        clear();
        mainButtons.add(new TextButton("Next Level",
                textButtonStyle));
        mainButtons.add(new TextButton("Restart", textButtonStyle));
        mainButtons.add(new TextButton("Main Menu", textButtonStyle));
        finishSetup();
    }

    public void setupMainMenu() {
        clear();
        mainButtons.add(new TextButton("Play", textButtonStyle));
        mainButtons.add(new TextButton("Tutorial", textButtonStyle));
        mainButtons.add(new TextButton("Level Select", textButtonStyle));
        mainButtons.add(new TextButton("Settings", textButtonStyle));
        mainButtons.add(new TextButton("Exit", textButtonStyle));
        Label title = new Label("Askew", labelStyle);
        title.setFontScale(2.0f);
        table.add(title).center().top().padTop(300f).padRight(300f).padLeft
                (500f);
        finishSetup();
    }

    private void finishSetup() {
        mainButtons.forEach(x -> x.addListener(changeListener));
        mainButtons.forEach(rightVerticalGroup::addActor);
        mainButtons.forEach(TextButton::toggle);
        mainButtons.forEach(TextButton::toggle);
        mainButtons.get(0).toggle();
    }

    /**
     * Does the logic update of the UI.
     *
     * @return a status code -1 if nothing of value has happened.
     */
    public Optional<String> update() {
        InputController input = InputControllerManager.getInstance()
                .getController(0);

        // awkward processing
        prevLeftUp = leftUp;
        prevLeftDown = leftDown;
        prevLeftLeft = leftLeft;
        prevLeftRight = leftRight;

        leftUp = input.getLeftVertical() < -0.5;
        leftDown = input.getLeftVertical() > 0.5;
        leftLeft = input.getLeftHorizontal() > 0.5;
        leftRight = input.getLeftHorizontal() < -0.5;

        if (input.didBottomButtonPress()) {
            // select
            return Optional.of(mainButtons.get(mainButtonIndex).getText().toString() +
                    mainButtonIndex);
        } else if ((leftLeft && !prevLeftLeft) || input.didLeftArrowPress()) {

        } else if ((leftRight && !prevLeftRight) || input.didRightArrowPress()) {
            // right

        } else if ((leftUp && !prevLeftUp) || input.didUpArrowPress()) {
            // up
            int prevIndex = mainButtonIndex;
            mainButtonIndex = mainButtonIndex == 0 ? mainButtons.size() - 1 :
                    (mainButtonIndex - 1);
            mainButtons.get(prevIndex).toggle();
            if (prevIndex != mainButtonIndex)
                mainButtons.get(mainButtonIndex).toggle();
        } else if ((leftDown && !prevLeftDown) || input.didDownArrowPress()) {
            // down
            int prevIndex = mainButtonIndex;
            mainButtonIndex = mainButtonIndex == (mainButtons.size() - 1) ? 0 :
                    (mainButtonIndex + 1);
            mainButtons.get(prevIndex).toggle();
            if (prevIndex != mainButtonIndex)
                mainButtons.get(mainButtonIndex).toggle();
        }
        return Optional.empty();
    }

    public void draw() {
        if (!didUpdate) {
            didUpdate = true;
        }
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
    }

}
