package com.unlogical.colored.level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.audio.AudioManager;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.audio.SoundWrapper;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.configuration.DisplayConstants;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.filesystem.files.TerrainFileHandler;
import com.unlogical.colored.filesystem.organisation.MapOrganisation;
import com.unlogical.colored.gui.menu.SelectionState;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.particle.EmitterWrapperConfiguration;
import com.unlogical.colored.particle.ParticleEmitter;
import com.unlogical.colored.particle.ParticleEmitterImpl;
import com.unlogical.colored.particle.ParticleSystem;
import com.unlogical.colored.profiling.Profiling;
import com.unlogical.colored.saving.SaveHandler;
import com.unlogical.colored.story.StoryHandler;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.TileCameraControl;
import com.unlogical.colored.terrain.tile.TileDoor;
import com.unlogical.colored.terrain.tile.TileLight;
import com.unlogical.colored.terrain.tile.TileWorldDoor;
import com.unlogical.colored.terrain.tile.cage.TileCage;
import com.unlogical.colored.terrain.tile.properties.ITileInteractable;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.GameObject;
import com.unlogical.colored.util.GameStatus;
import com.unlogical.colored.util.IDeepCopyable;
import com.unlogical.colored.util.ILightSource;
import com.unlogical.colored.util.IShareable;
import com.unlogical.colored.util.IZDistortion;
import com.unlogical.colored.util.LevelObject;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;
import com.unlogical.colored.util.TimeStepHandler;
import com.unlogical.colored.util.Transitions;
import com.unlogical.colored.util.ZHandler;
import com.unlogical.colored.wrapper.WrapperTexture;

public class Level
{
	public static final String LEVEL_CLEARED_TOKEN = "levelCleared";
	public static final String LEVEL_FINISHED_TOKEN = "levelFinished";
	public static final String LEVEL_VISITED_TOKEN = "levelVisited";
	public static final String LEVEL_DOOR_ENTRY_ID = "doorEntryID";
	public static final String LEVEL_DOOR_SHOULD_FIND_START = "doorShouldFindStart";
	public static final String LEVEL_SHOULD_USE_DOOR = "doorShouldUse";
	public static final String LEVEL_CLEARED_CAGE_COUNT = "clearedCageCountPerLevel";

	public static final String WORLD_FINISHED = "worldFinished";
	public static final String WORLD_CLEARED_CAGE_COUNT = "clearedCageCountPerWorld";
	public static final String WORLD_BONUS_CLEARED = "worldBonusCleared";

	public static final int STORED_FRAMES_PER_FRAME = 2;
	public static final int STORED_FRAMES_PER_SECOND = 60 / STORED_FRAMES_PER_FRAME;
	public static final int TIME_TRAVEL_TIME_DIFFERENCE_MILLIS = 7000;
	public static final int TIME_TRAVEL_TIME_DIFFERENCE_FRAMES = TIME_TRAVEL_TIME_DIFFERENCE_MILLIS / STORED_FRAMES_PER_SECOND;
	public static final int DEFAULT_TIME_LOOP_MILLIS = 3000;
	public static final int DEFAULT_TIME_LOOP_FRAMES = DEFAULT_TIME_LOOP_MILLIS / STORED_FRAMES_PER_SECOND;
	public static final int FROZEN_POSITION_TIME_MILLIS = 3000;
	public static final int FROZEN_POSITION_TIME_FRAMES = FROZEN_POSITION_TIME_MILLIS / STORED_FRAMES_PER_SECOND;

	private static final int CAGE_BRIGHTNESS_TRANSITIONING_TIME = 6000;

	public static final int MAX_DEPTH = 16;

	public static final float PORT_ADDITIVE_COLOR_FACTOR = 0.1f;
	public static final float PORT_INVERSE_COLOR_FACTOR = 0.35f;

	public static final float OUT_OF_MAP_TOLERANCE = 400.0f;

	public static final float FROZEN_POSITION_ALPHA_FACTOR = 0.5f;

	public static final float DEFAULT_FOREGROUND_ALPHA = 1.0f;
	public static final float DEFAULT_BACKGROUND_ALPHA = 0.4f;

	public float foregroundAlpha = DEFAULT_FOREGROUND_ALPHA;
	public float backgroundAlpha = DEFAULT_BACKGROUND_ALPHA;

	public static final float TIME_SCALE_FOREGROUND_FACTOR = 2.0f;
	public static final float TIME_SCALE_BACKGROUND_FACTOR = 1.0f / TIME_SCALE_FOREGROUND_FACTOR;
	public static final float DEFAULT_TIME_FACTOR = 1.0f;

	private static final float COLOR_ALPHA_FACTOR = 0.4f;
	private static final float COLOR_ALPHA_FACTOR_BASE = 0.25f;

	private static final float DEFAULT_LOW_BRITHNESS = 0.55f;
	private static final float FINAL_BRIGHTNESS_CHANGE_FACTOR = 0.20f;

	private static SelectionState[] levels;
	private static final HashMap<String, String> levelPathMap = new HashMap<String, String>();
	private static final HashMap<String, String> worldHubMap = new HashMap<String, String>();
	private static final List<String> chapters = new ArrayList<String>();

	private long lastID = 0;

	public TimeStepHandler timeStepHandler;

	private AudioManager soundManager;

	private StoryHandler storyHandler;

	private ParticleSystem particleSystem;

	private LevelType levelType = LevelType.NORMAL;

	private TileCameraControl absLeftLimit;
	private TileCameraControl absRightLimit;
	private TileCameraControl absTopLimit;
	private TileCameraControl absBottomLimit;

	private ArrayList<WrapperTexture> wrappers = new ArrayList<WrapperTexture>();
	private SortedMap<Integer, List<WrapperTexture>> perLayerWrappers = new TreeMap<Integer, List<WrapperTexture>>();

	private ArrayList<Entity> entities = new ArrayList<Entity>(64);
	private ArrayList<Entity> entitiesToRemove = new ArrayList<Entity>(16);
	private ArrayList<Entity> entitiesToAdd = new ArrayList<Entity>(16);

	private ArrayList<Tile> tiles = new ArrayList<Tile>(128);
	private ArrayList<Tile> tilesToRemove = new ArrayList<Tile>(16);
	private ArrayList<Tile> tilesToAdd = new ArrayList<Tile>(16);
	private ArrayList<Tile> interactableTiles = new ArrayList<Tile>();

	private Set<TileCage> brightnessChangingCages = new HashSet<TileCage>();

	private EntityPlayer controllingPlayer;

	private Dimension foregroundDimension = Dimension.COLORED;
	private Dimension backgroundDimension = Dimension.MONOCHROME;

	private Vector2 cameraOffset = new Vector2();
	private Vector2 safePoint = new Vector2();

	private Color drawColor = new Color(1.0f, 1.0f, 1.0f, this.foregroundAlpha);
	private Color monochromeDrawColor = new Color(1.0f, 1.0f, 1.0f, this.backgroundAlpha);

	private Random coloredRNG;
	private Random monochromeRNG;
	private Random coloredParticleRNG;
	private Random monochromeParticleRNG;
	private Random sharedRNG;

	private String id;
	private String name;
	private String worldID;
	private String chapter;
	private String internalID;

	private float foregroundDeltaFactor = DEFAULT_TIME_FACTOR;
	private float backgroundDeltaFactor = DEFAULT_TIME_FACTOR;
	private float controlAlphaFactor = 1.0f;
	private float controlAlpha = 1.0f;
	private float fadingAlpha = 1.0f;

