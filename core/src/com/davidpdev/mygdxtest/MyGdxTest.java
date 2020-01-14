package com.davidpdev.mygdxtest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.EmojiSupport;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class MyGdxTest extends ApplicationAdapter {
	Skin skin;
	Stage stage;

	EmojiSupport emojiSupport;

	// For free type test
	PixmapPacker packer;
	BitmapFont bitmapFontFreeType;
	public static final int FONT_ATLAS_WIDTH = 1024;
	public static final int FONT_ATLAS_HEIGHT = 512;
	public static final String FREETYPE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz\n1234567890"
			+ "\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*";

	@Override
	public void create () {
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		CreateFreeTypeFont(40);

		emojiSupport= new EmojiSupport();
		emojiSupport.Load(Gdx.files.internal("emojis25.atlas"));
		emojiSupport.AddEmojisToFont(skin.getFont("default-font"));
		emojiSupport.AddEmojisToFont(bitmapFontFreeType);

		String str1= "Scaled text 2x\ud83d\ude0e\n\ud83d\ude00\ud83d\ude00.";
		String str2= "Scaled text 0.75x\ud83d\ude0e\n\ud83d\ude00\ud83d\ude00.";
		String str3= "FreeType 40 size\ud83d\ude0e (\ud83d\ude00\ud83d\ude02).\uD83D\uDC99";

		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		Label label1 = new Label(emojiSupport.FilterEmojis(str1), skin);
		label1.setFontScale(2);

		Label label2 = new Label(emojiSupport.FilterEmojis(str2), skin);
		label2.setWrap(true);
		label2.setFontScale(0.75f, 0.75f);

		Label label3 = new Label(emojiSupport.FilterEmojis(str3), skin);
		Label.LabelStyle ls =new Label.LabelStyle(label3.getStyle());
		ls.font= bitmapFontFreeType;
		label3.setStyle(ls);

		// Show all emojis as pure text
		StringBuilder sb= new StringBuilder();
		for (int i=0; i<emojiSupport.regions.size(); i++) {
			sb.append((char)(EmojiSupport.START_CHAR + i));
		}
		Label label4 = new Label(sb.toString(), skin);
		label4.setWrap(true);
		label4.setFontScale(0.75f, 0.75f);

		// Direct access to emoji
		Image im2= new Image (emojiSupport.textureAtlas.findRegion("1f60e"));
		im2.setSize(128,128);;

		Table table = new Table();
		stage.addActor(table);
		table.setPosition(20,20);
		table.debug();
		table.add(label1).row();
		table.add(label2).fill().row();
		table.add(label3).row();
		table.add(label4).fill().row();
		table.add(im2).row();
		table.pack();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}

	private void CreateFreeTypeFont (int size)
	{
		packer = new PixmapPacker(FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, Pixmap.Format.RGBA8888, 2, false);
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		fontParameter.size = size;
		fontParameter.packer = packer;
		fontParameter.characters = FREETYPE_CHARACTERS;
		bitmapFontFreeType = gen.generateFont(fontParameter);
		gen.dispose();
	}

}
