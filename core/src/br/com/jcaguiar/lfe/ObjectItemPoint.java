package br.com.jcaguiar.lfe;

import lombok.ToString;

@ToString(callSuper = true)
public class ObjectItemPoint extends ObjectStickPoint {

    int kind;
    int weaponact;
    int attacking;
    int cover;
    int dvx;
    int dvy;
    int dvz;

}
