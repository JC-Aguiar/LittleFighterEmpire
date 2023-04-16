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

import static br.com.jcaguiar.lfe.CharacterCommandKeyword.*;
import static br.com.jcaguiar.lfe.CharacterCoreFrames.*;
import static br.com.jcaguiar.lfe.CharacterFrameKeyword.*;

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
    boolean punch1 = false;

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
    public static final float MAX_ACC_X = 30f;
    public static final float  MAX_ACC_Y = 50f;
    public static final float  MAX_ACC_Z = 30f;
    public static final float  MIN_ACC = 0.05f;

    //INPUTS
    boolean keyUp, keyDown, keyLeft, keyRight;
    boolean hitRun;
    boolean hitA, hitJ, hitD, hitAJ, hitDfA, hitDuA, hitDdA, hitDfJ, hitDuJ, hitDdJ, hitDAJ;
    int holdA, holdJ, timerA, timerJ, timerD;

    //SCORE
    int totalDamage, totalInjury, totalMpCost, totalKills, totalDeaths, totalItens;
    //int totalCure;            TODO: implement
    //int totalBuffs;            TODO: implement
    //int totalNerfs;            TODO: implement
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
        setCurrentSprite();
        setFaceSide();
        movement();
        checkNewFrame();
//        setRandomPosition();
    }

    public DataFrame currentDataFrame() {
        return dataFrames.get(frameIndex);
    }

    private void setFrameTimer() {
        frameTimer = (float) currentDataFrame().get(WAIT) * 0.08f;
    }

    private void checkValidFrame() {
        frameIndex = currentDataFrame() == null ? 0 : frameIndex;
        frameIndex = Math.max(frameIndex, 0);
    }

    private void checkNewFrame() {
        if(frameTimer <= 0) {
            val currentState = currentDataFrame().get(STATE);
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
            else if(currentDataFrame().get(NEXT_FRAME) == JUMP.frame  && !inAir) {
                if(holdJ > 5 && holdJ < 20) {
                    setNewFrame(PRE_JUMP.frame+1);
                    return;
                }
                setAccY(holdJ >= 20 ? -14f : -9f);
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
            //Jump State
            else if(currentState == JUMP.state && inAir) {
                setNewFrame(JUMP.frame);
            }
            //Land Frame
            else if(frameIndex == CROUCH.frame) {
                if(!hasEffectiveAccX() && !hasEffectiveAccZ()) {
                    setNewFrame(currentDataFrame().get(NEXT_FRAME));
                }
            }
            //Defend/Guard State
            else if(frameIndex == GUARD.frame) {
                if(!hitD) setNewFrame(currentDataFrame().get(NEXT_FRAME));
            }
            //Any Other State
            else setNewFrame(currentDataFrame().get(NEXT_FRAME));
        }
    }

    private void setBoundsPerSprite() {
//        currentSprite.setCenter(currentSprite.getWidth()/2, 0f);
        setBounds(currentSprite.getX(),
                  currentSprite.getY(),
                  currentSprite.getWidth(),
                  currentSprite.getHeight());
    }

    private void setCurrentSprite() {
        picIndex = currentDataFrame().get(PIC);
        currentSprite = new Sprite(sprites[picIndex]);
        currentSprite.setOriginCenter();
//        currentSprite.setPosition(40, 0);
//        setOriginX((float) currentSprite.getRegionWidth()/2);
//        setOriginY(currentSprite.getRegionY() + currentDataFrame().get(CENTER_Y) / 2);
//        currentSprite.setCenter(0f, 0f);
    }

    public boolean isMovable() {
        return currentDataFrame().get(STATE) == STAND.state
            || currentDataFrame().get(STATE) == WALK.state
            || currentDataFrame().get(STATE) == RUN.state;
    }

    public boolean isFlippable() {
        return currentDataFrame().get(STATE) == STAND.state
            || currentDataFrame().get(STATE) == WALK.state
            || (currentDataFrame().get(STATE) == JUMP.state && inAir)
            || currentDataFrame().get(STATE) == GUARD.state;
    }

    public boolean isJumpable() {
        return weaponId <= 0 && isMovable();
    }

    public boolean isAttackable() {
        return currentDataFrame().get(STATE) == STAND.state
            || currentDataFrame().get(STATE) == WALK.state
            || currentDataFrame().get(STATE) == RUN.state
            || currentDataFrame().get(STATE) == JUMP.state
            || currentDataFrame().get(STATE) == GUARD.state
            || currentDataFrame().get(STATE) == DASH_FRONT.state;
    }

    public boolean isDefendable() {
        return currentDataFrame().get(STATE) == STAND.state
            || currentDataFrame().get(STATE) == WALK.state;
    }

    private void setNewFrame(int index) {
        frameIndex = index;
        checkValidFrame();
        setFrameTimer();
        setNewFrameAcceleration();
    }

    private void setNewFrame(int index, float timer) {
        frameIndex = index;
        checkValidFrame();
        frameTimer = timer;
        setNewFrameAcceleration();
    }

    private void setNewFrameAcceleration() {
        final int relativeDvx = currentDataFrame().get(DVX) * getModBySide();
        final boolean isDvz = currentDataFrame().get(DVZ) != 0 && (keyUp || keyDown);

        //Apply acceleration when needed
        if(relativeDvx != 0) setAccX(accX + relativeDvx * getDvxMod());
        setAccY(accY + getDvyMod());
        if(isDvz) setAccZ(accZ + getDvzMod() * (keyUp? -1 : 1));
    }

    private void preJump() {
        if(currentDataFrame().get(STATE) == STAND.state || currentDataFrame().get(STATE) == WALK.state  && !inAir) {
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

    public float getObjectiveX() { return getRelativeBodyX() + getWidth() * getModBySide() - currentDataFrame().get(CENTER_X) * getModBySide(); }

    public float getObjectiveY() { return getY() + currentDataFrame().get(CENTER_Y); }

    public float getObjectiveZ() { return posZ + getHeight(); }

    public boolean isMovingForward() {
        return right ? accX > 0 : accX < 0;
    }

    public void movement() {
        float movZ = 0f, movX = 0f;

        //Check Movement Keys
        if (keyUp) movZ = -1f;
        else if (keyDown) movZ = 1f;
        if (keyRight) movX = 2f;
        else if (keyLeft) movX = -2f;

        //Check action keys
        if(hitA) {
            if(holdA == 0) timerA = 3;
            holdA += 1;
        } else {
            holdA = 0;
            timerA = timerA <= 0 ? 0 : timerA-1;
        } if(hitJ) {
            if(holdJ == 0) timerJ = 3;
            holdJ += 1;
        } else {
            holdJ = 0;
            timerJ = timerJ <= 0 ? 0 : timerJ-1;
        } if(hitD) {
            timerD = 3;
        } else {
            timerD = timerD <= 0 ? 0 : timerD-1;
        }

        //Flip X
        if (movX > 0 && isFlippable()) right = true;
        else if (movX < 0 && isFlippable()) right = false;

        //Check Command Keys
        checkSpecialCommands(movX, movZ);
        final int triggerFrame = doSpecialCommands();
        if(triggerFrame != 0)
            setNewFrame(triggerFrame);
        else {
            if (hitA && holdA < 3 && isAttackable()) basicAttack();
            else if (hitJ && holdJ < 3 && isJumpable()) preJump();
            else if (hitD && isDefendable()) setNewFrame(GUARD.frame);
        }

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
                if (currentDataFrame().get(STATE) == STAND.state) {
                    setNewFrame(WALK.frame, 2.0f * 0.1f);
                }
            }
            //No keys pressed
            else {
                walkCount = runCount = 0;
                walkReverse = runReverse = false;
                if (currentDataFrame().get(STATE) == WALK.state)
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

    private void checkSpecialCommands(float movX, float movZ) {
        hitDuA = (timerA > 0 && movZ < 0 && timerD > 0);
        hitDfA = (timerA > 0 && movX != 0 && timerD > 0);
        hitDdA = (timerA > 0 && movZ > 0 && timerD > 0);
        hitDuJ = (timerJ > 0 && movZ < 0 && timerD > 0);
        hitDfJ = (timerJ > 0 && movX != 0 && timerD > 0);
        hitDdJ = (timerJ > 0 && movZ > 0 && timerD > 0);
    }

    private int doSpecialCommands() {
        final Map<CharacterCommandKeyword, Integer> mapSkillCommands = currentDataFrame().getCommands();
        for(CharacterCommandKeyword cmd : mapSkillCommands.keySet()) {
            if(mapSkillCommands.get(cmd) == 0) continue;
            if(cmd == HIT_UA && hitDuA) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_FA && hitDfA) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_DA && hitDdA) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_UJ && hitDuJ) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_FJ && hitDfJ) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_DJ && hitDdJ) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_ALL && hitDAJ) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_AJ && hitAJ) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_A && timerA > 0) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_J && timerJ > 0) return mapSkillCommands.get(cmd);
            else if(cmd == HIT_D && timerD > 0) return mapSkillCommands.get(cmd);
        }
        return 0;
    }

    private void basicAttack() {
        val currentState = currentDataFrame().get(STATE);
        //Normal attack
        if(currentState == STAND.state || currentState == WALK.state || currentState == GUARD.state)
            setNewFrame(!punch1 ? ATTACK_1.frame : ATTACK_2.frame);
        //Running attack
        else if(currentState == RUN.state)
            setNewFrame(RUN_ATTACK.frame);
        //Jump attack
        else if(currentState == JUMP.state && inAir)
            setNewFrame(JUMP_ATTACK.frame);
        //Dash attack
        else if(currentState == DASH_FRONT.state && isMovingForward() && inAir)
            setNewFrame(DASH_ATTACK.frame);
        punch1 = !punch1;
    }

    public void setLocation(float movX, float movZ) {
        //Apply gravity and set X/Y/Z acceleration values
        doGravity();

        //Calculate acceleration X/Z axis when in the ground (friction)
        if(!inAir && accX != 0) setAccX(accX - accX * 0.1f);
        if(!inAir && accZ != 0) setAccZ(accZ - accZ * 0.1f);

        //Check minimum valid acceleration value or become 0
        if(!hasAccX()) setAccX(0f);
        if(!hasAccZ()) setAccZ(0f);

        //Apply X/Z acceleration to movement
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
        //Z < 1000: Foreground (Game Background)
        //Z < 2000: Middle (Game Objects)
        //Z >= 2000: Front (Game UI)
    }

    private void doGravity() {
        if(posY > -1 && accY >= 0) {
            setAccY(0f);
            posY = 0;
            if(inAir) setFrameIndex(CROUCH.frame);
            inAir = false;
        } else {
            setAccY(accY + Math.abs(accY * 0.1f) + 0.1f);
            if(isMovable()) setFrameIndex(JUMP.frame);
            inAir = true;
        }
        posY += accY;
    }

    private boolean hasAccX() { return accX < -MIN_ACC || accX > MIN_ACC; }

    private boolean hasAccZ() { return accZ < -MIN_ACC || accZ > MIN_ACC; }

    private boolean hasEffectiveAccX() { return accX < -MIN_ACC*4 || accX > MIN_ACC*4; }

    private boolean hasEffectiveAccZ() { return accZ < -MIN_ACC*4 || accZ > MIN_ACC*4; }

    private float getDvxMod() { return 0.2f; }

    private float getDvyMod() {
        return currentDataFrame().get(DVY) * 1.6f;
    }

    private float getDvzMod() {
        return 0.02f;
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

    public float getRelativeBodyX() {
        return right ?  getX() : getX() + getWidth();
    }

    public float getDisplayX() {
        return getX() + (getWidth()/2) * getModBySide() - currentDataFrame().get(CENTER_X) * getModBySide();
    }

    private int getModBySide() {
        return right ? 1 : -1;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        setBoundsPerSprite();
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
                getRelativeBodyX() + currentDataFrame().getBodies().get(0).x * getModBySide(),
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
        batch.draw(currentSprite, (int) getDisplayX(), getY(), (int) getWidth(), getHeight()); //batch.draw(batch, deltaTime);
        //Drawing numbers
        drawDebugInfo(batch);
        batch.end();

        //(int) getX() - currentDataFrame().get(CENTER_X) * getModBySide()
        //getRelativeSideX()

        //Drawing center point
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(1f, 0f, 0f, 1f);
        debugRenderer.circle(getObjectiveX(), getObjectiveY(), 2f);
        debugRenderer.end();
        batch.begin();



        frameTimer -= Gdx.graphics.getDeltaTime();
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
        font.draw(batch, "AtkTimer: " + timerA, 600, 20);
        font.draw(batch, "JmpTimer: " + timerJ, 750, 20);
        font.draw(batch, "DefTimer: " + timerD, 900, 20);

        //Keys
        if(keyLeft) font.draw(batch, "Left", 0, Gdx.graphics.getHeight()-40);
        if(keyUp) font.draw(batch, "Up", 75, Gdx.graphics.getHeight()-40);
        if(keyRight) font.draw(batch, "Right", 150, Gdx.graphics.getHeight()-40);
        if(keyDown) font.draw(batch, "Down", 225, Gdx.graphics.getHeight()-40);
        if(hitA) font.draw(batch, "Attack", 300, Gdx.graphics.getHeight()-40);
        if(hitJ) font.draw(batch, "Jump", 375, Gdx.graphics.getHeight()-40);
        if(hitD) font.draw(batch, "Defense", 425, Gdx.graphics.getHeight()-40);
        if(hitDuA) font.draw(batch, "D.U.A", 500, Gdx.graphics.getHeight()-40);
        if(hitDfA) font.draw(batch, "D.F.A", 575, Gdx.graphics.getHeight()-40);
        if(hitDdA) font.draw(batch, "D.D.A", 625, Gdx.graphics.getHeight()-40);
        if(hitDuJ) font.draw(batch, "D.U.J", 700, Gdx.graphics.getHeight()-40);
        if(hitDfJ) font.draw(batch, "D.F.J", 775, Gdx.graphics.getHeight()-40);
        if(hitDdJ) font.draw(batch, "D.D.J", 825, Gdx.graphics.getHeight()-40);

        //Stage
        font.draw(batch, "StageX: " + ((DefaultStage) getStage()).boundX, 0, Gdx.graphics.getHeight()-20);
        font.draw(batch, "StageW: " + ((DefaultStage) getStage()).boundW, 150, Gdx.graphics.getHeight()-20);
        font.draw(batch, "StageZ1: " + ((DefaultStage) getStage()).boundZ1, 300, Gdx.graphics.getHeight()-20);
        font.draw(batch, "StageZ2: " + ((DefaultStage) getStage()).boundZ2, 450, Gdx.graphics.getHeight()-20);
    }

}
