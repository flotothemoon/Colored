package com.unlogical.colored.filesystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class FileHandle extends com.badlogic.gdx.files.FileHandle
{
	private OutputStream activeOutputStream;
	private final String path;
	private FilePackage pkg;
	private boolean imaginary;
	private boolean available;
	private byte[] data;

	FileHandle(String path, FilePackage pkg)
	{
		this.path = path.replace('\\', '/');
		this.pkg = pkg;
		this.imaginary = false;
		this.available = true;
	}

	FileHandle(String path, FilePackage pkg, byte[] data)
	{
		this.path = path.replace('\\', '/');
		this.pkg = pkg;
		this.data = data;
		this.imaginary = true;
		this.available = true;
	}

	@Override
	public InputStream read()
	{
		try
		{
			return createInputStream();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Exception while reading input stream for " + this + "!", e);
		}
	}

	@Override
	public String pathWithoutExtension()
	{
		if (!path.contains("."))
		{
			return path;
		}

		return this.path.substring(0, this.path.lastIndexOf('.'));
	}

	@Override
	public String name()
	{
		return FileManager.getFileName(this.path);
	}

	@Override
	public String nameWithoutExtension()
	{
		String name = FileManager.getFileName(this.path);
		
		return name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
	}
	
	@Override
	public long length()
	{
		if (imaginary)
		{
			return data.length;
		}

		return new File(path).length();
	}

	@Override
	public com.badlogic.gdx.files.FileHandle parent()
	{
		if (imaginary)
		{				
			if (path.contains("/"))
			{
				try
				{
					return FileManager.getOrCreateFile(path.substring(0, path.lastIndexOf("/")));
				}
				catch (IOException e)
				{
					throw new RuntimeException("Error while creating parent file for " + path + ": " + e, e);
				}
			}
			else
			{
				throw new RuntimeException("What are you doing in a root directory...");
			}
		}
		else
		{
			return new com.badlogic.gdx.files.FileHandle(new File(path).getParentFile());
		}		
	}
	
	@Override
	public String extension()
	{
		if (this.path.contains("."))
		{
			return this.path.substring(this.path.lastIndexOf('.') + 1);
		}
		
		return "";
	}

	@Override
	public File file()
	{
		if (imaginary)
		{
			throw new RuntimeException("Cannot create actual file for imaginary file handle: " + this);
		}

		return new File(FileManager.globaliseFile(this.path));
	}
	
	public byte[] readAllBytes()
	{
		if (this.imaginary)
		{
			return data;
		}
		else
		{
			try
			{
				return Files.readAllBytes(new File(this.path).toPath());
			}
			catch (IOException e)
			{
				throw new RuntimeException("Error while reading all bytes for " + this + ": " + e, e);
			}
		}
	}

	public InputStream createInputStream() throws IOException
	{
		checkAvailable();

		if (imaginary)
		{
			return new ByteArrayInputStream(data);
		}
		else
		{
			return new FileInputStream(path);
		}
	}

	public OutputStream createOutputStream() throws IOException
	{
		checkAvailable();

		if (activeOutputStream != null)
		{
			throw new RuntimeException("Cannot create new output stream while old one is still active.");
		}

		if (imaginary)
		{
			activeOutputStream = new ByteArrayOutputStream();
		}
		else
		{
			activeOutputStream = new FileOutputStream(path);
		}

		return activeOutputStream;
	}

	public void closeOutputStream() throws IOException
	{
		checkAvailable();

		if (imaginary)
		{
			data = ((ByteArrayOutputStream) activeOutputStream).toByteArray();
		}

		activeOutputStream.close();
		activeOutputStream = null;
		pkg.onFileWritten(this);
	}

	boolean canBeDeleted()
	{
		return available && activeOutputStream == null;
	}

	void onDeleted()
	{
		this.available = true;
		this.data = null;
	}

	void readToBinary() throws IOException
	{
		checkAvailable();

		this.data = Files.readAllBytes(new File(path).toPath());
	}

	private void checkAvailable() throws IOException
	{
		if (!available)
		{
			throw new FileWasDeletedException("This file (\"" + path + "\") is not available for use, neither read nor write (it might have been deleted).");
		}
	}

	byte[] getData()
	{
		return data;
	}

	public boolean isImaginary()
	{
		return imaginary;
	}

	public String getPath()
	{
		return path;
	}

	public String getName()
	{
		return FileManager.getFileName(path);
	}

	public FilePackage getPackage()
	{
		return pkg;
	}

	public int getLength()
	{
		return (int) (imaginary ? data.length : new File(path).length()); // max
																			// 2GB
	}

	@Override
	public String toString()
	{
		return "[" + this.getClass().getSimpleName() + ": " + path + "]";
	}

	public static class FileWasDeletedException extends IOException
	{
		private static final long serialVersionUID = -4595407546203203924L;

		public FileWasDeletedException()
		{
			super();
		}

		public FileWasDeletedException(String message)
		{
			super(message);
		}

		public FileWasDeletedException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
}