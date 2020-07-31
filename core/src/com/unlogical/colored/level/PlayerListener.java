package com.unlogical.colored.level;

import com.unlogical.colored.entity.player.EntityPlayer;

public interface PlayerListener {

	public void playerMoved(Level level, EntityPlayer player, Direction direction);
	
	public void playerJumped(Level level, EntityPlayer player);
	
	public void playerDamaged(Level level, EntityPlayer player, int damage);
	
	public void playerDied(Level level, EntityPlayer player);
}
