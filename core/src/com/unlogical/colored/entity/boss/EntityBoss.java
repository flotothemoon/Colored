package com.unlogical.colored.entity.boss;

import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityAction;
import com.unlogical.colored.entity.EntityLiving;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.key.EntityKey;
import com.unlogical.colored.entity.key.KeyPort;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.entity.projectile.EntityProjectile;
import com.unlogical.colored.entity.property.EntityProperties;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.LevelObject;

public class EntityBoss extends EntityLiving
{
	protected Vector2 distanceToPlayer = new Vector2(0.0f, 0.0f);
	protected boolean movingRight;
	protected boolean justDied;

	public EntityBoss(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(type, position, level, dimension, allowMirrors);
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		EntityBoss copy = (EntityBoss) super.deepCopy(copiedReferences);

		copy.movingRight = this.movingRight;
		copy.distanceToPlayer.set(this.distanceToPlayer);
		copy.justDied = this.justDied;

		return copy;
	}

	public static final EntityBoss createBoss(Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		if (level != null)
		{
			if (level.getType() == LevelType.REVERSAL)
			{
				return new EntityReversalBoss(EntityType.BOSS, position, level, dimension, allowMirrors);
			}
			else if (level.getType() == LevelType.TIME_SCALE)
			{
				return new EntityTimeScaleBoss(EntityType.BOSS, position, level, dimension, allowMirrors);
			}
			else if (level.getType() == LevelType.FROZEN)
			{
				return new EntityFrozenBoss(EntityType.BOSS, position, level, dimension, allowMirrors);
			}
			else if (level.getType() == LevelType.TIME_TRAVEL)
			{
				return new EntityTimeTravelBoss(EntityType.BOSS, position, level, dimension, allowMirrors);
			}
		}

		return new EntityBoss(EntityType.BOSS, position, level, dimension, allowMirrors);
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{
		EntityPlayer player = this.level.getControllingPlayer();

		this.distanceToPlayer.set(player.getCenterX(), player.getCenterY());
		this.distanceToPlayer.x -= this.getCenterX();
		this.distanceToPlayer.y -= this.getCenterY();
	}

	@Override
	public void onDeathUpdate(float delta, Random rng)
	{
		if (!this.justDied && this.collidedBelow)
		{
			this.resetVelocities();
			this.enabled = false;
		}

		if (this.justDied)
		{
			this.justDied = false;
		}
	}

	@Override
	protected void customOnDeath(LevelObject damageSource)
	{
		if (damageSource instanceof EntityProjectile)
		{
			if (this.hasMirror)
			{
				((EntityBoss) this.mirroredEntity).onDeath(this);
			}

			EntityKey key = (EntityKey) Entity.createEntity(EntityType.KEY, new Vector2(this.getCenterX(), this.getCenterY()), this.level, this.dimension, false);

			key.setPort(KeyPort.GREEN);
			key.setShared(true);

			this.level.addEntity(key);
		}

		this.hitbox.set(this.hitbox.getX(), this.hitbox.getY(), this.hitbox.getRawWidth(), this.hitbox.getRawHeight() - 55);
		this.setRotation(this.velocity.x > 0 ? 15 : -15);

		this.velocity.x *= 0.5f;

		this.changeCurrentAction(EntityAction.DEAD, false);

		this.enabled = true;
		this.setSkipMainUpdate(false);

		this.collidedBelow = false;
		this.hitbox.resetCache();

		this.justDied = true;
	}

	@Override
	public void damagedByEntity(int damage, Entity damageSource)
	{
		if (damage > 0 && damageSource instanceof EntityProjectile)
		{
			((EntityProjectile) damageSource).onDeath(this);

			if (this.hasMirror)
			{
				((EntityBoss) this.mirroredEntity).health = this.health;
			}
		}
	}

	protected EntityProjectile shootProjectile(EntityType projectileType)
	{
		EntityProjectile projectile = (EntityProjectile) Entity.createEntity(EntityType.SHADOWBALL, new Vector2(this.getCenterX(), this.getCenterY()), this.level, this.dimension, false);

		float rotation = this.distanceToPlayer.angle() + 90;
		float shootSpeed = 1.0f;

		projectile.onShoot(this, rotation, shootSpeed);
		projectile.setVelocity(projectile.getStartVelocity(this, rotation, shootSpeed).cpy());

		if (this.dimension == Dimension.MONOCHROME)
		{
			Vector2 invPosition = projectile.calculateInverseStartPosition();

			projectile.setPosition(invPosition.x, invPosition.y);
			projectile.getVelocity().scl(-1.0f);
		}

		this.level.addEntity(projectile);

		return projectile;
	}

	@Override
	protected boolean canBeHarmedBy(Entity entity)
	{
		return entity instanceof EntityProjectile;
	}

	@Override
	protected boolean useAutomatedActions()
	{
		return false;
	}

	@Override
	protected EntityAction getCustomAction()
	{
		if (this.alive)
		{
			return EntityAction.IDLE_DEFAULT;
		}
		else
		{
			return EntityAction.DEAD;
		}
	}

	@Override
	protected Vector2 getHandOffset(boolean focused, EntityAction currentAction, int currentImage)
	{
		return null;
	}

	@Override
	public boolean isBlocked()
	{
		return true;
	}

	@Override
	protected boolean canInteractWith(Entity entity)
	{
		return true;
	}

	@Override
	protected EntityProperties getEntityProperty()
	{
		return EntityProperties.BOSS;
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
		return true;
	}

	@Override
	protected boolean canWalkSlopes()
	{
		return true;
	}

	@Override
	protected boolean shouldBounceOnGround()
	{
		return true;
	}

	@Override
	public boolean supportsAction(EntityAction action)
	{
		return action == EntityAction.IDLE_DEFAULT || action == EntityAction.WALKING || action == EntityAction.DEAD;
	}

	@Override
	protected boolean shouldUseFittingTextures()
	{
		return true;
	}

	@Override
	public String getTextureDirectory()
	{
		return FilePaths.ENTITY_BOSS;
	}
}
