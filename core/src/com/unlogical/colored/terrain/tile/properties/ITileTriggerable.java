package com.unlogical.colored.terrain.tile.properties;

import com.unlogical.colored.terrain.TriggerPort;

public interface ITileTriggerable extends ITileMirrorable
{	
	public static final int TOTAL_PORTS = TriggerPort.values().length - 1;
}
