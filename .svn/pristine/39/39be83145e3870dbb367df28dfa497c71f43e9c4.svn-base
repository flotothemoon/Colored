package com.unlogical.colored.audio;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.audio.MusicManager.MusicWrapper.MusicDimensionWrapper;
import com.unlogical.colored.configuration.SoundVolume;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.profiling.Profiling;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.GameStatus;
import com.unlogical.colored.util.Transitions;

public class MusicManager
{
	public static final String[] MUSIC_FORMATS = new String[] { "mp3", "wav", "ogg" };
	public static final float MIN_MUSIC_VOLUME = 0.1f;
	public static final float MAX_MUSIC_VOLUME = 1.0f;

	public static final int DEFAULT_FADE_IN_TIME = 12000;
	public static final int DEFAULT_FADE_OUT_TIME = 1500;

	private static Set<MusicWrapper> activeMusic = new HashSet<MusicWrapper>();
	private static Set<MusicWrapper> toRemove = new HashSet<MusicWrapper>();

	public static void initLevel(Level level)
	{
		String music = getMusicFor(level);

		boolean musicAlreadyPlaying = false;

		cleanUpMusic();

		for (MusicWrapper wrapper : activeMusic)
		{
			if (wrapper.music.equals(music))
			{
				musicAlreadyPlaying = true;
			}
		}

		if (!musicAlreadyPlaying)
		{
			play(music, DEFAULT_FADE_IN_TIME);

			for (MusicWrapper wrapper : activeMusic)
			{
				if (!wrapper.music.equals(music))
				{
					wrapper.fadeOut(DEFAULT_FADE_OUT_TIME);
				}
			}

			update(0.0f, level);
		}
	}

	public static void update(float delta, Level level)
	{
		Profiling.startSection("music");

		float coloredFactor = MIN_MUSIC_VOLUME + level.getDimensionFactor(Dimension.COLORED) * (MAX_MUSIC_VOLUME - MIN_MUSIC_VOLUME);
		float monochromeFactor = MIN_MUSIC_VOLUME + level.getDimensionFactor(Dimension.MONOCHROME) * (MAX_MUSIC_VOLUME - MIN_MUSIC_VOLUME);

		float coloredPitch = level.getColoredDeltaFactor();
		float monochromePitch = level.getMonochromeDeltaFactor();

		for (MusicWrapper wrapper : activeMusic)
		{
			wrapper.update(delta, coloredFactor, monochromeFactor, coloredPitch, monochromePitch);
		}

		cleanUpMusic();

		Profiling.endSection("music");
	}

	private static void play(String music, int fadeInTime)
	{
		MusicWrapper wrapper = load(music);

		if (wrapper == null)
		{
			Debug.warn("Unable to play music \"" + music + "\".");

			return;
		}

		wrapper.play(fadeInTime);

		activeMusic.add(wrapper);
	}

	private static MusicWrapper load(String musicName)
	{
		try
		{
			MusicWrapper wrapper = new MusicWrapper(musicName);

			MusicDimensionWrapper coloredMusic = loadMusic(musicName + "_colored", wrapper);
			MusicDimensionWrapper monochromeMusic = loadMusic(musicName + "_monochrome", wrapper);

			wrapper.init(coloredMusic, monochromeMusic);

			return wrapper;
		}
		catch (Exception e)
		{
			Debug.warn("Unable to load music \"" + musicName + "\": " + e);

			return null;
		}
	}

	private static MusicDimensionWrapper loadMusic(String musicPath, MusicWrapper wrapper)
	{
		Music mainMusic = loadMusicFile(musicPath);

		if (mainMusic == null)
		{
			throw new RuntimeException("Main music for \"" + musicPath + "\" not found");
		}

		Music onceIntroMusic = loadMusicFile(musicPath + "_onceIn");
		Music loopIntroMusic = loadMusicFile(musicPath + "_loopIn");

		return new MusicDimensionWrapper(mainMusic, onceIntroMusic, loopIntroMusic, wrapper);
	}

	private static Music loadMusicFile(String musicFilePath)
	{
		for (String format : MUSIC_FORMATS)
		{
			try
			{
				Music music = Gdx.audio.newMusic(FileManager.getFile(FileManager.globaliseFile(FilePaths.MUSIC + "/" + musicFilePath + "." + format)));

				if (music != null)
				{
					return music;
				}
			}
			catch (IOException e)
			{
			}
		}

		return null;
	}

	private static void cleanUpMusic()
	{
		for (MusicWrapper wrapper : activeMusic)
		{
			if (!wrapper.playing)
			{
				toRemove.add(wrapper);

				wrapper.dispose();
			}
		}

		if (!toRemove.isEmpty())
		{
			activeMusic.removeAll(toRemove);
		}
	}

