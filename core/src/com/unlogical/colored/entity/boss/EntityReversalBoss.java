package com.unlogical.colored.entity.boss;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityAction;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.key.EntityKeyChain;
import com.unlogical.colored.entity.projectile.EntityProjectile;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.LevelObject;

public class EntityReversalBoss extends EntityBoss
{
	private static final float MAX_TARGET_DISTANCE = 1200.0f;
	private static final float MAX_LOSE_TARGET_DISTANCE = 2000.0f;
	private static final int IN_BETWEEN_PROJECTILE_SHOOTING_COOLDOWN = 250;
	private static final int ACTION_COOLDOWN = 4000;
	private static final int NUM_STAGES = 3;

	private Set<EntityProjectile> projectiles = new HashSet<EntityProjectile>();
	private EntityBossSpikeBoots boots;
	private EntityKeyChain keyChain;
	private EntityBossEye leftEye;
	private EntityBossEye rightEye;
	private EntityBossState state;

	private boolean shouldMove;

	private Vector2 focus = new Vector2();
	private Vector2 originalFocus = new Vector2();

	private int actionCooldown = ACTION_COOLDOWN;
	private int stage;
	private int shootingRoundCount;

	private int betweenProjectileCooldown;
	private int perShootingProjectileCount = 3;
	private int shotProjectilesCount;

	private int totalTimeBeforeJump = 1500;
	private int timeBeforeJump;
	private int totalTimeToJumpHigh = 400;
	private int timeToJumpHigh;

	public EntityReversalBoss(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(type, position, level, dimension, allowMirrors);

		this.maxGravity = 1.4f;
	}

