package askew.playermode.leveleditor.button;


import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.playermode.leveleditor.button.Button;

import java.util.ArrayList;

public class ButtonList {

    private ArrayList<Button> buttons;
    private MantisAssetManager manager;

    public ButtonList(){
        buttons = new ArrayList<Button>();
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

    public void clear(){
        buttons.clear();
    }

    public void draw(GameCanvas canvas, float mousex, float mousey){
        for(Button b: buttons){
            b.draw(canvas, mousex, mousey);
        }
    }

    public void setManager(MantisAssetManager manager){
        this.manager = manager;
    }

    public void setTextures(MantisAssetManager manager){
        for(Button b: buttons){
            if(b.texture == null) {
                b.setTextures(manager);
            }
        }
    }



}
