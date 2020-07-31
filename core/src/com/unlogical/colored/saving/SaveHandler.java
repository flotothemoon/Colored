package com.unlogical.colored.saving;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.filesystem.FilePaths;

public class SaveHandler
{
	private static List<SaveState> availableSaveStates = new ArrayList<SaveState>();
	private static SaveState activeSave;

	public static void init()
	{
		fetchSaveStates();

		if (availableSaveStates.isEmpty())
		{
			try
			{
				activeSave = createSave();

				Debug.log("No save states found - using default save state.");
			}
			catch (Exception e)
			{
				throw new RuntimeException("Could not create default save state - FATAL!", e);
			}
		}
		else
		{
			activeSave = availableSaveStates.get(0);

			Debug.log("Active save state selected, last accessed on " + new Date(activeSave.getLastAccess()));
		}
	}

	public static List<SaveState> fetchSaveStates()
	{
		availableSaveStates.clear();

		for (String path : FileManager.listFiles(FileManager.globaliseFile(FilePaths.SAVES)))
		{
			if (path.endsWith(".dat"))
			{
				try
				{
					String name = FileManager.getFileName(path);
					name = name.substring(0, name.lastIndexOf('.'));
					Integer.parseInt(name);

					availableSaveStates.add(new SaveState(FileManager.getFile(FileManager.globaliseFile(path))));

					Debug.log("SaveState " + FileManager.getFileName(path) + " found and read successfully.");
				}
				catch (Throwable e)
				{
					Debug.warn("SaveState " + path + " is invalid: " + e);
				}
			}
		}

		Collections.sort(availableSaveStates, new Comparator<SaveState>()
		{
			@Override
			public int compare(SaveState o1, SaveState o2)
			{
				if (o2.getLastAccess() > o1.getLastAccess())
				{
					return 1;
				}
				else if (o2.getLastAccess() < o1.getLastAccess())
				{
					return -1;
				}

				return 0;
			}
		});

		if (activeSave != null)
		{
			for (SaveState saveState : availableSaveStates)
			{
				if (saveState.getSaveID() == activeSave.getSaveID())
				{
					activeSave = saveState;
				}
			}
		}

		return availableSaveStates;
	}

	public static SaveState createSave()
	{
		try
		{
			SaveState saveState = SaveState.createSave();
			saveState.writeAll();
			availableSaveStates.add(saveState);

			return saveState;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Exception while creating save state: " + e, e);
		}
	}

	public static int getHighestID()
	{
		int highestID = 0;

		for (SaveState saveState : availableSaveStates)
		{
			if (saveState.getSaveID() > highestID)
			{
				highestID = saveState.getSaveID();
			}
		}

		return highestID;
	}

	public static void useSaveState(SaveState saveState)
	{
		if (saveState != activeSave && availableSaveStates.contains(saveState))
		{
			activeSave = saveState;
		}
	}

	public static void removeSaveState(SaveState saveState)
	{
		if (saveState == activeSave)
		{
			throw new IllegalArgumentException("Cannot remove active save state!");
		}

		saveState.remove();

		availableSaveStates.remove(saveState);
	}

	public static SaveState getActiveSave()
	{
		return activeSave;
	}

	public static void saveCurrent() throws IOException
	{
		activeSave.writeAll();

		Debug.log("Active save was saved to disk.");
	}

	public static List<SaveState> getAvailableSaveStates()
	{
		return availableSaveStates;
	}
}
