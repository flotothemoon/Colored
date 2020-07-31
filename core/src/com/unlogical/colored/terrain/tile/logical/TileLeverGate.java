package com.unlogical.colored.terrain.tile.logical;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.properties.ITileReceiver;
import com.unlogical.colored.terrain.tile.properties.ITileSender;
import com.unlogical.colored.util.Dimension;

public class TileLeverGate extends TileGate implements ITileReceiver
{
	public TileLeverGate(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
	}

	@Override
	public void onTrigger(ITileSender sender, boolean hasInversedMirror)
	{
		if (!inverted)
		{
			open(transitioning, true, false, sender != null && !((Tile) sender).isShared());
		}
		else
		{
			close(transitioning, sender != null && !((Tile) sender).isShared());
		}
	}

	@Override
	public void onUntrigger(ITileSender changedTile, boolean hasInversedMirror)
	{
		if (!inverted)
		{
			close(transitioning, changedTile != null && !((Tile) changedTile).isShared());
		}
		else
		{
			open(transitioning, true, false, changedTile != null && !((Tile) changedTile).isShared());
		}
	}

	@Override
	public void readMetadata()
	{
		try
		{
			this.setPort(Integer.parseInt(metadata.trim()));
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public void writeMetadata()
	{
		this.metadata = this.getPort() + "";
	}

}
