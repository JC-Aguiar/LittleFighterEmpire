package br.com.jcaguiar.lfe;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataFrame {

    //ANIMATION
//    int frame;
    int pic;
    int state;
    int wait;
//    int timerWait;
    int dvx;
    int dvy;
    int dvz;
    int centerX;
    int centerY;
    String sound;
    int mpCost;
//    int nextId;
    int nextFrame;
//    int nextMpCost;

    //COMMANDS
    int hit_a;
    int hit_d;
    int hit_j;
    int hit_Fa;
    int hit_Ua;
    int hit_Da;
    int hit_Fj;
    int hit_Uj;
    int hit_Dj;
    int hit_ja;

    //3D VOLUME
    public static final int MAX_ELEMENTS_ARRAY = 4;
    final List<ObjectBodyBox> bodies = new ArrayList<>();
    final List<ObjectInteractionBox> interactions = new ArrayList<>();
//    final List<ObjectCreationPoint> creations = new ArrayList<>(); TODO: implement
    ObjectCatchPoint prey = new ObjectCatchPoint();
    ObjectItemPoint item = new ObjectItemPoint();
    ObjectStickPoint blood = new ObjectStickPoint();
    //ObjectStickPoint[] sticks = new ObjectStickPoint[2]; TODO: implement

    //GET TOTAL BODY VOLUME
    public ObjectBodyBox overallBody() {
        int xMin = Integer.MAX_VALUE;
        int yMin = Integer.MAX_VALUE;
        int wMax = Integer.MIN_VALUE;
        int hMax = Integer.MIN_VALUE;
        int zMin = Integer.MAX_VALUE;
        int zMax = Integer.MIN_VALUE;
        for(ObjectBodyBox body : bodies) {
            xMin = Math.min(xMin, body.x);
            yMin = Math.min(yMin, body.y);
            wMax = Math.max(wMax, body.w);
            hMax = Math.max(hMax, body.h);
            zMin = Math.min(zMin, body.z1);
            zMax = Math.max(zMax, body.z2);
        }
        val overall = new ObjectBodyBox();
        overall.x = xMin;
        overall.y = yMin;
        overall.w = wMax;
        overall.h = hMax;
        overall.z1 = zMin;
        overall.z2 = zMax;
        return overall;
    }

    public void setAttribute(Map<String, Integer> keyValues, DataFrameScope frameKey) {
        if(frameKey == null) return;
        switch(frameKey) {
            case PIC: setPicValues(keyValues);
                break;
            case BPOINT: setBpointValues(keyValues);
                break;
            case WPOINT: setWpointValues(keyValues);
                break;
            case CPOINT: setCpointValues(keyValues);
                break;
            case ITR: setItrValues(keyValues);
                break;
            case BDY: setBdyValues(keyValues);
                break;
        }
    }

    private void setBdyValues(Map<String, Integer> keyValues) {
        if(bodies.size() > MAX_ELEMENTS_ARRAY) return;
        val bdy = new ObjectBodyBox();

        if(bdy.kind == 0) bdy.kind =  Optional.ofNullable(keyValues.get("kind")).orElse(0);
        if(bdy.x == 0)    bdy.x =     Optional.ofNullable(keyValues.get("x")).orElse(0);
        if(bdy.y == 0)    bdy.y =     Optional.ofNullable(keyValues.get("y")).orElse(0);
        if(bdy.w == 0)    bdy.w =     Optional.ofNullable(keyValues.get("w")).orElse(0);
        if(bdy.h == 0)    bdy.h =     Optional.ofNullable(keyValues.get("h")).orElse(0);
        if(bdy.z1 == 0)   bdy.z1 =    Optional.ofNullable(keyValues.get("z1")).orElse(0);
        if(bdy.z2 == 0)   bdy.z2 =    Optional.ofNullable(keyValues.get("z2")).orElse(0);

        if(bdy.z1 == 0)   bdy.z1 = 15;
        if(bdy.z2 == 0)   bdy.z2 = 15;

        bodies.add(bdy);
    }

    private void setPicValues(Map<String, Integer> keyValues) {
//        frame = keyValues.get("frame");
        if(pic == 0)        pic =       Optional.ofNullable(keyValues.get("pic")).orElse(0);
        if(state == 0)      state =     Optional.ofNullable(keyValues.get("state")).orElse(0);
        if(wait == 0)       wait =      Optional.ofNullable(keyValues.get("wait")).orElse(0);
        if(nextFrame == 0)  nextFrame = Optional.ofNullable(keyValues.get("next")).orElse(0);
        if(dvx == 0)        dvx =       Optional.ofNullable(keyValues.get("dvx")).orElse(0);
        if(dvy == 0)        dvy =       Optional.ofNullable(keyValues.get("dvy")).orElse(0);
        if(dvz == 0)        dvz =       Optional.ofNullable(keyValues.get("dvz")).orElse(0);
        if(centerX == 0)    centerX =   Optional.ofNullable(keyValues.get("centerx")).orElse(0);
        if(centerY == 0)    centerY =   Optional.ofNullable(keyValues.get("centery")).orElse(0);
        if(hit_a == 0)      hit_a =     Optional.ofNullable(keyValues.get("hit_a")).orElse(0);
        if(hit_d == 0)      hit_d =     Optional.ofNullable(keyValues.get("hit_d")).orElse(0);
        if(hit_j == 0)      hit_j =     Optional.ofNullable(keyValues.get("hit_j")).orElse(0);
        if(hit_j == 0)      hit_j =     Optional.ofNullable(keyValues.get("hit_j")).orElse(0);
        if(hit_Fa == 0)     hit_Fa =    Optional.ofNullable(keyValues.get("hit_Fa")).orElse(0);
        if(hit_Ua == 0)     hit_Ua =    Optional.ofNullable(keyValues.get("hit_Ua")).orElse(0);
        if(hit_Da == 0)     hit_Da =    Optional.ofNullable(keyValues.get("hit_Da")).orElse(0);
        if(hit_Fj == 0)     hit_Fj =    Optional.ofNullable(keyValues.get("hit_Fj")).orElse(0);
        if(hit_Uj == 0)     hit_Uj =    Optional.ofNullable(keyValues.get("hit_Uj")).orElse(0);
        if(hit_Dj == 0)     hit_Dj =    Optional.ofNullable(keyValues.get("hit_Dj")).orElse(0);
        if(hit_ja == 0)     hit_ja =    Optional.ofNullable(keyValues.get("hit_ja")).orElse(0);
        if(mpCost == 0)     mpCost =    Optional.ofNullable(keyValues.get("mp")).orElse(0);
        //new attributes

    }

    private void setBpointValues(Map<String, Integer> keyValues) {
        if(blood.x == 0) blood.x = Optional.ofNullable(keyValues.get("x")).orElse(0);
        if(blood.y == 0) blood.y = Optional.ofNullable(keyValues.get("y")).orElse(0);
    }

    private void setWpointValues(Map<String, Integer> keyValues) {
        if(item.kind == 0)      item.kind =      Optional.ofNullable(keyValues.get("kind")).orElse(0);
        if(item.x == 0)         item.x =         Optional.ofNullable(keyValues.get("x")).orElse(0);
        if(item.y == 0)         item.y =         Optional.ofNullable(keyValues.get("y")).orElse(0);
        if(item.z == 0)         item.z =         Optional.ofNullable(keyValues.get("z")).orElse(0);
        if(item.weaponact == 0) item.weaponact = Optional.ofNullable(keyValues.get("weaponact")).orElse(0);
        if(item.attacking == 0) item.attacking = Optional.ofNullable(keyValues.get("attacking")).orElse(0);
        if(item.cover == 0)     item.cover =     Optional.ofNullable(keyValues.get("cover")).orElse(0);
        if(item.dvx == 0)       item.dvx =       Optional.ofNullable(keyValues.get("dvx")).orElse(0);
        if(item.dvy == 0)       item.dvy =       Optional.ofNullable(keyValues.get("dvy")).orElse(0);
        if(item.dvz == 0)       item.dvz =       Optional.ofNullable(keyValues.get("dvz")).orElse(0);
    }

    private void setCpointValues(Map<String, Integer> keyValues) {
        if(prey.kind == 0)        prey.kind =         Optional.ofNullable(keyValues.get("kind")).orElse(0);
        if(prey.x == 0)           prey.x =            Optional.ofNullable(keyValues.get("x")).orElse(0);
        if(prey.y == 0)           prey.y =            Optional.ofNullable(keyValues.get("y")).orElse(0);
        if(prey.z == 0)           prey.z =            Optional.ofNullable(keyValues.get("z")).orElse(0);
        if(prey.vaction == 0)     prey.vaction =      Optional.ofNullable(keyValues.get("vaction")).orElse(0);
        if(prey.throwvz == 0)     prey.throwvz =      Optional.ofNullable(keyValues.get("throwvz")).orElse(0);
        if(prey.hurtable == 0)    prey.hurtable =     Optional.ofNullable(keyValues.get("hurtable")).orElse(0);
        if(prey.throwinjury == 0) prey.throwinjury =  Optional.ofNullable(keyValues.get("throwinjury")).orElse(0);
        if(prey.decrease == 0)    prey.decrease =     Optional.ofNullable(keyValues.get("decrease")).orElse(0);
    }

    private void setItrValues(Map<String, Integer> keyValues) {
        if(interactions.size() > MAX_ELEMENTS_ARRAY) return;
        val itr = new ObjectInteractionBox();
        if(itr.kind == 0)        itr.kind =        Optional.ofNullable(keyValues.get("kind")).orElse(0);
        if(itr.x == 0)           itr.x =           Optional.ofNullable(keyValues.get("x")).orElse(0);
        if(itr.y == 0)           itr.y =           Optional.ofNullable(keyValues.get("y")).orElse(0);
        if(itr.w == 0)           itr.w =           Optional.ofNullable(keyValues.get("w")).orElse(0);
        if(itr.h == 0)           itr.h =           Optional.ofNullable(keyValues.get("h")).orElse(0);
        if(itr.z1 == 0)          itr.z1 =          Optional.ofNullable(keyValues.get("z1")).orElse(0);
        if(itr.z2 == 0)          itr.z2 =          Optional.ofNullable(keyValues.get("z2")).orElse(0);
        if(itr.dvx == 0)         itr.dvx =         Optional.ofNullable(keyValues.get("dvx")).orElse(0);
        if(itr.dvy == 0)         itr.dvy =         Optional.ofNullable(keyValues.get("dvy")).orElse(0);
        if(itr.dvz == 0)         itr.dvz =         Optional.ofNullable(keyValues.get("dvz")).orElse(0);
        if(itr.fall == 0)        itr.fall =        Optional.ofNullable(keyValues.get("fall")).orElse(0);
        if(itr.arest == 0)       itr.arest =       Optional.ofNullable(keyValues.get("arest")).orElse(0);
        if(itr.vrest == 0)       itr.vrest =       Optional.ofNullable(keyValues.get("vrest")).orElse(0);
        if(itr.bdefend == 0)     itr.bdefend =     Optional.ofNullable(keyValues.get("bdefend")).orElse(0);
        if(itr.injury == 0)      itr.injury =      Optional.ofNullable(keyValues.get("injury")).orElse(0);
        if(itr.catchingact == 0) itr.catchingact = Optional.ofNullable(keyValues.get("catchingact")).orElse(0);
        if(itr.caughtact == 0)   itr.caughtact =   Optional.ofNullable(keyValues.get("caughtact")).orElse(0);

        //new attributes
        if(itr.effect == 0)     itr.effect =        Optional.ofNullable(keyValues.get("effect")).orElse(0);
        if(itr.frame == 0)      itr.frame =         Optional.ofNullable(keyValues.get("frame")).orElse(0);
        if(itr.id == 0)         itr.id =            Optional.ofNullable(keyValues.get("id")).orElse(0);

        //set default z1 z2 if 0
        if(itr.z1 == 0) itr.z1 = 15;
        if(itr.z2 == 0) itr.z2 = 15;

        interactions.add(itr);
    }

}
