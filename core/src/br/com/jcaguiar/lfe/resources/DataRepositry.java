package br.com.jcaguiar.lfe.resources;

import br.com.jcaguiar.lfe.components.objects.GameObject;
import br.com.jcaguiar.lfe.components.objects.structure.DataBmp;
import br.com.jcaguiar.lfe.components.objects.structure.DataFrame;
import br.com.jcaguiar.lfe.components.objects.structure.DataFrameScope;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.val;
import lombok.var;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.jcaguiar.lfe.components.objects.structure.DataFrameScope.*;

/**
 * Reads and store all the sprite-sheets sources, then generates all frames per object's id
 */
public abstract class DataRepositry {

    //All the sprite-sheets sources loaded in the game
    public static final List<Texture> IMAGES_SOURCE = new ArrayList<>();

    //A map when the keys/values are for object's id/frame;
    public static final Map<Integer, GameObject> GAME_OBJS_MAP = new HashMap<>();
    public static final Map<Integer, TextureRegion[]> PARTICLES_MAP = new HashMap<>();

    //Dat Tags
    private static final String BMP_TAG = "<bmp_begin>";
    private static final String BMP_END_TAG = "<bmp_end>";
    private static final String FRAME_TAG = "<frame>";
    private static final String FRAME_END_TAG = "<frame_end>";

    private static void loadSprites(int objId, GameObject gameObj) {
        val spriteNum = gameObj.getBmpSources().stream().mapToInt(bmp -> bmp.row * bmp.col).sum();
        var animationFrames = new TextureRegion[spriteNum];
        int picIndex = 0;
        System.out.println("DataRepositry - spriteNum: " + spriteNum);

        //Loading all bmp sprite references
        System.out.println("DataRepositry - total bmps: " + gameObj.getBmpSources().size());
        for(DataBmp bmp : gameObj.getBmpSources()) {
            System.out.printf("DataRepositry - bmp.path: %s, bmp.w: %d, bmp.h: %d%n", bmp.path, bmp.w, bmp.h);
            val tempImg = new Texture(bmp.path);
            IMAGES_SOURCE.add(tempImg);
            final TextureRegion[][] tempFrames = TextureRegion.split(tempImg, bmp.w +1, bmp.h +1);

            //The next lopp row and col are inverted because the logic used in the .dat file are different
            for (int row = 0; row < bmp.col; row++) {
                for (int col = 0; col < bmp.row; col++) {
                    System.out.printf("DataRepositry - tempFrames[%d][%d]\n", row, col);
                    animationFrames[picIndex++] = tempFrames[row][col];
                }
            }
        }

        //Removing void/null frames
        animationFrames = Stream.of(animationFrames).filter(Objects::nonNull).toArray(TextureRegion[]::new);
        System.out.println("DataRepositry - animationFrames size: " + animationFrames.length);

        //Add to the repository
        GAME_OBJS_MAP.get(objId).setSprites(animationFrames);
    }

