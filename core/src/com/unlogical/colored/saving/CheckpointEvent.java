package com.unlogical.colored.saving;

public enum CheckpointEvent
{
	INITIAL(Checkpoint.MAX_PRIORITY), TOGGLE_DIMENSIONS(6), TURN_AROUND_LONG(7),
	LANDED(5), JUMP(5), CLIMB_STOP(5), CLIMB_START(5), INTERACT(7),
	SCHEDULE(Checkpoint.MIN_PRIORITY);

	public final int priority;

	private CheckpointEvent(int priority)
	{
		this.priority = priority;
	}
}