package com.unlogical.colored.story;

import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.cage.TileCage;
import com.unlogical.colored.terrain.tile.logical.TileLever;
import com.unlogical.colored.util.Dimension;

public class ColoredLevelStoryHandler extends CustomStoryHandler
{
	private static Random rng = new Random();
	private boolean cageOpened;

	public ColoredLevelStoryHandler(Level level)
	{
		super(level);
	}

	@Override
	public CustomStoryHandler deepCopy(Map<Object, Object> copiedReferences)
	{
		ColoredLevelStoryHandler copy = new ColoredLevelStoryHandler((Level) Level.getCopy(level, copiedReferences));

		copy.cageOpened = this.cageOpened;

		return copy;
	}

	@Override
	public void update(int delta)
	{
		if (cageOpened)
		{			
			if (level.getBackgroundFactor() < 1.0f)
			{
				float backgroundFactor = level.getBackgroundFactor();

				backgroundFactor += rng.nextFloat() / 5.0f;

				if (backgroundFactor > 1.0f)
				{
					backgroundFactor = 1.0f;
				}

				Color backgroundColor = level.getDimensionColor(level.getBackgroundDimension());

				backgroundColor.a = Level.DEFAULT_BACKGROUND_ALPHA + (Level.DEFAULT_FOREGROUND_ALPHA - Level.DEFAULT_BACKGROUND_ALPHA) * backgroundFactor;

				level.setBackgroundFactor(backgroundFactor);
			}
			else if (level.getForegroundDimension() != Dimension.COLORED)
			{
				LevelManager.toggleDimension(false);
			}
			else
			{
				if (((EntityPlayer) level.getControllingPlayer().getMirroredEntity()).getDistanceFromSpawnpoint().len() > 30.0f && level.getControllingPlayer().getHitbox().intersects(level.getControllingPlayer().getMirroredEntity().getHitbox()))
				{
					level.getControllingPlayer().onDeath(null);
				}
			}
		}
	}

	@Override
	public void onCageOpened(TileCage cage, int levelOpenedCages, int levelTotalCages, boolean fromSave)
	{
		cageOpened = true;

		if (!fromSave)
		{
			level.getStoryHandler().shatter(15.0f);
			
			for (Tile tile : LevelManager.getLevel().getInteractableTiles())
			{
				if (tile instanceof TileLever && ((TileLever) tile).isShared() && ((TileLever) tile).isTriggering())
				{
					((TileLever) tile).onInteract(level.getControllingPlayer());
				}
			}
		}
	}

	@Override
	public boolean canPlayerTravelDimensions()
	{
		return !cageOpened;
	}

	@Override
	public float getShatteringMultiplier()
	{
		return 2.0f;
	}

	@Override
	public boolean shouldWorldBeConsideredForShattering()
	{
		return false;
	}

	@Override
	public boolean shouldLeveldBeConsideredForShattering()
	{
		return false;
	}
	
	@Override
	public boolean shouldPlayerBeHandledAsShared()
	{
		return !cageOpened;
	}
}
