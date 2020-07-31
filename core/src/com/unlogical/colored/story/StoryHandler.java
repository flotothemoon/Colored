package com.unlogical.colored.story;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.entity.EntityLiving;
import com.unlogical.colored.entity.key.EntityKey;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.filesystem.ConfigFileHandler;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.gui.panel.CheatPanel;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.saving.SaveHandler;
import com.unlogical.colored.terrain.tile.TileDoor;
import com.unlogical.colored.terrain.tile.cage.TileCage;
import com.unlogical.colored.terrain.tile.logical.TileLever;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.IDeepCopyable;
import com.unlogical.colored.util.Transitions;

@SuppressWarnings("unused")
public class StoryHandler implements IDeepCopyable<StoryHandler>
{
	private static int totalCages;
	private static Map<String, Integer> totalCagesPerWorld = new HashMap<String, Integer>();
	private static Map<String, Integer> totalCagesPerLevel = new HashMap<String, Integer>();

	private static int timeSinceLastShatter;
	private static int randomCheckCooldown;
	private static Random rng = new Random();

	private Level level;
	private CustomStoryHandler customStoryHandler;
	private Dimension backgroundDimension;
	private Dimension foregroundDimension;
	private boolean shattering;
	private float shatterIntensity;
	private float shatterFactor;
	private int totalShatterLength;
	private int shatterLength;

	public static void init()
	{
		totalCages = 0;
		totalCagesPerWorld.clear();
		totalCagesPerLevel.clear();

		for (String levelID : Level.getLevelMap().keySet())
		{
			if (levelID.contains("-") && !levelID.substring(levelID.indexOf("-") + 1).equals("b"))
			{
				String worldID = levelID.substring(0, levelID.indexOf("-"));
				ConfigFileHandler cfg = new ConfigFileHandler(FileManager.getFileQuietly(Level.getMapPathByID(levelID) + "/config.yml"));

				int cages = cfg.getInteger("totalcages");

				totalCagesPerLevel.put(levelID, cages);
				totalCagesPerWorld.put(worldID, (totalCagesPerWorld.containsKey(worldID) ? totalCagesPerWorld.get(worldID) : 0) + cages);
				totalCages += cages;
			}
		}
	}

	public StoryHandler(Level level)
	{
		this.level = level;
		this.customStoryHandler = CustomStoryHandler.getHandlerFor(level);
	}

	private StoryHandler()
	{
	}

	@Override
	public StoryHandler deepCopy(Map<Object, Object> copiedReferences)
	{
		StoryHandler copy = new StoryHandler();

		copy.shattering = this.shattering;
		copy.shatterIntensity = this.shatterIntensity;
		copy.shatterLength = this.shatterLength;
		copy.level = (Level) Level.getCopy(this.level, copiedReferences);
		copy.customStoryHandler = (CustomStoryHandler) Level.getCopy(this.customStoryHandler, copiedReferences);

		return copy;
	}

	@Override
	public boolean shouldCopy()
	{
		return true;
	}

