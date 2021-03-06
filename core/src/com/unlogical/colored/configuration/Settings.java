package com.unlogical.colored.configuration;

import java.io.IOException;
import java.util.HashMap;

import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.ConfigFileHandler;
import com.unlogical.colored.filesystem.FileHandle;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.filesystem.FilePaths;

public final class Settings
{
	private static boolean fullscreen;

	public static void init()
	{

	}

	public static void saveSettings()
	{
		try
		{
			ConfigFileHandler cfg = new ConfigFileHandler(FileManager.getOrCreateFile(FilePaths.OPTIONS_YML));

			cfg.set("fullscreen", fullscreen);

			for (SoundVolume volume : SoundVolume.values())
			{
				cfg.set(volume.getName(), volume.volume());
			}

			cfg.write();
		}
		catch (IOException e)
		{
			Debug.warn("Exception while saving settings: " + e, e);
		}
	}

	public static void loadSettings()
	{
		Debug.log("Loading settings...");

		FileHandle settingsFile = null;

		try
		{
			settingsFile = FileManager.getOrCreateFile(FilePaths.OPTIONS_YML);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unable to get / create settings file: " + e, e);
		}

		@SuppressWarnings("serial")
		ConfigFileHandler settings = new ConfigFileHandler(settingsFile, new HashMap<String, String>()
		{
			{
				this.put("fullscreen", "true");
				this.put("sound", "67");
				this.put("music", "100");
			}
		});

		try
		{
			fullscreen = settings.getBoolean("fullscreen");

			for (SoundVolume volume : SoundVolume.values())
			{
				volume.setVolume(settings.getInteger(volume.getName()));
			}
		}
		catch (Exception e)
		{
			try
			{
				FileManager.deleteFile(settingsFile);
				FileManager.createFile(settingsFile.getPath());
			}
			catch (IOException e1)
			{
				throw new RuntimeException("Error while creating new settings file + " + e1);
			}

			Debug.warn("Settings file corrupted, created new empty settings file and set default values.");
		}

		Debug.log("Settings loaded.");
	}

	public static void setFullscreen(boolean fullscreen)
	{
		Settings.fullscreen = fullscreen;

		saveSettings();
	}

	public static boolean isFullscreen()
	{
		return fullscreen;
	}
}
