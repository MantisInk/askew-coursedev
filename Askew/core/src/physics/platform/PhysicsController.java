package physics.platform;

import com.badlogic.gdx.physics.box2d.*;
import lombok.Getter;
import lombok.Setter;
import physics.obstacle.BoxObstacle;
import physics.obstacle.Obstacle;
import physics.obstacle.PolygonObstacle;
import physics.platform.sloth.SlothModel;


public class PhysicsController implements ContactListener {
    @Getter
    private Body leftBody;
    @Getter
    private Body rightBody;
    @Getter
    private boolean isComplete;
    @Getter @Setter
    private SlothModel sloth;
    @Getter @Setter
    private BoxObstacle goalDoor;

    /**
     * This function deals with collisions.
     * Fixture collision mask groups:
     * 0x0001 ->
     *
     *
     */
    public PhysicsController() {
        reset();
    }

    public void reset(){
        sloth = null;
        goalDoor = null;
        isComplete = false;
        clearGrab();
    }



    @Override
    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();


            if (fd1 != null && fd1.equals("sloth left hand")  && (!sloth.badBodies().contains(bd2)) && (!(bd2 instanceof PolygonObstacle))) {
                //System.out.println(body2);
                leftBody = body2;
            }
            if (fd1 != null && fd1.equals("sloth right hand")  && bd2 != sloth && (!sloth.badBodies().contains(bd2))&& (!(bd2 instanceof PolygonObstacle))) {
                rightBody = body2;
            }

            if (fd2 != null && fd2.equals("sloth left hand")  && bd1 != sloth && (!sloth.badBodies().contains(bd1))&& (!(bd1 instanceof PolygonObstacle))) {
                leftBody = body1;
            }
            if (fd2 != null && fd2.equals("sloth right hand")  && bd1 != sloth && (!sloth.badBodies().contains(bd1))&& (!(bd1 instanceof PolygonObstacle))) {
                rightBody = body1;
            }

            // Check for win condition
            if ((bd1 == sloth   && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == sloth)) {
                isComplete = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if (fd1 != null && fd1.equals("sloth left hand") && body2 == leftBody && !sloth.isLeftGrab()) leftBody = null;
        if (fd2 != null && fd2.equals("sloth left hand") && body1 == leftBody && !sloth.isLeftGrab()) leftBody = null;
        if (fd1 != null && fd1.equals("sloth right hand") && body2 == rightBody && !sloth.isRightGrab()) rightBody = null;
        if (fd2 != null && fd2.equals("sloth right hand") && body1 == rightBody && !sloth.isRightGrab()) rightBody = null;
    }

    // only for forcing release on reset
    public void clearGrab(){
        leftBody = null;
        rightBody = null;
    }
    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}
}
