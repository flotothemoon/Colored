package com.unlogical.colored.util;

public enum Dimension
{
	COLORED(0),
	MONOCHROME(1);
	
	public static final int COUNT = values().length;
	
	private int code;
	
	private Dimension(int code)
	{
		this.code = code;
	}
	
	public int code()
	{
		return this.code;
	}
	
	public static int count()
	{
		return 2;
	}
	
	public static Dimension getDimension(int code)
	{
		if (code == 0)
		{
			return COLORED;
		}
		else if (code == 1)
		{
			return MONOCHROME;
		}
		
		throw new IllegalArgumentException("Dimension code "+code+" doesn't even exist! Think before coding...");
	}

	public static Dimension getOpposite(Dimension dimension)
	{
		if (dimension == COLORED)
		{
			return MONOCHROME;
		}
		else
		{
			return COLORED;
		}
	}
	
	public Dimension getOpposite()
	{
		return getOpposite(this);
	}
}
