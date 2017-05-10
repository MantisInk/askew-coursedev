package askew.playermode.leveleditor.button;


import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.playermode.leveleditor.LevelEditorController;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import lombok.Getter;

public class Button {

    private static final String BUTTON_TEXTURE = "texture/leveleditor/button.png";
    //bottom left corner is 0,0
    final float x;
    final float y;
    //width and height in pixels. Box2d Coords * worldscale.
    final float width;
    final float height;
    @Getter
    final
    String group;
    @Getter
    final
    int index;
    @Getter
    final
    String name;

    Texture texture;
    Texture yellowbox;

    public Button(float y, float width, String group, int index, String name){
        //noinspection SuspiciousNameCombination
        this(LevelEditorController.GUI_LEFT_BAR_MARGIN,y,width,
                LevelEditorController.GUI_LEFT_BAR_MARGIN,group,index,
                name);
    }

    Button(float x, float y, float width, float height, String group,
           int index,
           String name){
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
        if (texture != null) {
            if(inBounds(mousex,mousey)){
                canvas.draw(yellowbox ,Color.WHITE,0,0,x - 3f ,y-3f ,0,(width+6f) /yellowbox.getWidth(), (height + 6f)/yellowbox.getHeight());
            }
            canvas.draw(texture, Color.WHITE,0,0, x, y, width, height);
            canvas.drawTextStandard(name, x + 10 ,y + height);
        }
    }
}
