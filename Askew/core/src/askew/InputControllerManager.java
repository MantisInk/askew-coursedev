package askew;


import java.util.ArrayList;
import java.util.List;

public class InputControllerManager {

    private static final int NUM_CONTROLLERS = 2;

    private static InputControllerManager instance;

    private final List<InputController> inputControllerList;

    private InputControllerManager() {
        this.inputControllerList = new ArrayList<>();
        for (int i = 0; i < NUM_CONTROLLERS; ++i) {
            inputControllerList.add(new InputController(i));
        }
    }

    public static InputControllerManager getInstance() {
        if (instance == null) {
            instance = new InputControllerManager();
        }

        return instance;
    }

    public InputController getController(int index) {
        return inputControllerList.get(index);
    }

    public List<InputController> inputControllers() {
        return inputControllerList;
    }
}