	public static String getMusicFor(Level level)
	{
		return level.getWorldID();
	}

	public static String getDebugString()
	{
		if (activeMusic.isEmpty())
		{
			return "none";
		}

		String str = "";

		for (MusicWrapper wrapper : activeMusic)
		{
			str += wrapper.toString() + "\n";
		}

		return str;
	}

	public static class MusicWrapper
	{
		private String music;

		private MusicDimensionWrapper coloredMusic;
		private MusicDimensionWrapper monochromeMusic;

		private boolean looping = true;
		private boolean playing;
		private boolean canAccessPitch;

		private float volume;
		private float coloredVolume;
		private float monochromeVolume;
		private float coloredPitch;
		private float monochromePitch;
		private int totalFadeInTime;
		private int fadeInTime;
		private int totalFadeOutTime;
		private int fadeOutTime;

		private MusicWrapper(String music)
		{
			this.music = music;
		}

		public MusicWrapper(String music, MusicDimensionWrapper coloredMusic, MusicDimensionWrapper monochromeMusic)
		{
			this(music);

			this.init(coloredMusic, monochromeMusic);
		}

		public void init(MusicDimensionWrapper coloredMusic, MusicDimensionWrapper monochromeMusic)
		{
			this.coloredMusic = coloredMusic;
			this.monochromeMusic = monochromeMusic;

			try
			{
				this.canAccessPitch = coloredMusic.mainMusic instanceof OpenALMusic && monochromeMusic.mainMusic instanceof OpenALMusic;
			}
			catch (Exception e)
			{
				Debug.warn("Cannot cast to OpenALMusic. OpenAl access not supported. Cannot access sourceID and pitch: " + e);

				this.canAccessPitch = false;
			}
		}

		public void play(int fadeInTime)
		{
			this.totalFadeInTime = fadeInTime;
			this.fadeInTime = fadeInTime;

			this.coloredMusic.play();
			this.monochromeMusic.play();

			this.playing = true;
		}

		public void update(float delta, float coloredFactor, float monochromeFactor, float coloredPitch, float monochromePitch)
		{
			this.volume = SoundVolume.MUSIC.relVolumeClamped();

			if (GameLauncher.isDeveloperMode() && GameLauncher.getStatus() == GameStatus.IN_LEVEL && !MapEditor.isLockingInput())
			{
				for (int i = 0; i < 10; i++)
				{
					if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0 + i))
					{
						this.coloredMusic.setPosition(i * 10);
						this.monochromeMusic.setPosition(i * 10);
					}
				}
			}

			if (this.fadeInTime > 0)
			{
				this.fadeInTime -= delta;
				this.volume *= Transitions.getFactor(1.0f - (float) this.fadeInTime / this.totalFadeInTime);
			}

			if (this.fadeOutTime > 0)
			{
				this.fadeOutTime -= delta;
				this.volume *= Transitions.getFactor((float) this.fadeOutTime / this.totalFadeOutTime);

				if (this.fadeOutTime <= 0)
				{
					this.coloredMusic.stop();
					this.monochromeMusic.stop();

					this.playing = false;
				}
			}

			this.coloredVolume = this.volume * coloredFactor;
			this.monochromeVolume = this.volume * monochromeFactor;

			this.coloredMusic.setVolume(this.coloredVolume);
			this.monochromeMusic.setVolume(this.monochromeVolume);

			if (!this.coloredMusic.isPlaying() && !this.monochromeMusic.isPlaying())
			{
				this.playing = false;
			}

