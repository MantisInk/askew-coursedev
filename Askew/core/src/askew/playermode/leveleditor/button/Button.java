package askew.playermode.leveleditor.button;


import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import javax.xml.soap.Text;

public class Button {

    public static final String BUTTON_TEXTURE = "texture/leveleditor/placeholder.png";
    //bottom left corner is 0,0
    float x;
    float y;
    //width and height in pixels. Box2d Coords * worldscale.
    float width;
    float height;

    String group;
    int index;
    String name;

    TextureRegion texture;

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
        Texture tex = manager.get(BUTTON_TEXTURE);
        texture = new TextureRegion(tex);
    }

    public void draw(GameCanvas canvas){
        if (texture != null) {
            canvas.draw(texture, Color.WHITE,0,0,x,y,
                    (1.0f/texture.getRegionWidth()) * width,
                    (1.0f/texture.getRegionHeight() * height));
        }
    }
}
