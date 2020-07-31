package com.unlogical.colored.levelmanaging;

import static com.unlogical.colored.level.Level.DEFAULT_TIME_FACTOR;
import static com.unlogical.colored.level.Level.TIME_SCALE_BACKGROUND_FACTOR;
import static com.unlogical.colored.level.Level.TIME_SCALE_FOREGROUND_FACTOR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.audio.MusicManager;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.filesystem.ConfigFileHandler;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.filesystem.files.EntityFileHandler;
import com.unlogical.colored.filesystem.files.TerrainFileHandler;
import com.unlogical.colored.filesystem.files.WrapperTextureHandler;
import com.unlogical.colored.gui.HintProperties;
import com.unlogical.colored.gui.panel.SanityPanel;
import com.unlogical.colored.input.InputAction;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.profiling.Profiling;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.resources.image.Images;
import com.unlogical.colored.saving.Checkpoint;
import com.unlogical.colored.saving.CheckpointEvent;
import com.unlogical.colored.saving.SaveState;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.TileDoor;
import com.unlogical.colored.terrain.tile.properties.ITileInteractable;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.CursorType;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.GameStatus;
import com.unlogical.colored.util.LevelObject;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;
import com.unlogical.colored.util.Transitions;
import com.unlogical.colored.util.ZHandler;
import com.unlogical.colored.wrapper.WrapperTexture;

public class LevelManager
{
	public static final int DIMENSION_TRANSITION_TIME = 555;
	public static final int DEFAULT_FADE_IN_TIME = 1337 / 3;
	public static final int FADE_OUT_TIME = 1337 / 5;
	public static final int FADE_OUT_BLACKSCREEN_TIME = 250;
	public static final int PLAYER_DEATH_FADE_TIME = 800;
	private static final int CHECKPOINT_TRANSITION_TIME = 1000;

	private static String mapPath;
	private static String mapID;
	private static String fileName;
	private static String nextMapPath;

	private static LinkedList<Checkpoint> checkpoints = new LinkedList<Checkpoint>();
	private static LinkedList<Checkpoint> selectableCheckpoints = new LinkedList<Checkpoint>();
	private static Checkpoint currentCheckpoint;

	private static Level level;
	private static List<Level> deadLevels = new ArrayList<Level>();

	private static Vector2 spawnpoint;
	private static Vector2 cameraOffset = new Vector2(0.0f, 0.0f);

	private static boolean debugMode;
	private static boolean transitioningDimension;
	private static boolean fadingIn;
	private static boolean fadingOut;

	private static boolean paused;
	private static boolean renderMap = true;
	private static boolean frameByFrameDebug;
	private static boolean forceNormalDelta;
	private static boolean checkpointSelectionMode;
	private static boolean nextComesFromDoor;

	private static int fadeInTime;
	private static int totalFadeInTime;
	private static int fadeOutTime;
	private static int dimensionTransitionTime;
	private static int checkpointTransitionTime;
	private static int lastLoadingTime;
	private static int slowTimeFadeInTime;
	private static int totalSlowTimeFadeInTime;

	private static long runningTimestamp;

	public static void init() throws IOException
	{
		Images.registerAndLoad("cantToggle", FilePaths.GUI + "/canttoggle.png");

		GUIManager.init();
	}

	public static void initLevel(String mapPath, boolean comesFromDoor, int fadeOutTime)
	{
		nextMapPath = mapPath;
		nextComesFromDoor = comesFromDoor;
		fadingOut = true;
		LevelManager.fadeOutTime = fadeOutTime;
	}

	public static void initLevel(String mapPath, boolean comesFromDoor)
	{
		initLevel(mapPath, DEFAULT_FADE_IN_TIME, comesFromDoor, true);
	}