	public void update(int delta)
	{
		String currentWorld = this.level.getWorldID();
		String currentLevel = this.level.getID();

		if (!currentLevel.contains("-") || GameLauncher.isDemoMode())
		{
			return;
		}

		randomCheckCooldown -= delta;

		if (GameLauncher.isDeveloperMode() && Gdx.input.isKeyJustPressed(Input.Keys.Q))
		{
			this.shatter(5.0f);
		}

		if (this.shattering)
		{
			this.shatterLength -= delta;

			if (this.shatterLength < 0)
			{
				this.shatterLength = 0;
				this.shattering = false;
			}

			this.shatterFactor = Transitions.getFactor((float) this.shatterLength / (float) this.totalShatterLength);
			float xOffset = (float) Math.sin(this.shatterLength) * rng.nextInt((int) (20 * this.shatterIntensity + 1)) * this.shatterFactor;
			float yOffset = (float) Math.sin(this.shatterLength) * rng.nextInt((int) (20 * this.shatterIntensity + 1)) * this.shatterFactor;

			this.level.setXOffset(Dimension.MONOCHROME, xOffset);
			this.level.setYOffset(Dimension.MONOCHROME, yOffset);

			if (this.shatterIntensity > 10.0f)
			{
				this.level.setXOffset(Dimension.COLORED, -xOffset / 3);
				this.level.setYOffset(Dimension.COLORED, -yOffset / 3);
			}
		}
		else if (randomCheckCooldown < 0 && !this.level.getControllingPlayer().isJumping())
		{
			int totalCagesWorld = totalCagesPerWorld.get(currentWorld);
			int openCagesWorld = readIntFromWorld(currentWorld, Level.WORLD_CLEARED_CAGE_COUNT);
			int totalCagesLevel = this.level.isBonusLevel() ? 0 : totalCagesPerLevel.get(currentLevel);
			int openCagesLevel = readIntFromLevel(currentLevel, Level.LEVEL_CLEARED_CAGE_COUNT);
			int totalOpenCages = 0;

			for (String world : totalCagesPerWorld.keySet())
			{
				Object[] data = SaveHandler.getActiveSave().readWorld(world, Level.WORLD_CLEARED_CAGE_COUNT);

				totalOpenCages += data != null ? Integer.parseInt((String) data[0]) : 0;
			}

			float totalCleared = (float) totalOpenCages / (float) totalCages;
			float worldCleared = (float) openCagesWorld / (float) totalCagesWorld;
			float levelCleared = totalCagesLevel == 0 ? this.level.isBonusLevel() ? openCagesLevel : 0.0f : (float) openCagesLevel / (float) totalCagesLevel;

			if (worldCleared == 1.0f)
			{
				worldCleared = 0.0f;
			}

			if (this.shouldShatterWorldLikeMad())
			{
				levelCleared = worldCleared = totalCleared = 1.0f;
			}

			int shatterLimit = (int) (10000 / this.customStoryHandler.getShatteringMultiplier());

			if (this.shouldWorldBeConsideredForShattering())
			{
				shatterLimit += 100000 - worldCleared * worldCleared * 100000;
			}

			if (this.shouldTotalBeConsideredForShattering())
			{
				shatterLimit += 70000 - totalCleared * totalCleared * 70000;
			}

			if (this.shouldLeveldBeConsideredForShattering())
			{
				shatterLimit += 20000 - levelCleared * 20000;
			}

			timeSinceLastShatter += delta;

			if (shatterLimit > 0 && rng.nextInt(shatterLimit) < timeSinceLastShatter)
			{
				float baseStrength = (float) timeSinceLastShatter / (float) shatterLimit * 50;

				this.shatter(baseStrength * this.customStoryHandler.getShatteringMultiplier());
			}

			randomCheckCooldown = 200;
		}

		this.customStoryHandler.update(delta);
	}

	public void render(Batch batch)
	{

	}

	public void shatter(float strength)
	{
		if (GameLauncher.isDemoMode()) // no shattering in demo mode
		{
			return;
		}

		if (this.shattering)
		{
			this.level.setXOffset(Dimension.COLORED, 0.0f);
			this.level.setXOffset(Dimension.MONOCHROME, 0.0f);
			this.level.setYOffset(Dimension.COLORED, 0.0f);
			this.level.setYOffset(Dimension.MONOCHROME, 0.0f);
		}

		this.shattering = true;
		this.shatterLength = (int) (strength * 500);
		this.totalShatterLength = this.shatterLength;
		this.shatterIntensity = strength;
		this.backgroundDimension = this.level.getBackgroundDimension();
		this.foregroundDimension = this.level.getForegroundDimension();

		timeSinceLastShatter = 0;
	}

	private static Integer readIntFromWorld(String world, String key)
	{
		Object[] data = SaveHandler.getActiveSave().readWorld(world, key);

		return data != null ? Integer.parseInt((String) data[0]) : 0;
	}

	private static Integer readIntFromLevel(String level, String key)
	{
		Object[] data = SaveHandler.getActiveSave().readLevel(level, key);

		return data != null ? Integer.parseInt((String) data[0]) : 0;
	}

	public void onPreLevelInit()
	{
		if (!GameLauncher.isDemoMode() && !this.level.getID().equals("20-01") && !isPreviousWorldCompleted(this.level, this.level.getID()) && isFirstLevel(this.level, this.level.getID()))
		{
			this.level.setLevelType(LevelType.NORMAL);
		}

		this.customStoryHandler.onPreLevelInit();
	}

	public void onLevelInit()
	{
		if (!this.level.getWorldID().equals("20") && this.level.getInternalID().equals("01") && this.level.getType() == LevelType.NORMAL)
		{
			this.level.setChapter(Integer.parseInt(this.level.getWorldID()) - 1 + "");
			this.level.setName("Out Of Reach");

			for (String levelID : Level.getLevelMap().keySet())
			{
				if (levelID.startsWith(this.level.getChapter()) && CheatPanel.isStringNumeric(levelID.substring(levelID.indexOf("-") + 1)) && Integer.parseInt(levelID.substring(levelID.indexOf("-") + 1)) >= Integer.parseInt(this.level.getInternalID()))
				{
					this.level.setInternalID(Integer.parseInt(levelID.substring(levelID.indexOf("-") + 1)) + 1 + "");
				}
			}
		}

		this.customStoryHandler.onLevelInit();
	}

