package org.example.lfe;

public enum CharacterState {

    STAND(0),
    WALK(1),
    RUN(2),
    CROUCH(3),
    JUMP(4),
    LAND(5),
    DASH(6),
    DODGE(7),
    INJURY(8),
    HEAVY_INJURY(9),
    STUN(10),
    FALL(11),
    FLOOR(12),
    ATTACK(13),
    GUARD(14),
    NONE(15);

    public final int num;

    CharacterState(int num) {
        this.num = num;
    }
}
