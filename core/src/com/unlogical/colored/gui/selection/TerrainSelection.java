package com.unlogical.colored.gui.selection;

import com.unlogical.colored.gui.GUIItem;
import com.unlogical.colored.mapeditor.MapEditor;

public class TerrainSelection extends GUIItemSelection
{
	public TerrainSelection(GUIItem[] items, int itemsPerLine, int itemsPerColumn, int xOffset, int yOffset)
	{
		super("Terrain", items, itemsPerLine, itemsPerColumn, xOffset, yOffset);
	}

	@Override
	public void itemSelected(int number, String name, String metadata)
	{
		MapEditor.changeCurrentObject(name);
		MapEditor.setClickCooldown(250);
		MapEditor.clearEntitySelection();
		MapEditor.deselectAll();
	}
}
