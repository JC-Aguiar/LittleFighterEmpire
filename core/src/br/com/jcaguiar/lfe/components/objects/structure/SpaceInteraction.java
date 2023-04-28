package br.com.jcaguiar.lfe.components.objects.structure;

import lombok.AccessLevel;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class SpaceInteraction extends SpaceObject {

    int kind;       //warning, attack, react, block, wind, twister, grab, item;
    int id;         //the id to be created on trigger; if 0 = no trigger;
    int frame;      //the frame to be created on trigger; if 0 = no trigger;
    int effect;     //the additional effect the attack apply;
    int dvx;        //the x acceleration; block = repel force, wind = push force, twister = spin force, react = nothing
    int dvy;        //the y acceleration; block = repel force, wind = push force, twister = spin force, react = nothing
    int dvz;        //the z acceleration; block = repel force, wind = push force, twister = spin force, react = nothing
    int fall;       //the amount of fall points the enemy will receive
    int arest;      //the unit-hit timer tolerance
    int vrest;      //the many-hit timer tolerance
    int bdefend;    //the amount og guard-break the enemy will receive
    int injury;     //the damage output;
    int catchingact;//the frame to go if catch someone
    int caughtact; //the frame the enemy go if been catch

    public void checkColision() {

    }

}
