package com.unlogical.colored.terrain;

import com.unlogical.colored.terrain.property.TileProperty;

public enum TileType
{
	CAMERABLOCK("CameraBlock", new TileProperty().setRotationFixed().setScaleImage().useDefaultSize()),
	MARKER("Marker", new TileProperty().setFixed()),
	TEXT("Text", new TileProperty().setRotationFixed()),

	BASIC_TILE("BasicTile", new TileProperty().setBlocked().setHasNoTextures()),
	BASIC_BLOCKEDABOVE("BlockedAbove", new TileProperty().setBlocked().setVerticallyFixed()),

	SPIKES("Spikes", new TileProperty().setTouchDamage(2).setRenderBehindEntities()),

	LIGHT("Light", new TileProperty().setVerticallyFixed().setHorizontallyFixed()),

	SOUND_SOURCE("SoundSource", new TileProperty().setFixed()),

	GRID("Grid", new TileProperty().setClimbable().setRenderBehindEntities().setRenderBehindTiles().setSlowdown(0.35f).setRotationFixed()),
	LADDER("Ladder", new TileProperty().setClimbable().setRenderBehindEntities().setRenderBehindTiles().setSlowdown(0.3f).setHorizontallyFixed().setRotationFixed()),

	LEVER("Lever", new TileProperty().setRenderBehindTiles().setFixed()),

	PLATFORM("Platform", new TileProperty().setBlocked().setRotationStep(90.0f)),
	PLATFORM_LEFT_EDGE("LeftPlatform", new TileProperty().setRotationStep(90.0f)),
	PLATFORM_RIGHT_EDGE("RightPlatform", new TileProperty().setRotationStep(90.0f)),

	KEYGATE("KeyGate", new TileProperty().setFixed().setBlocked().setAnimated()),
	LEVERGATE("LeverGate", new TileProperty().setFixed().setBlocked().setAnimated()),

	CANNON("Cannon", new TileProperty().setHorizontallyFixed().setVerticallyFixed().setBlocked()),

	HOLE("Hole", new TileProperty().setFixed().setRenderBehindEntities()),

	DOOR("Door", new TileProperty().setFixed().setRenderBehindEntities()),

	WOODEN_CAGE("Cage", new TileProperty().setFixed().setBlocked().setAnimated()),

	WORLD_DOOR("WorldDoor", new TileProperty().setFixed().setRenderBehindEntities()),

	SPAWNPOINT("Spawnpoint", new TileProperty().setFixed()),

	CLOUDPLATFORM("CloudPlatform", new TileProperty().setBlocked().setFixed().setRenderBehindTiles()),;

	private String tileName;
	private String unchangedName;
	private TileProperty properties;

	private TileType(String tileName, TileProperty properties)
	{
		this.unchangedName = tileName;
		this.tileName = tileName.toLowerCase();
		this.properties = properties;
	}

	public static TileProperty getProperty(TileType tileType)
	{
		return getProperty(tileType.getTileName());
	}

	public static TileProperty getProperty(String tileName)
	{
		tileName = tileName.toLowerCase();

		for (TileType tileType : TileType.values())
		{
			if (tileType.getTileName().equals(tileName))
			{
				return tileType.getProperties();
			}
		}

		return null;
	}

	public static TileType getTileType(String tileName)
	{
		tileName = tileName.toLowerCase();

		for (TileType tileType : TileType.values())
		{
			if (tileType.getTileName().equals(tileName))
			{
				return tileType;
			}
		}

		return null;
	}

	public String getTileName()
	{
		return this.tileName;
	}

	public TileProperty getProperties()
	{
		return this.properties;
	}

	public String getUnchangedName()
	{
		return this.unchangedName;
	}
}