	public void onLevelEntry(boolean viaDoor)
	{
		this.customStoryHandler.onLevelEntry(viaDoor);
	}

	public void onLevelExit(TileDoor door)
	{
		this.customStoryHandler.onLevelExit(door);
	}

	public void onPlayerMoved(EntityPlayer player)
	{
		this.customStoryHandler.onPlayerMoved(player);
	}

	public void onLeverPulled(TileLever lever, EntityPlayer interactor, boolean realInput)
	{
		this.customStoryHandler.onLeverPulled(lever, interactor, realInput);
	}

	public boolean canLeverBePulled(TileLever lever, EntityPlayer interactor)
	{
		return this.customStoryHandler.canLeverBePulled(lever, interactor);
	}

	public void onKeyPickup(EntityKey key, EntityLiving interactor)
	{
		this.customStoryHandler.onKeyPickup(key, interactor);
	}

	public void onKeyUsage(EntityKey key, EntityLiving interactor)
	{
		this.customStoryHandler.onKeyUsage(key, interactor);
	}

	public void onKeyDropped(EntityKey key)
	{
		this.customStoryHandler.onKeyDropped(key);
	}

	public void onCageOpened(TileCage cage, int levelOpenedCages, int levelTotalCages, boolean fromSave)
	{
		this.customStoryHandler.onCageOpened(cage, levelOpenedCages, levelTotalCages, fromSave);

		if (!fromSave && readIntFromWorld(this.level.getWorldID(), Level.WORLD_CLEARED_CAGE_COUNT) == totalCagesPerWorld.get(this.level.getWorldID()))
		{
			// this.shatter(10.0f + 2.0f *
			// (Integer.parseInt(this.level.getWorldID()) - 20)); // last
			// cage
			// in
			// world
		}
	}

	public boolean canPlayerDoAnything()
	{
		return this.customStoryHandler.canPlayerDoAnything();
	}

	public boolean canPlayerTravelDimensions()
	{
		return this.customStoryHandler.canPlayerTravelDimensions();
	}

	public boolean canPlayerExitCurrentWorld()
	{
		return this.customStoryHandler.canPlayerExitCurrentWorld();
	}

	public boolean shouldPlayerBeHandledAsShared()
	{
		return this.customStoryHandler.shouldPlayerBeHandledAsShared();
	}

	public boolean shouldShatterWorldLikeMad()
	{
		return this.customStoryHandler.shouldShatterWorldLikeMad();
	}

	public boolean shouldWorldBeConsideredForShattering()
	{
		return this.customStoryHandler.shouldWorldBeConsideredForShattering();
	}

	public boolean shouldLeveldBeConsideredForShattering()
	{
		return this.customStoryHandler.shouldLeveldBeConsideredForShattering();
	}

	public boolean shouldTotalBeConsideredForShattering()
	{
		return this.customStoryHandler.shouldTotalBeConsideredForShattering();
	}

	public boolean shouldFavorDirectSpawnpoint()
	{
		return this.customStoryHandler.shouldFavorDirectSpawnpoint();
	}

	public boolean shouldRenderPlayer(EntityPlayer player)
	{
		return this.customStoryHandler.shouldRenderPlayer(player);
	}

	private static boolean isFirstLevel(Level level, String mapID)
	{
		return mapID.contains("-") && CheatPanel.isStringNumeric(mapID.substring(mapID.indexOf("-") + 1)) && Integer.parseInt(mapID.substring(mapID.indexOf("-") + 1)) == 1;
	}

	private static boolean isPreviousWorldCompleted(Level level, String mapID)
	{
		if (mapID == null || !mapID.contains("-"))
		{
			return true;
		}

		String worldID = mapID.substring(0, mapID.indexOf('-'));
		String prevWorld = Integer.parseInt(worldID) - 1 + "";

		Object[] data = SaveHandler.getActiveSave().readWorld(prevWorld, Level.WORLD_FINISHED);

		return data != null && data[0] != null && Boolean.parseBoolean((String) data[0]);
	}

	public boolean isShattering()
	{
		return this.shattering;
	}

	public CustomStoryHandler getCustomStoryHandler()
	{
		return this.customStoryHandler;
	}

	public float getShatterFactor()
	{
		return this.shatterFactor;
	}
}
