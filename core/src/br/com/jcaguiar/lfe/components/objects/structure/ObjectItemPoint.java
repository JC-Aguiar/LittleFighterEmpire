package br.com.jcaguiar.lfe.components.objects.structure;

import lombok.AccessLevel;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class ObjectItemPoint extends ObjectStickPoint {

    int kind;
    int weaponact;
    int attacking;
    int cover;
    int dvx;
    int dvy;
    int dvz;

}
