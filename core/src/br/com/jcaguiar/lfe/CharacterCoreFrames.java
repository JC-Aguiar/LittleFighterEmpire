package br.com.jcaguiar.lfe;

public enum CharacterCoreFrames {

    STAND(0, 0, false, true),
    WALK(1, 5, true, false),
    RUN(2, 9, true, false),
    HEAVY_WALK(1, 12, true, false),
    HEAVY_RUN(2, 16, true, false),
    HEAVY_STOP_RUN(15, 19, false, true),
    WEAPON_ATTACK_1(3, 20, false, true),
    WEAPON_ATTACK_2(3, 25, false, true),
    WEAPON_JUMP_ATTACK(3, 30, false, true),
    WEAPON_RUN_ATTACK(3, 35, false, true),
    WEAPON_DASH_ATTACK(3, 40, false, true),
    WEAPON_THROW(15, 45, false, true),
    HEAVY_THROW(15, 50, false, true),
    WEAPON_JUMP_THROW(15, 52, false, true),
    DRINK(17, 55, false, true),
    ATTACK_1(3, 60, false, true),
    ATTACK_2(3, 65, false, true),
    SUPER_ATTACK(3, 70, false, true),
    JUMP_ATTACK(3, 80, false, true),
    RUN_ATTACK(3, 85, false, true),
    DASH_ATTACK(3, 90, false, true),
    AIR_FLIP_BACK(6, 100, false, true),
    DODGE(6, 102, false, true),
    AIR_FLIP_FRONT(6, 108, false, true),
    GUARD(7, 110, false, true),
    BROKEN_GUARD(8, 112, false, true),
    PICK_WEAPON(15, 115, false, true),
    PICK_HEAVY(15, 116, false, true),
    CATCH(9, 120, false, true),
    CATCH_ATTACK(9, 122, false, true),
    CATCHED(10, -1, false, false),
    FALL_BACK(12, 180, false, false),
    FALL_FRONT(12, 186, false, false),
    ICE(13, 200, false, true),
    FIRE_UP(18, 203, false, true),
    FIRE_DOWN(18, 205, false, true),
    PRE_JUMP(4, 210, false, true),
    JUMP(4, 212, false, true),
    DASH_FRONT(5, 213, false, true),
    DASH_BACK(5, 214, false, true),
    CROUCH(15, 215, false, true),
    STOP_RUN(15, 218, false, true),
    INJURY_1_FRONT(11, 220, false, true),
    INJURY_1_BACK(11, 222, false, true),
    INJURY_2(11, 224, false, true),
    DANCE_OF_PAIN(16, 226, false, true),
    LYING_BACK(14, 230, false, true),
    LYING_FRONT(14, 231, false, true),
    LYING_THROW(9, 232, false, true);

    public final int state;
    public final int frame;
    public final boolean pingPong;
    public final boolean useNextKey;

    CharacterCoreFrames(int state, int frame, boolean pingPong, boolean useNextKey) {
        this.state = state;
        this.frame = frame;
        this.pingPong = pingPong;
        this.useNextKey = useNextKey;
    }
}
