package com.unlogical.colored.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.unlogical.colored.debug.Debug;

public class DataHandler
{
	public static void delete(File file)
	{
		Debug.log("Requested deletion of \"" + file.getName() + "\" at \"" + file.getPath() + "\" ...");

		if (file.isDirectory())
		{
			Debug.log("File is a directory, deleting everything inside...");

			for (File f : file.listFiles())
			{
				delete(f);
			}
		}

		file.delete();

		Debug.log("File \"" + file.getName() + "\" deleted.");
	}

	public static void delete(String file) throws IOException
	{
		file.replace('\\', '/');

		if (file.startsWith("../"))
		{
			file = file.substring(file.indexOf('/') + 1);
		}

		FileManager.deleteFile(FileManager.globaliseFile(FilePaths.DELETED) + "/" + file);
		FileManager.createDirectory(FileManager.globaliseFile(FilePaths.DELETED) + "/" + file);

		Files.move(Paths.get(FileManager.globaliseFile(file)), Paths.get(FileManager.globaliseFile(FilePaths.DELETED) + "/" + file), StandardCopyOption.REPLACE_EXISTING);
	}
}
