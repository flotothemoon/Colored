package com.unlogical.colored.particle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.collision.Point;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.ICopyable;
import com.unlogical.colored.util.LevelObject;
import com.unlogical.colored.wrapper.WrapperTexture;

public class ParticleEmitterImpl extends ParticleEmitter implements ICopyable<ParticleEmitterImpl>
{
	private static Vector2 buffer = new Vector2();

	private ParticleEmitterImpl mirroredEmitter;

	private IEmitterWrapper wrapper;

	private LinkedHashMap<LevelObject, AttractorConfiguration> attractors;
	private Set<LevelObject> particleEaters;

	private EmitterType type;

	private IntegerRange spawnInterval = new IntegerRange(500, 1000);
	private IntegerRange spawnCount = new IntegerRange(1, 3);

	private IntegerRange wobbleInterval = new IntegerRange(4000, 6000);

	private IntegerRange initialLife = new IntegerRange(15000, 20000);
	private IntegerRange initalSize = new IntegerRange(40, 60);

	private FloatRange xOffset = new FloatRange(0.0f, 1.0f);
	private FloatRange yOffset = new FloatRange(0.0f, 1.0f);

	private FloatRange velocityX = new FloatRange(0.0f, 0.0f);
	private FloatRange velocityY = new FloatRange(0.0f, 0.0f);

	private FloatRange startRotation = new FloatRange(0.0f, 0.0f);
	private FloatRange rotationVelocity = new FloatRange(0.0f, 0.0f);

	private FloatRange growthFactor = new FloatRange(0.0f, 0.0f);

	private FloatRange gravityFactor = new FloatRange(0.0f, 0.0f);
	private FloatRange windFactor = new FloatRange(0.0f, 0.0f);

	private IntegerRange startAlpha = new IntegerRange(255, 255);

	private IntegerRange fadeOutTime = new IntegerRange(4000, 6000);
	private IntegerRange fadeInTime = new IntegerRange(2000, 3000);

	private float trailFactor = 1.0f;

	private float newMinX = Float.MIN_VALUE;
	private float newMaxX = Float.MAX_VALUE;
	private float newMinY = Float.MIN_VALUE;
	private float newMaxY = Float.MAX_VALUE;
	private float minX = Float.MIN_VALUE;
	private float maxX = Float.MAX_VALUE;
	private float minY = Float.MIN_VALUE;
	private float maxY = Float.MAX_VALUE;

	private boolean boundToWrapperTexture;
	private boolean stopped;
	private boolean hasMirror;
	private boolean shouldFadeOut = true;
	private boolean shouldFadeIn = true;
	private boolean limitToWrapper = true;
	private boolean rotateAccelerations = true;
	private boolean tightBounce;
	private boolean shouldBounceOutOfArea;
	private boolean timeLimitedLifetime;
	private boolean useAttractors;
	private boolean scaleWithWidth;
	private boolean scaleWithHeight;
	private boolean maxOutParticles = true;
	private boolean circularEmitter;
	private boolean circularInversed;
	private boolean wobbleMode;
	private boolean wasUpdated;

	private int renderMode = RENDER_IDENTICAL;
	private int scissoredDirections;

	private int lifeTime = 1000;
	private int life = this.lifeTime;

	private int originalMaxParticles;
	private int originalMinCount;
	private int originalMaxCount;
	private int originalWidth;
	private int originalHeight;

	private int nextSpawn;

	public ParticleEmitterImpl(Dimension dimension, IEmitterWrapper wrapper)
	{
		super(dimension, wrapper.getLevel().getParticleSystem(), wrapper.getEmitterSprites(), wrapper.getMaxParticles());

		this.wrapper = wrapper;
		this.dimension = dimension;
		this.boundToWrapperTexture = wrapper instanceof WrapperTexture;

		this.enable();
		this.ensureManualRendering();
	}

	public ParticleEmitterImpl(Dimension dimension, IEmitterWrapper wrapper, EmitterType type)
	{
		this(dimension, wrapper, type.spawnInterval, type.scaleWithWidth || type.scaleWithHeight ? type.spawnCount.copy() : type.spawnCount, type.initialLife, type.initalSize, type.wobbleInterval, type.xOffset, type.yOffset, type.velocityX.copy(), type.velocityY.copy(), type.startRotation, type.rotationVelocity, type.growthFactor, type.gravityFactor, type.windFactor, type.startAlpha, type.fadeInTime, type.fadeOutTime, type.lifeTime, type.shouldFadeIn, type.shouldFadeOut, type.limitToWrapper, type.tightBounce, type.shouldBounceOutOfArea, type.hasLimitedLifetime, type.useAttractors, type.rotateAccelerations, type.maxOutParticles, type.circularEmitter, RENDER_IDENTICAL, (int) ((type.scaleWithWidth || type.scaleWithHeight ? type.getScaleFactor(wrapper.getEmitterWidth(), wrapper.getEmitterHeight()) : 1.0f) * type.maxParticles));

		if ((type.scaleWithWidth || type.scaleWithHeight) && wrapper.shouldApplySpawnScaleFactor(type))
		{
			this.spawnCount.minValue = Math.round(this.spawnCount.minValue * type.getScaleFactor(wrapper.getEmitterWidth(), wrapper.getEmitterHeight()));
			this.spawnCount.maxValue = Math.round(this.spawnCount.maxValue * type.getScaleFactor(wrapper.getEmitterWidth(), wrapper.getEmitterHeight()));
		}

		this.trailFactor = type.trailFactor;
		this.type = type;
		this.scaleWithWidth = type.scaleWithWidth;
		this.scaleWithHeight = type.scaleWithHeight;
		this.originalMinCount = type.spawnCount.minValue;
		this.originalMaxCount = type.spawnCount.maxValue;
		this.originalWidth = type.originalWidth;
		this.originalHeight = type.originalHeight;
		this.renderMode = type.renderMode;
		this.additive = type.additiveMode;
		this.circularInversed = type.circularInversed;
		this.wobbleMode = type.wobbleFactor;

		wrapper.applyCustomInit(this);
	}

	public ParticleEmitterImpl(Dimension dimension, IEmitterWrapper wrapper, IntegerRange spawnInterval, IntegerRange spawnCount, IntegerRange initalLife, IntegerRange initalSize, IntegerRange wobbleInterval, FloatRange xOffset, FloatRange yOffset, FloatRange velocityX, FloatRange velocityY, FloatRange startRotation, FloatRange rotationVelocity, FloatRange growthFactor, FloatRange gravityFactor, FloatRange windFactor, IntegerRange startAlpha, IntegerRange fadeInTime, IntegerRange fadeOutTime, int lifeTime, boolean shouldFadeIn, boolean shouldFadeOut, boolean shouldKillOutOfWrapper, boolean tightBounce, boolean shouldBounceOutOfWrapper, boolean hasLimitedLifetime, boolean useAttractors, boolean rotateAccelerations, boolean maxOutParticles, boolean circularEmitter, int renderMode, int maxParticles)
	{
		super(dimension, wrapper.getLevel().getParticleSystem(), wrapper.getEmitterSprites(), maxParticles);

		this.init(dimension, wrapper, spawnInterval, spawnCount, initalLife, initalSize, wobbleInterval, xOffset, yOffset, velocityX, velocityY, startRotation, rotationVelocity, growthFactor, gravityFactor, windFactor, startAlpha, fadeInTime, fadeOutTime, lifeTime, shouldFadeIn, shouldFadeOut, shouldKillOutOfWrapper, shouldBounceOutOfWrapper, hasLimitedLifetime, useAttractors, rotateAccelerations, tightBounce, maxOutParticles, circularEmitter, renderMode);
	}

