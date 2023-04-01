package org.example.engine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class GameComponent {

    GameObject gameObject;

    public abstract void update(float deltaTime);

    public void start() {
    }
}