	private float blinkAlphaFactor = this.drawColor.a;
	private float sharedAlphaFactor = 0.00128f;

	private float wrapperAlpha = this.drawColor.a;
	private float monochromeWrapperAlpha = this.monochromeDrawColor.a;

	private int clearedCages;
	private int totalCages;

	private boolean dead;
	private boolean fadingIn;
	private boolean brightnessChanging;
	private boolean ignoreTimeEffects;
	private boolean reSimulating;
	private boolean renderTiles = true;
	private boolean renderWrappers = true;
	private boolean renderForegroundWrappers = true;
	private boolean renderBackgroundWrappers = true;
	private boolean renderParticleEmitters = true;
	private boolean renderEntities = true;
	private boolean showHitboxes;
	private boolean showParticleAreas;
	private boolean hasSafePoint;
	private boolean transitioning;
	private boolean initialising;
	private boolean simulating;
	private boolean dummyLevel;
	private boolean playMode;
	private boolean updateOnlyCycleObjects;
	private boolean inversedCages;

	private float coloredDimensionXOffset;
	private float coloredDimensionYOffset;
	private float monochromeDimensionXOffset;
	private float monochromeDimensionYOffset;

	private float brightnessFactor;
	private float brightnessBefore;

	private float coloredDelta;
	private float monochromeDelta;

	private float foregroundFactor = 1.0f;
	private float backgroundFactor = 0.0f;

	private float fadeFactor;

	private int totalBrightnessTransitionTime;
	private int brightnessTransitionTime;

	private int deadTime;
	private int totalDeadTime;
	private int fadeInTime;
	private int totalFadeInTime;

	public Level()
	{
		this.initRNGs(1337);
	}

	public static void deepCopy(Level level, Level copy)
	{
		IdentityHashMap<Object, Object> copiedReferences = new IdentityHashMap<Object, Object>(256);

		copiedReferences.put(level, copy);

		copy.lastID = level.lastID;
		copy.levelType = level.levelType;

		copy.foregroundDimension = level.foregroundDimension;
		copy.backgroundDimension = level.backgroundDimension;

		copy.storyHandler = (StoryHandler) Level.getCopy(level.storyHandler, copiedReferences);
		copy.timeStepHandler = (TimeStepHandler) Level.getCopy(level.timeStepHandler, copiedReferences);
		copy.particleSystem = (ParticleSystem) Level.getCopy(level.particleSystem, copiedReferences);
		copy.soundManager = level.soundManager;

		copy.cameraOffset = level.cameraOffset.cpy();
		copy.safePoint = level.safePoint.cpy();
		copy.drawColor = new Color(level.drawColor);
		copy.monochromeDrawColor = new Color(level.monochromeDrawColor);

		copy.id = level.id;
		copy.name = level.name;
		copy.worldID = level.worldID;
		copy.chapter = level.chapter;
		copy.internalID = level.internalID;

		copy.controlAlpha = level.controlAlpha;
		copy.blinkAlphaFactor = level.blinkAlphaFactor;
		copy.wrapperAlpha = level.wrapperAlpha;
		copy.monochromeWrapperAlpha = level.monochromeWrapperAlpha;
		copy.clearedCages = level.clearedCages;
		copy.totalCages = level.totalCages;

		copy.dead = level.dead;
		copy.brightnessChanging = level.brightnessChanging;
		copy.ignoreTimeEffects = level.ignoreTimeEffects;
		copy.reSimulating = level.reSimulating;
		copy.renderTiles = level.renderTiles;
		copy.renderWrappers = level.renderWrappers;
		copy.renderForegroundWrappers = level.renderForegroundWrappers;
		copy.renderBackgroundWrappers = level.renderBackgroundWrappers;
		copy.renderParticleEmitters = level.renderParticleEmitters;
		copy.renderEntities = level.renderEntities;
		copy.showHitboxes = level.showHitboxes;
		copy.showParticleAreas = level.showParticleAreas;
		copy.hasSafePoint = level.hasSafePoint;
		copy.transitioning = level.transitioning;
		copy.simulating = level.simulating;
		copy.dummyLevel = level.dummyLevel;
		copy.playMode = level.playMode;
		copy.updateOnlyCycleObjects = level.updateOnlyCycleObjects;

		copy.coloredDimensionXOffset = level.coloredDimensionXOffset;
		copy.coloredDimensionYOffset = level.coloredDimensionYOffset;
		copy.monochromeDimensionXOffset = level.monochromeDimensionXOffset;
		copy.monochromeDimensionYOffset = level.monochromeDimensionYOffset;

		copy.brightnessFactor = level.brightnessFactor;
		copy.brightnessBefore = level.brightnessBefore;

		copy.coloredDelta = level.coloredDelta;
		copy.monochromeDelta = level.monochromeDelta;

		copy.foregroundFactor = level.foregroundFactor;
		copy.backgroundFactor = level.backgroundFactor;

		copy.totalBrightnessTransitionTime = level.totalBrightnessTransitionTime;
		copy.brightnessTransitionTime = level.brightnessTransitionTime;

		copy.deadTime = level.deadTime;
		copy.totalDeadTime = level.totalDeadTime;

		copy.absLeftLimit = (TileCameraControl) copiedReferences.get(level.absLeftLimit);
		copy.absRightLimit = (TileCameraControl) copiedReferences.get(level.absRightLimit);
		copy.absTopLimit = (TileCameraControl) copiedReferences.get(level.absTopLimit);
		copy.absBottomLimit = (TileCameraControl) copiedReferences.get(level.absBottomLimit);

		copy.brightnessChangingCages = new HashSet<TileCage>();
		copy.brightnessChangingCages = deepCopyCollection(level.brightnessChangingCages, copy.brightnessChangingCages, copiedReferences);
		copy.entities = new ArrayList<Entity>();
		copy.entities = deepCopyCollection(level.entities, copy.entities, copiedReferences);
		copy.entitiesToAdd = new ArrayList<Entity>();
		copy.entitiesToAdd = deepCopyCollection(level.entitiesToAdd, copy.entitiesToAdd, copiedReferences);
		copy.entitiesToRemove = new ArrayList<Entity>();
		copy.entitiesToRemove = deepCopyCollection(level.entitiesToRemove, copy.entitiesToRemove, copiedReferences);

		copy.tiles = new ArrayList<Tile>();
		copy.tiles = deepCopyCollection(level.tiles, copy.tiles, copiedReferences);
		copy.tilesToAdd = new ArrayList<Tile>();
		copy.tilesToAdd = deepCopyCollection(level.tilesToAdd, copy.tilesToAdd, copiedReferences);
		copy.tilesToRemove = new ArrayList<Tile>();
		copy.tilesToRemove = deepCopyCollection(level.tilesToRemove, copy.tilesToRemove, copiedReferences);
		copy.interactableTiles = new ArrayList<Tile>();
		copy.interactableTiles = deepCopyCollection(level.interactableTiles, copy.interactableTiles, copiedReferences);
		copy.wrappers = level.wrappers;
		copy.perLayerWrappers = level.perLayerWrappers;

		copy.controllingPlayer = (EntityPlayer) getCopy(level.controllingPlayer, copiedReferences);

		copy.controlAlpha = level.controlAlpha;
		copy.foregroundDeltaFactor = level.foregroundDeltaFactor;
		copy.backgroundDeltaFactor = level.backgroundDeltaFactor;
	}

