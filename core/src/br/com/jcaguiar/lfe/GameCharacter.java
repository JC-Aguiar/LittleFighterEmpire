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
import javafx.scene.shape.Circle;
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
    int strength, vitality, dexterity, agility, power; //will;
    //Level     = Goes 1~7 and the default level 1 hero has 5 points in all attributes.
    //            VS-Mode enable to start at level 4.
    //Strength  = +5% in all-attacks, throwing weapons, handling heavy-objects. +3% defense.
    //Vitality  = +10% hp-recovery and mp-recovery. +3% defense, fall-reduction, heal and status-effects (buff|nerf).
    //Dexterity = +4% critical-attack ratio (ratio = dexterity*4 ~ 100).
    //Agility   = +5% basic-attack speed and mobility (jump|dash|roll distance, walk|run speed, less roll time).
    //Power     = +10% in skill-attacks, heal and cast distance/speed.
    //
    //  Lv 1: basic skill-set       | Davis: spaming balls      | Deep: spin-attack         | Freeze: winter breath
    //  Lv 2: +1 super-skill        | Davis: flashbang punch    | Deep: blade blast         | Freeze: ice columns
    //  Lv 3: +1 super-skill        | Davis: dragon speed       | Deep: killer blade        | Freeze: whirlwind
    //  Lv 4: +1 super-skill        | Davis: dragon punch       | Deep: berserk assault     | Freeze: ice forge
    //  Lv 5: upgrade 1 super-skill (free-cost)
    //  Lv 6: upgrade 1 super-skill (free-cost)
    //  Lv 7: upgrade 1 super-skill (free-cost)
    //
    //  Core-Attributes:
    //  Each character can select 3 core-attributes: 1 principal and 2 secondaries.
    //  Principal attribute gain +2 for each level, while the secondaries gains +1.
    //
    //  Critical-Attacks:
    //  Converts a percentage of the attack as bonus damage/fall that ignores defense and reduce enemies max hp.
    //  Melee attacks gains +50% ratio against vulnerable enemies.
    //  Additional +25% damage for all Weapon-Attacks (shoot/throw a weapon decrease the ratio by they altitude).
    //  Area-Attacks apply critical by calculating how centered the enemy is (size * enemy-hit-position).
    //  Non-Area-Attacks apply critical by the ratio itself.


    //PHYSICS
    //int size TODO: implement
    boolean right = new Random().nextBoolean();
    boolean inAir = false;
    float posX, posY, posZ;
    float accX = 0, accY = 0, accZ = 0;
    float runMomentum = 0;
    float jumpX, jumpY, jumpZ;
    float startX, startZ;
    int weaponId, grabId = -1;
    int hitLag;
    public static final float MAX_ACC_X = 30f;
    public static final float MAX_ACC_Y = 50f;
    public static final float MAX_ACC_Z = 30f;
    public static final float MIN_ACC = 0.05f;

    //INPUTS
    boolean keyUp, keyDown, keyLeft, keyRight;
    boolean hitRun, hitA, hitJ, hitD, hitAJ, hitDfA, hitDuA, hitDdA, hitDfJ, hitDuJ, hitDdJ, hitDAJ;
    int holdLeft, holdRight, holdA, holdJ;
    int timerLeft, timerRight, timerA, timerJ, timerD;
    int lastInput = -1;

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
            case Input.Keys.RIGHT:  lastInput = keycode; keyRight = false; return true;
            case Input.Keys.LEFT:   lastInput = keycode; keyLeft = false; return true;
            case Input.Keys.UP:     lastInput = keycode; keyUp = false; return true;
            case Input.Keys.DOWN:   lastInput = keycode; keyDown = false; return true;
            case Input.Keys.Q:      lastInput = keycode; hitA = false; return true;
            case Input.Keys.W:      lastInput = keycode; hitJ = false; return true;
            case Input.Keys.E:      lastInput = keycode; hitD = false; return true;
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
        control();
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
                if(runCount >= runningFrameRate-1) runReverse = true;
                else if(runCount <= 0) runReverse = false;
                runCount += runReverse ? -1 : 1;
                setNewFrame(RUN.frame + runCount, 1.25f * 0.1f);
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
            else if(frameIndex == LAND.frame || frameIndex == CROUCH.frame) {
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
            || currentDataFrame().get(STATE) == RUN.state
            && !inAir;
    }

    public boolean isRunnable() {
        return currentDataFrame().get(STATE) == STAND.state
            || currentDataFrame().get(STATE) == WALK.state
            && !inAir;
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
            || currentDataFrame().get(STATE) == WALK.state
            && !inAir;
    }

    public boolean isRollable() {
        return currentDataFrame().get(STATE) == RUN.state
            || frameIndex == CROUCH.frame
            && !inAir;
    }

    public boolean isFrictioning() {
        return currentDataFrame().get(STATE) == LAND.state
            || currentDataFrame().get(STATE) == LYING_BACK.state;
    }

    public boolean isPunchable() {
        return  currentDataFrame().get(STATE) == STAND.state
            ||  currentDataFrame().get(STATE) == WALK.state;
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
        //When stopping run
        if(frameIndex == STOP_RUN.frame) setAccX(accX + 1f * getModBySide());

        //Apply acceleration when needed
        setAccX(accX + getDvxMod());
        setAccY(accY + getDvyMod());
        if(keyUp || keyDown) setAccZ(accZ + getDvzMod() * (keyUp? -1 : 1));
    }

    private void jumpDash() {
        if(currentDataFrame().get(STATE) == STAND.state || currentDataFrame().get(STATE) == WALK.state) {
            setNewFrame(PRE_JUMP.frame);
        } else if(currentDataFrame().get(STATE) == RUN.state) {
            setNewFrame(DASH_FRONT.frame);
            float movZ = 0f;
            if(keyUp) movZ = -1f;
            else if(keyDown) movZ = 1f;
            setAccX(accX + (movZ == 0 ? 2.5f : 1.5f) * getModBySide());
            setAccY(accY - Math.min(Math.abs(accX) / 1.5f + 3f, 9f));
            setAccZ(accZ + movZ);
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

//    public float getObjectiveX() {
//        return (right ?  getX() : getX() + getWidth()) + getWidth() * getModBySide() - currentDataFrame().get(CENTER_X) * getModBySide();
//    }
//
//    public float getObjectiveY() { return getY() + currentDataFrame().get(CENTER_Y); }
//
//    public float getObjectiveZ() { return posZ + getHeight(); }

    public boolean isMovingForward() {
        return right ? accX > 0 : accX < 0;
    }

    public void control() {
        float movZ = 0f, movX = 0f;

        //Check Movement Keys
        if(keyUp)
            movZ = -1f;
        else if(keyDown)
            movZ = 1f;
        if(keyRight) {
            movX = 2f;
            hitRun = timerRight > 0 && lastInput == Input.Keys.RIGHT;
            if(holdRight == 0) timerRight = 12;
            holdRight += 1;
            holdLeft = 0;
        } else if(keyLeft) {
            movX = -2f;
            hitRun = timerLeft > 0 && lastInput == Input.Keys.LEFT;
            if(holdLeft == 0) timerLeft = 12;
            holdLeft += 1;
            holdRight = 0;
        } else {
            holdLeft = holdRight = 0;
        }
        if(hitA) {
            if(holdA == 0) timerA = 8;
            holdA += 1;
        } else {
            holdA = 0;
        } if(hitJ) {
            if(holdJ == 0) timerJ = 8;
            holdJ += 1;
        } else {
            holdJ = 0;
        } if(hitD) {
            timerD = timerD == 0 ? 8 : timerD;
        }
        timerRight = timerRight <= 0 ? 0 : timerRight - 1;
        timerLeft = timerLeft <= 0 ? 0 : timerLeft - 1;
        timerA = timerA <= 0 ? 0 : timerA-1;
        timerJ = timerJ <= 0 ? 0 : timerJ-1;
        timerD = timerD <= 0 ? 0 : timerD-1;

        //Flip X
        if(movX > 0 && isFlippable()) right = true;
        else if(movX < 0 && isFlippable()) right = false;

        //Special actions
        checkSpecialCommands();
        final int triggerFrame = doSpecialCommands();
        if(triggerFrame != 0)
            setNewFrame(triggerFrame);
        //Basic actions
        else {
            if(hitA && holdA < 3 && isAttackable()) basicAttack();
            else if(hitJ && holdJ < 3 && isJumpable()) jumpDash();
            else if(hitD) {
                if(isDefendable() && timerD == 7)
                    setNewFrame(GUARD.frame+1);
                else if(isRollable()) {
                    right = accX >= 0;
                    setNewFrame(DODGE.frame);
                }
            }
        }

        //Basic movement
        if(isMovable()) {
            //Is moving
            if(movX != 0 || movZ != 0) {
                //Is dual movement
                if(movX != 0 && movZ != 0) {
                    movX = movX * 0.75f;
                    movZ = movZ * 0.75f;
                }
                //Start walking/running
                if(currentDataFrame().get(STATE) == STAND.state) {
                    if(hitRun && movX != 0) {
                        runMomentum = 4;
                        setAccX(accX +  6f * getModBySide());
                        setNewFrame(RUN.frame, 1.2f * 0.1f);
                    } else
                        setNewFrame(WALK.frame, 2.0f * 0.1f);

                } //Stop running frame
                else if(currentDataFrame().get(STATE) == RUN.state) {
                    if(movX < 0 && accX > 0) setNewFrame(STOP_RUN.frame);
                    if(movX > 0 && accX < 0) setNewFrame(STOP_RUN.frame);
                }

            } //No keys pressed
            else {
                if(currentDataFrame().get(STATE) == WALK.state) setNewFrame(STAND.frame);
            }

        } //Not movable
        else {
            movZ = movX = 0f;
            walkCount = runCount = 0;
            walkReverse = runReverse = false;
        }

        //Set running modifiers
        if(currentDataFrame().get(STATE) == RUN.state) {
            movX = runMomentum * getModBySide();
            movZ = movZ != 0 ? movZ * 0.5f : movZ;
//            setAccX(accX + runMomentum * getModBySide());
            runMomentum = runMomentum <= 0 ? 0 : runMomentum-0.05f;
        }
        else runMomentum = 0;

        //Update positions
        setLocation(movX, movZ);
    }

    private void checkSpecialCommands() {
        hitDuA = (timerA > 0 && keyUp && timerD > 0);
        hitDfA = (timerA > 0 && (keyLeft || keyRight) && timerD > 0);
        hitDdA = (timerA > 0 && keyDown && timerD > 0);
        hitDuJ = (timerJ > 0 && keyUp && timerD > 0);
        hitDfJ = (timerJ > 0 && (keyLeft || keyRight) && timerD > 0);
        hitDdJ = (timerJ > 0 && keyDown && timerD > 0);
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
        //Normal attack
        if(isPunchable())
            setNewFrame(!punch1 ? ATTACK_1.frame : ATTACK_2.frame);
        //Running attack
        else if(currentDataFrame().get(STATE) == RUN.state) {
            setAccX(accX + 1f * getModBySide());
            setNewFrame(RUN_ATTACK.frame);
        } //Jump attack
        else if(currentDataFrame().get(STATE) == JUMP.state && inAir)
            setNewFrame(JUMP_ATTACK.frame);
        //Dash attack
        else if(currentDataFrame().get(STATE) == DASH_FRONT.state && isMovingForward() && inAir)
            setNewFrame(DASH_ATTACK.frame);
        punch1 = !punch1;
    }

    public void setLocation(float movX, float movZ) {
        //Apply gravity and set X/Y/Z acceleration values
        doGravity();

        //Calculate friction (X/Z axis)
        if(!inAir && accX != 0 && currentDataFrame().get(STATE) != RUN.state)
            setAccX(accX - (isFrictioning() ? accX * 0.15f : accX * 0.08f));
        if(!inAir && accZ != 0)
            setAccZ(accZ - (isFrictioning() ? accZ * 0.15f : accZ * 0.08f));


        //Check minimum valid acceleration value or become 0
        if(!hasAccX()) setAccX(0f);
        if(!hasAccZ()) setAccZ(0f);

        //Apply X/Z acceleration and check collision
        movX = movX + accX;
        movZ = movZ + accZ;
        if(posX + movX < ((DefaultStage)getStage()).boundX) movX = 0;
        if(posX + movX > ((DefaultStage)getStage()).boundW) movX = 0;
        if(getDisplayZ() + movZ < ((DefaultStage)getStage()).boundZ1) movZ = 0;
        if(getDisplayZ() + movZ > ((DefaultStage)getStage()).boundZ2) movZ = 0;
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
            if(inAir) {
                int currentState = currentDataFrame().get(STATE);
                if(currentDataFrame().get(STATE) == JUMP.state)
                    setFrameIndex(CROUCH.frame);
                else if(currentDataFrame().get(STATE) == LYING_FRONT.state)
                    setFrameIndex(LYING_FRONT.frame);
                else
                    setFrameIndex(LAND.frame);
            }
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

    private float getDvxMod() {
        if(currentDataFrame().get(DVX) >= 550) return 0;
        return ((currentDataFrame().get(DVX) / 5) + (currentDataFrame().get(DVX) / 10) * 5
            + currentDataFrame().get(DVX) * 0.15f) * getModBySide();
    }

    private float getDvyMod() {
        if(currentDataFrame().get(DVY) >= 550) return 0;
        return currentDataFrame().get(DVY) * 1.6f;
    }

    private float getDvzMod() {
        if(currentDataFrame().get(DVZ) >= 550) return 0;
        return (currentDataFrame().get(DVZ) / 10) * 5 + currentDataFrame().get(DVZ) * 0.1f;
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

    public float getRelativeBodyX(int index) {
        if(currentDataFrame().getBodies().isEmpty()) return 0;
        return (right ? getDisplayX() : getDisplayX() + getWidth())
            + currentDataFrame().getBodies().get(index).x * getModBySide();
    }

    public float getDisplayX() {
        return getX() + (getWidth()/2)
            - (right ? currentDataFrame().get(CENTER_X) : (getWidth() - currentDataFrame().get(CENTER_X)));
    }

    public float getDisplayY() { return getY() - getHeight() + currentDataFrame().get(CENTER_Y); }

    public float getDisplayZ() { return posZ + getHeight(); }

    private int getModBySide() {
        return right ? 1 : -1;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        setBoundsPerSprite();
        control();
        checkNewFrame();
        setCurrentSprite();
        setFaceSide();

        //Drawing debug info (coordinates and bodies)
        batch.end();
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        for(int i = 0; i < currentDataFrame().getBodies().size(); i++) {
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
            debugRenderer.setColor(0.5f, 0f, 0.5f, 0.5f);
            debugRenderer.rect(
                getRelativeBodyX(i),
                getY() + currentDataFrame().getBodies().get(i).y,
                currentDataFrame().getBodies().get(i).w * getModBySide(),
                currentDataFrame().getBodies().get(i).h
            );
            debugRenderer.end();
        }

        //Drawing shadow
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(0f, 0f, 0f, 0.5f);
        debugRenderer.rect(getX() + getWidth()/2 - 15f, getDisplayZ() - 10f, 30f, 10f);
        debugRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        //Drawing the sprite
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(currentSprite, (int) getDisplayX(), getDisplayY(), (int) getWidth(), getHeight()); //batch.draw(batch, deltaTime);
        //Drawing numbers
        drawDebugInfo(batch);
        batch.end();

        //(int) getX() - currentDataFrame().get(CENTER_X) * getModBySide()
        //getRelativeSideX()

        //Drawing frame
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(1f, 0f, 0f, 1f);
        debugRenderer.rect(getX(), getY(), getWidth(), getHeight());
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
        font.draw(batch, "DisplayX: " + getDisplayX(), 600, 0);
        font.draw(batch, "DisplayY: " + getDisplayY(), 800, 0);
        font.draw(batch, "DisplayZ: " + getDisplayZ(), 1000, 0);
        font.draw(batch, "AccX: " + accX, 0, 20);
        font.draw(batch, "AccY: " + accY, 150, 20);
        font.draw(batch, "AccZ: " + accZ, 300, 20);
        font.draw(batch, "InAir: " + (inAir ? "true" : "false"), 450, 20);
        font.draw(batch, "RightTimer: " + timerRight, 0, 40);
        font.draw(batch, "LeftTimer: " + timerLeft, 125, 40);
        font.draw(batch, "HoldRight: " + holdRight, 250, 40);
        font.draw(batch, "HoldLeft: " + holdLeft, 375, 40);
        font.draw(batch, "AtkTimer: " + timerA, 500, 40);
        font.draw(batch, "JmpTimer: " + timerJ, 625, 40);
        font.draw(batch, "DefTimer: " + timerD, 750, 40);
        font.draw(batch, "RunMomentum: " + runMomentum, 875, 40);

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
