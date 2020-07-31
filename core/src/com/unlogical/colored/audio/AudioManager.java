package com.unlogical.colored.audio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.configuration.SoundVolume;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.util.Dimension;

public class AudioManager
{
	private static Random rng = new Random();

	private float managerVolume = 1.0f;
	private float managerPitch = 1.0f;
	private Set<SoundWrapper> activeSounds;
	private Set<SoundWrapper> soundsToRemove;

	public AudioManager()
	{
		this.activeSounds = new HashSet<SoundWrapper>();
		this.soundsToRemove = new HashSet<SoundWrapper>();
	}

	public void update(float delta)
	{
		for (SoundWrapper wrapper : this.activeSounds)
		{
			wrapper.update(delta);

			if (!wrapper.isVolumeSelfManaged())
			{
				wrapper.setVolume(wrapper.getOwnVolume() * GameLauncher.getLevelContext().getFadeFactor() * this.managerVolume * SoundVolume.SOUND.relVolumeClamped() * this.getVolumeFactorFor(wrapper.getSoundDimension(), wrapper.isSoundShared(), wrapper.getType()));
			}

			wrapper.setPitch(wrapper.getOwnPitch() * this.managerPitch * this.getPitchFactorFor(wrapper.getSoundDimension(), wrapper.isSoundShared(), wrapper.getType()));

			if (wrapper.isDead())
			{
				this.soundsToRemove.add(wrapper);
			}
		}

		this.activeSounds.removeAll(this.soundsToRemove);

		this.soundsToRemove.clear();
	}

	public SoundWrapper play(SoundType type)
	{
		return this.play(type, false);
	}

	public SoundWrapper play(SoundType type, boolean loop)
	{
		return this.play(type, loop, 1.0f, 1.0f, 0.0f, null, true, rng);
	}

	public SoundWrapper play(SoundType type, boolean loop, Dimension soundDimension, boolean soundShared, Random rng)
	{
		return this.play(type, loop, 1.0f, 1.0f, 0.0f, soundDimension, soundShared, rng);
	}

	public SoundWrapper play(SoundType type, boolean loop, float volume, float pitch, float pan, Dimension soundDimension, boolean soundShared, Random rng)
	{
		if (!this.isActive() || type.sounds.length == 0)
		{
			return null;
		}

		Sound sound = type.randomSound(rng);
		long soundID;

		float playVolume = volume * this.managerVolume * SoundVolume.SOUND.relVolumeClamped() * this.getVolumeFactorFor(soundDimension, soundShared, type);
		float playPitch = this.clampPitch(pitch * this.managerPitch * this.getPitchFactorFor(soundDimension, soundShared, type));

		if (loop)
		{
			soundID = sound.loop(playVolume, playPitch, pan);
		}
		else
		{
			soundID = sound.play(playVolume, playPitch, pan);
		}

		if (soundID == -1)
		{
			Debug.warn("Unable to play sound " + type + " at volume " + playVolume + " with pitch " + playPitch + " (" + (loop ? "looping" : "once") + ").");

			return null;
		}

		SoundWrapper wrapper = new SoundWrapper(type, sound, soundID, loop, volume, pitch, pan, soundDimension, soundShared);

		this.activeSounds.add(wrapper);

		Debug.log("Play sound " + type + " at volume " + playVolume + ", pitch " + playPitch + " and pan " + pan + " (" + (loop ? "looping" : "once") + ").");

		return wrapper;
	}

	protected float getVolumeFactorFor(Dimension soundDimension, boolean soundShared, SoundType type)
	{
		return 1.0f;
	}

	protected float getPitchFactorFor(Dimension soundDimension, boolean soundShared, SoundType type)
	{
		return 1.0f;
	}

	private float clampPitch(float pitch)
	{
		if (pitch <= 0.5f)
		{
			pitch = 0.51f;
		}

		if (pitch >= 2.0f)
		{
			pitch = 1.99f;
		}

		return pitch;
	}

	public void pauseAll()
	{
		for (SoundWrapper wrapper : this.activeSounds)
		{
			wrapper.pause();
		}
	}

	public void resumeAll()
	{
		for (SoundWrapper wrapper : this.activeSounds)
		{
			wrapper.resume();
		}
	}

	public void stopAll()
	{
		for (SoundWrapper wrapper : this.activeSounds)
		{
			wrapper.stop();
		}

		this.activeSounds.clear();
	}

	public void updateSoundVolume()
	{
		float totalManagerVolume = this.managerVolume * SoundVolume.SOUND.relVolumeClamped();

		for (SoundWrapper wrapper : this.activeSounds)
		{
			wrapper.setVolume(wrapper.getOwnVolume() * totalManagerVolume);
		}
	}

	public void updateSoundPitch()
	{
		float totalManagerPitch = this.managerPitch;

		for (SoundWrapper wrapper : this.activeSounds)
		{
			wrapper.setPitch(wrapper.getOwnPitch() * totalManagerPitch);
		}
	}

