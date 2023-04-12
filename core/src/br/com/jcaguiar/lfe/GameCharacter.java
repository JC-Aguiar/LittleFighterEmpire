package br.com.jcaguiar.lfe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
    private ShapeRenderer frameRenderer = new ShapeRenderer(); //TODO: teste! remove!
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
    boolean right, inAir = false;
    float posX, posY, posZ;
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
        this.right = new Random().nextBoolean();
        this.posX = posX;
        this.posY = posY;
        setFaceside();
        checkValidFrame();
        setFrameTimer();
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
        setX(getX() + posX);
        setY(getY() + posY);
        setOriginX(currentSprite.getRegionWidth() - currentDataFrame().getCenterX());
        setOriginY(currentSprite.getRegionY() + currentDataFrame().getCenterY() / 2);
        //        setX(posX + currentDataFrame().getCenterX());
        //        setY(posY - (getHeight() + 1) + currentDataFrame().getCenterY());
        //(currentSprite.getRegionWidth() - currentDataFrame().getCenterX()  +1)
        //(currentSprite.getRegionHeight() - currentDataFrame().getCenterY() +1)
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
        if(!isMovable()) return;
        float movY = 0f, movX = 0f;

        //Check pressed keys
        if(keyUp) movY = 1f;
        else if(keyDown) movY = -1f;
        if(keyRight) movX = 2f;
        else if(keyLeft) movX = -2f;

        //Case horizontal movement
        if(movX != 0 || movY != 0) {
            if(movX > 0 && isFlippable())
                right = true;
            else if(movX < 0 && isFlippable())
                right = false;
            if(movX != 0 && movY != 0) {
                movX = movX * 0.75f;
                movY = movY * 0.75f;
            }
            if(currentDataFrame().getState() == STAND.state) {
                setNewFrame(WALK.frame, 3.0f * 0.1f);
            }
        } //Case no horizontal movement
        else {
            walkCount = runCount = 0;
            walkReverse = runReverse = false;
            if(currentDataFrame().getState() == WALK.state)
                setNewFrame(STAND.frame);
        }

        posX += movX;
        posY += movY;
    }

    private void setFaceside() {
        currentSprite.flip(!right, false);
    }

    public float getRelativeSideX() {
        return right ?  getX() : currentSprite.getWidth();
    }

    private int getModBySide() {
        return right ? 1 : -1;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        movement();
        checkNewFrame();
        setCurrentSprite();
        setFaceside();

        batch.end();
        frameRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        frameRenderer.begin(ShapeRenderer.ShapeType.Filled);
        frameRenderer.setColor(1f, 0f, 0f, 0.33f);
        frameRenderer.rect(getX(), getY(), getWidth(), getHeight());
        frameRenderer.end();
        if(currentDataFrame().getBodies().size() > 0) {
            bodyRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            bodyRenderer.begin(ShapeRenderer.ShapeType.Filled);
            bodyRenderer.setColor(0.5f, 0f, 0.5f, 1f);
            /**
             * RIGHT:
             *          [     ]
             *          [  .  ]
             *       .
             *          . + x
             * LEFT:
             *          [     ]
             *          [  .  ]
             *                   .
             *                .
             */
            if(right) {
                bodyRenderer.rect(
                    getX() + currentDataFrame().getBodies().get(0).x,
                    getY() + getHeight() - currentDataFrame().getBodies().get(0).y,
                    currentDataFrame().getBodies().get(0).w,
                    -currentDataFrame().getBodies().get(0).h);
            } else {
                bodyRenderer.rect(
                    getX() + getWidth() - currentDataFrame().getBodies().get(0).x,
                    getY() + getHeight() - currentDataFrame().getBodies().get(0).y,
                    -currentDataFrame().getBodies().get(0).w,
                    -currentDataFrame().getBodies().get(0).h);
            }
            bodyRenderer.end();
        }
        batch.begin();

        batch.draw(currentSprite, getX(), getY(), getWidth(), getHeight());
        //batch.draw(batch, deltaTime);

        batch.end();
        centerRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        centerRenderer.begin(ShapeRenderer.ShapeType.Filled);
        centerRenderer.setColor(0f, 1f, 0f, 1f);
        centerRenderer.circle(getX() + currentDataFrame().getCenterX(), getY(), 1f);
        centerRenderer.end();
        batch.begin();

        frameTimer -= Gdx.graphics.getDeltaTime();

        setBoundsPerSprite();
    }

}
