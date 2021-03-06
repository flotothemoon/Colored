package com.unlogical.colored.terrain.tile.logical;

import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.properties.ITileIntersectionNotifier;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.LevelObject;
import com.unlogical.colored.util.ScheduledEvent;

public abstract class TileGate extends Tile implements ITileIntersectionNotifier
{
	private static String OPEN = "open";
	private static String CLOSE = "close";

	protected boolean closed = true;
	protected boolean inverted;
	private boolean tryClose;
	private boolean forceClose;
	private boolean recordClose;
	
	protected boolean containsEntity;

	protected TileGate(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);

		this.closed = this.innerType != 2;
		this.properties.setBlocked(closed);
		this.inverted = !this.closed;
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		TileGate copy = (TileGate) super.deepCopy(copiedReferences);

		copy.closed = this.closed;
		copy.inverted = this.inverted;
		copy.tryClose = this.tryClose;
		copy.forceClose = this.forceClose;
		copy.recordClose = this.recordClose;
		copy.containsEntity = this.containsEntity;

		return copy;
	}

	@Override
	public void initMirror(Tile mirroredTile, LevelType levelType)
	{
		super.initMirror(mirroredTile, levelType);

		TileGate mirror = (TileGate) mirroredTile;

		if (levelType == LevelType.REVERSAL)
		{
			if (this.closed)
			{
				mirror.closed = false;
				mirror.properties.setBlocked(false);
				mirror.setInnerType(2);
			}
			else
			{
				mirror.closed = true;
				mirror.properties.setBlocked(true);
				mirror.setInnerType(0);
			}
		}
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{
		if (tryClose && close(forceClose, recordClose))
		{
			tryClose = false;
		}
		
		if (level.getType() == LevelType.FROZEN && transitioning)
		{
			if (this.lastImage <= 5 && !this.isBlocked())
			{				
				this.properties.setBlocked(true);
			}
		}

		containsEntity = false;
	}

 	@Override
	public void onEntityIntersection(Entity entity)
	{
		containsEntity = true;
	}   

	public void open(boolean forceOpen, boolean syncMirror, boolean inputBased, boolean shouldRecord)
	{
		if ((closed && !transitioning) || forceOpen)
		{
			if (shouldRecord() && shouldRecord)
			{
				scheduleMirroredEvent(new ScheduledEvent(OPEN, inputBased));
			}

			this.transitionAnimation(1, 2, false, !forceOpen);
			closed = false;

			if (level.getType() != LevelType.FROZEN)
			{
				properties.setBlocked(false);
			}

			if (level.getLevelType() == LevelType.REVERSAL && this.hasMirror() && syncMirror)
			{
				((TileGate) this.mirroredTile).close(true, shouldRecord);
			}
		}
	}

	protected boolean close(boolean forceClose, boolean shouldRecord)
	{
		if ((!closed && !transitioning) || forceClose)
		{			
			if (containsEntity)
			{
				this.tryClose = true;
				this.forceClose = forceClose;
				this.recordClose = shouldRecord;
			}
			else
			{
				closed = true;

				if (level.getType() != LevelType.FROZEN)
				{					
					properties.setBlocked(true);
				}

				transitionAnimation(1, 0, true, !forceClose);

				if (shouldRecord() && shouldRecord)
				{
					scheduleMirroredEvent(new ScheduledEvent(CLOSE));
				}

				return true;
			}
		}

		return false;
	}

	@Override
	protected void onTransitionFinished(int transitionInnerType, int finalInnerType)
	{
		super.onTransitionFinished(transitionInnerType, finalInnerType);
		
		if (level.getType() == LevelType.FROZEN)
		{
			properties.setBlocked(finalInnerType == 0);
		}
	}

	@Override
	protected void onScheduledEvent(ScheduledEvent event)
	{
		if (event.type == OPEN)
		{
			open(true, true, false, false);
		}
		else if (event.type == CLOSE)
		{
			close(true, false);
		}
	}

	@Override
	public void onReSimulation()
	{
		super.onReSimulation();

		if (!this.shared)
		{
			this.setInnerType(this.mirroredTile.getInnerType());
			this.currentImage = ((TileGate) this.mirroredTile).currentImage;
			this.transitioning = ((TileGate) this.mirroredTile).transitioning;
			this.reverseAnimation = ((TileGate) this.mirroredTile).reverseAnimation;
			this.finalInnerType = ((TileGate) this.mirroredTile).finalInnerType;
			this.closed = ((TileGate) this.mirroredTile).closed;
			this.properties.setBlocked(((TileGate) this.mirroredTile).properties.isBlocked());
		}
	}

	@Override
	protected TextureRegion getParticleImageFor(EmitterType type)
	{
		return this.availableImages[dimension.code()][0];
	}

	@Override
	public boolean shouldUpdate()
	{
		return true;
	}

	public boolean isReallyChangeable()
	{
		return true;
	}

	public boolean renderStatic()
	{
		return false;
	}
}
