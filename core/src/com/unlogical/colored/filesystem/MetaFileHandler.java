package com.unlogical.colored.filesystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.unlogical.colored.debug.Debug;

public class MetaFileHandler
{
	private File metaFile;
	private Map<String, FileInfo> fileInfoByPath = new HashMap<String, FileInfo>();
	private boolean loaded;

	public MetaFileHandler(File metaFile)
	{
		this.metaFile = metaFile;
	}

	public void load() throws IOException
	{
		this.load(false);
	}

	public void load(boolean reload) throws IOException
	{
		if (this.loaded && !reload)
		{
			return;
		}

		BufferedReader reader = new BufferedReader(new FileReader(this.metaFile));
		String line = null;

		this.fileInfoByPath.clear();

		Debug.log("Loading meta info from \"" + this.metaFile.getPath() + "\"...");

		while ((line = reader.readLine()) != null)
		{
			String[] info = line.split(";");

			this.fileInfoByPath.put(FileManager.deglobaliseFile(info[0]), new FileInfo(Integer.parseInt(info[2]), Integer.parseInt(info[1])));
		}

		Debug.log("Loaded " + this.fileInfoByPath.size() + " items.");

		reader.close();

		this.loaded = true;
	}

	public void update(Map<String, FileHandle> handles)
	{
		this.fileInfoByPath.clear();
		int offset = 0;

		for (String path : handles.keySet())
		{
			int length = handles.get(path).getLength();

			this.fileInfoByPath.put(path, new FileInfo(length, offset));

			offset += length;
		}
	}

	public void update(FileHandle handle)
	{
		FileInfo fileInfo = this.fileInfoByPath.get(handle.getPath());

		if (fileInfo != null)
		{
			fileInfo.length = handle.getLength();
		}

		this.updateOffsets();
	}

	public void updateOffsets()
	{
		Map<String, FileInfo> oldFileInfoByPath = new HashMap<String, FileInfo>(this.fileInfoByPath);
		this.fileInfoByPath.clear();

		int offset = 0;
		for (String path : oldFileInfoByPath.keySet())
		{
			FileInfo fileInfo = oldFileInfoByPath.get(path);

			fileInfo.offset = offset;

			this.fileInfoByPath.put(path, fileInfo);

			offset += fileInfo.length;
		}
	}

	public void write() throws IOException
	{
		this.write(this.metaFile);
	}

	public void write(File outputFile) throws IOException
	{
		if (!outputFile.exists())
		{
			if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists())
			{
				outputFile.getParentFile().mkdirs();
			}

			outputFile.createNewFile();
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		for (String path : this.fileInfoByPath.keySet())
		{
			FileInfo fileInfo = this.fileInfoByPath.get(path);

			writer.write(path + ";" + fileInfo.offset + ";" + fileInfo.length + "\n");
		}

		writer.close();
	}

	public FileInfo getFileInfo(String path)
	{
		return this.fileInfoByPath.get(FileManager.deglobaliseFile(path));
	}

	public FileInfo getFileInfo(FileHandle handle)
	{
		return this.fileInfoByPath.get(handle.getPath());
	}

	public Collection<String> getPaths()
	{
		return this.fileInfoByPath.keySet();
	}

	public static class FileInfo
	{
		private int length;
		private int offset;

		public FileInfo(int length, int offset)
		{
			this.length = length;
			this.offset = offset;
		}

		public int getLength()
		{
			return this.length;
		}

		public int getOffset()
		{
			return this.offset;
		}
	}

	public File getFile()
	{
		return this.metaFile;
	}
}