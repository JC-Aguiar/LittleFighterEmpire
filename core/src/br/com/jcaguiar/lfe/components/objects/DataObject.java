package br.com.jcaguiar.lfe.components.objects;

import br.com.jcaguiar.lfe.components.objects.structure.DataBmp;
import br.com.jcaguiar.lfe.components.objects.structure.DataFrame;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class DataObject extends Actor {

    int objectId; //TODO: set this!
    String name;
    String head;
    final List<DataBmp> bmpSources = new ArrayList<>();
    final Map<Integer, DataFrame> dataFrames = new HashMap<>();
    @Setter TextureRegion[] sprites;

}
