package com.unlogical.colored.gui.panel;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.gui.InputField;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.terrain.tile.TileText;

public class TextPanel extends GUIPanel
{
	private InputField inputField;
	private TileText tile;

	public TextPanel(float xOffset, float yOffset, float width, float height)
	{
		super("Edit Text", xOffset, yOffset, width, height);

		this.setFillColor(MapEditor.panelColor);

		inputField = new InputField(xOffset, yOffset, width, height);
		inputField.setActive(true);
	}

	public void init(TileText textTile)
	{
		this.tile = textTile;
		this.inputField.init(tile.getText());
	}
	
	@Override
	protected void customUpdate(int delta)
	{
		inputField.update(delta);

		tile.setText(inputField.getText());
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		inputField.render(batch);
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		inputField.adjust(xChange, yChange);
	}
}
