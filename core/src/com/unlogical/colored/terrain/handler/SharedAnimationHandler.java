package com.unlogical.colored.terrain.handler;

import java.util.HashMap;

import com.unlogical.colored.level.Level;
import com.unlogical.colored.terrain.TileDefiner;
import com.unlogical.colored.terrain.TileType;

public class SharedAnimationHandler
{
	private static HashMap<TileDefiner, Integer> tileAnimationDurations = new HashMap<TileDefiner, Integer>();

	public static void init()
	{
		tileAnimationDurations.clear();

		tileAnimationDurations.put(new TileDefiner(TileType.KEYGATE, 0), 50);
		tileAnimationDurations.put(new TileDefiner(TileType.KEYGATE, 1), 25);
		tileAnimationDurations.put(new TileDefiner(TileType.KEYGATE, 2), 50);

		tileAnimationDurations.put(new TileDefiner(TileType.LEVERGATE, 0), 50);
		tileAnimationDurations.put(new TileDefiner(TileType.LEVERGATE, 1), 25);
		tileAnimationDurations.put(new TileDefiner(TileType.LEVERGATE, 2), 50);

		tileAnimationDurations.put(new TileDefiner(TileType.WOODEN_CAGE, 0), 50);
		tileAnimationDurations.put(new TileDefiner(TileType.WOODEN_CAGE, 1), 50);
		tileAnimationDurations.put(new TileDefiner(TileType.WOODEN_CAGE, 2), 50);
	}

	public static int getPerImageDuration(TileType tileType, Level level, int innerType)
	{
		for (TileDefiner definer : tileAnimationDurations.keySet())
		{
			if (definer.getInnerType() == innerType && definer.getTileType() == tileType)
			{				
				return tileAnimationDurations.get(definer).intValue();
			}
		}

		throw new RuntimeException("Nooooo! Must.. request.. invalid.. per .. image .. animation .. durations.. (" + tileType + " " + innerType + " doesn't exist.)");
	}
}
