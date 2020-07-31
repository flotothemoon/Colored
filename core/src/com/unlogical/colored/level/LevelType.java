package com.unlogical.colored.level;

public enum LevelType
{
	NORMAL,
	REVERSAL,
	TIME_SCALE,
	FROZEN,
	TIME_TRAVEL,
	SEPERATED
	;
	
	public LevelType next()
	{
		int nextIndex = this.ordinal() + 1;
		
		if (nextIndex >= values().length)
		{
			nextIndex = 0;
		}
		
		return values()[nextIndex];
	}
}
