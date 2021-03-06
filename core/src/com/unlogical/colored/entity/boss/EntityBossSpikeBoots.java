package com.unlogical.colored.entity.boss;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityAction;
import com.unlogical.colored.entity.EntityLiving;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.property.EntityProperties;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.util.Dimension;

public class EntityBossSpikeBoots extends EntityLiving
{
	public EntityBossSpikeBoots(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(type, position, level, dimension, allowMirrors);
	}

	@Override
	protected boolean canBeHarmedBy(Entity entity)
	{
		return false;
	}

	@Override
	protected Vector2 getHandOffset(boolean focused, EntityAction currentAction, int currentImage)
	{
		return null;
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{

	}

	@Override
	public boolean isBlocked()
	{
		return true;
	}

	@Override
	protected boolean canInteractWith(Entity entity)
	{
		return false;
	}

	@Override
	protected EntityProperties getEntityProperty()
	{
		return EntityProperties.BOSS_SPIKE_BOOTS;
	}

	@Override
	public String getTextureDirectory()
	{
		return FilePaths.ENTITY_BOSS;
	}

	@Override
	public boolean checkEntityCollisions()
	{
		return true;
	}

	@Override
	public boolean collidesWithEntities()
	{
		return true;
	}

	@Override
	public boolean collidesWithTerrain()
	{
		return false;
	}

	@Override
	protected boolean canWalkSlopes()
	{
		return false;
	}

	@Override
	public boolean shouldRenderInBatch()
	{
		return false;
	}
	
	@Override
	public boolean isActive()
	{
		return false;
	}

	@Override
	public boolean supportsAction(EntityAction action)
	{
		return action == EntityAction.IDLE_DEFAULT;
	}
}
