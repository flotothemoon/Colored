package com.unlogical.colored.terrain.tile;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.gui.HintProperties;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.resources.image.Images;
import com.unlogical.colored.resources.image.Images.HintIdentifier;
import com.unlogical.colored.saving.SaveHandler;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.terrain.tile.properties.ITileInteractable;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.ShaderHandler;

public class TileDoor extends Tile implements ITileInteractable
{
	private static final int CLEARED_OUTLINE_SIZE = 3;
	private static final int MAX_INTERACTION_DISTANCE = 20;

	private static int entryDoorID;

	private static TileDoor nextDoor;
	private static boolean nextFindStart;
	private static boolean nextOpened;
	private static String nextOpenedLevelID;

	private boolean levelStart;
	private boolean levelExit;
	private boolean wasVisited;
	private int doorID;

	protected float outlineSize;

	private String nextLevelID;
	private boolean leadsToStart;
	private int otherDoorID;

	public TileDoor(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);

		this.addEmitter(EmitterType.DOORSPARKLE, dimension).setStencilled(true);
		this.addEmitter(EmitterType.DOORSPIRAL, dimension).setStencilled(true);
	}

	private void generateAndSetDoorID()
	{
		int maxID = 0;

		for (Tile door : this.level.getTiles())
		{
			if (door.getDimension() == Dimension.COLORED && door instanceof TileDoor)
			{
				if (((TileDoor) door).getDoorID() > maxID)
				{
					maxID = ((TileDoor) door).getDoorID();
				}
			}
		}

		this.setDoorID(maxID + 1);
	}

	@Override
	public void init()
	{
		super.init();

		if (this.level.isWorldHub())
		{
			Object[] data = SaveHandler.getActiveSave().readLevel(this.nextLevelID, Level.LEVEL_VISITED_TOKEN);

			this.wasVisited = data != null && Boolean.parseBoolean((String) data[0]);
		}
	}

	@Override
	public void onCreated()
	{
		this.generateAndSetDoorID();
	}

	@Override
	public void onInteract(EntityPlayer interactor)
	{
		if (Level.getMapPathByID(this.nextLevelID) != null)
		{
			this.level.getSoundManager().play(SoundType.OPEN_DOOR, false);

			nextFindStart = this.leadsToStart;
			entryDoorID = this.otherDoorID;

			nextOpened = true;
			nextOpenedLevelID = this.nextLevelID;
			nextDoor = this;
		}
		else
		{
			Debug.warn("Cannot go through door " + this + ".");
		}
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{
		super.onUpdate(delta, rng);

		this.outlineSize = 0;

		if (this.level.isWorldHub() && this.nextLevelID != null)
		{
			Object[] cleared = SaveHandler.getActiveSave().readLevel(this.nextLevelID, Level.LEVEL_CLEARED_TOKEN);

			if (cleared != null && Boolean.parseBoolean((String) cleared[0]))
			{
				this.outlineSize = CLEARED_OUTLINE_SIZE;
			}
		}
	}

	@Override
	protected void preDraw(Color color, Dimension dimension, Batch batch)
	{
		super.preDraw(color, dimension, batch);

		if (this.outlineSize > 0.0f)
		{
			ShaderHandler.useOutlineShader(5.0f, color, batch);

			this.drawOutline(this.outlineSize, batch);

			batch.setShader(null);
		}
	}

	@Override
	public Color customModifyDrawColor(Color color)
	{
		if (!(this instanceof TileWorldDoor) && this.level.isWorldHub() && !this.wasVisited)
		{
			return ColorHelper.multiplyRGBAndCopy(super.customModifyDrawColor(color), Color.DARK_GRAY);
		}

		return super.customModifyDrawColor(color);
	}

	public static void postUpdate()
	{
		if (nextOpened)
		{
			nextOpened = false;

			GameLauncher.getLevelContext().onLevelExit(nextOpenedLevelID, nextDoor);
			LevelManager.initLevel(Level.getMapPathByID(nextOpenedLevelID), true, LevelManager.FADE_OUT_TIME);

			if (GameLauncher.getLevelContext().getControllingPlayer() != null)
			{
				GameLauncher.getLevelContext().getControllingPlayer().resetVelocities();
			}

			nextDoor = null;
		}
	}

	public static TileDoor getStart()
	{
		for (Tile tile : GameLauncher.getLevelContext().getTiles())
		{
			if (tile.getDimension() == Dimension.COLORED && tile instanceof TileDoor)
			{
				if (nextFindStart)
				{
					if (((TileDoor) tile).isLevelStart())
					{
						return (TileDoor) tile;
					}
				}
				else if (entryDoorID == ((TileDoor) tile).getDoorID())
				{
					return (TileDoor) tile;
				}
			}
		}

		return null;
	}

	@Override
	public boolean canInteract(EntityPlayer interactor)
	{
		return !LevelManager.isFadingOut() && (!this.level.isWorldHub() || this.wasVisited || this instanceof TileWorldDoor) && this.levelExit && interactor.isOnGround() && interactor.getHitbox().getMinY() > this.hitbox.getMinY() && this.hitbox.getMinX() - interactor.getHitbox().getMinX() < MAX_INTERACTION_DISTANCE && interactor.getHitbox().getMaxX() - this.hitbox.getMaxX() < MAX_INTERACTION_DISTANCE && (interactor.getHitbox().intersects(this.hitbox) || this.hitbox.contains(interactor.getHitbox()));
	}

	@Override
	public void writeMetadata()
	{
		this.metadata = this.levelStart + ":" + this.levelExit + ":" + this.doorID + ":" + this.nextLevelID + ":" + this.otherDoorID + ":" + this.leadsToStart;
	}

	@Override
	public void readMetadata()
	{
		String[] metadata = this.metadata.split(":");

		try
		{
			this.levelStart = Boolean.parseBoolean(metadata[0]);
			this.levelExit = Boolean.parseBoolean(metadata[1]);
			this.doorID = Integer.parseInt(metadata[2]);
			this.nextLevelID = metadata[3];
			this.otherDoorID = Integer.parseInt(metadata[4]);
			this.leadsToStart = Boolean.parseBoolean(metadata[5]);
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public boolean shouldUpdate()
	{
		return true;
	}

	@Override
	public boolean shouldCopy()
	{
		return false;
	}

	@Override
	public HintIdentifier getHintIdentifier()
	{
		return Images.HintIdentifier.DOOR_HINT;
	}

	@Override
	public HintProperties getHintProperties()
	{
		return HintProperties.DEFAULT_PROPERTIES;
	}

	@Override
	protected boolean shouldUseStencilledEmitters()
	{
		return true;
	}

	@Override
	public boolean shouldReSimulate()
	{
		return false;
	}

	@Override
	public boolean shouldHandleAsShared()
	{
		return true;
	}

	public boolean isLevelStart()
	{
		return this.levelStart;
	}

	public void setLevelStart(boolean levelStart)
	{
		this.levelStart = levelStart;
	}

	public boolean isLevelExit()
	{
		return this.levelExit;
	}

	public void setLevelExit(boolean levelExit)
	{
		this.levelExit = levelExit;
	}

	public int getDoorID()
	{
		return this.doorID;
	}

	public void setDoorID(int doorID)
	{
		this.doorID = doorID;
	}

	public String getNextLevelID()
	{
		return this.nextLevelID;
	}

	public void setNextLevelID(String nextLevelID)
	{
		this.nextLevelID = nextLevelID;
	}

	public boolean isLeadsToStart()
	{
		return this.leadsToStart;
	}

	public void setLeadsToStart(boolean leadsToStart)
	{
		this.leadsToStart = leadsToStart;
	}

	public int getOtherDoorID()
	{
		return this.otherDoorID;
	}

	public void setOtherDoorID(int otherDoorID)
	{
		this.otherDoorID = otherDoorID;
	}

	public static boolean shouldFindStart()
	{
		return nextFindStart;
	}

	public static int getEntryDoorID()
	{
		return entryDoorID;
	}

	public static void setEntryDoorID(int entryDoorID)
	{
		TileDoor.entryDoorID = entryDoorID;
	}

	public static void setFindStart(boolean findStart)
	{
		TileDoor.nextFindStart = findStart;
	}
}