	private ParticleEmitterImpl()
	{
		super(null, null, null, 0);
	}

	private void init(Dimension dimension, IEmitterWrapper wrapper, IntegerRange spawnInterval, IntegerRange spawnCount, IntegerRange initalLife, IntegerRange initalSize, IntegerRange wobbleInterval, FloatRange xOffset, FloatRange yOffset, FloatRange velocityX, FloatRange velocityY, FloatRange startRotation, FloatRange rotationVelocity, FloatRange growthFactor, FloatRange gravityFactor, FloatRange windFactor, IntegerRange startAlpha, IntegerRange fadeInTime, IntegerRange fadeOutTime, int lifeTime, boolean shouldFadeIn, boolean shouldFadeOut, boolean shouldKillOutOfWrapper, boolean shouldBounceOutOfWrapper, boolean hasLimitedLifetime, boolean useAttractors, boolean rotateAccelerations, boolean tightBounce, boolean maxOutParticles, boolean circularEmitter, int renderMode)
	{
		this.wrapper = wrapper;
		this.spawnInterval = spawnInterval;
		this.spawnCount = spawnCount;
		this.initialLife = initalLife;
		this.initalSize = initalSize;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.startRotation = startRotation;
		this.rotationVelocity = rotationVelocity;
		this.growthFactor = growthFactor;
		this.gravityFactor = gravityFactor;
		this.windFactor = windFactor;
		this.startAlpha = startAlpha.copy(); // TODO ugly hack because I don't
												// want to set up a parameter
												// for this right now
		this.fadeOutTime = fadeOutTime;
		this.fadeInTime = fadeInTime;
		this.lifeTime = this.life = lifeTime;
		this.shouldFadeIn = shouldFadeIn;
		this.shouldFadeOut = shouldFadeOut;
		this.limitToWrapper = shouldKillOutOfWrapper;
		this.shouldBounceOutOfArea = shouldBounceOutOfWrapper;
		this.timeLimitedLifetime = hasLimitedLifetime;
		this.dimension = dimension;
		this.useAttractors = useAttractors;
		this.boundToWrapperTexture = wrapper instanceof WrapperTexture;
		this.rotateAccelerations = rotateAccelerations;
		this.maxOutParticles = maxOutParticles;
		this.circularEmitter = circularEmitter;
		this.tightBounce = tightBounce;

		this.enable();
		this.ensureManualRendering();

		if (this.useAttractors)
		{
			this.attractors = new LinkedHashMap<LevelObject, AttractorConfiguration>(4);
		}
	}

	@Override
	public ParticleEmitter deepCopy(Map<Object, Object> copiedReferences)
	{
		ParticleEmitterImpl copy = new ParticleEmitterImpl();

		Level.putCopy(this, copy, copiedReferences);

		copy.system = (ParticleSystem) Level.getCopy(this.system, copiedReferences);
		copy.images = this.images;
		copy.maxParticles = this.maxParticles;
		copy.emitterXOffset = this.emitterXOffset;
		copy.emitterYOffset = this.emitterYOffset;
		copy.enabled = this.enabled;
		copy.renderViaSystem = this.renderViaSystem;
		copy.stencilled = this.stencilled;
		copy.init(this.dimension, (IEmitterWrapper) Level.getCopy(this.wrapper, copiedReferences), copy(this.spawnInterval), copy(this.spawnCount), copy(this.initialLife), copy(this.initalSize), copy(this.wobbleInterval), copy(this.xOffset), copy(this.yOffset), copy(this.velocityX), copy(this.velocityY), copy(this.startRotation), copy(this.rotationVelocity), copy(this.growthFactor), copy(this.gravityFactor), copy(this.windFactor), copy(this.startAlpha), copy(this.fadeInTime), copy(this.fadeOutTime), this.lifeTime, this.shouldFadeIn, this.shouldFadeOut, this.limitToWrapper, this.shouldBounceOutOfArea, this.timeLimitedLifetime, this.useAttractors, this.rotateAccelerations, this.tightBounce, this.maxOutParticles, this.circularEmitter, this.renderMode);
		copy.mirroredEmitter = (ParticleEmitterImpl) Level.getCopy(this.mirroredEmitter, copiedReferences);
		copy.attractors = new LinkedHashMap<LevelObject, AttractorConfiguration>();

		if (this.attractors != null)
		{
			for (LevelObject object : this.attractors.keySet())
			{
				copy.attractors.put((LevelObject) Level.getCopy(object, copiedReferences), this.attractors.get(object));
			}
		}

		Level.putCopy(this.attractors, copy.attractors, copiedReferences);

		copy.particleEaters = this.particleEaters != null ? new HashSet<LevelObject>() : null;
		copy.particleEaters = this.particleEaters == null ? null : (Set<LevelObject>) Level.deepCopyCollection(this.particleEaters, copy.particleEaters, copiedReferences);
		copy.type = this.type;

		copy.trailFactor = this.trailFactor;

		copy.stopped = this.stopped;
		copy.hasMirror = this.hasMirror;
		copy.renderMode = this.renderMode;
		copy.additive = this.additive;
		copy.life = this.life;
		copy.lifeTime = this.lifeTime;
		copy.nextSpawn = this.nextSpawn;

		copy.originalMaxParticles = this.originalMaxParticles;
		copy.originalMinCount = this.originalMinCount;
		copy.originalMaxCount = this.originalMaxCount;
		copy.originalWidth = this.originalWidth;
		copy.originalHeight = this.originalHeight;
		copy.circularInversed = this.circularInversed;
		copy.wobbleMode = this.wobbleMode;

		return copy;
	}

	@Override
	public boolean shouldCopy()
	{
		return this.wrapper.shouldCopy();
	}