	public static Object putCopy(Object original, Object copy, Map<Object, Object> copiedReferences)
	{
		copiedReferences.put(original, copy);

		return copy;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Collection<E>, E> T deepCopyCollection(T collection, T newCollection, Map<Object, Object> copiedReferences)
	{
		for (E element : collection)
		{
			newCollection.add((E) getCopy(element, copiedReferences));
		}

		return newCollection;
	}

	public static Object getCopy(Object element, Map<Object, Object> copiedReferences)
	{
		if (element == null)
		{
			return null;
		}
		else if (copiedReferences.containsKey(element))
		{
			return copiedReferences.get(element);
		}
		else
		{
			try
			{
				if (((IDeepCopyable<?>) element).shouldCopy())
				{
					return ((IDeepCopyable<?>) element).deepCopy(copiedReferences);
				}
				else
				{
					return element;
				}
			}
			catch (Throwable e)
			{
				Debug.warn("Error while deep copying element: " + e, e);

				throw new RuntimeException("Error while deep copying element: " + element, e);
			}
		}
	}

	public void initRNGs(int initialSeed)
	{
		this.coloredRNG = new Random(initialSeed);
		this.monochromeRNG = new Random(initialSeed);
		this.coloredParticleRNG = new Random(initialSeed);
		this.monochromeParticleRNG = new Random(initialSeed);
		this.sharedRNG = new Random(initialSeed);

		if (this.levelType == LevelType.FROZEN)
		{
			this.monochromeParticleRNG.nextInt(); // reseed rng
		}
	}

	public static void fetchChapters()
	{
		MapOrganisation organistion = new MapOrganisation(FileManager.globaliseFile(FilePaths.MAPS));

		levels = organistion.getMaps(true);

		for (SelectionState level : levels)
		{
			if (level.getID().contains("-"))
			{
				String worldID = level.getID().substring(0, level.getID().lastIndexOf('-'));

				if (!chapters.contains(worldID))
				{
					chapters.add(worldID);
				}
			}
		}

		Collections.sort(chapters);
	}

	public static void fetchMaps()
	{
		levelPathMap.clear();

		for (SelectionState level : levels)
		{
			levelPathMap.put(level.getID(), level.getPath());

			if (level.getID().contains("-"))
			{
				String worldID = level.getID().substring(0, level.getID().lastIndexOf('-'));

				if (worldHubMap.get(worldID) == null)
				{
					try
					{
						TerrainFileHandler handler = new TerrainFileHandler(level.getPath() + "/terrain.map");

						Level newLevel = new Level();
						newLevel.setDummyLevel(true);
						newLevel.createParticleSystem();
						newLevel.id = level.getID();

						List<Tile> tiles = handler.read(newLevel);

						for (Tile tile : tiles)
						{
							if (tile.getDimension() == Dimension.COLORED && tile instanceof TileWorldDoor)
							{
								worldHubMap.put(worldID, level.getID());

								Debug.log("Found world hub for world " + worldID + " at " + level.getID() + ".");

								break;
							}
						}
					}
					catch (Exception e)
					{
						Debug.log("Well, whatever.. but for some reason (" + e + ") the level " + level.getID() + " at " + level.getPath() + " couldn't be fetched.", e);
					}
				}
			}
		}
	}

	public static void createDefaultLevel(String filePath, String mapID, String mapName)
	{
		try
		{
			TerrainFileHandler terrainFile = new TerrainFileHandler(FileManager.getFile(filePath + "/terrain.map"));

			Level level = new Level();

			List<Tile> tiles = terrainFile.read(level);

			level.levelType = LevelType.NORMAL;

			tiles.add(Tile.createTile(TileType.CAMERABLOCK, "", 0, 0.0f, new Vector2(0.0f, 0.0f), level, Dimension.COLORED, TileType.getProperty(TileType.CAMERABLOCK), false));
			tiles.add(Tile.createTile(TileType.CAMERABLOCK, "", 0, 0.0f, new Vector2(GameLauncher.getWidth() - DisplayConstants.TILE_SIZE, GameLauncher.getHeight() - DisplayConstants.TILE_SIZE), level, Dimension.COLORED, TileType.getProperty(TileType.CAMERABLOCK), false));

			tiles.add(((TileLight) Tile.createTile(TileType.LIGHT, "", 0, 0.0f, new Vector2(GameLauncher.getWidth() / 2.0f - DisplayConstants.TILE_SIZE / 2.0f, GameLauncher.getHeight() / 2.0f - DisplayConstants.TILE_SIZE / 2.0f), level, Dimension.COLORED, TileType.getProperty(TileType.LIGHT), false)).initDefault());

			terrainFile.write(tiles, level);
		}
		catch (IOException e)
		{
			Debug.warn("Failed to initialise level: " + e, e);
		}
	}

	public void init(String id, String name, ArrayList<Tile> tiles, ArrayList<WrapperTexture> wrappers, ArrayList<Entity> entities, Vector2 offset, Vector2 spawnpoint)
	{
		this.name = name;

		this.initID(id);

		this.entities = entities;
		this.tiles = tiles;
		this.cameraOffset = offset;
		this.wrappers = wrappers;

		this.sortWrappers();

		for (Tile tile : tiles)
		{
			if (tile.getDimension() == Dimension.COLORED && tile instanceof TileCage)
			{
				this.totalCages++;
			}
		}

		if (this.totalCages > 0)
		{
			if (this.shouldInverseCages())
			{
				this.initBrightness(1.0f);
			}
			else
			{
				this.initBrightness(DEFAULT_LOW_BRITHNESS);
			}
		}

		if (this.levelType == LevelType.TIME_TRAVEL)
		{
			EmitterType.TIME_TRAVEL_CHANGED.initialLife.set(this.getTimeLoopLimit() - 500, this.getTimeLoopLimit() + 500);
			EmitterType.TIME_TRAVEL_CHANGED.fadeOutTime.set(10000, 10000);
		}
	}

	public void createSoundManager()
	{
		if (this.soundManager != null)
		{
			Debug.warn("SoundManager already created for this level.");

			return;
		}

		this.soundManager = new AudioManager()
		{
			@Override
			protected boolean isActive()
			{
				return !Level.this.dummyLevel;
			}

			@Override
			protected float getVolumeFactorFor(Dimension soundDimension, boolean soundShared, AudioManager.SoundType type)
			{
				if (soundShared)
				{
					return 1.0f;
				}

				return Level.this.getDimensionColor(soundDimension).a;
			}

			@Override
			protected float getPitchFactorFor(Dimension soundDimension, boolean soundShared, AudioManager.SoundType type)
			{
				if (soundShared || Level.this.getType() != LevelType.TIME_SCALE)
				{
					return 1.0f;
				}

				return soundDimension == Level.this.foregroundDimension ? Level.this.foregroundDeltaFactor : Level.this.backgroundDeltaFactor;
			}
		};
	}

	public SoundWrapper playSound(SoundType type, LevelObject object, float volume, float pitch, float pan, boolean loop)
	{
		return this.soundManager.play(type, loop, volume, pitch, pan, object.getDimension(), object.isShared() || object.shouldHandleAsShared() || this.levelType == LevelType.NORMAL, this.getRNG(object));
	}

	public SoundWrapper playSound(SoundType type, LevelObject object)
	{
		return this.playSound(type, 1.0f, 1.0f, false, object, object.getPosition());
	}

	public SoundWrapper playSound(SoundType type, float volume, LevelObject object, Vector2 sourcePosition)
	{
		return this.playSound(type, 1.0f, volume, true, object, sourcePosition);
	}

	public SoundWrapper playSound(SoundType type, float pitch, LevelObject object)
	{
		return this.playSound(type, pitch, 1.0f, false, object, object.getPosition());
	}

	public SoundWrapper playSound(SoundType type, float pitch, float volume, LevelObject object, Vector2 sourcePosition)
	{
		return this.playSound(type, pitch, volume, false, object, sourcePosition);
	}

	public SoundWrapper playSound(SoundType type, float pitch, float volume, boolean ignoreRelativeVolume, LevelObject object, Vector2 sourcePosition)
	{
		Vector2 buffer = Pools.get(Vector2.class).obtain();

		buffer.set(this.controllingPlayer.getCenterX(), this.controllingPlayer.getCenterY()).sub(sourcePosition);

		float relVolume = 1.0f - MathUtils.clamp((Math.abs(buffer.x) - 1000.0f) / 500.0f, 0.0f, 1.0f);
		float pan = MathUtils.clamp(-buffer.x / 2500.0f, -1.0f, 1.0f);

		relVolume = (float) Math.pow(volume, 0.5f);

		if (!ignoreRelativeVolume)
		{
			volume *= relVolume;
		}

		Pools.free(Vector2.class);

		return this.playSound(type, object, volume, pitch, pan, false);
	}

	public void createTimeStepHandler()
	{
		if (this.timeStepHandler != null)
		{
			Debug.warn("TimeStepHandler already created for this level.");

			return;
		}

		this.timeStepHandler = new TimeStepHandler(this);
	}

	public void startFadeIn(int fadeInTime)
	{
		this.fadingIn = true;
		this.fadeInTime = fadeInTime;
		this.totalFadeInTime = fadeInTime;
		this.fadingAlpha = 0.0f;
	}

	public void updateFadeIn(float delta)
	{
		this.fadeInTime -= delta;

		if (this.fadeInTime < 0)
		{
			this.fadeInTime = 0;
			this.fadingIn = false;
		}

		this.fadingAlpha = 1.0f - Transitions.getFactor((float) this.fadeInTime / (float) this.totalFadeInTime);
	}

	public void onDeath()
	{
		this.onDeath(LevelManager.PLAYER_DEATH_FADE_TIME);
	}

	public void onDeath(int deathTime)
	{
		this.dead = true;
		this.deadTime = deathTime;
		this.totalDeadTime = this.deadTime;
	}

	public void deadUpdate(float delta)
	{
		this.deadTime -= delta;
		this.controlAlpha = this.getDeadFactor();
	}

	public float getDeadFactor()
	{
		return Transitions.getFactor((float) this.deadTime / (float) this.totalDeadTime);
	}

	public boolean isGone()
	{
		return this.dead && this.deadTime <= 0;
	}

	public boolean isDead()
	{
		return this.dead;
	}

	public boolean hasWorldHub()
	{
		return this.id.contains("-") && worldHubMap.containsKey(this.id.substring(0, this.id.lastIndexOf('-')));
	}

	public boolean isWorldHub()
	{
		return this.id != null && this.id.contains("-") && this.id.equals(worldHubMap.get(this.id.substring(0, this.id.lastIndexOf('-'))));
	}

	public String getWorldHubLevelID()
	{
		return worldHubMap.get(this.id.substring(0, this.id.lastIndexOf('-')));
	}

	public static boolean mapFilesExist(String levelID)
	{
		return levelPathMap.containsKey(levelID);
	}

	public static String getMapPathByID(String levelID)
	{
		if (!levelPathMap.containsKey(levelID))
		{
			throw new RuntimeException("Level path for " + levelID + " not found! Fatal, aborting...");
		}

		return levelPathMap.get(levelID);
	}

	public int getTimeLoopLimit()
	{
		if (this.levelType == LevelType.TIME_TRAVEL)
		{
			return TIME_TRAVEL_TIME_DIFFERENCE_FRAMES;
		}
		else if (this.levelType == LevelType.FROZEN)
		{
			return FROZEN_POSITION_TIME_FRAMES;
		}

		return DEFAULT_TIME_LOOP_FRAMES;
	}

	public boolean isOutOfMap(Hitbox hitbox)
	{
		if (this.absLeftLimit == null || this.absRightLimit == null || this.absTopLimit == null || this.absBottomLimit == null)
		{
			return false;
		}

		return hitbox.getMaxX() < this.absLeftLimit.getCenterX() || hitbox.getMinX() > this.absRightLimit.getCenterX() || hitbox.getMinY() > this.absBottomLimit.getCenterY() || hitbox.getMaxY() < this.absTopLimit.getCenterY();
	}

	public boolean isOutOfMap(float x, float y)
	{
		if (this.absLeftLimit == null || this.absRightLimit == null || this.absTopLimit == null || this.absBottomLimit == null)
		{
			return false;
		}

		return x < this.absLeftLimit.getCenterX() - OUT_OF_MAP_TOLERANCE || x > this.absRightLimit.getCenterX() + OUT_OF_MAP_TOLERANCE || y > this.absBottomLimit.getCenterY() + OUT_OF_MAP_TOLERANCE || y < this.absTopLimit.getCenterY() - OUT_OF_MAP_TOLERANCE;
	}

	public void initBrightness(float brightness)
	{
		this.drawColor.r = this.drawColor.g = this.drawColor.b = brightness;
		this.monochromeDrawColor.r = this.monochromeDrawColor.g = this.monochromeDrawColor.b = brightness;
	}

	public float onCageCleared(TileCage cage, boolean clearedFromSave)
	{
		this.clearedCages++;

		float range = 1.0f - DEFAULT_LOW_BRITHNESS - FINAL_BRIGHTNESS_CHANGE_FACTOR;

		float factor = 1.0f / this.totalCages * range;

		if (this.clearedCages == this.totalCages)
		{
			factor += FINAL_BRIGHTNESS_CHANGE_FACTOR;
		}

		if (this.shouldInverseCages())
		{
			factor *= -1;
		}

		if (clearedFromSave)
		{
			this.drawColor.r = this.drawColor.g = this.drawColor.b = this.drawColor.r + factor;
			this.monochromeDrawColor.g = this.monochromeDrawColor.b = this.monochromeDrawColor.r = this.drawColor.r;
		}
		else if (!this.dummyLevel)
		{
			this.soundManager.play(SoundType.CLEAR_CAGE, false);

			this.transitionBrightness(cage, CAGE_BRIGHTNESS_TRANSITIONING_TIME, factor);
		}

		if (this.clearedCages == this.totalCages)
		{
			// TODO maybe special sound / happiness trigger?

			this.onLevelCleared();
		}

		this.storyHandler.onCageOpened(cage, this.clearedCages, this.totalCages, clearedFromSave);

		return factor / (1.0f - DEFAULT_LOW_BRITHNESS);
	}

	public void onLevelExit(String nextLevelID, TileDoor door)
	{
		if (nextLevelID.contains("-"))
		{
			try
			{
				int nextWorldID = Integer.parseInt(nextLevelID.substring(0, nextLevelID.indexOf('-')));
				int nextLevelNum = Integer.parseInt(nextLevelID.substring(nextLevelID.indexOf('-') + 1));
				int currentWorldID = Integer.parseInt(this.worldID);
				int currentLevelNum = Integer.parseInt(this.id.substring(this.id.indexOf('-') + 1));

				if (nextWorldID > currentWorldID || nextLevelNum > currentLevelNum && nextWorldID == currentWorldID)
				{
					this.writeToLevelSave(LEVEL_FINISHED_TOKEN, true);

					if (this.totalCages == 0)
					{
						this.onLevelCleared();
					}
				}

				this.storyHandler.onLevelExit(door);
			}
			catch (Exception e)
			{
				Debug.log("Weird, entering level with weird level id: " + nextLevelID, e);
			}
		}
	}

	private void onLevelCleared()
	{
		this.writeToLevelSave(LEVEL_CLEARED_TOKEN, true);

		if (this.isBonusLevel())
		{
			this.writeToWorldSave(WORLD_BONUS_CLEARED, true);
		}

		this.saveToActiveSave();
		// MenuDisplay.displaySavedMessage();

		Debug.log("Level " + this.id + " cleared.");
	}

	public void updateTransitions(float delta)
	{
		if (this.sharedAlphaFactor > 0.0f && this.blinkAlphaFactor > 1.0f || this.sharedAlphaFactor < 0.0f && this.blinkAlphaFactor < -1.0f)
		{
			this.sharedAlphaFactor *= -1.0f;
		}

		this.blinkAlphaFactor += this.sharedAlphaFactor * delta;

		if (this.brightnessChanging)
		{
			this.drawColor.r = this.brightnessBefore + this.brightnessFactor * Transitions.getFactor((int) ((float) this.brightnessTransitionTime / (float) this.totalBrightnessTransitionTime * 1000.0f));

			this.drawColor.g = this.drawColor.b = this.drawColor.r;
			this.monochromeDrawColor.g = this.monochromeDrawColor.b = this.monochromeDrawColor.r = this.drawColor.r;

			this.brightnessTransitionTime += delta;

			if (this.brightnessTransitionTime >= this.totalBrightnessTransitionTime)
			{
				this.brightnessChanging = false;

				for (TileCage cage : this.brightnessChangingCages)
				{
					cage.onBrightnessChangeFinished();
				}
			}
		}
	}

	private void transitionBrightness(TileCage cage, int totalTime, float factor)
	{
		this.brightnessTransitionTime = 0;
		this.totalBrightnessTransitionTime = totalTime;
		this.brightnessFactor = factor;
		this.brightnessBefore = this.drawColor.r;
		this.brightnessChangingCages.add(cage);

		this.brightnessChanging = true;
	}

	public float getBrightnessTransitionFactor()
	{
		return (float) this.brightnessTransitionTime / this.totalBrightnessTransitionTime;
	}

	public void initID(String mapID)
	{
		this.id = mapID;
		this.internalID = this.id != null && this.id.contains("-") ? this.id.substring(this.id.indexOf("-") + 1) : "unknown";
		this.worldID = this.id != null && this.id.contains("-") ? this.id.substring(0, this.id.indexOf('-')) : "unknown";
		this.chapter = this.worldID;
		this.storyHandler = new StoryHandler(this);
	}

	public void createParticleSystem()
	{
		this.particleSystem = new ParticleSystem();
	}

	public void updateWrapperPositions(float mainLayerChangeX, float mainLayerChangeY)
	{
		for (WrapperTexture wrapper : this.wrappers)
		{
			wrapper.adjustX(mainLayerChangeX * wrapper.getEmitterParallax());
			wrapper.adjustY(mainLayerChangeY * wrapper.getEmitterParallax());
		}
	}

	public void drawAsShared(IShareable<?> object, Batch batch)
	{
		float factor = Transitions.getFactor(Math.abs(this.blinkAlphaFactor)) * COLOR_ALPHA_FACTOR + COLOR_ALPHA_FACTOR_BASE;

		Color sharedColor = ColorHelper.thirdBuffer;

		ColorHelper.set(ColorHelper.thirdBuffer, factor, (factor + 0.33f) % 1.0f, (factor + 0.66f) % 1.0f, 1.0f);

		ColorHelper.set(ColorHelper.secondBuffer, 1.0f - (1.0f - sharedColor.r) * factor, 1.0f - (1.0f - sharedColor.g) * factor, 1.0f - (1.0f - sharedColor.b) * factor, 1.0f);

		ColorHelper.secondBuffer = object.customModifyDrawColor(ColorHelper.secondBuffer);

		Renderer.setGrayScaleFactor(1.0f - factor);

		Renderer.startUsingAdditiveTint(sharedColor.mul(0.25f));

		object.drawImageOffset(object.getImage(), Dimension.COLORED, ColorHelper.multiplyAndCopy(ColorHelper.applyAlphaAndCopy(this.drawColor, this.controlAlpha), ColorHelper.secondBuffer), 0.0f, 0.0f, batch);

		Renderer.endUsingAdditiveTint();
	}

	public Color getFinalSharedColor()
	{
		float factor = Math.abs(this.blinkAlphaFactor) * COLOR_ALPHA_FACTOR + COLOR_ALPHA_FACTOR_BASE;

		Color sharedColor = EmitterType.SHARED.startTint;

		ColorHelper.set(ColorHelper.secondBuffer, 1.0f - (1.0f - sharedColor.r) * factor, 1.0f - (1.0f - sharedColor.g) * factor, 1.0f - (1.0f - sharedColor.b) * factor, 1.0f);

		return ColorHelper.secondBuffer;
	}

	public ParticleEmitterImpl addParticleEmitter(Dimension dimension, EmitterWrapperConfiguration configuration, EmitterType type)
	{
		ParticleEmitterImpl emitter = new ParticleEmitterImpl(dimension, configuration, type);

		configuration.setEmitter(emitter);

		this.addParticleEmitter(emitter);

		emitter.ensureManualRendering();

		return emitter;
	}

	public void addParticleEmitter(ParticleEmitter emitter)
	{
		if (this.particleSystem != null)
		{
			this.particleSystem.addEmitter(emitter);
		}
	}

	public void removeParticleEmitter(ParticleEmitter emitter)
	{
		if (this.particleSystem != null)
		{
			this.particleSystem.removeEmitter(emitter);
		}
	}

	public void updateParticleSystem(float delta, float coloredDelta, float shadowDelta)
	{
		Profiling.startSection("particlesys");

		this.particleSystem.update(delta, coloredDelta, shadowDelta);

		Profiling.endSection("particlesys");
	}

	public WrapperTexture addWrapper(WrapperTexture wrapper)
	{
		this.wrappers.add(wrapper);

		this.sortWrappers();

		return wrapper;
	}

	public WrapperTexture removeWrapper(WrapperTexture wrapper)
	{
		this.removeParticleEmitter(wrapper.getEmitter());

		if (wrapper.isParticleWrapper())
		{
			this.removeParticleEmitter(wrapper.getEmitter().getMirroredEmitter());
		}

		this.wrappers.remove(wrapper);

		this.sortWrappers();

		return wrapper;
	}

	public void sortWrappers()
	{
		Collections.sort(this.wrappers);

		this.perLayerWrappers.clear();

		this.perLayerWrappers.put(-1, new ArrayList<WrapperTexture>());
		this.perLayerWrappers.put(1, new ArrayList<WrapperTexture>());

		for (WrapperTexture wrapper : this.wrappers)
		{
			this.perLayerWrappers.get(wrapper.isInForeground() ? 1 : -1).add(wrapper);
		}

		for (Integer depth : this.perLayerWrappers.keySet())
		{
			Collections.sort(this.perLayerWrappers.get(depth));
		}
	}

	public void renderBackgroundWrappers(Batch batch)
	{
		Profiling.startSection("bgwrapper");

		if (this.renderBackgroundWrappers || this.renderWrappers || this.renderParticleEmitters)
		{
			for (Integer depth : this.perLayerWrappers.keySet())
			{
				// for (WrapperTexture wrapper :
				// this.perLayerWrappers.get(depth))
				// {
				// this.renderBackgroundWrapper(wrapper, Dimension.MONOCHROME,
				// batch);
				// }

				for (WrapperTexture wrapper : this.perLayerWrappers.get(depth))
				{
					this.renderBackgroundWrapper(wrapper, Dimension.COLORED, batch);
				}
			}
		}

		Profiling.endSection("bgwrapper");
	}

	public void renderBackgroundWrapper(WrapperTexture wrapper, Dimension dimension, Batch batch)
	{
		if (wrapper.isInBackground() && (!wrapper.isParticleWrapper() && (this.renderWrappers || this.renderBackgroundWrappers) || this.renderParticleEmitters && wrapper.isParticleWrapper()))
		{
			wrapper.draw(batch, dimension);

			if (this.showHitboxes)
			{
				ShapeRenderer sr = Renderer.useShapeRenderer();

				sr.setColor(ColorHelper.applyAlphaAndCopy(Color.WHITE, 0.15f));
				sr.polygon(wrapper.getHitbox().getPoints());
			}
		}
	}

	public void renderForegroundWrappers(Batch batch)
	{
		Profiling.startSection("fgwrapper");

		if (this.renderForegroundWrappers || this.renderWrappers || this.renderParticleEmitters)
		{
			for (Integer depth : this.perLayerWrappers.keySet())
			{
				// for (WrapperTexture wrapper :
				// this.perLayerWrappers.get(depth))
				// {
				// this.renderForegroundWrapper(wrapper, Dimension.MONOCHROME,
				// batch);
				// }

				for (WrapperTexture wrapper : this.perLayerWrappers.get(depth))
				{
					this.renderForegroundWrapper(wrapper, Dimension.COLORED, batch);
				}
			}
		}

		Profiling.endSection("fgwrapper");
	}

	private void renderForegroundWrapper(WrapperTexture wrapper, Dimension dimension, Batch batch)
	{
		if (wrapper.isInForeground() && (!wrapper.isParticleWrapper() && (this.renderWrappers || this.renderForegroundWrappers) || this.renderParticleEmitters && wrapper.isParticleWrapper()))
		{
			wrapper.draw(batch, dimension);

			if (this.showHitboxes)
			{
				ShapeRenderer sr = Renderer.useShapeRenderer();

				sr.setColor(ColorHelper.applyAlphaAndCopy(Color.WHITE, 0.3f));
				sr.polygon(wrapper.getHitbox().getPoints());
			}
		}
	}

	public long generateID()
	{
		return this.lastID++;
	}

	public Random getRNG(GameObject object)
	{
		if (object.isShared() || object.shouldHandleAsShared())
		{
			return this.sharedRNG;
		}
		else if (object.getDimension() == Dimension.COLORED)
		{
			return this.coloredRNG;
		}
		else
		{
			return this.monochromeRNG;
		}
	}

	public Random getPRNG(boolean shared, Dimension dimension)
	{
		if (shared)
		{
			return this.sharedRNG;
		}
		else if (dimension == Dimension.COLORED)
		{
			return this.coloredParticleRNG;
		}
		else
		{
			return this.monochromeParticleRNG;
		}
	}

	public float getDelta(Dimension dimension)
	{
		return dimension == Dimension.COLORED ? this.coloredDelta : this.monochromeDelta;
	}

	public void setSafePoint(float x, float y)
	{
		this.safePoint.x = x;
		this.safePoint.y = y;

		this.hasSafePoint = true;
	}

	public Color getDimensionColor(Dimension dimension)
	{
		return dimension == Dimension.COLORED ? this.drawColor : this.monochromeDrawColor;
	}

	public float getWrapperAlpha(Dimension dimension)
	{
		return (dimension == Dimension.COLORED ? this.wrapperAlpha : this.monochromeWrapperAlpha) * this.controlAlpha;
	}

	public void setWrapperAlpha(float alpha, Dimension dimension)
	{
		if (dimension == Dimension.COLORED)
		{
			this.wrapperAlpha = alpha;
		}
		else
		{
			this.monochromeWrapperAlpha = alpha;
		}
	}

	public void writeToGlobalSave(Object key, Object... values)
	{
		SaveHandler.getActiveSave().writeGlobal(key, values);
	}

	public void writeToLevelSave(Object key, Object... values)
	{
		SaveHandler.getActiveSave().writeLevel(this.id, key, values);
	}

	public void writeToWorldSave(Object key, Object... values)
	{
		SaveHandler.getActiveSave().writeWorld(this.worldID, key, values);
	}

	public Object[] readFromGlobalSave(Object key)
	{
		return SaveHandler.getActiveSave().readGlobal(key);
	}

	public Object[] readFromLevelSave(Object key)
	{
		return SaveHandler.getActiveSave().readLevel(this.id, key);
	}

	public Object[] readFromWorldSave(Object key)
	{
		return SaveHandler.getActiveSave().readWorld(this.worldID, key);
	}

	public void removeFromGlobalSave(Object key)
	{
		SaveHandler.getActiveSave().removeGlobal(key);
	}

	public void removeFromLevelSave(Object key)
	{
		SaveHandler.getActiveSave().removeLevel(this.id, key);
	}

	public void removeFromWorldSave(Object key)
	{
		SaveHandler.getActiveSave().removeWorld(this.worldID, key);
	}

	public void saveToActiveSave()
	{
		try
		{
			SaveHandler.saveCurrent();
		}
		catch (IOException e)
		{
			Debug.warn("Could not save to current save state - FATAL!", e);
		}
	}

	public void findInteractableTiles()
	{
		this.interactableTiles.clear();

		for (Tile tile : this.tiles)
		{
			if (tile instanceof ITileInteractable)
			{
				this.interactableTiles.add(tile);
			}
		}
	}

	private void addObject(Object object)
	{
		if (!this.dummyLevel)
		{
			if (object instanceof ILightSource)
			{
				ShaderHandler.addLight((ILightSource) object);
			}

			if (object instanceof IZDistortion)
			{
				ZHandler.addDistortion(((IZDistortion) object).getZDistortion());
			}
		}
	}

	private void removeObject(Object object)
	{
		if (!this.dummyLevel)
		{
			if (object instanceof ILightSource)
			{
				ShaderHandler.removeLight((ILightSource) object);
			}

			if (object instanceof IZDistortion)
			{
				ZHandler.removeDistortion(((IZDistortion) object).getZDistortion());
			}
		}
	}

	public void addTile(Tile tile)
	{
		if (tile != null)
		{
			this.addObject(tile);

			this.tilesToAdd.add(tile);
		}
	}

	public void removeTile(Tile tile)
	{
		if (tile != null)
		{
			if (!tile.canBeRemoved())
			{
				Debug.warn("Tried to remove tile " + tile + " but that tile can currently not be removed.");

				return;
			}

			this.removeObject(tile);

			this.tilesToRemove.add(tile);
			tile.removeAllEmitters();
			tile.remove();
		}
	}

	public void addEntity(Entity entity)
	{
		if (entity != null)
		{
			this.addObject(entity);

			this.entitiesToAdd.add(entity);
		}
	}

	public void removeEntity(Entity entity)
	{
		if (entity != null)
		{
			if (!entity.canBeRemoved())
			{
				Debug.warn("Tried to remove entity " + entity + " but that entity can currently not be removed.");

				return;
			}

			this.removeObject(entity);

			this.entitiesToRemove.add(entity);
			entity.removeAllEmitters();
			entity.remove();
		}
	}

	public void transferBuffers()
	{
		this.addAddedEntities();
		this.addAddedTiles();

		this.removeRemovedEntities();
		this.removeRemovedTiles();
	}

	private void addAddedEntities()
	{
		if (!this.entitiesToAdd.isEmpty())
		{
			this.entities.addAll(this.entitiesToAdd);
			this.entitiesToAdd.clear();
		}
	}

	private void addAddedTiles()
	{
		if (!this.tilesToAdd.isEmpty())
		{
			this.tiles.addAll(this.tilesToAdd);
			this.tilesToAdd.clear();
		}
	}

	private void removeRemovedTiles()
	{
		if (!this.tilesToRemove.isEmpty())
		{
			this.tiles.removeAll(this.tilesToRemove);
			this.tilesToRemove.clear();
		}
	}

	private void removeRemovedEntities()
	{
		if (!this.entitiesToRemove.isEmpty())
		{
			this.entities.removeAll(this.entitiesToRemove);
			this.entitiesToRemove.clear();
		}
	}

	public int getMapWidth()
	{
		if (this.absRightLimit == null || this.absLeftLimit == null)
		{
			return 10000;
		}

		return (int) (this.absRightLimit.getCenterX() - this.absLeftLimit.getCenterX());
	}

	public int getMapHeight()
	{
		if (this.absBottomLimit == null || this.absTopLimit == null)
		{
			return 10000;
		}

		return (int) (this.absBottomLimit.getCenterY() - this.absTopLimit.getCenterY());
	}

	public float getDimensionFactor(Dimension dimension)
	{
		if (this.foregroundDimension == dimension)
		{
			return this.foregroundFactor;
		}
		else
		{
			return this.backgroundFactor;
		}
	}

	public float getCenterX()
	{
		return (this.absRightLimit.getCenterX() + this.absLeftLimit.getCenterX()) / 2.0f;
	}

	public float getCenterY()
	{
		return (this.absBottomLimit.getCenterY() + this.absTopLimit.getCenterY()) / 2.0f;
	}

	public EntityPlayer getPlayer()
	{
		return this.controllingPlayer;
	}

	public void setPlayer(EntityPlayer player)
	{
		this.controllingPlayer = player;
	}

	public String getID()
	{
		return this.id;
	}

	public String getWorldID()
	{
		return this.worldID;
	}

	public ArrayList<Entity> getEntities()
	{
		return this.entities;
	}

	public ArrayList<Tile> getTiles()
	{
		return this.tiles;
	}

	public ArrayList<Tile> getInteractableTiles()
	{
		return this.interactableTiles;
	}

	public Vector2 getCameraOffset()
	{
		return this.cameraOffset;
	}

	public void setCameraOffset(Vector2 cameraOffset)
	{
		this.cameraOffset = cameraOffset;
	}

	public boolean shouldRenderAll()
	{
		return this.renderTiles;
	}

	public void setRenderTiles(boolean renderTiles)
	{
		this.renderTiles = renderTiles;
	}

	public boolean shouldRenderWrappers()
	{
		return this.renderWrappers;
	}

	public void setRenderWrappers(boolean renderWrappers)
	{
		this.renderWrappers = renderWrappers;
	}

	public boolean shouldRenderEntities()
	{
		return this.renderEntities;
	}

	public void renderEntities()
	{
		this.renderEntities = true;
	}

	public void setRenderEntities(boolean renderEntities)
	{
		this.renderEntities = renderEntities;
	}

	public boolean shouldShowHitboxes()
	{
		return this.showHitboxes;
	}

	public void setShowHitboxes(boolean showHitboxes)
	{
		this.showHitboxes = showHitboxes;
	}

	public ArrayList<WrapperTexture> getWrappers()
	{
		return this.wrappers;
	}

	public boolean shouldShowParticleAreas()
	{
		return this.showParticleAreas;
	}

	public void setShouldShowParticleAreas(boolean showParticleAreas)
	{
		this.showParticleAreas = showParticleAreas;
	}

	public LevelType getType()
	{
		return this.levelType;
	}

	public EntityPlayer getControllingPlayer()
	{
		return this.controllingPlayer;
	}

	public void setControllingPlayer(EntityPlayer controllingPlayer)
	{
		this.controllingPlayer = controllingPlayer;
	}

	public Dimension getForegroundDimension()
	{
		return this.foregroundDimension;
	}

	public void setForegroundDimension(Dimension foregroundDimension)
	{
		this.foregroundDimension = foregroundDimension;
	}

	public Dimension getBackgroundDimension()
	{
		return this.backgroundDimension;
	}

	public void setBackgroundDimension(Dimension backgroundDimension)
	{
		this.backgroundDimension = backgroundDimension;
	}

	public ArrayList<Entity> getEntitiesToRemove()
	{
		return this.entitiesToRemove;
	}

	public LevelType getLevelType()
	{
		return this.levelType;
	}

	public void setLevelType(LevelType levelType)
	{
		this.levelType = levelType;
	}

	public boolean shouldCreateMirrors()
	{
		return this.levelType != LevelType.NORMAL;
	}

	public static HashMap<String, String> getLevelMap()
	{
		return levelPathMap;
	}

	public static void setLevels(SelectionState[] levels)
	{
		Level.levels = levels;
	}

	public static SelectionState[] getLevels()
	{
		return levels;
	}

	public static Set<String> getAllLevelIDs()
	{
		return levelPathMap.keySet();
	}

	public boolean hasSafePoint()
	{
		return this.hasSafePoint;
	}

	public Vector2 getSafePoint()
	{
		return this.safePoint;
	}

	public float getColoredDelta()
	{
		return this.coloredDelta;
	}

	public void setColoredDelta(float coloredDelta)
	{
		this.coloredDelta = coloredDelta;
	}

	public float getMonochromeDelta()
	{
		return this.monochromeDelta;
	}

	public void setMonochromeDelta(float monochromeDelta)
	{
		this.monochromeDelta = monochromeDelta;
	}

	public boolean shouldRenderForegroundWrappers()
	{
		return this.renderForegroundWrappers;
	}

	public void setRenderForegroundWrappers(boolean renderForegroundWrappers)
	{
		this.renderForegroundWrappers = renderForegroundWrappers;
	}

	public boolean shouldRenderBackgroundWrappers()
	{
		return this.renderBackgroundWrappers;
	}

	public void setRenderBackgroundWrappers(boolean renderBackgroundWrappers)
	{
		this.renderBackgroundWrappers = renderBackgroundWrappers;
	}

	public boolean shouldRenderParticleEmitters()
	{
		return this.renderParticleEmitters;
	}

	public void setRenderParticleEmitters(boolean renderParticleEmitters)
	{
		this.renderParticleEmitters = renderParticleEmitters;
	}

	public float getControlAlpha()
	{
		return this.controlAlpha;
	}

	public void setControlAlpha(float controlAlpha)
	{
		this.controlAlpha = controlAlpha;
	}

	public TileCameraControl getAbsLeftLimit()
	{
		return this.absLeftLimit;
	}

	public void setAbsLeftLimit(TileCameraControl absLeftLimit)
	{
		this.absLeftLimit = absLeftLimit;
	}

	public TileCameraControl getAbsRightLimit()
	{
		return this.absRightLimit;
	}

	public void setAbsRightLimit(TileCameraControl absRightLimit)
	{
		this.absRightLimit = absRightLimit;
	}

	public TileCameraControl getAbsTopLimit()
	{
		return this.absTopLimit;
	}

	public void setAbsTopLimit(TileCameraControl absTopLimit)
	{
		this.absTopLimit = absTopLimit;
	}

	public TileCameraControl getAbsBottomLimit()
	{
		return this.absBottomLimit;
	}

	public void setAbsBottomLimit(TileCameraControl absBottomLimit)
	{
		this.absBottomLimit = absBottomLimit;
	}

	public boolean ignoresTimeEffects()
	{
		return this.ignoreTimeEffects;
	}

	public void setIgnoreTimeEffects(boolean ignoreTimeEffects)
	{
		this.ignoreTimeEffects = ignoreTimeEffects;
	}

	public boolean isTransitioning()
	{
		return this.transitioning;
	}

	public void setTransitioning(boolean transitioning)
	{
		this.transitioning = transitioning;
	}

	public float getForegroundFactor()
	{
		return this.foregroundFactor;
	}

	public void setForegroundFactor(float foregroundFactor)
	{
		this.foregroundFactor = foregroundFactor;
	}

	public float getBackgroundFactor()
	{
		return this.backgroundFactor;
	}

	public void setBackgroundFactor(float backgroundFactor)
	{
		this.backgroundFactor = backgroundFactor;
	}

	public boolean isInitialising()
	{
		return this.initialising;
	}

	public void setInitialising(boolean initialising)
	{
		this.initialising = initialising;
	}

	public ArrayList<Entity> getEntitiesToAdd()
	{
		return this.entitiesToAdd;
	}

	public ArrayList<Tile> getTilesToRemove()
	{
		return this.tilesToRemove;
	}

	public ArrayList<Tile> getTilesToAdd()
	{
		return this.tilesToAdd;
	}

	public ParticleSystem getParticleSystem()
	{
		return this.particleSystem;
	}

	public boolean isReSimulating()
	{
		return this.reSimulating;
	}

	public void setReSimulating(boolean reSimulating)
	{
		this.reSimulating = reSimulating;
	}

	public void setDummyLevel(boolean dummyLevel)
	{
		this.dummyLevel = dummyLevel;
	}

	public boolean isDummyLevel()
	{
		return this.dummyLevel;
	}

	public void setID(String id)
	{
		this.id = id;
	}

	public boolean isPlayMode()
	{
		return this.playMode;
	}

	public void setPlayMode(boolean playMode)
	{
		this.playMode = playMode;
	}

	public boolean isUpdatingOnlyCycleObjects()
	{
		return this.updateOnlyCycleObjects;
	}

	public void setUpdateOnlyCycleObjects(boolean updateOnlyCycleObjects)
	{
		this.updateOnlyCycleObjects = updateOnlyCycleObjects;
	}

	public String getName()
	{
		return this.name;
	}

	public float getLightDimensionXOffset()
	{
		return this.coloredDimensionXOffset;
	}

	public void setColoredDimensionXOffset(float coloredDimensionXOffset)
	{
		this.coloredDimensionXOffset = coloredDimensionXOffset;
	}

	public float getColoredDimensionYOffset()
	{
		return this.coloredDimensionYOffset;
	}

	public void setColoredDimensionYOffset(float coloredDimensionYOffset)
	{
		this.coloredDimensionYOffset = coloredDimensionYOffset;
	}

	public float getColoredDimensionXOffset()
	{
		return this.monochromeDimensionXOffset;
	}

	public void setMonochromeDimensionXOffset(float monochromeDimensionXOffset)
	{
		this.monochromeDimensionXOffset = monochromeDimensionXOffset;
	}

	public float getShadowDimensionYOffset()
	{
		return this.monochromeDimensionYOffset;
	}

	public void setMonochromeDimensionYOffset(float monochromeDimensionYOffset)
	{
		this.monochromeDimensionYOffset = monochromeDimensionYOffset;
	}

	public float getXOffset(Dimension dimension)
	{
		return dimension == Dimension.COLORED ? this.coloredDimensionXOffset : this.monochromeDimensionXOffset;
	}

	public float getYOffset(Dimension dimension)
	{
		return dimension == Dimension.COLORED ? this.coloredDimensionYOffset : this.monochromeDimensionYOffset;
	}

	public void setXOffset(Dimension dimension, float offset)
	{
		if (dimension == Dimension.COLORED)
		{
			this.coloredDimensionXOffset = offset;
		}
		else
		{
			this.monochromeDimensionXOffset = offset;
		}
	}

	public void setYOffset(Dimension dimension, float offset)
	{
		if (dimension == Dimension.COLORED)
		{
			this.coloredDimensionYOffset = offset;
		}
		else
		{
			this.monochromeDimensionYOffset = offset;
		}
	}

	public boolean areDimensionOffset()
	{
		return this.storyHandler.isShattering() || this.coloredDimensionXOffset != 0.0f || this.coloredDimensionYOffset != 0.0f || this.monochromeDimensionXOffset != 0.0f || this.monochromeDimensionYOffset != 0.0f;
	}

	public boolean isSimulating()
	{
		return this.simulating;
	}

	public void setSimulating(boolean simulating)
	{
		this.simulating = simulating;
	}

	public void setBackgroundDeltaFactor(float backgroundDeltaFactor)
	{
		this.backgroundDeltaFactor = backgroundDeltaFactor;
	}

	public void setForegroundDeltaFactor(float foregroundDeltaFactor)
	{
		this.foregroundDeltaFactor = foregroundDeltaFactor;
	}

	public void setControlAlphaFactor(float controlAlphaFactor)
	{
		this.controlAlphaFactor = controlAlphaFactor;
	}

	public float getColoredDeltaFactor()
	{
		if (this.foregroundDimension == Dimension.COLORED)
		{
			return this.foregroundDeltaFactor;
		}
		else
		{
			return this.backgroundDeltaFactor;
		}
	}

	public float getMonochromeDeltaFactor()
	{
		if (this.foregroundDimension == Dimension.MONOCHROME)
		{
			return this.foregroundDeltaFactor;
		}
		else
		{
			return this.backgroundDeltaFactor;
		}
	}

	public float getBackgroundDeltaFactor()
	{
		return this.backgroundDeltaFactor;
	}

	public float getForegroundDeltaFactor()
	{
		return this.foregroundDeltaFactor;
	}

	public float getControlAlphaFactor()
	{
		return 1.0f;
	}

	public float getControlAlphaFactor0()
	{
		return this.controlAlphaFactor;
	}

	public float getShatterFactor()
	{
		return this.storyHandler.getShatterFactor();
	}

	public boolean isBrightening()
	{
		return this.brightnessChanging;
	}

	public StoryHandler getStoryHandler()
	{
		return this.storyHandler;
	}

	public String getChapter()
	{
		return this.chapter;
	}

	public void setChapter(String chapter)
	{
		this.chapter = chapter;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getInternalID()
	{
		return this.internalID;
	}

	public void setInternalID(String internalID)
	{
		this.internalID = internalID;
	}

	public boolean isBonusLevel()
	{
		return this.internalID.equals("b");
	}

	public static List<String> getChapters()
	{
		return chapters;
	}

	public boolean isFading()
	{
		return this.fadingIn;
	}

	public float getFadingAlpha()
	{
		return this.fadingAlpha;
	}

	public boolean shouldInverseCages()
	{
		return this.inversedCages;
	}

	public boolean shouldRemoveEmittersOnCompletion()
	{
		return GameLauncher.getStatus() != GameStatus.MAP_EDITOR;
	}

	public boolean shouldUpdateAllEmitters()
	{
		return this.simulating;
	}

	public AudioManager getSoundManager()
	{
		return this.soundManager;
	}

	public int getClearedCages()
	{
		return this.clearedCages;
	}

	public int getTotalCages()
	{
		return this.totalCages;
	}

	public float getFadeFactor()
	{
		return this.fadeFactor;
	}

	public void setFadeFactor(float fadeFactor)
	{
		this.fadeFactor = fadeFactor;
	}
}
