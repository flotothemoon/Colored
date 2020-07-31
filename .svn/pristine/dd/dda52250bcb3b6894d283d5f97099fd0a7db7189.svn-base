package com.unlogical.colored.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.MetaFileHandler.FileInfo;

public class FilePackage
{
	private String parent;
	private String path;
	private String name;
	private Map<String, FileHandle> filesByPath = new LinkedHashMap<String, FileHandle>();
	private Collection<String> allFiles = new LinkedHashSet<String>();
	private MetaFileHandler metaHandler;
	private boolean imaginary;
	private boolean global;

	FilePackage(String path, String name, boolean imaginary, boolean global)
	{
		this.path = path.replace("\\", "/");

		if (this.path.startsWith("./"))
		{
			this.path = this.path.substring(2);
		}

		int headCount = 0;

		while (this.path.startsWith("../"))
		{
			this.path = this.path.substring(3);

			headCount++;
		}

		if (this.path.contains("."))
		{
			this.path = this.path.substring(0, this.path.lastIndexOf('.'));
		}

		while (headCount-- > 0)
		{
			this.path = "../" + this.path;
		}

		this.imaginary = imaginary;
		this.global = global;

		if (!this.path.endsWith("/") && !this.path.isEmpty())
		{
			this.path = this.path + "/";
		}

		if (imaginary)
		{
			this.createMetaFileHandler();
		}

		this.parent = path.isEmpty() ? path : path.contains(".") ? path.substring(0, path.lastIndexOf('.')) : "";
		this.name = name;
	}

	private void createMetaFileHandler()
	{
		this.metaHandler = new MetaFileHandler(new File(FileManager.getMetaPath(this.path)));
		try
		{
			if (this.metaHandler.getFile().exists())
			{
				this.metaHandler.load();
			}
		}
		catch (IOException e)
		{
			Debug.warn("Unable to load meta file " + this.metaHandler.getFile() + ": " + e, e);
		}
	}

	public void createDirectory(String path) throws IOException
	{
		if (!this.imaginary)
		{
			new File(path).mkdirs();
		}
	}

	public FileHandle createFile(String path) throws IOException
	{
		FileHandle handle;

		if (this.imaginary)
		{
			handle = new FileHandle(path, this, new byte[] {});
		}
		else
		{
			handle = new FileHandle(path, this);

			File file = new File(path);

			if (file.getParentFile() != null && !file.getParentFile().exists())
			{
				file.getParentFile().mkdirs();
			}

			file.createNewFile();
		}

		this.allFiles.add(path);
		this.filesByPath.put(path, handle);

		this.writeAll();

		return handle;
	}

	public void fetchAll() throws IOException
	{
		this.allFiles.clear();

		if (this.imaginary)
		{
			this.metaHandler.load();

			this.allFiles.addAll(this.metaHandler.getPaths());
		}
		else
		{
			this.fetchFiles(this.path.isEmpty() ? "." : this.path);
		}
	}

	private void fetchFiles(String path)
	{
		Collection<File> files = new ArrayList<File>();
		File root = new File(path);

		if (root.isDirectory())
		{
			FileManager.fetchFiles(root, SearchMode.RECURSIVE, files);
		}

		for (File file : files)
		{
			if (!file.getName().endsWith(FileManager.PACKAGE_META_FILE_EXTENSION) && !file.getName().endsWith(FileManager.PACKAGE_RAW_FILE_EXTENSION))
			{
				String internalPath = file.getPath().replace('\\', '/');

				if (internalPath.startsWith("./"))
				{
					internalPath = internalPath.substring(2);
				}

				if (!this.allFiles.contains(internalPath))
				{
					this.allFiles.add(internalPath);
				}
			}
		}
	}

	public void loadAllBinaries(boolean reload) throws IOException
	{
		for (FileHandle handle : this.filesByPath.values())
		{
			if (handle.getData() == null || reload)
			{
				handle.readToBinary();
			}
		}
	}

	public void loadAll(boolean reload) throws IOException
	{
		this.loadFiles(this.filesByPath.keySet(), reload);
	}

	public void loadFiles(Collection<String> paths, boolean reload) throws IOException
	{
		RandomAccessFile file = this.imaginary ? new RandomAccessFile(FileManager.getPackagePath(this.path), "rw") : null;

		for (String path : paths)
		{
			this.loadFile(path, file, reload);
		}

		if (file != null)
		{
			file.close();
		}
	}

	public void loadFile(String path, boolean reload) throws IOException
	{
		RandomAccessFile file = this.imaginary ? new RandomAccessFile(FileManager.getPackagePath(this.path), "rw") : null;

		this.loadFile(path, file, reload);

		if (file != null)
		{
			file.close();
		}
	}

	private void loadFile(String path, RandomAccessFile file, boolean reload) throws IOException
	{
		if (this.isFileLoaded(path) && !reload)
		{
			return;
		}

		if (this.imaginary)
		{
			FileInfo info = this.metaHandler.getFileInfo(path);

			if (info == null)
			{
				throw new RuntimeException("Requested file info for file \"" + path + "\" not found.");
			}

			int length = info.getLength();

			byte[] data = new byte[length];

			file.seek(info.getOffset());
			int read = file.read(data, 0, length);

			if (read != length)
			{
				Debug.warn("Error while reading binary data for file " + path + " from " + file + ": " + read + " bytes != " + info.getLength() + " requested bytes");
			}

			this.filesByPath.put(path, new FileHandle(path, this, data));
		}
		else
		{
			this.filesByPath.put(path, new FileHandle(FileManager.globaliseFile(path), this));
		}
	}

