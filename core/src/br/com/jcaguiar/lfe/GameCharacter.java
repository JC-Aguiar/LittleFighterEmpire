package br.com.jcaguiar.lfe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static br.com.jcaguiar.lfe.CharacterCoreFrames.RUN;
import static br.com.jcaguiar.lfe.CharacterCoreFrames.WALK;

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
        setBoundsPerSprite();
    }

    //VS MODE
    public void startVSMode(int hp, int mp, int team, int frame, boolean isHuman, int posX, int posY) {
        this.hpMax = this.hpNow = this.hpLimit = hp;
        this.mpMax = mp;
        this.mpNow = this.mpLimit = 500;
        this.team = team;
        this.frameIndex = frame;
        checkValidFrame();
        setFrameTimer();
        this.isHuman = isHuman;
        this.right = new Random().nextBoolean();
        this.posX = posX;
        this.posY = posY;
//        setRandomPosition();
    }

    private void setFrameTimer() {
        frameTimer = (float) dataFrames.get(frameIndex).getWait() * 0.1f;
    }

    private void checkValidFrame() {
        frameIndex = dataFrames.get(frameIndex) == null ? 0 : frameIndex;
        frameIndex = Math.max(frameIndex, 0);
    }

    private void checkNewFrame() {
        if(frameTimer <= 0) {
            val currentState = dataFrames.get(frameIndex).getState();
            if(currentState == WALK.state) {
                if(walkCount >= walkingFrameRate) walkReverse = true;
                else if(walkCount <= (walkingFrameRate * -2)) walkReverse = false;
                walkCount += walkReverse ? -1 : 1;
                frameIndex += walkReverse ? -1 : 1;

            } else if(currentState == RUN.state) {
                if(runCount >= runningFrameRate) runReverse = true;
                else if(runCount <= 0) runReverse = false;
                runCount += runReverse ? -1 : 1;
                frameIndex += runReverse ? -1 : 1;

            } else {
                frameIndex = dataFrames.get(frameIndex).getNextFrame();
            }
            checkValidFrame();
            setFrameTimer();
        }
    }

    private void setBoundsPerSprite() {
        setBounds(sprites[picIndex].getRegionX(),
                  sprites[picIndex].getRegionY(),
                  sprites[picIndex].getRegionWidth(),
                  sprites[picIndex].getRegionHeight());
    }

    private void setCurrentSprite() {
        val currentDataFrame = dataFrames.get(frameIndex);
        picIndex = currentDataFrame.getPic();
        val currentSprite = sprites[picIndex];
        setX(posX + (currentDataFrame.getCenterX() - currentSprite.getRegionWidth()  +1));
        setY((posY + (currentDataFrame.getCenterY() - currentSprite.getRegionHeight() +1)));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        checkNewFrame();
        setCurrentSprite();
        setBoundsPerSprite();
        batch.draw(sprites[picIndex], getX(), getY());
//        batch.draw(batch, deltaTime);
        frameTimer -= Gdx.graphics.getDeltaTime();
    }

}
