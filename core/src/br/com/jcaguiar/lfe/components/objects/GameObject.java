package br.com.jcaguiar.lfe.components.objects;

import br.com.jcaguiar.lfe.components.objects.structure.*;
import br.com.jcaguiar.lfe.components.scene.DefaultStage;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.var;

import java.util.*;

import static br.com.jcaguiar.lfe.components.objects.CharCoreFrames.*;
import static br.com.jcaguiar.lfe.components.objects.SpritePicKeyword.*;
import static br.com.jcaguiar.lfe.resources.CombatSystem.ACTIONS;
import static br.com.jcaguiar.lfe.resources.CombatSystem.BODIES;

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class GameObject extends DataObject {

    //CORE
    Sprite currentSprite;
    GameObject owner = null;
    int team = 0;

    //ANIMATION
    int walkingFrameRate;
    int runningFrameRate;
    int picIndex = 0, frameIndex = 0;
    float frameTimer;
    private ShapeRenderer debugRenderer = new ShapeRenderer(); //TODO: teste! remove!

    //STATUS
    int hpNow, hpMax, hpLimit, hpRegen;
    int mpNow, mpMax, mpLimit, mpRegen;

    //PHYSICS
    int size = 1;
    boolean right = new Random().nextBoolean();
    boolean inAir = false;
    float posX, posY, posZ;
    float accX = 0, accY = 0, accZ = 0;
    float startX, startZ;
    int weaponId, grabId = -1;
    @Setter float hitLag = 0, aRest = 0, vRest = 0;
    public static final float MAX_ACC_X = 30f;
    public static final float MAX_ACC_Y = 50f;
    public static final float MAX_ACC_Z = 30f;
    public static final float MIN_ACC = 0.05f;

    //INPUTS
    boolean keyUp, keyDown, keyLeft, keyRight;


    //LOADER CHAR BMP-SCOPE METHOD ----------------------------------------------------------

    public void setBmpContent(String line) {
        if(line.startsWith("walking_frame_rate")) {
            System.out.println("Found walking_frame_rate");
            line = line.replace("walking_frame_rate", "");
            walkingFrameRate = Integer.parseInt(line.trim());
            System.out.println("walkingFrameRate: " + walkingFrameRate);
            return;
        }
        else if(line.startsWith("running_frame_rate")) {
            System.out.println("Found running_frame_rate");
            line = line.replace("running_frame_rate", "");
            runningFrameRate = Integer.parseInt(line.trim());
            System.out.println("runningFrameRate: " + runningFrameRate);
            return;
        }

        var keyValue =  Arrays.stream(line.replace(":", "").split(" "))
            .filter(l -> !l.equals("w") && !l.equals("h") && !l.equals("col") && !l.equals("row"))
            .filter(l -> !l.isEmpty())
            .map(String::trim)
            .toArray(String[]::new);

        int size = 2;
        if(line.startsWith("file")) {
            size = 6;
            if(!keyValue[0].matches("file\\(\\d+-\\d+\\)"))
                throw new RuntimeException("Load Data Fail: invalid value inside <bmp_begin> tag: " + line);
        }

        if(keyValue.length != size)
            throw new RuntimeException("Load Data Fail: invalid value inside <bmp_begin> tag: " + line);

        if(keyValue[0].startsWith("name"))
            name = keyValue[1];
        else if(keyValue[0].startsWith("head"))
            head = keyValue[1];
        else if(keyValue[0].startsWith("file")) {
            try {
                bmpSources.add(new DataBmp(
                    keyValue[0], keyValue[1], keyValue[2], keyValue[3], keyValue[4], keyValue[5]));
            } catch (Exception e) {
                throw new RuntimeException("Load Data File: Invalid value inside <bmp_begin> tag: " + line);
            }
        }
    }

    //CORE IN-GAME METHODS ------------------------------------------------------------------

    public DataFrame currentDataFrame() { return dataFrames.get(frameIndex); }

    protected void setFrameTimer() {
        frameTimer = (float) currentDataFrame().get(WAIT) * 0.075f;
    }

    protected void checkValidFrame() {
        frameIndex = currentDataFrame() == null ? 0 : frameIndex;
        frameIndex = Math.max(frameIndex, 0);
    }

    protected void checkNewFrame() {
        if(frameTimer <= 0) setNewFrame(currentDataFrame().get(NEXT_FRAME));
    }

    protected void setNewFrame(int index) {
        frameIndex = index;
        checkValidFrame();
        setFrameTimer();
        setNewFrameAcceleration();
    }

    protected void setNewFrame(int index, float timer) {
        frameIndex = index;
        checkValidFrame();
        frameTimer = timer;
        setNewFrameAcceleration();
    }

    protected void setCurrentSprite() {
        picIndex = currentDataFrame().get(PIC);
        currentSprite = new Sprite(sprites[picIndex]);
        currentSprite.setOriginCenter();
        //currentSprite.setPosition(40, 0);
        //setOriginX((float) currentSprite.getRegionWidth()/2);
        //setOriginY(currentSprite.getRegionY() + currentDataFrame().get(CENTER_Y) / 2);
        //currentSprite.setCenter(0f, 0f);
    }

    protected void setBoundsPerSprite() {
        //currentSprite.setCenter(currentSprite.getWidth()/2, 0f);
        setBounds(currentSprite.getX(),
                  currentSprite.getY(),
                  currentSprite.getWidth(),
                  currentSprite.getHeight());
    }

    public int getModBySide() {
        return right ? 1 : -1;
    }

    protected void setNewFrameAcceleration() {
        //When stopping run
        if(frameIndex == STOP_RUN.frame) setAccX(accX + 1f * getModBySide());

        //Apply acceleration when needed
        setAccX(accX + getDvxMod());
        setAccY(accY + getDvyMod());
        if(keyUp || keyDown) setAccZ(accZ + getDvzMod() * (keyUp? -1 : 1));
    }

    public float getDvxMod() {
        if(currentDataFrame().get(DVX) >= 550) return 0;
        return ((currentDataFrame().get(DVX) / 5) + (currentDataFrame().get(DVX) / 10) * 5
            + currentDataFrame().get(DVX) * 0.15f) * getModBySide();
    }

    public float getDvyMod() {
        if(currentDataFrame().get(DVY) >= 550) return 0;
        return currentDataFrame().get(DVY) * 1.7f;
    }

    public float getDvzMod() {
        if(currentDataFrame().get(DVZ) >= 550) return 0;
        return (currentDataFrame().get(DVZ) / 10) * 5 + currentDataFrame().get(DVZ) * 0.1f;
    }

    protected void setAccX(float newAccX) {
        accX = newAccX;
        if(accX > 0) accX = Math.min(accX, MAX_ACC_X);
        else if(accX < 0) accX = Math.max(accX, -MAX_ACC_X);
    }

    protected void setAccY(float newAccY) {
        accY = newAccY;
        if(accY > 0) accY = Math.min(accY, MAX_ACC_Y);
        else if(accY < 0) accY = Math.max(accY, -MAX_ACC_Y);
    }

    protected void setAccZ(float newAccZ) {
        accZ = newAccZ;
        if(accZ > 0) accZ = Math.min(accZ, MAX_ACC_Z);
        else if(accZ < 0) accZ = Math.max(accZ, -MAX_ACC_Z);
    }

    public boolean hasAccX() { return accX < -MIN_ACC || accX > MIN_ACC; }

    public boolean hasAccZ() { return accZ < -MIN_ACC || accZ > MIN_ACC; }

    public boolean hasEffectiveAccX() { return accX < -MIN_ACC*4 || accX > MIN_ACC*4; }

    public boolean hasEffectiveAccZ() { return accZ < -MIN_ACC*4 || accZ > MIN_ACC*4; }

    public boolean isInsideStageBoundX() {
        return posX < ((DefaultStage)getStage()).boundX;
    }

    public boolean isInsideStageBoundW() {
        return posX + getWidth() > ((DefaultStage)getStage()).boundW; }

    public boolean isInsideStageBoundZ1() {
        return getDisplayZ() < ((DefaultStage)getStage()).getLimitZ1();
    }

    public boolean isInsideStageBoundZ2() {
        return getDisplayZ() > ((DefaultStage)getStage()).getLimitZ2();
    }

    public boolean isMovingForward() {
        return right ? accX > 0 : accX < 0;
    }

    protected void setFaceSide() {
        currentSprite.flip(!right, true);
    }

    public boolean canHit() {
        return aRest <= 0;
    }

    public boolean canBeHit() {
        return vRest <= 0;
    }

    public float getDisplayX() {
        return getX() + (getWidth()/2)
            - (right ? currentDataFrame().get(CENTER_X) : (getWidth() - currentDataFrame().get(CENTER_X)))
            + ((hitLag * 100) % 2);
    }

    public float getDisplayY() { return getY() - getHeight() + currentDataFrame().get(CENTER_Y); }

    public float getDisplayZ() { return posZ + getHeight(); }

    protected void doGravity() {
        if(posY > -1 && accY >= 0) {
            setAccY(0f);
            posY = 0;
            if(inAir) doLanding();
            inAir = false;
        } else {
            inMidAir();
            inAir = true;
        }
        posY += accY;
    }

    protected void setLocation(float movX, float movZ) {
        if(hitLag <= 0) {
            //Apply gravity and set X/Y/Z acceleration values
            doGravity();
            //Calculate friction (X/Z axis)
            doFriction();
            //Apply X/Z acceleration and check collision
            movX = movX + accX;
            movZ = movZ + accZ;
        } else {
            movX = 0;
            movZ = 0;
        }

        //Check minimum valid acceleration value or become 0
        if(!hasAccX()) setAccX(0f);
        if(!hasAccZ()) setAccZ(0f);

        //Apply modifications and check stage bounds
        posX = getX() + posX + movX;
        posZ = getY() + posZ + movZ;
        if(isInsideStageBoundX())
            posX = ((DefaultStage)getStage()).boundX;
        else if(isInsideStageBoundW())
            posX = ((DefaultStage)getStage()).boundW - getWidth();
        if(isInsideStageBoundZ1())
            posZ = ((DefaultStage)getStage()).getLimitZ1() - getHeight();
        else if(isInsideStageBoundZ2())
            posZ = ((DefaultStage)getStage()).getLimitZ2() - getHeight();

        //Set X/Z axis position
        setX(posX);
        setY(posZ + posY);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        setBoundsPerSprite();
        control();
        if(hitLag <= 0) {
            checkNewFrame();
        }
        setCurrentSprite(); //TODO: test this only when sprite change
        setFaceSide();

        //Drawing debug info (bodies, etc...)
        batch.end();

        //EXAMPLE OF COLOR EFFECT
        //newBatch.begin();
        //newBatch.draw(currentSprite, (int) getDisplayX(), getStage().getHeight() - getDisplayY(), (int) getWidth(), -getHeight());
        //newBatch.end();
        //PALLETES
        //newBatch.setColor(0.35f, 0.75f, 0.75f, 1); //DARK RED
        //newBatch.setColor(0.6f, 0.6f, 0.699f, 1); //MID RED
        //newBatch.setTweak(0.75f, 0.4f, 0.2f, 0.35f); //LIGHT ICE
        //EFFECTS ANIMATION
        //crazyTime += Gdx.graphics.getDeltaTime();
        //FIRE 1)
        //(change) crazyTime += Gdx.graphics.getDeltaTime() * 0.25f;
        //crazyTime = crazyTime > 0.8f ? 0.5f : crazyTime;
        //crazyTime = crazyTime < 0.5 ? 0.5f : crazyTime;
        //newBatch.setColor(0.6f, 0.6f, crazyTime, 1);
        //FIRE 2)
        //newBatch.setColor(0.6f, 0.6f, MathUtils.sin(MathUtils.cos(crazyTime * 2) * MathUtils.PI) * 0.1f + 0.65f, 1f);
        //FIRE 3)
        //newBatch.setTweak(0.75f, 0.6f, 0.6f, 0.35f);
        //newBatch.setColor(0.5f, 0.6f, MathUtils.sin(MathUtils.cos(crazyTime * 2) * MathUtils.PI) * 0.05f + 0.6f, 1f);
        //FIRE 4)
        //newBatch.setTweak(0.75f, 0.7f, 0.3f, 0.35f);
        //newBatch.setColor(0.5f, 0.5f, MathUtils.sin(MathUtils.cos(crazyTime * 2) * MathUtils.PI) * 0.05f - 0.2f, 1f);
        //ICE 1)
        //newBatch.setTweak(0.75f, 0.4f, MathUtils.sin(MathUtils.cos(crazyTime * 2) * MathUtils.PI) * 0.2f + 0.3f, 0.35f);

        //Drawing the sprite
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(currentSprite, (int) getDisplayX(), getDisplayY(), (int) getWidth(), getHeight());
        batch.end();

        //At end
        batch.begin();
        setSpaceBoxes(batch);

        frameTimer -= Gdx.graphics.getDeltaTime();
        hitLag = hitLag <= 0 ? 0 : hitLag - Gdx.graphics.getDeltaTime();
        aRest = aRest <= 0 ? 0 : aRest - Gdx.graphics.getDeltaTime();
        vRest = vRest <= 0 ? 0 : vRest - Gdx.graphics.getDeltaTime();
    }

    private void setSpaceBoxes(Batch batch) {
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for(SpaceBody body : currentDataFrame().getBodies()) {
            //Set volume
            body.absoluteX  = (right ? getDisplayX() : getDisplayX() + getWidth()) + body.x * getModBySide();
            body.absoluteW  = body.absoluteX + body.w * getModBySide();
            body.absoluteY  = getY() + body.y;
            body.absoluteH  = body.absoluteY + body.h;
            body.absoluteZ1 = posZ - body.z1;
            body.absoluteZ2 = posZ + body.z2;

            //Draw
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
            debugRenderer.setColor(0f, 0f, 1f, 0.2f);
            debugRenderer.rect(body.absoluteX, body.absoluteY, body.w * getModBySide(), body.h);
            debugRenderer.end();
        }
        BODIES.put(this, currentDataFrame().getBodies());

        for(SpaceInteraction itr : currentDataFrame().getInteractions()) {
            //Set volume
            itr.absoluteX  = (right ? getDisplayX() : getDisplayX() + getWidth()) + itr.x * getModBySide();
            itr.absoluteW  = itr.absoluteX + itr.w * getModBySide();
            itr.absoluteY  = getY() + itr.y;
            itr.absoluteH  = itr.absoluteY + itr.h;
            itr.absoluteZ1 = posZ - itr.z1;
            itr.absoluteZ2 = posZ + itr.z2;

            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
            debugRenderer.setColor(1f, 0f, 0f, 0.35f);
            debugRenderer.rect(itr.absoluteX, itr.absoluteY, itr.w * getModBySide(), itr.h);
            debugRenderer.end();
        }
        ACTIONS.put(this, currentDataFrame().getInteractions());
    }

    //METHODS TO IMPLEMENT ---------------------------------------------------------------

    protected void doLanding() { }

    protected void inMidAir() { }

    protected void doFriction() { }

    protected void control() { }

}
