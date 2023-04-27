package br.com.jcaguiar.lfe.components.objects.structure;

import br.com.jcaguiar.lfe.components.objects.CommandsKeyword;
import br.com.jcaguiar.lfe.components.objects.SpritePicKeyword;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataFrame {

    //FRAME KEYS
    final Map<SpritePicKeyword, Integer> frameKeywords = new HashMap<>();
    String sound;
    //TODO: implement nextId;

    //COMMANDS KEYS
    final Map<CommandsKeyword, Integer> commandKeywords = new HashMap<>();

    //BODIES, INTERACTIONS AND COORDINATES
    public static final int MAX_ELEMENTS_ARRAY = 4;
    final List<ObjectBodyBox> bodies = new ArrayList<>();
    final List<ObjectInteractionBox> interactions = new ArrayList<>();
//    final List<ObjectCreationPoint> creations = new ArrayList<>(); TODO: implement
    ObjectCatchPoint prey = new ObjectCatchPoint();
    ObjectItemPoint item = new ObjectItemPoint();
    ObjectStickPoint blood = new ObjectStickPoint();
    //ObjectStickPoint[] sticks = new ObjectStickPoint[2]; TODO: implement

    public DataFrame() {
        //Set default 0 value for all frame keywords
        Arrays.stream(SpritePicKeyword.values())
            .forEach(fk -> frameKeywords.put(fk, 0));

        //Set default 0 value for all command keywords
        Arrays.stream(CommandsKeyword.values())
            .forEach(ck -> commandKeywords.put(ck, 0));
    }

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

    public Map<CommandsKeyword, Integer> getCommands() {
        return commandKeywords;
    }

    public void setAttribute(Map<String, Integer> keyValues, DataFrameScope frameKey) {
        if(frameKey == null) return;
        switch(frameKey) {
            case PIC: setFrameKeywords(keyValues);
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

        if(bdy.z1 == 0)   bdy.z1 = 5;
        if(bdy.z2 == 0)   bdy.z2 = 5;

        bodies.add(bdy);
    }

    private void setFrameKeywords(Map<String, Integer> keyValues) {
        //Set frame/command keywords value from keyMap parameter
        keyValues.keySet().forEach(key -> {
            SpritePicKeyword
                .validKeyword(key)
                .ifPresent(fk -> frameKeywords.put(fk, keyValues.get(key)));
            CommandsKeyword
                .validKeyword(key)
                .ifPresent(ck -> commandKeywords.put(ck, keyValues.get(key)));
        });
    }

    public int get(SpritePicKeyword keyword) {
        return frameKeywords.get(keyword);
    }

    public int get(CommandsKeyword keyword) {
        return commandKeywords.get(keyword);
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
        if(itr.z1 == 0) itr.z1 = 5;
        if(itr.z2 == 0) itr.z2 = 5;

        interactions.add(itr);
    }

}
