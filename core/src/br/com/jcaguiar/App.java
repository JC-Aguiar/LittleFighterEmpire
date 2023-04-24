package br.com.jcaguiar;

import br.com.jcaguiar.lfe.DataRepositry;
import br.com.jcaguiar.lfe.GameCharacter;
import br.com.jcaguiar.lfe.DefaultStage;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class App extends ApplicationAdapter {
	SpriteBatch batch;
	float elapsedTime;
    Stage stage;
    OrthographicCamera camera;
	Group players = new Group();
	Group ais = new Group();


	@Override
	public void create () {
		batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(true);
		camera.update();
        stage = new DefaultStage(new ScreenViewport(camera), 0f, 1800f, 200f, 200f);

		DataRepositry.loadDatFile(1, "C:\\joao.aguiar\\Workspace\\AGUIAR\\LFE\\LFE GDX\\assets\\Davis.dat");
        GameCharacter davis = new GameCharacter(DataRepositry.GAME_OBJS_MAP.get(1));
		players.addActor(davis);

        stage.addActor(players);
        stage.addActor(ais);
		Gdx.input.setInputProcessor(stage);
		stage.setKeyboardFocus(davis);

		davis.startVSMode(500, 255, 0, 0, true, 0, 50);
	}
	
	@Override
	public void render () {
		//elapsedTime += Gdx.graphics.getDeltaTime();

		camera.position.x = Arrays.stream(players.getChildren().toArray())
			.mapToInt(a -> (int)a.getX()/players.getChildren().size)
			.sum();
		if(camera.position.x < ((DefaultStage) stage).boundX + Gdx.graphics.getWidth()/2)
			camera.position.x = ((DefaultStage) stage).boundX + Gdx.graphics.getWidth()/2;
		else if(camera.position.x > ((DefaultStage) stage).boundW - Gdx.graphics.getWidth()/2)
			camera.position.x = ((DefaultStage) stage).boundW - Gdx.graphics.getWidth()/2;
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
