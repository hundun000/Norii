package com.jelte.norii.testUI;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.jelte.norii.entities.PlayerEntity;
import com.jelte.norii.magic.Ability;
import com.jelte.norii.ui.AttackActionUIButton;
import com.jelte.norii.ui.MoveActionUIButton;
import com.jelte.norii.ui.SkipActionUIButton;
import com.jelte.norii.ui.SpellActionUIButton;
import com.jelte.norii.utility.AssetManagerUtility;

public class ActionsUi extends Window {
	private static final int MOVE_AMOUNT_OUT_OF_BOUND_X = 32;
	private static final int MOVE_AMOUNT_OUT_OF_BOUND_Y = 32;
	private static final int HEIGHT_TILES = 2;
	private static final int WIDTH_TILES = 5;
	private static final int BUTTON_WIDTH = 16;
	private static final int BUTTON_HEIGHT = 16;
	private static final float ICON_PADDING = 2f;
	private static final float MAIN_WINDOW_PADDING = 0f;
	private static final String MOVE_BUTTON_SPRITE_NAME = "move";
	private static final String ATTACK_BUTTON_SPRITE_NAME = "attack";
	private static final String SKIP_BUTTON_SPRITE_NAME = "skip";

	private final int mapWidth;
	private final int mapHeight;

	private MoveActionUIButton moveActionUIButton;
	private AttackActionUIButton attackActionUIButton;
	private SkipActionUIButton skipActionUIButton;

	private ArrayList<ActionUIButton> buttons;
	private ArrayList<ActionInfoUiWindow> popUps;
	private PlayerEntity linkedEntity;

	public ActionsUi(final PlayerEntity entity, int mapWidth, int mapHeight) {
		super("", AssetManagerUtility.getSkin());
		this.mapHeight = mapHeight;
		this.mapWidth = mapWidth;
		configureMainWindow();
		initVariables(entity);
		createWidgets();
		addWidgets();
		initPopUps();
		this.setSize((NewHud.UI_VIEWPORT_WIDTH / mapWidth) * WIDTH_TILES, (NewHud.UI_VIEWPORT_HEIGHT / mapHeight) * HEIGHT_TILES);
		this.pad(MAIN_WINDOW_PADDING);
	}

	private void configureMainWindow() {
		setVisible(false);
		setKeepWithinStage(false);
	}

	private void initVariables(final PlayerEntity entity) {
		buttons = new ArrayList<>();
		linkedEntity = entity;
		entity.setActionsui(this);
	}

	private void createWidgets() {
		createButtons();
		storeButtons();
	}

	private void createButtons() {
		moveActionUIButton = new MoveActionUIButton(MOVE_BUTTON_SPRITE_NAME, linkedEntity, mapWidth, mapHeight);
		attackActionUIButton = new AttackActionUIButton(ATTACK_BUTTON_SPRITE_NAME, linkedEntity, mapWidth, mapHeight);
		skipActionUIButton = new SkipActionUIButton(this, SKIP_BUTTON_SPRITE_NAME, linkedEntity, mapWidth, mapHeight);
	}

	private void storeButtons() {
		buttons.add(moveActionUIButton);
		buttons.add(attackActionUIButton);
		buttons.add(skipActionUIButton);
	}

	private void initPopUps() {
		popUps = new ArrayList<>();
		for (final ActionUIButton button : buttons) {
			popUps.add(button.getPopUp());
		}
	}

	private void addWidgets() {
		addButtons();
		addSpells();
	}

	private void addButtons() {
		add(attackActionUIButton.getButton()).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(ICON_PADDING);
		add(moveActionUIButton.getButton()).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(ICON_PADDING);
		add(skipActionUIButton.getButton()).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(ICON_PADDING);
	}

	private void addSpells() {
		for (final Ability ability : linkedEntity.getAbilities()) {
			final SpellActionUIButton spellActionUIButton = new SpellActionUIButton(ability.getSpellData().getIconSpriteName(), linkedEntity, ability, mapWidth, mapHeight);
			buttons.add(spellActionUIButton);
			add(spellActionUIButton.getButton()).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(ICON_PADDING);
		}
	}

	public void update() {
		if (this.isVisible()) {
			updatePos();
		}
	}

	public void updatePos() {
		this.setPosition((linkedEntity.getCurrentPosition().getTileX() * (NewHud.UI_VIEWPORT_WIDTH / mapWidth)) + 16, ((linkedEntity.getCurrentPosition().getTileY() * (NewHud.UI_VIEWPORT_HEIGHT / mapHeight)) + 16));
		adjustPosition();
		adjustPopUps();
		setHovering();
	}

	private void adjustPopUps() {
		for (final ActionInfoUiWindow popUp : popUps) {
			popUp.setPosition(getX(), getY() + (BUTTON_HEIGHT * 2f));
		}
	}

	private void adjustPosition() {
		final Boolean right = getX() > NewHud.UI_VIEWPORT_WIDTH;
		final Boolean up = getY() > NewHud.UI_VIEWPORT_HEIGHT;
		final Boolean left = getX() < 0;
		final Boolean down = getY() < 0;

		if (Boolean.TRUE.equals(right)) {
			setX(getX() - (MOVE_AMOUNT_OUT_OF_BOUND_X));
		}

		if (Boolean.TRUE.equals(left)) {
			setX(getX() + (MOVE_AMOUNT_OUT_OF_BOUND_X));
		}

		if (Boolean.TRUE.equals(up)) {
			setY(getY() - MOVE_AMOUNT_OUT_OF_BOUND_Y);
		}

		if (Boolean.TRUE.equals(down)) {
			setY(getY() + MOVE_AMOUNT_OUT_OF_BOUND_Y);
		}
	}

	public List<ActionInfoUiWindow> getPopUps() {
		return popUps;
	}

	private void setHovering() {
		for (final ActionUIButton button : buttons) {
			if (button.isHovering() && button.entered) {
				linkedEntity.getEntityactor().setActionsHovering(true);
			}

			if (button.exited) {
				linkedEntity.getEntityactor().setActionsHovering(false);
				button.exited = false;
			}
		}
	}
}
