package edu.cornell.gdiac.physics.obstacle;

import com.badlogic.gdx.graphics.Color;
import edu.cornell.gdiac.physics.GameCanvas;

/**
 * Box obstacle but with no collisions
 */
public class TrevorObstacle extends BoxObstacle {

    Color tint = Color.WHITE;

    public TrevorObstacle(float x, float y, float width, float height) {
        super(x, y, width, height);
        fixture.shape = shape;
        // Disable collisions
        fixture.filter.maskBits = 0x0000;
    }

    public TrevorObstacle(float x, float y, float width, float height, Color t) {
        this(x,y,width,height);
        tint = t;

    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixture
        fixture.shape = shape;
        geometry = body.createFixture(fixture);
        markDirty(false);
    }

    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),1,1);
        }
    }
}
