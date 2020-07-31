package com.unlogical.colored.collision;

import com.badlogic.gdx.math.Vector2;

/**
 * Implemenation of a bunch of maths functions to do with lines. Note that lines can't be used as dynamic shapes right
 * now - also collision with the end of a line is undefined.
 * 
 * @author Kevin Glass
 */
public class Line extends Shape
{
	private static final long serialVersionUID = 5677794281224661506L;
	/** The start point of the line */
	private Vector2 start;
	/** The end point of the line */
	private Vector2 end;
	/** The vector between the two points */
	private Vector2 vec;

	/** Temporary storage - declared globally to reduce GC */
	private Vector2 loc = new Vector2(0, 0);

	/** Temporary storage - declared globally to reduce GC */
	private Vector2 closest = new Vector2(0, 0);

	/**
	 * Create a new line based on the origin and a single point
	 * 
	 * @param x
	 *            The end point of the line
	 * @param y
	 *            The end point of the line
	 * @param inner
	 *            True if this line blocks on it's inner edge
	 * @param outer
	 *            True if this line blocks on it's outer edge
	 */
	public Line(float x, float y, boolean inner, boolean outer)
	{
		this(0, 0, x, y);
	}

	/**
	 * Create a new line based on the origin and a single point
	 * 
	 * @param x
	 *            The end point of the line
	 * @param y
	 *            The end point of the line
	 */
	public Line(float x, float y)
	{
		this(x, y, true, true);
	}

	/**
	 * Create a new line based on two points
	 * 
	 * @param x1
	 *            The x coordinate of the start point
	 * @param y1
	 *            The y coordinate of the start point
	 * @param x2
	 *            The x coordinate of the end point
	 * @param y2
	 *            The y coordinate of the end point
	 */
	public Line(float x1, float y1, float x2, float y2)
	{
		this(new Vector2(x1, y1), new Vector2(x2, y2));
	}

	/**
	 * Create a line with relative second point
	 * 
	 * @param x1
	 *            The x coordinate of the start point
	 * @param y1
	 *            The y coordinate of the start point
	 * @param dx
	 *            The x change to get to the second point
	 * @param dy
	 *            The y change to get to the second point
	 * @param dummy
	 *            A dummy value
	 */
	public Line(float x1, float y1, float dx, float dy, boolean dummy)
	{
		this(new Vector2(x1, y1), new Vector2(x1 + dx, y1 + dy));
	}

	/**
	 * Create a new line based on two points
	 * 
	 * @param start
	 *            The start point
	 * @param end
	 *            The end point
	 */
	public Line(float[] start, float[] end)
	{
		super();

		set(start, end);
	}

	/**
	 * Create a new line based on two points
	 * 
	 * @param start
	 *            The start point
	 * @param end
	 *            The end point
	 */
	public Line(Vector2 start, Vector2 end)
	{
		super();

		set(start, end);
	}

	/**
	 * Configure the line
	 * 
	 * @param start
	 *            The start point of the line
	 * @param end
	 *            The end point of the line
	 */
	public void set(float[] start, float[] end)
	{
		set(start[0], start[1], end[0], end[1]);
	}

	/**
	 * Get the start point of the line
	 * 
	 * @return The start point of the line
	 */
	public Vector2 getStart()
	{
		return start;
	}

	/**
	 * Get the end point of the line
	 * 
	 * @return The end point of the line
	 */
	public Vector2 getEnd()
	{
		return end;
	}

	/**
	 * Find the length of the line
	 * 
	 * @return The the length of the line
	 */
	public float length()
	{
		return vec.len();
	}

	/**
	 * Find the length of the line squared (cheaper and good for comparisons)
	 * 
	 * @return The length of the line squared
	 */
	public float lengthSquared()
	{
		return vec.len2();
	}

	/**
	 * Configure the line
	 * 
	 * @param start
	 *            The start point of the line
	 * @param end
	 *            The end point of the line
	 */
	public void set(Vector2 start, Vector2 end)
	{
		super.pointsDirty = true;
		if (this.start == null)
		{
			this.start = new Vector2();
		}
		this.start.set(start);

		if (this.end == null)
		{
			this.end = new Vector2();
		}
		this.end.set(end);

		vec = new Vector2(end);
		vec.sub(start);
	}

	/**
	 * Configure the line without garbage
	 * 
	 * @param sx
	 *            The x coordinate of the start
	 * @param sy
	 *            The y coordinate of the start
	 * @param ex
	 *            The x coordiante of the end
	 * @param ey
	 *            The y coordinate of the end
	 */
	public void set(float sx, float sy, float ex, float ey)
	{
		super.pointsDirty = true;
		start.set(sx, sy);
		end.set(ex, ey);
		float dx = (ex - sx);
		float dy = (ey - sy);
		vec.set(dx, dy);
	}

	/**
	 * Get the x direction of this line
	 * 
	 * @return The x direction of this line
	 */
	public float getDX()
	{
		return end.x - start.x;
	}

