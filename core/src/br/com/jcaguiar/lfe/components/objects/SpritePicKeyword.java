package br.com.jcaguiar.lfe.components.objects;

import java.util.Arrays;
import java.util.Optional;

public enum SpritePicKeyword {
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

    SpritePicKeyword(String keyword) {
        this.keyword = keyword;
    }

    public static Optional<SpritePicKeyword> validKeyword(String text) {
        return Arrays.stream(SpritePicKeyword.values())
            .filter(fk -> fk.keyword.equals(text))
            .findFirst();
    }

}
