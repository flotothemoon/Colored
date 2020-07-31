package com.unlogical.colored.entity.key;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityAction;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.entity.property.EntityProperties;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.util.Dimension;

public class EntityKeyChain extends Entity
{
	public int stage;

	public EntityKeyChain(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(type, position, level, dimension, allowMirrors);
	}

	@Override
	public boolean isBlocked()
	{
		return false;
	}

	@Override
	public void updateEntity(float delta, Random rng)
	{

	}

	@Override
	protected boolean canInteractWith(Entity entity)
	{
		return entity instanceof EntityPlayer;
	}

	@Override
	protected EntityProperties getEntityProperty()
	{
		return EntityProperties.KEYCHAIN;
	}

	@Override
	public boolean checkEntityCollisions()
	{
		return true;
	}

	@Override
	public boolean collidesWithEntities()
	{
		return false;
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
	protected boolean useAutomatedActions()
	{
		return false;
	}

	@Override
	protected EntityAction getCustomAction()
	{
		if (this.stage == 1)
		{
			return EntityAction.CUSTOM_ONCE0;
		}
		else if (this.stage == 2)
		{
			return EntityAction.CUSTOM_ONCE1;
		}

		return EntityAction.IDLE_DEFAULT;
	}

	@Override
	public boolean supportsAction(EntityAction action)
	{
		return action == EntityAction.IDLE_DEFAULT || action == EntityAction.CUSTOM_ONCE0 || action == EntityAction.CUSTOM_ONCE1;
	}

	@Override
	public strictfp String getTextureDirectory()
	{
		return FilePaths.ENTITY_OTHER;
	}
}
