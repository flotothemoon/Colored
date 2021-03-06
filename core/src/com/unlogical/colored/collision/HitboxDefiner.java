package com.unlogical.colored.collision;

public class HitboxDefiner
{
	private float distanceTop;
	private float distanceBottom;
	private float distanceLeft;
	private float distanceRight;
	
	private float[][] distances = new float[4][4];

	public HitboxDefiner(float distanceTop, float distanceBottom, float distanceLeft, float distanceRight)
	{	
		this.distanceTop = distances[0][0] = distanceTop;
		this.distanceBottom = distances[0][1] = distanceBottom;
		this.distanceLeft = distances[0][2] = distanceLeft;
		this.distanceRight = distances[0][3] = distanceRight;
		
		this.distances[1] = getRotatedDistances(1);
		this.distances[2] = getRotatedDistances(2);
		this.distances[3] = getRotatedDistances(3);
	}
	
	private float[] getRotatedDistances(int rotation)
	{
		float[] rotatedDistances = new float[4];
		
		rotatedDistances[0] = distances[0][0];
		rotatedDistances[1] = distances[0][1];
		rotatedDistances[2] = distances[0][2];
		rotatedDistances[3] = distances[0][3];
		
		for (int i = rotation; i > 0; i--)
		{
			float temp = rotatedDistances[0];
			rotatedDistances[0] = rotatedDistances[2];
			rotatedDistances[2] = rotatedDistances[1];
			rotatedDistances[1] = rotatedDistances[3];
			rotatedDistances[3] = temp;
		}
		
		return rotatedDistances;
	}
	
	public float[] getDistances(float rotation)
	{
		return distances[(int) (rotation / 90)];
	}
	
	public float[][] getDistances()
	{
		return distances;
	}

	public float getDistanceTop()
	{
		return distanceTop;
	}

	public float getDistanceBottom()
	{
		return distanceBottom;
	}

	public float getDistanceLeft()
	{
		return distanceLeft;
	}

	public float getDistanceRight()
	{
		return distanceRight;
	}
}
