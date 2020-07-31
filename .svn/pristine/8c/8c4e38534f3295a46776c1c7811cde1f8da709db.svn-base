package com.unlogical.colored.story;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.terrain.TriggerPort;
import com.unlogical.colored.terrain.tile.logical.TileLever;
import com.unlogical.colored.util.Dimension;

public class BeginningStoryHandler extends CustomStoryHandler
{
	private boolean machineStopped;

	public BeginningStoryHandler(Level level)
	{
		super(level);
	}

	@Override
	public CustomStoryHandler deepCopy(Map<Object, Object> copiedReferences)
	{
		BeginningStoryHandler copy = new BeginningStoryHandler((Level) Level.getCopy(this.level, copiedReferences));

		return copy;
	}

	@Override
	public void update(int delta)
	{
		super.update(delta);
	}

	@Override
	public void onPreLevelInit()
	{
		super.onPreLevelInit();

		level.backgroundAlpha = level.foregroundAlpha;
		level.getDimensionColor(Dimension.MONOCHROME).a = level.getDimensionColor(Dimension.COLORED).a;
		level.setBackgroundFactor(level.getForegroundFactor());
	}

	@Override
	public void onLevelInit()
	{
		LevelManager.toggleDimension(false);
	}

	@Override
	public void onLeverPulled(TileLever lever, EntityPlayer interactor, boolean realInput)
	{
		if (TriggerPort.getColor(lever.getPort()) == Color.BLUE)
		{
			if (interactor.getType() == EntityType.PLAYER)
			{
				LevelManager.toggleDimension(true);
				machineStopped = true;
				level.getStoryHandler().shatter(7.0f);
			}
			else
			{
				level.getStoryHandler().shatter(10.0f);
			}
		}
	}

	@Override
	public boolean canPlayerTravelDimensions()
	{
		return machineStopped;
	}

	@Override
	public boolean shouldShatterWorldLikeMad()
	{
		return !machineStopped;
	}
	
	@Override
	public boolean shouldRenderPlayer(EntityPlayer player)
	{
		return player.getDimension() == Dimension.MONOCHROME || machineStopped;
	}
	
	@Override
	public boolean canLeverBePulled(TileLever lever, EntityPlayer interactor)
	{
		return !machineStopped || TriggerPort.getColor(lever.getPort()) != Color.BLUE;
	}
	
	@Override
	public boolean shouldFavorDirectSpawnpoint()
	{
		return true;
	}
	
	@Override
	public float getShatteringMultiplier()
	{
		return 1.5f;
	}
}