			if (this.canAccessPitch)
			{
				this.coloredMusic.setPitch(coloredPitch);
				this.monochromeMusic.setPitch(monochromePitch);

				this.coloredPitch = coloredPitch;
				this.monochromePitch = monochromePitch;
			}
		}

		public void stopFadeOut()
		{
			if (this.fadeOutTime > 0)
			{
				this.fadeOutTime = 0;
				this.totalFadeOutTime = 0;
			}
		}

		public void fadeOut(int fadeOutTime)
		{
			this.totalFadeOutTime = fadeOutTime;
			this.fadeOutTime = fadeOutTime;
		}

		public boolean isFadingIn()
		{
			return this.fadeInTime > 0;
		}

		public boolean isFadingOut()
		{
			return this.fadeOutTime > 0;
		}

		public void dispose()
		{
			this.coloredMusic.dispose();
			this.monochromeMusic.dispose();
		}

		@Override
		public String toString()
		{
			return this.music + " - " + String.format("c: t:%3.1fs v:%1.2f p:%1.2f | m: t:%3.1fs v:%1.2f p:%1.2f", this.coloredMusic.getPosition(), this.coloredVolume, this.coloredPitch, this.monochromeMusic.getPosition(), this.monochromeVolume, this.monochromePitch);
		}

		public static class MusicDimensionWrapper
		{
			private Music mainMusic;
			private Music onceIntroMusic;
			private Music loopIntroMusic;
			private MusicWrapper wrapper;
			private boolean playedOnceIntro;

			public MusicDimensionWrapper(Music mainMusic, Music onceIntroMusic, Music loopIntroMusic, MusicWrapper wrapper)
			{
				if (mainMusic == null)
				{
					throw new NullPointerException("MainMusic cannot be null");
				}

				if (wrapper == null)
				{
					throw new NullPointerException("Wrapper cannot be null");
				}

				this.mainMusic = mainMusic;
				this.wrapper = wrapper;

				this.mainMusic.setOnCompletionListener(new OnCompletionListener()
				{
					@Override
					public void onCompletion(Music music)
					{
						if (MusicDimensionWrapper.this.wrapper.looping)
						{
							MusicDimensionWrapper.this.play();
						}
					}
				});

				this.onceIntroMusic = onceIntroMusic;

				if (this.onceIntroMusic != null)
				{
					this.onceIntroMusic.setOnCompletionListener(new OnCompletionListener()
					{
						@Override
						public void onCompletion(Music music)
						{
							MusicDimensionWrapper.this.playedOnceIntro = true;
							MusicDimensionWrapper.this.mainMusic.play();
						}
					});
				}

				this.loopIntroMusic = loopIntroMusic;

				if (this.loopIntroMusic != null)
				{
					this.loopIntroMusic.setOnCompletionListener(new OnCompletionListener()
					{
						@Override
						public void onCompletion(Music music)
						{
							MusicDimensionWrapper.this.mainMusic.play();
						}
					});
				}
			}

			private void play()
			{
				if (!this.mainMusic.isPlaying())
				{
					if (!this.playedOnceIntro && this.onceIntroMusic != null)
					{
						if (!this.onceIntroMusic.isPlaying())
						{
							this.onceIntroMusic.play();
						}
					}
					else if (this.loopIntroMusic != null)
					{
						if (!this.loopIntroMusic.isPlaying())
						{
							this.loopIntroMusic.play();
						}
					}
					else
					{
						this.mainMusic.play();
					}
				}
			}

			private void dispose()
			{
				this.mainMusic.dispose();

				if (this.onceIntroMusic != null)
				{
					this.onceIntroMusic.dispose();
				}

				if (this.loopIntroMusic != null)
				{
					this.loopIntroMusic.dispose();
				}
			}

			private void setPitch(float pitch)
			{
				if (this.mainMusic.isPlaying())
				{
					AL10.alSourcef(((OpenALMusic) this.mainMusic).getSourceId(), AL10.AL_PITCH, pitch);
				}
				else if (this.onceIntroMusic != null && this.onceIntroMusic.isPlaying())
				{
					AL10.alSourcef(((OpenALMusic) this.onceIntroMusic).getSourceId(), AL10.AL_PITCH, pitch);
				}
				else if (this.loopIntroMusic != null && this.loopIntroMusic.isPlaying())
				{
					AL10.alSourcef(((OpenALMusic) this.loopIntroMusic).getSourceId(), AL10.AL_PITCH, pitch);
				}
			}

			private void setVolume(float volume)
			{
				if (this.mainMusic.isPlaying())
				{
					this.mainMusic.setVolume(volume);
				}
				else if (this.onceIntroMusic != null && this.onceIntroMusic.isPlaying())
				{
					this.onceIntroMusic.setVolume(volume);
				}
				else if (this.loopIntroMusic != null && this.loopIntroMusic.isPlaying())
				{
					this.loopIntroMusic.setVolume(volume);
				}
			}

			private void stop()
			{
				if (this.mainMusic.isPlaying())
				{
					this.mainMusic.stop();
				}
				else if (this.onceIntroMusic != null && this.onceIntroMusic.isPlaying())
				{
					this.onceIntroMusic.stop();
				}
				else if (this.loopIntroMusic != null && this.loopIntroMusic.isPlaying())
				{
					this.loopIntroMusic.stop();
				}
			}

			private void setPosition(float position)
			{
				if (this.mainMusic.isPlaying())
				{
					this.mainMusic.setPosition(position);
				}
			}

			private float getPosition()
			{
				return this.mainMusic.getPosition();
			}

			private boolean isPlaying()
			{
				return this.mainMusic.isPlaying() || this.onceIntroMusic != null && this.onceIntroMusic.isPlaying() || this.loopIntroMusic != null && this.loopIntroMusic.isPlaying();
			}
		}
	}
}
