package com.unlogical.colored.util;

import java.util.Map;

import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;

public class TimeStepHandler implements IDeepCopyable<TimeStepHandler>
{
	public int currentFrameBeforeStart;
	public int currentFrameStart;
	public int currentFrameEnd;
	public int nextFrameStart;
	public int timeLoopLimit;

	private float currentFrameStartPrecise;
	private float currentFrameEndPrecise;

	public Level level;

	public TimeStepHandler(Level level)
	{
		this.level = level;

		this.currentFrameStart = 0;
		this.currentFrameEnd = 0;

		if (level.getLevelType() == LevelType.TIME_TRAVEL || level.getLevelType() == LevelType.FROZEN)
		{
			this.timeLoopLimit = level.getTimeLoopLimit();
		}
		else
		{
			this.timeLoopLimit = Level.DEFAULT_TIME_LOOP_FRAMES;
		}
	}

	@Override
	public TimeStepHandler deepCopy(Map<Object, Object> copiedReferences)
	{
		TimeStepHandler copy = new TimeStepHandler((Level) Level.getCopy(this.level, copiedReferences));

		copy.currentFrameEnd = this.currentFrameEnd;
		copy.currentFrameStart = this.currentFrameStart;
		copy.currentFrameBeforeStart = this.currentFrameBeforeStart;
		copy.nextFrameStart = this.nextFrameStart;
		copy.timeLoopLimit = this.timeLoopLimit;

		return copy;
	}

	@Override
	public boolean shouldCopy()
	{
		return true;
	}

	public void update(int delta)
	{
		this.currentFrameStartPrecise = this.currentFrameEndPrecise;
		this.currentFrameEndPrecise = this.currentFrameStartPrecise + (float) delta / Level.STORED_FRAMES_PER_SECOND;

		this.currentFrameStart = (int) this.currentFrameStartPrecise;
		this.currentFrameEnd = (int) this.currentFrameEndPrecise;

		this.currentFrameBeforeStart = this.currentFrameStart - 1;

		if (this.currentFrameBeforeStart < 0)
		{
			this.currentFrameBeforeStart = this.timeLoopLimit - 1;
		}

		if (this.currentFrameStartPrecise >= this.timeLoopLimit)
		{
			this.currentFrameStartPrecise = this.currentFrameStartPrecise - (int) this.currentFrameStartPrecise + this.currentFrameStart % this.timeLoopLimit;
		}

		if (this.currentFrameEndPrecise >= this.timeLoopLimit)
		{
			this.currentFrameEndPrecise = this.currentFrameEndPrecise - (int) this.currentFrameEndPrecise + this.currentFrameEnd % this.timeLoopLimit;
		}

		this.currentFrameStart = (int) this.currentFrameStartPrecise;
		this.currentFrameEnd = (int) this.currentFrameEndPrecise;

		this.nextFrameStart = this.currentFrameEnd;
	}

	public boolean isInCurrentFrame(int time)
	{
		if (this.currentFrameEnd > this.currentFrameStart)
		{
			return time >= this.currentFrameStart && time < this.currentFrameEnd;
		}
		else
		{
			return time >= this.currentFrameStart && time < this.timeLoopLimit || time >= 0 && time < this.currentFrameEnd;
		}
	}

	public void onDimensionToggle(Dimension newDimension)
	{

	}
}
