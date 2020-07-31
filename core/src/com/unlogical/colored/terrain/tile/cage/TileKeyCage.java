package com.unlogical.colored.terrain.tile.cage;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.entity.key.EntityKey;
import com.unlogical.colored.entity.key.ITileKeyInteractable;
import com.unlogical.colored.entity.key.KeyPort;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.util.Dimension;

public class TileKeyCage extends TileCage implements ITileKeyInteractable
{
	private KeyPort port;

	public TileKeyCage(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);

		if (level.shouldCreateMirrors() && this.dimension == Dimension.COLORED)
		{
			((TileKeyCage) this.getMirroredTile()).setKeyPort(this.port);
		}
	}

	@Override
	public void onInteract(EntityKey key)
	{
		this.clear();
		
		key.onUsage(key.getHoldingEntity(), this);
	}

	@Override
	public KeyPort getKeyPort()
	{
		return port;
	}

	@Override
	public void setKeyPort(KeyPort port)
	{
		this.port = port;
	}

	@Override
	public void readMetadata()
	{
		try
		{
			this.port = KeyPort.getTrigger(Integer.parseInt(this.metadata));
		}
		catch (Exception e)
		{
			this.port = KeyPort.RED;
		}
	}

	@Override
	public void writeMetadata()
	{
		this.metadata = port.toPort() + "";
	}
}