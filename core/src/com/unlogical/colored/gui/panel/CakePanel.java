package com.unlogical.colored.gui.panel;

import java.util.LinkedHashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Align;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.profiling.Profiling;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public class CakePanel extends GUIPanel
{
	private static final int AVERAGED_TIME = 1337;

	private float totalFactors[] = new float[AVERAGED_TIME];

	private LinkedHashMap<PartialConsumer, float[]> storedValues = new LinkedHashMap<PartialConsumer, float[]>();

	private int currentBeforeStart;
	private int currentStart;
	private int currentEnd;

	public CakePanel(float xOffset, float yOffset, float width, float height)
	{
		super("Profiling", xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		this.initConsumers();
	}

	private void initConsumers()
	{
		this.addConsumer(new PartialConsumer("update", Color.BLUE)
		{
			@Override
			protected long getConsumedTime()
			{
				return GameLauncher.getUpdateTime();
			}
		});

		this.addConsumer(new PartialConsumer("render", Color.GREEN)
		{
			@Override
			protected long getConsumedTime()
			{
				return GameLauncher.getRenderTime();
			}
		});

		this.addConsumer(new PartialConsumer("unspecified", Color.CYAN)
		{
			@Override
			protected long getConsumedTime()
			{
				return GameLauncher.getUnspecifiedTime() < 0 ? 0 : GameLauncher.getUnspecifiedTime();
			}
		});

		this.addConsumer(new PartialConsumer("idle", new Color(Color.GRAY))
		{
			@Override
			protected long getConsumedTime()
			{
				return GameLauncher.getIdleTime();
			}
		});
	}

	private void addConsumer(PartialConsumer partialResource)
	{
		this.storedValues.put(partialResource, new float[AVERAGED_TIME]);
	}

	@Override
	public void customUpdate(int delta)
	{
		this.currentStart += delta;

		if (this.currentStart >= AVERAGED_TIME)
		{
			this.currentStart %= AVERAGED_TIME;
		}

		this.currentBeforeStart = this.currentStart - 1;

		if (this.currentBeforeStart < 0)
		{
			this.currentBeforeStart = AVERAGED_TIME - 1;
		}

		this.currentEnd = this.currentStart + delta;

		if (this.currentEnd >= AVERAGED_TIME)
		{
			this.currentEnd %= AVERAGED_TIME;
		}
	}

	public void storeCurrentValues()
	{
		if (GameLauncher.getRealDelta() == 0)
		{
			return;
		}

		float totalMilli = 1.0f / GameLauncher.getRealDelta();

		for (int i = this.currentStart; i != this.currentEnd; i++)
		{
			if (i >= AVERAGED_TIME)
			{
				i = 0;

				if (i == this.currentEnd)
				{
					break;
				}
			}

			this.totalFactors[i] = totalMilli;
		}

		for (PartialConsumer consumer : this.storedValues.keySet())
		{
			float factor = (float) consumer.getConsumedTime() / (float) GameLauncher.getTotalTime();
			float factorMilli = factor / GameLauncher.getRealDelta();

			for (int i = this.currentStart; i != this.currentEnd; i++)
			{
				if (i >= AVERAGED_TIME)
				{
					i = 0;

					if (i == this.currentEnd)
					{
						break;
					}
				}

				this.storedValues.get(consumer)[i] = factorMilli;
			}
		}
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		float circleRadius = this.width * 0.6f;
		float circleX = this.xOffset + this.width / 2.0f;
		float circleY = this.yOffset + 20 + circleRadius / 2;
		float angle = -90.0f;

		float total = 0.0f;

		for (PartialConsumer consumer : this.storedValues.keySet())
		{
			consumer.factor = 0.0f;
		}

		for (int i = this.currentStart; i != this.currentBeforeStart; i++)
		{
			if (i >= AVERAGED_TIME)
			{
				i = 0;

				if (i == this.currentBeforeStart)
				{
					break;
				}
			}

			if (this.totalFactors[i] != 0.0f)
			{
				total += this.totalFactors[i];
			}

			for (PartialConsumer consumer : this.storedValues.keySet())
			{
				if (this.storedValues.get(consumer)[i] != 0.0f)
				{
					consumer.factor += this.storedValues.get(consumer)[i];
				}
			}
		}

		float totalFactor = 0.0f;

		for (PartialConsumer consumer : this.storedValues.keySet())
		{
			consumer.factor /= total;

			totalFactor += consumer.factor;
		}

		if (totalFactor > 1.0f)
		{
			float scale = 1.0f / totalFactor;

			for (PartialConsumer consumer : this.storedValues.keySet())
			{
				consumer.factor *= scale;
			}

			totalFactor *= scale;
		}

		int index = 0;

		ShaderHandler.disableLighting();

		for (PartialConsumer consumer : this.storedValues.keySet())
		{
			if (consumer.factor > 0.0f)
			{
				float percent = Math.round(consumer.factor * 1000) / 10.0f;

				Renderer.useSpriteBatch();
				Renderer.drawString(percent + "% " + consumer.name, ColorHelper.applyAlphaAndCopy(consumer.color, alphaFactor), this.xOffset + (index > 1 ? this.width - Renderer.getWidth(percent + "% " + consumer.name) - 3 : 3), this.yOffset + index % 2 * 20, 1.0f, batch);

				ShapeRenderer sr = Renderer.useShapeRenderer();
				sr.set(ShapeType.Filled);
				sr.setColor(consumer.color);
				sr.arc(circleX, circleY, circleRadius / 2, Math.round(angle -= consumer.factor * 360.0f), Math.round(consumer.factor * 360.0f));

				index++;
			}
		}

		Renderer.useSpriteBatch();
		Renderer.drawString(Math.round(Math.round(totalFactor * 1000) / 10.0f) + "% root", this.xOffset, this.yOffset + this.height - Renderer.getLineHeight(), 1.0f, batch);

		int textY = (int) (circleY + circleRadius / 2 + 10);
		String info = Profiling.root.fancyInfoString();

		GlyphLayout layout = new GlyphLayout(Renderer.getFont(), info, Color.WHITE, this.width - 10, Align.right, true);

		Renderer.drawString(Profiling.root.fancyNameString(""), this.xOffset + 5, textY, batch);
		Renderer.drawString(info, Color.WHITE, layout, Renderer.getFont(), this.xOffset + 5, textY, batch);

		ShaderHandler.enableLighting();
	}

	@Override
	protected void onClose()
	{
		this.setShown(false);
	}

	private abstract class PartialConsumer
	{
		private float factor;
		private Color color;
		private String name;

		public PartialConsumer(String name, Color color)
		{
			this.name = name;
			this.color = color;
		}

		protected abstract long getConsumedTime();
	}
}
