package br.com.jcaguiar.lfe;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.var;

import java.util.*;

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class DataGameObj {

    int objectId;
    String name;
    String head;
    final List<DataBmp> bmpSources = new ArrayList<>();
    final Map<Integer, DataFrame> dataFrames = new HashMap<>();
    TextureRegion[] sprites;

    public void setBmpContent(String line) {
        var keyValue =  Arrays.stream(line.replace(":", "").split(" "))
            .filter(l -> !l.equals("w") && !l.equals("h") && !l.equals("col") && !l.equals("row"))
            .filter(l -> !l.isEmpty())
            .map(String::trim)
            .toArray(String[]::new);

        int size = 2;
        if(line.startsWith("file")) {
            size = 6;
            if(!keyValue[0].matches("file\\(\\d+-\\d+\\)"))
                throw new RuntimeException("Load Data Fail: invalid value inside <bmp_begin> tag: " + line);
        }
        if(keyValue.length != size)
            throw new RuntimeException("Load Data Fail: invalid value inside <bmp_begin> tag: " + line);

        if(keyValue[0].startsWith("name"))
            name = keyValue[1];
        else if(keyValue[0].startsWith("head"))
            head = keyValue[1];
        else if(keyValue[0].startsWith("file")) {
            try {
                bmpSources.add(new DataBmp(
                    keyValue[0], keyValue[1], keyValue[2], keyValue[3], keyValue[4], keyValue[5]));
            } catch (Exception e) {
                throw new RuntimeException("Load Data File: Invalid value inside <bmp_begin> tag: " + line);
            }
        }
    }

}
