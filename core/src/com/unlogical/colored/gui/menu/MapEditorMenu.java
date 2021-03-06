package com.unlogical.colored.gui.menu;

import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.filesystem.organisation.MapOrganisation;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.util.GameStatus;

public class MapEditorMenu extends MapSelection
{
	protected static MapOrganisation organisation;

	public MapEditorMenu(int yOffset, int width, int height)
	{
		super(yOffset, width, height);

		this.updateItems();
	}

	@Override
	protected void onSelect(SelectionMenuItem item)
	{
		if (item instanceof SelectionState)
		{
			MapEditor.initLevel(((SelectionState) item).getID(), ((SelectionState) item).getPath());

			GameLauncher.setStatus(GameStatus.MAP_EDITOR);
		}
	}

	@Override
	protected void onBackButton()
	{
		MenuHandler.transitionTo(GameStatus.MAIN_MENU, true);
	}

	@Override
	protected void onAddButton()
	{
		MenuHandler.transitionTo(GameStatus.MAP_CREATION_MENU, false);
	}

	@Override
	protected SelectionState[] loadItems()
	{
		return Level.getLevels();
	}

}
