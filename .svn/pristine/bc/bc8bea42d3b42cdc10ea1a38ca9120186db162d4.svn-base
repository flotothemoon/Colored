package com.unlogical.colored.gui.colorpicker;

import static com.unlogical.colored.util.ColorTransformer.toHSVColor;
import static com.unlogical.colored.util.ColorTransformer.toRGBColor;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.gui.InputChangeListener;
import com.unlogical.colored.gui.UserInputLine;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.gui.panel.GUIPanel;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;

public class ColorPicker extends GUIPanel
{
	private TextureRegion saturationTexture;
	private TextureRegion hueTexture;
	private TextureRegion hueSlider;
	private TextureRegion svSelector;

	private Color previousColor;

	private Color selectorColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);

	private Color hueColor;
	private Color selectedColor;

	private Rectangle previousColorBox;
	private Rectangle selectedColorBox;

	private Rectangle hueBox;
	private Rectangle saturationBox;

	private UserInputLine hexValue;

	private int saturationXOffset = 25;
	private int saturationYOffset = 25;

	private int selectorX;
	private int selectorY;
	private int sliderY;

	private float[] hsvValues = { 0f, 1f, 1f };
	private float hueValue = 1f;

	private float lastMouseX;
	private float lastMouseY;

	private boolean inSaturationBox;
	private boolean inSlider;

	private ColorPickerListener listener;

	private Button revertButton;
	private Button closeButton;

	public ColorPicker(String title, float xOffset, float yOffset, float width, float height)
	{
		super(title, xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		try
		{
			this.saturationTexture = new TextureRegion(new Texture(FileManager.getFile(FileManager.globaliseFile("res/gui/svPane.png"))));
			this.hueTexture = new TextureRegion(new Texture(FileManager.getFile(FileManager.globaliseFile("res/gui/huePane.png"))));
			this.hueSlider = new TextureRegion(new Texture(FileManager.getFile(FileManager.globaliseFile("res/gui/hueSlider.png"))));
			this.svSelector = new TextureRegion(new Texture(FileManager.getFile(FileManager.globaliseFile("res/gui/svSelector.png"))));

			this.saturationTexture.flip(false, true);
			this.hueTexture.flip(false, true);
			this.hueSlider.flip(false, true);
			this.svSelector.flip(false, true);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unable to load color picker images: " + e, e);
		}

		this.saturationXOffset += xOffset;
		this.saturationYOffset += yOffset;

		int halfWidth = (this.saturationTexture.getRegionWidth() + this.hueTexture.getRegionWidth() + 5) / 2;

		this.selectedColorBox = new Rectangle(this.saturationXOffset + halfWidth, this.saturationYOffset + this.saturationTexture.getRegionHeight() + 5, halfWidth, 40);
		this.previousColorBox = new Rectangle(this.saturationXOffset, this.saturationYOffset + this.saturationTexture.getRegionHeight() + 5, halfWidth, 40);

		this.hueBox = new Rectangle(this.saturationXOffset + this.saturationTexture.getRegionWidth() + 5, this.saturationYOffset, this.hueTexture.getRegionWidth(), this.hueTexture.getRegionHeight());
		this.saturationBox = new Rectangle(this.saturationXOffset, this.saturationYOffset, this.saturationTexture.getRegionWidth(), this.saturationTexture.getRegionHeight());

		this.hexValue = new UserInputLine((int) this.saturationBox.getX(), (int) (this.saturationBox.getY() + this.saturationBox.getHeight()) + 45, (int) this.previousColorBox.getWidth() * 2, 40);
		this.hexValue.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (ColorPicker.this.hexValue.getInput().length() == 6)
				{
					ColorPicker.this.setHexValue(ColorPicker.this.hexValue.getInput());
				}
			}
		});
		this.hexValue.setMaxChars(6);
		this.hexValue.setAllowedChars("aAbBcCdDeEfF0123456789");
		this.hexValue.setActive(true);

		this.revertButton = new Button("Revert", (int) this.previousColorBox.getX(), (int) (this.saturationBox.getY() + this.saturationBox.getHeight() + 45 + this.hexValue.getHeight()), (int) this.previousColorBox.getWidth(), 40, false)
		{
			@Override
			public void onClick()
			{
				ColorPicker.this.listener.colorChanged(ColorPicker.this.previousColor);
				ColorPicker.this.init(ColorPicker.this.previousColor);
			}
		};

		this.closeButton = new Button("Close", (int) this.selectedColorBox.getX(), (int) (this.saturationBox.getY() + this.saturationBox.getHeight() + 45 + this.hexValue.getHeight()), (int) this.previousColorBox.getWidth(), 40, false)
		{
			@Override
			public void onClick()
			{
				Debug.log("New color is " + ColorPicker.this.selectorColor.r + ", " + ColorPicker.this.selectedColor.g + ", " + ColorPicker.this.selectedColor.b + ".");
				ColorPicker.this.disable();
			}
		};

		this.setWidth(halfWidth * 2 + 50);
		this.setHeight(this.closeButton.getY() + this.closeButton.getHeight() - this.hueBox.getY() + 50);
	}

	protected void setHexValue(String hex)
	{
		hex = hex.toLowerCase();

		float r = Integer.decode("0x" + hex.substring(0, 2)) / 255.0f;
		float g = Integer.decode("0x" + hex.substring(2, 4)) / 255.0f;
		float b = Integer.decode("0x" + hex.substring(4, 6)) / 255.0f;

		this.selectedColor.r = r;
		this.selectedColor.g = g;
		this.selectedColor.b = b;

		this.listener.colorChanged(this.selectedColor);

		this.updateHSV(this.selectedColor);

		this.selectedColor.r = r;
		this.selectedColor.g = g;
		this.selectedColor.b = b;
	}

	private void updateHexValue()
	{
		int r, g, b;

		r = Math.round(this.selectedColor.r * 255);
		g = Math.round(this.selectedColor.g * 255);
		b = Math.round(this.selectedColor.b * 255);

		String hex = this.hexify(r, 2) + this.hexify(g, 2) + this.hexify(b, 2);
		hex = hex.toUpperCase();

		this.hexValue.setInput(hex);
	}

	private String hexify(int value, int length)
	{
		String hex = Integer.toHexString(value);
		if (hex.length() != length)
		{
			for (int i = 0; i < length - hex.length(); i++)
			{
				hex = "0" + hex;
			}
		}

		return hex;
	}

	public void init(Color color)
	{
		this.previousColor = new Color(color);

		this.hueColor = new Color(this.previousColor);

		this.selectedColor = new Color(this.previousColor);

		this.updateHSV(this.previousColor);

		this.updateHexValue();
	}

	private void updateHSV(Color color)
	{
		float[] prevHSV = toHSVColor(color, new float[3]);

		float hue = prevHSV[0] / 360.0f;

		this.sliderY = (int) ((1.0f - hue) * this.hueTexture.getRegionHeight());

		this.sliderY = this.sliderY == 0 ? 1 : this.sliderY;

		hue = 1.0f - this.sliderY / (float) this.hueTexture.getRegionHeight();

		this.hsvValues[0] = this.hueValue = hue * 360.0f;
		this.hsvValues[1] = this.hsvValues[2] = 1.0f;

		toRGBColor(this.hsvValues, this.hueColor);

		this.selectorX = (int) (prevHSV[1] * this.saturationTexture.getRegionWidth());
		this.selectorY = (int) ((1.0f - prevHSV[2]) * this.saturationTexture.getRegionHeight());

		float saturation = this.selectorX / (float) this.saturationTexture.getRegionWidth();
		float val = 1.0f - this.selectorY / (float) this.saturationTexture.getRegionHeight();

		this.hsvValues[0] = this.hueValue;
		this.hsvValues[1] = saturation;
		this.hsvValues[2] = val;

		toRGBColor(this.hsvValues, this.selectedColor);
	}

	public void addColorPickerListener(ColorPickerListener listener)
	{
		this.listener = listener;
	}

	@Override
	protected void customUpdate(int delta)
	{
		int mouseX = InputHandler.getCurrentInputX();
		int mouseY = InputHandler.getCurrentInputY();

		if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT))
		{
			if (Gdx.input.isKeyJustPressed(Input.Keys.C))
			{
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(this.hexValue.getInput()), null);
			}
			else if (Gdx.input.isKeyJustPressed(Input.Keys.V))
			{
				String selection = null;

				try
				{
					selection = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor);

					if (selection.length() == 6)
					{
						this.setHexValue(selection);
					}
					else
					{
						Debug.log("String from clipboard too long (" + selection.length() + " characters).");
					}
				}
				catch (HeadlessException | UnsupportedFlavorException
						| IOException e)
				{
					Debug.warn("Could not read string from clipboard: " + e, e);
				}
			}
		}

		if (!this.inSlider && !this.inSaturationBox && Gdx.input.isButtonPressed(Input.Buttons.LEFT))
		{
			if (this.hueBox.contains(mouseX, mouseY))
			{
				this.inSlider = true;
				this.lastMouseX = -1;
			}
			else if (this.saturationBox.contains(mouseX, mouseY))
			{
				this.inSaturationBox = true;
				this.lastMouseX = -1;
			}
		}
		else if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT))
		{
			this.inSlider = false;
			this.inSaturationBox = false;
		}

		if (mouseX != this.lastMouseX || mouseY != this.lastMouseY)
		{
			if (this.inSlider)
			{
				this.sliderY = (int) Math.max(0.0f, Math.min(this.hueBox.getHeight(), mouseY - this.hueBox.getY()));

				float hue = 1 - this.sliderY / (float) this.hueTexture.getRegionHeight();

				this.hsvValues[0] = this.hueValue = hue * 360.0f;
				this.hsvValues[1] = this.hsvValues[2] = 1.0f;

				toRGBColor(this.hsvValues, this.hueColor);

				float saturation = this.selectorX / (float) this.saturationTexture.getRegionWidth();
				float val = 1.0f - this.selectorY / (float) this.saturationTexture.getRegionHeight();

				this.hsvValues[0] = this.hueValue;
				this.hsvValues[1] = saturation;
				this.hsvValues[2] = val;

				toRGBColor(this.hsvValues, this.selectedColor);

				this.listener.colorChanged(this.selectedColor);
			}
			else if (this.inSaturationBox)
			{
				this.selectorX = (int) Math.max(0.0f, Math.min(this.saturationBox.getWidth(), mouseX - this.saturationBox.getX()));
				this.selectorY = (int) Math.max(0.0f, Math.min(this.saturationBox.getHeight(), mouseY - this.saturationBox.getY()));

				float saturation = this.selectorX / (float) this.saturationTexture.getRegionWidth();
				float val = 1.0f - this.selectorY / (float) this.saturationTexture.getRegionHeight();

				this.hsvValues[0] = this.hueValue;
				this.hsvValues[1] = saturation;
				this.hsvValues[2] = val;

				toRGBColor(this.hsvValues, this.selectedColor);

				this.listener.colorChanged(this.selectedColor);
			}
		}

		this.lastMouseX = mouseX;
		this.lastMouseY = mouseY;

		this.selectorColor.r = 1.0f - this.selectedColor.r;
		this.selectorColor.g = 1.0f - this.selectedColor.g;
		this.selectorColor.b = 1.0f - this.selectedColor.b;

		if (!this.hexValue.isFocused())
		{
			this.updateHexValue();
		}

		this.hexValue.update(delta);
		this.hexValue.setInput(this.hexValue.getInput().toUpperCase());

		this.revertButton.update(delta);
		this.closeButton.update(delta);
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		ShapeRenderer sr = Renderer.useShapeRenderer();

		sr.set(ShapeType.Filled);

		sr.setColor(this.hueColor);
		sr.rect(this.saturationXOffset, this.saturationYOffset, this.saturationTexture.getRegionWidth(), this.saturationTexture.getRegionHeight());

		sr.setColor(this.selectedColor);
		sr.rect(this.selectedColorBox.getX(), this.selectedColorBox.getY(), this.selectedColorBox.getWidth(), this.selectedColorBox.getHeight());

		sr.setColor(this.previousColor);
		sr.rect(this.previousColorBox.getX(), this.previousColorBox.getY(), this.previousColorBox.getWidth(), this.previousColorBox.getHeight());

		sr.set(ShapeType.Line);

		ColorHelper.applyAlphaAndCopy(Color.BLACK, alphaFactor, sr);
		sr.rect(this.selectedColorBox.getX(), this.selectedColorBox.getY(), this.selectedColorBox.getWidth(), this.selectedColorBox.getHeight());
		sr.rect(this.previousColorBox.getX(), this.previousColorBox.getY(), this.previousColorBox.getWidth(), this.previousColorBox.getHeight());
		sr.rect(this.hueBox.getX(), this.hueBox.getY(), this.hueBox.getWidth(), this.hueBox.getHeight());
		sr.rect(this.saturationBox.getX(), this.saturationBox.getY(), this.saturationBox.getWidth(), this.saturationBox.getHeight());

		Renderer.useSpriteBatch();
		Renderer.setGrayScaleFactor(0.0f);

		batch.draw(this.saturationTexture, this.saturationXOffset, this.saturationYOffset);
		batch.draw(this.hueTexture, this.saturationXOffset + 5 + this.saturationTexture.getRegionWidth(), this.saturationYOffset);

		batch.setColor(this.selectorColor);
		batch.draw(this.svSelector, this.saturationBox.getX() + this.selectorX - this.svSelector.getRegionWidth() / 2, this.saturationBox.getY() + this.selectorY - this.svSelector.getRegionHeight() / 2);
		batch.draw(this.hueSlider, this.hueBox.getX() + this.hueBox.getWidth() / 2 - this.hueSlider.getRegionWidth() / 2, this.hueBox.getY() + this.sliderY - this.hueSlider.getRegionHeight() / 2);

		this.hexValue.render(alphaFactor, batch);

		this.revertButton.render(alphaFactor, batch);
		this.closeButton.render(alphaFactor, batch);
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		this.saturationXOffset += xChange;
		this.saturationYOffset += yChange;

		this.selectedColorBox.setX(this.selectedColorBox.getX() + xChange);
		this.selectedColorBox.setY(this.selectedColorBox.getY() + yChange);

		this.previousColorBox.setX(this.previousColorBox.getX() + xChange);
		this.previousColorBox.setY(this.previousColorBox.getY() + yChange);

		this.hueBox.setX(this.hueBox.getX() + xChange);
		this.hueBox.setY(this.hueBox.getY() + yChange);

		this.saturationBox.setX(this.saturationBox.getX() + xChange);
		this.saturationBox.setY(this.saturationBox.getY() + yChange);

		this.hexValue.adjust(xChange, yChange);

		this.revertButton.adjust(xChange, yChange);
		this.closeButton.adjust(xChange, yChange);
	}

	public boolean shouldLockInput()
	{
		return this.hexValue.isFocused();
	}
}
