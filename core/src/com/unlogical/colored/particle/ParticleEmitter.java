package com.unlogical.colored.particle;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.IDeepCopyable;
import com.unlogical.colored.util.LevelObject;

public abstract class ParticleEmitter implements IDeepCopyable<ParticleEmitter>
{
	public static final int UP = 0b1;
	public static final int RIGHT = 0b10;
	public static final int DOWN = 0b100;
	public static final int LEFT = 0b1000;

	public static final int ANY = UP | RIGHT | LEFT | DOWN;

	public static final int RENDER_IDENTICAL = 0;
	public static final int RENDER_PARTIAL = 1;

	protected TextureRegion[][] images;

	protected ParticleSystem system;

	protected Dimension dimension;

	protected int activeParticleCount = 0;
	protected int maxParticles;

	protected float emitterXOffset;
	protected float emitterYOffset;

	protected float alphaFactor = 1.0f;

	protected boolean enabled;
	protected boolean renderViaSystem = true;
	protected boolean additive;
	protected boolean stencilled;

	public ParticleEmitter(Dimension dimension, ParticleSystem system, TextureRegion[][] sprites, int maxParticles)
	{
		this.system = system;
		this.images = sprites;
		this.maxParticles = maxParticles;
	}

	protected abstract ParticleEmitter copy(Level level);

	public abstract void setColorFor(Particle particle);

	public abstract void setImageFor(Particle particle);

	public abstract void updateParticle(Particle particle, float delta);

	public abstract void update(ParticleSystem system, float delta);

	public abstract void stop();

	public abstract void resume();

	public abstract boolean addAttractor(LevelObject attractor, AttractorConfiguration configuration);

	public abstract void removeAttractor(LevelObject hitbox);

	public abstract void addParticleEater(LevelObject object);

	public abstract boolean isCompleted();

	public abstract boolean hasLimitedLength();

	public abstract boolean isTimeEffected();

	public abstract boolean isInBackground();

	public abstract boolean shouldRemoveOnCompletion();

	public abstract boolean shouldScissor(int direction);

	public abstract boolean wasUpdated();

	public abstract int getDepth();

	public abstract int getRenderMode();

	public abstract int getScissoredDirections();

	public abstract void setWrapper(IEmitterWrapper wrapper);

	public abstract IEmitterWrapper getWrapper();

	public abstract EmitterType getType();

	public Particle getParticle(int lifetime)
	{
		return this.system.getParticle(this, lifetime);
	}

	public void setSystem(ParticleSystem system)
	{
		this.system = system;
	}

	public void reset()
	{
		if (this.system != null)
		{
			this.system.releaseAll(this);
		}
	}

	public void moveEmitter(float xChange, float yChange)
	{
		this.system.moveParticles(this, xChange, yChange);
	}

	public abstract void rotate(float angle);

	public void enable()
	{
		this.enabled = true;
	}

	public void disable()
	{
		this.enabled = false;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}

	public boolean isBoundToWrapperTexture()
	{
		return false;
	}

	public boolean hasActiveParticles()
	{
		return this.system.hasActiveParticles(this);
	}

	public boolean hasActiveParticlesOnScreen()
	{
		return this.system.hasActiveParticlesOnScreen(this);
	}

	public ParticleSystem getSystem()
	{
		return this.system;
	}

	public int getParticleCount()
	{
		return this.activeParticleCount;
	}

	public int getMaxParticles()
	{
		return this.maxParticles;
	}

	public TextureRegion[][] getImages()
	{
		return this.images;
	}

	public boolean renderViaSystem()
	{
		return this.renderViaSystem;
	}

	public void ensureManualRendering()
	{
		this.renderViaSystem = false;
	}

	public Dimension getDimension()
	{
		return this.dimension;
	}

	public boolean isShared()
	{
		return false;
	}

	public float getEmitterXOffset()
	{
		return this.emitterXOffset;
	}

	public float getEmitterYOffset()
	{
		return this.emitterYOffset;
	}

	public float getAlphaFactor()
	{
		return this.alphaFactor;
	}

	public void setAlphaFactor(float alphaFactor)
	{
		this.alphaFactor = alphaFactor;
	}

	public boolean shouldUpdate()
	{
		return true;
	}

	public boolean isAdditive()
	{
		return this.additive;
	}

	public void setAdditive(boolean additive)
	{
		this.additive = additive;
	}

	public void setStencilled(boolean stencilled)
	{
		this.stencilled = stencilled;
	}

	public boolean isStencilled()
	{
		return this.stencilled;
	}
}
