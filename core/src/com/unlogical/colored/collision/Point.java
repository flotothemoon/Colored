package com.unlogical.colored.collision;
/**
 * A single point shape
 * 
 * @author Kova
 */
public class Point extends Shape
{
	private static final long serialVersionUID = -674059626573624889L;

	/**
	 * Create a new point
	 * 
	 * @param x
	 *            The x coordinate of the point
	 * @param y
	 *            The y coordinate of the point
	 */
	public Point(float x, float y)
	{
		this.x = x;
		this.y = y;
		checkPoints();
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#transform(org.newdawn.slick.geom.Transform)
	 */
	@Override
	public Shape transform(Transform transform)
	{
		float result[] = new float[points.length];
		transform.transform(points, 0, result, 0, points.length / 2);

		return new Point(points[0], points[1]);
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#createPoints()
	 */
	@Override
	protected void createPoints()
	{
		points = new float[2];
		points[0] = getX();
		points[1] = getY();

		maxX = x;
		maxY = y;
		minX = x;
		minY = y;

		findCenter();
		calculateRadius();
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#findCenter()
	 */
	@Override
	protected void findCenter()
	{
		center = new float[2];
		center[0] = points[0];
		center[1] = points[1];
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#calculateRadius()
	 */
	@Override
	protected void calculateRadius()
	{
		boundingCircleRadius = 0;
	}
}