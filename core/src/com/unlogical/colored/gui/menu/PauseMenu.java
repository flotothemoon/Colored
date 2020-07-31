package com.unlogical.colored.gui.menu;

import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.gui.menu.MenuOptions.MenuOption;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.util.GameStatus;

public class PauseMenu extends Menu
{
	public PauseMenu(float yOffset, float offset)
	{
		super(MenuOptions.PAUSE, 0.0f, yOffset, GameLauncher.getWidth(), offset, true);
	}

	@Override
	public void onSelect(MenuItem item)
	{
		if (item.getItem().equals(MenuOption.CONTINUE.name))
		{
			LevelManager.resume();
			GameLauncher.setStatus(GameStatus.IN_LEVEL);
		}
		else if (item.getItem().equals(MenuOption.RESTART_LEVEL.name))
		{
			LevelManager.reset();
			GameLauncher.setStatus(GameStatus.IN_LEVEL);
		}
		else if (item.getItem().equals(MenuOption.EXIT_CURRENT_WORLD.name))
		{
			GameLauncher.setStatus(GameStatus.IN_LEVEL);
			LevelManager.initLevel(Level.getMapPathByID(GameLauncher.getLevelContext().getWorldHubLevelID()), false);
			LevelManager.resume();
		}
		else if (item.getItem().equals(MenuOption.SETTINGS.name))
		{
			MenuHandler.transitionTo(GameStatus.SETTINGS_MENU, false);
		}
		else if (item.getItem().equals(MenuOption.BACK_TO_MAIN_MENU.name))
		{
			MenuHandler.transitionTo(GameStatus.MAIN_MENU, true);
		}
	}

	@Override
	protected boolean isAvailable(MenuItem item)
	{
		return !item.getItem().equals(MenuOption.EXIT_CURRENT_WORLD.name) || GameLauncher.getLevelContext() != null && GameLauncher.getLevelContext().getStoryHandler().canPlayerExitCurrentWorld() && GameLauncher.getLevelContext().hasWorldHub() && !GameLauncher.getLevelContext().getWorldHubLevelID().equals(GameLauncher.getLevelContext().getID());
	}
}
