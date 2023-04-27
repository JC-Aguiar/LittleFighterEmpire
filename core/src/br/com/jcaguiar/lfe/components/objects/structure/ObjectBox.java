package br.com.jcaguiar.lfe.components.objects.structure;

import lombok.AccessLevel;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@FieldDefaults(level = AccessLevel.PUBLIC)
public abstract class ObjectBox {

    int x;
    int y;
    int h;
    int w;
    int z1;
    int z2;

}
