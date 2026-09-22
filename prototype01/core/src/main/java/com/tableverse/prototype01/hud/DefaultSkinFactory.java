package com.tableverse.prototype01.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.graphics.g2d.NinePatch;

/** Builds a minimal Scene2D Skin from generated primitives, so this prototype needs no packaged UI asset files. */
public final class DefaultSkinFactory {

    private DefaultSkinFactory() {
    }

    public static Skin create() {
        Skin skin = new Skin();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture whiteTexture = new Texture(pixmap);
        pixmap.dispose();
        skin.add("white", whiteTexture);
        TextureRegion whiteRegion = new TextureRegion(whiteTexture);

        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        NinePatch upPatch = new NinePatch(whiteRegion);
        upPatch.setColor(Color.DARK_GRAY);
        NinePatch downPatch = new NinePatch(whiteRegion);
        downPatch.setColor(Color.GRAY);
        NinePatchDrawable up = new NinePatchDrawable(upPatch);
        NinePatchDrawable down = new NinePatchDrawable(downPatch);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = up;
        buttonStyle.down = down;
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        return skin;
    }
}
