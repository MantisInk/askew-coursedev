package askew.playermode.gamemode;

import askew.entity.FilterGroup;
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

    private ArrayList<ArrayList<Body>> rightLists = new ArrayList<>();
    private ArrayList<ArrayList<Body>> leftLists = new ArrayList<>();

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
        for(ArrayList a : rightLists){
            a.clear();
        }
        rightLists.clear();
        for(ArrayList a : leftLists){
            a.clear();
        }
        leftLists.clear();

        goalDoor = null;
        isFlowKill = false;
        isFlowWin = false;
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

        Fixture me = null;
        Fixture other = null;

        boolean oneIsSloth = (fix1.getFilterData().categoryBits & FilterGroup.SLOTH) != 0;
        boolean twoIsSloth = (fix2.getFilterData().categoryBits & FilterGroup.SLOTH) != 0;

        if(oneIsSloth && twoIsSloth) {
            return;
        }else if (oneIsSloth){
            me = fix1;
            other = fix2;
        }else if (twoIsSloth){
            me = fix2;
            other = fix1;
        }else{
            return;
        }
        body1 = me.getBody();
        body2 = other.getBody();

        if((me.getFilterData().categoryBits & FilterGroup.HAND) != 0){
            for (SlothModel sloth : slothList) {
                if(body1 == sloth.getRightHand()){
                    rightLists.get(sloth.getId()).add(body2);
                }
                if(body1 == sloth.getLeftHand()){
                    leftLists.get(sloth.getId()).add(body2);
                }
            }
        }


        if((other.getFilterData().categoryBits & FilterGroup.LOSE) != 0) {
            for (SlothModel sloth : slothList) {
                for (Obstacle b : sloth.getBodies()) {
                    if (b.getBody() == body1) {
                        sloth.shouldDie = true;
                        System.out.println(sloth);
                    }
                }
            }
        }

        if((other.getFilterData().categoryBits & FilterGroup.WIN) != 0) {
            for (SlothModel sloth : slothList) {
                for (Obstacle b : sloth.getBodies()) {
                    if (b.getBody() == body1) {
                        victorySloth = sloth.getId();
                    }
                }
            }
            isFlowWin = true;
        }
        
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two entities cease to touch.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Fixture me = null;
        Fixture other = null;

        boolean oneIsSloth = (fix1.getFilterData().categoryBits & FilterGroup.SLOTH) != 0;
        boolean twoIsSloth = (fix2.getFilterData().categoryBits & FilterGroup.SLOTH) != 0;

        if(oneIsSloth && twoIsSloth) {
            return;
        }else if (oneIsSloth){
            me = fix1;
            other = fix2;
        }else if (twoIsSloth){
            me = fix2;
            other = fix1;
        }else{
            return;
        }
        body1 = me.getBody();
        body2 = other.getBody();

        if((me.getFilterData().categoryBits & FilterGroup.HAND) != 0){
            for (SlothModel sloth : slothList) {
                if(body1 == sloth.getRightHand()){
                    rightLists.get(sloth.getId()).remove(body2);
                }
                if(body1 == sloth.getLeftHand()){
                    leftLists.get(sloth.getId()).remove(body2);
                }
            }
        }
    }


    public Body getLeftBody(SlothModel sloth) {
        if (leftLists.get(sloth.getId()).size() > 0) {
            return leftLists.get(sloth.getId()).get(0);
        }
        return null;
    }

    public Body getRightBody(SlothModel sloth) {
        if (rightLists.get(sloth.getId()).size() > 0) {
            return rightLists.get(sloth.getId()).get(0);
        }
        return null;
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
        leftLists.add(new ArrayList<>());
        rightLists.add(new ArrayList<>());

    }

    public int winningSloth() {
        return victorySloth;
    }
}
