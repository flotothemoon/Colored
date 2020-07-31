package com.unlogical.colored.entity;

public enum EntityType
{
	PLAYER("PLAYER"),

	SCIENTIST("SCIENTIST"),

	GOOMBA("GOOMBA"),

	KEY("KEY"), STICKYKEY("STICKYKEY"), KEYCHAIN("KEYCHAIN"),

	SHADOWBALL("SHADOWBALL"),

	ICE_SPIKE("ICESPIKES"),

	MINI_ICE_SPIKES("MINIICESPIKES"),

	ANVIL("ANVIL"),

	BOSS("BOSS"), BOSS_SPIKE_BOOTS("BOSS_SPIKE_BOOTS"), BOSS_EYE("BOSS_EYE");

	private String name;

	public static boolean contains(String name)
	{
		for (EntityType type : values())
		{
			if (type.toLowerCaseNameTag().equals(name.toLowerCase()))
			{
				return true;
			}
		}

		return false;
	}

	public static EntityType getTypeByNameTag(String nameTag)
	{
		for (EntityType type : EntityType.values())
		{
			if (type.toLowerCaseNameTag().equalsIgnoreCase(nameTag))
			{
				return type;
			}
		}

		return null;
	}

	private EntityType(String typename)
	{
		this.name = typename;
	}

	public String getTypeName()
	{
		return this.name;
	}

	public String toLowerCaseNameTag()
	{
		return this.name.toLowerCase();
	}

	public void setTypename(String typename)
	{
		this.name = typename;
	}

	@Override
	public String toString()
	{
		return this.toLowerCaseNameTag();
	}
}
