package org.example.lfe;

public class ObjectInteractionBox extends ObjectBox {

    int type;   //warning, attack, react, block, wind, twister, grab, item;
    int id;     //the id to be created on trigger; if 0 = no trigger;
    int frame;  //the frame to be created on trigger; if 0 = no trigger;
    int injury; //the damage output;
    int effect; //the additional effect the attack apply;
    int accX;   //the x acceleration; block = repel force, wind = push force, twister = spin force, react = nothing
    int accY;   //the x acceleration; block = repel force, wind = push force, twister = spin force, react = nothing
    int accZ;   //the x acceleration; block = repel force, wind = push force, twister = spin force, react = nothing

}
