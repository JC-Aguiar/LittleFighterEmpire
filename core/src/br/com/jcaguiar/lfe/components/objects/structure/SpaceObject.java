package br.com.jcaguiar.lfe.components.objects.structure;

import com.badlogic.gdx.Gdx;
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


    public boolean collide(SpaceObject obj) {
        String body = String.format("BODY-X: %f, BODY-Y: %f, BODY-W: %f, BODY-H: %f, BODY-Z1: %f, BODY-Z2: %f",
            this.absoluteX, this.absoluteY, this.absoluteW, this.absoluteH, this.absoluteZ1, this.absoluteZ2);
        String itr = String.format("ITR-X: %f, ITR-Y: %f, ITR-W: %f, ITR-H: %f, ITR-Z1: %f, ITR-Z2: %f",
           obj.absoluteX, obj.absoluteY, obj.absoluteW, obj.absoluteH, obj.absoluteZ1, obj  .absoluteZ2);
        Gdx.app.log("COLLISION-CHECK", body);
        Gdx.app.log("COLLISION-CHECK", itr);
        // BODY-X: 122     BODY-Y: 268    BODY-W: 165   BODY-H: 330     BODY-Z1: 245   BODY-Z2: 255
        // ITR-X:  56      ITR-Y:  151    ITR-W: 81     ITR-H: 216      ITR-Z1:  130   ITR-Z2:  140

        // 122 <= 81 && 165 >= 56

        return this.absoluteX <= obj.absoluteW   && this.absoluteW  >= obj.absoluteX
            && this.absoluteY <= obj.absoluteH   && this.absoluteH  >= obj.absoluteY
            && this.absoluteZ1 <= obj.absoluteZ2 && this.absoluteZ2 >= obj.absoluteZ1;
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