	public static void initLevel(String mapPath, int fadeInTime, boolean comesFromDoor, boolean showIcon)
	{
		LevelManager.mapPath = mapPath;

		if (GameLauncher.getLevelContext() != null)
		{
			GameLauncher.getLevelContext().getSoundManager().stopAll();
		}

		Debug.log("Loading map " + mapPath + " ...");

		long beforeTime = System.currentTimeMillis();
		Level previousLevel = level;

		ZHandler.clearDistortions();

		level = new Level();
		level.setPlayMode(true);
		level.createParticleSystem();

		ConfigFileHandler cfg = new ConfigFileHandler(FileManager.getFileQuietly(mapPath + "/config.yml"));

		mapID = cfg.getString("ID");

		TerrainFileHandler terrainFile = new TerrainFileHandler(FileManager.getFileQuietly(mapPath + "/terrain.map"));
		EntityFileHandler entityFile = new EntityFileHandler(FileManager.getFileQuietly(mapPath + "/entities.map"));

		spawnpoint = new Vector2(0.0f, 0.0f);

		level.initID(mapID);

		long beforeReading = System.currentTimeMillis();

		level.setLevelType(LevelType.values()[cfg.getInteger("type")]);

		level.getStoryHandler().onPreLevelInit();

		ArrayList<WrapperTexture> wrappers = new ArrayList<WrapperTexture>();
		ArrayList<Tile> tiles = new ArrayList<Tile>();
		ArrayList<Entity> entities = new ArrayList<Entity>();

		try
		{
			level.createTimeStepHandler();
			level.createSoundManager();
			tiles = terrainFile.read(level);
			level.transferBuffers();

			MapManager.initLevel(null, level);

			WrapperTextureHandler wrapperHandler = new WrapperTextureHandler(FileManager.getFile(mapPath + "/wrappers.cfg"));
			wrappers = wrapperHandler.read(level);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Even more bacon while loading map: " + e, e);
		}

		level.init(mapID, cfg.getString("name"), tiles, wrappers, entities, cameraOffset.set(0.0f, 0.0f), spawnpoint);

		Debug.log("Loading map files took " + (System.currentTimeMillis() - beforeReading) + "ms.");

		level.findInteractableTiles();

		GameLauncher.setLevelContext(level);

		ArrayList<TileDoor> doors = new ArrayList<TileDoor>();

		boolean viaDoor = false;

		for (Tile tile : tiles)
		{
			tile.init();

			if (tile.getType() == TileType.SPAWNPOINT)
			{
				spawnpoint.set(tile.getX(), tile.getY());
			}

			if (tile.getDimension() == Dimension.COLORED && tile instanceof TileDoor)
			{
				doors.add((TileDoor) tile);
			}
		}

		TileDoor entryDoor = null;

		if (!level.getStoryHandler().shouldFavorDirectSpawnpoint())
		{
			Object[] lastEntryDoor = level.readFromGlobalSave(SaveState.LAST_ENTRY_DOOR);
			Object[] lastEntryLevel = level.readFromGlobalSave(SaveState.LAST_VISITED_LEVEL);

			if (!comesFromDoor && lastEntryDoor != null && lastEntryLevel != null && mapID.equals(lastEntryLevel[0]))
			{
				String doorID = (String) lastEntryDoor[0];

				TileDoor.setEntryDoorID(Integer.parseInt(doorID));

				comesFromDoor = true;
			}

			for (TileDoor door : doors)
			{
				if ((!comesFromDoor || TileDoor.shouldFindStart()) && door.isLevelStart() || comesFromDoor && TileDoor.getEntryDoorID() == door.getDoorID())
				{
					spawnpoint.x = door.getCenterX() - Entity.getMainImage(EntityType.PLAYER).getRegionWidth() / 2;
					spawnpoint.y = door.getHitbox().getMaxY() - Entity.getMainImage(EntityType.PLAYER).getRegionHeight();

					viaDoor = true;
					entryDoor = door;

					break;
				}
			}
		}

		if (level.getType() == LevelType.TIME_SCALE)
		{
			level.setForegroundDeltaFactor(DEFAULT_TIME_FACTOR);
			level.setBackgroundDeltaFactor(TIME_SCALE_BACKGROUND_FACTOR);
		}
		else if (level.getType() == LevelType.FROZEN)
		{
			level.setForegroundDeltaFactor(0.0f);
			level.setBackgroundDeltaFactor(DEFAULT_TIME_FACTOR);
		}
		else
		{
			level.setForegroundDeltaFactor(DEFAULT_TIME_FACTOR);
			level.setBackgroundDeltaFactor(DEFAULT_TIME_FACTOR);
		}

		transitioningDimension = false;

		EntityPlayer player = (EntityPlayer) Entity.createEntity(EntityType.PLAYER, spawnpoint.cpy(), level, Dimension.COLORED, true);

		entities.add(player);
		level.setControllingPlayer(player);

		player.setFocused(player.getX() > level.getCenterX());

		if (viaDoor)
		{
			player.updateEntryDoorPosition(entryDoor);
		}

		entityFile.read(level);

		level.transferBuffers();

		for (Entity entity : level.getEntities())
		{
			entity.init();
		}

		level.getStoryHandler().onLevelInit();

		GUIManager.initLevel(level, showIcon);
		MapManager.initLevel(spawnpoint, level);
		MapManager.focusCamera();
		MusicManager.initLevel(level);
		ShaderHandler.initlevel(level);
		ShaderHandler.enable();

		if (level.getLevelType() == LevelType.TIME_TRAVEL)
		{
			preSimulateTimeTravelObjects(Level.TIME_TRAVEL_TIME_DIFFERENCE_MILLIS - 1, 25, entities, tiles);
		}
		else if (level.getLevelType() == LevelType.FROZEN)
		{
			forceNormalDelta = true;

			// TODO hack in a solution for 23-01
			simulateAll(10000, 25, false, level.getID().equals("23-01"));

			forceNormalDelta = false;
		}

		preSimulateCycleObjects(25000, 200);

		level.initRNGs(1337);
		simulateParticlesAhead(150.0f, 300);

		if (previousLevel != null && previousLevel.getForegroundDimension() != level.getForegroundDimension())
		{
			toggleDimension(false);
		}

		level.setControlAlphaFactor(0.0f);
		fadingIn = true;
		LevelManager.fadeInTime = fadeInTime;
		LevelManager.totalFadeInTime = fadeInTime;

		CursorType.useCursor(CursorType.DEFAULT);

		deadLevels.clear();

		checkpoints.clear();
		currentCheckpoint = Checkpoint.storeLevel(level, CheckpointEvent.INITIAL);
		checkpoints.add(currentCheckpoint);

		EntityPlayer.clearRequestCheckpoints();

		checkpointSelectionMode = false;

		level.writeToGlobalSave(SaveState.LAST_VISITED_LEVEL, mapID);

		if (entryDoor != null)
		{
			level.writeToGlobalSave(SaveState.LAST_ENTRY_DOOR, entryDoor.getDoorID());
		}

		level.writeToGlobalSave(Level.LEVEL_DOOR_ENTRY_ID, TileDoor.getEntryDoorID());
		level.writeToGlobalSave(Level.LEVEL_DOOR_SHOULD_FIND_START, TileDoor.shouldFindStart());
		level.writeToGlobalSave(Level.LEVEL_SHOULD_USE_DOOR, comesFromDoor);
		level.writeToLevelSave(Level.LEVEL_VISITED_TOKEN, true);

		level.saveToActiveSave();

		lastLoadingTime = (int) (System.currentTimeMillis() - beforeTime);

		level.getStoryHandler().onLevelEntry(viaDoor);

		slowTimeFadeInTime = 0;
		totalSlowTimeFadeInTime = 0;

		Debug.log("Loaded " + mapID + " (took " + lastLoadingTime + "ms).");
	}

	private static void preSimulateCycleObjects(int totalTime, int preferredDelta)
	{
		if (level.getType() != LevelType.TIME_TRAVEL)
		{
			if (level.getType() == LevelType.FROZEN)
			{
				forceNormalDelta = true;
			}

			simulateAll(totalTime, preferredDelta, false, true);

			if (level.getType() == LevelType.FROZEN)
			{
				forceNormalDelta = false;
			}
		}
	}

