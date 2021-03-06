package com.unlogical.colored.entity.projectile;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.boss.EntityBoss;
import com.unlogical.colored.entity.property.EntityProperties;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.LevelObject;

public class EntityAnvil extends EntityShadowBall
{
	public EntityAnvil(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(type, position, level, dimension, allowMirrors);
		
		this.setGravityAffected(true);
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{
		super.onUpdate(delta, rng);
		
		if (Math.abs(this.velocity.x) > 0.0f)
		{
			this.velocity.x *= 1.0f - (0.017f * delta);
		}
	}
	
	@Override
	protected void onCollision(LevelObject object)
	{
		if (object instanceof Tile && this.velocity.y > this.maxGravity / 2.0f || object instanceof EntityBoss)
		{
			super.onCollision(object);
		}
	}
	
	@Override
	protected EntityProperties getEntityProperty()
	{
		return EntityProperties.ANVIL;
	}
}