	@Override
	public void createMirror(LevelType levelType)
	{
		this.mirroredEmitter = this.copy(this.wrapper.getLevel());
		this.mirroredEmitter.dimension = this.dimension.getOpposite();

		this.hasMirror = true;
		this.mirroredEmitter.mirroredEmitter = this;
		this.mirroredEmitter.hasMirror = true;

		if (levelType == LevelType.REVERSAL)
		{
			this.mirroredEmitter.velocityX.set(-this.mirroredEmitter.velocityX.minValue, -this.mirroredEmitter.velocityX.maxValue);
			this.mirroredEmitter.velocityY.set(-this.mirroredEmitter.velocityY.minValue, -this.mirroredEmitter.velocityY.maxValue);
			this.mirroredEmitter.circularInversed = !this.circularInversed;

			if (!this.tightBounce) // tight bounce flag hack for goo and stuff
			{
				IntegerRange help = this.mirroredEmitter.fadeInTime;
				this.mirroredEmitter.fadeInTime = this.mirroredEmitter.fadeOutTime;
				this.mirroredEmitter.fadeOutTime = help;

				this.mirroredEmitter.gravityFactor.set(-this.mirroredEmitter.gravityFactor.minValue, -this.mirroredEmitter.gravityFactor.maxValue);

				int averageLifetime = this.mirroredEmitter.initialLife.averageValue();
				float averageFinalSize = this.mirroredEmitter.initalSize.averageValue() + averageLifetime * this.growthFactor.averageValue();
				float averageFinalX = this.xOffset.averageValue() * this.wrapper.getEmitterWidth() + this.emitterXOffset + this.wrapper.getLevel().getCameraOffset().x * this.wrapper.getEmitterParallax();
				float averageFinalY = this.yOffset.averageValue() * this.wrapper.getEmitterHeight() + this.emitterXOffset + this.wrapper.getLevel().getCameraOffset().x * this.wrapper.getEmitterParallax();
				float averageStartX = averageFinalX;
				float averageStartY = averageFinalY;

				buffer.set((this.velocityX.averageValue() + this.windFactor.averageValue() * averageLifetime) * averageLifetime, (this.velocityY.averageValue() + this.gravityFactor.averageValue() * averageLifetime) * averageLifetime);
				buffer.setAngle(buffer.angle() + this.wrapper.getRotation());

				averageFinalX = Math.min(Math.max(averageFinalX + buffer.x, this.wrapper.getLevel().getAbsLeftLimit().getCenterX()), this.wrapper.getLevel().getAbsRightLimit().getCenterX());
				averageFinalY = Math.min(Math.max(averageFinalY + buffer.y, this.wrapper.getLevel().getAbsTopLimit().getCenterY()), this.wrapper.getLevel().getAbsBottomLimit().getCenterY());

				this.mirroredEmitter.xOffset.set(1.0f - this.mirroredEmitter.xOffset.minValue + (averageFinalX - averageStartX) / this.wrapper.getEmitterWidth(), 1.0f - this.mirroredEmitter.xOffset.maxValue);
				this.mirroredEmitter.yOffset.set(1.0f - this.mirroredEmitter.yOffset.minValue + (averageFinalY - averageStartY) / this.wrapper.getEmitterHeight(), 1.0f - this.mirroredEmitter.yOffset.maxValue);

				this.mirroredEmitter.initalSize.set((int) (this.mirroredEmitter.initalSize.minValue - this.mirroredEmitter.initalSize.averageValue() + averageFinalSize), (int) (this.mirroredEmitter.initalSize.averageValue() - this.mirroredEmitter.initalSize.minValue + averageFinalSize));
				this.mirroredEmitter.growthFactor.set(-this.mirroredEmitter.growthFactor.minValue, -this.mirroredEmitter.growthFactor.maxValue);
			}
		}

		this.wrapper.getLevel().addParticleEmitter(this.mirroredEmitter);
	}

	public void initMirror()
	{
		if (this.hasMirror)
		{
			this.wrapper.getLevel().removeParticleEmitter(this.mirroredEmitter);

			this.createMirror(this.wrapper.getLevel().getType());
		}
	}

	@Override
	public void moveEmitter(float xChange, float yChange)
	{
		super.moveEmitter(xChange * this.trailFactor, yChange * this.trailFactor);
	}

	public void moveParticles(float xChange, float yChange)
	{
		Particle[] particles = this.system.particlePoolsByEmitter.get(this).particles;

		for (Particle particle : particles)
		{
			if (particle.isInUse())
			{
				particle.adjustX(xChange);
				particle.adjustY(yChange);

				particle.setInitialX(particle.getInitialX() + xChange);
				particle.setInitialY(particle.getInitialY() + yChange);
			}
		}
	}

	// ( ͡° ͜ʖ ͡°)
	public void resize(float newWidth, float oldWidth, float newHeight, float oldHeight, boolean scaleX, boolean scaleY, float rotation)
	{
		Particle[] particles = this.system.particlePoolsByEmitter.get(this).particles;

		Vector2 cameraOffset = this.wrapper.getLevel().getCameraOffset();

		float offsetX = this.wrapper.getPosition().x - this.emitterXOffset - cameraOffset.x * this.wrapper.getEmitterParallax();
		float offsetY = this.wrapper.getPosition().y - this.emitterYOffset - cameraOffset.y * this.wrapper.getEmitterParallax();
		float xScale = newWidth / oldWidth - 1.0f;
		float yScale = newHeight / oldHeight - 1.0f;

		scaleX = scaleX && this.xOffset.averageValue() != 0.0f;
		scaleY = scaleY && this.yOffset.averageValue() != 0.0f;

		for (Particle particle : particles)
		{
			if (particle.isInUse())
			{
				if (scaleX)
				{
					float xChange = (particle.getInitialX() - offsetX) * xScale;

					particle.adjustX(xChange);

					particle.setInitialX(particle.getInitialX() + xChange);
				}

				if (scaleY)
				{
					float yChange = (particle.getInitialY() - offsetY) * yScale;

					particle.adjustY(yChange);

					particle.setInitialY(particle.getInitialY() + yChange);
				}
			}
		}
	}

	@Override
	public void rotate(float angle)
	{
		this.rotate(angle, this.wrapper.getCenterOfRotationX(), this.wrapper.getCenterOfRotationY());
	}

	public void rotate(float angle, float centerX, float centerY)
	{
		Vector2 cameraOffset = this.wrapper.getLevel().getCameraOffset();

		float offsetX = cameraOffset.x + this.emitterXOffset;
		float offsetY = cameraOffset.y + this.emitterYOffset;

		for (Particle particle : this.system.particlePoolsByEmitter.get(this).particles)
		{
			if (particle.isInUse())
			{
				Point point = Hitbox.rotatePoint(particle.getX() + offsetX, particle.getY() + offsetY, centerX, centerY, angle);

				particle.setXPos(point.getX() - offsetX);
				particle.setYPos(point.getY() - offsetY);

				particle.setRotation(particle.getRotation() + angle);

				buffer.set(particle.getVelocityX(), particle.getVelocityY());
				buffer.setAngle(buffer.angle() + angle);

				particle.setVelocityX(buffer.x);
				particle.setVelocityY(buffer.y);
			}
		}
	}

	@Override
	public void setColorFor(Particle particle)
	{
		Random rng = this.wrapper.getRNG();

		Color color;

		if (this.wobbleMode)
		{
			float wobbleFactor = particle.calculateWobbleFactor();

			color = this.wrapper.getColorDistributionMode().getColor(this.wrapper.getStartEmitterTint(), this.wrapper.getEndEmitterTint(), wobbleFactor, wobbleFactor, wobbleFactor);
		}
		else
		{
			color = this.wrapper.getColorDistributionMode().getColor(this.wrapper.getStartEmitterTint(), this.wrapper.getEndEmitterTint(), rng.nextFloat(), rng.nextFloat(), rng.nextFloat());
		}

		particle.setColor(color.r, color.g, color.b);
	}

	@Override
	public void setImageFor(Particle particle)
	{
		if (this.renderMode == RENDER_IDENTICAL)
		{
			particle.setImage(this.wrapper.getEmitterSprites()[this.wrapper.getLevel().getPRNG(this.wrapper.isShared(), this.dimension).nextInt(this.wrapper.getEmitterSprites().length)][0]);
		}
		else if (this.renderMode == RENDER_PARTIAL)
		{
			particle.setImage(this.wrapper.getImage());
		}
		else
		{
			throw new UnsupportedOperationException("Can't set particle image for render mode " + this.renderMode + ".");
		}
	}

