package com.unlogical.colored.wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.resources.ResourceLoader;
import com.unlogical.colored.resources.ResourceLoader.LoadMode;

public class WrapperTextureFinder
{
	private String sourceFolder;

	public WrapperTextureFinder(String sourceFolder)
	{
		this.sourceFolder = sourceFolder;
	}

	public ArrayList<WrapperTexture> getRawWrappers() throws IOException
	{
		ArrayList<WrapperTexture> wrappers = new ArrayList<WrapperTexture>();

		loadRawWrappers(sourceFolder, wrappers);

		Collections.sort(wrappers, new Comparator<WrapperTexture>()
		{
			@Override
			public int compare(WrapperTexture o1, WrapperTexture o2)
			{
				return o1.getGroup().compareTo(o2.getGroup());
			}
		});

		return wrappers;
	}

	private void loadRawWrappers(String sourceFolder, ArrayList<WrapperTexture> wrappers) throws IOException
	{
		for (String path : FileManager.listFiles(FileManager.globaliseFile(sourceFolder), true))
		{
			if (path.endsWith("-1.png") && !Character.isDigit(path.charAt(path.length() - 7)))
			{				
				try
				{	
					path = path.substring(0, path.length() - 6).replace('\\', '/');
					
					TextureRegion[][] availableImages = ResourceLoader.getAnimatedImages(path, LoadMode.INDIVIDUAL_FILES);
													
					wrappers.add(new WrapperTexture(path, availableImages));
				}
				catch (Exception e)
				{
					Debug.warn("Couldn't process wrapper " + path + ": " + e, e);
				}
			}
		}
	}

	public static String getGroup(WrapperTexture wrapper)
	{		
		if (wrapper.getSimpleName().contains("/"))
		{
			return wrapper.getSimpleName().substring(0, wrapper.getSimpleName().lastIndexOf("/"));
		}

		return "";
	}
}