package com.unlogical.colored.gui.menu;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.saving.SaveHandler;
import com.unlogical.colored.saving.SaveState;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.GameStatus;
import com.unlogical.colored.util.Renderer;

public class SaveSlotMenu extends SelectionMenu
{
	private SaveState previousSaveState;

	public SaveSlotMenu(int yOffset, int width, int height)
	{
		super(loadItems0(), yOffset, width, height);
	}

	protected static SelectionMenuItem[] loadItems0()
	{
		List<SaveState> saveStates = SaveHandler.fetchSaveStates();

		SaveSlotSelectionItem[] items = new SaveSlotSelectionItem[saveStates.size()];

		for (int i = 0; i < saveStates.size(); i++)
		{
			items[i] = new SaveSlotSelectionItem(saveStates.get(i));
		}

		return items;
	}

	@Override
	protected SelectionMenuItem[] loadItems()
	{
		return loadItems0();
	}

	@Override
	protected void onSelect(SelectionMenuItem item)
	{
		if (item instanceof SaveSlotSelectionItem)
		{
			SaveHandler.useSaveState(((SaveSlotSelectionItem) item).saveState);
		}
	}

	@Override
	protected void show()
	{
		this.previousSaveState = SaveHandler.getActiveSave();
	}

	@Override
	protected void onAddButton()
	{
		SaveHandler.createSave();

		this.updateItems();
	}

	@Override
	protected void onBackButton()
	{
		MenuHandler.transitionTo(GameStatus.MAIN_MENU, true);

		if (this.previousSaveState != SaveHandler.getActiveSave())
		{
			GameLauncher.goToLastVisitedLevel();
		}
	}

	@Override
	protected void onDelete(SelectionMenuItem item)
	{
		if (item instanceof SaveSlotSelectionItem)
		{
			SaveHandler.removeSaveState(((SaveSlotSelectionItem) item).saveState);

			this.updateItems();
		}
	}

	private static class SaveSlotSelectionItem extends SelectionMenuItem
	{
		private SaveState saveState;
		private String lastAccess;

		public SaveSlotSelectionItem(SaveState saveState)
		{
			this.saveState = saveState;

			this.lastAccess = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date(saveState.getLastAccess()));
		}

		@Override
		public void render(float alphaFactor, float x, float y, float width, float height, boolean selected, boolean hovered, boolean deleting, Batch batch)
		{
			Color color = ColorHelper.applyAlphaAndCopy(Color.WHITE, selected ? 1.0f : hovered ? 0.85f : 0.7f);
			color = ColorHelper.multiplyAlphaAndCopy(color, alphaFactor);

			if (this.saveState == SaveHandler.getActiveSave())
			{
				ShapeRenderer sr = Renderer.useShapeRenderer();

				sr.set(ShapeType.Line);
				sr.setColor(color);
				sr.rect(x, y, width, height);
			}

			super.render(alphaFactor, x, y, width, height, selected, hovered, deleting, batch);

			Renderer.useSpriteBatch();

			Renderer.setFont(Fonts.getBigGameFont());
			Renderer.drawString(this.saveState.getSaveID() + " - " + this.lastAccess, color, x, this.centerY - Renderer.getLineHeight() / 2, batch);
		}

		@Override
		public boolean isDeletable()
		{
			return this.saveState != SaveHandler.getActiveSave();
		}
	}
}
