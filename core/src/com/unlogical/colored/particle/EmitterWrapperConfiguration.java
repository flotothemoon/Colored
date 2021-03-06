package com.unlogical.colored.particle;

import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.util.LevelObject;

public class EmitterWrapperConfiguration implements IEmitterWrapper
{
	private int emitterWidth;
	private int emitterHeight;
	private int maxParticles;

	private float rotation;
	private float emitterParallax = 1.0f;

	private boolean limitToImage;

	private Color startColor;
	private Color endColor;
	private Color addColor;
	private IColorDistributionMode colorDistrMode;
	private LevelObject wrappedObject;
	private Hitbox hitbox;
	private TextureRegion image;
	private TextureRegion[][] sprites;
	private ParticleEmitterImpl emitter;
	private EmitterType type;

	public EmitterWrapperConfiguration(LevelObject wrappedObject, EmitterType type, Color startColor, Color endColor, Hitbox hitbox, int emitterWidth, int emitterHeight, float rotation, int maxParticles, TextureRegion image, boolean limitToImage)
	{
		this.init(wrappedObject, type, startColor, endColor, hitbox, emitterWidth, emitterHeight, rotation, maxParticles, image, limitToImage);
	}

	private EmitterWrapperConfiguration()
	{

	}

	private void init(LevelObject wrappedObject, EmitterType type, Color startColor, Color endColor, Hitbox hitbox, int emitterWidth, int emitterHeight, float rotation, int maxParticles, TextureRegion image, boolean limitToImage)
	{
		this.startColor = startColor;
		this.endColor = endColor;
		this.addColor = type.addTint;
		this.colorDistrMode = type.colorDistrMode;
		this.hitbox = hitbox;
		this.emitterWidth = emitterWidth;
		this.emitterHeight = emitterHeight;
		this.sprites = type.sprites;
		this.maxParticles = maxParticles;
		this.image = image;
		this.limitToImage = limitToImage;
		this.rotation = rotation;
		this.wrappedObject = wrappedObject;
		this.type = type;

		if (type == EmitterType.PIXELIFY)
		{
			this.maxParticles = image.getRegionWidth() / LevelObject.PIXELIFIED_PARTICLE_SIZE * (image.getRegionHeight() / LevelObject.PIXELIFIED_PARTICLE_SIZE);
		}
	}

	@Override
	public void applyCustomInit(ParticleEmitterImpl emitter)
	{

	}

	@Override
	public IEmitterWrapper deepCopy(Map<Object, Object> copiedReferences)
	{
		EmitterWrapperConfiguration copy = new EmitterWrapperConfiguration();

		Level.putCopy(this, copy, copiedReferences);

		copy.init((LevelObject) Level.getCopy(this.wrappedObject, copiedReferences), this.type, this.startColor, this.endColor, (Hitbox) Level.getCopy(this.hitbox, copiedReferences), this.emitterWidth, this.emitterHeight, this.rotation, this.maxParticles, this.image, this.limitToImage);

		return copy;
	}

	@Override
	public boolean shouldCopy()
	{
		return this.wrappedObject.shouldCopy();
	}

	@Override
	public boolean canCreateParticleAt(float x, float y)
	{
		// TODO find way to get pixel alpha values in libgdx (hopefully
		// something other than pixmaps)

		return true;

		// return !limitToImage || getImage().getColor((int) (x /
		// (hitbox.getRawWidth() / getImage().getRegionWidth())), (int) (y /
		// (hitbox.getRawHeight() / getImage().getRegionHeight()))).a > 0.0f;
	}

	@Override
	public float getSpawnScaleFactor(EmitterType emitterType)
	{
		return 1.0f;
	}

	@Override
	public boolean shouldApplySpawnScaleFactor(EmitterType emitterType)
	{
		return true;
	}

	@Override
	public boolean shouldCreateParticles()
	{
		return true;
	}

	@Override
	public boolean isRotateable()
	{
		return true;
	}

	@Override
	public float getCenterOfRotationX()
	{
		return this.hitbox.getCenterX();
	}

	@Override
	public float getCenterOfRotationY()
	{
		return this.hitbox.getCenterY();
	}

	@Override
	public TextureRegion getImage()
	{
		return this.image;
	}

	@Override
	public IEmitterWrapper copy(Level level)
	{
		return null;
	}

	@Override
	public Random getRNG()
	{
		return this.wrappedObject.getLevel().getPRNG(this.isShared(), this.emitter.getDimension());
	}

	@Override
	public void setPosition(float x, float y)
	{
		this.wrappedObject.setPosition(x, y);
	}

	@Override
	public Vector2 getPosition()
	{
		return this.wrappedObject.getPosition();
	}

	@Override
	public Level getLevel()
	{
		return this.wrappedObject.getLevel();
	}

	@Override
	public void setLevel(Level level)
	{

	}

	@Override
	public TextureRegion[][] getEmitterSprites()
	{
		return this.sprites;
	}

	@Override
	public Color getStartEmitterTint()
	{
		return this.startColor;
	}

	@Override
	public Color getEndEmitterTint()
	{
		return this.endColor;
	}

	@Override
	public IColorDistributionMode getColorDistributionMode()
	{
		return this.colorDistrMode;
	}

	@Override
	public float getEmitterParallax()
	{
		return this.emitterParallax;
	}

	@Override
	public int getEmitterWidth()
	{
		return this.emitterWidth;
	}

	@Override
	public int getEmitterHeight()
	{
		return this.emitterHeight;
	}

	@Override
	public int getMaxParticles()
	{
		return this.maxParticles;
	}

	@Override
	public float getRotation()
	{
		return this.rotation;
	}

	@Override
	public Hitbox getHitbox()
	{
		return this.hitbox;
	}

	@Override
	public void setRotation(float rotation)
	{
		this.rotation = rotation;
	}

	@Override
	public int getEmitterDepth()
	{
		return 0;
	}

	@Override
	public boolean isInBackground()
	{
		return false;
	}

	@Override
	public Color getAddEmitterTint()
	{
		return this.addColor;
	}

	@Override
	public boolean validTextures()
	{
		return true;
	}

	@Override
	public boolean isTimeEffected()
	{
		return true;
	}

	@Override
	public boolean isShared()
	{
		return this.wrappedObject.isShared() || this.wrappedObject.shouldHandleAsShared();
	}

	@Override
	public boolean shouldUpdateEmitter()
	{
		return true;
	}

	public ParticleEmitterImpl getEmitter()
	{
		return this.emitter;
	}

	public void setEmitter(ParticleEmitterImpl emitter)
	{
		this.emitter = emitter;
	}

	@Override
	public boolean isTimeEffectedAnyWay()
	{
		return this.wrappedObject.isTimeEffectedAnyWay();
	}

	@Override
	public boolean forceDirectPositionHack()
	{
		return false;
	}

	public void setWrappedObject(LevelObject wrappedObject)
	{
		this.wrappedObject = wrappedObject;
	}

	public LevelObject getWrappedObject()
	{
		return this.wrappedObject;
	}
}
