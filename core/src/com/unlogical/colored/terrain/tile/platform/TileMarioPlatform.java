package com.unlogical.colored.terrain.tile.platform;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.terrain.tile.properties.ITileBlockedFromAbove;
import com.unlogical.colored.util.Dimension;

public class TileMarioPlatform extends TilePlatform implements ITileBlockedFromAbove
{
	public TileMarioPlatform(TileType type, String metadata, int innerType, float rotation, Vector2 position,
			Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
	}
}
