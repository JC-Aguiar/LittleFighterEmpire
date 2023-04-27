package br.com.jcaguiar.lfe.components.scene;

import br.com.jcaguiar.lfe.components.objects.GameObject;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Getter
public class DefaultStage extends Stage {

    public final float boundX, boundW, boundZ1, boundZ2;
    public final static int Z_OFFSET_BACKGROUND = 0;
    public final static int Z_OFFSET_OBJECTS = 1000;
    public final static int Z_OFFSET_FOREGROUND = 3000;
    public final static int Z_OFFSET_UI = 4000;

    private ShapeRenderer renderer = new ShapeRenderer(); //TODO: teste! remove!

    /** Creates a stage with a {@link ScalingViewport} set to {@link Scaling#stretch}. The stage will use its own {@link Batch}
     * which will be disposed when the stage is disposed. */
    public DefaultStage(float boundX, float boundW, float boundZ1, float boundZ2) {
        super();
        this.boundX = boundX;
        this.boundW = boundW;
        this.boundZ1 = boundZ1;
        this.boundZ2 = boundZ2;
    }

    /** Creates a stage with the specified viewport. The stage will use its own {@link Batch} which will be disposed when the stage
     * is disposed. */
    public DefaultStage(Viewport viewport, float boundX, float boundW, float boundZ1, float boundZ2) {
        super(viewport);
        this.boundX = boundX;
        this.boundW = boundW;
        this.boundZ1 = boundZ1;
        this.boundZ2 = boundZ1 + boundZ2;
    }

    /** Creates a stage with the specified viewport and batch. This can be used to specify an existing batch or to customize which
     * batch implementation is used.
     * @param batch Will not be disposed if {@link #dispose()} is called, handle disposal yourself. */
    public DefaultStage(Viewport viewport, Batch batch, float boundX, float boundW, float boundZ1, float boundZ2) {
        super(viewport, batch);
        this.boundX = boundX;
        this.boundW = boundW;
        this.boundZ1 = boundZ1;
        this.boundZ2 = boundZ1 + boundZ2;
    }



    @Override
    public void draw() {
        //Draw the floor
        renderer.setProjectionMatrix(getBatch().getProjectionMatrix());
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(0.5f, 0.25f, 0f, 1f);
        renderer.rect(boundX, boundZ1, boundW, boundZ2);
        renderer.end();

        //Draw stage actors
        for(Actor actor : getRoot().getChildren().items) {
            if(actor instanceof Group) {
                Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                //Lopping all GameObjects
                Arrays.stream(((Group) actor).getChildren().items)
                    .filter(obj -> obj instanceof GameObject)
                    .map(obj -> (GameObject) obj)
                    .sorted(Comparator.comparing(GameObject::getPosZ))
                    .forEach(this::processActors);

                Gdx.gl.glDisable(GL20.GL_BLEND);
                }
            }

        //At end, draw the rest in super-class
        super.draw();
    }

    private void processActors(GameObject obj) {
        //Set Z-Index of each object
        obj.setZIndex((int) obj.getPosZ());

        //Draw the shadow
        renderer.setProjectionMatrix(getBatch().getProjectionMatrix());
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(0f, 0f, 0f, 0.5f);
        renderer.rect(obj.getX() + obj.getWidth()/2 - 20f, obj.getDisplayZ() - 8f, 40f, 10f);
        renderer.end();
    }

    //TODO: change to something better (by using objects size)
    public float getLimitZ1() {
        return boundZ1 + 15;
    }

    public float getLimitZ2() {
        return boundZ1 + boundZ2;
    }

    public int getPositionZ(int posY) { return (int) boundZ1 + posY; }

}
