package askew.playermode.gamemode;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ObjectSet;
import lombok.Getter;
import lombok.Setter;
import askew.entity.obstacle.BoxObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.obstacle.PolygonObstacle;
import askew.entity.sloth.SlothModel;

import java.util.Arrays;
import java.util.Objects;


public class PhysicsController implements ContactListener {
    @Getter
    private boolean isFlowKill;
    @Getter @Setter
    private SlothModel sloth;
    @Getter @Setter
    private BoxObstacle goalDoor;
    @Getter
    private boolean isFlowWin;

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
        isFlowKill = false;
        isFlowWin = false;
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
            if(!(body1.getUserData() instanceof Obstacle) || !(body2.getUserData() instanceof Obstacle))
                return;

            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            // Check for thorns
            if (
                    (fd1 != null && ((String)fd1).contains("hand") ||   fd2!= null && ((String)fd2).contains("hand")) ||
                    (bd1 != null && bd2 != null && ((bd1.getName() != null && bd1.getName().contains("slothpart")) ||( bd2.getName() != null && bd2.getName().contains("slothpart"))))
                    ) {
                Obstacle slothy;
                Obstacle other;
                if ( (fd1 != null && ((String)fd1).contains("hand")) || (bd1.getName() != null && bd1.getName().equals("slothpart"))) {
                    if (fd2 !=null && ((String)fd2).contains("slothpart")) return;
                    slothy = bd1;
                    other = bd2;
                } else {
                    if (fd1 !=null && ((String)fd1).contains("slothpart")) return;
                    slothy = bd2;
                    other = bd1;
                }

                if (other.getName() != null && (other.getName().equals("thorns") || other.getName().equals("ghost") )) {
                    isFlowKill = true;
                }

                if (other.getName() != null && other.getName().equals("owl") ) {
                    System.out.println("Hoot hoot");
                    isFlowWin = true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.
     */
    public void endContact(Contact contact) {}

    private Body getBody(World world, String checkString) {
        return Arrays.stream(world.getContactList().toArray()).filter(Contact::isTouching).map(contact->{
            Fixture fix1 = contact.getFixtureA();
            Fixture fix2 = contact.getFixtureB();

            Body body1 = fix1.getBody();
            Body body2 = fix2.getBody();

            Object fd1 = fix1.getUserData();
            Object fd2 = fix2.getUserData();

            if(!(body1.getUserData() instanceof Obstacle) || !(body2.getUserData() instanceof Obstacle)) return null;

            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            if (fd1 != null && ((String)fd1).contains(checkString) && bd2 != sloth && bd2 != null) {
                return body2;
            }

            if (fd2 != null && ((String)fd2).contains(checkString) && bd1 != sloth) {
                return body1;
            }
            
            return null;
        }).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public Body getLeftBody(World world) {
        return getBody(world, "sloth left hand");
    }

    public Body getRightBody(World world) {
        return getBody(world, "sloth right hand");
    }

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}
}
