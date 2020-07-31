package com.unlogical.colored.entity.boss;

import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityAction;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.property.EntityProperties;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.IExtendedSpriteBatch;
import com.unlogical.colored.util.LevelObject;

public class EntityBossEye extends Entity
{
	protected float longestDistance = 500.0f;
	protected float shortestDistance = 0.0f;
	protected float eyeSize = 16.0f;
	protected float eyeCenterOffsetX;
	protected float eyeCenterOffsetY;
	protected float lookAtAbsX;
	protected float lookAtAbsY;
	protected float redFactor;

	public EntityBossEye(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(type, position, level, dimension, allowMirrors);
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		EntityBossEye copy = (EntityBossEye) super.deepCopy(copiedReferences);

		copy.eyeSize = this.eyeSize;
		copy.eyeCenterOffsetX = this.eyeCenterOffsetX;
		copy.eyeCenterOffsetY = this.eyeCenterOffsetY;
		copy.lookAtAbsX = this.lookAtAbsX;
		copy.lookAtAbsY = this.lookAtAbsY;

		return copy;
	}

	@Override
	public void updateEntity(float delta, Random rng)
	{

	}

	public void updatePosition(EntityBoss owner)
	{
		this.lookAt(this.lookAtAbsX, this.lookAtAbsY, owner);
	}

	public void lookAt(float x, float y, EntityBoss owner)
	{
		this.lookAtAbsX = x;
		this.lookAtAbsY = y;

		float eyeCenterX = owner.getPosition().x + this.eyeCenterOffsetX;
		float eyeCenterY = owner.getPosition().y + this.eyeCenterOffsetY;

		Vector2 buffer = Pools.get(Vector2.class).obtain();

		buffer.set(x, y);
		buffer.sub(eyeCenterX, eyeCenterY);

		float distance = buffer.len();

		distance = MathUtils.clamp(distance, this.shortestDistance, this.longestDistance);

		buffer.setLength(this.eyeSize / 2.0f * (distance / (this.longestDistance - this.shortestDistance)));

		Pools.free(buffer);

		this.setPosition(eyeCenterX + buffer.x - this.hitbox.getWidth() / 2.0f, eyeCenterY + buffer.y - this.hitbox.getHeight() / 2.0f);
	}

	@Override
	public void draw(Batch batch)
	{
		this.draw(batch, this.dimension);
	}

	@Override
	public void draw(Batch batch, Dimension dimension)
	{
		float beforeColor = ((IExtendedSpriteBatch) batch).getAdditiveColor();

		((IExtendedSpriteBatch) batch).setAdditiveColor(ColorHelper.multiplyRGBAndCopy(Color.BLACK, Color.RED, this.redFactor));

		super.draw(batch, dimension);

		((IExtendedSpriteBatch) batch).setAdditiveColor(beforeColor);
	}

	@Override
	protected boolean canInteractWith(Entity entity)
	{
		return false;
	}

	@Override
	protected EntityProperties getEntityProperty()
	{
		return EntityProperties.BOSS_EYE;
	}

	@Override
	public boolean checkEntityCollisions()
	{
		return false;
	}

	@Override
	public boolean collidesWithEntities()
	{
		return false;
	}

	@Override
	public boolean isBlocked()
	{
		return false;
	}

	@Override
	public boolean collidesWithTerrain()
	{
		return false;
	}

	@Override
	protected boolean canWalkSlopes()
	{
		return false;
	}

	@Override
	protected boolean useAutomatedActions()
	{
		return false;
	}

	@Override
	public boolean shouldRenderInBatch()
	{
		return false;
	}

	@Override
	protected EntityAction getCustomAction()
	{
		return EntityAction.IDLE_DEFAULT;
	}

	@Override
	public boolean supportsAction(EntityAction action)
	{
		return action == EntityAction.IDLE_DEFAULT;
	}

	@Override
	public String getTextureDirectory()
	{
		return FilePaths.ENTITY_BOSS;
	}
}
