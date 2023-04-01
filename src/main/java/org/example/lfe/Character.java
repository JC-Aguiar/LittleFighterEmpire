package org.example.lfe;

import java.util.HashMap;
import java.util.Map;

public class Character {

    //CORE
    int id;
    String name;
    int team;
    byte figma;
    byte face;
    boolean isHuman;
    int weaponId;
    int grabId;
    Sprite sprite;

    //STATUS
    int hpNow;
    int hpMax;
    int hpLimit;
    int hpRegen;
    int mpNow;
    int mplimit;
    int mpMax;
    int mpRegen;
    int fall;
    int fallMax;
    int guard;
    int guardMax;
    int armor;
    int armorMax;
    int timerSprite;
    int timerDead;
    CharacterState state;
    final Map<StatusEffect, Integer> effectsMod = new HashMap<>();
    final Map<StatusEffect, Integer> effectsTime = new HashMap<>();
    boolean isLowHp;
    boolean isLowMp;
    boolean isSafe;

    //ATTRIBUTES
    int attack;
    int defense;
    int critic;
    int fatal;
    int dexterity;
    int movement;
    int power;
    int will;

    //PHYSICS
    boolean right;
    //int size TODO: implement
    float posX;
    float posY;
    float posZ;
    float accX;
    float accY;
    float accZ;
    float speedWalkX;
    float speedWalkZ;
    float speedRunX;
    float speedRunZ;
    float jumpX;
    float jumpY;
    float jumpZ;
    float startX;
    float startZ;

    //SCORE
    int totalDamage;
    int totalInjury;
    int totalMpCost;
    int totalKills;
    int totalDeaths;
    int totalItens;
    //int totalHelp;            TODO: implement
    //int totalMovement;        TODO: implement

}
