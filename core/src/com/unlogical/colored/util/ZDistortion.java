package com.unlogical.colored.util;

public abstract class ZDistortion
{
	private boolean limitedLifetime;
	private int lifetime;
	protected float age;
	protected float ageFactor;
	protected float invertedAgeFactor;
	
	public ZDistortion()
	{
		
	}
	
	public ZDistortion(int lifetime)
	{
		if (lifetime <= 0)
		{
			throw new IllegalArgumentException("Umm. Negative lifetime... sure... (lifetime=" + lifetime + ")");
		}
		
		this.lifetime = lifetime;
		this.limitedLifetime = true;
	}
	
	public void update(int delta)
	{
		age += delta;
		
		if (limitedLifetime)
		{
			ageFactor = 1.0f - (age) / (lifetime);
			invertedAgeFactor = 1.0f - ageFactor;
		}
	}
	
	public boolean isFinished()
	{
		return limitedLifetime  && age >= lifetime;
	}

	public abstract float getZDistortion(float x, float y);
}