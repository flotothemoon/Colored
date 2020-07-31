package com.unlogical.colored.entity.projectile;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.boss.EntityBoss;
import com.unlogical.colored.entity.property.EntityProperties;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.util.Dimension;

public class EntityMiniIceSpike extends EntityIceSpike
{
	public EntityMiniIceSpike(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(type, position, level, dimension, allowMirrors);
	}
	
	@Override
	protected boolean canInteractWith(Entity entity)
	{
		return !(entity instanceof EntityIceSpike) && !(entity instanceof EntityBoss);
	}
	
	@Override
	protected EntityProperties getEntityProperty()
	{
		return EntityProperties.MINI_ICE_SPIKE;
	}
}
