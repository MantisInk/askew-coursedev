package askew.playermode.leveleditor.button;


import askew.MantisAssetManager;
import askew.playermode.leveleditor.button.Button;

import java.util.ArrayList;

public class ButtonList {

    private ArrayList<Button> buttons;
    private MantisAssetManager manager;

    public ButtonList(MantisAssetManager m){
        buttons = new ArrayList<Button>();
        manager = m;
    }


    public Button findButton(float mousex, float mousey){
        Button ans = null;
        for(Button b :buttons){
            if(b.inBounds(mousex, mousey)){
                ans = b;
            }
        }
        return ans;

    }

    public Button buttonByName(String s, int i){
        Button ans = null;
        for(Button b : buttons){
            if(b.isName(s,i)){
                ans = b;
            }
        }
        return ans;


    }

    public void add(Button b){
        buttons.add(b);
        b.setTextures(manager);
    }



}
