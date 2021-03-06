package com.unlogical.colored.util;

import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.utils.FloatArray;

import box2dLight.ChainLight;
import box2dLight.ConeLight;
import box2dLight.Light;
import box2dLight.RayHandler;

public enum LightType implements ILightType
{
	AMBIENT()
	{
		@Override
		public Light createLightFor(ILightSource source, int numRays, RayHandler rayHandler)
		{
			return null;
		}
	},
	CONE()
	{
		@Override
		public Light createLightFor(ILightSource source, int numRays, RayHandler rayHandler)
		{
			return new ConeLight(rayHandler, numRays, source.getColor(), source.getDistance(), source.getPositionX(), source.getPositionX(), source.getSpotDirection(), source.getAngle());
		}

		@Override
		public Light updateLight(Light light, ILightSource source, RayHandler rayHandler)
		{
			((ConeLight) light).setConeDegree(source.getAngle());

			return super.updateLight(light, source, rayHandler);
		}
	},
	CHAIN()
	{
		@Override
		public Light createLightFor(ILightSource source, int numRays, RayHandler rayHandler)
		{
			List<ILightSource> sources = source.getConnectedLights();

			if (sources.size() < 2)
			{
				return null;
			}

			return new ChainLight(rayHandler, numRays, source.getColor(), source.getDistance(), source.getSpotDirection(), this.createPoints(sources));
		}

		@Override
		public Light updateLight(Light light, ILightSource source, RayHandler rayHandler)
		{
			List<ILightSource> sources = source.getConnectedLights();
			ChainLight cLight = (ChainLight) light;
			FloatArray chain = cLight.chain;
			boolean chainChanged = false;

			// if (sources.size() == cLight.chain.size / 2)
			// {
			// int i = 0;
			//
			// for (ILightSource s : sources)
			// {
			// if (chain.items[i] != s.getPositionX() || chain.items[i + 1] !=
			// s.getPositionY())
			// {
			// chainChanged = true;
			//
			// cLight.chain.items[i] = s.getPositionX();
			// cLight.chain.items[i] = s.getPositionY();
			// }
			//
			// i += 2;
			// }
			// }
			// else
			{
				chain.items = this.createPoints(sources);
				chain.size = chain.items.length;
			}

			if (chainChanged)
			{
				cLight.updateChain();
			}

			return super.updateLight(light, source, rayHandler);
		}

		private float[] createPoints(Collection<ILightSource> sources)
		{
			float[] points = new float[sources.size() * 2];

			int i = 0;
			for (ILightSource s : sources)
			{
				points[i] = s.getPositionX();
				points[i + 1] = s.getPositionY();
				i += 2;
			}

			return points;
		}
	},;

	private LightType()
	{
	}

	@Override
	public Light createLightFor(ILightSource source, int numRays, RayHandler rayHandler)
	{
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public Light updateLight(Light light, ILightSource source, RayHandler rayHandler)
	{
		light.setPosition(source.getPositionX(), source.getPositionY());
		light.setColor(source.getColor().r, source.getColor().g, source.getColor().b, source.getColor().a * source.getIntensity());
		light.setDirection(-source.getSpotDirection());
		light.setDistance(source.getDistance());

		return light;
	}
}
