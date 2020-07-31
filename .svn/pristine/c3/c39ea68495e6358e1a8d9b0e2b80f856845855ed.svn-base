package com.unlogical.colored.configuration;

public enum SoundVolume
{
	SOUND("sound", 100)
	{
		@Override
		public int relVolume()
		{
			return volume;
		}
		
		@Override
		public void setVolume(int volume)
		{
			super.setVolume(volume);
		}
	},
	MUSIC("music", 100)
	{
		@Override
		public int relVolume()
		{
			return volume;
		}
	};

	private String name;
	protected int volume;
	protected float volumeFactor;

	private SoundVolume(String name, int volume)
	{
		this.name = name;
		this.volume = volume;
		this.volumeFactor = (volume) / 100.0f;
	}
	
	public float relVolumeClamped()
	{
		return ((float) relVolume()) / 100.0f;
	}

	public abstract int relVolume();

	public static int getVolume(SoundVolume volume)
	{
		return volume.volume();
	}

	static SoundVolume getVolume(String name)
	{
		return valueOf(name);
	}

	String getName()
	{
		return this.name;
	}

	public int volume()
	{
		return this.volume;
	}

	public void setVolume(int volume)
	{
		this.volume = volume;
		this.volumeFactor = (volume) / 100.0f;
	}
}