package com.unlogical.colored.gui.selection;

import com.unlogical.colored.gui.GUIItem;
import com.unlogical.colored.mapeditor.MapEditor;

public class EntitySelection extends GUIItemSelection
{
	public EntitySelection(GUIItem[] items, int itemsPerLine, int itemsPerColumn, int xOffset, int yOffset)
	{
		super("Entities", items, itemsPerLine, itemsPerColumn, xOffset, yOffset);
	}

	@Override
	public void itemSelected(int number, String name, String metadata)
	{
		MapEditor.setEntityName(name);
		MapEditor.setClickCooldown(250);
		MapEditor.clearTerrainSelection();
		MapEditor.deselectAll();
	}
}
