package br.com.jcaguiar;

import br.com.jcaguiar.lfe.DataRepositry;
import br.com.jcaguiar.lfe.GameCharacter;
import br.com.jcaguiar.lfe.StageTest;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class App extends ApplicationAdapter {
	SpriteBatch batch;
	float elapsedTime;
    Stage stage;
    OrthographicCamera camera;


	@Override
	public void create () {
		batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(true);
		camera.update();
        stage = new StageTest(new ScreenViewport(camera), 0f, 500f, 0f, 200f);

		DataRepositry.loadDatFile(1, "C:\\joao.aguiar\\Workspace\\AGUIAR\\LFE\\LFE GDX\\assets\\Davis.dat");
        GameCharacter davis = new GameCharacter(DataRepositry.GAME_OBJS_MAP.get(1));
		davis.startVSMode(500, 255, 0, 300, true, 0, 0);

		stage.addActor(davis);
		Gdx.input.setInputProcessor(stage);
		stage.setKeyboardFocus(davis);
	}
	
	@Override
	public void render () {
		//elapsedTime += Gdx.graphics.getDeltaTime();

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		ScreenUtils.clear(0.2f, 0, 0.6f, 1);

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
