package com.unlogical.colored.util;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.collision.Hitbox;

public interface IShootable
{
	public void onShoot(LevelObject source, float rotation, float shootSpeed);

	public Vector2 getStartVelocity(LevelObject source, float rotation, float speedFactor);

	public Vector2 getPosition();

	public Hitbox getHitbox();

}
