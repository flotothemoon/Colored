package com.unlogical.colored.util;

import box2dLight.Light;
import box2dLight.RayHandler;

public interface ILightType
{
 	public Light createLightFor(ILightSource lightSource, int numRays, RayHandler rayHandler);
 	
 	public Light updateLight(Light light, ILightSource lightSource, RayHandler rayHandler);
}