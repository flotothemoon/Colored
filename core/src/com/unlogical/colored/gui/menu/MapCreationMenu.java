package com.unlogical.colored.gui.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.configuration.DisplayConstants;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.filesystem.MapFiles;
import com.unlogical.colored.gui.UserInputLine;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.story.StoryHandler;
import com.unlogical.colored.util.GameStatus;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public class MapCreationMenu implements IMenu
{
	private UserInputLine nameInput;
	private UserInputLine IDInput;
	private Button cancelButton;
	private CreateButton createButton;

	private boolean active = false;

	private int displayWidth;
	private int displayHeight;
	private int labelPosition;
	private int inputPosition;
	private int inputWidth;
	private int width;
	private int yOffset;

	public MapCreationMenu()
	{
		displayWidth = 1920;
		displayHeight = 1080;

		labelPosition = displayWidth / 4;
		inputPosition = displayWidth / 2;

		inputWidth = displayWidth / 4;

		width = displayWidth / 2;

		yOffset = (displayHeight / 2 - DisplayConstants.USER_INPUT_FIELD_HEIGHT * 2);

		this.nameInput = new UserInputLine(inputPosition, yOffset, inputWidth, DisplayConstants.USER_INPUT_FIELD_HEIGHT);
		this.IDInput = new UserInputLine(inputPosition, yOffset + 2 * nameInput.getHeight() + 5, inputWidth, DisplayConstants.USER_INPUT_FIELD_HEIGHT);

		this.nameInput.setSuggestedInput("Name");
		this.IDInput.setSuggestedInput("ID");

		this.nameInput.setActive(true);
		this.IDInput.setActive(true);

		this.cancelButton = new Button("Cancel", labelPosition + width / 2, IDInput.getY() + IDInput.getHeight() + 10, displayWidth / 4, DisplayConstants.USER_INPUT_FIELD_HEIGHT, false)
		{
			@Override
			public void onClick()
			{
				MenuHandler.transitionTo(GameStatus.MAP_EDITOR_MENU, true);
			}
		};

		this.createButton = new CreateButton("Create", displayWidth / 4, DisplayConstants.USER_INPUT_FIELD_HEIGHT, labelPosition, IDInput.getY() + IDInput.getHeight() + 10);

		this.createButton.updateInterface();
		this.cancelButton.updateInterface();
	}

	public void update(int delta)
	{
		this.nameInput.update(delta);
		this.IDInput.update(delta);

		this.createButton.update(delta);
		this.cancelButton.update(delta);
	}

	public void render(float alphaFactor, Batch batch)
	{
		if (active)
		{
			ShaderHandler.disableLighting();

			Renderer.setFont(Fonts.getDefaultFont());
			Renderer.drawString("Create New Map", labelPosition + width / 2 - Renderer.getWidth("Create New Map") / 2, yOffset - 105 - Renderer.getLineHeight(), 1.0f, batch);
			Renderer.drawString("Name:", labelPosition, nameInput.getY() + nameInput.getHeight() / 2 - Renderer.getLineHeight() / 2, 1.0f, batch);
			Renderer.drawString("ID:", labelPosition, IDInput.getY() + IDInput.getHeight() / 2 - Renderer.getLineHeight() / 2, 1.0f, batch);

			ShapeRenderer sr = Renderer.useShapeRenderer();
			
			sr.setColor(Color.WHITE);
			sr.line(labelPosition + width / 2 - Renderer.getWidth("Create New Map") / 2 - 10, yOffset - 105, labelPosition + width / 2 + Renderer.getWidth("Create New Map") / 2 + 10, yOffset - 105);
			
			IDInput.render(1.0f, batch);
			nameInput.render(1.0f, batch);

			createButton.render(1.0f, batch);
			cancelButton.render(1.0f, batch);
		}
	}

	private class CreateButton extends Button
	{
		public CreateButton(String label, int width, int height, int xOffset, int yOffset)
		{
			super(label, xOffset, yOffset, width, height, false);
		}

		@Override
		public void onClick()
		{
			if (!nameInput.getInput().isEmpty() && !IDInput.getInput().isEmpty())
			{
				String mapName = nameInput.getInput();
				String mapID = IDInput.getInput();

				try
				{
					String mapFolder = FilePaths.MAPS + "/" + mapID;
					
					MapFiles.initFiles(mapFolder, mapFolder, mapName, mapID);

					Level.createDefaultLevel(mapFolder, mapID, mapName);

					MapEditor.initLevel(mapID, FilePaths.MAPS + "/" + mapID);

					GameLauncher.clearRecords();

					GameLauncher.setStatus(GameStatus.MAP_EDITOR);

					Level.fetchMaps();
					
					StoryHandler.init();
				}
				catch (Exception e)
				{
					Debug.warn("Could not create map " + nameInput.getInput() + ".");
					Debug.warnStackTrace(e);

					GameLauncher.setStatus(GameStatus.MAIN_MENU);
				}
			}
		}
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean isActive)
	{
		this.active = isActive;
	}
}
