package askew.playermode.leveleditor.button;


import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import javax.xml.soap.Text;

public class Button {

    public static final String BUTTON_TEXTURE = "texture/leveleditor/button.png";
    //bottom left corner is 0,0
    float x;
    float y;
    //width and height in pixels. Box2d Coords * worldscale.
    float width;
    float height;

    String group;
    int index;
    String name;

    Texture texture;
    Texture yellowbox;

    public Button(float x, float y, float width, float height, String group, int index, String name){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.group = group;
        this.index = index;
        this.name = name;

    }

    public boolean inBounds(float mousex, float mousey){
        return  mousex >= x && mousex <= x + width && mousey >= y && mousey <= y + height;
    }

    public boolean isName(String s, int i){
        return s.equals(group) && i == index;
    }

    public void setTextures(MantisAssetManager manager){
        if(manager != null) {
            texture =  manager.get(BUTTON_TEXTURE);
            yellowbox = manager.get("texture/leveleditor/yellowbox.png");
        }
    }

    public void draw(GameCanvas canvas, float mousex, float mousey){
        System.out.println("buttonssss");
        if (texture != null) {
            if(inBounds(mousex,mousey)){
                canvas.draw(yellowbox ,Color.WHITE,0,0,x - 3f ,y-3f ,0,(width+6f) /yellowbox.getWidth(), (height + 6f)/yellowbox.getHeight());
            }

            canvas.draw(texture, Color.WHITE,0,0, x, y, width, height);
            canvas.drawTextStandard(name, x + 10 ,y + height);


        }
    }
}
