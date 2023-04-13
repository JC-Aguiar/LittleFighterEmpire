package br.com.jcaguiar.lfe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import lombok.Getter;

@Getter
public class StageTest extends Stage {

    public final float boundX, boundW, boundZ1, boundZ2;
    private ShapeRenderer floorRenderer = new ShapeRenderer(); //TODO: teste! remove!

    /** Creates a stage with a {@link ScalingViewport} set to {@link Scaling#stretch}. The stage will use its own {@link Batch}
     * which will be disposed when the stage is disposed. */
    public StageTest(float boundX, float boundW, float boundZ1, float boundZ2) {
        super();
        this.boundX = boundX;
        this.boundW = boundW;
        this.boundZ1 = boundZ1;
        this.boundZ2 = boundZ2;
    }

    /** Creates a stage with the specified viewport. The stage will use its own {@link Batch} which will be disposed when the stage
     * is disposed. */
    public StageTest(Viewport viewport, float boundX, float boundW, float boundZ1, float boundZ2) {
        super(viewport);
        this.boundX = boundX;
        this.boundW = boundW;
        this.boundZ1 = boundZ1;
        this.boundZ2 = boundZ2;
    }

    /** Creates a stage with the specified viewport and batch. This can be used to specify an existing batch or to customize which
     * batch implementation is used.
     * @param batch Will not be disposed if {@link #dispose()} is called, handle disposal yourself. */
    public StageTest(Viewport viewport, Batch batch, float boundX, float boundW, float boundZ1, float boundZ2) {
        super(viewport, batch);
        this.boundX = boundX;
        this.boundW = boundW;
        this.boundZ1 = boundZ1;
        this.boundZ2 = boundZ2;
    }

    @Override
    public void draw() {
        floorRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
        floorRenderer.begin(ShapeRenderer.ShapeType.Filled);
        floorRenderer.setColor(0.5f, 0.25f, 0f, 1f);
        floorRenderer.rect(boundX, boundZ1, boundW, boundZ2);
        floorRenderer.end();
        super.draw();
    }
}
