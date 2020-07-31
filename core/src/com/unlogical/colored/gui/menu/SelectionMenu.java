package com.unlogical.colored.gui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.configuration.DisplayConstants;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.gui.menu.MenuHandler.MenuEmitterHandler;
import com.unlogical.colored.input.InputAction;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.util.CursorType;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public abstract class SelectionMenu extends InputAdapter implements IMenu
{
	public static final float SELECTION_ALPHA = 0.9f;
	public static final float HOVER_ALPHA = 0.8f;
	public static final float FILL_ALPHA = 0.6f;

	public static final float BETWEEN_ITEMS_OFFSET = 18;

	protected SelectionMenuItem[] items;

	private int topOption;
	private int bottomOption;

	private int selectedOption = -1;
	private int hoveredOption = -1;
	private int deleteOption = -1;

	private int xOffset;
	private int yOffset;

	private int height;
	private int width;

	private int actualOffset;
	private int barHeight;
	private int selectedButton = -1;
	private int timeSinceLastClicked;
	private int lastClickedOption = -1;

	private float barOffset = 0.0f;

	private boolean anyOptionHovered = false;
	private boolean active = false;
	private boolean wasLoaded = false;
	private boolean movingMode = false;
	private boolean showBar = false;
	private boolean addedListener = false;

	private MenuEmitterHandler movableScrollbar;

	private Color backgroundColor = new Color(0.1f, 0.1f, 0.15f, 0.4f);
	private Color infoColor = Color.LIGHT_GRAY;
	private Color notActiveColor = Color.GRAY;
	private Color selectionColor = new Color(0.3f, 0.3f, 0.3f, SELECTION_ALPHA);
	private Color hoverColor = new Color(0.3f, 0.3f, 0.3f, HOVER_ALPHA);
	private Color fillColor = new Color(0.2f, 0.2f, 0.2f, FILL_ALPHA);
	private Color barColor = new Color(0.4f, 0.4f, 0.4f, 0.3f);

	private Button backButton;
	private Button initiateDeleteButton;
	private Button confirmDeleteButton;

	public SelectionMenu(SelectionMenuItem[] items, int yOffset, int width, int height)
	{
		this.height = height;
		this.width = width;

		this.initItems(items);

		this.wasLoaded = true;

		this.xOffset = GameLauncher.TARGET_WIDTH / 2 - width / 2;
		this.yOffset = yOffset;

		this.initiateDeleteButton = new Button("Delete", 0, 0, 0, 0, true)
		{
			@Override
			public void onClick()
			{
				int option = SelectionMenu.this.anyOptionHovered ? SelectionMenu.this.hoveredOption : SelectionMenu.this.selectedOption;

				if (SelectionMenu.this.initiateDeleteButton.getButtonLabel().equalsIgnoreCase("delete"))
				{
					SelectionMenu.this.deleteOption = option;

					SelectionMenu.this.initiateDeleteButton.setText("cancel");
					SelectionMenu.this.initiateDeleteButton.setLinkedAction(InputAction.CANCEL);
				}
				else
				{
					SelectionMenu.this.deleteOption = -1;

					SelectionMenu.this.initiateDeleteButton.setText("delete");
					SelectionMenu.this.initiateDeleteButton.setLinkedAction(InputAction.DELETE);
				}
			}
		};
		this.initiateDeleteButton.setShowBox(false);
		this.initiateDeleteButton.setUseEventColors(true);
		this.initiateDeleteButton.setLinkedAction(InputAction.DELETE);

		this.confirmDeleteButton = new Button("Confirm Delete", 0, 0, 0, 0, true)
		{
			@Override
			public void onClick()
			{
				SelectionMenu.this.onDelete(SelectionMenu.this.items[SelectionMenu.this.anyOptionHovered ? SelectionMenu.this.hoveredOption : SelectionMenu.this.selectedOption]);

				SelectionMenu.this.deleteOption = -1;
			}
		};
		this.confirmDeleteButton.setShowBox(false);
		this.confirmDeleteButton.setUseEventColors(true);
		this.confirmDeleteButton.setLinkedAction(InputAction.CONFIRM);

		this.backButton = new Button("Back", this.xOffset + (width - DisplayConstants.SELECTION_MENU_SCROLLBAR_WIDTH) / 2, (int) (yOffset + height + DisplayConstants.SELECTION_MENU_BOX_HEIGHT * 1.5f), 0, 0, true)
		{
			@Override
			public void onClick()
			{
				SelectionMenu.this.onBackButton();
			}
		};
		this.backButton.setShowBox(false);
		this.backButton.setUseEventColors(true);

		InputHandler.addInputProcessor(this);

		this.updateGUI(items);
	}

	private void initItems(SelectionMenuItem[] items)
	{
		if (this.items != null)
		{
			for (SelectionMenuItem item : this.items)
			{
				if (item.emitter != null)
				{
					item.emitter.remove();
				}
			}
		}

		this.items = new SelectionMenuItem[items.length + 1];
		this.items[items.length] = new SelectionMenuAddItem();

		for (int i = 0; i < this.items.length; i++)
		{
			if (i < items.length)
			{
				this.items[i] = items[i];
			}

			this.items[i].init(this.width - 50, DisplayConstants.SELECTION_MENU_BOX_HEIGHT);
			this.items[i].menu = this;
		}
	}

	public void refresh()
	{
		Level.fetchChapters();
		Level.fetchMaps();
		this.updateItems();
	}

	protected abstract void onBackButton();

	@Override
	public void update(int delta)
	{
		if (this.active)
		{
			this.selectedButton = -1;

			if (!this.addedListener)
			{
				this.addedListener = true;
			}

			this.movableScrollbar.setPosition(this.xOffset - DisplayConstants.SELECTION_MENU_SCROLLBAR_WIDTH + this.width, this.yOffset + this.barOffset);
			if (this.movableScrollbar.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
			{
				if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
				{
					this.movingMode = true;
				}

				this.barColor.a = 0.4f;

				CursorType.useCursor(CursorType.GRAB);
			}
			else if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT))
			{
				this.movingMode = false;
				this.barColor.a = 0.3f;
			}

			this.clampBarOffset();

			this.actualOffset = -(int) (this.barOffset / this.height * this.getTotalHeight(this.items));

			for (int i = 0; i < this.items.length; i++)
			{
				this.items[i].centerX = this.xOffset + 5 + this.width / 2 - DisplayConstants.SELECTION_MENU_SCROLLBAR_WIDTH / 2;

				if (i == 0)
				{
					this.items[0].centerY = this.yOffset + this.actualOffset + DisplayConstants.SELECTION_MENU_BOX_HEIGHT / 2;
				}
				else
				{
					this.items[i].centerY = this.items[i - 1].centerY + BETWEEN_ITEMS_OFFSET + this.items[i - 1].getHeight() / 2 + this.items[i].getHeight() / 2;
				}
			}

			for (int i = 0; i < this.items.length; i++)
			{
				if (this.items[i].getY() >= this.yOffset)
				{
					this.topOption = i;

					break;
				}
			}

			this.bottomOption = this.items.length - 1;

			for (int i = this.topOption; i < this.items.length; i++)
			{
				if (this.items[i].getY() + this.items[i].getHeight() > this.yOffset + this.height)
				{
					this.bottomOption = i - 1;

					break;
				}
			}

			int lastHoveredOption = this.hoveredOption;

			this.hoveredOption = -1;
			this.anyOptionHovered = false;

			if (this.items.length <= 0)
			{
				this.bottomOption = -1;
			}

			for (int i = 0; i < this.items.length; i++)
			{
				this.items[i].active = i >= this.topOption && i <= this.bottomOption;
			}

			int inputX = InputHandler.getCurrentInputX();
			int inputY = InputHandler.getCurrentInputY();

			for (int i = this.topOption; i <= this.bottomOption; i++)
			{
				if (i >= 0 && i < this.items.length)
				{
					if (this.items[i].contains(inputX, inputY) && this.isSelectable(this.items[i]))
					{
						if (!this.confirmDeleteButton.contains(inputX, inputY) && !this.initiateDeleteButton.contains(inputX, inputY) && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched())
						{
							MenuHandler.getSoundManager().play(SoundType.MENU_CLICK, false);

							this.selectedOption = i;

							if (lastHoveredOption != i)
							{
								this.deleteOption = -1;
							}

							if (this.lastClickedOption == i && this.timeSinceLastClicked < 500 && this.isSelectable(this.items[i]))
							{
								this.items[i].onSelect();
								this.onSelect(this.items[i]);
							}

							this.lastClickedOption = i;
							this.timeSinceLastClicked = 0;
						}
						else
						{
							this.hoveredOption = i;
							this.anyOptionHovered = true;
						}
					}

					this.items[i].update(delta, i == this.hoveredOption, i == this.selectedOption);
				}
			}

			if (InputHandler.isKeyJustPressed(InputAction.DELETE))
			{
				this.initiateDeleteButton.onClick();
			}

			if (InputHandler.isKeyJustPressed(InputAction.CONFIRM))
			{
				if (this.confirmDeleteButton.isActive())
				{
					this.confirmDeleteButton.onClick();
				}
				else if (this.selectedOption >= 0)
				{
					if (this.selectedOption < this.items.length && this.isSelectable(this.items[this.selectedOption]))
					{
						this.items[this.selectedOption].onSelect();
						this.onSelect(this.items[this.selectedOption]);
					}
					else
					{
						this.onAddButton();
					}
				}
			}

			if (lastHoveredOption != this.hoveredOption && this.hoveredOption != -1)
			{
				this.deleteOption = -1;
			}

			if (InputHandler.isKeyJustPressed(InputAction.MOVE_UP))
			{
				this.selectedOption--;

				if (this.selectedOption < 0)
				{
					this.selectedOption = this.items.length - 1;
				}

				this.deleteOption = -1;

				this.updateSelectionPosition();
			}

			if (InputHandler.isKeyJustPressed(InputAction.MOVE_DOWN))
			{
				this.selectedOption++;

				if (this.selectedOption >= this.items.length)
				{
					this.selectedOption = 0;
				}

				this.deleteOption = -1;

				this.updateSelectionPosition();
			}

			this.initiateDeleteButton.setActive(false);
			this.confirmDeleteButton.setActive(false);

			if (this.anyOptionHovered && this.items[this.hoveredOption].isDeletable() || !this.anyOptionHovered && this.selectedOption >= 0 && this.items[this.selectedOption].isDeletable())
			{
				int option = this.anyOptionHovered ? this.hoveredOption : this.selectedOption;

				if (this.items[option].active)
				{
					this.initiateDeleteButton.setPosition((int) (this.items[option].getX() + this.items[option].getWidth()) - 40, (int) (this.items[option].getY() + this.items[option].getHeight() / 2));
					this.initiateDeleteButton.setActive(true);

					if (this.deleteOption != -1)
					{
						this.confirmDeleteButton.setPosition((int) (this.items[option].getX() + this.items[option].getWidth()) - 120, (int) (this.items[option].getY() + this.items[option].getHeight() / 2));
						this.confirmDeleteButton.setActive(true);
					}
				}
			}

			this.timeSinceLastClicked += delta;

			this.backButton.update(delta);

			if (InputHandler.isKeyPressed(InputAction.BACK))
			{
				this.backButton.onClick();
			}

			if (this.hoveredOption < this.items.length)
			{
				if (this.deleteOption == -1)
				{
					this.initiateDeleteButton.setText("delete");
					this.initiateDeleteButton.setLinkedAction(InputAction.DELETE);
				}

				this.initiateDeleteButton.update(delta);
				this.confirmDeleteButton.update(delta);
			}

			if (this.movingMode || this.anyOptionHovered)
			{
				CursorType.useCursor(CursorType.GRAB);
			}
		}

		this.customUpdate(delta);
	}

	@Override
	public void render(float alphaFactor, Batch batch)
	{
		ShaderHandler.disableLighting();

		Renderer.useSpriteBatch();

		Renderer.setFont(Fonts.getDefaultFont());

		for (int i = this.topOption; i <= this.bottomOption; i++)
		{
			if (i >= 0 && i < this.items.length)
			{
				if (i == this.hoveredOption || i == this.selectedOption)
				{
					batch.setColor(Menu.SELECTION_COLOR);
				}
				else
				{
					batch.setColor(Menu.UNSELECTED_COLOR);
				}

				this.items[i].render(alphaFactor, this.items[i].getX(), this.items[i].getY(), this.items[i].getWidth(), this.items[i].getHeight(), i == this.selectedOption, i == this.hoveredOption, i == this.deleteOption, batch);
			}
		}

		if (this.showBar)
		{
			this.movableScrollbar.getEmitter().setAlphaFactor(alphaFactor);
			this.movableScrollbar.getWrapper().draw(batch);
		}

		this.backButton.render(alphaFactor, batch);

		if (this.initiateDeleteButton.isActive())
		{
			this.initiateDeleteButton.render(alphaFactor, batch);
		}

		if (this.confirmDeleteButton.isActive())
		{
			this.confirmDeleteButton.render(alphaFactor, batch);
		}

		ShaderHandler.enableLighting();

		batch.setColor(Menu.SELECTION_COLOR);

		this.customRender(batch);
	}

	private void updateSelectionPosition()
	{
		if (this.selectedOption < this.topOption || this.selectedOption > this.bottomOption)
		{
			float wantedY = this.selectedOption > this.bottomOption ? this.height - this.items[this.selectedOption].getHeight() : 0;

			this.actualOffset = (int) (-this.getTotalHeight(this.items, this.selectedOption + (this.selectedOption > this.bottomOption ? 1 : 0)) + wantedY);
		}

		this.barOffset = -this.actualOffset / this.getTotalHeight(this.items) * this.height;

		this.clampBarOffset();

		this.actualOffset = -(int) (this.barOffset / this.height * this.getTotalHeight(this.items));
	}

	private float getTotalHeight(SelectionMenuItem[] items)
	{
		return this.getTotalHeight(items, items.length);
	}

	private float getTotalHeight(SelectionMenuItem[] items, int length)
	{
		float totalHeight = 0.0f;

		for (int i = 0; i < length && i < items.length; i++)
		{
			totalHeight += items[i].getHeight();
		}

		return totalHeight + (Math.min(items.length, length) - 1) * BETWEEN_ITEMS_OFFSET;
	}

	private void updateGUI(SelectionMenuItem[] items)
	{
		this.initItems(items);

		for (int i = 0; i < items.length; i++)
		{
			this.items[i].centerX = this.xOffset + 5 + (this.width - DisplayConstants.SELECTION_MENU_SCROLLBAR_WIDTH) / 2;
			this.items[i].centerY = this.yOffset + i * DisplayConstants.SELECTION_MENU_BOX_HEIGHT + this.actualOffset + 5 + DisplayConstants.SELECTION_MENU_BOX_HEIGHT / 2;
		}

		float displayableOptions = 0.0f;
		float tempTotalHeight = 0.0f;

		for (int i = 0; i < items.length * 5; i++) // kind of ugly hack but it
													// works
		{
			tempTotalHeight += items[i % items.length].getHeight();

			if (tempTotalHeight > this.height)
			{
				break;
			}

			displayableOptions++;

			tempTotalHeight += BETWEEN_ITEMS_OFFSET;
		}

		if (displayableOptions > this.items.length)
		{
			displayableOptions = this.items.length;
		}

		this.barHeight = (int) (this.height * (displayableOptions / this.items.length));

		if (this.movableScrollbar != null)
		{
			this.movableScrollbar.remove();
		}

		this.movableScrollbar = new MenuEmitterHandler(this.xOffset + this.width - DisplayConstants.SELECTION_MENU_SCROLLBAR_WIDTH, this.yOffset, DisplayConstants.SELECTION_MENU_SCROLLBAR_WIDTH, this.barHeight, 0, EmitterType.SCROLLBAR)
		{
			@Override
			public boolean isActive()
			{
				return SelectionMenu.this.active;
			}
		};

		this.showBar = displayableOptions < this.items.length;

		if (this.selectedOption >= this.items.length)
		{
			this.selectedOption = -1;
		}
	}

	private void clampBarOffset()
	{
		if (this.barOffset + this.barHeight > this.height)
		{
			this.barOffset = this.height - this.barHeight;
		}
		else if (this.barOffset < 0)
		{
			this.barOffset = 0;
		}
	}

	@Override
	public boolean scrolled(int change)
	{
		// if (showBar)
		{
			this.barOffset += 10.0f * change;

			this.clampBarOffset();
		}

		return false;
	}

	@Override
	public boolean touchDragged(int newx, int newy, int pointer)
	{
		if (this.movingMode)
		{
			Vector2 pos = Renderer.unproject(newx, newy);

			newx = (int) pos.x;
			newy = (int) pos.y;

			this.barOffset += newy - InputHandler.getLastInputY();

			if (this.barOffset + this.barHeight > this.height)
			{
				this.barOffset = this.height - this.barHeight;
			}
			else if (this.barOffset < 0)
			{
				this.barOffset = 0;
			}
		}

		return false;
	}

	protected abstract SelectionMenuItem[] loadItems();

	public void updateItems()
	{
		this.updateGUI(this.loadItems());
	}

	protected boolean isSelectable(SelectionMenuItem item)
	{
		return item.isSelectable();
	}

	protected abstract void onSelect(SelectionMenuItem saveState);

	protected void onDelete(SelectionMenuItem item)
	{

	}

	protected abstract void onAddButton();

	public void customUpdate(int delta)
	{

	}

	public void customRender(Batch batch)
	{

	}

	@Override
	public void setActive(boolean active)
	{
		boolean beforeActive = this.active;

		this.active = active;

		if (this.active && !beforeActive)
		{
			this.show();
		}
	}

	protected void show()
	{

	}

	@Override
	public boolean isActive()
	{
		return this.active;
	}

	public int getSelectedOption()
	{
		return this.selectedOption;
	}

	public void setSelectedOption(int selectedOption)
	{
		this.selectedOption = selectedOption;
	}

	public int getSelectedButton()
	{
		return this.selectedButton;
	}

	public void setSelectedButton(int selectedButton)
	{
		this.selectedButton = selectedButton;
	}

	public boolean isWasLoaded()
	{
		return this.wasLoaded;
	}

	public int getTopOption()
	{
		return this.topOption;
	}

	public void setTopOption(int topOption)
	{
		this.topOption = topOption;
	}

	public int getBottomOption()
	{
		return this.bottomOption;
	}

	public void setBottomOption(int bottomOption)
	{
		this.bottomOption = bottomOption;
	}

	public int getHoveredOption()
	{
		return this.hoveredOption;
	}

	public void setHoveredOption(int hoveredOption)
	{
		this.hoveredOption = hoveredOption;
	}

	public int getHeight()
	{
		return this.height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getWidth()
	{
		return this.width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getxOffset()
	{
		return this.xOffset;
	}

	public void setxOffset(int xOffset)
	{
		this.xOffset = xOffset;
	}

	public int getyOffset()
	{
		return this.yOffset;
	}

	public void setyOffset(int yOffset)
	{
		this.yOffset = yOffset;
	}

	public float getBarOffset()
	{
		return this.barOffset;
	}

	public void setBarOffset(float barOffset)
	{
		this.barOffset = barOffset;
	}

	public int getActualOffset()
	{
		return this.actualOffset;
	}

	public void setActualOffset(int actualOffset)
	{
		this.actualOffset = actualOffset;
	}

	public int getBarHeight()
	{
		return this.barHeight;
	}

	public void setBarHeight(int barHeight)
	{
		this.barHeight = barHeight;
	}

	public boolean isAnyOptionHovered()
	{
		return this.anyOptionHovered;
	}

	public void setAnyOptionHovered(boolean anyOptionHovered)
	{
		this.anyOptionHovered = anyOptionHovered;
	}

	public boolean isMovingMode()
	{
		return this.movingMode;
	}

	public void setMovingMode(boolean movingMode)
	{
		this.movingMode = movingMode;
	}

	public Color getBackgroundColor()
	{
		return this.backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	public Color getFillColor()
	{
		return this.fillColor;
	}

	public void setFillColor(Color fillColor)
	{
		this.fillColor = fillColor;
	}

	public Color getInfoColor()
	{
		return this.infoColor;
	}

	public void setInfoColor(Color infoColor)
	{
		this.infoColor = infoColor;
	}

	public Color getNotActiveColor()
	{
		return this.notActiveColor;
	}

	public void setNotActiveColor(Color notActiveColor)
	{
		this.notActiveColor = notActiveColor;
	}

	public Color getActiveColor()
	{
		return this.selectionColor;
	}

	public void setActiveColor(Color activeColor)
	{
		this.selectionColor = activeColor;
	}

	public Color getHoverColor()
	{
		return this.hoverColor;
	}

	public void setHoverColor(Color hoverColor)
	{
		this.hoverColor = hoverColor;
	}

	public Color getBarColor()
	{
		return this.barColor;
	}

	public void setBarColor(Color barColor)
	{
		this.barColor = barColor;
	}

	public static class SelectionMenuItem
	{
		protected SelectionMenu menu;
		protected MenuEmitterHandler emitter;

		protected float centerX;
		protected float centerY;
		private float defaultWidth;
		private float defaultHeight;

		protected float scale = 1.0f;
		protected boolean active;
		private boolean dirtyEmitter;

		void init(float defaultWidth, float defaultHeight)
		{
			this.defaultWidth = defaultWidth;
			this.defaultHeight = defaultHeight;
			this.dirtyEmitter = true;
		}

		private void checkInitEmitter()
		{
			if (this.dirtyEmitter)
			{
				if (this.emitter != null)
				{
					this.emitter.remove();
				}

				this.emitter = new MenuEmitterHandler(0.0f, 0.0f, (int) this.defaultWidth, (int) this.defaultHeight, 0, EmitterType.SELECTION_BOX)
				{
					@Override
					public boolean isActive()
					{
						return SelectionMenuItem.this.active && SelectionMenuItem.this.menu.active;
					}
				};

				this.dirtyEmitter = false;

				this.onActuallyInitEmitter();
			}
		}

		protected void onActuallyInitEmitter()
		{

		}

		public void onSelect()
		{

		}

		public void update(float delta, boolean hovered, boolean selected)
		{
			this.checkInitEmitter();

			this.emitter.setPosition(this.centerX - this.emitter.getWidth() / 2, this.centerY - this.emitter.getHeight() / 2);
		}

		public void render(float alphaFactor, float x, float y, float width, float height, boolean selected, boolean hovered, boolean deleting, Batch batch)
		{
			this.checkInitEmitter();

			if (selected)
			{
				this.emitter.getWrapper().setAddTint(Color.GRAY);
			}
			else if (hovered)
			{
				this.emitter.getWrapper().setAddTint(Color.DARK_GRAY);
			}
			else
			{
				this.emitter.getWrapper().setAddTint(Color.BLACK);
			}

			if (deleting)
			{
				this.emitter.getWrapper().setAddTint(this.emitter.getWrapper().getAddTint().cpy().mul(1.0f, 0.0f, 0.0f, 1.0f));
			}

			this.emitter.getEmitter().setAlphaFactor(alphaFactor);
			this.emitter.getWrapper().draw(batch);

			// ShapeRenderer sr = Renderer.useShapeRenderer();
			//
			// if (selected)
			// {
			// sr.setColor(menu.selectionColor);
			// }
			// else if (hovered)
			// {
			// sr.setColor(menu.hoverColor);
			// }
			// else
			// {
			// sr.setColor(menu.fillColor);
			// }
			//
			// sr.set(ShapeType.Filled);
			// sr.rect(getX(), getY(), getWidth(), getHeight());
		}

		public boolean contains(float x, float y)
		{
			return x > this.centerX - this.defaultWidth * this.scale / 2 && x < this.centerX + this.defaultWidth * this.scale / 2 && y > this.centerY - this.defaultHeight * this.scale / 2 && y < this.centerY + this.defaultHeight * this.scale / 2;
		}

		public boolean isDeletable()
		{
			return true;
		}

		public boolean isSelectable()
		{
			return true;
		}

		public float getX()
		{
			return this.centerX - this.defaultWidth * this.scale / 2;
		}

		public float getY()
		{
			return this.centerY - this.defaultHeight * this.scale / 2;
		}

		public float getWidth()
		{
			return this.defaultWidth * this.scale;
		}

		public float getHeight()
		{
			return this.defaultHeight * this.scale;
		}
	}

	public class SelectionMenuAddItem extends SelectionMenuItem
	{
		private Button addNew;

		public SelectionMenuAddItem()
		{
			this.addNew = new Button("Add", (int) this.centerX, (int) this.centerY, 0, 0, true)
			{
				@Override
				public void onClick()
				{
					SelectionMenu.this.onAddButton();
				}
			};
			this.addNew.setAutoHoverControl(false);
		}

		@Override
		void init(float defaultWidth, float defaultHeight)
		{
			super.init(defaultWidth, defaultHeight);
		}

		@Override
		protected void onActuallyInitEmitter()
		{
			super.onActuallyInitEmitter();

			this.emitter.getEmitter().getStartAlpha().mul(0.33f);
		}

		@Override
		public void update(float delta, boolean hovered, boolean selected)
		{
			super.update(delta, hovered, selected);

			this.addNew.setPosition((int) this.centerX, (int) this.centerY);
			this.addNew.update((int) delta);

			this.addNew.setHovered(hovered);
		}

		@Override
		public void render(float alphaFactor, float x, float y, float width, float height, boolean selected, boolean hovered, boolean deleting, Batch batch)
		{
			super.render(alphaFactor, x, y, width, height, selected, hovered, deleting, batch);

			this.addNew.render(alphaFactor, batch);
		}

		@Override
		public void onSelect()
		{
			this.addNew.onClick();
		}

		@Override
		public boolean isSelectable()
		{
			return true;
		}

		@Override
		public boolean isDeletable()
		{
			return false;
		}
	}
}