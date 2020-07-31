package com.unlogical.colored.audio;

import com.badlogic.gdx.audio.Sound;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.util.Dimension;

public class SoundWrapper
{
	private static final int DEFAULT_ALIVE_TIME = 30000;

	private SoundType type;
	private Sound sound;
	private float ownVolume;
	private float ownPitch;
	private float actualVolume;
	private float actualPitch;
	private float pan;
	private long soundID;
	private boolean playing = true;
	private boolean looping;
	private boolean soundShared;
	private boolean volumeSelfManaged;
	private Dimension soundDimension;
	private int aliveTime;

	public SoundWrapper(SoundType type, Sound sound, long soundID, boolean looping, float ownVolume, float ownPitch, float pan, Dimension soundDimension, boolean soundShared)
	{
		this.type = type;
		this.sound = sound;
		this.soundID = soundID;
		this.looping = looping;
		this.aliveTime = DEFAULT_ALIVE_TIME;
		this.ownPitch = ownPitch;
		this.ownVolume = ownVolume;
		this.soundDimension = soundDimension;
		this.soundShared = soundShared;
		this.pan = pan;
	}

	public void update(float delta)
	{
		if (this.playing && !this.looping)
		{
			this.aliveTime -= delta;
		}
	}

	public void pause()
	{
		this.playing = false;

		this.sound.pause(this.soundID);
	}

	public void resume()
	{
		this.playing = true;

		this.sound.resume(this.soundID);
	}

	public void stop()
	{
		this.playing = false;

		this.sound.stop(this.soundID);
	}

	public void setPitch(float pitch)
	{
		if (pitch != this.actualPitch)
		{
			this.sound.setPitch(this.soundID, pitch);

			this.actualPitch = pitch;
		}
	}

	public void setLooping(boolean looping)
	{
		this.sound.setLooping(this.soundID, looping);
	}

	public void setVolume(float volume)
	{
		if (this.actualVolume != volume)
		{
			this.sound.setVolume(this.soundID, volume);

			this.actualVolume = volume;
		}
	}

	public void setPan(float pan)
	{
		if (this.pan != pan)
		{
			this.sound.setPan(this.soundID, pan, this.actualVolume);

			this.pan = pan;
		}
	}

	public float getActualVolume()
	{
		return this.actualVolume;
	}

	public float getPan()
	{
		return this.pan;
	}

	public SoundType getType()
	{
		return this.type;
	}

	public boolean isPlaying()
	{
		return this.playing && !this.isDead();
	}

	public boolean isLooping()
	{
		return this.looping;
	}

	public boolean isDead()
	{
		return this.aliveTime <= 0;
	}

	public float getOwnPitch()
	{
		return this.ownPitch;
	}

	public float getOwnVolume()
	{
		return this.ownVolume;
	}

	public boolean isSoundShared()
	{
		return this.soundShared;
	}

	public Dimension getSoundDimension()
	{
		return this.soundDimension;
	}

	public boolean isVolumeSelfManaged()
	{
		return this.volumeSelfManaged;
	}

	public void setVolumeSelfManaged(boolean volumeSelfManaged)
	{
		this.volumeSelfManaged = volumeSelfManaged;
	}
}
