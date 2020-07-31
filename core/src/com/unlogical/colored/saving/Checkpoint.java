package com.unlogical.colored.saving;

import com.unlogical.colored.level.Level;
import com.unlogical.colored.levelmanaging.LevelManager;

public class Checkpoint
{
	public static final int MAX_PRIORITY = 10;
	public static final int MIN_PRIORITY = 1;

	public final CheckpointEvent trigger;
	private Level level;
	private long timestamp;

	private Checkpoint(Level level, CheckpointEvent event, long timestamp)
	{
		this.level = level;
		this.trigger = event;
		this.timestamp = timestamp;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Checkpoint))
		{
			return false;
		}

		return this.timestamp == ((Checkpoint) obj).timestamp;
	}

	public Checkpoint copy()
	{
		return new Checkpoint(copyLevel(this.level), this.trigger, timestamp);
	}

	public static Checkpoint storeLevel(Level level, CheckpointEvent trigger)
	{
		return new Checkpoint(copyLevel(level), trigger, LevelManager.getRunningTimestamp());
	}

	private static Level copyLevel(Level level)
	{
		Level newLevel = new Level();

		Level.deepCopy(level, newLevel);

		return newLevel;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public Level getLevel()
	{
		return level;
	}
}
