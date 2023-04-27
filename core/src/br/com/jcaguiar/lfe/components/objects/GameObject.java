package br.com.jcaguiar.lfe.components.objects;

import br.com.jcaguiar.lfe.components.objects.structure.DataBmp;
import br.com.jcaguiar.lfe.components.objects.structure.DataFrame;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.var;

import java.util.*;

import static br.com.jcaguiar.lfe.components.objects.CharCoreFrames.STOP_RUN;
import static br.com.jcaguiar.lfe.components.objects.SpritePicKeyword.*;

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class GameObject extends DataObject {

    //CORE
    Sprite currentSprite;

    //ANIMATION
    int walkingFrameRate;
    int runningFrameRate;
    int picIndex = 0, frameIndex = 0;
    float frameTimer;

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
    int hitLag;
    public static final float MAX_ACC_X = 30f;
    public static final float MAX_ACC_Y = 50f;
    public static final float MAX_ACC_Z = 30f;
    public static final float MIN_ACC = 0.05f;

    //INPUTS
    boolean keyUp, keyDown, keyLeft, keyRight;


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

    protected int getModBySide() {
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

    protected float getDvxMod() {
        if(currentDataFrame().get(DVX) >= 550) return 0;
        return ((currentDataFrame().get(DVX) / 5) + (currentDataFrame().get(DVX) / 10) * 5
            + currentDataFrame().get(DVX) * 0.15f) * getModBySide();
    }

    protected float getDvyMod() {
        if(currentDataFrame().get(DVY) >= 550) return 0;
        return currentDataFrame().get(DVY) * 1.7f;
    }

    protected float getDvzMod() {
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

    protected void setFaceSide() {
        currentSprite.flip(!right, true);
    }

    protected float getDisplayX() {
        return getX() + (getWidth()/2)
            - (right ? currentDataFrame().get(CENTER_X) : (getWidth() - currentDataFrame().get(CENTER_X)));
    }

    protected float getDisplayY() { return getY() - getHeight() + currentDataFrame().get(CENTER_Y); }

    public float getDisplayZ() { return posZ + getHeight(); }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }
}
