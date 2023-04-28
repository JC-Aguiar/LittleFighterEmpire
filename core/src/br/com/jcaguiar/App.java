package br.com.jcaguiar;

import br.com.jcaguiar.lfe.components.objects.GamePlayer;
import br.com.jcaguiar.lfe.resources.DataRepositry;
import br.com.jcaguiar.lfe.components.objects.GameChar;
import br.com.jcaguiar.lfe.components.scene.DefaultStage;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.util.Arrays;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class App extends ApplicationAdapter {

	SpriteBatch batch;
	float elapsedTime;
	DefaultStage stage;
    OrthographicCamera camera;
	Group gameObjs = new Group();
	Group debug = new Group();
	@Getter static GamePlayer mainPlayer;
    public static BitmapFont debugFont;
	public final static boolean DEBUG = true;


	@Override
	public void create() {
        //Creating the core-basics: batch, camera and stage
		batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(true);
		camera.update();
        stage = new DefaultStage(new ScreenViewport(camera), 0f, 1800f, 200f, 200f);

        //Loading data-file
		DataRepositry.loadDatFile(1, "C:\\joao.aguiar\\Workspace\\AGUIAR\\LFE\\LFE GDX\\assets\\Davis.dat");
		//TODO: replace fixed string Path to File.separator()

        //Adding chars
		mainPlayer = new GamePlayer(DataRepositry.GAME_OBJS_MAP.get(1));
        GameChar davisComp = new GameChar(DataRepositry.GAME_OBJS_MAP.get(1));
		gameObjs.addActor(mainPlayer);
		gameObjs.addActor(davisComp);
        stage.addActor(gameObjs);
		Gdx.input.setInputProcessor(stage);
		stage.setKeyboardFocus(mainPlayer);

        //Setting game-mode
		mainPlayer.startVSMode(500, 255, 0, 0,  0, 50);
		davisComp.startVSMode(500, 255, 0, 0, 100, 250);

        //Creating fonts for debug-info (TrueTypeFont)
        val generator = new FreeTypeFontGenerator(Gdx.files.internal("Carlito-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.color = Color.WHITE;
        parameter.flip = true;
        parameter.shadowColor = Color.BLACK;
        parameter.shadowOffsetX = 3;
        parameter.shadowOffsetY = 3;
        debugFont = generator.generateFont(parameter);
        generator.dispose();
	}
	
	@Override
	public void render() {
		ScreenUtils.clear(0, 0, 0, 1);
		batch.begin();
		stage.act();
		stage.draw();
		batch.end();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		DataRepositry.IMAGES_SOURCE.forEach(Texture::dispose);
	}
}
