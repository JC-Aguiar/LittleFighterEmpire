package br.com.jcaguiar.lfe.components.objects.structure;

import lombok.AccessLevel;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@FieldDefaults(level = AccessLevel.PUBLIC)
public abstract class SpaceObject {

    int x;
    int y;
    int h;
    int w;
    int z1 = 5;
    int z2 = 5;

    float absoluteX;
    float absoluteY;
    float absoluteH;
    float absoluteW;
    float absoluteZ1;
    float absoluteZ2;


    public boolean collision(SpaceObject space) {
        return (this.absoluteX <= space.absoluteW && this.absoluteW >= space.absoluteX)
            || (this.absoluteY <= space.absoluteH && this.absoluteH >= space.absoluteY)
            && this.absoluteZ1 <= space.absoluteZ2 && this.absoluteZ2 >= space.absoluteZ1;
        /**
         *      y -- z1 -- w
         *      |          |
         *      |    01    |
         *      |          |
         *      x -- z2 -- h
         *               y -- z1 -- w
         *               |          |
         *               |    02    |
         *               |          |
         *               x -- z2 -- h
         */
    }

}
