package com.unlogical.colored.terrain.tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.ShaderHandler;

public class TileText extends Tile
{
	private static boolean showText = false;

	public TileText(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
	}

	public static void toggleText()
	{
		showText = !showText;
	}

	@Override
	public void onCreated()
	{
		super.onCreated();

		this.metadata = "Text";
	}

	@Override
	protected void customDraw(Dimension dimension, Batch batch)
	{
		super.customDraw(dimension, batch);

		ShaderHandler.disableLighting();
		
		Fonts.drawFancy(getText(), Fonts.getDefaultFont(), Color.WHITE, position.x, position.y, this.hitbox.getRawWidth(), this.hitbox.getRawHeight(), true, true, batch);
		
		ShaderHandler.enableLighting();
	}

	@Override
	public boolean shouldUpdate()
	{
		return false;
	}

	@Override
	public boolean shouldRenderInGame()
	{
		return false;
	}

	@Override
	protected boolean shouldDrawTextures()
	{
		return false;
	}

	@Override
	public boolean shouldCopy()
	{
		return false;
	}

	@Override
	public boolean isActive()
	{
		return showText;
	}

	public String getText()
	{
		return this.metadata;
	}

	public void setText(String text)
	{
		this.metadata = text;
	}
}
