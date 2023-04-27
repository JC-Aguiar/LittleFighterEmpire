package br.com.jcaguiar;

import br.com.jcaguiar.lfe.components.objects.GameObject;
import br.com.jcaguiar.lfe.components.objects.GamePlayer;
import br.com.jcaguiar.lfe.resources.DataRepositry;
import br.com.jcaguiar.lfe.components.objects.GameChar;
import br.com.jcaguiar.lfe.components.sceens.DefaultStage;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.util.Arrays;
import java.util.Comparator;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class App extends ApplicationAdapter {
	SpriteBatch batch;
	float elapsedTime;
	DefaultStage stage;
    OrthographicCamera camera;
	Group gameObjs = new Group();
	Group ais = new Group();

	final static boolean DEBUG = true;


	@Override
	public void create () {
		batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(true);
		camera.update();
        stage = new DefaultStage(new ScreenViewport(camera), 0f, 1800f, 200f, 200f);

		DataRepositry.loadDatFile(1, "C:\\joao.aguiar\\Workspace\\AGUIAR\\LFE\\LFE GDX\\assets\\Davis.dat");
        GamePlayer davis = new GamePlayer(DataRepositry.GAME_OBJS_MAP.get(1));
        GameChar davisComp = new GameChar(DataRepositry.GAME_OBJS_MAP.get(1));
		gameObjs.addActor(davis);
		gameObjs.addActor(davisComp);
//		ais.addActor(davisComp);

        stage.addActor(gameObjs);
//        stage.addActor(ais);
		Gdx.input.setInputProcessor(stage);
		stage.setKeyboardFocus(davis);

		davis.startVSMode(500, 255, 0, 0,  0, 50);
		davis.debugNumbers = true;
		davisComp.startVSMode(500, 255, 0, 0, 100, 250);
	}
	
	@Override
	public void render () {
		//elapsedTime += Gdx.graphics.getDeltaTime();

		camera.position.x = Arrays.stream(gameObjs.getChildren().toArray())
			.filter(actor -> actor instanceof GameChar)
			.map(actor -> (GameChar) actor)
			.filter(GameChar::isHuman)
			.mapToInt(a -> (int)a.getX())
			.findFirst()
			.orElse((int) camera.position.x);
//			.sum();
		if(camera.position.x < stage.boundX + Gdx.graphics.getWidth()/2)
			camera.position.x = stage.boundX + Gdx.graphics.getWidth()/2;
		else if(camera.position.x > stage.boundW - Gdx.graphics.getWidth()/2)
			camera.position.x = stage.boundW - Gdx.graphics.getWidth()/2;
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		ScreenUtils.clear(0, 0, 0, 1);

		batch.begin();
		stage.act();
		stage.draw();
//		davis.draw(Gdx.graphics.getDeltaTime(), batch);
//		batch.draw(animationFrames[picIndex], posX + alingX, posY + alingY);
//		batch.draw(animation.getKeyFrame(elapsedTime, true), 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		DataRepositry.IMAGES_SOURCE.forEach(Texture::dispose);
	}
}
