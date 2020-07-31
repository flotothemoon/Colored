package com.unlogical.colored.desktop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.ConfigFileHandler;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.filesystem.FilePaths;

public class DesktopLauncher
{
	public static void main(String[] args)
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		Debug.log("Initialising, passed args: " + Arrays.toString(args));

		// if (GameLauncher.isDeveloperMode())
		// {
		// packEssentials();
		// }

		config.title = "Colored";

		config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
		config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
		config.vSyncEnabled = true;
		config.foregroundFPS = 60; // pcmasterrace
		config.backgroundFPS = 60;
		config.samples = 2;
		config.stencil = 8;

		config.fullscreen = false;

		try
		{
			ConfigFileHandler cfg = new ConfigFileHandler(FileManager.getFile(FilePaths.LAUNCH_ARGS));

			if (cfg.isSet("numsamples"))
			{
				config.samples = cfg.getInteger("numsamples");
			}
		}
		catch (IOException e)
		{
			Debug.warn("Unable to fetch launch.args from desktop launcher: " + e);
		}

		if (GameLauncher.isDeveloperMode())
		{
			try
			{
				File coreIcons = new File(FileManager.globaliseFile("core/assets/icon"));
				File desktopIcons = new File("../desktop/bin/icon");

				if (coreIcons.exists())
				{
					if (!desktopIcons.exists())
					{
						desktopIcons.mkdirs();
					}

					for (File file : coreIcons.listFiles())
					{
						Files.copy(file.toPath(), new File(desktopIcons.getPath() + "/" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
					}

					Debug.log("Copied icon files.");
				}
			}
			catch (IOException e)
			{
				Debug.warn("Unable to copy icon files: " + e, e);
			}
		}

		// TODO exclude items and other startup critical files from file
		// packaging
		config.addIcon("icon/icon_128x128.png", FileType.Internal);
		config.addIcon("icon/icon_64x64.png", FileType.Internal);
		config.addIcon("icon/icon_32x32.png", FileType.Internal);
		config.addIcon("icon/icon_16x16.png", FileType.Internal);

		new LwjglApplication(new GameLauncher(), config);
	}

	// private static void packEssentials()
	// {
	// try
	// {
	// Debug.log("Packing essential resource files...");
	//
	// FileManager.pack("../res", "../worlds", "../dev");
	//
	// Debug.log("Files packed.");
	// }
	// catch (Exception e)
	// {
	// Debug.warn("Unable to pack essential resources: " + e);
	// }
	// }
}
