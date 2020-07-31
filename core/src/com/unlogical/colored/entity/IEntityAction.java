package com.unlogical.colored.entity;

public interface IEntityAction
{
	public void customUpdate(float delta, Entity entity);

	public boolean isLoopedAnimation(Entity entity);

	public boolean shouldUpdateAnimation(Entity entity);

	public boolean shouldContinueAnimation(Entity entity);

	public boolean canBeInterruptedBy(EntityAction action, Entity entity);

	public float getAnimationDelta(float delta, Entity entity);

	public boolean shouldReverseAnimation(Entity entity);

	public EntityAction getNextAction(Entity entity);
}