	@Override
	public boolean addAttractor(LevelObject attractor, AttractorConfiguration configuration)
	{
		if (this.attractors == null)
		{
			this.attractors = new LinkedHashMap<LevelObject, AttractorConfiguration>();
		}

		return this.attractors.put(attractor, configuration) != configuration;
	}

	@Override
	public void removeAttractor(LevelObject attractor)
	{
		if (this.attractors != null)
		{
			this.attractors.remove(attractor);
		}
	}

	@Override
	public void addParticleEater(LevelObject object)
	{
		if (this.particleEaters == null)
		{
			this.particleEaters = new HashSet<LevelObject>(4);
		}

		this.particleEaters.add(object);
	}

	public void removeParticleEater(LevelObject object)
	{
		if (this.particleEaters != null)
		{
			this.particleEaters.remove(object);
		}
	}

	@Override
	public void update(ParticleSystem system, float delta)
	{
		Random rng = this.wrapper.getLevel().getPRNG(this.wrapper.isShared(), this.dimension);

		if (this.timeLimitedLifetime)
		{
			this.life -= delta;
		}

		this.minX = this.newMinX;
		this.maxX = this.newMaxX;
		this.minY = this.newMinY;
		this.maxY = this.newMaxY;

		if (this.particleEaters != null)
		{
			Iterator<LevelObject> iterator = this.particleEaters.iterator();

			while (iterator.hasNext())
			{
				LevelObject object = iterator.next();

				if (!object.isEnabled() || object.getHitbox().getMinX() < this.minX || object.getHitbox().getMaxX() > this.maxX || object.getHitbox().getMinY() < this.minY || object.getHitbox().getMaxY() > this.maxY)
				{
					iterator.remove();
				}
			}
		}

		this.newMinX = Float.MIN_VALUE;
		this.newMaxX = Float.MAX_VALUE;
		this.newMinY = Float.MIN_VALUE;
		this.newMaxY = Float.MAX_VALUE;

		this.nextSpawn -= delta;

		if (!this.stopped && this.life > 0 && (this.nextSpawn <= 0 || this.maxOutParticles) && this.wrapper.validTextures())
		{
			this.nextSpawn = this.spawnInterval.randomValue(rng);

			int count = this.maxOutParticles ? this.maxParticles - this.activeParticleCount : this.spawnCount.randomValue(rng);

			if (this.wrapper.shouldCreateParticles())
			{
				for (int i = 0; i < count; i++)
				{
					int value = this.initialLife.randomValue(rng);

					Particle particle = system.getParticle(this, value);

					if (particle != null)
					{
						if (!this.initParticle(particle))
						{
							particle.kill();
						}
					}
				}

				if (this.attractors != null)
				{
					for (LevelObject attractor : this.attractors.keySet())
					{
						Hitbox hitbox = attractor.getHitbox();

						int additionalCount = (int) (this.attractors.get(attractor).getAttractionFactor() * count);

						for (int i = 0; i < additionalCount; i++)
						{
							int value = (int) (this.initialLife.randomValue(this.wrapper.getLevel().getPRNG(this.wrapper.isShared(), this.dimension)) * 2.5f);

							Particle p = system.getParticle(this, value);

							if (p != null)
							{
								if (!this.initParticle(p))
								{
									continue;
								}

								float xAttract = hitbox.getCenterX();
								float yAttract = hitbox.getMaxY() - this.attractors.get(attractor).getAttractionRange() * hitbox.getHeight() * this.wrapper.getLevel().getPRNG(this.wrapper.isShared(), this.dimension).nextFloat();
								float relXPos = p.getX() + this.emitterXOffset + this.wrapper.getLevel().getCameraOffset().x * this.wrapper.getEmitterParallax();
								float relYPos = p.getY() + this.emitterYOffset + this.wrapper.getLevel().getCameraOffset().y * this.wrapper.getEmitterParallax();

								buffer.set(this.velocityX.randomValue(this.wrapper.getLevel().getPRNG(this.wrapper.isShared(), this.dimension)) * 1.5f, this.velocityY.randomValue(this.wrapper.getLevel().getPRNG(this.wrapper.isShared(), this.dimension)) * 1.5f);
								buffer.setAngle((float) Math.toDegrees(Math.tan((relYPos - yAttract) / Math.abs(xAttract - relXPos))));

								p.setVelocityX(xAttract < relXPos ? -buffer.x : buffer.x);
								p.setVelocityY(-Math.abs(buffer.y));
							}
						}
					}
				}
			}
		}

		this.wasUpdated = true;
	}