	private static void preSimulateTimeTravelObjects(int totalTime, int preferredDelta, List<Entity> entities, List<Tile> tiles)
	{
		for (Entity entity : entities)
		{
			if (entity.isShared())
			{
				entity.storeReSimulationData();
			}
		}

		for (Tile tile : tiles)
		{
			if (tile.isShared())
			{
				tile.storeReSimulationData();
			}
		}

		simulateAll(totalTime, preferredDelta, true, false);

		for (Entity entity : entities)
		{
			if (entity.isShared())
			{
				entity.restoreReSimulationData();
			}
		}

		for (Tile tile : tiles)
		{
			if (tile.isShared())
			{
				tile.restoreReSimulationData();
			}
		}
	}

	public static void simulateAll(int milliseconds, int preferredDelta, boolean initialising, boolean onlyCycleObjects)
	{
		int simulatedTime = 0;
		boolean beforePaused = paused;
		boolean beforeInitialising = false;

		level.setSimulating(true);
		level.setInitialising(initialising);
		level.setUpdateOnlyCycleObjects(onlyCycleObjects);

		while (simulatedTime < milliseconds)
		{
			paused = false;

			update(simulatedTime < milliseconds - preferredDelta ? preferredDelta : milliseconds - simulatedTime);

			simulatedTime += preferredDelta;
		}

		paused = beforePaused;

		level.setSimulating(false);
		level.setInitialising(beforeInitialising);
		level.setUpdateOnlyCycleObjects(false);
	}

	public static void simulateParticlesAhead(float delta, int count)
	{
		Debug.log("Pre-simulating particles for " + delta * count + "ms (" + count + " x " + delta + "ms)...");

		level.setIgnoreTimeEffects(true);
		level.setSimulating(true);

		for (int i = 0; i < count; i++)
		{
			level.updateParticleSystem((int) delta, delta, getBackgroundDelta((int) delta));
		}

		level.setIgnoreTimeEffects(false);
		level.setSimulating(false);

		Debug.log("Particle pre-simulation complete. Particles are now ahead of time by " + delta * count + "ms.");
	}