    //Read the dat file in order to generate the frames' data for all actions/animations
    public static void loadDatFile(int objId, String path) {
        //Checks the file's path
        val filePath = Paths.get(path);
        if(!Files.exists(filePath) || !Files.isRegularFile(filePath))
            throw new RuntimeException("File not found: " + path);

        //Open and read the file's content
        GameObject gameObject = new GameObject();
        AtomicBoolean bmpContent = new AtomicBoolean(false);
        AtomicInteger frameIndex = new AtomicInteger(-1);
        AtomicReference<DataFrame> frameData = new AtomicReference<>(new DataFrame());
        AtomicReference<DataFrameScope> currentScope = new AtomicReference<>(null);
        try {
            val input = new FileInputStream(filePath.toFile());
            val buffer = new BufferedReader(new InputStreamReader(input));
            buffer.lines().filter(line -> !line.isEmpty()).forEach(line -> {
                System.out.println("Linha alvo: " + line);
                //Check if is inside <bmp> tag for the next loop
                if(frameIndex.get() == -1 && isBmpBegin(line) && !bmpContent.get()) {
                    System.out.println("Found: <bmp> tag");
                    bmpContent.set(true);
                    return;
                }
                //When inside <bmp> section
                if(bmpContent.get() && !isBmpEnd(line)) {
                    System.out.println("Inside: <bmp> tag");
                    gameObject.setBmpContent(line);
                    return;
                }
                //When outside <bmp> section
                bmpContent.set(false);
                //When outside a <frame> section, check if a new <frame> start
                if(frameIndex.get() == -1) {
                    frameIndex.set(getFrameBeginIndex(line));
                    return;
                }
                //Check if this line is a <frame_end> tag to restart values
                System.out.println("Inside: <frame> " + frameIndex.get());
                if(isFrameEnd(line)) {
                    System.out.println("Found: <frame_end> tag");
                    gameObject.getDataFrames().put(frameIndex.get(), frameData.get());
                    frameIndex.set(-1);
                    currentScope.set(null);
                    frameData.set(new DataFrame());
                    return;
                }
                //Here whe are inside a <frame> section, so read keys and values
                var nextFrameScope = getFrameScope(line);
                if(nextFrameScope != null) {
                    System.out.println("Found: scope " + nextFrameScope.flag);
                    if(!nextFrameScope.notEnd) {
                        nextFrameScope = null;
                        currentScope.set(null);
                    }
                }
                if(nextFrameScope == PIC) {
                    val keyValues = getLineKeyValues(line);
                    frameData.get().setAttribute(keyValues, PIC);
                    currentScope.set(null);
                } else if(nextFrameScope == SOUND) {
                    val soundPath = line.split(":")[1].trim(); //TODO: handle
                    frameData.get().setSound(soundPath);
                    currentScope.set(null);
                } else if(currentScope.get() != null) {
                    System.out.println("Inside: scope " + currentScope.get().flag);
                    val keyValues = getLineKeyValues(line);
                    frameData.get().setAttribute(keyValues, currentScope.get());
                } else {
                    currentScope.set(nextFrameScope);
                }
            });
            //Close file/buffer
            input.close();
            buffer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final String dataLoaderString = gameObject.getDataFrames().keySet()
            .stream()
            .map(f -> f + ": " + gameObject.getDataFrames().get(f).toString())
            .collect(Collectors.joining("\n"));
        System.out.println("Load Data File:\n" + dataLoaderString);

        GAME_OBJS_MAP.put(objId, gameObject);
        loadSprites(objId, gameObject);
    }

    private static DataFrameScope getFrameScope(String line) {
       return Arrays.stream(values())
            .filter(k -> line.trim().startsWith(k.flag))
            .findFirst()
            .orElse(null);
    }

    private static boolean isBmpBegin(String line) {
        return line.trim().startsWith(BMP_TAG);
    }

    private static boolean isBmpEnd(String line) {
        return line.trim().startsWith(BMP_END_TAG);
    }

    private static boolean isFrameBegin(String line) {
        return line.trim().startsWith(FRAME_TAG);
    }

    private static int getFrameBeginIndex(String line) {
        if(line.trim().startsWith(FRAME_TAG)) {
            val frameId= line.replace(FRAME_TAG, "").trim().split(" ")[0];
            return Integer.parseInt(frameId);
        }
        return -1;
    }

    private static boolean isFrameEnd(String line) {
        return line.trim().startsWith(FRAME_END_TAG);
    }

    private static Map<String, Integer> getLineKeyValues(String line) {
        val map = new HashMap<String, Integer>();
        val keyValues = Stream.of(line.trim().replace(":", " ").split(" "))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toList());
        for(int i = 0; i+1 < keyValues.size(); i += 2) {
            if(keyValues.get(i).matches("\\d+")) {
                --i;
                continue;
            }
            try {
                val value = Integer.parseInt(keyValues.get(i+1));
                map.put(keyValues.get(i).trim(), value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }
}
