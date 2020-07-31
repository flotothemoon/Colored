package com.unlogical.colored.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//dummy class for testing
public class ExtendedTestSpriteBatch extends SpriteBatch implements IExtendedSpriteBatch
{
	@Override
	public void setAdditiveColor(Color color)
	{

	}

	@Override
	public void setAdditiveColor(float color)
	{

	}

	@Override
	public float getAdditiveColor()
	{
		return Color.BLACK.toFloatBits();
	}

	@Override
	public float getGrayScaleFactor()
	{
		return 0.0f;
	}

	@Override
	public float getSaturation()
	{
		return 1.0f;
	}

	@Override
	public void setSaturation(float factor)
	{

	}

	@Override
	public void setGrayScaleFactor(float factor)
	{

	}

	@Override
	public void setAmbientColor(float r, float g, float b, float a)
	{

	}

	@Override
	public void setAmbientColor(Color color)
	{

	}

	@Override
	public Color getAmbientColor()
	{
		return Color.WHITE;
	}
}
