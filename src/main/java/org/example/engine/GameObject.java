package org.example.engine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameObject {

    String name;
    List<GameComponent> components = new ArrayList<>();

    public GameObject(String name) {
        this.name = name;
    }

    public Optional<? extends GameComponent> getComponents(Class<? extends GameComponent> componentClass) {
        return components.stream()
            .filter(c -> componentClass.isAssignableFrom(c.getClass())) //TODO: remove?
            .map(componentClass::cast)
            .findFirst();
    }

    public boolean addComponent(Class<? extends GameComponent> componentClass) {
        val targetComp =  components.stream()
            .filter(c -> componentClass.isAssignableFrom(c.getClass())) //TODO: remove?
            .map(componentClass::cast)
            .findFirst();
        targetComp.ifPresent(components::add);
        return targetComp.isPresent();
    }

    public boolean removeComponent(GameComponent component) {
        val result = components.add(component);
        if(result) component.setGameObject(this);
        return result;
    }

    public void start() {
        components.forEach(GameComponent::start);
    }

    public void update(float deltaTime) {
        components.forEach(comp -> comp.update(deltaTime));
    }


}