	@Override
	public void updateParticle(Particle particle, float delta)
	{
		buffer.x = particle.getWindFactor() * delta;
		buffer.y = particle.getGravityFactor() * delta;

		if (this.rotateAccelerations && (buffer.x != 0.0f || buffer.y != 0.0f))
		{
			buffer.setAngle(buffer.angle() + this.wrapper.getRotation());
		}

		particle.adjustVelocityX(buffer.x);
		particle.adjustVelocityY(buffer.y);

		float camOffsetX = this.wrapper.getLevel().getCameraOffset().x * particle.getParallax();
		float camOffsetY = this.wrapper.getLevel().getCameraOffset().y * particle.getParallax();

		// TODO fix bug particle out of area don't disappear because rotation
		if (this.limitToWrapper)
		{
			if (particle.getX() + this.emitterXOffset + camOffsetX - this.wrapper.getHitbox().getPoints()[0] < -particle.getSize() / 2 && !(particle.getVelocityX() > 0.0f) || this.wrapper.getHitbox().getPoints()[0] + this.wrapper.getEmitterWidth() - (particle.getX() + this.emitterXOffset + camOffsetX) < -particle.getSize() / 2 && !(particle.getVelocityX() < 0.0f))
			{
				this.onParticleOutOfArea(particle, true);
			}

			if (particle.getY() + this.emitterYOffset + camOffsetY - this.wrapper.getHitbox().getPoints()[1] < -particle.getSize() / 2 && !(particle.getVelocityY() > 0.0f) || this.wrapper.getHitbox().getPoints()[1] + this.wrapper.getEmitterHeight() - (particle.getY() + this.emitterYOffset + camOffsetY) < (this.tightBounce ? particle.getSize() / 2 : -particle.getSize() / 2) && !(particle.getVelocityY() < 0.0f))
			{
				this.onParticleOutOfArea(particle, false);
			}
		}
		else
		{
			Level level = this.wrapper.getLevel();

			float absMinX = level.getAbsLeftLimit() == null ? -10000 : level.getAbsLeftLimit().getCenterX() - Level.OUT_OF_MAP_TOLERANCE - particle.getWidth() / 2;
			float absMaxX = level.getAbsRightLimit() == null ? 10000 : level.getAbsRightLimit().getCenterX() + Level.OUT_OF_MAP_TOLERANCE + particle.getWidth() / 2;
			float absMinY = level.getAbsTopLimit() == null ? -10000 : level.getAbsTopLimit().getCenterY() - Level.OUT_OF_MAP_TOLERANCE - particle.getHeight() / 2;
			float absMaxY = level.getAbsBottomLimit() == null ? 10000 : level.getAbsBottomLimit().getCenterY() + Level.OUT_OF_MAP_TOLERANCE + particle.getHeight() / 2;

			if (particle.getX() + this.emitterXOffset + camOffsetX > absMaxX && particle.getVelocityX() >= 0.0f || particle.getX() + this.emitterXOffset + camOffsetX < absMinX && particle.getVelocityX() <= 0.0f)
			{
				this.onParticleOutOfArea(particle, true);
			}

			if (particle.getY() + this.emitterYOffset + camOffsetY > absMaxY && particle.getVelocityY() >= 0.0f || particle.getY() + this.emitterYOffset + camOffsetY < absMinY && particle.getVelocityY() <= 0.0f)
			{
				this.onParticleOutOfArea(particle, false);
			}
		}

		if (particle.getLifetime() < particle.getFadeOutTime() && this.shouldFadeOut && !particle.isFadingOut())
		{
			particle.setFadingOut(true);
		}

		if (particle.isFadingIn() && this.shouldFadeIn)
		{
			particle.adjustAlpha(delta * particle.getTargetAlpha() / particle.getFadeInTime());
		}

		if (particle.isFadingOut() && this.shouldFadeOut)
		{
			particle.adjustAlpha(-(delta * particle.getTargetAlpha()) / particle.getFadeOutTime());
		}

		particle.adjustSize(particle.getGrowthFactor() * delta);

		if (this.wobbleMode && particle.getTotalWobbleInterval() > 0)
		{
			float lastWobbleFactor = particle.calculateWobbleFactor();

			particle.setWobbleInterval((int) (particle.getWobbleInterval() + delta) % particle.getTotalWobbleInterval());

			float currentWobbleFactor = particle.calculateWobbleFactor();

			particle.setSize(particle.getSize() - this.initalSize.valueAt(lastWobbleFactor) + this.initalSize.valueAt(currentWobbleFactor));
			particle.setRotation(particle.getRotation() - this.startRotation.valueAt(lastWobbleFactor) + this.startRotation.valueAt(currentWobbleFactor));
			particle.setVelocityX(particle.getVelocityX() - this.velocityX.valueAt(lastWobbleFactor) + this.velocityX.valueAt(currentWobbleFactor));
			particle.setVelocityY(particle.getVelocityY() - this.velocityY.valueAt(lastWobbleFactor) + this.velocityY.valueAt(currentWobbleFactor));

			this.setColorFor(particle);
		}

		if (this.wrapper.getLevel().getType() == LevelType.REVERSAL && this.dimension == Dimension.MONOCHROME)
		{
			// another tightbounce flag hack for goo in reversal

			if (!particle.isFadingOut() && this.wrapper.getHitbox().contains(particle) && !this.tightBounce)
			{
				if (this.shouldFadeOut)
				{
					if (!particle.isFadingOut())
					{
						particle.setFadingOut(true);
					}
				}
				else
				{
					particle.kill();
				}
			}
		}

		if (this.wrapper.getLevel().isOutOfMap(particle.getX() + this.wrapper.getLevel().getCameraOffset().x * this.wrapper.getEmitterParallax() + this.emitterXOffset, particle.getY() + this.wrapper.getLevel().getCameraOffset().y * this.wrapper.getEmitterParallax() + this.emitterYOffset))
		{
			particle.kill();
		}

		if (particle.getX() < this.newMinX)
		{
			this.newMinX = particle.getX();
		}

		if (particle.getY() < this.newMinY)
		{
			this.newMinY = particle.getY();
		}

		if (particle.getX() > this.newMaxX)
		{
			this.newMaxX = particle.getX();
		}

		if (particle.getX() > this.newMaxY)
		{
			this.newMaxY = particle.getY();
		}
	}

	private boolean initParticle(Particle particle)
	{
		Random rng = this.wrapper.getLevel().getPRNG(this.wrapper.isShared(), this.dimension);

		float wobbleFactor = 0.0f;

		if (this.wobbleMode)
		{
			wobbleFactor = rng.nextFloat();

			particle.setInitialWobbleFactor(wobbleFactor);

			particle.setTotalWobbleInterval(this.wobbleInterval.randomValue(rng));
			particle.setWobbleInterval((int) (wobbleFactor * particle.getTotalWobbleInterval()));
		}

		buffer.set(this.velocityX.valueAt(this.wobbleMode ? wobbleFactor : rng.nextFloat()), this.velocityY.valueAt(this.wobbleMode ? wobbleFactor : rng.nextFloat()));

		if (this.circularEmitter)
		{
			buffer.scl(2.0f);

			buffer.setAngle(this.startRotation.valueAt(this.wobbleMode ? wobbleFactor : rng.nextFloat()));
		}

		buffer.setAngle(buffer.angle() + this.wrapper.getRotation());

		particle.setVelocityX(buffer.x);
		particle.setVelocityY(buffer.y);

		if (this.circularEmitter)
		{
			particle.setRotation(buffer.angle());
		}

		if (this.xOffset != null && this.yOffset != null)
		{
			buffer.set(this.xOffset.randomValue(rng) * this.wrapper.getEmitterWidth(), this.yOffset.randomValue(rng) * this.wrapper.getEmitterHeight());

			if (!this.wrapper.canCreateParticleAt(buffer.x, buffer.y))
			{
				return false;
			}

			particle.setSize(this.initalSize.valueAt(this.wobbleMode ? wobbleFactor : rng.nextFloat()));

			if (this.circularEmitter)
			{
				particle.setXPos(this.wrapper.getHitbox().getCenterX() - this.emitterXOffset - this.wrapper.getLevel().getCameraOffset().x * this.wrapper.getEmitterParallax());
				particle.setYPos(this.wrapper.getHitbox().getCenterY() - this.emitterYOffset - this.wrapper.getLevel().getCameraOffset().y * this.wrapper.getEmitterParallax());

				if (this.circularInversed)
				{
					float finalPositionX = particle.getX() + particle.getVelocityX() * particle.getLifetime();
					float finalPositionY = particle.getY() + particle.getVelocityY() * particle.getLifetime();

					particle.setVelocityX(-particle.getVelocityX());
					particle.setVelocityY(-particle.getVelocityY());

					particle.setXPos(finalPositionX);
					particle.setYPos(finalPositionY);
				}

				particle.setInitialX(particle.getX());
				particle.setInitialY(particle.getY());
			}
			else
			{
				particle.setInitialX(this.wrapper.getPosition().x - this.emitterXOffset - this.wrapper.getLevel().getCameraOffset().x * this.wrapper.getEmitterParallax() + buffer.x);
				particle.setInitialY(this.wrapper.getPosition().y - this.emitterYOffset - this.wrapper.getLevel().getCameraOffset().y * this.wrapper.getEmitterParallax() + buffer.y);

				buffer.setAngle(buffer.angle() + this.wrapper.getRotation());

				particle.setXPos((this.wrapper.forceDirectPositionHack() ? this.wrapper.getPosition().x : this.wrapper.getHitbox().getPoints()[0]) - this.emitterXOffset - this.wrapper.getLevel().getCameraOffset().x * this.wrapper.getEmitterParallax() + buffer.x);
				particle.setYPos((this.wrapper.forceDirectPositionHack() ? this.wrapper.getPosition().y : this.wrapper.getHitbox().getPoints()[1]) - this.emitterYOffset - this.wrapper.getLevel().getCameraOffset().y * this.wrapper.getEmitterParallax() + buffer.y);
			}
		}

		particle.setGravityFactor(this.gravityFactor.randomValue(rng) / 5.0f);
		particle.setWindFactor(this.windFactor.randomValue(rng) / 5.0f);
		particle.setGrowthFactor(this.growthFactor.randomValue(rng));

		particle.setRotationVelocity(this.rotationVelocity.randomValue(rng) / 120.0f);

		if (!this.circularEmitter)
		{
			particle.setRotation(this.startRotation.valueAt(this.wobbleMode ? wobbleFactor : rng.nextFloat()) + this.wrapper.getRotation());
		}

		particle.setFadeOutTime(this.fadeOutTime.randomValue(rng));

		if (this.shouldFadeIn)
		{
			particle.setFadeInTime(this.fadeInTime.randomValue(rng));
			particle.setAlpha(0.0f);
			particle.setTargetAlpha(this.startAlpha.randomValue(rng) / 255.0f);
		}
		else
		{
			particle.setAlpha(this.startAlpha.randomValue(rng) / 255.0f);
		}

		this.setColorFor(particle);

		particle.setFadingIn(this.shouldFadeIn);
		particle.setParallax(this.wrapper.getEmitterParallax());

		return true;
	}

