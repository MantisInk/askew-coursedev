package askew.playermode.leveleditor.button;


import askew.GameCanvas;
import com.badlogic.gdx.graphics.Color;
import lombok.Getter;
import lombok.Setter;

public class ToggleButton extends Button{

    public static final float DEFAULT_WIDTH = 64;
    public static final float DEFAULT_HEIGHT = 16;

    @Getter @Setter
    public boolean on;

    public ToggleButton(float x, float y, float width, float height, String group, int index, String name, int priority, boolean on){
        super(x,y,width,height,group,index,name,priority);
        this.on = on;

    }

    public ToggleButton(float x, float y, float width, float height, String group, int index, String name){
        this(x,y,width,height,group,index, name,0,false);
    }

    public ToggleButton(float x, float y, String group, int index, String name) {
        this(x,y,DEFAULT_WIDTH,DEFAULT_HEIGHT,group,index,name,0,false);
    }

    @Override
    public void draw(GameCanvas canvas, float mousex, float mousey) {
        if (texture != null) {
            if(inBounds(mousex,mousey)){
                canvas.draw(yellowbox , Color.WHITE,0,0,x - 3f ,y-3f ,0,(width+6f) /yellowbox.getWidth(), (height + 6f)/yellowbox.getHeight());
            }
            Color tint = Color.FOREST;
            if(!on){
                tint = Color.FIREBRICK;
            }
            canvas.draw(texture, tint ,0,0, x, y, width, height);
            canvas.drawTextStandard(name, x + 10 ,y + height);
        }
    }
}
