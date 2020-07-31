package com.unlogical.colored.terrain.tile.logical;

import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.gui.HintProperties;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.levelmanaging.MapManager;
import com.unlogical.colored.resources.image.Images;
import com.unlogical.colored.resources.image.Images.HintIdentifier;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.properties.ITileInteractable;
import com.unlogical.colored.terrain.tile.properties.ITileMirrorable;
import com.unlogical.colored.terrain.tile.properties.ITileSender;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.LevelObject;
import com.unlogical.colored.util.ScheduledEvent;

public class TileLever extends Tile implements ITileInteractable, ITileMirrorable, ITileSender
{
	private static final String ACTIVATE = "activate";
	private static final String DEACTIVATE = "deactivate";

	private static final int MAX_INTERACTION_DISTANCE = 20;
	private int baseType;
	private boolean oneTimeUsage;

	public TileLever(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);

		this.baseType = this.innerType - this.innerType % 2;

		if (this.innerType % 2 == 1)
		{
			this.startSending();
		}
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		TileLever copy = (TileLever) super.deepCopy(copiedReferences);

		copy.baseType = this.baseType;

		return copy;
	}

	@Override
	public void initMirror(Tile mirroredTile, LevelType levelType)
	{
		super.initMirror(mirroredTile, levelType);

		if (levelType == LevelType.REVERSAL)
		{
			this.updateMirror(0.0f);
		}
	}

	protected void updateMirror(float delta)
	{
		if (this.level.getLevelType() == LevelType.REVERSAL && this.innerType % 2 == this.mirroredTile.getInnerType() % 2)
		{
			if (this.innerType % 2 == 0)
			{
				((TileLever) this.mirroredTile).activate(true);
			}
			else
			{
				((TileLever) this.mirroredTile).deactivate(true);
			}
		}
	}

	@Override
	public void onInteract(EntityPlayer player)
	{
		this.level.getSoundManager().play(SoundType.TOGGLE_LEVER, false);

		if (this.getInnerType() % 2 == 0)
		{
			this.activate(true);

			if (this.hasMirror() && this.level.getLevelType() == LevelType.REVERSAL)
			{
				((TileLever) this.mirroredTile).deactivate(true);
			}
		}
		else if (!this.oneTimeUsage)
		{
			this.deactivate(true);

			if (this.hasMirror() && this.level.getLevelType() == LevelType.REVERSAL)
			{
				((TileLever) this.mirroredTile).activate(true);
			}
		}

		if (this.level.getType() == LevelType.TIME_TRAVEL && this.dimension == Dimension.MONOCHROME)
		{
			if (this.hasMirror())
			{
				if (this.isTriggering())
				{
					this.clearEvent(DEACTIVATE);
					((TileLever) this.mirroredTile).activate(false);
				}
				else
				{
					this.clearEvent(ACTIVATE);
					((TileLever) this.mirroredTile).deactivate(false);
				}
			}

			MapManager.triggerAndSimulateAll(this, this.sendPort, this.isTriggering());
		}

		this.level.getStoryHandler().onLeverPulled(this, player, true);
	}

	@Override
	public void onInnerTypeChanged()
	{
		if (this.innerType % 2 == 1)
		{
			this.startSending();
		}
		else
		{
			this.stopSending();
		}
	}

	void activate(boolean recordEvent)
	{
		this.setInnerType(this.baseType + (this.oneTimeUsage ? 2 : 1));
		this.startSending();

		if (recordEvent && this.shouldRecord())
		{
			this.scheduleMirroredEvent(new ScheduledEvent(ACTIVATE, true));
		}
	}

	void deactivate(boolean recordEvent)
	{
		this.setInnerType(this.baseType);
		this.stopSending();

		if (recordEvent && this.shouldRecord())
		{
			this.scheduleMirroredEvent(new ScheduledEvent(DEACTIVATE, true));
		}
	}

	@Override
	protected void onScheduledEvent(ScheduledEvent event)
	{
		if (event.type == ACTIVATE)
		{
			this.activate(true);
		}
		else if (event.type == DEACTIVATE)
		{
			this.deactivate(true);
		}
	}

	@Override
	public void onPortStateChanged(boolean state)
	{
		this.sending = state;

		if (this.level.getLevelType() == LevelType.REVERSAL && this.hasMirror() && this.dimension == Dimension.COLORED)
		{
			((TileLever) this.mirroredTile).sending = !this.sending;
		}

		if (state)
		{
			this.setInnerType(this.baseType + 1);
		}
		else
		{
			this.setInnerType(this.baseType);
		}
	}

	@Override
	public void readMetadata()
	{
		try
		{
			this.setPort(Integer.parseInt(this.metadata.trim()));
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

	@Override
	public boolean shouldUpdate()
	{
		return true;
	}

	@Override
	public void setTimeEffected(boolean timeEffected)
	{
		this.timeEffected = timeEffected;
		this.oneTimeUsage = !this.timeEffected;
	}

	@Override
	public boolean canInteract(EntityPlayer interactor)
	{
		return interactor.inSameDimension(this) && this.level.getStoryHandler().canLeverBePulled(this, interactor) && (!this.isTriggering() || !this.oneTimeUsage) && interactor.isOnGround() && (this.hitbox.contains(interactor.getHitbox().getCenterX(), interactor.getHitbox().getCenterY()) && this.hitbox.getMinX() - interactor.getHitbox().getMinX() < MAX_INTERACTION_DISTANCE && interactor.getHitbox().getMaxX() - this.hitbox.getMaxX() < MAX_INTERACTION_DISTANCE && this.hitbox.contains(interactor.getHitbox()) || this.hitbox.intersects(interactor.getHitbox()));
	}

	@Override
	public boolean renderStatic()
	{
		return false;
	}

	@Override
	public boolean isReallyChangeable()
	{
		return true;
	}

	@Override
	public HintIdentifier getHintIdentifier()
	{
		return this.innerType % 2 == 0 ? Images.HintIdentifier.LEVER_HINT_INACTIVE : Images.HintIdentifier.LEVER_HINT_ACTIVE;
	}

	@Override
	public HintProperties getHintProperties()
	{
		return HintProperties.DEFAULT_PROPERTIES;
	}
}