	protected void onParticleOutOfArea(Particle particle, boolean horizontal)
	{
		if (this.shouldBounceOutOfArea)
		{
			if (horizontal)
			{
				particle.setVelocityX(-particle.getVelocityX());
			}
			else
			{
				particle.setVelocityY(-particle.getVelocityY());
			}
		}
		else
		{
			if (this.shouldFadeOut)
			{
				if (!particle.isFadingOut())
				{
					particle.setFadingOut(true);
					particle.setLifetime(particle.getFadeOutTime());
				}
			}
			else
			{
				particle.kill();
			}
		}
	}

	@Override
	public int getRenderMode()
	{
		return this.renderMode;
	}

	@Override
	public TextureRegion[][] getImages()
	{
		return this.wrapper.getEmitterSprites();
	}

	@Override
	public boolean isBoundToWrapperTexture()
	{
		return this.boundToWrapperTexture;
	}

	@Override
	public boolean isCompleted()
	{
		return this.timeLimitedLifetime && this.life <= 0;
	}

	@Override
	public boolean hasLimitedLength()
	{
		return this.timeLimitedLifetime;
	}

	@Override
	public Vector2 getPosition()
	{
		return this.wrapper.getPosition();
	}

	@Override
	public boolean shouldScissor(int direction)
	{
		return (this.scissoredDirections & direction) > 0;
	}

	@Override
	public int getScissoredDirections()
	{
		return this.scissoredDirections;
	}

	public void setScissoredDirections(int scissoredDirections)
	{
		this.scissoredDirections = scissoredDirections;
	}

	public void setShouldScissor(int direction, boolean value)
	{
		if (value)
		{
			this.scissoredDirections |= direction;
		}
		else if (this.shouldScissor(direction))
		{
			this.scissoredDirections -= direction;
		}
	}

	@Override
	public boolean isTimeEffected()
	{
		return this.wrapper.isTimeEffectedAnyWay() || !(this.type == EmitterType.TIME_RESISTANT_AREA || this.type == EmitterType.TIME_RESISTANT_AREA || this.type == EmitterType.TIME_RESISTANT_SURFACE) && this.wrapper.isTimeEffected();
	}

	public boolean hasMirror()
	{
		return this.hasMirror;
	}

	private static IntegerRange copy(IntegerRange integerRange)
	{
		return integerRange == null ? null : integerRange.copy();
	}

	private static FloatRange copy(FloatRange floatRange)
	{
		return floatRange == null ? null : floatRange.copy();
	}

	public static class IntegerRange
	{
		private int minValue;
		private int maxValue;

		public IntegerRange(int minValue, int maxValue)
		{
			this.set(minValue, maxValue);
		}

		public int averageValue()
		{
			return (this.minValue + this.maxValue) / 2;
		}

		public IntegerRange copy()
		{
			return new IntegerRange(this.minValue, this.maxValue);
		}

		public void scale(float factor)
		{
			this.minValue *= factor;
			this.maxValue *= factor;
		}

		public void set(int minValue, int maxValue)
		{
			this.minValue = Math.min(minValue, maxValue);
			this.maxValue = Math.max(minValue, maxValue);
		}

		public void setMinValue(int minValue)
		{
			this.minValue = minValue;
		}

		public void setMaxValue(int maxValue)
		{
			this.maxValue = maxValue;
		}

		public int minValue()
		{
			return this.minValue;
		}

		public int maxValue()
		{
			return this.maxValue;
		}

		public int valueAt(float value)
		{
			return (int) (this.minValue + value * (this.maxValue - this.minValue));
		}

		public int randomValue(Random rng)
		{
			return (int) (this.minValue + rng.nextFloat() * (this.maxValue - this.minValue));
		}

		@Override
		public String toString()
		{
			return "IntegerRange(" + this.minValue + ", " + this.maxValue + ")";
		}

		public String buildString()
		{
			return "new " + this.toString();
		}

		public void mul(float factor)
		{
			this.minValue *= factor;
			this.maxValue *= factor;
		}
	}

	public static class FloatRange
	{
		private float minValue;
		private float maxValue;

		public FloatRange(float minValue, float maxValue)
		{
			this.set(minValue, maxValue);
		}

		public FloatRange copy()
		{
			return new FloatRange(this.minValue, this.maxValue);
		}

		public void set(float minValue, float maxValue)
		{
			this.minValue = Math.min(minValue, maxValue);
			this.maxValue = Math.max(minValue, maxValue);
		}

		public void scale(float factor)
		{
			this.minValue *= factor;
			this.maxValue *= factor;
		}

		public void setMinValue(float minValue)
		{
			this.minValue = minValue;
		}

		public void setMaxValue(float maxValue)
		{
			this.maxValue = maxValue;
		}

		public float averageValue()
		{
			return (this.minValue + this.maxValue) / 2;
		}

		public float minValue()
		{
			return this.minValue;
		}

		public float maxValue()
		{
			return this.maxValue;
		}

		public float valueAt(float value)
		{
			return this.minValue + value * (this.maxValue - this.minValue);
		}

		public float randomValue(Random rng)
		{
			return this.minValue + rng.nextFloat() * (this.maxValue - this.minValue);
		}

		@Override
		public String toString()
		{
			return "FloatRange(" + this.minValue + "f, " + this.maxValue + "f)";
		}

		public String buildString()
		{
			return "new " + this.toString();
		}

		public void mul(float factor)
		{
			this.minValue *= factor;
			this.maxValue *= factor;
		}
	}

	public void fadeOutAll(int fadeOutTime)
	{
		ParticlePool pool = this.system.particlePoolsByEmitter.get(this);

		for (Particle particle : pool.particles)
		{
			if (particle.isInUse())
			{
				if (particle.getLifetime() >= fadeOutTime)
				{
					particle.setLifetime(fadeOutTime);
					particle.setFadingIn(false);
					particle.setFadingOut(true);
					particle.setFadeOutTime(fadeOutTime);
				}
			}
		}
	}

