package br.com.jcaguiar.lfe.components.objects;

import br.com.jcaguiar.lfe.components.scene.DefaultStage;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

import static br.com.jcaguiar.lfe.components.objects.CharCoreFrames.*;
import static br.com.jcaguiar.lfe.components.objects.CommandsKeyword.*;
import static br.com.jcaguiar.lfe.components.objects.SpritePicKeyword.*;

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class GameChar extends GameObject {

    //CORE
    String name;
    boolean isHuman;
    int walkCount = 0, runCount = 0;
    boolean walkReverse = false, runReverse = false;
    boolean punch1 = false;
    int runTimer; //only for player
    public boolean debugNumbers = false; //TODO:remove
    private ShapeRenderer debugRenderer = new ShapeRenderer(); //TODO: teste! remove!
    BitmapFont font;  //TODO: teste! remove!

    //STATUS
    int lv;
    int fall, fallMax;
    int guard, guardMax;
    int armor, armorMax;
    int timerDead;
    final Map<StatusEffect, Integer> effectsMod = new HashMap<>();
    final Map<StatusEffect, Integer> effectsTime = new HashMap<>();
    final Map<Integer, Integer> marksMod = new HashMap<>();
    final Map<Integer, Integer> marksTime = new HashMap<>();
    boolean isLowHp = false, isLowMp = false, isSafe = false;

    //ATTRIBUTES
    int strength, vitality, dexterity, agility, power; //will;
    //Level     = Goes 1~7 and the default level 1 hero has 5 points in all attributes.
    //            VS-Mode enable to start at level 4.
    //Strength  = +5% in all-attacks, throwing weapons, handling heavy-objects. +3% defense.
    //Vitality  = +10% base hp-max, hp-recovery, mp-recovery, heal and status-effects (buff|nerf).
    //Dexterity = +4% minimal critical-attack and +5% max critical-attack (ratio = dexterity*4 ~ dexterity*5+25).
    //Agility   = +5% basic-attack speed and mobility (jump|dash|roll distance, walk|run speed, less roll time).
    //Power     = +10% in skill-attacks, heal and cast distance.
    // RATIO  | *5-5  *3+45 |   *4  *5+25 |
    // Dex 01 |  000 ~ 050  |  004 ~ 030  |
    // Dex 03 |  010 ~ 054  |  012 ~ 040  |
    // Dex 05 |  020 ~ 060  |  020 ~ 050  |
    // Dex 07 |  030 ~ 066  |  028 ~ 070  |
    // Dex 13 |  060 ~ 084  |  052 ~ 095  |
    // Dex 15 |  080 ~ 090  |  060 ~ 105  |
    // Dex 19 |  090 ~ 102  |  076 ~ 120  |
    // Dex 22 |  105 ~ 111  |  088 ~ 135  |
    // Dex 25 |  120 ~ 120  |  100 ~ 150  |
    //
    //  Lv 1: basic skill-set       | Davis: quick attack       | Deep: spin-attack         | Freeze: winter breath
    //  Lv 2: +1 super-skill        | Davis: spaming balls      | Deep: sharped blast       | Freeze: ice columns
    //  Lv 3: +1 super-skill        | Davis: flashbang punch    | Deep: killer blade        | Freeze: whirlwind
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
    //  Melee attacks gains +50% ratio against vulnerable enemies (the max can goes to 150%).
    //  Additional +25% damage for all Weapon-Attacks (shoot/throw a weapon decrease the ratio by they altitude).
    //  Area-Attacks apply critical by calculating how centered the enemy is (size * enemy-hit-position).
    //  Non-Area-Attacks apply critical by the ratio itself.

    //PHYSICS
    float runMomentum = 0;
    float jumpX, jumpY, jumpZ;

    //INPUTS
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

    public GameChar(GameObject dataObj) {
        this.sprites = dataObj.sprites;
        this.dataFrames.putAll(dataObj.dataFrames);
        this.bmpSources.addAll(dataObj.bmpSources);
        this.head = dataObj.head;
        this.name = dataObj.name;
        this.walkingFrameRate = dataObj.walkingFrameRate;
        this.runningFrameRate = dataObj.runningFrameRate;

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

    //VS MODE
    public void startVSMode(int hp, int mp, int team, int frame, int posX, int posZ) {
        this.hpMax = this.hpNow = this.hpLimit = hp;
        this.mpMax = mp;
        this.mpNow = this.mpLimit = 500;
        this.team = team;
        this.frameIndex = frame;
        this.isHuman = false;
        this.posX = posX;
        this.posY = 0;
        this.posZ = posZ;
        setCurrentSprite();
        setFaceSide();
        control();
        checkNewFrame();
//        setRandomPosition();
    }

    @Override
    protected void checkNewFrame() {
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
                    if(accX != 0) right = accX >= 0;
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
        hitDuA = (hitA && keyUp && timerD > 0);
        hitDfA = (hitA && (keyLeft || keyRight) && timerD > 0);
        hitDdA = (hitA && keyDown && timerD > 0);
        hitDuJ = (hitJ && keyUp && timerD > 0);
        hitDfJ = (hitJ && (keyLeft || keyRight) && timerD > 0);
        hitDdJ = (hitJ && keyDown && timerD > 0);
    }

    private int doSpecialCommands() {
        final Map<CommandsKeyword, Integer> mapSkillCommands = currentDataFrame().getCommands();
        for(CommandsKeyword cmd : mapSkillCommands.keySet()) {
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

    @Override
    protected void doFriction() {
        if(!inAir && accX != 0 && currentDataFrame().get(STATE) != RUN.state)
            setAccX(accX - (isFrictioning() ? accX * 0.15f : accX * 0.08f));
        if(!inAir && accZ != 0)
            setAccZ(accZ - (isFrictioning() ? accZ * 0.15f : accZ * 0.08f));
    }

    @Override
    protected void doLanding() {
        int currentState = currentDataFrame().get(STATE);
        if(currentDataFrame().get(STATE) == JUMP.state)
            frameIndex = CROUCH.frame;
        else if(currentDataFrame().get(STATE) == LYING_FRONT.state)
            frameIndex = LYING_FRONT.frame;
        else
            frameIndex = LAND.frame;
    }

    @Override
    protected void inMidAir() {
        setAccY(accY + Math.abs(accY * 0.1f) + 0.1f);
        if(isMovable()) frameIndex = JUMP.frame;
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
