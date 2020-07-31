package com.unlogical.colored.util;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public strictfp class GameObject
{
	public static final String ENABLE_RENDERING_EVENT = "enableRendering";

	protected TextureRegion image;
	protected Vector2 position;

	protected boolean hidden;
	protected boolean enabled = true;
	protected boolean removed;

	protected boolean shared;
	protected boolean reSimulating;
	protected boolean dimensionUnique;

	protected Dimension dimension;

	public GameObject(TextureRegion image, Vector2 position)
	{
		this.image = image;
		this.position = position;
	}

	@Override
	public String toString()
	{
		return "[" + this.getClass().getSimpleName() + ": dimension:" + this.dimension.toString().toLowerCase() + (this.removed ? " removed" : "") + (this.shared ? " shared" : "") + this.customToString() + " x:" + this.position.x + " y:" + this.position.y + "]";
	}

	public String customToString()
	{
		return "";
	}

	public void setPosition(float x, float y)
	{
		this.position.set(x, y);

		this.updateHitbox();
	}

	public void setX(float x)
	{
		this.position.set(x, this.position.y);

		this.updateHitbox();
	}

	public void setY(float y)
	{
		this.position.set(this.position.x, y);

		this.updateHitbox();
	}

	public void adjustX(float x)
	{
		this.position.set(this.position.x + x, this.position.y);

		this.updateHitbox();
	}

	public void adjustY(float y)
	{
		this.position.set(this.position.x, this.position.y + y);

		this.updateHitbox();
	}

	public void updateHitbox()
	{

	}

	public void draw(Batch batch)
	{
		this.draw(batch, this.dimension);
	}

	public void draw(Batch batch, Dimension dimension)
	{
		if (!this.hidden && this.position != null)
		{
			batch.draw(this.image, this.position.x, this.position.y);
		}
	}

	public void setPosition(Vector2 position)
	{
		this.position = position;

		this.updateHitbox();
	}

	public boolean inSameDimension(GameObject other)
	{
		return other.getDimension() == this.dimension || !other.dimensionUnique || other.shared || this.shared;
	}

	public Dimension getDimension()
	{
		return this.dimension;
	}

	public void setDimension(Dimension dimension)
	{
		this.dimension = dimension;
		this.onDimensionChanged(dimension);
	}

	protected void onDimensionChanged(Dimension newDimension)
	{

	}

	public boolean isShared()
	{
		return this.shared;
	}

	public void setShared(boolean shared)
	{
		if (this.shared == shared ? false : (this.shared = shared) || true)
		{
			this.onSharingChanged();
		}
	}

	public void onSharingChanged()
	{
		throw new UnsupportedOperationException("Can you read..? You are supposed to implement this when you want something to be shareable! That's what it's called, damnit!");
	}

	public boolean isHidden()
	{
		return this.hidden;
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	public void hide()
	{
		this.hidden = true;
	}

	public void show()
	{
		this.hidden = false;
	}

	public boolean isRemoved()
	{
		return this.removed;
	}

	public void remove()
	{
		this.removed = true;
	}

	public void reAdd()
	{
		this.removed = false;
	}

	public void enable()
	{
		this.enabled = true;
	}

	public void disable()
	{
		this.enabled = false;
	}

	public TextureRegion getImage()
	{
		return this.image;
	}

	public void setImage(TextureRegion image)
	{
		this.image = image;
	}

	public Vector2 getPosition()
	{
		return this.position;
	}

	public float getX()
	{
		return this.position.x;
	}

	public float getY()
	{
		return this.position.y;
	}

	public boolean shouldHandleAsShared()
	{
		return this.shared;
	}

	public boolean isReSimulating()
	{
		return this.reSimulating;
	}

	public void setReSimulating(boolean reSimulating)
	{
		this.reSimulating = reSimulating;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}
}
