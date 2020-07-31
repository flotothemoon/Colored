package com.unlogical.colored.terrain;

public class TileDefiner
{
	private TileType tileType;
	private int innerType;

	public TileDefiner(TileType type, int innerType)
	{
		this.tileType = type;
		this.innerType = innerType;
	}

	public TileDefiner set(TileType tileType, int innerType)
	{
		this.tileType = tileType;
		this.innerType = innerType;

		return this;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof TileDefiner))
		{
			return false;
		}
		else
		{
			return ((TileDefiner) obj).getTileType() == this.tileType && ((TileDefiner) obj).getInnerType() == this.innerType;
		}
	}

	public TileType getTileType()
	{
		return tileType;
	}

	public int getInnerType()
	{
		return innerType;
	}
}
