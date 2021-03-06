package com.unlogical.colored.terrain.tile;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.saving.SaveHandler;
import com.unlogical.colored.saving.SaveState;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.ILightSource;
import com.unlogical.colored.util.LevelObject;
import com.unlogical.colored.util.LightType;

public class TileWorldDoor extends TileDoor implements ILightSource
{
	private static final int CLEARED_OUTLINE_SIZE = 10;

	private static final Color lightColor = new Color(0.9882353f, 0.8608475f, 0.0038754323f, 1.0f);

	private float intensityFactor = 1.0f;
	private float intensityOffset;

	private boolean worldCompleted;
	private int clearedCount;

	private SortedMap<String, Boolean> worldLevels = new TreeMap<String, Boolean>();

	public TileWorldDoor(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		TileWorldDoor copy = (TileWorldDoor) super.deepCopy(copiedReferences);

		copy.worldCompleted = this.worldCompleted;

		return copy;
	}

	@Override
	public void init()
	{
		super.init();

		Set<String> levelIDs = Level.getAllLevelIDs();
		SaveState activeSave = SaveHandler.getActiveSave();

		this.worldCompleted = true;
		this.worldLevels.clear();

		this.clearedCount = 0;

		for (String levelID : levelIDs)
		{
			if (levelID.startsWith(this.level.getWorldID()) && !levelID.equals(this.level.getID()) && !levelID.substring(levelID.indexOf("-") + 1).equals("b"))
			{
				Object[] data = activeSave.readLevel(levelID, Level.LEVEL_CLEARED_TOKEN);

				this.worldLevels.put(levelID, data != null && Boolean.parseBoolean((String) data[0]));

				if (!this.worldLevels.get(levelID))
				{
					this.worldCompleted = false;
				}
				else
				{
					this.clearedCount++;
				}
			}
		}

		this.level.writeToWorldSave(Level.WORLD_FINISHED, this.worldCompleted);

		if (this.worldCompleted)
		{
			this.intensityFactor = 1.3f;
		}
		else
		{
			this.intensityFactor = 0.3f + (float) this.clearedCount / (float) this.worldLevels.size();
		}
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{
		super.onUpdate(delta, rng);

		this.intensityOffset = (float) Math.sin(Math.toRadians(this.age / 4.0f)) / 8.0f;

		float clearFactor = (float) this.clearedCount / this.worldLevels.size();

		this.outlineSize = clearFactor * CLEARED_OUTLINE_SIZE;
	}

	@Override
	public boolean shouldUpdate()
	{
		return true;
	}

	@Override
	public Color getColor()
	{
		return lightColor;
	}

	@Override
	public float getPositionX()
	{
		return this.getCenterX();
	}

	@Override
	public float getPositionY()
	{
		return this.getCenterY();
	}

	@Override
	public float getIntensity()
	{
		return this.intensityFactor * (1.0f + this.intensityOffset);
	}

	@Override
	public int getSpotDirection()
	{
		return 0;
	}

	@Override
	public int getAngle()
	{
		return 360;
	}

	@Override
	public LightType getLightType()
	{
		return LightType.CONE;
	}

	@Override
	public int getDistance()
	{
		return 200;
	}

	@Override
	public List<ILightSource> getConnectedLights()
	{
		return null;
	}
}
