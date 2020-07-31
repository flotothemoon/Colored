package com.unlogical.colored.filesystem.organisation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.unlogical.colored.gui.menu.SelectionState;




public class MusicOrganisation
{
	private File directory;

	public MusicOrganisation(File directory)
	{
		this.directory = directory;
	}

	public SelectionState[] getBackgroundSaveStates()
	{
		List<File> backgrounds = new ArrayList<File>();

		for (File f : directory.listFiles())
		{
			if (f.getName().endsWith(".ogg"))
			{
				backgrounds.add(f);
			}
		}
		SelectionState[] saveStates = new SelectionState[backgrounds.size()];

		for (int i = 0; i < backgrounds.size(); i++)
		{
			saveStates[i] = getSaveState(backgrounds.get(i));
		}

		return saveStates;
	}

	private SelectionState getSaveState(File file)
	{
		return new SelectionState(file.getName(), null, "music", file.getPath(), null);
	}
}
