package com.unlogical.colored.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.level.LevelType;

public interface IShareable<T extends GameObject & IShareable<T>>
{
	public Hitbox getHitbox();

	public void onSharingChanged();

	public boolean shouldCreateMirror(boolean allowMirrors);

	public void createMirror(LevelType type);

	public void initMirror(T mirror, LevelType type);

	public void removeMirror();

	public void drawImageOffset(TextureRegion image, Dimension dimension, Color color, float xOffset, float yOffset, Batch batch);

	public Color customModifyDrawColor(Color color);

	public TextureRegion getImage();
}
