package com.unlogical.colored.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

public interface IExtendedSpriteBatch extends Batch
{
	public float getAdditiveColor();

	public void setAdditiveColor(Color color);

	public void setAdditiveColor(float color);

	public float getGrayScaleFactor();

	public void setGrayScaleFactor(float factor);

	public float getSaturation();

	public void setSaturation(float factor);

	public Color getAmbientColor();

	public void setAmbientColor(Color color);

	public void setAmbientColor(float r, float g, float b, float a);
}
