package com.unlogical.colored.gui.panel;

import java.io.IOException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.ConfigFileHandler;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.gui.UserInputLine;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.gui.menu.MenuHandler;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.util.Renderer;

public class RenamePanel extends GUIPanel
{
	private Level level;
	private UserInputLine name;
	private Button cancelButton;
	private Button applyButton;

	public RenamePanel(float xOffset, float yOffset, float width, float height)
	{
		super("Rename Level", xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		int lineHeight = 30;
		int inputWidth = (int) (width / 2) + 15;
		int currentOffset = (int) (yOffset + 10);
		int inputX = (int) (xOffset + width / 2) - 20;

		name = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		name.setActive(true);
		name.setAllowSpecialCharacters(true);
		name.setOnlyNumeric(false);

		currentOffset += name.getHeight() + 10;

		this.cancelButton = new Button("Cancel", (int) (xOffset), (int) (currentOffset + height), (int) (width / 2), 40, false)
		{
			@Override
			public void onClick()
			{
				disable();
			}
		};

		this.applyButton = new Button("Apply", (int) (xOffset + width / 2), (int) (currentOffset + height), (int) (width / 2), 40, false)
		{
			@Override
			public void onClick()
			{
				if (!name.getInput().isEmpty())
				{
					try
					{
						ConfigFileHandler cfg;

						cfg = new ConfigFileHandler(FileManager.getFile(Level.getMapPathByID(level.getID()) + "/config.yml"));

						cfg.set("name", name.getInput());
						
						cfg.write();
						
						MapEditor.saveItAll();
						MapEditor.initLevel(level.getID(), Level.getMapPathByID(level.getID()));
						MapEditor.adjustCameraOffset(level.getCameraOffset().x, level.getCameraOffset().y);
						MenuHandler.refresh();

						disable();
					}
					catch (IOException e)
					{
						Debug.warn("Exception while applying rename change: " + e, e);
					}
				}
			}
		};

		this.setHeight(currentOffset - yOffset + this.applyButton.getHeight() + 30);
	}

	public void init(Level level)
	{
		this.level = level;

		name.setInput(level.getName());
	}

	@Override
	protected void customUpdate(int delta)
	{
		name.update(delta);
		cancelButton.update(delta);
		applyButton.update(delta);
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		name.render(alphaFactor, batch);
		cancelButton.render(alphaFactor, batch);
		applyButton.render(alphaFactor, batch);

		Renderer.setFont(Fonts.getDefaultFont());
		Renderer.drawString("New Name", xOffset + 5, name.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		name.adjust(xChange, yChange);
		cancelButton.adjust(xChange, yChange);
		applyButton.adjust(xChange, yChange);
	}
}
