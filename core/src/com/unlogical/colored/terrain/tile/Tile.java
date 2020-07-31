package com.unlogical.colored.terrain.tile;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.configuration.DisplayConstants;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.particle.AttractorConfiguration;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.particle.IEmitterWrapper.ICustomEmitterInit;
import com.unlogical.colored.particle.ParticleEmitter;
import com.unlogical.colored.terrain.TerrainObject;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.terrain.tile.cage.TileWoodenCage;
import com.unlogical.colored.terrain.tile.cannon.TileCannon;
import com.unlogical.colored.terrain.tile.logical.TileKeyGate;
import com.unlogical.colored.terrain.tile.logical.TileLever;
import com.unlogical.colored.terrain.tile.logical.TileLeverGate;
import com.unlogical.colored.terrain.tile.platform.TileCloudPlatform;
import com.unlogical.colored.terrain.tile.platform.TilePlatform;
import com.unlogical.colored.terrain.tile.properties.ITileBlockedFromAbove;
import com.unlogical.colored.terrain.tile.properties.ITileInteractable;
import com.unlogical.colored.terrain.tile.properties.ITileIntersectionNotifier;
import com.unlogical.colored.terrain.tile.properties.ITileMirrorable;
import com.unlogical.colored.terrain.tile.properties.ITileMoveable;
import com.unlogical.colored.terrain.tile.properties.ITileReceiver;
import com.unlogical.colored.terrain.tile.properties.ITileSender;
import com.unlogical.colored.terrain.tile.properties.ITileTriggerable;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.GameObject;
import com.unlogical.colored.util.GameStatus;
import com.unlogical.colored.util.ICopyable;
import com.unlogical.colored.util.IShareable;
import com.unlogical.colored.util.LevelObject;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ScheduledEvent;

