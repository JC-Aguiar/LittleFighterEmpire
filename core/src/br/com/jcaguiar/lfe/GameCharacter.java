package br.com.jcaguiar.lfe;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameCharacter extends DataGameObj {

    //CORE
    int characterId;
    int team;
    boolean isHuman;
    float displayX, displayY;

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
    boolean right;
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
    }

    //VS MODE
    public void startVSMode(int hp, int mp, int team, int frame, boolean isHuman, int posX, int posY) {
        this.hpMax = this.hpNow = this.hpLimit = hp;
        this.mpMax = this.mpNow = this.mpLimit = mp;
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

    private void setNewFrame() {
        if(frameTimer <= 0) {
            frameIndex = dataFrames.get(frameIndex).getNextFrame();
            checkValidFrame();
            setFrameTimer();
        }
    }

    public void draw(float deltaTime, SpriteBatch batch) {
        setNewFrame();
        val currentDataFrame = dataFrames.get(frameIndex);
        picIndex = currentDataFrame.getPic();
        val currentSprite = sprites[picIndex];
        displayX = posX + (currentSprite.getRegionWidth() - currentDataFrame.getCenterX() +1);
        displayY = posY + (currentSprite.getRegionHeight() - currentDataFrame.getCenterY() +1);
        batch.draw(sprites[picIndex], displayX, displayY * -1);
        frameTimer -= deltaTime;
    }


}