	@Override
	public void init()
	{
		super.init();

		this.boots = (EntityBossSpikeBoots) Entity.createEntity(EntityType.BOSS_SPIKE_BOOTS, new Vector2(), this.level, this.dimension, false);
		this.boots.setPosition(this.updateBootPosition(this.boots, this.position));
		this.boots.setShared(true);
		this.boots.setPersistent(false);

		this.level.addEntity(this.boots);

		this.keyChain = (EntityKeyChain) Entity.createEntity(EntityType.KEYCHAIN, new Vector2(), this.level, this.dimension, false);
		this.keyChain.setPosition(this.updateKeyChainPosition(this.keyChain, this.position));
		this.keyChain.setShared(true);
		this.keyChain.setPersistent(false);

		this.level.addEntity(this.keyChain);

		float eyeSize = 10;

		this.leftEye = (EntityBossEye) Entity.createEntity(EntityType.BOSS_EYE, new Vector2(), this.level, this.dimension, false);
		this.leftEye.eyeSize = eyeSize;
		this.leftEye.eyeCenterOffsetX = 60;
		this.leftEye.eyeCenterOffsetY = 80;
		this.leftEye.setPersistent(false);

		this.level.addEntity(this.leftEye);

		this.rightEye = (EntityBossEye) Entity.createEntity(EntityType.BOSS_EYE, new Vector2(), this.level, this.dimension, false);
		this.rightEye.eyeSize = eyeSize;
		this.rightEye.eyeCenterOffsetX = 120;
		this.rightEye.eyeCenterOffsetY = 80;
		this.rightEye.setPersistent(false);

		this.level.addEntity(this.rightEye);

		this.state = EntityBossState.SLEEP;
		this.stage = 1;
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		EntityReversalBoss copy = (EntityReversalBoss) super.deepCopy(copiedReferences);

		copy.boots = (EntityBossSpikeBoots) Level.getCopy(this.boots, copiedReferences);
		copy.keyChain = (EntityKeyChain) Level.getCopy(this.keyChain, copiedReferences);
		copy.leftEye = (EntityBossEye) Level.getCopy(this.leftEye, copiedReferences);
		copy.rightEye = (EntityBossEye) Level.getCopy(this.rightEye, copiedReferences);
		copy.state = this.state;
		copy.stage = this.stage;
		copy.shouldMove = this.shouldMove;
		copy.focus = this.focus.cpy();
		copy.originalFocus = this.originalFocus.cpy();

		return copy;
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{
		super.onUpdate(delta, rng);

		this.focus.set(this.level.getControllingPlayer().getPosition().x, this.level.getControllingPlayer().getPosition().y);

		if (this.dimension == this.level.getForegroundDimension())
		{
			if (this.state == EntityBossState.SLEEP)
			{
				if (Math.abs(this.distanceToPlayer.x) < MAX_TARGET_DISTANCE)
				{
					this.shouldMove = true;
					this.state = EntityBossState.IDLE;
					this.actionCooldown = ACTION_COOLDOWN;
					this.syncState();
				}
			}
			else if (this.state == EntityBossState.IDLE)
			{
				if (Math.abs(this.distanceToPlayer.x) > MAX_LOSE_TARGET_DISTANCE)
				{
					this.shouldMove = false;
					this.state = EntityBossState.SLEEP;
					this.syncState();
				}
				else
				{
					this.actionCooldown -= delta;

					if (this.actionCooldown <= 0)
					{
						if (this.shootingRoundCount >= this.stage)
						{
							this.state = EntityBossState.STOMP;

							this.timeBeforeJump = this.totalTimeBeforeJump;
							this.timeToJumpHigh = this.totalTimeToJumpHigh;
							this.originalFocus.set(this.focus);
							this.shootingRoundCount = 0;
						}
						else
						{
							this.state = EntityBossState.SHOOTING;
						}

						this.actionCooldown = ACTION_COOLDOWN;
					}
				}
			}
			else if (this.state == EntityBossState.SHOOTING)
			{
				this.betweenProjectileCooldown -= delta;

				if (this.betweenProjectileCooldown <= 0)
				{
					EntityProjectile projectile = this.shootProjectile(EntityType.SHADOWBALL);

					if (this.hitbox.contains(projectile.getHitbox()) || this.hitbox.intersects(projectile.getHitbox()))
					{
						this.projectiles.add(projectile);
					}

					this.betweenProjectileCooldown = IN_BETWEEN_PROJECTILE_SHOOTING_COOLDOWN;

					this.shotProjectilesCount++;

					if (this.shotProjectilesCount >= this.perShootingProjectileCount)
					{
						this.shootingRoundCount++;
						this.shotProjectilesCount = 0;

						this.state = EntityBossState.IDLE;
					}
				}
			}
			else if (this.state == EntityBossState.STOMP)
			{
				if (this.timeBeforeJump >= 0)
				{
					this.timeBeforeJump -= delta;

					this.originalFocus.setAngle(this.originalFocus.angle() + delta);
					this.focus.set(this.originalFocus);
				}
				else if (this.timeToJumpHigh >= 0)
				{
					this.timeToJumpHigh -= delta;

					this.velocity.y -= 0.06f * this.timeToJumpHigh / this.totalTimeToJumpHigh + 0.06f;
				}
				else if (this.isOnGround())
				{
					this.state = EntityBossState.IDLE;
				}
			}

			if (this.shouldMove)
			{
				this.movingRight = this.distanceToPlayer.x > 0.0f;

				this.velocity.x += this.properties.getAcceleration(delta) * (this.movingRight ? 1 : -1);

				if (Math.abs(this.velocity.x) > this.properties.getMaxSpeed())
				{
					this.velocity.x = this.properties.getMaxSpeed() * Math.signum(this.velocity.x);
				}
			}
			else
			{
				this.velocity.x *= 0.9f;
			}
		}
		else
		{
			this.velocity.x = -this.mirroredEntity.getVelocity().x;
			this.movingRight = !((EntityBoss) this.mirroredEntity).movingRight;
		}

		this.setFocused(this.movingRight);
	}

	@Override
	public void damage(int damage, LevelObject damageSource)
	{
		super.damage(damage, damageSource);

		if (damage > 0)
		{
			this.stage++;

			if (this.stage >= NUM_STAGES)
			{
				this.stage = NUM_STAGES;
			}

			this.keyChain.stage = this.stage;
		}
	}

	@Override
	public void draw(Batch batch)
	{
		draw(batch, dimension);
	}

	@Override
	public void draw(Batch batch, Dimension dimension)
	{
		super.draw(batch, dimension);

		this.updateBootPosition(this.boots, this.position);
		this.updateKeyChainPosition(this.keyChain, this.position);

		this.boots.draw(batch, dimension);
		this.keyChain.draw(batch, dimension);

		if (this.level.getControllingPlayer() != null)
		{
			float shootingFactor = 1.0f - (float) this.actionCooldown / ACTION_COOLDOWN;

			this.leftEye.redFactor = shootingFactor;
			this.rightEye.redFactor = shootingFactor;

			this.leftEye.lookAt(this.focus.x, this.focus.y, this);
			this.rightEye.lookAt(this.focus.x, this.focus.y, this);

			if (this.state != EntityBossState.SLEEP)
			{
				this.leftEye.draw(batch, dimension);
				this.rightEye.draw(batch, dimension);
			}
		}
	}

	private Vector2 updateKeyChainPosition(EntityKeyChain keyChain, Vector2 myPosition)
	{
		Vector2 result = keyChain.getPosition();

		result.x = myPosition.x + this.hitbox.getRawWidth() - keyChain.getHitbox().getWidth() - 20;
		result.y = myPosition.y + this.hitbox.getRawHeight() - keyChain.getHitbox().getHeight() - 10;

		keyChain.setPosition(result.x, result.y);

		return result;
	}

	private Vector2 updateBootPosition(EntityBossSpikeBoots boots, Vector2 myPosition)
	{
		Vector2 result = boots.getPosition();

		result.x = this.getCenterX() - boots.getHitbox().getWidth() / 2;
		result.y = this.getMaxY() - boots.getHitbox().getHeight();

		boots.setPosition(result.x, result.y);

		return result;
	}

	public void syncState()
	{
		((EntityReversalBoss) this.mirroredEntity).state = this.state;
	}

	@Override
	protected boolean canInteractWith(Entity entity)
	{
		return super.canInteractWith(entity) && entity != this.boots;
	}

	@Override
	protected boolean canBeHarmedBy(Entity entity)
	{
		if (!(entity instanceof EntityProjectile) && !(entity instanceof EntityBossSpikeBoots) || entity == this.boots)
		{
			return false;
		}

		return super.canBeHarmedBy(entity) && ((EntityProjectile) entity).getSource() != this && !this.projectiles.contains(entity);
	}

	@Override
	protected boolean shouldBounceOnGround()
	{
		return false;
	}

	@Override
	protected EntityAction getCustomAction()
	{
		if (this.state == EntityBossState.SLEEP)
		{
			return EntityAction.IDLE_DEFAULT;
		}
		else
		{
			return EntityAction.WALKING;
		}
	}

	private enum EntityBossState
	{
		SHOOTING, IDLE, SLEEP, STOMP
	}
}