	/**
	 * Get the y direction of this line
	 * 
	 * @return The y direction of this line
	 */
	public float getDY()
	{
		return end.y - start.y;
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#x
	 */
	@Override
	public float getX()
	{
		return getX1();
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#y
	 */
	@Override
	public float getY()
	{
		return getY1();
	}

	/**
	 * Get the x coordinate of the start point
	 * 
	 * @return The x coordinate of the start point
	 */
	public float getX1()
	{
		return start.x;
	}

	/**
	 * Get the y coordinate of the start point
	 * 
	 * @return The y coordinate of the start point
	 */
	public float getY1()
	{
		return start.y;
	}

	/**
	 * Get the x coordinate of the end point
	 * 
	 * @return The x coordinate of the end point
	 */
	public float getX2()
	{
		return end.x;
	}

	/**
	 * Get the y coordinate of the end point
	 * 
	 * @return The y coordinate of the end point
	 */
	public float getY2()
	{
		return end.y;
	}

	/**
	 * Get the shortest distance from a point to this line
	 * 
	 * @param point
	 *            The point from which we want the distance
	 * @return The distance from the line to the point
	 */
	public float distance(Vector2 point)
	{
		return (float) Math.sqrt(distanceSquared(point));
	}

	/**
	 * Check if the given point is on the line
	 * 
	 * @param point
	 *            The point to check
	 * @return True if the point is on this line
	 */
	public boolean on(Vector2 point)
	{
		getClosestPoint(point, closest);

		return point.equals(closest);
	}

	/**
	 * Get the shortest distance squared from a point to this line
	 * 
	 * @param point
	 *            The point from which we want the distance
	 * @return The distance squared from the line to the point
	 */
	public float distanceSquared(Vector2 point)
	{
		getClosestPoint(point, closest);
		closest.sub(point);

		float result = closest.len2();

		return result;
	}

	/**
	 * Get the closest point on the line to a given point
	 * 
	 * @param point
	 *            The point which we want to project
	 * @param result
	 *            The point on the line closest to the given point
	 */
	public void getClosestPoint(Vector2 point, Vector2 result)
	{
		loc.set(point);
		loc.sub(start);

		float projDistance = vec.dot(loc);

		projDistance /= vec.len2();

		if (projDistance < 0)
		{
			result.set(start);
			return;
		}
		if (projDistance > 1)
		{
			result.set(end);
			return;
		}

		result.x = start.x + projDistance * vec.x;
		result.y = start.y + projDistance * vec.y;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[Line " + start + "," + end + "]";
	}

	/**
	 * Intersect this line with another
	 * 
	 * @param other
	 *            The other line we should intersect with
	 * @return The intersection point or null if the lines are parallel
	 */
	public Vector2 intersect(Line other)
	{
		return intersect(other, false);
	}

	/**
	 * Intersect this line with another
	 * 
	 * @param other
	 *            The other line we should intersect with
	 * @param limit
	 *            True if the collision is limited to the extent of the lines
	 * @return The intersection point or null if the lines don't intersect
	 */
	public Vector2 intersect(Line other, boolean limit)
	{
		Vector2 temp = new Vector2();

		if (!intersect(other, limit, temp))
		{
			return null;
		}

		return temp;
	}

	/**
	 * Intersect this line with another
	 * 
	 * @param other
	 *            The other line we should intersect with
	 * @param limit
	 *            True if the collision is limited to the extent of the lines
	 * @param result
	 *            The resulting intersection point if any
	 * @return True if the lines intersect
	 */
	
	public boolean intersect(Line other, boolean limit, Vector2 result)
	{
		float dx1 = end.x - start.x;
		float dx2 = other.end.x - other.start.x;
		float dy1 = end.y - start.y;
		float dy2 = other.end.y - other.start.y;
		float denom = (dy2 * dx1) - (dx2 * dy1);

		if (denom == 0)
		{
			return false;
		}

		float ua = (dx2 * (start.y - other.start.y)) - (dy2 * (start.x - other.start.x));
		ua /= denom;
		float ub = (dx1 * (start.y - other.start.y)) - (dy1 * (start.x - other.start.x));
		ub /= denom;

		if ((limit) && ((ua < 0) || (ua > 1) || (ub < 0) || (ub > 1)))
		{
			return false;
		}

		float u = ua;

		float ix = start.x + (u * (end.x - start.x));
		float iy = start.y + (u * (end.y - start.y));

		result.set(ix, iy);
		return true;
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#createPoints()
	 */
	@Override
	protected void createPoints()
	{
		points = new float[4];
		points[0] = getX1();
		points[1] = getY1();
		points[2] = getX2();
		points[3] = getY2();
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#transform(org.newdawn.slick.geom.Transform)
	 */
	@Override
	public Shape transform(Transform transform)
	{
		float[] temp = new float[4];
		createPoints();
		transform.transform(points, 0, temp, 0, 2);

		return new Line(temp[0], temp[1], temp[2], temp[3]);
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#closed()
	 */
	@Override
	public boolean closed()
	{
		return false;
	}

	/**
	 * @see org.newdawn.slick.geom.Shape#intersects(org.newdawn.slick.geom.Shape)
	 */
	@Override
	public boolean intersects(Shape shape)
	{
		if (shape instanceof Circle)
		{
			return shape.intersects(this);
		}
		return super.intersects(shape);
	}
}
