package com.unlogical.colored.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public class UserInputLine
{
	private final int DELETE_COOLDOWN = 100;
	private int timeToSwitchBlink = 0;
	private int deleteCooldown = 0;

	private int width;
	private int height;
	private int xOffset;
	private int yOffset;
	private int fontSize = 25;
	private int charHeight;
	private int blinkOffset = 0;
	private int maxChars = 99999;

	private final int BLINK_TIME = 500;

	private boolean isActive = false;
	private boolean textFieldFull = false;
	private boolean isFocused = false;
	private boolean allowUserInput = true;
	private boolean protectedInput = false;
	private boolean onlyNumeric = false;
	private boolean allowSpecialCharacters = false;
	private boolean displayBlinkingChar = true;
	private boolean alwaysFocus = false;

	private final char BLINKING_CHAR = '|';
	private char blinkingChar = '|';
	private char secretChar = '*';
	private char userInputChar = ' ';

	private InputChangeListener inputListener;

	private String userInput = "";
	private String displayedInput = "";
	private String suggestedInput = "";
	private Rectangle textField;

	private Color borderColor = new Color(0.8f, 0.8f, 0.8f, 1.0f);
	private Color fillColor = new Color(0.1f, 0.1f, 0.1f, 0.6f);
	private Color alertColor = Color.RED;
	private Color suggestedColor = Color.GRAY;
	private Color forbiddenColor = new Color(0.08f, 0.08f, 0.08f, 1.0f);
	private Color textColor = new Color(Color.WHITE);;

	private String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,:;!?\"'+-*/\\<>_~()[]{}%^�&= ";

	public UserInputLine(int xOffset, int yOffset, int width, int height)
	{
		this.userInput = "";
		this.displayedInput = "";
		this.width = width;
		this.height = height;
		this.xOffset = xOffset;
		this.yOffset = yOffset;

		this.updateInterface();

		this.addInputProcessor();
	}

	public UserInputLine(int width, int height, int xOffset, int yOffset, UserInputLine other)
	{
		this(xOffset, yOffset, width, height);

		this.allowUserInput = other.isAllowUserInput();
		this.allowSpecialCharacters = other.isAllowWhitespace();
		this.alertColor = other.getAlertColor();
		this.fillColor = other.getFillColor();
		this.borderColor = other.getBorderColor();
		this.forbiddenColor = other.getForbiddenColor();
		this.textColor = other.getTextColor();
		this.blinkingChar = other.getBlinkingChar();
		this.maxChars = other.getMaxChars();
		this.displayBlinkingChar = other.isDisplayBlinkingChar();
		this.onlyNumeric = other.onlyNumeric;

		this.updateInterface();

		this.addInputProcessor();
	}

	private void addInputProcessor()
	{
		InputHandler.addInputProcessor(new InputAdapter()
		{
			@Override
			public boolean keyUp(int keycode)
			{
				return false;
			}

			@Override
			public boolean keyTyped(char c)
			{
				if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || UserInputLine.this.allowSpecialCharacters && UserInputLine.this.allowedChars.contains(c + "") || c == '.' || c == '-' || c == '_')
				{
					UserInputLine.this.userInputChar = c;
				}
				else
				{
					UserInputLine.this.userInputChar = '\\';
				}

				if (UserInputLine.this.userInputChar != '\\' && (!Character.isWhitespace(UserInputLine.this.userInputChar) || UserInputLine.this.allowSpecialCharacters) && UserInputLine.this.isFocused && UserInputLine.this.allowUserInput && (!UserInputLine.this.onlyNumeric || Character.isDigit(UserInputLine.this.userInputChar) || UserInputLine.this.userInputChar == '-'))
				{
					if (UserInputLine.this.userInput.length() < UserInputLine.this.maxChars && (UserInputLine.this.displayedInput.isEmpty() && Renderer.getWidth(UserInputLine.this.userInput + UserInputLine.this.userInputChar, Fonts.getSmallFont()) + 15 <= UserInputLine.this.width || !UserInputLine.this.displayedInput.isEmpty() && Renderer.getWidth(UserInputLine.this.displayedInput + UserInputLine.this.userInputChar, Fonts.getSmallFont()) + 15 <= UserInputLine.this.width))
					{
						UserInputLine.this.userInput = UserInputLine.this.userInput + UserInputLine.this.userInputChar;
						if (UserInputLine.this.protectedInput)
						{
							UserInputLine.this.displayedInput = UserInputLine.this.displayedInput + UserInputLine.this.secretChar;
						}

						if (UserInputLine.this.inputListener != null)
						{
							try
							{
								UserInputLine.this.inputListener.onInputChanged();
							}
							catch (Exception e)
							{
								Debug.warn("Something went wrong while notifying input listener: " + e, e);
							}
						}
					}
				}

				if (Renderer.getWidth(UserInputLine.this.userInput + UserInputLine.this.userInputChar, Fonts.getSmallFont()) + 15 > UserInputLine.this.width && !UserInputLine.this.allowUserInput)
				{
					for (int i = 0; Renderer.getWidth(UserInputLine.this.userInput.substring(UserInputLine.this.userInput.length() - i, UserInputLine.this.userInput.length()), Fonts.getSmallFont()) + 15 < UserInputLine.this.width && i < UserInputLine.this.userInput.length(); i++)
					{
						UserInputLine.this.displayedInput = UserInputLine.this.userInput.substring(UserInputLine.this.userInput.length() - i, UserInputLine.this.userInput.length());
					}
				}

				return false;
			}

			@Override
			public boolean keyDown(int keycode)
			{
				return false;
			}
		});
	}

	public void update(int delta)
	{
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
		{
			this.isFocused = this.textField.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY());
		}

		if (this.alwaysFocus)
		{
			this.isFocused = true;
		}

		Renderer.setFont(Fonts.getSmallFont());

		if (this.deleteCooldown > 0)
		{
			this.deleteCooldown -= delta;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.DEL) && this.userInput.length() - 1 >= 0 && this.isFocused && this.deleteCooldown <= 0 && this.allowUserInput)
		{
			this.userInput = this.userInput.substring(0, this.userInput.length() - 1);
			if (!this.displayedInput.isEmpty())
			{
				this.displayedInput = this.displayedInput.substring(0, this.displayedInput.length() - 1);
			}

			if (this.inputListener != null)
			{
				try
				{
					this.inputListener.onInputChanged();
				}
				catch (NumberFormatException e)
				{
				}
			}

			this.deleteCooldown = this.DELETE_COOLDOWN;
		}

		if (Renderer.getWidth(this.userInput + this.userInputChar, Fonts.getSmallFont()) + 15 > this.width)
		{
			this.textFieldFull = true;
		}
		else
		{
			this.textFieldFull = false;
		}

		if (this.timeToSwitchBlink > 0)
		{
			this.timeToSwitchBlink -= delta;
		}
		else
		{
			this.timeToSwitchBlink = this.BLINK_TIME;

			if (this.blinkingChar == this.BLINKING_CHAR)
			{
				this.blinkingChar = ' ';
			}
			else
			{
				this.blinkingChar = this.BLINKING_CHAR;
			}
		}

		this.customUpdate(delta);
	}

	public void render(float alphaFactor, Batch batch)
	{
		if (this.isActive)
		{
			ShaderHandler.disableLighting();

			ShapeRenderer sr = Renderer.useShapeRenderer();

			ColorHelper.applyAlphaAndCopy(this.fillColor, alphaFactor, sr);

			if (!this.allowUserInput)
			{
				ColorHelper.applyAlphaAndCopy(this.forbiddenColor, alphaFactor, sr);
			}

			Renderer.setFont(Fonts.getSmallFont());
			sr.set(ShapeType.Filled);
			sr.rect(this.textField.x, this.textField.y, this.textField.width, this.textField.height);
			ColorHelper.applyAlphaAndCopy(this.borderColor, alphaFactor, batch);

			sr.set(ShapeType.Line);
			sr.rect(this.textField.x, this.textField.y, this.textField.width, this.textField.height);
			ColorHelper.applyAlphaAndCopy(this.textColor, alphaFactor, batch);

			Renderer.useSpriteBatch();

			if (this.displayedInput.isEmpty())
			{
				Renderer.drawString(this.userInput, this.xOffset + 5, this.yOffset + (this.height - this.charHeight) / 2, 1.0f, batch);
			}
			else
			{
				Renderer.drawString(this.displayedInput, this.xOffset + 5, this.yOffset + (this.height - this.charHeight) / 2, 1.0f, batch);
			}

			if (!this.textFieldFull && this.isFocused && this.allowUserInput)
			{
				if (this.displayedInput.isEmpty())
				{
					this.blinkOffset = this.xOffset + 5 + Renderer.getWidth(this.userInput);
				}
				else
				{
					this.blinkOffset = this.xOffset + 5 + Renderer.getWidth(this.displayedInput);
				}

				if (this.displayBlinkingChar)
				{
					Renderer.drawString(this.blinkingChar + "", this.blinkOffset, this.yOffset + (this.height - this.charHeight) / 2, 1.0f, batch);
				}
			}

			if (!this.isFocused && this.userInput.isEmpty() && !this.suggestedInput.isEmpty() && this.allowUserInput)
			{
				ColorHelper.applyAlphaAndCopy(this.suggestedColor, alphaFactor, batch);
				Renderer.drawString(this.suggestedInput, this.xOffset + 5, this.yOffset + this.height / 2 - Fonts.getSmallFont().getLineHeight() / 2, 1.0f, batch);
			}

			this.customRender(batch);

			ShaderHandler.enableLighting();
		}
	}

	public void updateInterface()
	{
		this.textField = new Rectangle(this.xOffset, this.yOffset, this.width, this.height);
		this.charHeight = (int) Fonts.getSmallFont().getCapHeight();
	}

	public void adjustX(int xChange)
	{
		this.xOffset += xChange;
		this.textField.setX(this.textField.getX() + xChange);
	}

	public void adjustY(int yChange)
	{
		this.yOffset += yChange;
		this.textField.setY(this.textField.getY() + yChange);
	}

	public void adjust(int xChange, int yChange)
	{
		this.adjustX(xChange);
		this.adjustY(yChange);
	}

	public void addInputChangedListener(InputChangeListener listener)
	{
		this.inputListener = listener;
	}

	public void alert(int timeInMiliseconds)
	{
	}

	public void setInput(String userInput)
	{
		this.userInput = userInput == null ? "" : userInput;
	}

	public void customUpdate(int delta)
	{

	}

	public void customRender(Batch batch)
	{

	}

	public int getWidth()
	{
		return this.width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return this.height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getX()
	{
		return this.xOffset;
	}

	public void setxOffset(int xOffset)
	{
		this.xOffset = xOffset;
	}

	public int getY()
	{
		return this.yOffset;
	}

	public void setyOffset(int yOffset)
	{
		this.yOffset = yOffset;
	}

	public int getFontSize()
	{
		return this.fontSize;
	}

	public void setFontSize(int fontSize)
	{
		this.fontSize = fontSize;
	}

	public boolean isActive()
	{
		return this.isActive;
	}

	public void setActive(boolean isActive)
	{
		this.isActive = isActive;
	}

	public char getBlinkingChar()
	{
		return this.blinkingChar;
	}

	public void setBlinkingChar(char blinkingChar)
	{
		this.blinkingChar = blinkingChar;
	}

	public char getUserInputChar()
	{
		return this.userInputChar;
	}

	public void setUserInputChar(char userInputChar)
	{
		this.userInputChar = userInputChar;
	}

	public String getInput()
	{
		return this.userInput;
	}

	public Color getBorderColor()
	{
		return this.borderColor;
	}

	public void setBorderColor(Color borderColor)
	{
		this.borderColor = borderColor;
	}

	public Color getFillColor()
	{
		return this.fillColor;
	}

	public void setFillColor(Color fillColor)
	{
		this.fillColor = fillColor;
	}

	public Color getTextColor()
	{
		return this.textColor;
	}

	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}

	public boolean isFocused()
	{
		return this.isFocused;
	}

	public void setFocused(boolean isFocused)
	{
		this.isFocused = isFocused;
	}

	public boolean isAllowUserInput()
	{
		return this.allowUserInput;
	}

	public void setAllowUserInput(boolean allowUserInput)
	{
		this.allowUserInput = allowUserInput;
	}

	public boolean isOnlyNumeric()
	{
		return this.onlyNumeric;
	}

	public void setOnlyNumeric(boolean onlyNumeric)
	{
		this.onlyNumeric = onlyNumeric;
	}

	public Color getAlertColor()
	{
		return this.alertColor;
	}

	public void setAlertColor(Color alertColor)
	{
		this.alertColor = alertColor;
	}

	public boolean isProtectedInput()
	{
		return this.protectedInput;
	}

	public void setProtectedInput(boolean protectedInput)
	{
		this.protectedInput = protectedInput;
	}

	public char getSecretChar()
	{
		return this.secretChar;
	}

	public void setSecretChar(char secretChar)
	{
		this.secretChar = secretChar;
	}

	public String getSuggestedInput()
	{
		return this.suggestedInput;
	}

	public void setSuggestedInput(String suggestedInput)
	{
		this.suggestedInput = suggestedInput;
	}

	public String getDisplayedInput()
	{
		return this.displayedInput;
	}

	public void setDisplayedInput(String displayedInput)
	{
		this.displayedInput = displayedInput;
	}

	public Color getSuggestedColor()
	{
		return this.suggestedColor;
	}

	public void setSuggestedColor(Color suggestedColor)
	{
		this.suggestedColor = suggestedColor;
	}

	public Color getForbiddenColor()
	{
		return this.forbiddenColor;
	}

	public void setForbiddenColor(Color forbiddenColor)
	{
		this.forbiddenColor = forbiddenColor;
	}

	public boolean isAllowWhitespace()
	{
		return this.allowSpecialCharacters;
	}

	public void setAllowSpecialCharacters(boolean allowSpecialCharacters)
	{
		this.allowSpecialCharacters = allowSpecialCharacters;
	}

	public int getMaxChars()
	{
		return this.maxChars;
	}

	public void setMaxChars(int maxChars)
	{
		this.maxChars = maxChars;
	}

	public boolean isDisplayBlinkingChar()
	{
		return this.displayBlinkingChar;
	}

	public void setDisplayBlinkingChar(boolean displayBlinkingChar)
	{
		this.displayBlinkingChar = displayBlinkingChar;
	}

	public float getCenterY()
	{
		return this.textField.getY() + this.textField.height / 2;
	}

	public String getAllowedChars()
	{
		return this.allowedChars;
	}

	public void setAllowedChars(String allowedChars)
	{
		this.allowedChars = allowedChars;
	}

	public void setAlwaysFocus(boolean alwaysFocus)
	{
		this.alwaysFocus = alwaysFocus;
	}

	public boolean isAlwaysFocus()
	{
		return this.alwaysFocus;
	}
}