public abstract strictfp class Tile extends TerrainObject implements ICopyable<Tile>, IShareable<Tile>
{
	public static final int MAX_BORDER_WIDTH = 15;
	public static final int MIN_BORDER_WIDTH = 5;

	public static final Color borderColor = new Color(1.0f, 0.84f, 0.0f, 0.41f);
	public static final Color hoverColor = new Color(1.0f, 0.92f, 0.0f, 0.6f);
	public static final Color blockedColor = new Color(1.0f, 0.6f, 0.0f, 0.15f);

	protected int receivePort = -1;
	protected int sendPort = -1;
	protected int renderPriority = 1;

	protected boolean automaticHitbox = true;
	protected boolean intersectionNotifier;
	protected boolean gravityActive;
	protected boolean moving;
	protected boolean blocksDimensionTravel;
	protected boolean hasAttractableEmitters;
	protected boolean timeEffected = true;
	protected boolean sending;

	private boolean triggered;
	private boolean mirrorable;
	private boolean leftConnectionLock;
	private boolean rightConnectionLock;

	private float latestDelta;

	protected long age;

	private Hitbox[] borders;

	protected Tile mirroredTile;

	private Tile leftConnectedTile;
	private Tile rightConnectedTile;

	public Tile(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, innerType, position, level, properties, dimension);

		this.dimension = dimension;
		this.metadata = metadata;

		this.intersectionNotifier = this instanceof ITileIntersectionNotifier;

		if (rotation != this.rotation)
		{
			this.setRotation(rotation);
		}

		this.readMetadata();

		this.mirrorable = this instanceof ITileMirrorable;
		this.dimensionUnique = this.mirrorable;

		if (this.shouldCreateMirror(allowMirrors))
		{
			this.createMirror(level.getLevelType());
		}

		this.checkResized();
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		Tile copy = (Tile) super.deepCopy(copiedReferences);

		copy.receivePort = this.receivePort;
		copy.sendPort = this.sendPort;
		copy.renderPriority = this.renderPriority;
		copy.automaticHitbox = this.automaticHitbox;
		copy.intersectionNotifier = this.intersectionNotifier;
		copy.gravityActive = this.gravityActive;
		copy.moving = this.moving;
		copy.blocksDimensionTravel = this.blocksDimensionTravel;
		copy.hasAttractableEmitters = this.hasAttractableEmitters;
		copy.timeEffected = this.timeEffected;
		copy.sending = this.sending;
		copy.triggered = this.triggered;
		copy.mirrorable = this.mirrorable;
		copy.leftConnectionLock = this.leftConnectionLock;
		copy.rightConnectionLock = this.rightConnectionLock;
		copy.latestDelta = this.latestDelta;
		copy.age = this.age;
		copy.mirroredTile = (Tile) Level.getCopy(this.mirroredTile, copiedReferences);
		copy.leftConnectedTile = (Tile) Level.getCopy(this.leftConnectedTile, copiedReferences);
		copy.rightConnectedTile = (Tile) Level.getCopy(this.rightConnectedTile, copiedReferences);

		return copy;
	}

	@Override
	public void createMirror(LevelType levelType)
	{
		this.mirroredTile = Tile.createTile(this.type, this.metadata, this.innerType, 0.0f, this.position.cpy(), this.level, Dimension.MONOCHROME, this.properties.copy(), true);
		this.mirroredTile.setHidden(this.hidden);
		this.mirroredTile.setWidth(this.hitbox.getRawWidth());
		this.mirroredTile.setHeight(this.hitbox.getRawHeight());
		this.mirroredTile.setRotation(this.rotation);
		this.mirroredTile.mirroredTile = this;

		this.level.addTile(this.mirroredTile);
	}

	@Override
	public final boolean shouldCreateMirror(boolean allowMirrors)
	{
		return this.level.shouldCreateMirrors() && allowMirrors && this.dimension == Dimension.COLORED && this.mirrorable && !this.shared;
	}

	@Override
	public LevelObject getMirror()
	{
		return this.mirroredTile;
	}

	@Override
	public void init()
	{
		this.checkResized();

		this.initConnections();

		if (this.hasMirror() && this.dimension == Dimension.COLORED)
		{
			this.initMirror(this.mirroredTile, this.level.getType());
		}
	}

	public void initConnections()
	{
		if (this.isBlocked())
		{
			this.updateHitbox();

			this.hitbox.findTopLeftIndex();

			this.hitbox.setConnectedLeft(false);
			this.hitbox.setConnectedRight(false);

			this.hitbox.setRoughLeft(false);
			this.hitbox.setRoughRight(false);

			float[] points = this.hitbox.getPoints();

			// so.. so... sorry :(
			for (int y = 0; y < this.level.getTiles().size(); y++)
			{
				Tile tile = this.level.getTiles().get(y);

				if (tile.isEnabled() && this != tile && tile.isBlocked() && (this.inSameDimension(tile) || !tile.mirrorable || !this.mirrorable))
				{
					if (!(this.hitbox.getMaxX() < tile.hitbox.getMinX() || this.hitbox.getMinX() > tile.hitbox.getMaxX() || this.hitbox.getMaxY() < tile.getHitbox().getMinY() || this.hitbox.getMinY() > tile.getHitbox().getMaxY()))
					{
						float[] otherPoints = tile.getHitbox().getPoints();

						for (int i = 0; i < points.length; i += 2)
						{
							for (int j = 0; j < tile.getHitbox().getPoints().length; j += 2)
							{
								if (points[i + 1] != this.hitbox.getMaxY() && (i == this.hitbox.getTopLeftIndex() || i == this.hitbox.getSecondPointIndex()) && otherPoints[j + 1] != tile.hitbox.getMaxY())
								{
									float horizontalDifference = points[i] - tile.getHitbox().getPoints()[j];

									if (Math.abs(points[i + 1] - otherPoints[j + 1]) < 0.5f || (this.isMoveable() || tile.isMoveable()) && Math.abs(points[i + 1] - otherPoints[j + 1]) < 1.0f)
									{
										if (this.hitbox.getCenterX() < tile.hitbox.getCenterX() && (Math.abs(horizontalDifference) < 0.5f || (this.isMoveable() || tile.isMoveable()) && horizontalDifference > 0.0f && horizontalDifference < 5.0f))
										{
											float rightRotation = (float) Math.toDegrees(Math.atan((otherPoints[tile.hitbox.nextPoint(j) + 1] - otherPoints[j + 1]) / (otherPoints[tile.hitbox.nextPoint(j)] - otherPoints[j])));
											float ownRotation = (float) Math.toDegrees(Math.atan((points[i + 1] - points[this.hitbox.nextPoint(i, 3) + 1]) / (points[i] - points[this.hitbox.nextPoint(i, 3)])));

											if (Math.abs(rightRotation) < 45 && Math.abs(ownRotation) < 45)
											{
												this.hitbox.setConnectedRight(true);
												this.hitbox.setHitboxRight(tile.hitbox);

												this.hitbox.setTopLeftIndex(this.hitbox.previousPoint(i));

												this.rightConnectedTile = tile;

												float leftDifference = points[i + 1] - points[this.hitbox.nextPoint(i, 3) + 1];
												float rightDifference = otherPoints[tile.hitbox.nextPoint(j) + 1] - otherPoints[j + 1];

												if (leftDifference < 0 ^ rightDifference < 0 && leftDifference != 0.0f && rightDifference != 0.0f)
												{
													this.hitbox.setRoughRight(true);
												}
											}
										}
										else if (this.hitbox.getCenterX() > tile.hitbox.getCenterX() && (horizontalDifference == 0.0f || (this.isMoveable() || tile.isMoveable()) && horizontalDifference < 0.0f && horizontalDifference > -5.0f))
										{
											float leftRotation = (float) Math.toDegrees(Math.atan((otherPoints[tile.hitbox.nextPoint(j, 3) + 1] - otherPoints[j + 1]) / (otherPoints[tile.hitbox.nextPoint(j, 3)] - otherPoints[j])));
											float ownRotation = (float) Math.toDegrees(Math.atan((points[this.hitbox.nextPoint(i) + 1] - points[i + 1]) / (points[this.hitbox.nextPoint(i)] - points[i])));

											if (Math.abs(ownRotation) < 45 && Math.abs(leftRotation) < 45)
											{
												this.hitbox.setConnectedLeft(true);
												this.hitbox.setHitboxLeft(tile.hitbox);

												this.hitbox.setTopLeftIndex(i);

												this.leftConnectedTile = tile;

												float leftDifference = otherPoints[j + 1] - otherPoints[tile.hitbox.nextPoint(j, 3) + 1];
												float rightDifference = points[tile.hitbox.nextPoint(i) + 1] - points[i + 1];

												if (leftDifference < 0 ^ rightDifference < 0 && leftDifference != 0.f && rightDifference != 0.0f)
												{
													this.hitbox.setRoughLeft(true);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public static Tile createDefaultTile(TileType type, Vector2 position, Level level)
	{
		return Tile.createTile(type, "", 0, 0, position, level, Dimension.COLORED, TileType.getProperty(type).copy(), true);
	}

	@Override
	public Tile shallowCopy(Level level)
	{
		return this.copy(level);
	}

	@Override
	public Tile copy(Level level)
	{
		this.writeMetadata();

		Tile copy = Tile.createTile(this.type, this.metadata, this.innerType, 0.0f, this.position.cpy(), level, this.dimension, this.properties.copy(), false);

		copy.updateInnerType();
		copy.setWidth(this.hitbox.getRawWidth());
		copy.setHeight(this.hitbox.getRawHeight());
		copy.setRotation(this.rotation);
		copy.setShared(this.shared);
		copy.setTimeEffected(this.timeEffected);
		copy.setBlocksDimensionTravel(this.blocksDimensionTravel);
		copy.readMetadata();

		return copy;
	}

	@Override
	public void initMirror(Tile mirroredTile, LevelType levelType)
	{
		this.mirroredTile.wasCreatedFromFile = this.wasCreatedFromFile;
	}

	@Override
	public void setTimeEffected(boolean timeEffected)
	{
		if (this.timeEffected ^ timeEffected)
		{
			this.timeEffected = timeEffected;

			if (this.timeEffected)
			{
				if (this.isEmitterOnSurface())
				{
					this.removeEmitter(EmitterType.TIME_RESISTANT_SURFACE, this.dimension);
					this.removeEmitter(EmitterType.TIME_RESISTANT_SURFACE, this.dimension.getOpposite());
				}
				else
				{
					this.removeEmitter(EmitterType.TIME_RESISTANT_AREA, this.dimension);
					this.removeEmitter(EmitterType.TIME_RESISTANT_AREA, this.dimension.getOpposite());
				}
			}
			else
			{
				if (this.isEmitterOnSurface())
				{
					this.addEmitter(EmitterType.TIME_RESISTANT_SURFACE, this.dimension, (int) this.hitbox.getRawWidth(), (int) this.hitbox.getRawHeight(), null);
				}
				else
				{
					this.addEmitter(EmitterType.TIME_RESISTANT_AREA, this.dimension, (int) this.hitbox.getRawWidth(), (int) this.hitbox.getRawHeight(), null);
				}
			}
		}
	}

	@Override
	public void onSharingChanged()
	{
		if (this.shared)
		{
			this.removeMirror();
			this.addEmitter(EmitterType.SHARED, this.dimension, (int) this.hitbox.getRawWidth(), (int) this.hitbox.getRawHeight(), null);
		}
		else
		{
			this.createMirror(this.level.getLevelType());
			this.mirroredTile.init();
			this.initMirror(this.mirroredTile, this.level.getLevelType());
			this.removeEmitter(EmitterType.SHARED, this.dimension);
		}
	}

	public void setBlocksDimensionTravel(boolean blocksDimensionTravel)
	{
		if (this.blocksDimensionTravel == blocksDimensionTravel ? false : ((this.blocksDimensionTravel = blocksDimensionTravel) || true) && this.canBlockDimensionTravel())
		{

		}
	}

	protected boolean canBlockDimensionTravel()
	{
		return true;
	}

	@Override
	protected ParticleEmitter addEmitter(EmitterType type, Dimension dimension, Vector2 forcedPosition, int width, int height, int maxParticles, ICustomEmitterInit customEmitterInit)
	{
		return this.addEmitter(type, dimension, forcedPosition, width, height, maxParticles, false, 0, customEmitterInit);
	}

	@Override
	protected ParticleEmitter addEmitter(EmitterType type, Dimension dimension, Vector2 forcedPosition, int width, int height, int maxParticles, boolean forceRotation, int forcedRotation, ICustomEmitterInit customEmitterInit)
	{
		if (type.useAttractors)
		{
			this.hasAttractableEmitters = true;
		}

		ParticleEmitter emitter = super.addEmitter(type, dimension, forcedPosition, width, height, maxParticles, forceRotation, forcedRotation, customEmitterInit);

		if (this.level.shouldCreateMirrors() && !this.mirrorable && this.isChangeable())
		{
			super.addEmitter(type, dimension.getOpposite(), forcedPosition, width, height, maxParticles, forceRotation, forcedRotation, customEmitterInit);
		}

		return emitter;
	}

	@Override
	protected void removeEmitter(EmitterType type, Dimension dimension)
	{
		super.removeEmitter(type, dimension);

		this.checkAttractableEmitters();
	}

	public void addAttractor(Hitbox hitbox, AttractorConfiguration configuration)
	{
		if (this.hasAttractableEmitters)
		{
			for (EmitterType type : this.particleEffects.keySet())
			{
				for (ParticleEmitter emitter : this.particleEffects.get(type))
				{
					if (emitter.getType().useAttractors)
					{
						emitter.addAttractor(this, configuration);
					}
				}
			}
		}
	}

	public void removeAttractor(Hitbox hitbox)
	{
		if (this.hasAttractableEmitters)
		{
			for (EmitterType type : this.particleEffects.keySet())
			{
				for (ParticleEmitter emitter : this.particleEffects.get(type))
				{
					if (emitter.getType().useAttractors)
					{
						emitter.removeAttractor(this);
					}
				}
			}
		}
	}

	protected void checkAttractableEmitters()
	{
		this.hasAttractableEmitters = false;

		for (EmitterType type : this.particleEffects.keySet())
		{
			for (ParticleEmitter emitter : this.particleEffects.get(type))
			{
				if (emitter.getType().useAttractors)
				{
					this.hasAttractableEmitters = true;

					return;
				}
			}
		}
	}

	public boolean hasAttractableEmitters()
	{
		return this.hasAttractableEmitters;
	}

	public void tryMove(Vector2 distance)
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
				}
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
				}
			}
		}

		if (distance.y > 0)
		{
			if (!this.collidedBelow || this.hitbox.isLeftBottom() ^ this.hitbox.isRightBottom())
			{
				float distanceToBlockBelow = 0.0f;

				if (distance.x > 0 && this.hitbox.getLeftAngle() != 0)
				{
					distanceToBlockBelow = this.hitbox.getDistanceToBlockBelowRight();
				}
				else if (distance.x < 0 && this.hitbox.getRightAngle() != 0)
				{
					distanceToBlockBelow = this.hitbox.getDistanceToBlockBelowLeft();
				}
				else
				{
					distanceToBlockBelow = this.hitbox.getDistanceToBlockBelow();
				}

				if (!(this.hitbox.isCheckedBelowLeft() || this.hitbox.isCheckedBelowRight()) || distance.y <= distanceToBlockBelow)
				{
					this.setY(this.position.y + distance.y);
				}
				else
				{
					this.setY(this.position.y + distanceToBlockBelow);

					this.collidedBelow = true;
					this.gravityActive = false;

					distance.y = distanceToBlockBelow;
				}
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
					this.collidedAbove = true;

					distance.y = -this.hitbox.getDistanceToBlockAbove();
				}
			}
		}
	}

	public final void update(float delta)
	{
		this.hitbox.resetCache();

		this.latestDelta = delta;
		this.age += delta;

		this.collidedAbove = false;
		this.collidedBelow = false;
		this.collidedLeft = false;
		this.collidedRight = false;

		this.gravityActive = this.isGravityEffected();

		if (this.collidesWithTerrain())
		{
			for (int i = 0; i < this.level.getTiles().size(); i++)
			{
				Tile tile = this.level.getTiles().get(i);

				if (tile.isEnabled() && this != tile && tile != this.mirroredTile && this.isNearby(tile.getHitbox(), this.getAdditionalCheckDistance(tile.getVelocity())) && this.inSameDimension(tile))
				{
					if (this.isBlocked(tile, this.velocity))
					{
						if (this.stopOnCollisionWith(tile))
						{
							this.hitbox.checkCollision(tile.getHitbox(), this.getAdditionalCheckDistance(tile.getVelocity()), false);
						}

						if (this.hitbox.collides(tile.getHitbox(), false))
						{
							boolean beforeRight = this.collidedRight,
									beforeLeft = this.collidedLeft;
							boolean beforeAbove = this.collidedAbove,
									beforeBelow = this.collidedBelow;

							if (!(tile instanceof ITileBlockedFromAbove))
							{
								if (this.hitbox.collidedRight() && !(tile instanceof ITileMoveable && ((ITileMoveable) tile).isVerticallyMoveable() && tile.getHitbox().getMaxY() - this.hitbox.getMinY() < 1.0f))
								{
									this.collidedRight = true;
								}

								if (this.hitbox.collidedLeft() && !(tile instanceof ITileMoveable && ((ITileMoveable) tile).isVerticallyMoveable() && tile.getHitbox().getMaxY() - this.hitbox.getMinY() < 1.0f))
								{
									this.collidedLeft = true;
								}

								if (this.hitbox.collidedTop())
								{
									this.collidedAbove = true;
								}
							}

							if (this.hitbox.collidedBottom())
							{
								this.collidedBelow = true;

								this.gravityActive = false;
							}

							if (this.stopOnCollisionWith(tile))
							{
								if (this.collidedRight && this.velocity.x > 0.0f || this.collidedLeft && this.velocity.x < 0.0f)
								{
									this.velocity.x = 0.0f;
								}

								if (this.collidedAbove && this.velocity.y < 0.0f || this.collidedBelow && this.velocity.y > 0.0f)
								{
									this.velocity.y = 0.0f;
								}
							}
							else
							{
								this.collidedLeft = beforeLeft;
								this.collidedRight = beforeRight;
								this.collidedAbove = beforeAbove;
								this.collidedBelow = beforeBelow;
							}
						}
					}
				}
			}
		}

		this.outOfMap = this.level.isOutOfMap(this.hitbox);

		if (this.collidedBelow)
		{
			this.gravityActive = false;
		}

		if (this.gravityActive)
		{
			if (this.velocity.y < this.getMaxGravity())
			{
				this.velocity.y += this.getGravityFactor();
			}
			else
			{
				this.velocity.y = this.getMaxGravity();
			}
		}

		if (Math.abs(this.velocity.x) < 0.0001f)
		{
			this.velocity.x = 0.0f;
		}

		if (Math.abs(this.velocity.y) < 0.0001f)
		{
			this.velocity.y = 0.0f;
		}

		this.oldVelocity.set(this.velocity.x, this.velocity.y);

		if (this.reSimulationCooldown > 0)
		{
			this.reSimulationCooldown -= delta;
		}

		this.onUpdate(delta, this.level.getRNG(this));

		if (this.isAnimated())
		{
			this.updateAnimation(delta);
		}

		this.updateScheduledEvents();

		if (this.level.getType() != LevelType.FROZEN || this.shouldRecordRecentPositions())
		{
			this.updateFrameLimits(delta);
		}

		this.removeFinishedEmitters();

		if (this instanceof ITileMoveable)
		{
			this.movingDistance.set(((this.velocity.x + this.oldVelocity.x) / 2.0f + this.additionalVelocity.x) * delta, ((this.velocity.y + this.oldVelocity.y) / 2.0f + this.additionalVelocity.y) * delta);

			float absBeforePositionX = this.position.x - this.level.getCameraOffset().x;
			float absBeforePositionY = this.position.y - this.level.getCameraOffset().y;

			this.applyFrameValues(delta, 0.0f, 0.0f);

			if ((this.level.getType() != LevelType.TIME_TRAVEL || this.shared || this.dimension == Dimension.COLORED) && this.shouldMove())
			{
				if (this.movingDistance.len() != 0.0f)
				{
					this.lockConnections();

					this.tryMove(this.movingDistance);

					if (this.movingDistance.len() != 0.0f)
					{
						this.updateConnections();
					}
				}
			}

			if (this.level.getType() != LevelType.TIME_TRAVEL && this.level.getType() != LevelType.FROZEN || this.level.getType() == LevelType.TIME_TRAVEL && this.dimension == Dimension.COLORED || this.level.getLevelType() == LevelType.FROZEN && this.shouldRecordRecentPositions() && delta > 0.0f)
			{
				this.updateFrameRecords(delta, absBeforePositionX, absBeforePositionY);
			}

			if (this.movingDistance.len() > 0.0f)
			{
				this.moving = true;
			}

			if (this.moving && this.particleEffects != null)
			{
				for (Set<ParticleEmitter> emitterSet : this.particleEffects.values())
				{
					for (ParticleEmitter emitter : emitterSet)
					{
						emitter.moveEmitter(this.movingDistance.x, this.movingDistance.y);
					}
				}
			}

			if (this.outOfMap)
			{
				this.level.removeTile(this);
			}
		}

		if (this.automaticHitbox)
		{
			this.updateHitbox();
		}

		if (this.dimensionUnique && this.dimension == this.level.getForegroundDimension())
		{
			this.updateMirror(delta);
		}
	}

	protected boolean stopOnCollisionWith(Tile tile)
	{
		return true;
	}

	@Override
	protected void preDraw(Color color, Dimension dimension, Batch batch)
	{
		this.drawEmitters(false, false, true, dimension, batch);

		if (this.shouldUseStencilledEmitters())
		{
			this.enableStencilDrawing(batch);
		}
	}

	@Override
	protected void customDraw(Dimension dimension, Batch batch)
	{
		if (this.shouldUseStencilledEmitters())
		{
			this.enableStencilCheck(batch);

			this.drawEmitters(true, true, false, dimension, batch);

			this.disableStencilCheck(batch);

			this.drawEmitters(true, false, false, dimension, batch);
		}
		else
		{
			this.drawEmitters(true, false, true, dimension, batch);
		}
	}

	@Override
	protected Color getDrawColor(Dimension dimension, Color colorBuffer)
	{
		ColorHelper.copy(this.level.getDimensionColor(dimension), colorBuffer);

		if (!this.hasMirror() || this.position.x != this.mirroredTile.position.x || this.position.y != this.mirroredTile.position.y || this instanceof ITileMirrorable && !((ITileMirrorable) this).renderStatic())
		{
			return colorBuffer;
		}
		else
		{
			colorBuffer.a = this.level.getWrapperAlpha(dimension);

			return colorBuffer;
		}
	}

	public void changeRotationTo(float rotation)
	{
		this.lockConnections();
		this.setRotation(rotation);
		this.updateConnections();
	}

	protected void lockConnections()
	{
		this.leftConnectionLock = this.hitbox.isConnectedLeft();
		this.rightConnectionLock = this.hitbox.isConnectedRight();
	}

	protected void updateConnections()
	{
		this.initConnections();

		if (this.leftConnectionLock ^ this.hitbox.isConnectedLeft())
		{
			this.leftConnectedTile.initConnections();
		}

		if (this.rightConnectionLock ^ this.hitbox.isConnectedRight())
		{
			this.rightConnectedTile.initConnections();
		}
	}

	@Override
	protected void onScheduledEvent(ScheduledEvent event)
	{
		if (event.type == GameObject.ENABLE_RENDERING_EVENT)
		{
			this.enabled = true;
		}
	}

	protected boolean shouldRecord()
	{
		return this.level.getType() == LevelType.TIME_TRAVEL && this.dimension == Dimension.COLORED && !this.shared && GameLauncher.getStatus() != GameStatus.MAP_EDITOR;
	}

	protected boolean isBlocked(TerrainObject tile, Vector2 myVelocity)
	{
		if (tile.isBlocked())
		{
			if (tile instanceof ITileBlockedFromAbove)
			{
				return myVelocity.y >= 0.0f;
			}
			else
			{
				return true;
			}
		}

		return false;
	}

	private final void updateMirror(float delta)
	{

	}

	public boolean blocksOnStanding()
	{
		return this.properties.isBlocked() && this.blocksDimensionTravel;
	}

	public boolean blocksOnClimbing()
	{
		return this.properties.isClimbable() && this.blocksDimensionTravel;
	}

	public boolean shouldRecordRecentPositions()
	{
		return true;
	}

	public boolean shouldDrawRecentPositions()
	{
		return true;
	}

	public boolean isEmitterOnSurface()
	{
		return this.properties.isBlocked();
	}

	public abstract boolean shouldUpdate();

	protected boolean collidesWithTerrain()
	{
		return false;
	}

	public void onUpdate(float delta, Random rng)
	{

	}

	protected boolean shouldUseStencilledEmitters()
	{
		return false;
	}

	protected boolean shouldUseFriction()
	{
		return false;
	}

	protected float getFrictionFactor()
	{
		return 0.7f;
	}

	protected boolean isGravityEffected()
	{
		return false;
	}

	protected float getGravityFactor()
	{
		return 0.015f;
	}

	protected float getMaxGravity()
	{
		return 0.6f;
	}

	public final void trigger(boolean triggered, boolean hasInversedMirror, Tile sender)
	{
		if (this instanceof ITileReceiver)
		{
			this.setTriggered(triggered);

			if (triggered)
			{
				((ITileReceiver) this).onTrigger((ITileSender) sender, hasInversedMirror);
			}
			else
			{
				((ITileReceiver) this).onUntrigger((ITileSender) sender, hasInversedMirror);
			}
		}
	}

	public void trigger()
	{
		this.startSending(this.sendPort);
	}

	protected final void startSending(int sendPort)
	{
		if (sendPort >= 0 && sendPort <= ITileTriggerable.TOTAL_PORTS)
		{
			this.sending = true;

			for (Tile tile : this.level.getTiles())
			{
				if (tile != this && this.inSameDimension(tile) && tile.isSender() && tile.getSendPort() == sendPort)
				{
					((ITileSender) tile).onPortStateChanged(true);
				}
			}

			for (Tile tile : this.level.getTiles())
			{
				if (this.inSameDimension(tile) && tile.isReceiver() && tile.getReceivePort() == sendPort)
				{
					tile.trigger(true, this.hasMirror() && !this.mirroredTile.sending, this);
				}
			}
		}
	}

	protected final void startSending()
	{
		this.startSending(this.sendPort);
	}

	protected final void stopSending()
	{
		this.sending = false;

		for (int i = 0; i < this.level.getTiles().size(); i++)
		{
			Tile tile = this.level.getTiles().get(i);

			if (tile != this && this.inSameDimension(tile) && tile.isSender() && tile.getSendPort() == this.sendPort)
			{
				((ITileSender) tile).onPortStateChanged(false);
			}
		}

		for (int i = 0; i < this.level.getTiles().size(); i++)
		{
			Tile tile = this.level.getTiles().get(i);

			if (this.inSameDimension(tile) && tile.isReceiver() && tile.getReceivePort() == this.sendPort)
			{
				tile.listen();
			}
		}
	}

	public void listen()
	{
		Tile sender = this.listen(this.receivePort);

		this.trigger(sender != null, sender != null && sender.hasMirror() && sender.sending != sender.getMirroredTile().sending, sender);
	}

	protected Tile listen(int receivePort)
	{
		Tile sender = null;

		if (this instanceof ITileTriggerable)
		{
			if (receivePort >= 0 && receivePort <= ITileTriggerable.TOTAL_PORTS)
			{
				for (int i = 0; i < this.level.getTiles().size(); i++)
				{
					Tile tile = this.level.getTiles().get(i);

					if (this.inSameDimension(tile) && tile instanceof ITileSender)
					{
						if (tile.isTriggering() && tile.getSendPort() == receivePort)
						{
							sender = tile;
						}
					}
				}
			}
		}

		return sender;
	}

	public int getPort()
	{
		return this instanceof ITileSender ? this.sendPort : this.receivePort;
	}

	public void setPort(int port)
	{
		if (this instanceof ITileSender)
		{
			this.sendPort = port;
		}
		else
		{
			this.receivePort = port;
		}
	}

	protected boolean isNearby(Hitbox other, float additionalCheckDistance)
	{
		if (other.getMinX() > this.hitbox.getMaxX() + Hitbox.MIN_CHECK_DISTANCE + additionalCheckDistance || other.getMaxX() < this.hitbox.getMinX() - (Hitbox.MIN_CHECK_DISTANCE + additionalCheckDistance))
		{
			return false;
		}

		if (other.getMinY() > this.hitbox.getMaxY() + Hitbox.MIN_CHECK_DISTANCE + additionalCheckDistance || other.getMaxY() < this.hitbox.getMinY() - (Hitbox.MIN_CHECK_DISTANCE + additionalCheckDistance))
		{
			return false;
		}

		return true;
	}

	protected float getAdditionalCheckDistance(Vector2 otherVelocity)
	{
		return this.velocity.len() + otherVelocity.len() * 20;
	}

	public static Tile createTile(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		if (type == TileType.BASIC_TILE)
		{
			return new TileBase(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.SPAWNPOINT)
		{
			return new TileSpawnpoint(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.CAMERABLOCK)
		{
			return new TileCameraControl(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.BASIC_BLOCKEDABOVE)
		{
			return new TileBlockedAbove(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.GRID)
		{
			return new TileGrid(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.LADDER)
		{
			return new TileLadder(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.LEVER)
		{
			return new TileLever(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.PLATFORM || type == TileType.PLATFORM_LEFT_EDGE || type == TileType.PLATFORM_RIGHT_EDGE)
		{
			return new TilePlatform(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.CLOUDPLATFORM)
		{
			return new TileCloudPlatform(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.SPIKES)
		{
			return new TileSpike(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.KEYGATE)
		{
			return new TileKeyGate(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.LEVERGATE)
		{
			return new TileLeverGate(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.DOOR)
		{
			return new TileDoor(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.HOLE)
		{
			return new TileHole(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.WORLD_DOOR)
		{
			return new TileWorldDoor(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.WOODEN_CAGE)
		{
			return new TileWoodenCage(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.CANNON)
		{
			return new TileCannon(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.LIGHT)
		{
			return new TileLight(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.SOUND_SOURCE)
		{
			return new TileSoundSource(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.TEXT)
		{
			return new TileText(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}
		else if (type == TileType.MARKER)
		{
			return new TileMarker(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
		}

		return null;
	}

	public void writeMetadata()
	{

	}

	public void readMetadata()
	{

	}

	public boolean hasBorders()
	{
		return this.borders != null;
	}

	public final void createBorders()
	{
		if (!this.hasBorders())
		{
			this.borders = new Hitbox[4];

			this.borders[0] = new Hitbox(0.0f, 0.0f, 0.0f, 0.0f);
			this.borders[1] = new Hitbox(0.0f, 0.0f, 0.0f, 0.0f);
			this.borders[2] = new Hitbox(0.0f, 0.0f, 0.0f, 0.0f);
			this.borders[3] = new Hitbox(0.0f, 0.0f, 0.0f, 0.0f);

			this.updateBorders();
		}
	}

	public void updateBorders()
	{
		if (!this.hasBorders())
		{
			this.createBorders();
		}

		float borderFactor = Math.min(this.isHorizontallyResizable() ? this.hitbox.getRawWidth() : DisplayConstants.TILE_SIZE * 3, this.isVerticallyResizable() ? this.hitbox.getRawHeight() : DisplayConstants.TILE_SIZE * 3) / (DisplayConstants.TILE_SIZE * 3);

		if (borderFactor > 1.0f)
		{
			borderFactor = 1.0f;
		}

		float borderWidth = MIN_BORDER_WIDTH + (MAX_BORDER_WIDTH - MIN_BORDER_WIDTH) * borderFactor;

		this.borders[0].set(this.position.x, this.position.y, this.hitbox.getRawWidth(), borderWidth);
		this.borders[1].set(this.position.x + this.hitbox.getRawWidth() - borderWidth, this.position.y, borderWidth, this.hitbox.getRawHeight());
		this.borders[2].set(this.position.x, this.position.y + this.hitbox.getRawHeight() - borderWidth, this.hitbox.getRawWidth(), borderWidth);
		this.borders[3].set(this.position.x, this.position.y, borderWidth, this.hitbox.getRawHeight());

		Hitbox.rotate(this.borders, this.getCenterOfRotationX(), this.getCenterOfRotationY(), this.rotation);
	}

	public void mouseDragged(int oldX, int oldY, int newX, int newY, float mouseXChange, float mouseYChange)
	{
		float minSize = DisplayConstants.TILE_SIZE / 2.0f;

		Vector2 absMouseChange = new Vector2(mouseXChange, mouseYChange);
		absMouseChange.setAngle(absMouseChange.angle() - this.rotation);

		mouseXChange = absMouseChange.x;
		mouseYChange = absMouseChange.y;

		float actRotation = this.rotation;

		boolean rotateTop = this.isVerticallyResizable() && this.borders[0].contains(oldX, oldY) && (this.hitbox.getRawHeight() - mouseYChange >= minSize || mouseYChange < 0);
		boolean rotateRight = this.isHorizontallyResizable() && this.borders[1].contains(oldX, oldY) && (this.hitbox.getRawWidth() - mouseXChange >= minSize || mouseXChange > 0);
		boolean rotateBottom = this.isVerticallyResizable() && this.borders[2].contains(oldX, oldY) && (this.hitbox.getRawHeight() - mouseYChange >= minSize || mouseYChange > 0);
		boolean rotateLeft = this.isHorizontallyResizable() && this.borders[3].contains(oldX, oldY) && (this.hitbox.getRawWidth() - mouseXChange >= minSize || mouseXChange < 0);

		float[] points = this.hitbox.getPoints();

		int pIndex = -1;

		if (rotateTop)
		{
			if (rotateRight)
			{
				pIndex = 1;
			}
			else
			{
				pIndex = 0;
			}
		}
		else if (rotateBottom)
		{
			if (rotateRight)
			{
				pIndex = 2;
			}
			else if (rotateLeft)
			{
				pIndex = 3;
			}
			else
			{
				pIndex = 3;
			}
		}
		else if (rotateRight)
		{
			pIndex = 1;
		}
		else if (rotateLeft)
		{
			pIndex = 0;
		}

		if (pIndex >= 0)
		{
			pIndex = (pIndex + points.length / 4) % (points.length / 2);

			Vector2 referencePos = new Vector2(points[pIndex * 2], points[pIndex * 2 + 1]);

			this.setRotation(0.0f);

			if (rotateTop)
			{
				this.adjustY(mouseYChange);
				this.adjustHeight(-mouseYChange);
			}

			if (rotateRight)
			{
				this.adjustWidth(mouseXChange);
			}

			if (rotateBottom)
			{
				this.adjustHeight(mouseYChange);
			}

			if (rotateLeft)
			{
				this.adjustX(mouseXChange);
				this.adjustWidth(-mouseXChange);
			}

			this.setRotation(actRotation);

			Vector2 newReferencePos = new Vector2(points[pIndex * 2], points[pIndex * 2 + 1]);

			newReferencePos.sub(referencePos);

			this.adjustX(-newReferencePos.x);
			this.adjustY(-newReferencePos.y);

			this.updateBorders();
		}
	}

	public void drawBorders(Batch batch)
	{
		for (int i = 0; i < this.borders.length; i++)
		{
			ShapeRenderer sr = Renderer.useShapeRenderer();

			Hitbox border = this.borders[i];

			sr.set(ShapeType.Filled);

			if (this.isHorizontallyResizable() && i % 2 == 1 || this.isVerticallyResizable() && i % 2 == 0)
			{
				if (border.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
				{
					sr.setColor(hoverColor);
				}
				else
				{
					sr.setColor(borderColor);
				}

				Renderer.drawPolygonAsTriangles(this.borders[i].getPoints(), sr);
			}
		}
	}

	public void adjustWidth(float widthChange)
	{
		this.setWidth(this.hitbox.getWidth() + widthChange);
		this.hitbox.update();
	}

	public void adjustHeight(float heightChange)
	{
		this.setHeight(this.hitbox.getHeight() + heightChange);
		this.hitbox.update();
	}

	public void setWidth(float width)
	{
		if (this.isHorizontallyResizable())
		{
			this.hitbox.setWidth(width);
		}
		else
		{
			this.hitbox.setWidth(this.image.getRegionWidth());
		}

		this.checkResized();
	}

	public void setHeight(float height)
	{
		if (this.isVerticallyResizable())
		{
			this.hitbox.setHeight(height);
		}
		else
		{
			this.hitbox.setHeight(this.image.getRegionHeight());
		}

		this.checkResized();
	}

	protected boolean isHorizontallyResizable()
	{
		return this.properties.isHorizontalResizable();
	}

	protected boolean isVerticallyResizable()
	{
		return this.properties.isVerticalResizable();
	}

	public void checkResized()
	{
		this.resized = !this.hasTextures || (int) this.hitbox.getRawHeight() != this.image.getRegionHeight() || (int) this.hitbox.getRawWidth() != this.image.getRegionWidth();
	}

	public boolean bordersContain(int mouseX, int mouseY)
	{
		for (int i = 0; i < this.borders.length; i++)
		{
			if (this.borders[i].contains(mouseX, mouseY) && (i % 2 == 1 && this.isHorizontallyResizable() || i % 2 == 0 && this.isVerticallyResizable()))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void removeMirror()
	{
		if (this.hasMirror())
		{
			this.level.removeTile(this.mirroredTile);
			this.mirroredTile = null;
		}
	}

	@Override
	protected boolean shouldCreateParticles()
	{
		return !this.reSimulating || !(this instanceof ITileMoveable) || this.moving;
	}

	@Override
	protected boolean shouldCreateReSimulationParticles()
	{
		return this.isMoveable();
	}

	@Override
	protected boolean canChangeOnReSimulation()
	{
		return this.isChangeable();
	}

	@Override
	public boolean shouldReSimulate()
	{
		return this.isChangeable();
	}

	public void disconnectLeft(Tile tile)
	{
		this.hitbox.setConnectedLeft(false);
		this.hitbox.setHitboxLeft(null);

		this.leftConnectedTile = null;
	}

	public void disconnectRight(Tile tile)
	{
		this.hitbox.setConnectedRight(false);
		this.hitbox.setHitboxRight(null);

		this.rightConnectedTile = null;
	}

	@Override
	protected final void onRotationChanged()
	{
		if (this.particleEffects != null)
		{
			for (Set<ParticleEmitter> emitterSet : this.particleEffects.values())
			{
				for (ParticleEmitter emitter : emitterSet)
				{
					emitter.getWrapper().setRotation(this.rotation);
				}
			}
		}
	}

	@Override
	public boolean shouldUseOriginalTextures()
	{
		return this.mirrorable;
	}

	public void resetPositionToStart()
	{
		this.setX(this.positionRecordX[this.level.timeStepHandler.nextFrameStart] + this.level.getCameraOffset().x);
		this.setY(this.positionRecordY[this.level.timeStepHandler.nextFrameStart] + this.level.getCameraOffset().y);
	}

	public boolean shouldRenderInGame()
	{
		return true;
	}

	public float onRotationOverflow(float newRotation)
	{
		return 90.0f * (newRotation < 0 ? -1 : 1);
	}

	public boolean shouldMove()
	{
		return true;
	}

	public boolean entitiesBounceHorizontally()
	{
		return true;
	}

	@Override
	public boolean hasMirror()
	{
		return this.mirroredTile != null;
	}

	public boolean isDimensionUnique()
	{
		return this.dimensionUnique;
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

	public boolean isInteractable()
	{
		return this instanceof ITileInteractable;
	}

	public String getMetadata()
	{
		return this.metadata;
	}

	public void setMetadata(String metadata)
	{
		this.metadata = metadata;
	}

	@Override
	public boolean isMoveable()
	{
		return this instanceof ITileMoveable;
	}

	public int getRenderPriority()
	{
		return this.renderPriority;
	}

	public void setRenderPriority(int renderPriority)
	{
		this.renderPriority = renderPriority;
	}

	public boolean isMoving()
	{
		return this.moving;
	}

	public boolean isSender()
	{
		return this instanceof ITileSender;
	}

	public boolean isReceiver()
	{
		return this instanceof ITileReceiver;
	}

	public boolean isTriggered()
	{
		return this.triggered;
	}

	public boolean isTriggering()
	{
		return this.sending;
	}

	public int getReceivePort()
	{
		return this.receivePort;
	}

	public int getSendPort()
	{
		return this.sendPort;
	}

	public float getWidth()
	{
		return this.hitbox.getWidth();
	}

	public float getHeight()
	{
		return this.hitbox.getHeight();
	}

	public Tile getMirroredTile()
	{
		return this.mirroredTile;
	}

	public boolean isIntersectionNotifier()
	{
		return this.intersectionNotifier;
	}

	public void setTriggered(boolean triggered)
	{
		this.triggered = triggered;
	}

	public boolean isOutOfMap()
	{
		return this.outOfMap;
	}

	@Override
	public boolean isChangeable()
	{
		return this.mirrorable && ((ITileMirrorable) this).isReallyChangeable();
	}

	public boolean isCollidedLeft()
	{
		return this.collidedLeft;
	}

	public boolean isCollidedRight()
	{
		return this.collidedRight;
	}

	public Tile getLeftConnectedTile()
	{
		return this.leftConnectedTile;
	}

	public Tile getRightConnectedTile()
	{
		return this.rightConnectedTile;
	}

	public float getCenterX()
	{
		return this.hitbox.getCenterX();
	}

	public float getCenterY()
	{
		return this.hitbox.getCenterY();
	}

	public boolean blocksDimensionTravel()
	{
		return this.blocksDimensionTravel;
	}

	@Override
	public boolean isTimeEffected()
	{
		return this.timeEffected && !this.level.ignoresTimeEffects();
	}

	public float getLatestDelta()
	{
		return this.latestDelta;
	}

	public void setMirroredTile(Tile mirroredTile)
	{
		this.mirroredTile = mirroredTile;

		if (this.hasMirror() && !this.mirroredTile.hasMirror())
		{
			this.mirroredTile.mirroredTile = this;
		}
	}

	public boolean isRawTimeEffected()
	{
		return this.timeEffected;
	}
}
