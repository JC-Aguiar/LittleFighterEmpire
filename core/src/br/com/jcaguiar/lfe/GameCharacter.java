package br.com.jcaguiar.lfe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
    float displayX, displayY;
    int walkCount, runCount = 0;
    boolean walkReverse, runReverse = false;
    Sprite currentSprite;

    int runTimer; //only for player
    private ShapeRenderer zRenderer = new ShapeRenderer(); //TODO: teste! remove!
    private ShapeRenderer centerRenderer = new ShapeRenderer(); //TODO: teste! remove!
    private ShapeRenderer bodyRenderer = new ShapeRenderer(); //TODO: teste! remove!

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
    float posX, posY, posZ, altitude;
    float accX, accY, accZ;
    float speedWalkX, speedWalkZ;
    float speedRunX, speedRunZ;
    float jumpX, jumpY, jumpZ;
    float startX, startZ;
    int weaponId, grabId = -1;
    int hitLag;

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
        setCurrentSprite();
        setFaceSide();

        addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return keyPress(keycode);
            }
        });
        addListener(new InputListener(){
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                return keyRelease(keycode);
            }
        });
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
    public void startVSMode(int hp, int mp, int team, int frame, boolean isHuman, int posX, int posY) {
        this.hpMax = this.hpNow = this.hpLimit = hp;
        this.mpMax = mp;
        this.mpNow = this.mpLimit = 500;
        this.team = team;
        this.frameIndex = frame;
        this.isHuman = isHuman;
        this.posX = posX;
        this.posY = posY;
        setFaceSide();
        checkValidFrame();
        setFrameTimer();
        setZIndex((int) (this.posY + getHeight()));
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
            if(currentState == WALK.state) {
                if(walkCount >= walkingFrameRate) walkReverse = true;
                else if(walkCount <= 0) walkReverse = false;
                walkCount += walkReverse ? -1 : 1;
                setNewFrame(WALK.frame + walkCount, 2.0f * 0.1f);
            }
            //Running State
            else if(currentState == RUN.state) {
                if(runCount >= runningFrameRate) runReverse = true;
                else if(runCount <= 0) runReverse = false;
                runCount += runReverse ? -1 : 1;
                setNewFrame(runReverse ? frameIndex-1 : frameIndex+1 , 1.25f * 0.1f);
            }
            //Pre-Jump Frame
            else if(currentDataFrame().getNextFrame() == JUMP.frame) {
                setNewFrame(currentDataFrame().getNextFrame());
                inAir = true;
                accY = -10.f;
            }
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
        setX(getX() + posX + accX);
        setY(getY() + posY + gravity());
        setOriginX(currentSprite.getRegionWidth() - currentDataFrame().getCenterX());
        setOriginY(currentSprite.getRegionY() + currentDataFrame().getCenterY() / 2);
    }

    private float gravity() {
        altitude = accY + (accY * - 0.3f) + 0.01f;
        altitude = Math.min(altitude, 10f);
        altitude = Math.max(altitude, -10f);
        return !inAir ? 0f : accY + (accY * - 0.3f) + 0.01f;
    }

    private void setAccX(float newAccX) {
        accX = Math.min(newAccX, 10f);
        accX = Math.max(newAccX, -10f);
    }

    private void setAccY(float newAccY) {
        accY = Math.min(newAccY, 10f);
        accY = Math.max(newAccY, -10f);
    }

    private void setAccZ(float newAccZ) {
        accZ = Math.min(newAccZ, 10f);
        accZ = Math.max(newAccZ, -10f);
    }

    public boolean isMovable() {
        return currentDataFrame().getState() == STAND.state
            || currentDataFrame().getState() == WALK.state
            || currentDataFrame().getState() == RUN.state;
    }

    public boolean isFlippable() {
        return currentDataFrame().getState() == STAND.state
            || currentDataFrame().getState() == WALK.state
            || currentDataFrame().getState() == JUMP.state
            || currentDataFrame().getState() == GUARD.state;
    }

    public boolean isJumpable() {
        return weaponId > 0 ? isMovable() : false;
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

    public void movement() {
        if(hitJ && isJumpable()) jump();
        if(!isMovable()) return;
        float movY = 0f, movX = 0f;

        //Check pressed keys
        if(keyUp && getZIndex() > 0) movY = -1f;
        else if(keyDown && getZIndex() < Gdx.graphics.getHeight()) movY = 1f;
        if(keyRight && getX() < Gdx.graphics.getWidth()) movX = 2f;
        else if(keyLeft && getX() > 0) movX = -2f;

        //Case horizontal movement
        if(movX != 0 || movY != 0) {
            //Flip X
            if(movX > 0 && isFlippable())
                right = true;
            else if(movX < 0 && isFlippable())
                right = false;
            //Z axis
            if(movY != 0) setZIndex(Math.max(getZIndex() + (int) movY, 0));
            //Dual movement
            if(movX != 0 && movY != 0) {
                movX = movX * 0.75f;
                movY = movY * 0.75f;
            }
            //Start walking frame
            if(currentDataFrame().getState() == STAND.state) {
                setNewFrame(WALK.frame, 2.0f * 0.1f);
            }
        }
        //Case no horizontal movement
        else {
            walkCount = runCount = 0;
            walkReverse = runReverse = false;
            if(currentDataFrame().getState() == WALK.state)
                setNewFrame(STAND.frame);
        }

        posX = Math.max(posX + movX + currentDataFrame().getDvx(), 0);
        posY += movY + currentDataFrame().getDvy();
    }

    private void jump() {
        if(currentDataFrame().getState() == STAND.state || currentDataFrame().getState() == WALK.state) {
            setNewFrame(PRE_JUMP.frame);
        }
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
        zRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        zRenderer.begin(ShapeRenderer.ShapeType.Filled);
        zRenderer.setColor(1f, 0f, 0f, 0.33f);
        zRenderer.rect(0, getZIndex(), Gdx.graphics.getWidth(), 5);
        zRenderer.end();
        if(currentDataFrame().getBodies().size() > 0) {
            bodyRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            bodyRenderer.begin(ShapeRenderer.ShapeType.Filled);
            bodyRenderer.setColor(0.5f, 0f, 0.5f, 1f);
            bodyRenderer.rect(
                getRelativeSideX() + currentDataFrame().getBodies().get(0).x * getModBySide(),
                getY() + currentDataFrame().getBodies().get(0).y,
                currentDataFrame().getBodies().get(0).w * getModBySide(),
                currentDataFrame().getBodies().get(0).h);
            bodyRenderer.end();
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);

        //Drawing the sprite
        batch.begin();
        batch.draw(currentSprite, getX(), getY(), getWidth(), getHeight());
        //batch.draw(batch, deltaTime);
        batch.end();

        centerRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        centerRenderer.begin(ShapeRenderer.ShapeType.Filled);
        centerRenderer.setColor(1f, 0f, 0f, 1f);
        centerRenderer.circle(getX() + getOriginX(), getY() + getOriginY(), 2f);
        centerRenderer.end();
        batch.begin();

        frameTimer -= Gdx.graphics.getDeltaTime();

        setBoundsPerSprite();
    }

}
