package br.com.jcaguiar.lfe.components.objects.structure;

import lombok.AccessLevel;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@FieldDefaults(level = AccessLevel.PUBLIC)
public class ObjectStickPoint {

    int x;
    int y;
    int z;

}
