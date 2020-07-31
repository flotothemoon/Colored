package com.unlogical.colored.gui.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;

public class PerformancePanel extends GUIPanel
{
	private static final int DISPLAYED_FRAMES = 200;
	private static Runtime runtime = Runtime.getRuntime();

	private long[] memoryUsage = new long[DISPLAYED_FRAMES];
	private long lastUpdate = System.currentTimeMillis();
	
	private int[] realDeltas = new int[DISPLAYED_FRAMES];
	private int[] deltas = new int[DISPLAYED_FRAMES];
	private int currentOffset;
	
	private long allocatedMemory = (runtime.totalMemory() / 1024) / 1024;
	private long freeMemory;
	private long usedMemory;
	
	public PerformancePanel(float xOffset, float yOffset, float width, float height)
	{
		super("Performance & Memory Usage", xOffset, yOffset, width, height);
		
		this.fillColor = new Color(MapEditor.panelColor);
		
		initData();
	}
	
	private void initData()
	{
		for (int i = 0; i < DISPLAYED_FRAMES; i++)
		{
			realDeltas[i] = -1;
			deltas[i] = -1;
			memoryUsage[i] = -1L;
		}
	}
	
	@Override
	public void customUpdate(int delta)
	{
		freeMemory = (runtime.freeMemory() / 1024) / 1024;		
		usedMemory = Math.max(0, allocatedMemory - freeMemory);
		
		realDeltas[currentOffset] = (int) (System.currentTimeMillis() - lastUpdate);		
		deltas[currentOffset] = delta;
		memoryUsage[currentOffset] = usedMemory;
		
		currentOffset++;
	
		if (currentOffset >= DISPLAYED_FRAMES)
		{
			currentOffset = 0;
		}
		
		lastUpdate = System.currentTimeMillis();
	}
	
	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{	
		ShapeRenderer sr = Renderer.useShapeRenderer();
		
		for (int i = 0; i < DISPLAYED_FRAMES; i++)
		{
			int dataIndex = i;
			
			dataIndex -= DISPLAYED_FRAMES - currentOffset;
			
			if (dataIndex < 0)
			{
				dataIndex = dataIndex + DISPLAYED_FRAMES;
			}			
			
			if (realDeltas[i] != -1)
			{							
				float factor = (realDeltas[dataIndex]) / (1000.0f / 60.0f);
				float barHeight = Math.min(realDeltas[dataIndex] * (height / (2000.0f / 60.0f)), this.height);				
				
				Color buffer = ColorHelper.buffer;
				
				buffer.a = alphaFactor * 0.9f;

				factor -= 1.0f;
				
				if (factor < 0.0f)
				{
					factor = 0.0f;
				}
				
				buffer.r = (factor) * 2.5f;
				buffer.g = (1.0f - factor) * 2.5f;
				buffer.b = 0.0f;
								
				sr.setColor(buffer);
				sr.set(ShapeType.Filled);
				sr.rect(this.xOffset + this.width / DISPLAYED_FRAMES * i, this.yOffset + this.height - barHeight, this.width / DISPLAYED_FRAMES, barHeight);
			}
			
			if (deltas[i] != -1 && i != DISPLAYED_FRAMES - 1)
			{								
				float barHeight = Math.min(deltas[dataIndex] * (height / (2000.0f / 60.0f)), this.height);				
				
				int nextIndex = i + 1 >= DISPLAYED_FRAMES ? 0 : i + 1;
				int nextDataIndex = dataIndex + 1 >= DISPLAYED_FRAMES ? 0 : dataIndex + 1;
				
				float nextBarHeight = Math.min(deltas[nextDataIndex] * (height / (2000.0f / 60.0f)), this.height);
				
				sr.setColor(ColorHelper.applyAlphaAndCopy(Color.RED, alphaFactor));				
				sr.rectLine(this.xOffset + this.width / DISPLAYED_FRAMES * (i + 0.5f), this.yOffset + this.height - barHeight, this.xOffset + this.width / DISPLAYED_FRAMES * (nextIndex + 0.5f), this.yOffset + this.height - nextBarHeight, 3.0f);
			}
			
			if (memoryUsage[i] != -1L)
			{
				float factor = ((float) memoryUsage[dataIndex]) / ((float) allocatedMemory);
				float barHeight = factor * height;
					
				sr.set(ShapeType.Filled);
				sr.setColor(ColorHelper.applyAlphaAndCopy(Color.BLUE, alphaFactor * 0.8f));
				sr.rect(this.xOffset + this.width / DISPLAYED_FRAMES * i, this.yOffset + this.height - barHeight, this.width / DISPLAYED_FRAMES, barHeight);
				
				sr.setColor(ColorHelper.applyAlphaAndCopy(Color.BLUE, alphaFactor * 0.6f));
				sr.rect(this.xOffset + this.width / DISPLAYED_FRAMES * i, this.yOffset + this.height - barHeight, this.width / DISPLAYED_FRAMES, barHeight);
			}
		}
		
		Renderer.useSpriteBatch();				
		Renderer.drawString("FPS: " + GameLauncher.getFPS(), this.xOffset, this.yOffset, 1.0f, batch);
				
//		g.line(xOffset + g.getLineWidth(), yOffset, xOffset + width - g.getLineWidth(), yOffset);
//		g.drawString("30 FPS / 100% Memory", xOffset, yOffset);
//		
//		g.line(xOffset + g.getLineWidth(), yOffset + height / 2.0f, xOffset + width - g.getLineWidth(), yOffset + height / 2.0f);
//		g.drawString("60 FPS / 50% Memory", xOffset, yOffset + height / 2.0f);
	}

	@Override
	protected void onClose()
	{
		setShown(false);
	}
	
	public int getRealDelta()
	{
		return realDeltas[currentOffset];
	}
	
	public static Runtime getRuntime()
	{
		return runtime;
	}
	
	public long getAllocatedMemory()
	{
		return allocatedMemory;
	}
	
	public long getFreeMemory()
	{
		return freeMemory;
	}
	
	public long getUsedMemory()
	{
		return usedMemory;
	}
}