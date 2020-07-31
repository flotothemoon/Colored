package com.unlogical.colored.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.collision.Line;
import com.unlogical.colored.collision.Point;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.entity.boss.EntityBoss;
import com.unlogical.colored.entity.boss.EntityBossEye;
import com.unlogical.colored.entity.boss.EntityBossSpikeBoots;
import com.unlogical.colored.entity.goomba.EntityGoomba;
import com.unlogical.colored.entity.key.EntityKey;
import com.unlogical.colored.entity.key.EntityKeyChain;
import com.unlogical.colored.entity.key.EntityStickyKey;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.entity.player.EntityScientist;
import com.unlogical.colored.entity.projectile.EntityAnvil;
import com.unlogical.colored.entity.projectile.EntityIceSpike;
import com.unlogical.colored.entity.projectile.EntityMiniIceSpike;
import com.unlogical.colored.entity.projectile.EntityProjectile;
import com.unlogical.colored.entity.projectile.EntityShadowBall;
import com.unlogical.colored.entity.property.EntityProperties;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.gui.panel.CheatPanel;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.levelmanaging.MapManager;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.resources.ResourceLoader;
import com.unlogical.colored.terrain.TerrainObject;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.TileClimbable;
import com.unlogical.colored.terrain.tile.properties.ITileBlockedFromAbove;
import com.unlogical.colored.terrain.tile.properties.ITileCollissionNotifier;
import com.unlogical.colored.terrain.tile.properties.ITileConveyorBelt;
import com.unlogical.colored.terrain.tile.properties.ITileIntersectionNotifier;
import com.unlogical.colored.terrain.tile.properties.ITileMoveable;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.GameObject;
import com.unlogical.colored.util.ICopyable;
import com.unlogical.colored.util.IShareable;
import com.unlogical.colored.util.LevelObject;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ScheduledEvent;

