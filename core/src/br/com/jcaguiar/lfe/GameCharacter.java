package br.com.jcaguiar.lfe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static br.com.jcaguiar.lfe.CharacterCoreFrames.*;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameCharacter extends DataGameObj {

    //CORE
    int characterId;
    int team;
    boolean isHuman;
    int walkCount, runCount = 0;
    boolean walkReverse, runReverse = false;
    Sprite currentSprite;

    int runTimer; //only for player

    private ShapeRenderer debugRenderer = new ShapeRenderer(); //TODO: teste! remove!
    BitmapFont font;  //TODO: teste! remove!

    //STATUS
    int lv;
    int hpNow, hpMax, hpLimit, hpRegen;
    int mpNow, mpMax, mpLimit, mpRegen;
    int fall, fallMax;
    int guard, guardMax;
    int armor, armorMax;
    int timerSprite;
    int timerDead;
    final Map<StatusEffect, Integer> effectsMod = new HashMap<>();
    final Map<StatusEffect, Integer> effectsTime = new HashMap<>();
    boolean isLowHp, isLowMp, isSafe = false;

    //ATTRIBUTES
    int attack, defense, critic, fatal, dexterity, movement, power, will;

    //PHYSICS
    //int size TODO: implement
    boolean right = new Random().nextBoolean();
    boolean inAir = false;
    float posX, posY, posZ;
    float accX = 0, accY = 0, accZ = 0;
    float speedWalkX, speedWalkZ;
    float speedRunX, speedRunZ;
    float jumpX, jumpY, jumpZ;
    float startX, startZ;
    int weaponId, grabId = -1;
    int hitLag;
    public static final float MAX_ACC_X = 50f;
    public static final float  MAX_ACC_Y = 100f;
    public static final float  MAX_ACC_Z = 50f;

    //INPUTS
    boolean keyUp, keyDown, keyLeft, keyRight;
    boolean hitRun;
    boolean hitA, hitJ, hitD, hitJA, hitDfA, hitDuA, hitDdA, hitDfJ, hitDuJ, hitDdJ, hitDAJ;

    //SCORE
    int totalDamage, totalInjury, totalMpCost, totalKills, totalDeaths, totalItens = 0;
    //int totalHelp;            TODO: implement
    //int totalMovement;        TODO: implement

    //ANIMATION
    int picIndex, frameIndex = 0;
    float frameTimer;

    public GameCharacter(DataGameObj dataObj) {
        this.sprites = dataObj.sprites;
        this.dataFrames.putAll(dataObj.dataFrames);
        this.bmpSources.addAll(dataObj.bmpSources);
        this.head = dataObj.head;
        this.name = dataObj.name;
        this.walkingFrameRate = dataObj.walkingFrameRate;
        this.runningFrameRate = dataObj.runningFrameRate;

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return keyPress(keycode);
            }
        });
        addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                return keyRelease(keycode);
            }
        });

        // Carregando fonte TrueTypeFont
        val generator = new FreeTypeFontGenerator(Gdx.files.internal("Carlito-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.color = Color.WHITE;
        parameter.flip = true;
        parameter.shadowColor = Color.BLACK;
        parameter.shadowOffsetX = 3;
        parameter.shadowOffsetY = 3;
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    private boolean keyPress(int keycode) {
        switch(keycode) {
            case Input.Keys.RIGHT:  return keyRight = true;
            case Input.Keys.LEFT:   return keyLeft = true;
            case Input.Keys.UP:     return keyUp = true;
            case Input.Keys.DOWN:   return keyDown = true;
            case Input.Keys.Q:      return hitA = true;
            case Input.Keys.W:      return hitJ = true;
            case Input.Keys.E:      return hitD = true;
            default:                return false;
        }
    }

    private boolean keyRelease(int keycode) {
        switch(keycode) {
            case Input.Keys.RIGHT:  keyRight = false; return true;
            case Input.Keys.LEFT:   keyLeft = false; return true;
            case Input.Keys.UP:     keyUp = false; return true;
            case Input.Keys.DOWN:   keyDown = false; return true;
            case Input.Keys.Q:      hitA = false; return true;
            case Input.Keys.W:      hitJ = false; return true;
            case Input.Keys.E:      hitD = false; return true;
            default:                return false;
        }
    }

    //VS MODE
    public void startVSMode(int hp, int mp, int team, int frame, boolean isHuman, int posX, int posZ) {
        this.hpMax = this.hpNow = this.hpLimit = hp;
        this.mpMax = mp;
        this.mpNow = this.mpLimit = 500;
        this.team = team;
        this.frameIndex = frame;
        this.isHuman = isHuman;
        this.posX = posX;
        this.posY = 0;
        this.posZ = posZ;
        movement();
        checkNewFrame();
        setCurrentSprite();
        setFaceSide();
//        setRandomPosition();
    }

    public DataFrame currentDataFrame() {
        return dataFrames.get(frameIndex);
    }

    private void setFrameTimer() {
        frameTimer = (float) currentDataFrame().getWait() * 0.1f;
    }

    private void checkValidFrame() {
        frameIndex = currentDataFrame() == null ? 0 : frameIndex;
        frameIndex = Math.max(frameIndex, 0);
    }

    private void checkNewFrame() {
        if(frameTimer <= 0) {
            val currentState = currentDataFrame().getState();
            //Walking State
            if(currentState == WALK.state && !inAir) {
                if(walkCount >= walkingFrameRate) walkReverse = true;
                else if(walkCount <= 0) walkReverse = false;
                walkCount += walkReverse ? -1 : 1;
                setNewFrame(WALK.frame + walkCount, 2.0f * 0.1f);
            }
            //Running State
            else if(currentState == RUN.state && !inAir) {
                if(runCount >= runningFrameRate) runReverse = true;
                else if(runCount <= 0) runReverse = false;
                runCount += runReverse ? -1 : 1;
                setNewFrame(runReverse ? frameIndex-1 : frameIndex+1 , 1.25f * 0.1f);
            }
            //Pre-Jump Frame
            else if(currentDataFrame().getNextFrame() == JUMP.frame  && !inAir) {
                setAccY(-9f);
                setNewFrame(JUMP.frame);
                float movX = 0f, movZ = 0f;
                if(keyUp) movZ = -1.5f;
                else if(keyDown) movZ = 1.5f;
                if(keyRight) movX = 5f;
                else if(keyLeft) movX = -5f;
                if (movX != 0 && movZ != 0) {
                    movX = movX * 0.75f;
                    movZ = movZ * 0.75f;
                }
                movX += accX;
                movZ += accZ;
                setAccX(movX);
                setAccZ(movZ);
            }
            //Jump state
            else if(currentState == JUMP.state && inAir)
                setNewFrame(JUMP.frame);
            //Any Other State
            else setNewFrame(currentDataFrame().getNextFrame());
        }
    }

    private void setBoundsPerSprite() {
        setBounds(currentSprite.getX(), currentSprite.getY(),
                  currentSprite.getWidth(),
                  currentSprite.getHeight());
    }

    private void setCurrentSprite() {
        picIndex = currentDataFrame().getPic();
        currentSprite = new Sprite(sprites[picIndex]);
        setOriginX(currentSprite.getRegionWidth() - currentDataFrame().getCenterX());
        setOriginY(currentSprite.getRegionY() + currentDataFrame().getCenterY() / 2);
    }

    public boolean isMovable() {
        return currentDataFrame().getState() == STAND.state
            || currentDataFrame().getState() == WALK.state
            || currentDataFrame().getState() == RUN.state;
    }

    public boolean isFlippable() {
        return currentDataFrame().getState() == STAND.state
            || currentDataFrame().getState() == WALK.state
            || (currentDataFrame().getState() == JUMP.state && inAir)
            || currentDataFrame().getState() == GUARD.state;
    }

    public boolean isJumpable() {
        return weaponId <= 0 && isMovable();
    }

    private void setNewFrame(int index) {
        frameIndex = index;
        checkValidFrame();
        setFrameTimer();
    }

    private void setNewFrame(int index, float timer) {
        frameIndex = index;
        checkValidFrame();
        frameTimer = timer;
    }

    private void preJump() {
        if(currentDataFrame().getState() == STAND.state || currentDataFrame().getState() == WALK.state  && !inAir) {
            setNewFrame(PRE_JUMP.frame);
        }
    }

    private void updateZ(int z) {
        setY(posZ + z);
        setZIndex((int) (1000 + posZ + z));
    }

    public boolean isInsideStageBoundX() {
        return getX() > ((DefaultStage) getStage()).boundX;
    }

    public boolean isInsideStageBoundW() {
        return getX() < ((DefaultStage) getStage()).boundW;
    }

    public boolean isInsideStageBoundZ1() { return getZIndex() > ((DefaultStage) getStage()).boundZ1; }

    public boolean isInsideStageBoundZ2() {
        return getZIndex() < ((DefaultStage) getStage()).boundZ2;
    }

    public float diferenceStageBoundZ1(float z) { return getZIndex() + z - ((DefaultStage) getStage()).boundZ1; }

    public float diferenceStageBoundZ2(float z) { return getZIndex() + z - ((DefaultStage) getStage()).boundZ2; }

    public float getObjectiveX() { return getRelativeSideX() + currentDataFrame().getCenterX() * getModBySide(); }

    public float getObjectiveY() { return getY() + currentDataFrame().getCenterY(); }

    public float getObjectiveZ() { return posZ + getHeight(); }

    public void movement() {
        if(hitJ && isJumpable()) preJump();
        float movZ = 0f, movX = 0f;

        //Check pressed keys
        if (keyUp) movZ = -1f;
        else if (keyDown) movZ = 1f;
        if (keyRight) movX = 2f;
        else if (keyLeft) movX = -2f;

        //Flip X
        if (movX > 0 && isFlippable()) right = true;
        else if (movX < 0 && isFlippable()) right = false;

        //Basic movement
        if(isMovable()) {
            //Basic movement in X axis
            if (movX != 0 || movZ != 0) {
                //Dual movement
                if (movX != 0 && movZ != 0) {
                    movX = movX * 0.75f;
                    movZ = movZ * 0.75f;
                }
                //Start walking frame
                if (currentDataFrame().getState() == STAND.state) {
                    setNewFrame(WALK.frame, 2.0f * 0.1f);
                }
            }
            //No keys pressed
            else {
                walkCount = runCount = 0;
                walkReverse = runReverse = false;
                if (currentDataFrame().getState() == WALK.state)
                    setNewFrame(STAND.frame);
            }
        } else {
            movZ = 0f;
            movX = 0f;
            walkCount = runCount = 0;
            walkReverse = runReverse = false;
        }

        //Update positions
        setLocation(movX, movZ);
    }

    public void setLocation(float movX, float movZ) {
        //Apply gravity (Y axis)
        doGravity();

        //Set and check X/Z axis values
        doAcceleration();
        movX = movX + accX;
        movZ = movZ + accZ;
        if(posX + getObjectiveX() + movX < ((DefaultStage)getStage()).boundX) movX = 0;
        if(posX + getObjectiveX() + movX > ((DefaultStage)getStage()).boundW) movX = 0;
        if(getObjectiveZ() + movZ < ((DefaultStage)getStage()).boundZ1) movZ = 0;
        if(getObjectiveZ() + movZ > ((DefaultStage)getStage()).boundZ2) movZ = 0;
        posX = getX() + posX + movX;
        posZ = getY() + posZ + movZ;

        //Set X/Z axis position
        setX(posX);
        setY(posZ + posY);
        //setZIndex((int) (getZIndex() + posZ));
    }

    private void doGravity() {
        posY += accY;
        inAir = posY <= -1;
        setAccY(!inAir ? 0f : accY + Math.abs(accY * 0.1f) + 0.1f);
        if(inAir)
            setFrameIndex(JUMP.frame);
        else {
            if(frameIndex == JUMP.frame) setFrameIndex(CROUCH.frame);
            posY = 0;
        }
    }

    private void doAcceleration() {
        //Calculate acceleration X/Z axis when in the ground
        if(!inAir) setAccX(accX - accX * 0.1f);
        if(!inAir) setAccZ(accZ - accZ * 0.1f);

        //Check minimum acceleration until become 0
        if(accX > -0.1f && accX < 0.1f) setAccX(0f);
        if(accZ > -0.1f && accZ < 0.1f) setAccZ(0f);
    }

    private void setAccX(float newAccX) {
        accX = newAccX;
        if(accX > 0) accX = Math.min(accX, MAX_ACC_X);
        else if(accX < 0) accX = Math.max(accX, -MAX_ACC_X);
    }

    private void setAccY(float newAccY) {
        accY = newAccY;
        if(accY > 0) accY = Math.min(accY, MAX_ACC_Y);
        else if(accY < 0) accY = Math.max(accY, -MAX_ACC_Y);
    }

    private void setAccZ(float newAccZ) {
        accZ = newAccZ;
        if(accZ > 0) accZ = Math.min(accZ, MAX_ACC_Z);
        else if(accZ < 0) accZ = Math.max(accZ, -MAX_ACC_Z);
    }

    private void setFaceSide() {
        currentSprite.flip(!right, true);
    }

    public float getRelativeSideX() {
        return right ?  getX() : getX() + getWidth();
    }

    private int getModBySide() {
        return right ? 1 : -1;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        movement();
        checkNewFrame();
        setCurrentSprite();
        setFaceSide();

        //Drawing debug info (coordinates and bodies)
        batch.end();
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if(currentDataFrame().getBodies().size() > 0) {
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
            debugRenderer.setColor(0.5f, 0f, 0.5f, 0.5f);
            debugRenderer.rect(
                getRelativeSideX() + currentDataFrame().getBodies().get(0).x * getModBySide(),
                getY() + currentDataFrame().getBodies().get(0).y,
                currentDataFrame().getBodies().get(0).w * getModBySide(),
                currentDataFrame().getBodies().get(0).h);
            debugRenderer.end();
        }

        //Drawing shadow
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(0f, 0f, 0f, 0.5f);
        debugRenderer.rect(getObjectiveX() - 15f, getObjectiveZ() - 10f, 30f, 10f);
//        debugRenderer.scale(1.5f, 0.5f, 1f);
        debugRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        //Drawing the sprite
        batch.draw(currentSprite, (int) getX(), getY(), (int) getWidth(), getHeight()); //batch.draw(batch, deltaTime);
        //Drawing numbers
        drawDebugInfo(batch);
        batch.end();

        //Drawing center point
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(1f, 0f, 0f, 1f);
        debugRenderer.circle(getX() + getOriginX(), getY() + getOriginY(), 2f);
        debugRenderer.end();
        batch.begin();

        frameTimer -= Gdx.graphics.getDeltaTime();

        setBoundsPerSprite();
    }

    private void drawDebugInfo(Batch batch) {
        //Char
        font.draw(batch, "X: " + getX(), 0, 0);
        font.draw(batch, "Y: " + getY(), 150, 0);
        font.draw(batch, "Z: " + posZ, 300, 0);
        font.draw(batch, "Altitude: " + posY, 450, 0);
        font.draw(batch, "ObjectiveX: " + getObjectiveX(), 600, 0);
        font.draw(batch, "ObjectiveY: " + getObjectiveY(), 800, 0);
        font.draw(batch, "ObjectiveZ: " + getObjectiveZ(), 1000, 0);
        font.draw(batch, "AccX: " + accX, 0, 20);
        font.draw(batch, "AccY: " + accY, 150, 20);
        font.draw(batch, "AccZ: " + accZ, 300, 20);
        font.draw(batch, "InAir: " + (inAir ? "true" : "false"), 450, 20);

        //Keys
        if(keyLeft) font.draw(batch, "Left", 0, Gdx.graphics.getHeight()-40);
        if(keyUp) font.draw(batch, "Up", 75, Gdx.graphics.getHeight()-40);
        if(keyRight) font.draw(batch, "Right", 150, Gdx.graphics.getHeight()-40);
        if(keyDown) font.draw(batch, "Down", 225, Gdx.graphics.getHeight()-40);
        if(hitA) font.draw(batch, "Attack", 300, Gdx.graphics.getHeight()-40);
        if(hitJ) font.draw(batch, "Jump", 375, Gdx.graphics.getHeight()-40);
        if(hitD) font.draw(batch, "Defense", 425, Gdx.graphics.getHeight()-40);

        //Stage
        font.draw(batch, "StageX: " + ((DefaultStage) getStage()).boundX, 0, Gdx.graphics.getHeight()-20);
        font.draw(batch, "StageW: " + ((DefaultStage) getStage()).boundW, 150, Gdx.graphics.getHeight()-20);
        font.draw(batch, "StageZ1: " + ((DefaultStage) getStage()).boundZ1, 300, Gdx.graphics.getHeight()-20);
        font.draw(batch, "StageZ2: " + ((DefaultStage) getStage()).boundZ2, 450, Gdx.graphics.getHeight()-20);
    }

}
