package br.com.jcaguiar.lfe;


public enum CharacterCoreFrames {

    STAND(0, 0),
    WALK(1, 5),
    RUN(2, 9),
    HEAVY_WALK(1, 12),
    HEAVY_RUN(2, 16),
    HEAVY_STOP_RUN(15, 19),
    WEAPON_ATTACK_1(3, 20),
    WEAPON_ATTACK_2(3, 25),
    WEAPON_JUMP_ATTACK(3, 30),
    WEAPON_RUN_ATTACK(3, 35),
    WEAPON_DASH_ATTACK(3, 40),
    WEAPON_THROW(15, 45),
    HEAVY_THROW(15, 50),
    WEAPON_JUMP_THROW(15, 52),
    DRINK(17, 55),
    ATTACK_1(3, 60),
    ATTACK_2(3, 65),
    SUPER_ATTACK(3, 70),
    JUMP_ATTACK(3, 80),
    RUN_ATTACK(3, 85),
    DASH_ATTACK(3, 90),
    AIR_FLIP_BACK(6, 100),
    DODGE(6, 102),
    AIR_FLIP_FRONT(6, 108),
    GUARD(7, 110),
    BROKEN_GUARD(8, 112),
    PICK_WEAPON(15, 115),
    PICK_HEAVY(15, 116),
    CATCH(9, 120),
    CATCH_ATTACK(9, 122),
    CATCHED(10, -1),
    FALL_BACK(12, 180),
    FALL_FRONT(12, 186),
    ICE(13, 200),
    FIRE_UP(18, 203),
    FIRE_DOWN(18, 205),
    PRE_JUMP(4, 210),
    JUMP(4, 212),
    DASH_FRONT(5, 213),
    DASH_BACK(5, 214),
    CROUCH(15, 215),
    LAND(15, 219),
    STOP_RUN(15, 218),
    INJURY_1_FRONT(11, 220),
    INJURY_1_BACK(11, 222),
    INJURY_2(11, 224),
    DANCE_OF_PAIN(16, 226),
    LYING_BACK(14, 230),
    LYING_FRONT(14, 231),
    LYING_THROW(9, 232);

    public final int state;
    public final int frame;

    CharacterCoreFrames(int state, int frame) {
        this.state = state;
        this.frame = frame;
    }
}
