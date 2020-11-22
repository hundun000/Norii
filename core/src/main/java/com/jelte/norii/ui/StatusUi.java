package com.jelte.norii.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.jelte.norii.entities.Entity;
import com.jelte.norii.utility.AssetManagerUtility;

public class StatusUi extends Window {
	private int levelVal;
	private int hpVal;
	private int maxHpVal;
	private int apVal;
	private int maxApVal;
	private int xpVal;
	private int maxXpVal;

	private final float tilePixelWidth;
	private final float tilePixelHeight;

	private Label heroName;
	private Label hp;
	private Label ap;
	private Label xp;
	private Label levelValLabel;

	private Label hpLabel;
	private Label apLabel;
	private Label xpLabel;
	private Label levelLabel;

	private Entity linkedEntity;
	private boolean actionsUIIsHovering = false;

	private static final float WIDTH_TILES = 8;
	private static final float HEIGHT_TILES = 7;
	private static final float WINDOW_PADDING = 0;
	private static final int HERO_NAME_COLSPAN = 3;

	public StatusUi(final Entity entity, int mapWidth, int mapHeight) {
		super("", AssetManagerUtility.getSkin());

		tilePixelWidth = Hud.UI_VIEWPORT_WIDTH / mapWidth;
		tilePixelHeight = Hud.UI_VIEWPORT_HEIGHT / mapHeight;

		initVariables(entity);
		configureMainWindow();
		createWidgets();
		addWidgets();

		this.setSize(tilePixelWidth * WIDTH_TILES, tilePixelHeight * HEIGHT_TILES);
		this.pad(WINDOW_PADDING);
	}

	private void configureMainWindow() {
		setVisible(false);
		setResizable(true);
	}

	private void initVariables(final Entity entity) {
		linkedEntity = entity;
		entity.setStatusui(this);
		updateStats();
	}

	private void createWidgets() {
		createLabels();
		createGroups();
	}

	private void createLabels() {
		final Skin statusUISkin = AssetManagerUtility.getSkin();
		heroName = new Label(linkedEntity.getEntityData().getName(), statusUISkin, "smallFont");
		hpLabel = new Label(" hp:", statusUISkin, "smallFont");
		hp = new Label(String.valueOf(hpVal) + "/" + maxHpVal, statusUISkin, "smallFont");
		apLabel = new Label(" ap:", statusUISkin, "smallFont");
		ap = new Label(String.valueOf(apVal) + "/" + maxApVal, statusUISkin, "smallFont");
		xpLabel = new Label(" xp:", statusUISkin, "smallFont");
		xp = new Label(String.valueOf(xpVal) + "/" + maxXpVal, statusUISkin, "smallFont");
		levelLabel = new Label(" lv:", statusUISkin, "smallFont");
		levelValLabel = new Label(String.valueOf(levelVal), statusUISkin, "smallFont");
	}

	private void createGroups() {
		final WidgetGroup group = new WidgetGroup();
		final WidgetGroup group2 = new WidgetGroup();
		group.setFillParent(true);
		group2.setFillParent(true);

		defaults().expand().fill();
	}

	private void addWidgets() {
		this.add(heroName).colspan(HERO_NAME_COLSPAN);
		row();

		this.add(hpLabel).align(Align.left).expandX();
		this.add(hp).align(Align.left);
		row();

		this.add(apLabel).align(Align.left).expandX();
		this.add(ap).align(Align.left);
		row();

		this.add(levelLabel).align(Align.left).expandX();
		this.add(levelValLabel).align(Align.left);
		row();

		this.add(xpLabel).align(Align.left).expandX();
		this.add(xp).align(Align.left);
		row();

		pack();

	}

	public void update() {
		updateStats();
		updateLabels();

		final boolean isHovering = linkedEntity.getEntityactor().getIsHovering();

		if (Boolean.TRUE.equals(isHovering)) {
			setVisible(true);
		}

		if (actionsUIIsHovering) {
			setVisible(false);
		}

		updatePos();
	}

	private void updatePos() {
		this.setPosition((linkedEntity.getCurrentPosition().getTileX() * tilePixelWidth) + tilePixelWidth, ((linkedEntity.getCurrentPosition().getTileY() * tilePixelHeight) + tilePixelHeight));
	}

	private void updateStats() {
		levelVal = linkedEntity.getEntityData().getLevel();
		hpVal = linkedEntity.getHp();
		maxHpVal = linkedEntity.getEntityData().getMaxHP();
		apVal = linkedEntity.getAp();
		maxApVal = linkedEntity.getEntityData().getMaxAP();
		xpVal = linkedEntity.getEntityData().getXp();
		maxXpVal = linkedEntity.getEntityData().getMaxXP();
	}

	private void updateLabels() {
		hp.setText(String.valueOf(hpVal) + "/" + maxHpVal);
		ap.setText(String.valueOf(apVal) + "/" + maxApVal);
		xp.setText(String.valueOf(xpVal) + "/" + maxXpVal);
		levelValLabel.setText(String.valueOf(levelVal));
	}

	public void setActionsUIHovering(boolean actionsUIHovering) {
		this.actionsUIIsHovering = actionsUIHovering;
	}
}
