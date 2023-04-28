package br.com.jcaguiar.lfe.resources;

import br.com.jcaguiar.lfe.components.objects.GameObject;
import br.com.jcaguiar.lfe.components.objects.structure.SpaceBody;
import br.com.jcaguiar.lfe.components.objects.structure.SpaceInteraction;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CombatSystem {

    public static final Map<GameObject, List<SpaceBody>> BODIES = new HashMap<>();
    public static final Map<GameObject, List<SpaceInteraction>> ACTIONS = new HashMap<>();
    //TODO: each GameObject should fill the stage space (x, y, w, h, z1, z2) whit they id/ref

    public static void checkCollision() {
//        val hits = new HashMap<GameObject, GameObject>();
        BODIES.keySet().forEach(obj1 -> {
            BODIES.get(obj1).forEach(body -> {
                ACTIONS.keySet().forEach(obj2 -> {
                    ACTIONS.get(obj2).forEach(itr -> {
                        if(body.collision(itr) && (obj1.getTeam() == 0 || obj1.getTeam() != obj2.getTeam())) {
                            //obj1 = hited
                            //obj2 = attacker
                            obj1.setHitLag(20);
                            obj2.setHitLag(20);
                        }
                    });
                });
            });
        });
    }

}
