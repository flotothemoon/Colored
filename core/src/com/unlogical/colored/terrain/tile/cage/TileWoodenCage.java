package com.unlogical.colored.terrain.tile.cage;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.gui.HintProperties;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.resources.image.Images;
import com.unlogical.colored.resources.image.Images.HintIdentifier;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.terrain.tile.properties.ITileInteractable;
import com.unlogical.colored.util.Dimension;

public class TileWoodenCage extends TileCage implements ITileInteractable
{
	public TileWoodenCage(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
	}

	@Override
	public void onInteract(EntityPlayer interactor)
	{
		if (!this.isCleared())
		{
			this.clear();
			interactor.onCageCleared(this);
		}
	}

	@Override
	public boolean entitiesBounceHorizontally()
	{
		return this.isCleared();
	}

	@Override
	public boolean canInteract(EntityPlayer interactor)
	{
		return interactor.inSameDimension(this) && !this.isCleared() && interactor.isOnGround() && (this.hitbox.containsY(interactor.getCenterY()) && (interactor.getCenterX() < this.getCenterX() && this.hitbox.getMinX() - interactor.getHitbox().getMaxX() < 5.0f || interactor.getCenterX() > this.getCenterX() && interactor.getHitbox().getMinX() - this.hitbox.getMaxX() < 5.0f) || this.hitbox.contains(interactor.getHitbox()) || this.hitbox.intersects(interactor.getHitbox()));
	}

	@Override
	public HintIdentifier getHintIdentifier()
	{
		return Images.HintIdentifier.CAGE_HINT;
	}

	@Override
	public HintProperties getHintProperties()
	{
		return HintProperties.DEFAULT_PROPERTIES;
	}
}