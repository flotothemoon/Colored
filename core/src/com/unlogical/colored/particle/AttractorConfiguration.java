package com.unlogical.colored.particle;

public enum AttractorConfiguration
{
	DEFAULT_ENTITY(0.6f, 1.0f),
	
	;
	
	private float attractionFactor;
	private float attractionRange;
	
	private AttractorConfiguration(float attractionFactor, float attractionRange)
	{
		this.attractionFactor = attractionFactor;
		this.attractionRange = attractionRange;
	}

	public float getAttractionFactor()
	{
		return attractionFactor;
	}

	public float getAttractionRange()
	{
		return attractionRange;
	}
}
