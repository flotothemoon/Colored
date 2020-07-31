package com.unlogical.colored.terrain.tile.cannon;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.IShootable;
import com.unlogical.colored.util.LevelObject;

public interface ICannonBall extends IShootable
{
	public boolean isDead();
	public boolean isOutOfMap();
	public boolean isRemoved();
	public boolean hasCollided();
	public boolean hasMirror();
	
	public Vector2 getStartPosition(LevelObject source, float rotation);
	
	public Dimension getDimension();
		
	public ICannonBall getMirroredBall();
	
	public SoundType getOnShotSound();
	public EmitterType getPreShootEmitterType();
	
	public void update(float delta);
	public void removeMirror();
}
