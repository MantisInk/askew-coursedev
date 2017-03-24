package physics;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import lombok.Setter;
import physics.InputController;
import physics.WorldController;
import physics.obstacle.ComplexObstacle;
import physics.obstacle.Obstacle;
import physics.platform.StiffBranch;
import physics.platform.Vine;
import physics.platform.sloth.SlothModel;
import util.PooledList;
import lombok.Getter;



public class MainMenuController extends WorldController {
    @Getter @Setter
    private int selected = 0;
    @Getter @Setter
    private int minLevel = 0;
    @Getter @Setter
    private int maxLevel = 10;


    public MainMenuController() {
    }

    @Override
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }

        InputController input = InputController.getInstance();

        if (input.didLeftButtonPress()) {
            System.out.println("GM");
            listener.exitScreen(this, EXIT_MM_GM);
            return false;
        } else if (input.didTopButtonPress()) {
            System.out.println("MM");
            listener.exitScreen(this, EXIT_MM_LE);
            return false;
        }
        else if (input.didBottomButtonPress()) {
            System.out.println("GM_OLD");
            listener.exitScreen(this, EXIT_MM_GM_OLD);
            return false;
        }



        return true;

    }

    @Override
    public void draw(float delta) {
        canvas.clear();
        displayFont.setColor(Color.GREEN);
        canvas.begin(); // DO NOT SCALE
        canvas.drawTextCentered("Level: " + selected, displayFont, 0.0f);
        canvas.end();
    }

    @Override
    public void reset() {

    }

    @Override
    public void update(float dt) {
        InputController input = InputController.getInstance();
        if(input.didLeftDPadPress() && selected < maxLevel) {
            selected++;
        }
        else if(input.didRightDPadPress() && selected > minLevel){
            selected--;
        }

    }
}