	public void radialExplode(float centerX, float centerY, float acceleration)
	{
		ParticlePool pool = this.system.particlePoolsByEmitter.get(this);

		for (Particle particle : pool.particles)
		{
			if (particle.isInUse())
			{
				buffer.set(particle.getRelativeX(this.dimension), particle.getRelativeY(this.dimension));
				buffer.sub(centerX, centerY);

				buffer.setLength(-acceleration);

				particle.adjustVelocityX(buffer.x);
				particle.adjustVelocityY(buffer.y);
			}
		}
	}

	public void radialPull(float centerX, float centerY, float acceleration, Random rng, float minRngFactor)
	{
		ParticlePool pool = this.system.particlePoolsByEmitter.get(this);

		for (Particle particle : pool.particles)
		{
			if (particle.isInUse())
			{
				buffer.set(particle.getRelativeX(this.dimension), particle.getRelativeY(this.dimension));
				buffer.sub(centerX, centerY);

				if (rng.nextFloat() > minRngFactor)
				{
					particle.adjustVelocityX(-buffer.x * acceleration);
					particle.adjustVelocityY(-buffer.y * acceleration);
				}

				float factor = buffer.len() / 40.0f;

				if (factor > 1.0f)
				{
					factor = 1.0f;
				}

				particle.setVelocityX(factor * particle.getVelocityX());
				particle.setVelocityY(factor * particle.getVelocityY());

				float alphaFactor = buffer.len() / 50.0f;

				if (alphaFactor > 1.0f)
				{
					alphaFactor = 1.0f;
				}

				particle.adjustAlpha(-(1.0f - alphaFactor) / 50.0f);
			}
		}
	}

	public void partTheSeaX(float accelerationLeftX, float accelerationRightX, int splitX)
	{
		ParticlePool pool = this.system.particlePoolsByEmitter.get(this);

		for (Particle particle : pool.particles)
		{
			if (particle.isInUse())
			{
				if (particle.getRelativeX(this.dimension) >= splitX)
				{
					particle.adjustVelocityX(accelerationLeftX);
				}
				else
				{
					particle.adjustVelocityX(-accelerationRightX);
				}
			}
		}
	}

	public void partTheSeaY(float accelerationLeftY, float accelerationRightY, int splitY)
	{
		ParticlePool pool = this.system.particlePoolsByEmitter.get(this);

		for (Particle particle : pool.particles)
		{
			if (particle.isInUse())
			{
				if (particle.getRelativeY(this.dimension) >= splitY)
				{
					particle.adjustVelocityY(accelerationLeftY);
				}
				else
				{
					particle.adjustVelocityY(-accelerationRightY);
				}
			}
		}
	}

	public void restart()
	{
		this.reset();

		this.stopped = false;
		this.life = this.lifeTime;
	}

	@Override
	public void stop()
	{
		this.stopped = true;
	}

	@Override
	public void resume()
	{
		this.stopped = false;
	}

	@Override
	public Hitbox getHitbox()
	{
		return this.wrapper.getHitbox();
	}

	@Override
	public boolean shouldCreateMirror(boolean allowMirrors)
	{
		return allowMirrors && this.wrapper.getLevel().shouldCreateMirrors();
	}

	@Override
	public void setLevel(Level level)
	{
		this.wrapper.setLevel(level);
	}

	@Override
	public ParticleEmitterImpl copy(Level level)
	{
		return this.copy(this.wrapper, false, level);
	}

	public ParticleEmitterImpl copy(IEmitterWrapper wrapper, boolean useWrapperValues, Level level)
	{
		ParticleEmitterImpl copy = new ParticleEmitterImpl(this.dimension, wrapper, this.spawnInterval.copy(), this.spawnCount.copy(), this.initialLife.copy(), this.initalSize.copy(), this.wobbleInterval.copy(), this.xOffset.copy(), this.yOffset.copy(), this.velocityX.copy(), this.velocityY.copy(), this.startRotation.copy(), this.rotationVelocity.copy(), this.growthFactor.copy(), this.gravityFactor.copy(), this.windFactor.copy(), this.startAlpha.copy(), this.fadeInTime.copy(), this.fadeOutTime.copy(), this.lifeTime, this.shouldFadeIn, this.shouldFadeOut, this.limitToWrapper, this.tightBounce, this.shouldBounceOutOfArea, this.timeLimitedLifetime, this.useAttractors, this.rotateAccelerations, this.maxOutParticles, this.circularEmitter, this.renderMode, useWrapperValues ? wrapper.getMaxParticles() : this.maxParticles);

		copy.scaleWithWidth = this.scaleWithWidth;
		copy.scaleWithHeight = this.scaleWithHeight;
		copy.originalWidth = this.originalWidth;
		copy.originalHeight = this.originalHeight;
		copy.originalMaxParticles = this.originalMaxParticles;
		copy.originalMinCount = this.originalMinCount;
		copy.originalMaxCount = this.originalMaxCount;
		copy.scissoredDirections = this.scissoredDirections;
		copy.additive = this.additive;
		copy.circularInversed = this.circularInversed;
		copy.wobbleMode = this.wobbleMode;
		copy.stencilled = this.stencilled;

		return copy;
	}

	@Override
	public boolean isShared()
	{
		return this.wrapper.isShared();
	}

	@Override
	public void setPosition(float x, float y)
	{
		this.wrapper.setPosition(x, y);
	}

	@Override
	public boolean isRotateable()
	{
		return this.wrapper.isRotateable();
	}

	@Override
	public float getCenterOfRotationX()
	{
		return this.wrapper.getCenterOfRotationX();
	}

	@Override
	public float getCenterOfRotationY()
	{
		return this.wrapper.getCenterOfRotationY();
	}

	@Override
	public int getDepth()
	{
		return this.wrapper.getEmitterDepth();
	}

	@Override
	public boolean isInBackground()
	{
		return this.wrapper.isInBackground();
	}

	@Override
	public boolean ownsMouse()
	{
		return false;
	}

	@Override
	public IEmitterWrapper getWrapper()
	{
		return this.wrapper;
	}

	@Override
	public void setWrapper(IEmitterWrapper wrapper)
	{
		this.wrapper = wrapper;
	}

	public IntegerRange getSpawnInterval()
	{
		return this.spawnInterval;
	}

	public IntegerRange getSpawnCount()
	{
		return this.spawnCount;
	}

	public IntegerRange getInitialLife()
	{
		return this.initialLife;
	}

	public IntegerRange getInitialSize()
	{
		return this.initalSize;
	}

	public FloatRange getXOffset()
	{
		return this.xOffset;
	}

	public FloatRange getYOffset()
	{
		return this.yOffset;
	}

	public FloatRange getVelocityX()
	{
		return this.velocityX;
	}

	public FloatRange getVelocityY()
	{
		return this.velocityY;
	}

	public FloatRange getStartRotation()
	{
		return this.startRotation;
	}

	public FloatRange getRotationVelocity()
	{
		return this.rotationVelocity;
	}

	public FloatRange getGrowthFactor()
	{
		return this.growthFactor;
	}

	public FloatRange getGravityFactor()
	{
		return this.gravityFactor;
	}

