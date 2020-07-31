package com.unlogical.colored.terrain.tile.properties;

import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.gui.HintProperties;
import com.unlogical.colored.resources.image.Images.HintIdentifier;

public interface ITileInteractable
{
	public boolean canInteract(EntityPlayer interactor);

	public void onInteract(EntityPlayer interactor);

	public HintIdentifier getHintIdentifier();

	public HintProperties getHintProperties();
}
