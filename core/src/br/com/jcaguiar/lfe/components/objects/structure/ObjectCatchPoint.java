package br.com.jcaguiar.lfe.components.objects.structure;

import lombok.AccessLevel;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class ObjectCatchPoint extends ObjectStickPoint {

    int kind;
    int vaction;
    int throwvz;
    int hurtable;
    int throwinjury;
    int decrease;

}