	public void releaseAll()
	{
		this.filesByPath.clear();
	}

	public void releaseFiles(Collection<String> paths)
	{
		for (String path : paths)
		{
			this.filesByPath.remove(path);
		}
	}

	public void releaseFile(String path)
	{
		this.filesByPath.remove(path);
	}

	public void onFileWritten(FileHandle handle)
	{
		if (this.imaginary)
		{
			try
			{
				this.writeAll();
			}
			catch (IOException e)
			{
				Debug.warn("Error while writing meta / package files: " + e, e);
			}
		}
	}

	public void writeAll() throws IOException
	{
		if (this == FileManager.GLOBAL_FILE_PACKAGE)
		{
			return;
		}

		if (this.metaHandler == null)
		{
			this.createMetaFileHandler();
		}

		this.loadFiles(this.allFiles, false);
		this.loadAllBinaries(false);
		this.metaHandler.update(this.filesByPath);
		this.metaHandler.write();
		this.writePackageFile();
	}

	public void writeMetaFile() throws IOException
	{
		this.metaHandler.write();
	}

	public void writePackageFile() throws IOException
	{
		for (FileHandle handle : this.filesByPath.values())
		{
			if (handle.getData() == null)
			{
				throw new RuntimeException("Unable to write raw package file as binary data of file handle " + handle.getPath() + " is null.");
			}
		}

		File pkgFile = new File(FileManager.getPackagePath(this.path));

		if (!pkgFile.exists())
		{
			if (pkgFile.getParentFile() != null && !pkgFile.getParentFile().exists())
			{
				pkgFile.getParentFile().mkdirs();
			}

			pkgFile.createNewFile();
		}

		FileOutputStream out = new FileOutputStream(pkgFile);

		List<FileHandle> sortedHandles = new ArrayList<FileHandle>(this.filesByPath.values());
		Collections.sort(sortedHandles, new Comparator<FileHandle>()
		{
			@Override
			public int compare(FileHandle o1, FileHandle o2)
			{
				return FilePackage.this.metaHandler.getFileInfo(o2).getOffset() - FilePackage.this.metaHandler.getFileInfo(o1).getOffset();
			}
		});

		for (FileHandle handle : this.filesByPath.values())
		{
			out.write(handle.getData());
		}

		out.close();
	}

	public List<String> listFiles(String folder, boolean recursive)
	{
		List<String> subFiles = new ArrayList<String>();

		if (!folder.endsWith("/"))
		{
			folder = folder + "/";
		}

		if (this.global)
		{
			this.allFiles.clear();

			this.fetchFiles(folder);
		}

		int folderDepth = folder.length() - folder.replaceAll("/", "").length();

		for (String path : this.allFiles)
		{
			if (path.startsWith(folder) && (recursive || folderDepth == this.getDepth(path)))
			{
				subFiles.add(path);
			}
		}

		return subFiles;
	}

	public void deleteFile(String path) throws IOException
	{
		Set<String> toDelete = new HashSet<String>();
		String pathAsFolder = path.endsWith("/") ? path : path + "/";

		for (String other : this.allFiles)
		{
			if (other.equals(path) || other.startsWith(pathAsFolder))
			{
				toDelete.add(path);

				FileHandle handle = this.filesByPath.get(path);

				if (handle != null)
				{
					if (!handle.canBeDeleted())
					{
						throw new IOException("File \"" + "\" cannot be deleted, it might be in use (read/write).");
					}

					handle.onDeleted();

					this.filesByPath.remove(path);
				}
			}
		}

		if (this.global)
		{
			for (String file : toDelete)
			{
				new File(file).delete();
			}
		}

		this.allFiles.removeAll(toDelete);

		this.writeAll();
	}

	public long getTotalLoadedLength()
	{
		long totalLength = 0;

		for (FileHandle handle : this.filesByPath.values())
		{
			if (handle.getData() != null)
			{
				totalLength += handle.getLength();
			}
		}

		return totalLength;
	}

	@Override
	public String toString()
	{
		return "[" + this.getClass().getSimpleName() + " " + this.name + ": " + (this.imaginary ? "imaginary " : "") + "path=" + this.path + " parent=" + this.parent + " loadedfiles=" + this.filesByPath.size() + "]";
	}

	public boolean existsFile(String path)
	{
		if (this.allFiles.contains(path) || this.global && new File(FileManager.globaliseFileRaw(path)).exists())
		{
			if (this.global && !this.allFiles.contains(path))
			{
				this.allFiles.add(path);
			}

			return true;
		}

		return false;
	}

	public FileHandle getFile(String path)
	{
		return this.filesByPath.get(path);
	}

	public boolean isFileLoaded(String path)
	{
		return this.filesByPath.containsKey(path);
	}

	public String getPath()
	{
		return this.path;
	}

	public String getParent()
	{
		return this.parent;
	}

	public String getName()
	{
		return this.name;
	}

	public int getDepth()
	{
		return this.path.length() - this.path.replaceAll("/", "").length();
	}

	public int getDepth(String path)
	{
		return path.length() - path.replaceAll("/", "").length();
	}

	public boolean isImaginary()
	{
		return this.imaginary;
	}

	public int getFileCount()
	{
		return this.filesByPath.size();
	}

	public Collection<String> getAllFiles()
	{
		return this.allFiles;
	}
}