public abstract strictfp class Entity extends LevelObject implements ICopyable<Entity>, IShareable<Entity>
{
	public static final String CURRENT_ACTION_CHANGED = "currentActionChanged";

	private static final Color colorBuffer = new Color(1.0f, 1.0f, 1.0f, 1.0f);

	protected static HashMap<EntityType, HashMap<String, HashMap<EntityAction, TextureRegion[][]>>> bufferedImages = new HashMap<EntityType, HashMap<String, HashMap<EntityAction, TextureRegion[][]>>>();
	protected static final float EDGE_MIN_STAND_LENGTH = 14.0f;

	private static Line otherLine = new Line(0.0f, 0.0f);
	private static Line line = new Line(0.0f, 0.0f);

	protected HashMap<EntityAction, TextureRegion[][]> availableImages = new HashMap<EntityAction, TextureRegion[][]>();

	protected TextureRegion[] images;

	protected float maxGravity = 0.9f;
	protected float horizontalBounceFactor = -0.55f;
	protected float verticalBounceFactor = -0.5f;

	protected int animationVersion;
	private int durationPerImage;
	private int timeToNextImage;

	private boolean skipMainUpdate;
	protected boolean passiveUpdate;
	protected boolean focused;
	protected boolean isLookingRight;
	protected boolean forceAnimation;
	protected boolean gravityActive = true;
	protected boolean shooting;
	protected boolean suffocating;
	protected boolean timeResistantSuffocating;
	protected boolean jumping;

	protected boolean canFall = true;
	protected boolean onPassableTile;
	protected boolean onRightConnectedTile;
	protected boolean onLeftConnectedTile;
	protected boolean dontResetVertically;
	protected boolean lastBelow;
	protected boolean closeBelow;
	protected boolean closeBelowBefore;
	protected boolean closeAbove;

	protected boolean resumeTimeResistance;
	protected boolean hasMirror;
	protected boolean outOfMap;
	protected boolean holded;
	protected boolean climbing;
	protected boolean climbingOnGround;
	protected boolean onClimbable;
	protected boolean onLadder;
	protected boolean frozen;
	protected boolean tryMoveRight;
	protected boolean tryMoveLeft;

	private boolean render = true;

	private Tile nextTileBelowRight;
	private Tile nextTileBelowLeft;

	protected Tile tileBelow;
	protected TileClimbable climbedTile;

	protected Entity mirroredEntity;
	protected EntityLiving holdingEntity;

	protected EntityProperties properties;
	protected EntityAction currentAction;
	protected EntityAction lastAction;
	protected EntityType type;

	protected Entity(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(null, position, dimension, level);

		this.dimensionUnique = true;
		this.type = type;

		if (this.getEntityProperty() == null)
		{
			throw new IllegalStateException("this.getEntityProperty() cannot be null");
		}

		this.properties = this.getEntityProperty().copy();

		if (level != null)
		{
			this.focused = this.hasDefaultFocus();

			this.currentAction = EntityAction.IDLE_DEFAULT;

			this.durationPerImage = this.properties.getImageDuration(this.currentAction);
			this.currentImage = 0;
			this.timeToNextImage = this.durationPerImage;
			this.availableImages = getAvailableImages(level.getChapter(), type);

			this.images = this.availableImages.get(this.currentAction)[0];
			this.image = this.images[this.currentImage];

			this.hitbox = new Hitbox(position.x, position.y, this.image.getRegionWidth(), this.image.getRegionHeight());

			if (this.shouldCreateMirror(allowMirrors))
			{
				this.createMirror(level.getLevelType());
			}

			this.updateHitbox();
		}
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		Entity copy = (Entity) super.deepCopy(copiedReferences);

		copy.maxGravity = this.maxGravity;
		copy.horizontalBounceFactor = this.horizontalBounceFactor;
		copy.verticalBounceFactor = this.verticalBounceFactor;

		copy.animationVersion = this.animationVersion;
		copy.durationPerImage = this.durationPerImage;
		copy.timeToNextImage = this.timeToNextImage;
		copy.currentImage = this.currentImage;
		copy.image = this.image;
		copy.beforeCurrentImage = this.beforeCurrentImage;
		copy.beforeInnerType = this.beforeInnerType;
		copy.images = this.images;

		copy.skipMainUpdate = this.skipMainUpdate;
		copy.passiveUpdate = this.passiveUpdate;
		copy.setFocused(this.focused);
		copy.isLookingRight = this.isLookingRight;
		copy.forceAnimation = this.forceAnimation;
		copy.gravityActive = this.gravityActive;
		copy.shooting = this.shooting;
		copy.suffocating = this.suffocating;
		copy.timeResistantSuffocating = this.timeResistantSuffocating;
		copy.jumping = this.jumping;

		copy.canFall = this.canFall;
		copy.onPassableTile = this.onPassableTile;
		copy.onRightConnectedTile = this.onRightConnectedTile;
		copy.onLeftConnectedTile = this.onLeftConnectedTile;
		copy.lastBelow = this.lastBelow;
		copy.closeBelow = this.closeBelow;
		copy.closeBelowBefore = this.closeBelowBefore;
		copy.closeAbove = this.closeAbove;

		copy.resumeTimeResistance = this.resumeTimeResistance;
		copy.hasMirror = this.hasMirror;
		copy.outOfMap = this.outOfMap;
		copy.holded = this.holded;
		copy.climbing = this.climbing;
		copy.climbingOnGround = this.climbingOnGround;
		copy.onClimbable = this.onClimbable;
		copy.onLadder = this.onLadder;
		copy.frozen = this.frozen;
		copy.tryMoveLeft = this.tryMoveLeft;
		copy.tryMoveRight = this.tryMoveRight;
		copy.render = this.render;

		copy.nextTileBelowRight = (Tile) Level.getCopy(this.nextTileBelowRight, copiedReferences);
		copy.nextTileBelowLeft = (Tile) Level.getCopy(this.nextTileBelowLeft, copiedReferences);

		copy.tileBelow = (Tile) Level.getCopy(this.tileBelow, copiedReferences);
		copy.climbedTile = (TileClimbable) Level.getCopy(this.climbedTile, copiedReferences);

		copy.mirroredEntity = (Entity) Level.getCopy(this.mirroredEntity, copiedReferences);
		copy.holdingEntity = (EntityLiving) Level.getCopy(this.holdingEntity, copiedReferences);

		copy.properties = this.properties.copy();

		copy.currentAction = this.currentAction;
		copy.lastAction = this.lastAction;
		copy.type = this.type;

		return copy;
	}

	@Override
	public Entity shallowCopy(Level level)
	{
		return this.copy(level);
	}

	@Override
	public Entity copy(Level level)
	{
		Entity copy = Entity.createEntity(this.type, this.position.cpy(), level, this.dimension, false);

		copy.setShared(this.shared);
		copy.setTimeEffected(this.timeEffected);

		return copy;
	}

	@Override
	public void createMirror(LevelType levelType)
	{
		this.mirroredEntity = Entity.createEntity(this.type, new Vector2(this.position.x, this.position.y), this.level, Dimension.MONOCHROME, true);
		this.mirroredEntity.setMirroredEntity(this);

		this.hasMirror = true;

		this.level.addEntity(this.mirroredEntity);
	}

	@Override
	public final boolean shouldCreateMirror(boolean allowMirrors)
	{
		return this.level.shouldCreateMirrors() && allowMirrors && this.dimension == Dimension.COLORED && !this.shared;
	}

	@Override
	public void initMirror(Entity mirroredEntity, LevelType levelType)
	{
		this.mirroredEntity.wasCreatedFromFile = this.wasCreatedFromFile;
	}

	public static HashMap<EntityAction, TextureRegion[][]> getAvailableImages(String chapter, EntityType type)
	{
		if (chapter == null || !CheatPanel.isStringNumeric(chapter))
		{
			chapter = "20";
		}

		return bufferedImages.get(type).get(chapter);
	}

	public static TextureRegion getMainImage(EntityType type)
	{
		return getAvailableImages(null, type).get(EntityAction.IDLE_DEFAULT)[0][0];
	}

	public static void reloadAll()
	{
		bufferedImages.clear();
		loadAll();
	}

	public static void loadAll()
	{
		for (EntityType type : EntityType.values())
		{
			bufferedImages.put(type, ResourceLoader.getEntityImages(type));
		}
	}

	public final static Entity createEntity(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		if (type == EntityType.PLAYER)
		{
			return new EntityPlayer(type, position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.SCIENTIST)
		{
			return new EntityScientist(position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.GOOMBA)
		{
			return new EntityGoomba(position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.KEY)
		{
			return new EntityKey(position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.STICKYKEY)
		{
			return new EntityStickyKey(position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.SHADOWBALL)
		{
			return new EntityShadowBall(type, position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.ICE_SPIKE)
		{
			return new EntityIceSpike(type, position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.MINI_ICE_SPIKES)
		{
			return new EntityMiniIceSpike(type, position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.BOSS)
		{
			return EntityBoss.createBoss(position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.BOSS_SPIKE_BOOTS)
		{
			return new EntityBossSpikeBoots(type, position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.BOSS_EYE)
		{
			return new EntityBossEye(type, position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.ANVIL)
		{
			return new EntityAnvil(type, position, level, dimension, allowMirrors);
		}
		else if (type == EntityType.KEYCHAIN)
		{
			return new EntityKeyChain(type, position, level, dimension, allowMirrors);
		}

		throw new IllegalArgumentException("EntityType could not be created: " + type);
	}

	public final void tryMove(Vector2 distance)
	{
		if (distance.x > 0)
		{
			if (!this.collidedRight)
			{
				if (this.hitbox.canMoveRight(distance.x))
				{
					this.setX(this.position.x + distance.x);
				}
				else
				{
					this.setX(this.position.x + this.hitbox.getDistanceToBlockRight());
					distance.x = this.hitbox.getDistanceToBlockRight();

					this.onCloseMoveToBlockRight(distance.x);
				}
			}
			else
			{
				distance.x = 0.0f;
			}
		}
		else if (distance.x < 0)
		{
			if (!this.collidedLeft)
			{
				if (this.hitbox.canMoveLeft(-distance.x))
				{
					this.setX(this.position.x + distance.x);
				}
				else
				{
					this.setX(this.position.x - this.hitbox.getDistanceToBlockLeft());
					distance.x = -this.hitbox.getDistanceToBlockLeft();

					this.onCloseMoveToBlockLeft(distance.x);
				}
			}
			else
			{
				distance.x = 0.0f;
			}
		}

		if (distance.y > 0)
		{
			if (!this.collidedBelow || (this.hitbox.isLeftBottom() && this.hitbox.getLeftAngle() != 0.0f) ^ (this.hitbox.isRightBottom() && this.hitbox.getRightAngle() != 0.0f))
			{
				Tile nextTile;
				float distanceToBlockBelow = 0.0f;

				if (distance.x > 0 && this.hitbox.getLeftAngle() != 0)
				{
					distanceToBlockBelow = this.hitbox.getDistanceToBlockBelowRight();
					nextTile = this.nextTileBelowRight;
				}
				else if (distance.x < 0 && this.hitbox.getRightAngle() != 0)
				{
					distanceToBlockBelow = this.hitbox.getDistanceToBlockBelowLeft();
					nextTile = this.nextTileBelowLeft;
				}
				else
				{
					distanceToBlockBelow = this.hitbox.getDistanceToBlockBelow();

					if (!this.hitbox.isCheckedBelowRight())
					{
						nextTile = this.nextTileBelowRight;
					}
					else if (!this.hitbox.isCheckedBelowLeft())
					{
						nextTile = this.nextTileBelowLeft;
					}
					else
					{
						nextTile = this.hitbox.getDistanceToBlockBelowLeft() < this.hitbox.getDistanceToBlockBelowLeft() ? this.nextTileBelowLeft : this.nextTileBelowRight;
					}
				}

				if (!(this.hitbox.isCheckedBelowLeft() || this.hitbox.isCheckedBelowRight()) || distance.y <= distanceToBlockBelow)
				{
					this.setY(this.position.y + distance.y);
				}
				else
				{
					this.setY(this.position.y + distanceToBlockBelow);

					if (nextTile != null && nextTile.isMoveable() && nextTile.isMoving())
					{
						this.additionalVelocity.y = nextTile.getVelocity().y;
						this.dontResetVertically = true;
					}

					this.collidedBelow = true;
					this.gravityActive = false;
					this.closeBelow = true;

					distance.y = distanceToBlockBelow;

					this.onCloseMoveToBlockBelow(nextTile, distanceToBlockBelow);
				}
			}
			else
			{
				distance.y = 0.0f;
			}
		}
		else if (distance.y < 0)
		{
			if (!this.collidedAbove)
			{
				if (this.hitbox.canMoveUp(-distance.y))
				{
					this.setY(this.position.y + distance.y);
				}
				else
				{
					this.setY(this.position.y - this.hitbox.getDistanceToBlockAbove());
					this.velocity.y = 0.0f;
					this.closeAbove = true;
					this.collidedAbove = true;

					distance.y = -this.hitbox.getDistanceToBlockAbove();

					this.onCloseMoveToBlockAbove(distance.y);
				}
			}
			else
			{
				distance.y = 0.0f;
			}
		}
	}

	protected void onCloseMoveToBlockBelow(Tile nextTile, float distanceToBlockBelow)
	{

	}

	protected void onCloseMoveToBlockAbove(float distance)
	{

	}

	protected void onCloseMoveToBlockLeft(float distance)
	{

	}

	protected void onCloseMoveToBlockRight(float distance)
	{

	}

	public abstract boolean isBlocked();

	public abstract void updateEntity(float delta, Random rng);

	public final void update(float delta)
	{
		boolean addAdditionalDistance = true, bouncedHorizontally = false,
				bounceVertically = false;
		boolean pushLeft = false, pushRight = false, pushUp = false,
				pushDown = false;
		float additionalHorizontalDelta = 0.0f;
		float additionalVerticalDelta = 0.0f;

		if (this.skipMainUpdate)
		{
			this.updateEntity(delta, this.level.getRNG(this));
		}

		if (!this.skipMainUpdate || this.passiveUpdate)
		{
			this.hitbox.resetCache();

			this.tileBelow = null;
			this.climbedTile = null;
			this.nextTileBelowLeft = null;
			this.nextTileBelowRight = null;
			this.additionalVelocity.x *= 0.8f;
			if (!this.dontResetVertically)
			{
				this.additionalVelocity.y *= 0.8f;
			}

			this.collidedAbove = false;
			this.collidedBelow = false;
			this.collidedLeft = false;
			this.collidedRight = false;
			this.onPassableTile = false;
			this.onRightConnectedTile = false;
			this.onLeftConnectedTile = false;
			this.onClimbable = false;
			this.onLadder = false;
			this.suffocating = false;

			this.gravityActive = this.isGravityAffected();

			if (this.collidesWithTerrain())
			{
				boolean bounceHorizontally = true;

				for (int i = 0; i < this.level.getTiles().size(); i++)
				{
					Tile tile = this.level.getTiles().get(i);

					if (tile.isEnabled() && this.inSameDimension(tile) && this.isNearby(tile.getHitbox(), this.getAdditionalCheckDistance(tile.getVelocity())))
					{
						if (this.isBlocked(tile, this.velocity))
						{
							boolean beforeRight = this.collidedRight,
									beforeLeft = this.collidedLeft;
							boolean collideLeft = false, collideRight = false;

							if (this.hitbox.collides(tile.getHitbox(), true))
							{
								if (!(tile instanceof ITileBlockedFromAbove))
								{
									if (this.hitbox.collidedTop())
									{
										this.collidedAbove = true;
										if (this.velocity.y < 0)
										{
											this.velocity.y *= this.verticalBounceFactor;
										}

										if (this instanceof IEntityCollisionNotifier)
										{
											((IEntityCollisionNotifier) this).onCollisionWithTile(tile, true, false, false, false, false);
										}

										if (tile instanceof ITileCollissionNotifier)
										{
											((ITileCollissionNotifier) tile).onCollisionWithEntity(this, false, true, false, false);
										}
									}

									if (this.hitbox.collidedRight() && (!tile.isMoveable() || !((ITileMoveable) tile).isVerticallyMoveable() || this.hitbox.getMaxY() - tile.getHitbox().getMinY() > 2.0f))
									{
										this.collidedRight = true;
										collideRight = true;

										if (this instanceof IEntityCollisionNotifier)
										{
											((IEntityCollisionNotifier) this).onCollisionWithTile(tile, false, false, true, false, false);
										}

										if (tile instanceof ITileCollissionNotifier)
										{
											((ITileCollissionNotifier) tile).onCollisionWithEntity(this, false, false, false, true);
										}

										if (!tile.entitiesBounceHorizontally())
										{
											bounceHorizontally = false;
										}
									}

									// ??? !collideTop || hitbox.getRotation() %
									// 90 != 0.0f
									if (this.hitbox.collidedLeft() && (!tile.isMoveable() || !((ITileMoveable) tile).isVerticallyMoveable() || this.hitbox.getMaxY() - tile.getHitbox().getMinY() > 2.0))
									{
										this.collidedLeft = true;
										collideLeft = true;

										if (this instanceof IEntityCollisionNotifier)
										{
											((IEntityCollisionNotifier) this).onCollisionWithTile(tile, false, false, false, true, false);
										}

										if (tile instanceof ITileCollissionNotifier)
										{
											((ITileCollissionNotifier) tile).onCollisionWithEntity(this, false, false, true, false);
										}

										if (!tile.entitiesBounceHorizontally())
										{
											bounceHorizontally = false;
										}
									}
								}

								if (this.hitbox.collidedBottom() && !(tile.isMoveable() && ((ITileMoveable) tile).isHorizontallyMoveable() && this.hitbox.getMaxY() - tile.getHitbox().getMinY() > 1))
								{
									if (this.tileBelow == null || this.hitbox.getMinX() + EDGE_MIN_STAND_LENGTH > tile.getHitbox().getMinX() && this.hitbox.getMaxX() - EDGE_MIN_STAND_LENGTH < tile.getHitbox().getMaxX())
									{
										this.tileBelow = tile;
									}

									this.collidedBelow = true;

									if (tile instanceof ITileBlockedFromAbove)
									{
										this.onPassableTile = true;
									}

									if (this.velocity.y > 0.0f)
									{
										if (this.shouldBounceOnGround() && this.velocity.y > 0.3f)
										{
											bounceVertically = true;
										}
										else
										{
											this.velocity.y = 0.0f;
										}
									}

									if (tile.getHitbox().isConnectedLeft())
									{
										this.onLeftConnectedTile = true;
									}

									if (tile.getHitbox().isConnectedRight())
									{
										this.onRightConnectedTile = true;
									}

									this.gravityActive = false;

									if (this instanceof IEntityCollisionNotifier)
									{
										((IEntityCollisionNotifier) this).onCollisionWithTile(tile, false, true, false, false, false);
									}

									if (tile instanceof ITileCollissionNotifier)
									{
										((ITileCollissionNotifier) tile).onCollisionWithEntity(this, true, false, false, false);
									}
								}
							}

							boolean checkedRight = this.hitbox.isCheckedRight();
							boolean checkedLeft = this.hitbox.isCheckedLeft();
							boolean checkedAbove = this.hitbox.isCheckedAbove();
							float beforeRightDistance = this.hitbox.getDistanceToBlockRight();
							float beforeLeftDistance = this.hitbox.getDistanceToBlockLeft();
							float beforeAboveDistance = this.hitbox.getDistanceToBlockAbove();

							if (tile != this.tileBelow && (this.tileBelow == null || tile != this.tileBelow.getLeftConnectedTile() && tile != this.tileBelow.getRightConnectedTile()))
							{
								this.hitbox.checkCollision(tile.getHitbox(), this.getAdditionalCheckDistance(tile.getVelocity()), this.rotation != 0.0f);
							}

							if (this.hitbox.isClosestBelowLeft())
							{
								this.nextTileBelowLeft = tile;
							}

							if (this.hitbox.isClosestBelowRight())
							{
								this.nextTileBelowRight = tile;
							}

							if (this.checkAdditionalSuffocation() && tile.isChangeable() && !(tile instanceof ITileBlockedFromAbove) && this.hitbox.reallyIntersects(tile.getHitbox()))
							{
								this.suffocating = true;

								if (!tile.isTimeEffected())
								{
									this.timeResistantSuffocating = true;
								}
							}

							if (this.hitbox.collidedBottom() || this.hitbox.getCenterX() > tile.getCenterX() && this.hitbox.getMaxY() == tile.getHitbox().getPoints()[tile.getHitbox().getSecondPointIndex() + 1] || this.hitbox.getCenterX() < tile.getCenterX() && this.hitbox.getMaxY() == tile.getHitbox().getPoints()[tile.getHitbox().getTopLeftIndex() + 1] || tile instanceof ITileBlockedFromAbove)
							{
								this.hitbox.setCheckedRight(checkedRight);
								this.hitbox.setCheckedLeft(checkedLeft);

								if (tile.getRotation() % 90 == 0.0f && tile.getHitbox().getMinY() >= this.hitbox.getMaxY() - 0.5f)
								{
									this.collidedLeft = beforeLeft;
									this.collidedRight = beforeRight;
								}

								if (tile instanceof ITileBlockedFromAbove)
								{
									this.hitbox.setDistanceToBlockRight(beforeRightDistance);
									this.hitbox.setDistanceToBlockLeft(beforeLeftDistance);
									this.hitbox.setDistanceToBlockAbove(beforeAboveDistance);
									this.hitbox.setCheckedAbove(checkedAbove);
								}
							}

							if (tile.isMoveable())
							{
								if (!((ITileMoveable) tile).isRotatable() && tile.isMoving())
								{
									if (this.hitbox.collidedBottom())
									{
										if (Math.abs(tile.getVelocity().x) * tile.getLatestDelta() > Math.abs(this.additionalVelocity.x) * additionalHorizontalDelta)
										{
											additionalHorizontalDelta = tile.getLatestDelta();
											this.additionalVelocity.x = tile.getVelocity().x;
										}

										if (Math.abs(tile.getVelocity().y) * tile.getLatestDelta() > Math.abs(this.additionalVelocity.y) * additionalVerticalDelta)
										{
											additionalVerticalDelta = tile.getLatestDelta();
											this.additionalVelocity.y = tile.getVelocity().y;
										}

										if (tile.getVelocity().y < 0.0f)
										{
											pushUp = true;
										}

										if (((ITileMoveable) tile).isVerticallyMoveable())
										{
											this.collidedBelow = true;

											if (this.velocity.y > 0.0f)
											{
												this.velocity.y = 0.0f;
											}
										}
									}
									else if (this.hitbox.intersects(tile.getHitbox()) && !(tile instanceof ITileBlockedFromAbove) && this.hitbox.getMinY() < tile.getHitbox().getMaxY())
									{
										if (tile.getVelocity().x * tile.getLatestDelta() < 0.0f && this.hitbox.getCenterX() < tile.getHitbox().getCenterX())
										{
											pushLeft = true;
											this.collidedRight = true;
										}
										else if (tile.getVelocity().x * tile.getLatestDelta() > 0.0f && this.hitbox.getCenterX() > tile.getHitbox().getCenterX())
										{
											pushRight = true;
											this.collidedLeft = true;
										}

										this.additionalVelocity.x = tile.getVelocity().x;
										additionalHorizontalDelta = tile.getLatestDelta();

										if (Math.abs(tile.getVelocity().y) * tile.getLatestDelta() > Math.abs(this.additionalVelocity.y) * additionalVerticalDelta && !collideRight && !collideLeft)
										{
											additionalVerticalDelta = tile.getLatestDelta();
											this.additionalVelocity.y = tile.getVelocity().y;

											if (this.additionalVelocity.y > 0.0f)
											{
												pushDown = true;
											}
										}
									}
								}

								if (((ITileMoveable) tile).isRotatable())
								{
									if (((ITileMoveable) tile).getRotationChange() != 0.0f)
									{
										addAdditionalDistance = false;

										float pointX = 0.0f, pointY = 0.0f;

										if (this.hitbox.collidedBottom())
										{
											if (((ITileMoveable) tile).getRotationChange() > 0.0f)
											{
												pointX = this.hitbox.getMinX();
											}
											else
											{
												pointX = this.hitbox.getMaxX();
											}

											pointY = this.hitbox.getMaxY();

											Hitbox tHitbox = tile.getHitbox();

											otherLine.set(tHitbox.getPoints()[tHitbox.getTopLeftIndex()], tHitbox.getPoints()[tHitbox.getTopLeftIndex() + 1], tHitbox.getPoints()[tHitbox.nextPoint(tHitbox.getTopLeftIndex())], tHitbox.getPoints()[tHitbox.nextPoint(tHitbox.getTopLeftIndex()) + 1]);

											float xPos;

											if (tHitbox.contains(this.hitbox.getMinX(), this.hitbox.getMaxY()))
											{
												if (tHitbox.contains(this.hitbox.getMaxX(), this.hitbox.getMaxY()))
												{
													xPos = this.hitbox.getMinX();
												}
												else
												{
													if (((ITileMoveable) tile).getRotationChange() > 0.0f)
													{
														xPos = this.hitbox.getMinX();
													}
													else
													{
														xPos = this.hitbox.getMaxX();
													}
												}
											}
											else
											{
												xPos = this.hitbox.getMaxX();
											}

											line.set(xPos, this.hitbox.getMinY(), xPos, this.hitbox.getMaxY());

											Vector2 intersection = otherLine.intersect(line, true);

											if (intersection != null)
											{
												pointY = intersection.y;
											}

											Point result = Hitbox.rotatePoint(pointX, pointY, tile.getCenterOfRotationX(), tile.getCenterOfRotationY(), ((ITileMoveable) tile).getRotationChange());

											this.additionalVelocity.x = (result.getX() - pointX) / tile.getLatestDelta();
											this.additionalVelocity.y = (result.getY() - pointY) / tile.getLatestDelta();

											additionalHorizontalDelta = additionalVerticalDelta = tile.getLatestDelta();
										}
										else if (this.hitbox.intersects(tile.getHitbox()))
										{
											if (this.hitbox.getCenterX() > tile.getHitbox().getCenterX())
											{
												pointX = this.hitbox.getMaxX();
											}
											else
											{
												pointX = this.hitbox.getMinX();
											}

											pointY = this.hitbox.getCenterY();

											Point result = Hitbox.rotatePoint(pointX, pointY, tile.getCenterOfRotationX(), tile.getCenterOfRotationY(), ((ITileMoveable) tile).getRotationChange());

											this.additionalVelocity.x = (result.getX() - pointX) / delta;
											this.additionalVelocity.y = (result.getY() - pointY) / delta;

											additionalHorizontalDelta = additionalVerticalDelta = tile.getLatestDelta();

											if ((this.hitbox.isLeftTop() || this.hitbox.isRightTop()) && this.additionalVelocity.y > 0.0f)
											{
												pushDown = true;
											}
										}
									}
								}
							}

							if (this.hitbox.collidedBottom() && tile instanceof ITileConveyorBelt)
							{
								this.additionalVelocity.x = ((ITileConveyorBelt) tile).getHorizontalVelocity();
								additionalHorizontalDelta = tile.getLatestDelta();
							}
						}

						if (tile.isClimbable() && (tile.getHitbox().intersects(this.hitbox) || tile.getHitbox().contains(this.hitbox)))
						{
							this.onClimbable = true;

							if (tile.getType() == TileType.LADDER)
							{
								this.onLadder = true;
							}

							this.climbedTile = (TileClimbable) tile;
						}

						if (tile.isIntersectionNotifier() || this instanceof IEntityCollisionNotifier)
						{
							if (this.hitbox.intersects(tile.getHitbox()) || tile.getHitbox().contains(this.hitbox))
							{
								if (tile.isIntersectionNotifier())
								{
									((ITileIntersectionNotifier) tile).onEntityIntersection(this);
								}

								if (this instanceof IEntityCollisionNotifier)
								{
									((IEntityCollisionNotifier) this).onCollisionWithTile(tile, false, false, false, false, true);
								}
							}
						}
					}
				}

				if (this.collidedAbove && this.canWalkSlopes())
				{
					if (this.hitbox.isLeftBottom() && !this.hitbox.isRightBottom() && this.hitbox.getLeftAngle() != 0.0f)
					{
						if (this.velocity.x < 0.0f && this.hitbox.isLeftTop())
						{
							this.collidedLeft = true;
						}
						else if (this.hitbox.isRightTop())
						{
							this.collidedRight = true;
						}
					}

					if (this.hitbox.isRightBottom() && !this.hitbox.isLeftBottom() && this.hitbox.getRightAngle() != 0.0f)
					{
						if (this.velocity.x > 0.0f && this.hitbox.isRightTop())
						{
							this.collidedRight = true;
						}
						else if (this.hitbox.isLeftTop())
						{
							this.collidedLeft = true;
						}
					}
				}

				if (bounceVertically && this.shouldBounceOnGround())
				{
					this.velocity.y *= this.verticalBounceFactor;
				}

				if (this.collidedRight && this.velocity.x > 0.0f || this.collidedLeft && this.velocity.x < 0.0f)
				{
					if (bounceHorizontally && Math.abs(this.velocity.x) > 0.05f && !this.isOnGround())
					{
						this.velocity.x *= this.horizontalBounceFactor;

						bouncedHorizontally = true;
					}
					else
					{
						this.velocity.x = 0.0f;
					}
				}
			}

			if (this.checkEntityCollisions())
			{
				for (Entity entity : this.reSimulating ? MapManager.getTotalEntities() : this.level.getEntities())
				{
					if (this != entity && entity != this.mirroredEntity && entity.enabled && !entity.removed && this.canInteractWith(entity) && this.inSameDimension(entity) && this.isNearby(entity.getHitbox(), this.getAdditionalCheckDistance(entity.getVelocity())))
					{
						if (entity.isBlocked())
						{
							if (this.hitbox.intersects(entity.getHitbox()))
							{
								boolean collideTop = false,
										collideBottom = false,
										collideRight = false,
										collideLeft = false;
								boolean beforeLeft = this.collidedLeft,
										beforeRight = this.collidedRight,
										beforeAbove = this.collidedAbove,
										beforeBelow = this.collidedBelow;

								if (this.hitbox.getMaxY() < entity.getHitbox().getCenterY() && !this.collidedBelow)
								{
									if (this.velocity.y > 0.0f)
									{
										if (this.shouldBounceFromEntities() && entity.canBeBouncedOff())
										{
											this.onBounceOff(entity);
										}
										else
										{
											collideBottom = true;
										}
									}
								}
								else
								{
									if (this.hitbox.contains(entity.hitbox.getMinX() + 20, entity.hitbox.getMaxY() - 30) || this.hitbox.contains(entity.hitbox.getMaxX() - 20, entity.hitbox.getMaxY() - 30))
									{
										collideTop = true;
									}
									else
									{
										if (this.hitbox.contains(entity.hitbox.getMinX() + 18, entity.hitbox.getCenterY()))
										{
											collideRight = true;
										}
										else if (this.hitbox.contains(entity.hitbox.getMaxX() - 18, entity.hitbox.getCenterY()))
										{
											collideLeft = true;
										}
									}
								}

								if (this instanceof IEntityCollisionNotifier || entity instanceof IEntityCollisionNotifier)
								{
									((IEntityCollisionNotifier) this).onCollisionWithEntity(entity, collideTop, collideBottom, collideRight, collideLeft, false);

									if (entity instanceof IEntityCollisionNotifier)
									{
										boolean help = collideTop;
										collideTop = collideBottom;
										collideBottom = help;

										help = collideRight;
										collideRight = collideLeft;
										collideLeft = help;
									}

									if (!((IEntityCollisionNotifier) entity).onCollisionWithEntity(this, collideTop, collideBottom, collideRight, collideLeft, true))
									{
										this.collidedBelow = beforeBelow;
										this.collidedAbove = beforeAbove;
										this.collidedRight = beforeRight;
										this.collidedLeft = beforeLeft;
									}
									else if (this.checkEntitySuffocation() && this.checkSuffocation())
									{
										this.suffocating = true;
									}
								}

								if (this.collidesWithEntities() && this.shouldStopOnEntityCollision())
								{
									if (collideBottom && this.velocity.y > 0.0f || collideTop && this.velocity.y < 0.0f)
									{
										this.velocity.y = 0.0f;
									}

									if (collideRight && this.velocity.x > 0.0f || collideLeft && this.velocity.x < 0.0f)
									{
										this.velocity.x = 0.0f;
									}
								}
							}

							if (this.checkEntitySuffocation() && this.checkSuffocation() && (this.hitbox.reallyIntersects(entity.hitbox) || entity instanceof EntityProjectile && entity.getDimension() != this.level.getForegroundDimension() && entity.isMoving() && this.level.getType() == LevelType.FROZEN && this.isNearby0(entity.hitbox, 20.0f)))
							{
								this.suffocating = true;
							}
						}
					}
				}
			}
		}

		this.outOfMap = this.level.isOutOfMap(this.hitbox);

		if (!this.onClimbable && this.climbing)
		{
			this.climbing = false;
		}

		if ((this.collidedBelow || this.closeBelow || this.climbing || this.lastBelow) && !this.skipMainUpdate)
		{
			this.gravityActive = false;
			this.canFall = true;
		}

		if (this.collidedBelow && !this.jumping)
		{
			this.canFall = !(this.onRightConnectedTile && this.velocity.x > 0.0f || this.onLeftConnectedTile && this.velocity.x < 0.0f);
		}

		if ((this.level.getType() == LevelType.FROZEN || this.level.getType() == LevelType.TIME_SCALE) && this.collidedBelow && this.tileBelow != null)
		{
			if (!this.tileBelow.isTimeEffected() && this.velocity.x > 0 && this.tileBelow.getHitbox().isConnectedRight() && this.tileBelow.getRightConnectedTile() != null && !this.tileBelow.getRightConnectedTile().isTimeEffected() || this.velocity.x < 0.0f && this.tileBelow.getHitbox().isConnectedLeft() && this.tileBelow.getLeftConnectedTile() != null && !this.tileBelow.getLeftConnectedTile().isTimeEffected())
			{
				this.resumeTimeResistance = true;
			}
			else if (!this.tileBelow.isTimeEffected() && !this.tileBelow.isMoveable() && (this.velocity.x > 0 && this.velocity.x * delta + this.hitbox.getWidth() < this.hitbox.getDistanceToRightEnd() || this.velocity.x < 0 && -this.velocity.x * delta < this.hitbox.getDistanceToLeftEnd()))
			{
				this.resumeTimeResistance = true;
			}
			else
			{
				this.resumeTimeResistance = false;
			}

			if (!this.tileBelow.isTimeEffected() && this.timeEffected)
			{
				this.resumeOrAddEmitter(EmitterType.TIME_RESISTANT_AREA, this.dimension);

				delta = 0.0f;
				this.timeEffected = false;

				this.onTimeResistantStart(this.tileBelow);
			}

			if (this.tileBelow.isTimeEffected() && !this.timeEffected)
			{
				this.stopAndRemoveEmitter(EmitterType.TIME_RESISTANT_AREA, this.dimension);
				this.timeEffected = true;

				delta = this.level.getDelta(this.dimension);

				this.onTimeResistantEnd(this.tileBelow);
			}
		}

		if (!this.timeResistant && !this.collidedBelow && !this.timeEffected && !this.resumeTimeResistance)
		{
			this.stopAndRemoveEmitter(EmitterType.TIME_RESISTANT_AREA, this.dimension);
			this.timeEffected = true;

			delta = this.level.getDelta(this.dimension);
		}

		if (this.jumping)
		{
			this.canFall = true;
			this.resumeTimeResistance = false;
		}

		if (this.climbing)
		{
			this.jumping = false;
		}

		if (Math.abs(this.velocity.x) < 0.0001f)
		{
			this.velocity.x = 0.0f;
		}

		if (Math.abs(this.velocity.y) < 0.0001f)
		{
			this.velocity.y = 0.0f;
		}

		if (Math.abs(this.additionalVelocity.x) < 0.0001f)
		{
			this.additionalVelocity.x = 0.0f;
		}

		if (Math.abs(this.additionalVelocity.y) < 0.0001f)
		{
			this.additionalVelocity.y = 0.0f;
		}

		if (this.holded)
		{
			this.gravityActive = false;
		}

		this.oldVelocity.x = this.velocity.x;
		this.oldVelocity.y = this.velocity.y;

		if (pushLeft && this.collidedLeft || pushRight && this.collidedRight || pushUp && this.collidedAbove || pushDown && this.collidedBelow)
		{
			if (additionalHorizontalDelta > 0.0f && (pushLeft || pushRight) || additionalVerticalDelta > 0.0f && (pushUp || pushDown))
			{
				this.timeResistantSuffocating = true;
			}

			this.suffocating = true;
		}

		this.removeFinishedEmitters();

		if (!this.skipMainUpdate)
		{
			if (this.collidedBelow && !this.holded && this.hitbox.getHitboxBelow() != null && this.shouldPositionBeFixed())
			{
				if (!(this.tileBelow instanceof ITileBlockedFromAbove) && !(this.tileBelow instanceof ITileMoveable && ((ITileMoveable) this.tileBelow).isVerticallyMoveable()) && this.hitbox.getHitboxBelow().getRotation() % 90 == 0.0f && this.hitbox.getMinY() + this.hitbox.getHeight() - this.hitbox.getHitboxBelow().getMinY() != 0.0f && this.hitbox.getMinY() + this.hitbox.getHeight() - this.hitbox.getHitboxBelow().getMinY() < 10.0f)
				{
					this.position.y = this.hitbox.getHitboxBelow().getMinY() - this.hitbox.getHeight();
				}

				if (this.tileBelow != null && this.tileBelow.getRotation() % 90.0f != 0.0f && this.canWalkSlopes() && (this.tileBelow.getHitbox().isTiltLeft() && this.velocity.x >= 0.0f || this.tileBelow.getHitbox().isTiltRight() && this.velocity.x <= 0.0f))
				{
					otherLine.set(this.tileBelow.getHitbox().getPoints()[this.tileBelow.getHitbox().getTopLeftIndex()], this.tileBelow.getHitbox().getPoints()[this.tileBelow.getHitbox().getTopLeftIndex() + 1], this.tileBelow.getHitbox().getPoints()[this.tileBelow.getHitbox().getSecondPointIndex()], this.tileBelow.getHitbox().getPoints()[this.tileBelow.getHitbox().getSecondPointIndex() + 1]);

					line.set(this.hitbox.getPoints()[0], this.hitbox.getPoints()[1], this.hitbox.getPoints()[6], this.hitbox.getPoints()[7]);

					Vector2 intersection = line.intersect(otherLine, true);

					if (intersection != null && this.hitbox.getMaxY() - intersection.y > 0.0f)
					{
						this.position.y = intersection.y - this.hitbox.getHeight() + 0.0002f;
					}

					line.set(this.hitbox.getPoints()[2], this.hitbox.getPoints()[3], this.hitbox.getPoints()[4], this.hitbox.getPoints()[5]);

					intersection = line.intersect(otherLine, true);

					if (intersection != null && this.hitbox.getMaxY() - intersection.y > 0.0f)
					{
						this.position.y = intersection.y - this.hitbox.getHeight() + 0.0002f;
					}
				}
			}

			this.tryMoveRight = false;
			this.tryMoveLeft = false;

			if (this.checkSuffocation() && this.suffocating)
			{
				this.onSuffocation(this.timeResistantSuffocating);
			}

			this.updateEntity(delta, this.level.getRNG(this));
		}

		if (this.gravityActive && this.enabled)
		{
			if (this.velocity.y < this.maxGravity)
			{
				this.velocity.y += this.properties.getGravity(delta);
			}
			else
			{
				if (this.shooting)
				{
					this.velocity.y -= 0.00033f * delta;
				}
				else
				{
					this.velocity.y = this.maxGravity;
				}
			}
		}

		if (this.reSimulationCooldown > 0)
		{
			this.reSimulationCooldown -= delta;
		}

		this.applyFrameValues(delta, additionalHorizontalDelta, additionalVerticalDelta);

		float dx = 0.0f, dy = 0.0f;

		if (!(this.level.getType() == LevelType.TIME_TRAVEL && this.dimension == Dimension.MONOCHROME && !this.shouldHandleAsShared()))
		{
			dx = (this.velocity.x + this.oldVelocity.x) / 2.0f * delta + this.additionalVelocity.x * additionalHorizontalDelta;
			dy = (this.velocity.y + this.oldVelocity.y) / 2.0f * delta + this.additionalVelocity.y * additionalVerticalDelta;

			if (this.level.getType() == LevelType.FROZEN && this.level.getDelta(this.dimension) == 0.0f && !this.timeEffected && this.collidedBelow && this.tileBelow instanceof ITileMoveable && !this.tileBelow.isTimeEffected())
			{
				if (this.velocity.x > 0.0f == this.additionalVelocity.x > 0.0f || this.additionalVelocity.x == 0.0f)
				{
					if (dx > 0.0f && dx > this.hitbox.getDistanceToRightEnd() && this.hitbox.getMaxX() > this.tileBelow.getHitbox().getMaxX())
					{
						dx = this.hitbox.getDistanceToRightEnd();
					}
					else if (dx < 0.0f && dx < -this.hitbox.getDistanceToLeftEnd() && this.hitbox.getMinX() < this.tileBelow.getHitbox().getMinX())
					{
						dx = -this.hitbox.getDistanceToLeftEnd();
					}
				}
			}

			if (!this.skipMainUpdate)
			{
				if (pushRight && this.velocity.x < 0.0f)
				{
					dx -= this.velocity.x * delta;
				}
				else if (pushLeft && this.velocity.x > 0.0f)
				{
					dx -= this.velocity.x * delta;
				}

				if (this.collidedBelow)
				{
					if (this.hitbox.getLeftAngle() > 45 && this.hitbox.isLeftBottom() && !this.hitbox.isRightBottom())
					{
						this.velocity.x += this.properties.getAcceleration(delta) * this.hitbox.getLeftAngle() * 3.5f / 45.0f;
					}
					else if (-this.hitbox.getRightAngle() > 45 && this.hitbox.isRightBottom() && !this.hitbox.isLeftBottom())
					{
						this.velocity.x += this.properties.getAcceleration(delta) * this.hitbox.getRightAngle() * 3.5f / 45.0f;
					}
				}

				if (this.canWalkSlopes() && !bouncedHorizontally)
				{
					boolean rightOverflow = dx > this.hitbox.getDistanceToRightEnd() && this.collidedBelow && this.onRightConnectedTile && !this.hitbox.isOnCorner();
					boolean leftOverflow = dx < 0 && -dx > this.hitbox.getDistanceToLeftEnd() && this.collidedBelow && this.onLeftConnectedTile && !this.hitbox.isOnCorner();
					boolean blockedAbove = this.collidedAbove || this.closeAbove;

					float odx = rightOverflow ? dx - this.hitbox.getDistanceToRightEnd() : dx + this.hitbox.getDistanceToLeftEnd();
					float sinRight = (float) -Math.sin(Math.toRadians(-this.hitbox.getRightAngle()));
					float cosRight = (float) Math.cos(Math.toRadians(-this.hitbox.getRightAngle()));
					float sinLeft = (float) -Math.sin(Math.toRadians(-this.hitbox.getLeftAngle()));
					float cosLeft = (float) Math.cos(Math.toRadians(-this.hitbox.getLeftAngle()));

					if (this.hitbox.isLeftBottom() && !this.hitbox.isRightBottom() && this.hitbox.isCheckedRight() && dx > 0 && dx > this.hitbox.getDistanceToBlockRight() && !(this.hitbox.getHitboxBelow() != null && this.hitbox.getHitboxBelow().getHitboxRight() != null && this.hitbox.getHitboxBelow().getHitboxRight().getRotation() == 0.0f))
					{
						this.onCloseMoveToBlockRight(this.hitbox.getDistanceToBlockRight());
						dx = this.hitbox.getDistanceToBlockRight();
					}
					else if (rightOverflow && this.hitbox.getDistanceToRightEnd() != 0.0f && !this.hitbox.isLeftBottom() && !this.hitbox.getHitboxBelow().isRoughRight())
					{
						dx = this.hitbox.getDistanceToRightEnd();
					}

					if (this.hitbox.isRightBottom() && !this.hitbox.isLeftBottom() && this.hitbox.isCheckedLeft() && dx < 0 && -dx > this.hitbox.getDistanceToBlockLeft() && !(this.hitbox.getHitboxBelow() != null && this.hitbox.getHitboxBelow().getHitboxLeft() != null && this.hitbox.getHitboxBelow().getHitboxLeft().getRotation() == 0.0f))
					{
						this.onCloseMoveToBlockLeft(this.hitbox.getDistanceToBlockLeft());
						dx = this.hitbox.getDistanceToBlockLeft();
					}
					else if (leftOverflow && this.hitbox.getDistanceToLeftEnd() != 0.0f && !this.hitbox.isRightBottom() && !this.hitbox.getHitboxBelow().isRoughLeft())
					{
						dx = -this.hitbox.getDistanceToLeftEnd();
					}

					if (dx > 0 && !this.collidedRight)
					{
						if (this.hitbox.isRightBottom())
						{
							if (blockedAbove && -this.hitbox.getRightAngle() > 0.0f)
							{
								dx = 0.0f;
							}
							else
							{
								dy += dx * sinRight;
								dx *= cosRight;
							}
						}
						else if (this.hitbox.isLeftBottom() && this.hitbox.getLeftAngle() < 45 && addAdditionalDistance)
						{
							dy += dx * sinLeft;
							dx *= cosLeft;
						}
					}
					else if (dx < 0 && !this.collidedLeft)
					{
						if (this.hitbox.isLeftBottom())
						{
							if (blockedAbove && this.hitbox.getLeftAngle() > 0.0f)
							{
								dx = 0.0f;
							}
							else
							{
								dy += dx * sinLeft;
								dx *= cosLeft;
							}
						}
						else if (this.hitbox.isRightBottom() && -this.hitbox.getRightAngle() < 45 && addAdditionalDistance)
						{
							dy += dx * sinRight;
							dx *= cosRight;
						}
					}

					if (rightOverflow && this.hitbox.getHitboxBelow().getHitboxRight() != null || leftOverflow && this.hitbox.getHitboxBelow().getHitboxLeft() != null)
					{
						float rightAngle = 0.0f;
						float leftAngle = 0.0f;

						if (rightOverflow)
						{
							rightAngle = this.hitbox.getHitboxBelow().getHitboxRight().getRotation();
							rightAngle = rightAngle > 0 ? -90 - rightAngle : rightAngle;

							if (-rightAngle > 45)
							{
								rightAngle = 0.0f;
							}
						}

						if (leftOverflow)
						{
							leftAngle = this.hitbox.getHitboxBelow().getHitboxLeft().getRotation();
							leftAngle = leftAngle < 0 ? leftAngle + 90 : leftAngle;

							if (leftAngle > 45)
							{
								leftAngle = 0.0f;
							}
						}

						if (odx > 0 && !this.collidedRight)
						{
							if (this.hitbox.isRightBottom() && !blockedAbove)
							{
								dy += odx * -Math.sin(Math.toRadians(-rightAngle));
								odx *= Math.cos(Math.toRadians(-rightAngle));
							}
							else if (this.hitbox.isLeftBottom())
							{
								dy += odx * -Math.sin(Math.toRadians(-leftAngle));
								odx *= Math.cos(Math.toRadians(-leftAngle));
							}
						}
						else if (odx < 0 && !this.collidedLeft)
						{
							if (this.hitbox.isLeftBottom() && !blockedAbove)
							{
								dy += odx * -Math.sin(Math.toRadians(-leftAngle));
								odx *= Math.cos(Math.toRadians(-leftAngle));
							}
							else if (this.hitbox.isRightBottom())
							{
								dy += odx * -Math.sin(Math.toRadians(-rightAngle));
								odx *= Math.cos(Math.toRadians(rightAngle));
							}
						}

						dx += odx;
					}

					if (this.hitbox.isOnCorner() && (this.tileBelow == null || !this.tileBelow.isMoveable()))
					{
						dy -= this.hitbox.getCornerDistance();
					}
				}
			}

			if (this.canWalkSlopes() && !bouncedHorizontally)
			{
				if (this.hitbox.isCheckedBelowLeft() && this.nextTileBelowLeft.getHitbox().isTiltRight())
				{
					this.hitbox.setDistanceToBlockBelowLeft((float) (this.hitbox.getDistanceToBlockBelowLeft() + dx * Math.tan(Math.toRadians(this.nextTileBelowLeft.getRotation() % 90 < 0 ? this.nextTileBelowLeft.getRotation() % 90 + 90 : this.nextTileBelowLeft.getRotation() % 90))));

					if (this.nextTileBelowLeft == this.nextTileBelowRight)
					{
						this.hitbox.setDistanceToBlockBelowRight(this.hitbox.getDistanceToBlockBelowLeft());
					}
				}

				if (this.hitbox.isCheckedBelowRight() && this.nextTileBelowRight.getHitbox().isTiltLeft())
				{
					this.hitbox.setDistanceToBlockBelowRight((float) (this.hitbox.getDistanceToBlockBelowRight() + dx * Math.tan(Math.toRadians(this.nextTileBelowRight.getRotation() % 90 > 0 ? -90 + this.nextTileBelowRight.getRotation() % 90 : this.nextTileBelowRight.getRotation() % 90))));

					if (this.nextTileBelowLeft == this.nextTileBelowRight)
					{
						this.hitbox.setDistanceToBlockBelowLeft(this.hitbox.getDistanceToBlockBelowRight());
					}
				}
			}
		}

		this.updateScheduledEvents();

		this.dontResetVertically = false;
		this.closeBelowBefore = this.closeBelow;
		this.lastBelow = this.collidedBelow;
		this.closeBelow = false;
		this.closeAbove = false;

		float absBeforePositionX = this.position.x - this.level.getCameraOffset().x;
		float absBeforePositionY = this.position.y - this.level.getCameraOffset().y;

		if (this.level.getType() != LevelType.TIME_TRAVEL || this.dimension == Dimension.COLORED || this.shouldHandleAsShared())
		{
			this.tryMove(this.movingDistance.set(dx, dy));
		}

		this.onPostMovement(delta);

		this.updateFrameLimits(Math.max(dx != 0.0f || dy != 0.0f ? Math.max(additionalHorizontalDelta, additionalVerticalDelta) : 0.0f, delta));

		if (this.level.getType() != LevelType.FROZEN && this.level.getType() != LevelType.TIME_TRAVEL || this.level.getType() == LevelType.TIME_TRAVEL && this.dimension == Dimension.COLORED || this.level.getLevelType() == LevelType.FROZEN && (delta > 0.0f || additionalHorizontalDelta > 0.0f || additionalVerticalDelta > 0.0f) && this.shouldRecordRecentPositions())
		{
			this.updateFrameRecords(Math.max(dx != 0.0f || dy != 0.0f ? Math.max(additionalHorizontalDelta, additionalVerticalDelta) : 0.0f, delta), absBeforePositionX, absBeforePositionY);
		}

		if (!this.skipMainUpdate)
		{
			if (this.collidedBelow && !this.climbingOnGround)
			{
				this.climbing = false;
			}

			float xVelocity = Math.abs(this.velocity.x);

			if (this.level.getType() == LevelType.TIME_TRAVEL && this.dimension == Dimension.MONOCHROME && this.level.getForegroundDimension() == Dimension.COLORED)
			{
				xVelocity = this.movingDistance.x / delta;
				xVelocity -= this.additionalVelocity.x;

				if (xVelocity < 0 ^ this.movingDistance.x < 0)
				{
					xVelocity = 0;
				}
				else
				{
					xVelocity = Math.abs(xVelocity);
				}
			}

			this.lastAction = this.currentAction;

			if (!this.useAutomatedActions())
			{
				this.currentAction = this.getCustomAction();

				if (this.currentAction == null)
				{
					throw new IllegalStateException("Custom actions cannot be null when automated actions are disabled.");
				}
			}

			this.moving = this.checkForMovement();

			if (this.lastAction != this.currentAction)
			{
				this.onActionChanged(this.currentAction);

				if (this.level.getLevelType() == LevelType.TIME_TRAVEL)
				{
					this.scheduleMirroredEvent(new ScheduledEvent(CURRENT_ACTION_CHANGED, this.currentAction));
				}
			}
		}

		this.updateAnimation(delta);

		this.updateHitbox();

		if (this.dimension == this.level.getForegroundDimension() && this.hasMirror)
		{
			this.updateMirror(delta);
		}
	}

	@Override
	protected void onScheduledEvent(ScheduledEvent event)
	{
		if (event.type == CURRENT_ACTION_CHANGED)
		{
			this.changeCurrentAction((EntityAction) event.object, false);
		}

		if (event.type == GameObject.ENABLE_RENDERING_EVENT)
		{
			this.render = true;
		}
	}

	@Override
	public boolean checkPreReSimulation()
	{
		if (this.shared)
		{
			return super.checkPreReSimulation();
		}

		if (!this.hasMirror || this.mirroredEntity.removed)
		{
			this.level.removeEntity(this);
		}

		return this.hasMirror && super.checkPreReSimulation();
	}

	@Override
	protected boolean shouldCreateReSimulationParticles()
	{
		return true;
	}

	@Override
	protected boolean canChangeOnReSimulation()
	{
		return true;
	}

	@Override
	public void onReSimulation()
	{
		super.onReSimulation();

		if (!this.shared)
		{
			this.shooting = this.mirroredEntity.shooting;
		}
	}

	protected boolean shouldPositionBeFixed()
	{
		return true;
	}

	protected boolean shouldStopOnEntityCollision()
	{
		return true;
	}

	protected abstract boolean canInteractWith(Entity entity);

	@Override
	public LevelObject getMirror()
	{
		return this.mirroredEntity;
	}

	@Override
	public boolean isHolded()
	{
		return this.holded;
	}

	protected void onTimeResistantEnd(Tile tileBelow)
	{

	}

	protected void onTimeResistantStart(Tile tileBelow)
	{

	}

	protected boolean shouldUseFittingTextures()
	{
		return false;
	}

	protected boolean canInterruptAnimation(EntityAction action)
	{
		return true;
	}

	protected abstract EntityProperties getEntityProperty();

	public String getTextureDirectory()
	{
		return FilePaths.ENTITY_MOB;
	}

	public boolean hasDefaultFocus()
	{
		return false;
	}

	protected boolean useDefaultTextureLoader()
	{
		return true;
	}

	public abstract boolean checkEntityCollisions();

	public abstract boolean collidesWithEntities();

	public abstract boolean collidesWithTerrain();

	protected void onBounceOff(Entity entity)
	{
		this.velocity.y *= -0.47f;
		this.velocity.y -= 0.5f;
	}

	protected void onPostMovement(float delta)
	{

	}

	protected abstract boolean canWalkSlopes();

	protected boolean useAutomatedActions()
	{
		return true;
	}

	protected void changeCurrentAction(EntityAction newAction, boolean force)
	{
		if (force || this.currentAction.canBeInterruptedBy(newAction, this))
		{
			if (!this.supportsAction(newAction))
			{
				throw new UnsupportedOperationException("Can't change current action to " + newAction + " because it is not supported by this entity " + this + ".");
			}

			this.currentAction = newAction;
			this.images = this.availableImages.get(this.currentAction)[0];
			this.durationPerImage = this.properties.getImageDuration(this.currentAction);
			this.timeToNextImage = this.durationPerImage;
			this.currentImage = 0;

			if (this.images == null)
			{
				throw new RuntimeException("Can't change current action to " + newAction + " as there is no animation for it.");
			}
		}
	}

	private void updateAnimation(float delta)
	{
		if (this.shouldUpdateAnimation(this.currentAction) && this.currentAction.shouldUpdateAnimation(this))
		{
			this.timeToNextImage -= this.currentAction.getAnimationDelta(delta, this);

			if (this.useAutomatedActions())
			{
				int attempts = 0;

				while (!this.currentAction.shouldContinueAnimation(this))
				{
					this.changeCurrentAction(this.currentAction.getNextAction(this), false);

					if (attempts++ >= EntityAction.values().length)
					{
						Debug.warn("Failed to find proper animation for " + this + ".");

						break;
					}
				}
			}

			if (this.timeToNextImage <= 0)
			{
				if (this.currentAction.shouldReverseAnimation(this))
				{
					this.currentImage--;
				}
				else
				{
					this.currentImage++;
				}

				this.timeToNextImage = this.durationPerImage;
			}

			if (this.currentImage < 0 || this.currentImage >= this.images.length)
			{
				if (!this.useAutomatedActions() || this.currentAction.isLoopedAnimation(this))
				{
					if (this.currentImage < 0)
					{
						this.currentImage = this.images.length - 1;
					}
					else
					{
						this.currentImage = 0;
					}
				}
				else
				{
					this.changeCurrentAction(this.currentAction.getNextAction(this), false);
				}
			}

			this.currentAction.customUpdate(delta, this);

			this.setImage(this.images[this.currentImage]);
		}
	}

	protected boolean shouldUpdateAnimation(EntityAction currentAction)
	{
		return this.dimension == this.level.getForegroundDimension() || this.shared || !this.shouldHandleAsShared() || this.level.getType() == LevelType.NORMAL;
	}

	public void changeImagesTo(Entity entity)
	{
		this.availableImages = entity.availableImages;

		this.changeCurrentAction(entity.currentAction, false);

		this.image = entity.image;
	}

	public void synchronizeAnimation(Entity mirroredEntity)
	{
		if (mirroredEntity.currentAction != this.currentAction)
		{
			mirroredEntity.changeCurrentAction(this.currentAction, false);
		}

		mirroredEntity.currentImage = this.currentImage;
		mirroredEntity.setImage(mirroredEntity.images[mirroredEntity.currentImage]);
		mirroredEntity.timeToNextImage = this.timeToNextImage;
		mirroredEntity.focused = this.focused;
	}

	protected EntityAction getCustomAction()
	{
		return null;
	}

	protected void updateMirror(float delta)
	{

	}

	protected void onActionChanged(EntityAction action)
	{

	}

	public abstract boolean supportsAction(EntityAction action);

	protected boolean isBlocked(TerrainObject tile, Vector2 myVelocity)
	{
		if (tile.isBlocked())
		{
			if (tile instanceof ITileBlockedFromAbove)
			{
				if (!this.climbing && myVelocity.y >= 0.0f)
				{
					otherLine.set(tile.getHitbox().getPoints()[tile.getHitbox().getTopLeftIndex()], tile.getHitbox().getPoints()[tile.getHitbox().getTopLeftIndex() + 1], tile.getHitbox().getPoints()[tile.getHitbox().getSecondPointIndex()], tile.getHitbox().getPoints()[tile.getHitbox().getSecondPointIndex() + 1]);

					line.set(this.hitbox.getPoints()[0], this.hitbox.getPoints()[1], this.hitbox.getPoints()[6], this.hitbox.getPoints()[7]);

					Vector2 intersection = line.intersect(otherLine, true);

					if (intersection != null && this.hitbox.getMaxY() - intersection.y > 1.0f)
					{
						return false;
					}

					line.set(this.hitbox.getPoints()[2], this.hitbox.getPoints()[3], this.hitbox.getPoints()[4], this.hitbox.getPoints()[5]);

					intersection = line.intersect(otherLine, true);

					if (intersection != null && this.hitbox.getMaxY() - intersection.y > 1.0f)
					{
						return false;
					}

					return true;
				}
			}
			else
			{
				return true;
			}
		}

		return false;
	}

	protected boolean setEntityAction(EntityAction action)
	{
		if (this.currentAction != action)
		{
			this.changeCurrentAction(action, false);

			return true;
		}

		return false;
	}

	protected boolean shouldRecordRecentPositions()
	{
		return true;
	}

	protected boolean shouldDrawRecentPositions()
	{
		return true;
	}

	public boolean checkSuffocation()
	{
		return false;
	}

	public boolean checkAdditionalSuffocation()
	{
		return false;
	}

	public boolean checkEntitySuffocation()
	{
		return false;
	}

	protected boolean canBeBouncedOff()
	{
		return false;
	}

	protected boolean isGravityAffected()
	{
		return true;
	}

	protected float getFrictionFactor()
	{
		return 0.8f;
	}

	protected void onTerrainNearby(TerrainObject object, Vector2 distance)
	{
	}

	protected int getTileCheckRadius()
	{
		return 5;
	}

	protected boolean notifyOnNearbyTerrain()
	{
		return false;
	}

	protected boolean shouldBounceOnGround()
	{
		return false;
	}

	protected boolean isNearby(Hitbox other, float additionalCheckDistance)
	{
		return this.isNearby0(other, Hitbox.MIN_CHECK_DISTANCE + additionalCheckDistance);
	}

	private boolean isNearby0(Hitbox other, float checkDistance)
	{
		if (other.getMinX() > this.hitbox.getMaxX() + checkDistance || other.getMaxX() < this.hitbox.getMinX() - checkDistance)
		{
			return false;
		}

		if (other.getMinY() > this.hitbox.getMaxY() + checkDistance || other.getMaxY() < this.hitbox.getMinY() - checkDistance)
		{
			return false;
		}

		return true;
	}

	protected float getAdditionalCheckDistance(Vector2 otherVelocity)
	{
		return (Math.abs(this.velocity.x) + Math.abs(this.velocity.y) + Math.abs(otherVelocity.x) + Math.abs(otherVelocity.y)) * 50.0f; // magic
	}

	@Override
	protected void onDimensionChanged(Dimension newDimension)
	{
		this.images = this.availableImages.get(this.currentAction)[0];

		this.setImage(this.images[this.currentImage]);
	}

	@Override
	public void updateHitbox()
	{
		this.hitbox.setLocation(this.position.x, this.position.y);
		this.hitbox.update();
	}

	@Override
	public boolean isRotateable()
	{
		return false;
	}

	@Override
	public EntityLiving getHoldingEntity()
	{
		return this.holdingEntity;
	}

	public TextureRegion getImage(Dimension dimension)
	{
		return this.availableImages.get(this.currentAction)[0][this.currentImage];
	}

	@Override
	public void draw(Batch batch)
	{
		this.draw(batch, this.dimension);
	}

	@Override
	public void draw(Batch batch, Dimension dimension)
	{
		if (!this.hidden && this.isRendered())
		{
			Renderer.useSpriteBatch();

			this.drawEmitters(false, false, true, dimension, batch);

			if (this.renderImage)
			{
				if (!this.shared)
				{
					this.drawTextured(batch);
				}
				else
				{
					this.level.drawAsShared(this, batch);
				}
			}

			this.drawEmitters(true, false, true, dimension, batch);

			this.customDraw(batch);

			// drawTimeTravelDebug(batch);

			// drawScheduledEvents(batch);
		}
	}

	private void drawTextured(Batch batch)
	{
		if (this.level.areDimensionOffset() && this.level.getType() == LevelType.NORMAL && this.level.getForegroundDimension() == this.dimension && !this.shared)
		{
			this.drawTextured(this.dimension.getOpposite(), batch);
		}

		this.drawTextured(this.dimension, batch);

		if (this.level.areDimensionOffset() && this.level.getType() == LevelType.NORMAL && this.level.getForegroundDimension() == this.dimension.getOpposite() && !this.shared)
		{
			this.drawTextured(this.dimension.getOpposite(), batch);
		}
	}

	private void drawTextured(Dimension dimension, Batch batch)
	{
		Color color = this.getDrawColor(this.level.getType() == LevelType.NORMAL ? this.level.getForegroundDimension() : dimension, colorBuffer);

		if (this.level.getType() == LevelType.NORMAL)
		{
			color.a = 1.0f;
		}

		Renderer.setGrayScaleFactor(this.level.getType() == LevelType.NORMAL && !this.level.areDimensionOffset() ? this.level.getDimensionFactor(Dimension.MONOCHROME) : dimension == Dimension.COLORED ? 0.0f : 1.0f);

		if (this.level.isFading())
		{
			color.a *= this.level.getFadingAlpha();
		}

		color.a *= this.level.getControlAlpha();

		color = this.customModifyDrawColor(color);

		ColorHelper.copy(color, colorBuffer);

		TextureRegion image = this.getImage(dimension);
		boolean passedOnSharedness = this.holdingEntity != null && this.holdingEntity.shared || this.holdingEntity instanceof EntityPlayer;
		boolean inRightDimension = this.dimension == this.level.getForegroundDimension() || this.dimension == this.level.getBackgroundDimension() && this.level.isTransitioning();
		boolean lel = this.timeEffected && this.level.getControllingPlayer() != null && this.level.getControllingPlayer().isRawTimeEffected();

		if (this.reSimulationCooldown > 0 || LevelManager.isCheckpointing() && (!this.holded || this.holdingEntity.type != EntityType.PLAYER) || this.level.getLevelType() == LevelType.FROZEN && this.shouldDrawRecentPositions() && !passedOnSharedness && inRightDimension && lel)
		{
			int trailFrames = (int) (this.level.getType() == LevelType.FROZEN && this.shouldDrawRecentPositions() ? 16 : this.reSimulationCooldown > 0 ? (float) this.reSimulationCooldown / (float) DEFAULT_RE_SIMULATION_COOLDOWN * 24 : 16 * LevelManager.getCheckpointingFactor());

			if (this.level.getType() == LevelType.FROZEN && this.level.isTransitioning())
			{
				trailFrames *= this.dimension == this.level.getBackgroundDimension() ? LevelManager.getTransitionFactor() : 1.0f - LevelManager.getTransitionFactor();
			}

			this.drawTrailImage(image, trailFrames, 8, dimension, ColorHelper.copy(colorBuffer, ColorHelper.secondBuffer), 0.4f, batch);
		}

		this.drawImage(image, dimension, this.animationVersion, this.position.x, this.position.y, colorBuffer, batch);
	}

	@Override
	protected Color getFinalDrawColor()
	{
		return ColorHelper.multiplyAndCopy(this.customModifyDrawColor(ColorHelper.applyAlphaAndCopy(this.getDrawColor(this.dimension, ColorHelper.buffer), this.level.getDimensionColor(this.dimension).a)), this.shared ? this.level.getFinalSharedColor() : Color.WHITE);
	}

	@Override
	public void drawImageOffset(TextureRegion image, Dimension dimension, Color color, float xOffset, float yOffset, Batch batch)
	{
		this.drawImage(image, dimension, this.rotation, this.position.x + xOffset, this.position.y + yOffset, color, batch);
	}

	@Override
	protected final void drawImage(TextureRegion image, Dimension dimension, float rotation, float x, float y, Color color, Batch batch)
	{
		x = x + this.hitbox.getRawWidth() / 2 - image.getRegionWidth() / 2;
		y = y + this.hitbox.getRawHeight() - image.getRegionHeight();

		image.flip(this.focused ^ this.hasDefaultFocus() ^ image.isFlipX(), false);

		if (this.shouldShatter())
		{
			x += this.level.getXOffset(dimension);
			y += this.level.getYOffset(dimension);
		}

		batch.setColor(color);

		batch.draw(image, x, y, this.image.getRegionWidth() / 2, this.image.getRegionHeight() / 2, this.image.getRegionWidth(), this.image.getRegionHeight(), 1.0f, 1.0f, rotation);
	}

	protected boolean shouldShatter()
	{
		return true;
	}

	protected Color getDrawColor(Dimension dimension, Color colorBuffer)
	{
		ColorHelper.copy(this.level.getDimensionColor(dimension), colorBuffer);

		if (!this.hasMirror || this.position.x != this.mirroredEntity.position.x || this.position.y != this.mirroredEntity.position.y)
		{
			return colorBuffer;
		}
		else
		{
			colorBuffer.a = this.level.getWrapperAlpha(dimension);

			return colorBuffer;
		}
	}

	protected void customDraw(Batch batch)
	{

	}

	public void jump()
	{
		this.velocity.y -= this.getJumpSpeed();
	}

	public Vector2 getDistanceTo(Entity entity)
	{
		bufferedVector.set(entity.getPosition());
		bufferedVector.sub(this.position);

		return bufferedVector;
	}

	@Override
	public void removeMirror()
	{
		if (this.hasMirror)
		{
			this.level.removeEntity(this.mirroredEntity);
			this.hasMirror = false;
			this.mirroredEntity = null;
		}
	}

	public void setVelocity(float x, float y)
	{
		this.velocity.x = x;
		this.velocity.y = y;
	}

	protected void onSuffocation(boolean timeResistantSuffocation)
	{

	}

	public void readMetadata()
	{

	}

	public void writeMetadata()
	{

	}

	public void setFocused(boolean focused)
	{
		this.focused = focused;
	}

	public void setMirroredEntity(Entity mirroredEntity)
	{
		this.mirroredEntity = mirroredEntity;
		this.hasMirror = this.mirroredEntity != null;

		if (this.hasMirror && !this.mirroredEntity.hasMirror)
		{
			this.mirroredEntity.mirroredEntity = this;
			this.mirroredEntity.hasMirror = true;
		}
	}

	public boolean isSuffocating()
	{
		return this.suffocating;
	}

	protected boolean shouldBePushedByTiles()
	{
		return true;
	}

	protected boolean shouldBounceFromEntities()
	{
		return false;
	}

	public boolean shouldRenderInBatch()
	{
		return true;
	}

	public int getMaxJumpHeight()
	{
		return 3;
	}

	public void setMetadata(String metadata)
	{
		this.metadata = metadata;

		this.readMetadata();
	}

	public float getMaxX()
	{
		return this.hitbox.getMaxX();
	}

	public float getMinX()
	{
		return this.hitbox.getMinX();
	}

	public float getMaxY()
	{
		return this.hitbox.getMaxY();
	}

	public float getMinY()
	{
		return this.hitbox.getMinY();
	}

	public float getAbsDistanceTo(Entity entity)
	{
		return Math.abs(this.getDistanceTo(entity).len());
	}

	protected float getJumpSpeed()
	{
		return 0.15f;
	}

	protected float getAnimatedDelta(float delta, Vector2 velocity)
	{
		return delta;
	}

	public boolean isOnClimbable()
	{
		return this.onClimbable;
	}

	public boolean isImageFocused()
	{
		return this.isLookingRight;
	}

	public boolean isOnGround()
	{
		return this.collidedBelow || this.closeBelow || this.lastBelow;
	}

	public boolean isClimbing()
	{
		return this.climbing;
	}

	public boolean isWalking()
	{
		return (this.isOnGround() || Math.abs(this.velocity.y) < 0.15f) && Math.abs(this.velocity.x) > this.properties.getMaxSpeed() / 10.0f;
	}

	public void setMaxSpeed(float speed)
	{
		this.properties.setMaxSpeed(speed);
	}

	public void setLookingRight(boolean isLookingRight)
	{
		this.isLookingRight = isLookingRight;
	}

	public boolean checkForMovement()
	{
		return this.movingDistance.len() > 0.05f;
	}

	public boolean isBlockedAbove()
	{
		return this.collidedAbove;
	}

	public boolean isBlockedBelow()
	{
		return this.collidedBelow;
	}

	public void setBlockedBelow(boolean blockedBelow)
	{
		this.collidedBelow = blockedBelow;
	}

	public boolean isBlockedLeft()
	{
		return this.collidedLeft;
	}

	public boolean isBlockedRight()
	{
		return this.collidedRight;
	}

	public boolean isFocused()
	{
		return this.focused;
	}

	public boolean isGravityActive()
	{
		return this.gravityActive;
	}

	public boolean isOutOfMap()
	{
		return this.outOfMap;
	}

	public EntityProperties getProperties()
	{
		return this.properties;
	}

	@Override
	public Vector2 getVelocity()
	{
		return this.velocity;
	}

	public void setVelocity(Vector2 velocity)
	{
		this.velocity = velocity;
	}

	@Override
	public Hitbox getHitbox()
	{
		return this.hitbox;
	}

	public void setHitbox(Hitbox hitbox)
	{
		this.hitbox = hitbox;
	}

	public boolean isLookingRight()
	{
		return this.isLookingRight;
	}

	public boolean isMoving()
	{
		return this.moving;
	}

	public EntityType getType()
	{
		return this.type;
	}

	public boolean isInteractable()
	{
		return this instanceof IEntityInteractable;
	}

	public EntityAction getCurrentAction()
	{
		return this.currentAction;
	}

	public boolean isOnLadder()
	{
		return this.onLadder;
	}

	public Entity getMirroredEntity()
	{
		return this.mirroredEntity;
	}

	public boolean isJumping()
	{
		return this.jumping;
	}

	public String getMetadata()
	{
		return this.metadata;
	}

	@Override
	public void setRotation(float rotation)
	{
		this.rotation = rotation;
	}

	@Override
	public boolean hasMirror()
	{
		return this.hasMirror;
	}

	public void setSkipMainUpdate(boolean skipMainUpdate)
	{
		this.skipMainUpdate = skipMainUpdate;
	}

	public boolean isSkipMainUpdate()
	{
		return this.skipMainUpdate;
	}

	public void setRender(boolean render)
	{
		this.render = render;
	}

	public boolean isRendered()
	{
		return this.render;
	}

	@Override
	public float getRotation()
	{
		return this.rotation;
	}

	public float getCenterX()
	{
		return this.hitbox.getCenterX();
	}

	public float getCenterY()
	{
		return this.hitbox.getCenterY();
	}

	public boolean justLanded()
	{
		return this.collidedBelow && !this.lastBelow;
	}

	public boolean isRawTimeEffected()
	{
		return this.timeEffected;
	}

	public Tile getTileBelow()
	{
		return this.tileBelow;
	}

	public boolean isOnPassableTile()
	{
		return this.onPassableTile;
	}
}