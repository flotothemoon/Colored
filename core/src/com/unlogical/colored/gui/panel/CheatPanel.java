package com.unlogical.colored.gui.panel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.filesystem.files.TerrainFileHandler;
import com.unlogical.colored.gui.InputField;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.saving.InformationPool;
import com.unlogical.colored.saving.SaveHandler;
import com.unlogical.colored.saving.SaveState;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.cage.TileCage;
import com.unlogical.colored.util.GameStatus;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;
import com.unlogical.colored.wrapper.WrapperTexture;

public class CheatPanel extends GUIPanel
{
	private InputField inputField;
	private String lastCommand;
	private List<String> log = new ArrayList<String>();

	public CheatPanel(float xOffset, float yOffset, float width, float height)
	{
		super("Command Panel", xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);
		this.inputField = new InputField(xOffset + 5, yOffset + height - 55, width - 10, 50);
		this.inputField.init("");
		this.inputField.setActive(true);
	}

	@Override
	protected void customUpdate(int delta)
	{
		this.inputField.update(delta);

		if (this.lastCommand != null && Gdx.input.isKeyJustPressed(Input.Keys.UP))
		{
			this.inputField.init(this.lastCommand);
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER))
		{
			this.processCommand(this.inputField.getText());

			this.inputField.init("");
		}
	}

	private void processCommand(String command)
	{
		Debug.log("Processing command \"" + command + "\" ...");

		try
		{
			SaveState activeSave = SaveHandler.getActiveSave();
			String currentLevel = GameLauncher.getLevelContext().getID();
			String currentWorld = currentLevel.contains("-") ? currentLevel.substring(0, GameLauncher.getLevelContext().getID().indexOf("-")) : "";

			if (command.startsWith("reset "))
			{
				String subCommand = command.substring(command.indexOf(" ") + 1);

				if (subCommand.equals("all"))
				{
					resetAll();

					this.log.add("Reset entire game state to start.");
				}
				else if (subCommand.startsWith("level "))
				{
					subCommand = subCommand.substring(subCommand.indexOf(" ") + 1);

					if (subCommand.equals("current") || subCommand.equals(currentLevel))
					{
						resetLevel(currentWorld, currentLevel);

						reloadCurrentLevel();

						this.log.add("Reset current level (" + currentLevel + ").");
					}
					else
					{
						resetLevel(currentWorld, subCommand);

						this.log.add("Reset level: " + subCommand + ".");
					}
				}
				else if (subCommand.startsWith("world "))
				{
					subCommand = subCommand.substring(subCommand.indexOf(" ") + 1);

					if (subCommand.equals("current") || subCommand.equals(currentWorld))
					{
						resetWorld(currentWorld);

						GameLauncher.setStatus(GameStatus.IN_LEVEL);
						LevelManager.initLevel(Level.getMapPathByID(currentWorld + "-01"), false);

						this.log.add("Reset current world (" + currentWorld + ").");
					}
					else
					{
						resetWorld(subCommand);

						this.log.add("Reset world: " + subCommand + ".");
					}
				}
				else
				{
					this.reportUnknownCommand(command);
				}
			}
			else if (command.startsWith("complete "))
			{
				String subCommand = command.substring(command.indexOf(" ") + 1);

				if (subCommand.equals("all"))
				{
					resetAll();

					activeSave.perLevelInformation.clear();
					activeSave.perWorldInformation.clear();

					completeWorld("20", "21", "22", "23", "24", "25");

					reloadCurrentLevel();
				}
				else if (subCommand.startsWith("level "))
				{
					subCommand = subCommand.substring(subCommand.indexOf(" ") + 1);

					if (subCommand.equals("current") || subCommand.equals(currentLevel))
					{
						completeLevel(currentLevel);

						reloadCurrentLevel();

						this.log.add("Completed current level (" + currentLevel + ").");
					}
					else
					{
						completeLevel(subCommand);

						this.log.add("Completed level: " + subCommand + ".");
					}
				}
				else if (subCommand.startsWith("world "))
				{
					subCommand = subCommand.substring(subCommand.indexOf(" ") + 1);

					if (subCommand.equals("current") || subCommand.equals(currentWorld))
					{
						completeWorld(currentWorld);

						reloadCurrentLevel();

						this.log.add("Completed current world (" + currentWorld + ").");
					}
					else
					{
						completeWorld(subCommand);

						this.log.add("Completed world: " + subCommand);
					}
				}
				else
				{
					this.reportUnknownCommand(command);
				}
			}
			else if (command.startsWith("goto"))
			{
				String subCommand = command.substring(command.indexOf(" ") + 1);

				GameLauncher.setStatus(GameStatus.IN_LEVEL);
				LevelManager.initLevel(Level.getMapPathByID(subCommand.contains("-") || !isStringNumeric(subCommand) ? subCommand : subCommand + "-01"), false);
			}
			else if (command.equals("save"))
			{
				SaveHandler.saveCurrent();
				this.log.add("Current active game state saved to disk.");
			}
			else
			{
				this.reportUnknownCommand(command);
			}

			this.lastCommand = command;
		}
		catch (Exception e)
		{
			this.log.add("Invalid command / arguments: \"" + command + "\"");
			Debug.warnStackTrace(e);
		}
	}

	private static void completeLevel(String levelID) throws IOException
	{
		resetLevel(levelID.contains("-") ? levelID.substring(0, levelID.indexOf("-")) : "unknown", levelID);

		SaveHandler.getActiveSave().writeLevel(levelID, Level.LEVEL_FINISHED_TOKEN, true);
		SaveHandler.getActiveSave().writeLevel(levelID, Level.LEVEL_CLEARED_TOKEN, true);
		SaveHandler.getActiveSave().writeLevel(levelID, Level.LEVEL_VISITED_TOKEN, true);

		TerrainFileHandler handler = new TerrainFileHandler(Level.getMapPathByID(levelID) + "/terrain.map");

		Level newLevel = new Level();
		newLevel.setDummyLevel(true);
		newLevel.createParticleSystem();
		newLevel.initID(levelID);
		newLevel.init(levelID, null, new ArrayList<Tile>(), new ArrayList<WrapperTexture>(), new ArrayList<Entity>(), new Vector2(), new Vector2());

		List<Tile> tiles = handler.read(newLevel);

		for (Tile tile : tiles)
		{
			if (tile instanceof TileCage)
			{
				((TileCage) tile).clear();
			}
		}
	}

	private static void completeWorld(String... worldID) throws IOException
	{
		for (String world : worldID)
		{
			resetWorld(world);

			SaveHandler.getActiveSave().writeWorld(world, Level.WORLD_FINISHED, true);

			for (String levelID : Level.getLevelMap().keySet())
			{
				if (levelID.startsWith(world))
				{
					completeLevel(levelID);
				}
			}
		}
	}

	private static void reloadCurrentLevel()
	{
		GameLauncher.setStatus(GameStatus.IN_LEVEL);
		LevelManager.initLevel(Level.getMapPathByID(GameLauncher.getLevelContext().getID()), false);
	}

	public static void resetAll()
	{
		SaveHandler.getActiveSave().perLevelInformation.clear();
		SaveHandler.getActiveSave().perWorldInformation.clear();

		GameLauncher.setStatus(GameStatus.IN_LEVEL);
		LevelManager.initLevel(Level.getMapPathByID("20-01"), false);
	}

	private static void resetWorld(String world)
	{
		SaveHandler.getActiveSave().perWorldInformation.remove(world);

		for (String levelID : Level.getLevelMap().keySet())
		{
			if (levelID.startsWith(world))
			{
				resetLevel(world, levelID);
			}
		}
	}

	private static void resetLevel(String world, String level)
	{
		Object[] data = SaveHandler.getActiveSave().perLevelInformation.get(level) != null ? SaveHandler.getActiveSave().perLevelInformation.get(level).get(Level.LEVEL_CLEARED_CAGE_COUNT) : null;

		if (data != null)
		{
			int levelOpenedCages = Integer.parseInt((String) data[0]);

			SaveHandler.getActiveSave().perLevelInformation.remove(level);
			InformationPool worldPool = SaveHandler.getActiveSave().perWorldInformation.get(world);

			if (worldPool != null)
			{
				data = worldPool.get(Level.WORLD_CLEARED_CAGE_COUNT);

				if (data != null)
				{
					worldPool.put(Level.WORLD_CLEARED_CAGE_COUNT, Integer.parseInt((String) data[0]) - levelOpenedCages + "");
				}
			}
		}
	}

	private void reportUnknownCommand(String command)
	{
		this.log.add("Unknown command: \"" + command + "\"");
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		this.inputField.render(batch);

		if (!this.log.isEmpty())
		{
			int maxLines = (int) ((this.height - 55) / Fonts.getSmallFont().getLineHeight()) - 1;

			Renderer.setFont(Fonts.getSmallFont());

			ShaderHandler.disableLighting();

			for (int i = this.log.size() - 1; this.log.size() - i < maxLines && i >= 0; i--)
			{
				Renderer.drawString(this.log.get(i), this.xOffset + 5, this.yOffset + (i - this.log.size()) * Renderer.getLineHeight() + this.height - 55, 1.0f, batch);
			}

			ShaderHandler.enableLighting();
		}
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		this.inputField.adjust(xChange, yChange);
	}

	public static boolean isStringNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (!Character.isDigit(c))
			{
				return false;
			}
		}

		return true;
	}
}
