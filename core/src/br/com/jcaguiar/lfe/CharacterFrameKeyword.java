package br.com.jcaguiar.lfe;

import java.util.Arrays;
import java.util.Optional;

public enum CharacterFrameKeyword {
    PIC("pic"),
    STATE("state"),
    WAIT("wait"),
    NEXT_FRAME("next"),
    DVX("dvx"),
    DVY("dvy"),
    DVZ("dvz"),
    CENTER_X("centerx"),
    CENTER_Y("centery"),
    MP_COST("mp");

    public final String keyword;

    CharacterFrameKeyword(String keyword) {
        this.keyword = keyword;
    }

    public static Optional<CharacterFrameKeyword> validKeyword(String text) {
        return Arrays.stream(CharacterFrameKeyword.values())
            .filter(fk -> fk.keyword.equals(text))
            .findFirst();
    }

}
