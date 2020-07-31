package com.unlogical.colored.terrain.property;


public class TileProperty
{
	private boolean blocked = false;
	private boolean animated = false;
	private boolean renderBehindEntities = false;
	private boolean renderBehindTiles = false;
	private boolean hasTextures = true;
	private boolean climbable = false;
	private boolean horizontalResizable = true;
	private boolean verticalResizable = true;
	private boolean rotatable = true;
	private boolean forceRotationStep = false;
	private boolean shouldScaleImage = false;
	private boolean useDefaultSize = false;

	private float slowdown = 1.0f;
	private float rotationStep = 1.0f;

	private int touchDamage = 0;
	private int brightness = 0;

	// for constructing convenience
	public TileProperty()
	{

	}

	public TileProperty copy()
	{
		TileProperty copy = new TileProperty();

		copy.blocked = blocked;
		copy.animated = animated;
		copy.renderBehindTiles = renderBehindTiles;
		copy.hasTextures = hasTextures;
		copy.climbable = climbable;
		copy.horizontalResizable = horizontalResizable;
		copy.verticalResizable = verticalResizable;
		copy.rotatable = rotatable;
		copy.forceRotationStep = forceRotationStep;
		copy.slowdown = slowdown;
		copy.rotationStep = rotationStep;
		copy.touchDamage = touchDamage;
		copy.brightness = brightness;
		copy.shouldScaleImage = shouldScaleImage;
		copy.renderBehindEntities = renderBehindEntities;

		return copy;
	}

	public TileProperty useDefaultSize()
	{
		this.useDefaultSize = true;

		return this;
	}

	public TileProperty setRotationStep(float rotationStep)
	{
		this.rotationStep = rotationStep;
		this.forceRotationStep = true;

		return this;
	}

	public TileProperty setHorizontallyFixed()
	{
		this.horizontalResizable = false;

		return this;
	}

	public TileProperty setVerticallyFixed()
	{
		this.verticalResizable = false;

		return this;
	}

	public TileProperty setRotationFixed()
	{
		this.rotatable = false;

		return this;
	}

	public TileProperty setFixed()
	{
		return this.setHorizontallyFixed().setVerticallyFixed().setRotationFixed();
	}

	public TileProperty setBlocked()
	{
		this.blocked = true;

		return this;
	}

	public TileProperty setSlowdown(float slowdown)
	{
		this.slowdown = slowdown;

		return this;
	}

	public TileProperty setTouchDamage(int touchDamage)
	{
		this.touchDamage = touchDamage;

		return this;
	}

	public TileProperty setAnimated()
	{
		this.animated = true;

		return this;
	}

	public TileProperty setHasNoTextures()
	{
		this.setHasTextures(false);

		return this;
	}

	public TileProperty setRenderBehindTiles()
	{
		this.renderBehindTiles = true;

		return this;
	}

	public TileProperty setRenderBehindEntities()
	{
		this.renderBehindEntities = true;

		return this;
	}

	public TileProperty setClimbable()
	{
		this.climbable = true;

		return this;
	}

	public TileProperty setScaleImage()
	{
		this.shouldScaleImage = true;

		return this;
	}

	public boolean isBlocked()
	{
		return this.blocked;
	}

	public void setBlocked(boolean blocked)
	{
		this.blocked = blocked;
	}

	public float getSlowdown()
	{
		return slowdown;
	}

	public int getTouchDamage()
	{
		return touchDamage;
	}

	public boolean isAnimated()
	{
		return animated && hasTextures();
	}

	public void setAnimated(boolean animated)
	{
		this.animated = animated;
	}

	public int getBrightness()
	{
		return brightness;
	}

	public TileProperty setBrightness(int brightness)
	{
		this.brightness = brightness;

		return this;
	}

	public boolean isClimbable()
	{
		return climbable;
	}

	public void setHasTextures(boolean hasTextures)
	{
		this.hasTextures = hasTextures;
	}

	public boolean hasTextures()
	{
		return hasTextures;
	}

	public boolean shouldRenderBehindTiles()
	{
		return renderBehindTiles;
	}

	public boolean shouldRenderBehindEntities()
	{
		return renderBehindEntities;
	}

	public boolean isHorizontalResizable()
	{
		return horizontalResizable;
	}

	public boolean isRotatable()
	{
		return rotatable;
	}

	public boolean isVerticalResizable()
	{
		return verticalResizable;
	}

	public float getRotationStep()
	{
		return rotationStep;
	}

	public boolean isForceRotationStep()
	{
		return forceRotationStep;
	}

	public boolean shouldScaleImage()
	{
		return shouldScaleImage;
	}

	public boolean shouldUseDefaultSize()
	{
		return useDefaultSize;
	}
}
