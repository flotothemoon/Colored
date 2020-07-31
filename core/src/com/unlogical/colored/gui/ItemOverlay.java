package com.unlogical.colored.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.resources.image.Images;
import com.unlogical.colored.resources.image.Images.HintIdentifier;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;

public class ItemOverlay
{
	private boolean fadingIn;
	private boolean fadingOut;
	private boolean addingScale;

	private int fadeInTime;
	private int fadeOutTime;

	private float lifeTime;
	private int displayTime;

	private float alpha;
	private float scale;

	private HintIdentifier identifier;

	private Vector2 base;

	private float xOffset;
	private float yOffset;

	public ItemOverlay(HintIdentifier identifier, Vector2 base, float xOffset, float yOffset, int displayTime, int fadeInTime, int fadeOutTime, float startAlpha)
	{
		init(identifier, base, xOffset, yOffset, displayTime, fadeInTime, fadeOutTime, startAlpha);
	}

	public ItemOverlay init(HintIdentifier identifier, Vector2 base, float xOffset, float yOffset, int displayTime, int fadeInTime, int fadeOutTime, float startAlpha)
	{
		this.identifier = identifier;
		this.fadeInTime = fadeInTime;
		this.fadeOutTime = fadeOutTime;

		this.base = base;

		this.xOffset = xOffset;
		this.yOffset = yOffset;

		this.displayTime = displayTime;

		this.alpha = startAlpha;
		this.fadingIn = true;
		this.fadingOut = false;
		this.lifeTime = 0;

		return this;
	}

	public void update(float delta)
	{
		if (fadingIn)
		{
			if (alpha < 1.0f)
			{
				alpha += (delta) / (fadeInTime);
			}

			if (lifeTime >= fadeInTime)
			{
				fadingIn = false;
			}
		}
		
		if (!fadingOut)
		{
			scale += Button.WOBBLE_AVAILABLE_SPEED / 3.0f * delta * (addingScale ? 1.0f : -1.0f);

			if (scale > Button.WOBBLE_UNAVAILABLE_MAX_SCALE_FACTOR)
			{
				addingScale = false;
			}
			else if (scale < Button.WOBBLE_AVAILABLE_MIN_SCALE_FACTOR)
			{
				addingScale = true;
			}
		}
		else if (scale != 1.0f)
		{
			scale = (scale - 1.0f) * Button.WOBBLE_AVAILABLE_SLOW_DOWN_FACTOR + 1.0f;
		}

		if (!fadingOut && lifeTime >= displayTime - fadeOutTime)
		{
			fadingOut = true;
		}

		if (fadingOut && alpha > 0.0f)
		{
			alpha -= (delta) / (fadeOutTime);

			if (alpha < 0.0f)
			{
				alpha = 0.0f;
			}
		}

		lifeTime += delta;
	}

	public void draw(Batch batch)
	{		
		if (identifier != null)
		{
			Renderer.useSpriteBatch();

			String imageIdentifier;

			if (identifier.linkedAction == null)
			{
				imageIdentifier = identifier.value;
			}
			else
			{
				imageIdentifier = InputHandler.getInputHintIdentifier(identifier.linkedAction);
			}

			
			Images.draw(imageIdentifier, base.x + xOffset, base.y + yOffset, scale, 0.0f, ColorHelper.applyAlphaAndCopy(Color.WHITE, alpha * GameLauncher.getLevelContext().getControlAlphaFactor()), batch);
		}
	}

	public boolean isFadingIn()
	{
		return fadingIn;
	}

	public void setFadingIn(boolean fadingIn)
	{
		this.fadingIn = fadingIn;
	}

	public boolean isFadingOut()
	{
		return fadingOut;
	}

	public void setFadingOut(boolean fadingOut)
	{
		this.fadingOut = fadingOut;
	}

	public float getLifeTime()
	{
		return lifeTime;
	}

	public void setLifeTime(float lifeTime)
	{
		this.lifeTime = lifeTime;
	}

	public float getAlpha()
	{
		return alpha;
	}

	public void setAlpha(float alpha)
	{
		this.alpha = alpha;
	}

	public Vector2 getBase()
	{
		return base;
	}

	public void setBase(Vector2 base)
	{
		this.base = base;
	}

	public float getXOffset()
	{
		return xOffset;
	}

	public void setXOffset(float xOffset)
	{
		this.xOffset = xOffset;
	}

	public float getYOffset()
	{
		return yOffset;
	}

	public void setYOffset(float yOffset)
	{
		this.yOffset = yOffset;
	}

	public int getFadeInTime()
	{
		return fadeInTime;
	}

	public void setFadeInTime(int fadeInTime)
	{
		this.fadeInTime = fadeInTime;
	}

	public int getFadeOutTime()
	{
		return fadeOutTime;
	}

	public void setFadeOutTime(int fadeOutTime)
	{
		this.fadeOutTime = fadeOutTime;
	}

	public int getDisplayTime()
	{
		return displayTime;
	}

	public void setDisplayTime(int displayTime)
	{
		this.displayTime = displayTime;
	}
	
	public HintIdentifier getIdentifier()
	{
		return identifier;
	}
	
	public void setIdentifier(HintIdentifier identifier)
	{
		this.identifier = identifier;
	}
}