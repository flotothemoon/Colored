package com.unlogical.colored.entity;

public enum EntityAction implements IEntityAction
{
	IDLE_DEFAULT("idle")
	{
		@Override
		public boolean shouldContinueAnimation(Entity entity)
		{
			return (entity.isOnGround() || Math.abs(entity.getVelocity().y) < 0.1f) && !entity.isWalking();
		}

		@Override
		public EntityAction getNextAction(Entity entity)
		{
			return entity.isOnGround() ? entity.justLanded() && entity.supportsAction(LANDING) ? LANDING : WALKING : entity.supportsAction(FALL_START) ? FALL_START : FALLING;
		}
	},

	WALKING("walk")
	{
		@Override
		public boolean shouldContinueAnimation(Entity entity)
		{
			return entity.isWalking();
		}

		@Override
		public float getAnimationDelta(float delta, Entity entity)
		{
			return Math.abs(entity.getVelocity().x) / entity.getProperties().getMaxSpeed() * delta;
		}
	},

	JUMPING("jump")
	{
		@Override
		public boolean isLoopedAnimation(Entity entity)
		{
			return false;
		}

		@Override
		public boolean shouldContinueAnimation(Entity entity)
		{
			return !entity.isBlockedAbove();
		}

		@Override
		public EntityAction getNextAction(Entity entity)
		{
			return entity.supportsAction(FALL_START) ? FALL_START : FALLING;
		}
	},

	FALL_START("fall_start")
	{
		@Override
		public boolean isLoopedAnimation(Entity entity)
		{
			return false;
		}

		@Override
		public boolean shouldContinueAnimation(Entity entity)
		{
			return !entity.isOnGround();
		}

		@Override
		public EntityAction getNextAction(Entity entity)
		{
			return entity.isOnGround() ? IDLE_DEFAULT : FALLING;
		}
	},

	FALLING("fall")
	{
		@Override
		public boolean shouldContinueAnimation(Entity entity)
		{
			return !entity.isOnGround();
		}

		@Override
		public EntityAction getNextAction(Entity entity)
		{
			return entity.supportsAction(LANDING) ? LANDING : IDLE_DEFAULT;
		}
	},

	LANDING("landing")
	{
		@Override
		public boolean isLoopedAnimation(Entity entity)
		{
			return false;
		}

		@Override
		public boolean shouldContinueAnimation(Entity entity)
		{
			return entity.isOnGround();
		}

		@Override
		public EntityAction getNextAction(Entity entity)
		{
			return EntityAction.IDLE_DEFAULT;
		}
	},

	CLIMBING("climb")
	{
		@Override
		public boolean shouldContinueAnimation(Entity entity)
		{
			return entity.isClimbing();
		}

		@Override
		public EntityAction getNextAction(Entity entity)
		{
			return IDLE_DEFAULT;
		}

		@Override
		public float getAnimationDelta(float delta, Entity entity)
		{
			return Math.max(Math.abs(entity.getVelocity().x), Math.abs(entity.getVelocity().y)) / entity.getProperties().getMaxClimbSpeed() * delta;
		}

		@Override
		public boolean shouldReverseAnimation(Entity entity)
		{
			return entity.getVelocity().y < 0.0f;
		}
	},

	CLIMBING_HORIZONTAL("climbh")
	{
		@Override
		public boolean shouldContinueAnimation(Entity entity)
		{
			return entity.isClimbing();
		}

		@Override
		public EntityAction getNextAction(Entity entity)
		{
			return IDLE_DEFAULT;
		}

		@Override
		public float getAnimationDelta(float delta, Entity entity)
		{
			return Math.max(Math.abs(entity.getVelocity().x), Math.abs(entity.getVelocity().y)) / entity.getProperties().getMaxClimbSpeed() * delta;
		}

		@Override
		public boolean shouldReverseAnimation(Entity entity)
		{
			return entity.getVelocity().y < 0.0f;
		}
	},

	ATTACKING("att"),

	DYING("dying")
	{
		@Override
		public boolean isLoopedAnimation(Entity entity)
		{
			return false;
		}

		@Override
		public EntityAction getNextAction(Entity entity)
		{
			return DEAD;
		}
	},

	DEAD("dead")
	{
		@Override
		public boolean canBeInterruptedBy(EntityAction action, Entity entity)
		{
			return false;
		}
	},

	DEFAULT_V2("v2"),

	CUSTOM_ONCE0("custom")
	{
		@Override
		public boolean isLoopedAnimation(Entity entity)
		{
			return false;
		}
	},

	CUSTOM_ONCE1("custom1")
	{
		@Override
		public boolean isLoopedAnimation(Entity entity)
		{
			return false;
		}
	},

	CUSTOM_ONCE2("custom2")
	{
		@Override
		public boolean isLoopedAnimation(Entity entity)
		{
			return false;
		}
	},

	CUSTOM_LOOPED3("custom3"), CUSTOM_LOOPED4("custom4"),
	CUSTOM_LOOPED5("custom5");

	private String identifier;

	@Override
	public boolean isLoopedAnimation(Entity entity)
	{
		return true;
	}

	@Override
	public boolean shouldUpdateAnimation(Entity entity)
	{
		return true;
	}

	@Override
	public boolean shouldContinueAnimation(Entity entity)
	{
		return true;
	}

	@Override
	public boolean canBeInterruptedBy(EntityAction action, Entity entity)
	{
		return true;
	}

	@Override
	public float getAnimationDelta(float delta, Entity entity)
	{
		return delta;
	}

	@Override
	public boolean shouldReverseAnimation(Entity entity)
	{
		return false;
	}

	@Override
	public void customUpdate(float delta, Entity entity)
	{

	}

	@Override
	public EntityAction getNextAction(Entity entity)
	{
		return IDLE_DEFAULT;
	}

	private EntityAction(String saveName)
	{
		this.identifier = saveName;
	}

	public String getIdentifier()
	{
		return this.identifier;
	}

	@Override
	public String toString()
	{
		return this.name();
	}
}
