package askew.playermode.gamemode;

import askew.entity.obstacle.BoxObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.sloth.SlothModel;
import com.badlogic.gdx.physics.box2d.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


class PhysicsController implements ContactListener {
    @Getter
    private boolean isFlowKill;
    @Getter
    @Setter
    private List<SlothModel> slothList;
    @Getter
    @Setter
    private BoxObstacle goalDoor;
    @Getter
    private boolean isFlowWin;
    private int victorySloth;

    /**
     * This function deals with collisions.
     * Fixture collision mask groups:
     * 0x0001 ->
     */
    PhysicsController() {
        this.slothList = new ArrayList<>();
        reset();
    }

    public void reset() {
        slothList.clear();
        goalDoor = null;
        isFlowKill = false;
        isFlowWin = false;
    }

    private boolean isSlothPart(Obstacle obs) {
        return obs.getName() != null && obs.getName().contains("slothpart");
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when we first get a collision between two entities.  We use
     * this method to test if it is the "right" kind of collision.
     *
     * @param contact The two bodies that collided
     */
    @Override
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        try {
            if (!(body1.getUserData() instanceof Obstacle) || !(body2.getUserData() instanceof Obstacle))
                return;

            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();

            // Check for thorns
            if (bd1 != null && bd2 != null && (isSlothPart(bd1) || isSlothPart(bd2))) {
                Obstacle other;
                Obstacle slothy;
                // TODO: Sloth Homicide
                if (isSlothPart(bd1) && isSlothPart(bd2)) return;

                if (isSlothPart(bd1)) {
                    other = bd2;
                    slothy = bd1;
                } else {
                    other = bd1;
                    slothy = bd2;
                }

                if (other.getName() != null && (other.getName().equals("thorns") || other.getName().equals("ghost"))) {
                    for (SlothModel sloth : slothList) {
                        for (Obstacle b : sloth.getBodies()) {
                            if (b == slothy) {
                                sloth.shouldDie = true;
                                System.out.println(sloth);
                            }
                        }
                    }
                }

                if (other.getName() != null && other.getName().equals("owl")) {
                    for (int i = 0 ; i < slothList.size(); i++) {
                        SlothModel sloth = slothList.get(i);
                        for (Obstacle b : sloth.getBodies()){
                            if (b == slothy) {
                                victorySloth = i;
                            }
                        }
                    }
                    isFlowWin = true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two entities cease to touch.
     */
    public void endContact(Contact contact) {
    }

    private Body getBody(World world, String checkString, SlothModel sloth) {
        return Arrays.stream(world.getContactList().toArray()).filter(Contact::isTouching).map(contact -> {
            Fixture fix1 = contact.getFixtureA();
            Fixture fix2 = contact.getFixtureB();

            Body body1 = fix1.getBody();
            Body body2 = fix2.getBody();

            Object fd1 = fix1.getUserData();
            Object fd2 = fix2.getUserData();

            if (!(body1.getUserData() instanceof Obstacle) || !(body2.getUserData() instanceof Obstacle))
                return null;

            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();

            if (fd1 != null && ((String) fd1).contains(checkString) && bd2 != sloth && bd2 != null) {
                return body2;
            }

            if (fd2 != null && ((String) fd2).contains(checkString) && bd1 != sloth) {
                return body1;
            }

            return null;
        }).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public Body getLeftBody(World world, SlothModel sloth) {
        return getBody(world, "sloth left hand slothid" + sloth.getId(), sloth);
    }

    public Body getRightBody(World world, SlothModel sloth) {
        return getBody(world, "sloth right hand slothid" + sloth.getId(), sloth);
    }

    /**
     * Unused ContactListener method
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    /**
     * Unused ContactListener method
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    public void addSloth(SlothModel sloth) {
        slothList.add(sloth);
    }

    public int winningSloth() {
        return victorySloth;
    }
}
