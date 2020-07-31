package com.unlogical.colored.terrain.tile.properties;

public interface ITileReceiver extends ITileTriggerable
{
	public void onTrigger(ITileSender sender, boolean hasInversedMirror);
	
	public void onUntrigger(ITileSender changedTile, boolean hasInversedMirror);
}
