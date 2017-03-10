package edu.cornell.gdiac.physics.obstacle;

/**
 * Box obstacle but with no collisions
 */
public class TrevorObstacle extends BoxObstacle {

    public TrevorObstacle(float x, float y, float width, float height) {
        super(x, y, width, height);
        fixture.shape = shape;
        // Disable collisions
        fixture.filter.maskBits = 0x0000;
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
}