	public static void update(int delta)
	{
		Profiling.startSection("levelmng");

		if (InputHandler.isKeyJustPressed(InputAction.PAUSE))
		{
			if (paused)
			{
				resume();
				GameLauncher.setStatus(GameStatus.IN_LEVEL);
			}
			else
			{
				pause();
				GameLauncher.setStatus(GameStatus.PAUSE_MENU);
			}
		}

		if (GameLauncher.isDeveloperMode())
		{
			if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Input.Keys.F))
			{
				frameByFrameDebug = !frameByFrameDebug;

				if (!frameByFrameDebug)
				{
					paused = false;
				}
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.F3))
			{
				debugMode = !debugMode;
			}

			if (!GameLauncher.getCheatPanel().isActive())
			{
				if (Gdx.input.isKeyJustPressed(Input.Keys.G))
				{
					level.setShowHitboxes(!level.shouldShowHitboxes());
				}

				if (Gdx.input.isKeyJustPressed(Input.Keys.N))
				{
					Hitbox.debugMode = !Hitbox.debugMode;
				}
			}

			if (!GameLauncher.getCheatPanel().isActive())
			{
				if (Gdx.input.isKeyJustPressed(Input.Keys.X))
				{
					MapManager.toggleCameraMode();
				}

				if (MapManager.isFreeCamera())
				{
					float cameraFactor = delta;

					if (Gdx.input.isKeyPressed(Input.Keys.U))
					{
						MapManager.updatePositions(level, 0.0f, cameraFactor);
					}

					if (Gdx.input.isKeyPressed(Input.Keys.J))
					{
						MapManager.updatePositions(level, 0.0f, -cameraFactor);
					}

					if (Gdx.input.isKeyPressed(Input.Keys.H))
					{
						MapManager.updatePositions(level, cameraFactor, 0.0f);
					}

					if (Gdx.input.isKeyPressed(Input.Keys.K))
					{
						MapManager.updatePositions(level, -cameraFactor, 0.0f);
					}
				}
			}
		}

		if (frameByFrameDebug && (Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyPressed(Input.Keys.R)))
		{
			paused = false;
		}

		if (frameByFrameDebug && !paused)
		{
			Debug.log("--- NEW UPDATE FRAME (" + delta + ") ---");
		}

		if (!level.isSimulating() && GameLauncher.isDeveloperMode() && (Gdx.input.isKeyJustPressed(Input.Keys.F11) || Gdx.input.isKeyJustPressed(Input.Keys.F1) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Input.Keys.M)))
		{
			MapEditor.initLevel(mapID, mapPath);

			MapEditor.adjustCameraOffset(level.getCameraOffset().x, level.getCameraOffset().y);

			GameLauncher.setStatus(GameStatus.MAP_EDITOR);

			return;
		}

		try
		{
			if (!paused)
			{
				if (slowTimeFadeInTime > 0)
				{
					slowTimeFadeInTime -= delta;

					if (slowTimeFadeInTime < 0)
					{
						slowTimeFadeInTime = 0;
					}

					delta *= 1.0f - Transitions.getFactor((float) slowTimeFadeInTime / (float) totalSlowTimeFadeInTime);
				}

				runningTimestamp += delta;

				if (checkpointSelectionMode && !GameLauncher.getCheatPanel().isActive())
				{
					updateCheckpointSelection();
				}

				if (!checkpointSelectionMode && checkpointTransitionTime > 0)
				{
					checkpointTransitionTime -= delta;

					if (checkpointTransitionTime < 0)
					{
						checkpointTransitionTime = 0;
					}
				}

				EntityPlayer.updateKeyRecords();
				EntityPlayer.updateCheckpointInteractors();

				if (EntityPlayer.requestsCheckpoint() && !transitioningDimension)
				{
					EntityPlayer.clearRequestCheckpoints();

					takeCheckpoint(EntityPlayer.getRequestedCheckpointTrigger());
				}

				for (int i = 0; i < deadLevels.size(); i++)
				{
					Level level = deadLevels.get(i);
					float deadDelta = delta; // ((1.0f - level.getDeadFactor())
												// / 2.0f + 0.5f);

					level.deadUpdate(deadDelta);

					updateLevel(level, deadDelta);

					if (level.isGone())
					{
						deadLevels.remove(i);
						i--;
					}
				}

				if (level.isFading())
				{
					level.updateFadeIn(delta);
				}

				if (!checkpointSelectionMode)
				{
					updateFading(delta);

					updateLevel(level, delta);

					if (EntityPlayer.requestsRevival())
					{
						revive();
					}

					if (EntityPlayer.requestsCheckpoint() && !transitioningDimension)
					{
						EntityPlayer.clearRequestCheckpoints();

						takeCheckpoint(EntityPlayer.getRequestedCheckpointTrigger());
					}

					TileDoor.postUpdate();

					if (InputHandler.isKeyJustPressed(InputAction.BACK) && !GameLauncher.getCheatPanel().isActive())
					{
						checkpoints.addLast(Checkpoint.storeLevel(level, CheckpointEvent.INITIAL));

						currentCheckpoint = checkpoints.getLast();

						if (isPreviousCheckpointAvailable()) // not sure about
																// this
						{
							updateSelectableCheckpoints();

							currentCheckpoint = selectableCheckpoints.get(selectableCheckpoints.indexOf(currentCheckpoint) - 1).copy();
						}

						applyCheckpoint(currentCheckpoint);
					}

					if (GameLauncher.isDeveloperMode())
					{
						if (Gdx.input.isKeyJustPressed(Input.Keys.F9))
						{
							currentCheckpoint = checkpoints.removeLast();

							checkpoints.addLast(currentCheckpoint.copy());

							applyCheckpoint(currentCheckpoint);
						}
					}

					if (!level.isInitialising() && !level.isUpdatingOnlyCycleObjects() && !level.isSimulating())
					{
						level.getStoryHandler().update(delta);
					}
				}

				if (!level.isInitialising() && !level.isUpdatingOnlyCycleObjects() && !level.isSimulating())
				{
					level.getSoundManager().update(delta);
					MapManager.update(delta);
					GUIManager.update(delta);

					float posChangeX = (int) -MapManager.getCameraOffsetChangeX();
					float posChangeY = (int) -MapManager.getCameraOffsetChangeY();

					for (Level deadLevel : deadLevels)
					{
						for (Entity entity : deadLevel.getEntities())
						{
							entity.adjustX(posChangeX);
							entity.adjustY(posChangeY);
						}

						for (Tile tile : deadLevel.getTiles())
						{
							if (tile.shouldCopy())
							{
								tile.adjustX(posChangeX);
								tile.adjustY(posChangeY);
							}
						}
					}
				}
			}

			if (Hitbox.debugMode)
			{
				Hitbox.cachedLines.clear();
			}
		}
		catch (Exception e)
		{
			Debug.warn("Error while updating level / physics - jumping back to menu screen.", e);

			if (SanityPanel.isRunningCheck())
			{
				GameLauncher.getSanityPanel().onError(e);
			}
			else
			{
				GameLauncher.setStatus(GameStatus.PLAY_MAP_SELECTION);
			}
		}

		if (frameByFrameDebug)
		{
			paused = true;
		}

		Profiling.endSection("levelmng");
	}

	private static void updateLevel(Level level, float delta)
	{
		updateDimensionTransition(level, delta);

		float foregroundDelta = getForegroundDelta(delta);
		float backgroundDelta = getBackgroundDelta(delta);

		if (level.getForegroundDimension() == Dimension.COLORED)
		{
			level.setColoredDelta(foregroundDelta);
			level.setMonochromeDelta(backgroundDelta);
		}
		else
		{
			level.setColoredDelta(backgroundDelta);
			level.setMonochromeDelta(foregroundDelta);
		}

		level.timeStepHandler.update((int) delta);

		updateInteractables(level);

		MapManager.updateTiles(level, level.getForegroundDimension(), delta, foregroundDelta);

		if (!level.isInitialising())
		{
			MapManager.updateTiles(level, level.getBackgroundDimension(), delta, backgroundDelta);
		}

		EntityManager.updateEntities(level, level.getForegroundDimension(), delta, foregroundDelta);

		if (!level.isInitialising())
		{
			EntityManager.updateEntities(level, level.getBackgroundDimension(), delta, backgroundDelta);
		}

		level.transferBuffers();
		level.updateParticleSystem(delta, level.getColoredDelta(), level.getMonochromeDelta());
		level.updateTransitions(delta);
	}

	private static void updateCheckpointSelection()
	{
		if (InputHandler.isKeyJustPressed(InputAction.JUMP))
		{
			checkpointSelectionMode = false;
			checkpointTransitionTime = CHECKPOINT_TRANSITION_TIME;

			if (checkpoints.indexOf(currentCheckpoint) != checkpoints.size() - 1 || !(currentCheckpoint.getLevel().getControllingPlayer().isOnGround() || currentCheckpoint.getLevel().getControllingPlayer().isClimbing()))
			{
				for (int i = checkpoints.indexOf(currentCheckpoint) + (currentCheckpoint.getLevel().getControllingPlayer().isOnGround() || currentCheckpoint.getLevel().getControllingPlayer().isClimbing() ? 1 : 0); i < checkpoints.size(); i++)
				{
					checkpoints.remove(i);
					i--;
				}
			}
		}

		if (InputHandler.isKeyJustPressed(InputAction.MOVE_DOWN) || InputHandler.isKeyJustPressed(InputAction.BACK))
		{
			updateSelectableCheckpoints();

			if (isPreviousCheckpointAvailable())
			{
				currentCheckpoint = selectableCheckpoints.get(selectableCheckpoints.indexOf(currentCheckpoint) - 1).copy();
				applyCheckpoint(currentCheckpoint);
			}
		}

		if (InputHandler.isKeyJustPressed(InputAction.MOVE_UP))
		{
			updateSelectableCheckpoints();

			if (isNextCheckpointAvailable())
			{
				currentCheckpoint = selectableCheckpoints.get(selectableCheckpoints.indexOf(currentCheckpoint) + 1).copy();
				applyCheckpoint(currentCheckpoint);
			}
		}
	}

	private static void updateFading(int delta)
	{
		float fadeFactor = 0.0f;

		if (fadingIn)
		{
			fadeInTime -= delta;

			if (fadeInTime < 0)
			{
				fadeInTime = 0;
				fadingIn = false;
			}

			fadeFactor = Transitions.getFactor((int) ((float) fadeInTime / (float) totalFadeInTime * 1000));

			level.setControlAlphaFactor(1.0f - Transitions.getFactor((int) ((float) fadeInTime / (float) totalFadeInTime * 1000)));
		}

		if (fadingOut)
		{
			fadeOutTime -= delta;

			if (fadeOutTime < -FADE_OUT_BLACKSCREEN_TIME)
			{
				fadeOutTime = 0;
				fadingOut = false;

				initLevel(nextMapPath, nextComesFromDoor);
			}

			fadeFactor = 1.0f - Transitions.getFactor((int) ((float) Math.max(fadeOutTime, 0) / (float) FADE_OUT_TIME * 1000));

			level.setControlAlphaFactor(Transitions.getFactor((int) ((float) Math.max(fadeOutTime, 0) / (float) FADE_OUT_TIME * 1000)));
		}

		level.setFadeFactor(1.0f - fadeFactor);
	}

	public static void takeCheckpoint(CheckpointEvent trigger)
	{
		currentCheckpoint = Checkpoint.storeLevel(level, trigger);
		checkpoints.add(currentCheckpoint);

		long maxTime = checkpoints.get(checkpoints.size() - 1).getTimestamp();
		int lastIndex = 0;

		Set<Checkpoint> toRemove = new HashSet<Checkpoint>();

		for (int i = 1; i < checkpoints.size() - 4; i++)
		{
			Vector2 distanceToLastCheckpoint = Pools.get(Vector2.class).obtain();
			Checkpoint curCheckpoint = checkpoints.get(i);

			distanceToLastCheckpoint.set(curCheckpoint.getLevel().getControllingPlayer().getPosition());
			distanceToLastCheckpoint.sub(curCheckpoint.getLevel().getCameraOffset());
			distanceToLastCheckpoint.scl(-1.0f);

			distanceToLastCheckpoint.add(checkpoints.get(lastIndex).getLevel().getControllingPlayer().getPosition());
			distanceToLastCheckpoint.sub(checkpoints.get(lastIndex).getLevel().getCameraOffset());

			int multiplier = curCheckpoint.trigger.priority;

			int minTimeDiff = 3000 / multiplier;
			int minDistance = 200 / multiplier;

			if (curCheckpoint.trigger.priority != Checkpoint.MAX_PRIORITY)
			{
				minTimeDiff *= Math.log(maxTime - curCheckpoint.getTimestamp());
			}

			int timeDiff = (int) (curCheckpoint.getTimestamp() - checkpoints.get(lastIndex).getTimestamp());

			if (timeDiff < minTimeDiff || distanceToLastCheckpoint.len() < minDistance)
			{
				if (checkpoints.get(lastIndex).trigger.priority >= curCheckpoint.trigger.priority)
				{
					checkpoints.remove(i);

					i--;
				}
			}

			lastIndex = i;

			Pools.free(distanceToLastCheckpoint);
		}

		checkpoints.removeAll(toRemove);
		toRemove.clear();
	}

	private static void applyCheckpoint(Checkpoint checkpoint)
	{
		level = currentCheckpoint.getLevel();
		GameLauncher.setLevelContext(level);

		for (WrapperTexture wrapper : level.getWrappers())
		{
			wrapper.adjustX((-wrapper.getLevel().getCameraOffset().x + level.getCameraOffset().x) * wrapper.getEmitterParallax());
			wrapper.adjustY((-wrapper.getLevel().getCameraOffset().y + level.getCameraOffset().y) * wrapper.getEmitterParallax());

			wrapper.setLevel(level);
		}

		for (Tile tile : level.getTiles())
		{
			if (!tile.shouldCopy())
			{
				tile.adjustX(-tile.getLevel().getCameraOffset().x + level.getCameraOffset().x);
				tile.adjustY(-tile.getLevel().getCameraOffset().y + level.getCameraOffset().y);

				tile.setLevel(level);
			}
		}

		level.findInteractableTiles();

		MapManager.initLevel(spawnpoint, level);
		GUIManager.initLevel(level, false);

		if (level.getControllingPlayer().isOnGround())
		{
			level.getControllingPlayer().resetVelocities();
		}

		checkpointSelectionMode = true;
		checkpointTransitionTime = CHECKPOINT_TRANSITION_TIME;
	}

	private static void updateSelectableCheckpoints()
	{
		selectableCheckpoints.clear();

		long lastTimestamp = checkpoints.get(0).getTimestamp();
		Vector2 lastPlayerPosition = new Vector2(checkpoints.get(0).getLevel().getControllingPlayer().getPosition()).sub(checkpoints.get(0).getLevel().getCameraOffset());

		selectableCheckpoints.addFirst(checkpoints.get(0));

		for (int i = 1; i < checkpoints.size(); i++)
		{
			Vector2 buffer = LevelObject.bufferedVector;

			buffer.set(checkpoints.get(i).getLevel().getControllingPlayer().getPosition());
			buffer.sub(checkpoints.get(i).getLevel().getCameraOffset());

			buffer.sub(lastPlayerPosition);

			if (checkpoints.get(i).getTimestamp() - lastTimestamp > 2000 || buffer.len() > 250)
			{
				selectableCheckpoints.add(checkpoints.get(i));

				lastTimestamp = checkpoints.get(i).getTimestamp();
				lastPlayerPosition.set(checkpoints.get(i).getLevel().getControllingPlayer().getPosition());
				lastPlayerPosition.sub(checkpoints.get(i).getLevel().getCameraOffset());
			}
		}

		if (selectableCheckpoints.get(selectableCheckpoints.size() - 1) != checkpoints.get(checkpoints.size() - 1))
		{
			selectableCheckpoints.addLast(checkpoints.get(checkpoints.size() - 1));
		}
	}

	public static boolean canPlayerInteract()
	{
		EntityPlayer player = level.getControllingPlayer();

		for (Tile tile : level.getInteractableTiles())
		{
			if (((ITileInteractable) tile).canInteract(player))
			{
				return true;
			}
		}

		return false;
	}

	private static void updateInteractables(Level level)
	{
		EntityPlayer player = level.getControllingPlayer();

		for (Tile tile : level.getInteractableTiles())
		{
			if (((ITileInteractable) tile).canInteract(player))
			{
				ITileInteractable interactable = (ITileInteractable) tile;

				if (EntityPlayer.tryInteract() && !GameLauncher.getCheatPanel().isActive())
				{
					interactable.onInteract(player);
				}

				HintProperties hintProperties = interactable.getHintProperties();

				player.displayOverlay(player.getPosition(), hintProperties, interactable.getHintIdentifier(), hintProperties.shouldForceDisplay());
			}
		}
	}

	private static void updateDimensionTransition(Level level, float delta)
	{
		if (InputHandler.isKeyJustPressed(InputAction.TOGGLE_DIMENSIONS) && level.getStoryHandler().canPlayerTravelDimensions())
		{
			if (canToggleDimensions())
			{
				toggleDimension(true);
			}
			else
			{
				level.getControllingPlayer().displayOverlay(level.getControllingPlayer().getPosition(), HintProperties.WARNING_PROPERTIES, Images.HintIdentifier.CANT_TOGGLE, true);
			}
		}

		if (transitioningDimension)
		{
			float foregroundFactor = 1.0f;
			float backgroundFactor = 0.0f;

			if (dimensionTransitionTime > 0)
			{
				backgroundFactor = Transitions.getFactor((int) ((float) dimensionTransitionTime / (float) DIMENSION_TRANSITION_TIME * 1000.0f));
				foregroundFactor = 1.0f - backgroundFactor;

				dimensionTransitionTime -= delta;
			}
			else
			{
				dimensionTransitionTime = 0;
				transitioningDimension = false;

				level.setTransitioning(false);

				if (level.getType() != LevelType.NORMAL)
				{
					EntityPlayer.onDimensionChanged((EntityPlayer) level.getControllingPlayer().getMirroredEntity(), level.getControllingPlayer(), level.getForegroundDimension());
				}
			}

			Color foregroundColor = level.getDimensionColor(level.getForegroundDimension());
			Color backgroundColor = level.getDimensionColor(level.getBackgroundDimension());

			foregroundColor.a = level.backgroundAlpha + (level.foregroundAlpha - level.backgroundAlpha) * foregroundFactor;
			backgroundColor.a = level.backgroundAlpha + (level.foregroundAlpha - level.backgroundAlpha) * backgroundFactor;

			level.setWrapperAlpha(foregroundFactor, level.getForegroundDimension());
			level.setWrapperAlpha(1.0f, level.getBackgroundDimension());

			level.setForegroundFactor(foregroundFactor);
			level.setBackgroundFactor(backgroundFactor);

			if (level.getType() == LevelType.TIME_SCALE)
			{
				if (level.getForegroundDimension() == Dimension.COLORED)
				{
					level.setBackgroundDeltaFactor(TIME_SCALE_BACKGROUND_FACTOR + (DEFAULT_TIME_FACTOR - TIME_SCALE_BACKGROUND_FACTOR) * backgroundFactor);
					level.setForegroundDeltaFactor(DEFAULT_TIME_FACTOR + (TIME_SCALE_FOREGROUND_FACTOR - DEFAULT_TIME_FACTOR) * backgroundFactor);
				}
				else
				{
					level.setForegroundDeltaFactor(TIME_SCALE_BACKGROUND_FACTOR + (DEFAULT_TIME_FACTOR - TIME_SCALE_BACKGROUND_FACTOR) * foregroundFactor);
					level.setBackgroundDeltaFactor(DEFAULT_TIME_FACTOR + (TIME_SCALE_FOREGROUND_FACTOR - DEFAULT_TIME_FACTOR) * foregroundFactor);
				}
			}
			else if (level.getType() == LevelType.FROZEN)
			{
				level.setForegroundDeltaFactor(backgroundFactor);
				level.setBackgroundDeltaFactor(foregroundFactor);
			}
		}
	}

	public static void toggleDimension(boolean shouldTransition)
	{
		level.setForegroundDimension(level.getForegroundDimension().getOpposite());
		level.setBackgroundDimension(level.getBackgroundDimension().getOpposite());

		if (level.getLevelType() != LevelType.NORMAL)
		{
			level.setControllingPlayer((EntityPlayer) level.getControllingPlayer().getMirroredEntity());

			EntityPlayer.onDimensionChanging((EntityPlayer) level.getControllingPlayer().getMirroredEntity(), level.getControllingPlayer(), level.getForegroundDimension());

			if (level.getType() == LevelType.TIME_TRAVEL)
			{
				level.timeStepHandler.onDimensionToggle(level.getControllingPlayer().getDimension());
			}
		}

		float foregroundFactor = 1.0f;
		float backgroundFactor = 0.0f;

		if (shouldTransition)
		{
			level.playSound(SoundType.SWITCH_DIMENSION, level.getControllingPlayer());

			transitioningDimension = true;
			dimensionTransitionTime = DIMENSION_TRANSITION_TIME - dimensionTransitionTime;
			level.setTransitioning(true);
		}
		else
		{
			dimensionTransitionTime = 0;

			if (level.getType() != LevelType.NORMAL)
			{
				EntityPlayer.onDimensionChanged((EntityPlayer) level.getControllingPlayer().getMirroredEntity(), level.getControllingPlayer(), level.getForegroundDimension());
			}

			Color foregroundColor = level.getDimensionColor(level.getForegroundDimension());
			Color backgroundColor = level.getDimensionColor(level.getBackgroundDimension());

			foregroundColor.a = level.backgroundAlpha + (level.foregroundAlpha - level.backgroundAlpha) * foregroundFactor;
			backgroundColor.a = level.backgroundAlpha + (level.foregroundAlpha - level.backgroundAlpha) * backgroundFactor;

			level.setWrapperAlpha(foregroundFactor, level.getForegroundDimension());
			level.setWrapperAlpha(1.0f, level.getBackgroundDimension());

			level.setForegroundFactor(foregroundFactor);
			level.setBackgroundFactor(backgroundFactor);

			if (level.getType() == LevelType.TIME_SCALE)
			{
				if (level.getForegroundDimension() == Dimension.COLORED)
				{
					level.setForegroundDeltaFactor(DEFAULT_TIME_FACTOR);
					level.setBackgroundDeltaFactor(TIME_SCALE_BACKGROUND_FACTOR);
				}
				else
				{
					level.setForegroundDeltaFactor(DEFAULT_TIME_FACTOR);
					level.setBackgroundDeltaFactor(TIME_SCALE_FOREGROUND_FACTOR);
				}
			}
		}
	}

	private static boolean canToggleDimensions()
	{
		return (!level.getControllingPlayer().hasMirror() || !level.getControllingPlayer().getMirroredEntity().isSuffocating()) && level.getControllingPlayer().canChangeDimensions();
	}

	public static float getBackgroundDelta(float delta)
	{
		if (level.getLevelType() == LevelType.FROZEN || level.getType() == LevelType.TIME_SCALE)
		{
			if (!level.getControllingPlayer().isRawTimeEffected() || forceNormalDelta)
			{
				return delta;
			}
		}

		return delta * level.getBackgroundDeltaFactor();
	}

	public static float getForegroundDelta(float delta)
	{
		if (level.getLevelType() == LevelType.FROZEN || level.getType() == LevelType.TIME_SCALE)
		{
			if (!level.getControllingPlayer().isRawTimeEffected() || forceNormalDelta)
			{
				return delta;
			}
		}

		return delta * level.getForegroundDeltaFactor();
	}

	public static void render(Batch batch)
	{
		Profiling.startSection("levelmng");

		Renderer.useSpriteBatch();

		if (level.getTotalCages() > 0)
		{
			Renderer.setSaturation(0.6f + level.getDimensionColor(Dimension.COLORED).r * 0.6f);
		}
		else
		{
			Renderer.setSaturation(1.0f);
		}

		float beforeLightAlpha = level.getDimensionColor(Dimension.COLORED).a;
		float beforeShadowAlpha = level.getDimensionColor(Dimension.MONOCHROME).a;

		level.getDimensionColor(Dimension.COLORED).a *= level.getControlAlphaFactor();
		level.getDimensionColor(Dimension.MONOCHROME).a *= level.getControlAlphaFactor();

		level.setControlAlpha(level.getControlAlphaFactor());

		if (level.shouldRenderWrappers())
		{
			level.renderBackgroundWrappers(batch);
		}

		for (Level deadLevel : deadLevels)
		{
			float beforeLA = deadLevel.getDimensionColor(Dimension.COLORED).a;
			float beforeSA = deadLevel.getDimensionColor(Dimension.MONOCHROME).a;

			deadLevel.getDimensionColor(Dimension.COLORED).a *= deadLevel.getControlAlpha();
			deadLevel.getDimensionColor(Dimension.MONOCHROME).a *= deadLevel.getControlAlpha();

			renderGeometry(deadLevel, batch);

			deadLevel.getDimensionColor(Dimension.COLORED).a = beforeLA;
			deadLevel.getDimensionColor(Dimension.MONOCHROME).a = beforeSA;
		}

		renderGeometry(level, batch);

		if (level.shouldRenderWrappers())
		{
			level.renderForegroundWrappers(batch);
		}

		Hitbox.renderDebug(batch);

		level.getDimensionColor(Dimension.COLORED).a = beforeLightAlpha;
		level.getDimensionColor(Dimension.MONOCHROME).a = beforeShadowAlpha;

		level.getStoryHandler().render(batch);

		GUIManager.render(batch);

		ShaderHandler.updateAndRender(batch);

		ShapeRenderer sr = Renderer.useShapeRenderer();

		if (level.getControlAlphaFactor0() != 1.0f)
		{
			sr.setColor(ColorHelper.applyAlphaAndCopy(Color.BLACK, 1.0f - level.getControlAlphaFactor0()));
			sr.set(ShapeType.Filled);
			sr.rect(-ShaderHandler.getHorizontalFadeSize() - 5, -ShaderHandler.getVerticalFadeSize() - 5, GameLauncher.getWidth() + ShaderHandler.getHorizontalFadeSize() * 2 + 10, GameLauncher.getHeight() + ShaderHandler.getVerticalFadeSize() * 2 + 10);
		}

		if (debugMode)
		{
			renderCheckpoints(batch);
		}

		Profiling.endSection("levelmng");
	}

	private static void renderGeometry(Level level, Batch batch)
	{
		Profiling.startSection("geometry");

		Renderer.setGrayScaleFactor(level.getDimensionFactor(Dimension.MONOCHROME));

		Dimension foregroundDimension = level.getType() == LevelType.NORMAL ? Dimension.COLORED : level.getForegroundDimension();
		Dimension backgroundDimension = level.getType() == LevelType.NORMAL ? Dimension.MONOCHROME : level.getBackgroundDimension();

		MapManager.renderBehindAllEntities(level, backgroundDimension, level.shouldRenderAll(), batch);
		MapManager.renderBehindAllEntities(level, foregroundDimension, level.shouldRenderAll(), batch);

		EntityManager.renderEntities(level, foregroundDimension, true, batch);

		EntityManager.renderEntities(level, backgroundDimension, false, batch);

		MapManager.renderBackground(level, backgroundDimension, level.shouldRenderAll(), batch);

		EntityManager.renderEntities(level, foregroundDimension, false, batch);

		MapManager.renderForeground(level, foregroundDimension, level.shouldRenderAll(), batch);

		Profiling.endSection("geometry");
	}

	public static void renderCheckpoints(Batch batch)
	{
		float lastRelX = Float.NaN;
		float lastRelY = Float.NaN;

		Gdx.gl.glEnable(GL20.GL_BLEND);

		for (int i = 0; i < checkpoints.size(); i++)
		{
			Checkpoint checkpoint = checkpoints.get(i);
			int size = 40;

			EntityPlayer player = checkpoint.getLevel().getControllingPlayer();

			float relX = player.getPosition().x - checkpoint.getLevel().getCameraOffset().x + level.getCameraOffset().x + player.getHitbox().getWidth() / 2.0f;
			float relY = player.getPosition().y - checkpoint.getLevel().getCameraOffset().y + level.getCameraOffset().y + player.getHitbox().getHeight() / 2.0f;

			Color pointColor = ColorHelper.colorBetween(Color.CYAN, Color.RED, (float) checkpoint.trigger.priority / (float) Checkpoint.MAX_PRIORITY);

			ShapeRenderer sr = Renderer.useShapeRenderer();

			sr.setColor(ColorHelper.applyAlphaAndCopy(pointColor, 0.2f));
			sr.set(ShapeType.Filled);
			sr.circle(relX, relY, size / 2);

			if (!Float.isNaN(lastRelX))
			{
				sr.setColor(ColorHelper.applyAlphaAndCopy(pointColor, 0.02f + 0.1f * ((float) i / (float) checkpoints.size())));
				sr.rectLine(lastRelX, lastRelY, relX, relY, 10.0f);
			}

			Renderer.setFont(Fonts.getSmallBoldFont());

			String str = i + "";

			Renderer.useSpriteBatch();

			Renderer.drawString(str, Color.WHITE, relX - Renderer.getWidth(str) / 2, relY - Renderer.getLineHeight() / 2, 1.0f, batch);

			lastRelX = relX;
			lastRelY = relY;
		}
	}

	public static void revive()
	{
		GameLauncher.clearRecords();
		EntityPlayer.clearRecords();

		Checkpoint currentCopy = Checkpoint.storeLevel(level, CheckpointEvent.INITIAL);

		currentCopy.getLevel().onDeath();

		deadLevels.add(currentCopy.getLevel());

		currentCheckpoint = checkpoints.removeLast();

		Vector2 distanceToCheckpoint = Pools.get(Vector2.class).obtain();

		distanceToCheckpoint.set(0, 0);

		while (level.getControllingPlayer().getTimeOfDeath() - currentCheckpoint.getTimestamp() < 1100 && distanceToCheckpoint.len() < 160)
		{
			distanceToCheckpoint.set(level.getControllingPlayer().getPosition());
			distanceToCheckpoint.sub(level.getCameraOffset());
			distanceToCheckpoint.scl(-1.0f);

			distanceToCheckpoint.add(currentCheckpoint.getLevel().getControllingPlayer().getPosition());
			distanceToCheckpoint.sub(currentCheckpoint.getLevel().getCameraOffset());

			currentCheckpoint = checkpoints.removeLast();
		}

		Pools.free(distanceToCheckpoint);

		checkpoints.addLast(currentCheckpoint.copy());

		currentCheckpoint.getLevel().startFadeIn((int) (PLAYER_DEATH_FADE_TIME * 0.5f));

		applyCheckpoint(currentCheckpoint);
	}

	public static void slowFadeInTime(int fadeInTime)
	{
		slowTimeFadeInTime = fadeInTime;
		totalSlowTimeFadeInTime = fadeInTime;
	}

	public static void reset()
	{
		initLevel(mapPath, false);

		GameLauncher.clearRecords();
		EntityPlayer.clearRecords();

		resume();
	}

	public static void pause()
	{
		paused = true;
	}

	public static void resume()
	{
		slowFadeInTime(800);

		paused = false;
	}

	public static boolean isCheckpointing()
	{
		return checkpointSelectionMode || checkpointTransitionTime > 0;
	}

	public static float getCheckpointingFactor()
	{
		return Transitions.getFactor((float) checkpointTransitionTime / (float) CHECKPOINT_TRANSITION_TIME);
	}

	public static float getTransitionFactor()
	{
		return (float) dimensionTransitionTime / (float) DIMENSION_TRANSITION_TIME;
	}

	public static boolean isPreviousCheckpointAvailable()
	{
		return checkpoints.indexOf(currentCheckpoint) > 0;
	}

	public static boolean isNextCheckpointAvailable()
	{
		return checkpoints.indexOf(currentCheckpoint) + 1 < checkpoints.size();
	}

	public static boolean isPaused()
	{
		return paused;
	}

	public static float getXOffset()
	{
		return getCameraOffset().x;
	}

	public static float getYOffset()
	{
		return getCameraOffset().y;
	}

	public static boolean isDevMode()
	{
		return debugMode;
	}

	public static boolean isRenderMap()
	{
		return renderMap;
	}

	public static void setRenderMap(boolean renderMap)
	{
		LevelManager.renderMap = renderMap;
	}

	public static String getMapName()
	{
		return mapID;
	}

	public static String getFileName()
	{
		return fileName;
	}

	public static Level getLevel()
	{
		return level;
	}

	public static boolean isTransitioning()
	{
		return transitioningDimension;
	}

	public static Vector2 getCameraOffset()
	{
		return cameraOffset;
	}

	public static int getDimensionTransitionTime()
	{
		return dimensionTransitionTime;
	}

	public static Checkpoint getCurrentCheckpoint()
	{
		return currentCheckpoint;
	}

	public static Collection<Checkpoint> getCheckpoints()
	{
		return checkpoints;
	}

	public static boolean isInCheckpointSelectionMode()
	{
		return checkpointSelectionMode;
	}

	public static boolean isFadingOut()
	{
		return fadingOut;
	}

	public static long getRunningTimestamp()
	{
		return runningTimestamp;
	}
}