package com.unlogical.colored.util;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.unlogical.colored.level.Level;

public interface ILightSource
{
	public LightType getLightType();

	public Color getColor();

	public float getPositionX();

	public float getPositionY();

	public float getIntensity();
	
	public int getDistance();

	public int getSpotDirection();

	public int getAngle();
	
	public Level getLevel();
	
	public List<ILightSource> getConnectedLights();
}
