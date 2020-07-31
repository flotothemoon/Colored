package com.unlogical.colored.gui.menu;

import static com.unlogical.colored.gui.menu.MenuOptions.MenuOption.EDITOR;
import static com.unlogical.colored.gui.menu.MenuOptions.MenuOption.PLAY;
import static com.unlogical.colored.gui.menu.MenuOptions.MenuOption.QUIT;
import static com.unlogical.colored.gui.menu.MenuOptions.MenuOption.SAVE_SLOT;
import static com.unlogical.colored.gui.menu.MenuOptions.MenuOption.SETTINGS;

import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.util.GameStatus;

public class MainMenu extends Menu
{
	public MainMenu(float yOffset, float offset)
	{
		super(MenuOptions.MAIN, 0.0f, yOffset, GameLauncher.getWidth(), offset, true);
	}

	@Override
	public void onSelect(MenuItem item)
	{
		if (item.getItem().equals(PLAY.name))
		{
			GameLauncher.goToLastVisitedLevel();
			GameLauncher.setStatus(GameStatus.IN_LEVEL);
			LevelManager.resume();
		}
		else if (item.getItem().equals(SAVE_SLOT.name))
		{
			MenuHandler.transitionTo(GameStatus.SAVE_SLOT_MENU, false);
		}
		else if (item.getItem().equals(EDITOR.name))
		{
			MenuHandler.transitionTo(GameStatus.MAP_EDITOR_MENU, false);
		}
		else if (item.getItem().equals(SETTINGS.name))
		{
			MenuHandler.transitionTo(GameStatus.SETTINGS_MENU, false);
		}
		else if (item.getItem().equals(QUIT.name))
		{
			GameLauncher.exit();
		}
	}
	
	@Override
	public void onBack()
	{

	}
}
