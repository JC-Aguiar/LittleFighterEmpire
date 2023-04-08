package br.com.jcaguiar.lfe;

public class ObjectCreationPoint extends ObjectStickPoint {

    int type;   //cast, forje, dual-cast, multiple-cast, dual-multiple-cast;
    int id;     //the id to be created on trigger;
    int frame;  //the frame to be created on trigger;
    int accX;   //the x acceleration; with block = repel force, wind = push force, twister = spin force
    int accY;   //the x acceleration; with block = repel force, wind = push force, twister = spin force
    int accZ;   //the x acceleration; with block = repel force, wind = push force, twister = spin force

}
