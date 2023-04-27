package br.com.jcaguiar.lfe.components.objects;

import java.util.Arrays;
import java.util.Optional;

public enum CommandsKeyword {
    HIT_A("hit_a"),
    HIT_J("hit_j"),
    HIT_D("hit_d"),
    HIT_UA("hit_Ua"),
    HIT_FA("hit_Fa"),
    HIT_DA("hit_Da"),
    HIT_UJ("hit_Uj"),
    HIT_FJ("hit_Fj"),
    HIT_DJ("hit_Dj"),
    HIT_AJ("hit_ja"),
    HIT_ALL("hit_dja");

    public final String keyword;

    CommandsKeyword(String keyword) {
        this.keyword = keyword;
    }

    public static Optional<CommandsKeyword> validKeyword(String text) {
        return Arrays.stream(CommandsKeyword.values())
            .filter(fk -> fk.keyword.equals(text))
            .findFirst();
    }
}