	public FloatRange getWindFactor()
	{
		return this.windFactor;
	}

	public IntegerRange getStartAlpha()
	{
		return this.startAlpha;
	}

	public IntegerRange getFadeOutTime()
	{
		return this.fadeOutTime;
	}

	public int getLifeTime()
	{
		return this.lifeTime;
	}

	public boolean shouldFadeOut()
	{
		return this.shouldFadeOut;
	}

	public boolean limittedToWrapper()
	{
		return this.limitToWrapper;
	}

	public boolean hasLimitedLifetime()
	{
		return this.timeLimitedLifetime;
	}

	@Override
	public boolean shouldRemoveOnCompletion()
	{
		return this.wrapper.getLevel().shouldRemoveEmittersOnCompletion() && (this.type == null || this.type.shouldRemoveOnCompletion());
	}

	public boolean shouldBounceOutOfArea()
	{
		return this.shouldBounceOutOfArea;
	}

	public void setWrapper(WrapperTexture wrapper)
	{
		this.wrapper = wrapper;
	}

	public void setLifeTime(int lifeTime)
	{
		this.lifeTime = lifeTime;
		this.life = lifeTime;
	}

	public IntegerRange getWobbleInterval()
	{
		return this.wobbleInterval;
	}

	public void setWobbleInterval(IntegerRange wobbleInterval)
	{
		this.wobbleInterval = wobbleInterval;
	}

	public void setSpawnInterval(IntegerRange spawnInterval)
	{
		this.spawnInterval = spawnInterval;
	}

	public void setSpawnCount(IntegerRange spawnCount)
	{
		this.spawnCount = spawnCount;
	}

	public void setInitalLife(IntegerRange initalLife)
	{
		this.initialLife = initalLife;
	}

	public void setInitalSize(IntegerRange initalSize)
	{
		this.initalSize = initalSize;
	}

	public void setxOffset(FloatRange xOffset)
	{
		this.xOffset = xOffset;
	}

	public void setyOffset(FloatRange yOffset)
	{
		this.yOffset = yOffset;
	}

	public void setVelocityX(FloatRange velocityX)
	{
		this.velocityX = velocityX;
	}

	public void setVelocityY(FloatRange velocityY)
	{
		this.velocityY = velocityY;
	}

	public void setStartRotation(FloatRange startRotation)
	{
		this.startRotation = startRotation;
	}

	public void setRotationVelocity(FloatRange rotationVelocity)
	{
		this.rotationVelocity = rotationVelocity;
	}

	public void setGrowthFactor(FloatRange growthFactor)
	{
		this.growthFactor = growthFactor;
	}

	public void setGravityFactor(FloatRange gravityFactor)
	{
		this.gravityFactor = gravityFactor;
	}

	public void setWindFactor(FloatRange windFactor)
	{
		this.windFactor = windFactor;
	}

	public void setStartAlpha(IntegerRange startAlpha)
	{
		this.startAlpha = startAlpha;
	}

	public void setFadeOutTime(IntegerRange fadeOutTime)
	{
		this.fadeOutTime = fadeOutTime;
	}

	public void setShouldFadeOut(boolean shouldFadeOut)
	{
		this.shouldFadeOut = shouldFadeOut;
	}

	public void setLimitToWrapper(boolean limitToWrapper)
	{
		this.limitToWrapper = limitToWrapper;
	}

	public void setShouldBounceOutOfArea(boolean shouldBounceOutOfArea)
	{
		this.shouldBounceOutOfArea = shouldBounceOutOfArea;
	}

	public void setHasLimitedLifetime(boolean hasLimitedLifetime)
	{
		this.timeLimitedLifetime = hasLimitedLifetime;
	}

	public boolean shouldFadeIn()
	{
		return this.shouldFadeIn;
	}

	public void setShouldFadeIn(boolean shouldFadeIn)
	{
		this.shouldFadeIn = shouldFadeIn;
	}

	public IntegerRange getFadeInTime()
	{
		return this.fadeInTime;
	}

	public void setFadeInTime(IntegerRange fadeInTime)
	{
		this.fadeInTime = fadeInTime;
	}

	public ParticleEmitterImpl getMirroredEmitter()
	{
		return this.mirroredEmitter;
	}

	public int getOriginalMaxParticles()
	{
		return this.originalMaxParticles;
	}

	public void setOriginalMaxParticles(int originalMaxParticles)
	{
		this.originalMaxParticles = originalMaxParticles;
	}

	public int getOriginalMinCount()
	{
		return this.originalMinCount;
	}

	public void setOriginalMinCount(int originalMinCount)
	{
		this.originalMinCount = originalMinCount;
	}

	public int getOriginalMaxCount()
	{
		return this.originalMaxCount;
	}

	public void setOriginalMaxCount(int originalMaxCount)
	{
		this.originalMaxCount = originalMaxCount;
	}

	public int getOriginalWidth()
	{
		return this.originalWidth;
	}

	public void setOriginalWidth(int originalWidth)
	{
		this.originalWidth = originalWidth;
	}

	public int getOriginalHeight()
	{
		return this.originalHeight;
	}

	public void setOriginalHeight(int originalHeight)
	{
		this.originalHeight = originalHeight;
	}

	public boolean isStopped()
	{
		return this.stopped;
	}

	public boolean isRunning()
	{
		return !this.stopped;
	}

	@Override
	public boolean shouldUpdate()
	{
		return this.wrapper.shouldUpdateEmitter();
	}

	@Override
	public EmitterType getType()
	{
		return this.type;
	}

	public boolean isScaleWithWidth()
	{
		return this.scaleWithWidth;
	}

	public boolean isScaleWithHeight()
	{
		return this.scaleWithHeight;
	}

	public void setScaleWithWidth(boolean scaleWithWidth)
	{
		this.scaleWithWidth = scaleWithWidth;
	}

	public void setScaleWithHeight(boolean scaleWithHeight)
	{
		this.scaleWithHeight = scaleWithHeight;
	}

	public boolean isRotateAccelerations()
	{
		return this.rotateAccelerations;
	}

	public void setRotateAccelerations(boolean rotateAccelerations)
	{
		this.rotateAccelerations = rotateAccelerations;
	}

	public boolean isMaxOutParticles()
	{
		return this.maxOutParticles;
	}

	public void setMaxOutParticles(boolean maxOutParticles)
	{
		this.maxOutParticles = maxOutParticles;
	}

	public boolean isCircularEmitter()
	{
		return this.circularEmitter;
	}

	public void setCircularEmitter(boolean circularEmitter)
	{
		this.circularEmitter = circularEmitter;
	}

	@Override
	public boolean wasUpdated()
	{
		return this.wasUpdated;
	}

	public boolean isTightBounce()
	{
		return this.tightBounce;
	}

	public void setTightBounce(boolean tightBounce)
	{
		this.tightBounce = tightBounce;
	}

	public boolean isCircularInversed()
	{
		return this.circularInversed;
	}

	public void setCircularInversed(boolean circularInversed)
	{
		this.circularInversed = circularInversed;
	}

	public boolean isWobbleMode()
	{
		return this.wobbleMode;
	}

	public void setWobbleMode(boolean wobbleMode)
	{
		this.wobbleMode = wobbleMode;
	}
}
