package br.com.jcaguiar.lfe.components.objects;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class GamePlayer extends GameChar {

    public GamePlayer(GameObject dataObj) {
        super(dataObj);
        this.isHuman = true;
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

}
