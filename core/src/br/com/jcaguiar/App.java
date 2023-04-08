package br.com.jcaguiar;

import br.com.jcaguiar.lfe.DataRepositry;
import br.com.jcaguiar.lfe.GameCharacter;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class App extends ApplicationAdapter {
	SpriteBatch batch;
	float elapsedTime;
	GameCharacter davis;


	@Override
	public void create () {
		batch = new SpriteBatch();
		DataRepositry.loadDatFile(1, "C:\\joao.aguiar\\Workspace\\AGUIAR\\LFE\\LFE GDX\\assets\\Davis.dat");
		davis = new GameCharacter(DataRepositry.GAME_OBJS_MAP.get(1));
		davis.startVSMode(500, 255, 0, 300, true, 100, -50);
	}
	
	@Override
	public void render () {
		//elapsedTime += Gdx.graphics.getDeltaTime();
		ScreenUtils.clear(0.2f, 0, 0.6f, 1);
		batch.begin();
		davis.draw(Gdx.graphics.getDeltaTime(), batch);
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