	protected boolean isActive()
	{
		return true;
	}

	public void setManagerPitch(float managerPitch)
	{
		this.managerPitch = managerPitch;

		this.updateSoundPitch();
	}

	public void setManagerVolume(float managerVolume)
	{
		this.managerVolume = managerVolume;

		this.updateSoundVolume();
	}

	public float getManagerPitch()
	{
		return this.managerPitch;
	}

	public float getManagerVolume()
	{
		return this.managerVolume;
	}

	public int getActiveSoundCount()
	{
		return this.activeSounds.size();
	}

	public static void load()
	{
		long beforeTime = System.currentTimeMillis();
		int loadedCount = 0;

		for (String file : FileManager.listFiles(FileManager.globaliseFile(FilePaths.SOUNDS)))
		{
			String name = file.contains("/") ? file.substring(file.lastIndexOf('/') + 1) : file;
			name = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
			name = name.substring(0, name.indexOf('_')).toUpperCase();

			if (!SoundType.allSounds.containsKey(name))
			{
				SoundType.allSounds.put(name, new SoundType(name.toUpperCase()));
			}
		}

		for (SoundType soundType : SoundType.allSounds.values())
		{
			try
			{
				int count = soundType.load();

				if (count == 0)
				{
					Debug.warn("No sounds found for " + soundType + ".");
				}

				loadedCount += count;
			}
			catch (Exception e)
			{
				throw new RuntimeException("Error while loading sounds for " + soundType + ": " + e, e);
			}
		}

		Debug.log("Loaded " + loadedCount + " sounds, took " + (System.currentTimeMillis() - beforeTime) + "ms.");
	}

	public static class SoundType
	{
		public static final TreeMap<String, SoundType> allSounds = new TreeMap<String, SoundType>();

		public static final SoundType MENU_CLICK = new SoundType("MENUCLICK");
		public static final SoundType JUMP = new SoundType("JUMP");
		public static final SoundType BOUNCE = new SoundType("BOUNCE");
		public static final SoundType SOFT_LANDING = new SoundType("SOFTLANDING");
		public static final SoundType HARD_LANDING = new SoundType("HARDLANDING");
		public static final SoundType CLEAR_CAGE = new SoundType("CLEARCAGE");
		public static final SoundType PICK_UP_KEY = new SoundType("PICKUPKEY");
		public static final SoundType TOGGLE_LEVER = new SoundType("TOGGLELEVER");
		public static final SoundType UNLOCK_KEYGATE = new SoundType("UNLOCKKEYGATE");
		public static final SoundType OPEN_DOOR = new SoundType("OPENDOOR");
		public static final SoundType SWITCH_DIMENSION = new SoundType("SWITCHDIMENSION");
		public static final SoundType CANNON_SHOOT_BALL = new SoundType("CANNONSHOOTBALL");
		public static final SoundType CANNON_SHOOT_CLOUD = new SoundType("CANNONSHOOTCLOUD");
		public static final SoundType CANNON_SHOOT_CREATURE = new SoundType("CANNONSHOOTCREATURE");
		public static final SoundType DEATH = new SoundType("DEATH");
		public static final SoundType BALL_HIT_WALL = new SoundType("BALLHITWALL");

		public final String name;
		private Sound[] sounds;
		private final String soundName;

		private SoundType(String name)
		{
			this.name = name;
			this.soundName = this.name.toLowerCase().replace("_", "");
			allSounds.put(this.soundName.toUpperCase(), this);
		}

		private int load() throws Exception
		{
			List<Sound> sounds = new ArrayList<Sound>();

			for (String file : FileManager.listFiles(FileManager.globaliseFile(FilePaths.SOUNDS)))
			{
				String name = file.contains("/") ? file.substring(file.lastIndexOf('/') + 1) : file;
				name = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;

				if (name.startsWith(this.soundName))
				{
					sounds.add(Gdx.audio.newSound(FileManager.getFile(FileManager.globaliseFile(file))));
				}
			}

			this.sounds = new Sound[sounds.size()];

			this.sounds = sounds.toArray(this.sounds);

			return this.sounds.length;
		}

		public static SoundType next(SoundType type)
		{
			return type.next();
		}

		public SoundType next()
		{
			String nextKey = allSounds.lowerKey(this.name);

			if (nextKey == null)
			{
				nextKey = allSounds.lastKey();
			}

			return allSounds.get(nextKey);
		}

		public Sound randomSound(Random rng)
		{
			return this.sounds[rng.nextInt(this.sounds.length)];
		}

		@Override
		public String toString()
		{
			return this.name;
		}

		public static SoundType randomSoundFromAll()
		{
			int index = (int) (Math.random() * allSounds.keySet().size());

			String key = null;
			for (String k : allSounds.keySet())
			{
				if (index-- <= 0)
				{
					key = k;
					break;
				}
			}

			return key == null ? null : allSounds.get(key);
		}
	}
}
