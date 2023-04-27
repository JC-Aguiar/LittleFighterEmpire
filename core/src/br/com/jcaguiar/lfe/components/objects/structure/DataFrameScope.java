package br.com.jcaguiar.lfe.components.objects.structure;

public enum DataFrameScope {

    PIC("pic:", true),
    BPOINT("bpoint:", true),
    BPOINT_END("bpoint_end:", false),
    WPOINT("wpoint:", true),
    WPOINT_END("wpoint_end:", false),
    CPOINT("cpoint:", true),
    CPOINT_END("cpoint_end:", false),
    ITR("itr:", true),
    ITR_END("itr_end:", false),
    BDY("bdy:", true),
    BDY_END("bdy_end:", false),
    SOUND("sound:", true);

    public final String flag;
    public final boolean notEnd;

    DataFrameScope(String flag, boolean notEnd) {
        this.flag = flag;
        this.notEnd = notEnd;
    }
}
