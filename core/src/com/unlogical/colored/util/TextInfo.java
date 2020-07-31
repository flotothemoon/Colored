package com.unlogical.colored.util;

public class TextInfo
{
	public final String text;
	public final float velocity;
	public final boolean immediate;

	public TextInfo(String text)
	{
		this(text, 1.0f, false);
	}

	public TextInfo(String text, float velocity)
	{
		this(text, velocity, false);
	}

	public TextInfo(String text, boolean immediate)
	{
		this(text, 1.0f, immediate);
	}

	public TextInfo(String text, float velocity, boolean immediate)
	{
		this.text = text;
		this.velocity = velocity;
		this.immediate = immediate;
	}
}
