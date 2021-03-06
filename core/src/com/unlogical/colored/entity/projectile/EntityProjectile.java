package com.unlogical.colored.entity.projectile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.collision.Line;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityAction;
import com.unlogical.colored.entity.EntityLiving;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.terrain.TerrainObject;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.cannon.ICannonBall;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.LevelObject;

public abstract class EntityProjectile extends EntityLiving implements ICannonBall
{
	public static List<Line> lines = new ArrayList<Line>();

	protected boolean inSource;
	protected boolean reversing;

	protected LevelObject source;

	public EntityProjectile(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(type, position, level, dimension, allowMirrors);
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		EntityProjectile copy = (EntityProjectile) super.deepCopy(copiedReferences);

		copy.inSource = this.inSource;
		copy.reversing = this.reversing;

		copy.source = (LevelObject) Level.getCopy(this.source, copiedReferences);

		return copy;
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{
		if (this.reversing)
		{
			if (!this.inSource && this.source.getHitbox().intersects(this.hitbox))
			{
				this.inSource = true;
			}

			if (this.inSource && this.source.getHitbox().contains(this.hitbox))
			{
				this.onDeath(null);
			}
		}

		if (this.inSource && !this.source.getHitbox().intersects(this.hitbox) && !this.source.getHitbox().contains(this.hitbox))
		{
			this.inSource = false;
		}
	}

	@Override
	public void initMirror(Entity mirroredEntity, LevelType levelType)
	{
		super.initMirror(mirroredEntity, levelType);

		if (levelType == LevelType.REVERSAL && !this.wasCreatedFromFile)
		{
			Vector2 closestIntersection = this.calculateInverseStartPosition();

			if (!Float.isNaN(closestIntersection.x))
			{
				this.mirroredEntity.setPosition(closestIntersection.x, closestIntersection.y);
			}
		}
	}

	public Vector2 calculateInverseStartPosition()
	{
		lines.clear();

		Line ownLine1 = new Line(this.hitbox.getPoints()[0], this.hitbox.getPoints()[1], this.hitbox.getPoints()[2], this.hitbox.getPoints()[3]);
		Line ownLine2 = new Line(this.hitbox.getPoints()[6], this.hitbox.getPoints()[7], this.hitbox.getPoints()[4], this.hitbox.getPoints()[5]);
		Line otherLine = new Line(0.0f, 0.0f);

		bufferedVector.set(ownLine1.getEnd());
		bufferedVector.sub(ownLine1.getStart());
		bufferedVector.scl(10000.0f);

		ownLine1.set(ownLine1.getX1(), ownLine1.getY1(), ownLine1.getX1() + bufferedVector.x, ownLine1.getY1() + bufferedVector.y);

		bufferedVector.set(ownLine2.getEnd());
		bufferedVector.sub(ownLine2.getStart());
		bufferedVector.scl(10000.0f);

		ownLine2.set(ownLine2.getX1(), ownLine2.getY1(), ownLine2.getX1() + bufferedVector.x, ownLine2.getY1() + bufferedVector.y);

		Vector2 intersection = new Vector2(0.0f, 0.0f);
		Vector2 closestIntersection = new Vector2(Float.NaN, Float.NaN);

		Vector2 firstOffset = new Vector2(this.hitbox.getPoints()[0] - this.position.x, this.hitbox.getPoints()[1] - this.position.y).scl(-1.0f);
		Vector2 secondOffset = new Vector2(this.hitbox.getPoints()[6] - this.position.x, this.hitbox.getPoints()[7] - this.position.y).scl(-1.0f);

		float closestDistance = Float.MAX_VALUE;

		Dimension beforeDimension = this.dimension;

		this.dimension = Dimension.MONOCHROME;

		for (Tile tile : this.level.getTiles())
		{
			if (this.inSameDimension(tile) && (!tile.hasMirror() || !this.source.hasMirror() || tile != this.source.getMirror()) && this.isBlocked(tile, this.velocity))
			{
				Hitbox otherHitbox = tile.getHitbox();
				float[] otherPoints = otherHitbox.getPoints();

				int lastIndex = 3;

				for (int i = 0; i < otherHitbox.getPoints().length / 2; i++)
				{
					otherLine.set(otherPoints[lastIndex * 2], otherPoints[lastIndex * 2 + 1], otherPoints[i * 2], otherPoints[i * 2 + 1]);

					if (ownLine1.intersect(otherLine, true, intersection))
					{
						intersection.x -= this.hitbox.getPoints()[2];
						intersection.y -= this.hitbox.getPoints()[3];

						if (intersection.len() < closestDistance)
						{
							closestIntersection.set(intersection.x + this.hitbox.getPoints()[0] + firstOffset.x, intersection.y + this.hitbox.getPoints()[1] + firstOffset.y);
							closestDistance = intersection.len();
						}
					}

					if (ownLine2.intersect(otherLine, true, intersection))
					{
						intersection.x -= this.hitbox.getPoints()[4];
						intersection.y -= this.hitbox.getPoints()[5];

						if (intersection.len() < closestDistance)
						{
							closestIntersection.set(intersection.x + this.hitbox.getPoints()[6] + secondOffset.x, intersection.y + this.hitbox.getPoints()[7] + secondOffset.y);
							closestDistance = intersection.len();
						}
					}

					lastIndex = i;
				}
			}
		}

		this.dimension = beforeDimension;

		if (Float.isNaN(closestIntersection.x))
		{
			float[] levelLimits = new float[] { this.level.getAbsLeftLimit().getCenterX(), this.level.getAbsTopLimit().getCenterY(), this.level.getAbsRightLimit().getCenterX(), this.level.getAbsTopLimit().getCenterY(), this.level.getAbsRightLimit().getCenterX(), this.level.getAbsBottomLimit().getCenterY(), this.level.getAbsLeftLimit().getCenterX(), this.level.getAbsBottomLimit().getCenterY() };
			int lastIndex = 3;

			ownLine1.set(this.getCenterX(), this.getCenterY(), this.getCenterX() + bufferedVector.x, this.getCenterY() + bufferedVector.y);

			for (int i = 0; i < levelLimits.length / 2; i++)
			{
				otherLine.set(levelLimits[lastIndex * 2], levelLimits[lastIndex * 2 + 1], levelLimits[i * 2], levelLimits[i * 2 + 1]);

				if (ownLine1.intersect(otherLine, true, intersection))
				{
					closestIntersection.set(intersection);

					break;
				}

				lastIndex = i;
			}
		}

		Vector2 ownLength = new Vector2(this.hitbox.getRawWidth(), 0.0f);

		ownLength.setAngle(this.velocity.angle());

		closestIntersection.sub(ownLength);

		return closestIntersection;
	}

	@Override
	public void onShoot(LevelObject source, float rotation, float shootSpeed)
	{
		this.shooting = true;
		this.inSource = true;
		this.source = source;

		this.hitbox.setRotation(rotation - 90, this.hitbox.getCenterX() - this.hitbox.getMinX(), this.hitbox.getCenterY() - this.hitbox.getMinY());
		this.setRotation(rotation - 90);

		if (this.hasMirror && this.dimension == Dimension.COLORED && this.level.getType() == LevelType.REVERSAL)
		{
			EntityProjectile mirror = (EntityProjectile) this.mirroredEntity;
			Vector2 mirrorVelocity = ((ICannonBall) this.mirroredEntity).getStartVelocity(source, rotation, shootSpeed).cpy();

			this.mirroredEntity.setPosition(this.position.x, this.position.y);
			this.mirroredEntity.setVelocity(mirrorVelocity);

			mirror.source = this.source.getMirror();

			this.initMirror(this.mirroredEntity, this.level.getLevelType());

			bufferedVector.set(mirror.getCenterX(), mirror.getCenterY());
			bufferedVector.x -= source.getHitbox().getCenterX();
			bufferedVector.y -= source.getHitbox().getCenterY();

			mirror.velocity.setAngle(180 + bufferedVector.angle());

			mirror.onShoot(source.hasMirror() ? source.getMirror() : source, rotation, shootSpeed);
			mirror.reversing = true;
		}
	}

	@Override
	public Vector2 getStartPosition(LevelObject source, float cannonRotation)
	{
		if (source.getRotation() == cannonRotation)
		{
			bufferedVector.x = (source.getHitbox().getPoints()[0] + source.getHitbox().getPoints()[2] - this.hitbox.getWidth()) / 2;
			bufferedVector.y = (source.getHitbox().getPoints()[1] + source.getHitbox().getPoints()[3] - this.hitbox.getHeight()) / 2;
		}
		else
		{
			bufferedVector.x = (source.getHitbox().getPoints()[4] + source.getHitbox().getPoints()[6] - this.hitbox.getWidth()) / 2;
			bufferedVector.y = (source.getHitbox().getPoints()[5] + source.getHitbox().getPoints()[7] - this.hitbox.getHeight()) / 2;
		}

		return bufferedVector;
	}

	@Override
	public Vector2 getStartVelocity(LevelObject source, float rotation, float speedFactor)
	{
		bufferedVector.setAngle(0.0f);
		bufferedVector.x = 0.0f;
		bufferedVector.y = -speedFactor * this.properties.getMaxSpeed();
		bufferedVector.setAngle(bufferedVector.angle() + rotation);

		return bufferedVector;
	}

	@Override
	protected float getAdditionalCheckDistance(Vector2 otherVelocity)
	{
		if (this.initialising)
		{
			return Math.max(this.level.getMapWidth(), this.level.getMapHeight());
		}
		else
		{
			return (this.velocity.len() + otherVelocity.len()) * 50.0f;
		}
	}

	@Override
	protected boolean canBeHarmedBy(Entity entity)
	{
		return false;
	}

	@Override
	protected boolean isBlocked(TerrainObject tile, Vector2 myVelocity)
	{
		return this.source != tile && super.isBlocked(tile, myVelocity);
	}

	@Override
	public boolean canPickUpEntities()
	{
		return false;
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
	protected boolean canWalkSlopes()
	{
		return false;
	}

	@Override
	public boolean isDead()
	{
		return !this.alive;
	}

	@Override
	public boolean hasCollided()
	{
		return this.collidedLeft || this.collidedRight || this.collidedAbove || this.collidedBelow;
	}

	@Override
	public ICannonBall getMirroredBall()
	{
		return (ICannonBall) this.mirroredEntity;
	}

	@Override
	protected boolean useAutomatedActions()
	{
		return false;
	}

	@Override
	protected EntityAction getCustomAction()
	{
		return EntityAction.IDLE_DEFAULT;
	}

	@Override
	public boolean supportsAction(EntityAction action)
	{
		return action == EntityAction.IDLE_DEFAULT;
	}

	@Override
	public String getTextureDirectory()
	{
		return FilePaths.PROJECTILES;
	}

	@Override
	protected boolean canInteractWith(Entity entity)
	{
		return true;
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
	protected boolean notifyOnCollisions()
	{
		return true;
	}

	@Override
	protected boolean shouldRemoveOnDeath()
	{
		return true;
	}

	@Override
	public boolean shouldReSimulate()
	{
		return super.shouldReSimulate() && !this.shared;
	}

	@Override
	public SoundType getOnShotSound()
	{
		return SoundType.CANNON_SHOOT_BALL;
	}

	@Override
	public EmitterType getPreShootEmitterType()
	{
		return EmitterType.BALL_PRESHOOT;
	}

	public LevelObject getSource()
	{
		return this.source;
	}

	public boolean isInSource()
	{
		return this.inSource;
	}
}
