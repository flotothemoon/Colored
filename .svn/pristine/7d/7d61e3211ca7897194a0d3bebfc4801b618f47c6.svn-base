package com.unlogical.colored.util;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;

public interface ICopyable<T extends ICopyable<T>>
{
	public T copy(Level level);
	
	public Hitbox getHitbox();
	
	public Vector2 getPosition();
	
	public void createMirror(LevelType levelType);
	
	public void setLevel(Level level);
	
	public void setPosition(float x, float y);
	
	public boolean shouldCreateMirror(boolean allowMirrors);
	
	public boolean isRotateable();
	
	public boolean ownsMouse();
	
	public float getCenterOfRotationX();
	
	public float getCenterOfRotationY();
}
