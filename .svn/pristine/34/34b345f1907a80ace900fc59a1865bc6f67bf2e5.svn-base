package com.unlogical.colored.entity.key;

import com.badlogic.gdx.graphics.Color;

public enum KeyPort
{
	RED(Color.RED),
	YELLOW(Color.YELLOW),
	GREEN(Color.GREEN),
	BLUE(Color.BLUE),
	PINK(Color.PINK),
	WHITE(Color.WHITE),
	BLACK(Color.BLACK);
	
	private Color color;

	private KeyPort(Color color)
	{
		this.color = color;
	}

	public static KeyPort getTrigger(int index)
	{
		return index >= 0 && index < values().length ? values()[index] : RED;
	}

	public static Color getColor(int index)
	{
		return index >= 0 && index < values().length ? values()[index].getColor() : RED.getColor();
	}

	public Color getColor()
	{
		return color;
	}

	public KeyPort next()
	{
		int index = this.ordinal();

		index++;

		if (index >= values().length)
		{
			index = 0;
		}

		return values()[index];
	}

	public KeyPort previous()
	{
		int index = this.ordinal();

		index--;

		if (index < 0)
		{
			index = values().length - 1;
		}

		return values()[index];
	}

	public int toPort()
	{
		return this.ordinal();
	}
}
