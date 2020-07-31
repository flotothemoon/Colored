package com.unlogical.colored.gui.menu;

import com.unlogical.colored.GameLauncher;

public final class MenuOptions
{
	public enum MenuOption
	{
		PLAY("Play"), SAVE_SLOT("Save Slot"), EDITOR("Editor"),
		SETTINGS("Settings"), QUIT("Quit"), CONTINUE("CONTINUE"),
		RESTART_LEVEL("Restart Level"),
		EXIT_CURRENT_WORLD("Exit Current World"),
		BACK_TO_MAIN_MENU("Back to Main Menu"), NEW_MAP("New Map"),
		LOAD_MAP("Load Map"), BACK("Back"),
		SWITCH_TO_FULLSCREEN("Switch To Fullscreen"),
		SWITCH_TO_WINDOWED("Switch To Windowed"), SOUND_VOLUME("Sound Volume"),
		SOUND_OFF("Sound Off"), MUSIC_VOLUME("Music Volume"),
		MUSIC_OFF("Music Off"),;

		public final String name;

		private MenuOption(String fancyName)
		{
			this.name = this.name().toLowerCase();
		}

		public String n()
		{
			return this.name;
		}
	}

	public static final String[] MAIN = //
	GameLauncher.isDeveloperMode() ? new String[] { MenuOption.PLAY.name, MenuOption.EDITOR.name, MenuOption.SETTINGS.name, MenuOption.SAVE_SLOT.name, MenuOption.QUIT.name } //
	: new String[] { MenuOption.PLAY.name, MenuOption.SAVE_SLOT.name, MenuOption.SETTINGS.name, MenuOption.QUIT.name };
	public static final String[] PAUSE = //
	GameLauncher.isDemoMode() ? new String[] { MenuOption.CONTINUE.name, MenuOption.RESTART_LEVEL.name, MenuOption.EXIT_CURRENT_WORLD.name, MenuOption.SETTINGS.name, "Back to Main Menu" }//
	: new String[] { MenuOption.CONTINUE.name, MenuOption.RESTART_LEVEL.name, MenuOption.SETTINGS.name, "Back to Main Menu" };
	public static final String[] SETTINGS = {};
}
