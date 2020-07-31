package com.unlogical.colored.terrain.tile;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.terrain.tile.properties.ITileMirrorable;
import com.unlogical.colored.util.Dimension;

public class TileSpike extends Tile implements ITileMirrorable
{
	public TileSpike(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
	}

	@Override
	public boolean shouldUpdate()
	{
		return false;
	}
	
	@Override
	public boolean isRotationLimited()
	{
		return false;
	}
	
	@Override
	public boolean renderStatic()
	{
		return true;
	}
	
	@Override
	public boolean shouldCopy()
	{
		return false;
	}

	@Override
	public boolean isReallyChangeable()
	{
		return false;
	}
}
