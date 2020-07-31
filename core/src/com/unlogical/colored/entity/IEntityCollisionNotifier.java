package com.unlogical.colored.entity;

import com.unlogical.colored.terrain.tile.Tile;

public interface IEntityCollisionNotifier
{
	public boolean onCollisionWithEntity(Entity entity, boolean top, boolean bottom, boolean right, boolean left, boolean inversed);
	
	public void onCollisionWithTile(Tile tile, boolean top, boolean bottom, boolean right, boolean left, boolean intersect);
}
