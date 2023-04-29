package br.com.jcaguiar.lfe.components.scene;

import br.com.jcaguiar.lfe.components.objects.GameChar;
import br.com.jcaguiar.lfe.components.objects.GameObject;
import br.com.jcaguiar.lfe.components.objects.GamePlayer;
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
import lombok.val;

import java.util.Arrays;
import java.util.Comparator;

import static br.com.jcaguiar.App.*;
import static br.com.jcaguiar.App.debugFont;

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
        //Set camera position
        val focus = getMainPlayer().getFocusObj();
        final float posX = focus != null ? focus.getX() : getMainPlayer().getX();
        getCamera().position.x = posX;
        if(getCamera().position.x < boundX + Gdx.graphics.getWidth()/2)
            getCamera().position.x = (boundX + Gdx.graphics.getWidth()/2f);
        else if(getCamera().position.x > boundW - Gdx.graphics.getWidth()/2)
            getCamera().position.x = (boundW - Gdx.graphics.getWidth()/2f);

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
        if(DEBUG) drawDebugInfo(getBatch());
        //CombatSystem.checkCollision();

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

    public void drawDebugInfo(Batch batch) {
        GamePlayer hero = getMainPlayer();
        GameChar target = getTestChar();

        if(hero.currentDataFrame().getInteractions().size() > 0 && target.currentDataFrame().getBodies().size() > 0) {
            val itr1 = hero.currentDataFrame().getInteractions().get(0);
            val body1 = target.currentDataFrame().getBodies().get(0);
            boolean collision = Math.min(body1.absoluteX, body1.absoluteW) <= Math.max(itr1.absoluteW,itr1.absoluteX)
                && Math.max(body1.absoluteW, body1.absoluteX) >= Math.min(itr1.absoluteX, itr1.absoluteW)
                && body1.absoluteY <= itr1.absoluteH && body1.absoluteH  >= itr1.absoluteY
                && body1.absoluteZ1 <= itr1.absoluteZ2 && body1.absoluteZ2 >= itr1.absoluteZ1;;
            if(collision) {
                if(itr1.kind == 0 && hero.canHit() && target.canBeHit()) {
                    hero.setHitLag(0.5f);
                    target.setHitLag(0.5f);
                    hero.setARest(Math.max(5, itr1.arest/2f));
                    target.setVRest(Math.max(5, itr1.vrest/2f));
                }
            }
        }


        //Char
        batch.begin();
        debugFont.draw(batch, "X: " + hero.getX(), 0, 0);
        debugFont.draw(batch, "Y: " + hero.getY(), 150, 0);
        debugFont.draw(batch, "Z: " + hero.getPosZ(), 300, 0);
        debugFont.draw(batch, "Altitude: " + hero.getPosY(), 450, 0);
        debugFont.draw(batch, "DisplayX: " + hero.getDisplayX(), 600, 0);
        debugFont.draw(batch, "DisplayY: " + hero.getDisplayY(), 800, 0);
        debugFont.draw(batch, "DisplayZ: " + hero.getDisplayZ(), 1000, 0);
        debugFont.draw(batch, "AccX: " + hero.getAccX(), 0, 20);
        debugFont.draw(batch, "AccY: " + hero.getAccY(), 150, 20);
        debugFont.draw(batch, "AccZ: " + hero.getAccZ(), 300, 20);
        debugFont.draw(batch, "InAir: " + (hero.isInAir() ? "true" : "false"), 450, 20);
        debugFont.draw(batch, "RightTimer: " + hero.getTimerRight(), 0, 40);
        debugFont.draw(batch, "LeftTimer: " + hero.getTimerLeft(), 125, 40);
        debugFont.draw(batch, "HoldRight: " + hero.getHoldRight(), 250, 40);
        debugFont.draw(batch, "HoldLeft: " + hero.getHoldLeft(), 375, 40);
        debugFont.draw(batch, "AtkTimer: " + hero.getTimerA(), 500, 40);
        debugFont.draw(batch, "JmpTimer: " + hero.getTimerJ(), 625, 40);
        debugFont.draw(batch, "DefTimer: " + hero.getTimerD(), 750, 40);
        debugFont.draw(batch, "RunMomentum: " + hero.getRunMomentum(), 875, 40);

        //Combat
//        debugFont.draw(batch, "HitRest: " + (int) hero.getaRest(), 0, Gdx.graphics.getHeight()-60);
        if(hero.currentDataFrame().getInteractions().size() > 0) {
            val itr1 = hero.currentDataFrame().getInteractions().get(0);
            debugFont.draw(batch, "ItrX: " + itr1.absoluteX, 100, Gdx.graphics.getHeight()-60);
            debugFont.draw(batch, "ItrY: " + itr1.absoluteY, 225, Gdx.graphics.getHeight()-60);
            debugFont.draw(batch, "ItrW: " + itr1.absoluteW, 350, Gdx.graphics.getHeight()-60);
            debugFont.draw(batch, "ItrH: " + itr1.absoluteH, 475, Gdx.graphics.getHeight()-60);
            debugFont.draw(batch, "ItrZ1: " + itr1.absoluteZ1, 600, Gdx.graphics.getHeight()-60);
            debugFont.draw(batch, "ItrZ2: " + itr1.absoluteZ2, 725, Gdx.graphics.getHeight()-60);
        }
        if(target.currentDataFrame().getBodies().size() > 0) {
            val body1 = target.currentDataFrame().getBodies().get(0);
            debugFont.draw(batch, "BodyX: " + body1.absoluteX, 100, Gdx.graphics.getHeight()-80);
            debugFont.draw(batch, "BodyY: " + body1.absoluteY, 225, Gdx.graphics.getHeight()-80);
            debugFont.draw(batch, "BodyW: " + body1.absoluteW, 350, Gdx.graphics.getHeight()-80);
            debugFont.draw(batch, "BodyH: " + body1.absoluteH, 475, Gdx.graphics.getHeight()-80);
            debugFont.draw(batch, "BodyZ1: " + body1.absoluteZ1, 600, Gdx.graphics.getHeight()-80);
            debugFont.draw(batch, "BodyZ2: " + body1.absoluteZ2, 725, Gdx.graphics.getHeight()-80);
        }

        //Keys
        if(hero.isKeyLeft()) debugFont.draw(batch, "Left", 0, Gdx.graphics.getHeight()-40);
        if(hero.isKeyUp()) debugFont.draw(batch, "Up", 75, Gdx.graphics.getHeight()-40);
        if(hero.isKeyRight()) debugFont.draw(batch, "Right", 150, Gdx.graphics.getHeight()-40);
        if(hero.isKeyDown()) debugFont.draw(batch, "Down", 225, Gdx.graphics.getHeight()-40);
        if(hero.isHitA()) debugFont.draw(batch, "Attack", 300, Gdx.graphics.getHeight()-40);
        if(hero.isHitJ()) debugFont.draw(batch, "Jump", 375, Gdx.graphics.getHeight()-40);
        if(hero.isHitD()) debugFont.draw(batch, "Defense", 425, Gdx.graphics.getHeight()-40);
        if(hero.isHitDuA()) debugFont.draw(batch, "D.U.A", 500, Gdx.graphics.getHeight()-40);
        if(hero.isHitDfA()) debugFont.draw(batch, "D.F.A", 575, Gdx.graphics.getHeight()-40);
        if(hero.isHitDdA()) debugFont.draw(batch, "D.D.A", 625, Gdx.graphics.getHeight()-40);
        if(hero.isHitDuJ()) debugFont.draw(batch, "D.U.J", 700, Gdx.graphics.getHeight()-40);
        if(hero.isHitDfJ()) debugFont.draw(batch, "D.F.J", 775, Gdx.graphics.getHeight()-40);
        if(hero.isHitDdJ()) debugFont.draw(batch, "D.D.J", 825, Gdx.graphics.getHeight()-40);

        //Stage
        debugFont.draw(batch, "StageX: " + boundX, 0, Gdx.graphics.getHeight()-20);
        debugFont.draw(batch, "StageW: " + boundW, 150, Gdx.graphics.getHeight()-20);
        debugFont.draw(batch, "StageZ1: " + boundZ1, 300, Gdx.graphics.getHeight()-20);
        debugFont.draw(batch, "StageZ2: " + boundZ2, 450, Gdx.graphics.getHeight()-20);
        debugFont.draw(batch, "CameraX: " + getCamera().position.x, 600, Gdx.graphics.getHeight()-20);
        batch.end();
    }

}
