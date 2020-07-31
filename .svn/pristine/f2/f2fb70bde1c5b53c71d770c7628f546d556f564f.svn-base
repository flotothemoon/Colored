package com.unlogical.colored.terrain.tile.logical;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.entity.key.EntityKey;
import com.unlogical.colored.entity.key.ITileKeyInteractable;
import com.unlogical.colored.entity.key.KeyPort;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.IExtendedSpriteBatch;

public class TileKeyGate extends TileGate implements ITileKeyInteractable
{
	private KeyPort port;

	public TileKeyGate(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);

		if (this.hasMirror())
		{
			((TileKeyGate) this.getMirroredTile()).setKeyPort(this.port);
		}
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
			this.port = KeyPort.YELLOW;
		}
	}

	@Override
	public void writeMetadata()
	{
		this.metadata = this.port.toPort() + "";
	}

	@Override
	public void draw(Batch batch, Dimension dimension)
	{
		ColorHelper.copy(this.port.getColor(), ColorHelper.buffer);
		ColorHelper.buffer.r *= Level.PORT_ADDITIVE_COLOR_FACTOR;
		ColorHelper.buffer.g *= Level.PORT_ADDITIVE_COLOR_FACTOR;
		ColorHelper.buffer.b *= Level.PORT_ADDITIVE_COLOR_FACTOR;

		float previous = ((IExtendedSpriteBatch) batch).getAdditiveColor();
		((IExtendedSpriteBatch) batch).setAdditiveColor(ColorHelper.buffer);

		super.draw(batch, dimension);

		((IExtendedSpriteBatch) batch).setAdditiveColor(previous);
	}

	@Override
	public Color customModifyDrawColor(Color color)
	{
		return ColorHelper.inverseMultiplyRGBAndCopy(color, this.port.getColor(), Level.PORT_INVERSE_COLOR_FACTOR);
	}

	@Override
	public void onInteract(EntityKey key)
	{
		if (this.closed)
		{
			this.level.getSoundManager().play(SoundType.UNLOCK_KEYGATE);

			this.open(false, !key.isShared(), false, !key.isShared());

			if (key.isShared() && this.hasMirror() && this.level.getType() != LevelType.REVERSAL)
			{
				((TileKeyGate) this.mirroredTile).open(false, false, false, !key.isShared());
			}

			key.onUsage(key.getHoldingEntity(), this);
		}
	}

	@Override
	public void setKeyPort(KeyPort port)
	{
		this.port = port;
	}

	@Override
	public boolean renderStatic()
	{
		return false;
	}

	@Override
	public KeyPort getKeyPort()
	{
		return this.port;
	}
}
