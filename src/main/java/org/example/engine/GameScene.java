package org.example.engine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class GameScene {

    GameCamera camera;
    List<GameObject> objects = new ArrayList<>();
    boolean isRunning = false;

    public void init() {

    }

    public void start() {
        objects.forEach(GameObject::start);
    }

    public void addObjectToScene(GameObject obj) {
        objects.add(obj);
        if(isRunning) obj.start();
    }

    public abstract void update(float deltaTime);

}
