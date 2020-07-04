package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Screen.BattleScreen;

import Utility.Utility;

public class StatusUI extends UIWindow {
	private int levelVal;
	private int hpVal;
	private int maxHpVal;
	private int apVal;
	private int maxApVal;
	private int xpVal;
	private int maxXpVal;
	private int iniVal;

	private WidgetGroup group;
	private WidgetGroup group2;
	private Image hpBarBackground;
	private Image hpBar;
	private Image xpBarBackground;
	private Image xpBar;

	private Label heroName;
	private Label hp;
	private Label ap;
	private Label xp;
	private Label levelValLabel;
	private Label iniValLabel;

	private LabelStyle labelStyle;
	private Label hpLabel;
	private Label apLabel;
	private Label xpLabel;
	private Label levelLabel;
	private Label iniLabel;

	private Entity linkedEntity;

	private float statsUIOffsetX;
	private float statsUIOffsetY;

	private static final float WIDTH_TILES = 6;
	private static final float HEIGHT_TILES = 8;
	private static final float BAR_WIDTH = 1.5f;
	private static final float BAR_HEIGHT = 0.3f;
	private static final float BAR_BOTTOM_PAD = 10f;

	public StatusUI(final Entity entity) {
		super("", WIDTH_TILES, HEIGHT_TILES);
		initVariables(entity);
		configureMainWindow();
		createWidgets();
		addWidgets();
	}

	@Override
	protected void configureMainWindow() {
		setVisible(false);
		setResizable(true);
	}

	private void initVariables(final Entity entity) {
		linkedEntity = entity;
		entity.setStatusui(this);
		initiateHeroStats();
		statsUIOffsetX = Gdx.graphics.getWidth() / (float) BattleScreen.VISIBLE_WIDTH;
		statsUIOffsetY = Gdx.graphics.getHeight() / (float) BattleScreen.VISIBLE_HEIGHT;
	}

	private void initiateHeroStats() {
		levelVal = linkedEntity.getEntityData().getLevel();
		hpVal = linkedEntity.getHp();
		maxHpVal = linkedEntity.getEntityData().getMaxHP();
		apVal = linkedEntity.getAp();
		maxApVal = linkedEntity.getEntityData().getMaxAP();
		xpVal = linkedEntity.getEntityData().getXp();
		maxXpVal = linkedEntity.getEntityData().getMaxXP();
		iniVal = linkedEntity.getEntityData().getBaseInitiative();
	}

	@Override
	protected void createWidgets() {
		createFont();
		createLabels();
		createDynamicHpBar();
		createDynamicXpBar();
		createGroups();
	}

	private void createFont() {
		final BitmapFont font = Utility.getFreeTypeFontAsset("fonts/twilight.ttf");
		labelStyle = new LabelStyle();
		labelStyle.font = font;
	}

	private void createLabels() {
		heroName = new Label(linkedEntity.getEntityData().getName(), labelStyle);
		hpLabel = new Label(" hp:", labelStyle);
		hp = new Label(String.valueOf(hpVal) + "/" + maxHpVal, labelStyle);
		apLabel = new Label(" ap:", labelStyle);
		ap = new Label(String.valueOf(apVal) + "/" + maxApVal, labelStyle);
		xpLabel = new Label(" xp:", labelStyle);
		xp = new Label(String.valueOf(xpVal) + "/" + maxXpVal, labelStyle);
		levelLabel = new Label(" lv:", labelStyle);
		levelValLabel = new Label(String.valueOf(levelVal), labelStyle);
		iniLabel = new Label(" ini:", labelStyle);
		iniValLabel = new Label(String.valueOf(iniVal), labelStyle);
	}

	private void createDynamicHpBar() {
		final TextureAtlas skinAtlas = Utility.getSkinTextureAtlas();
		final NinePatch hpBarBackgroundPatch = new NinePatch(skinAtlas.findRegion("progress-bar"), 5, 5, 4, 4);
		final NinePatch hpBarPatch = new NinePatch(skinAtlas.findRegion("progress-bar"), 5, 5, 4, 4);
		hpBar = new Image(hpBarPatch);
		hpBarBackground = new Image(hpBarBackgroundPatch);

		hpBar.setWidth(BAR_WIDTH * tileWidthPixel);
		hpBarBackground.setWidth(BAR_WIDTH * tileWidthPixel);
	}

	private void createDynamicXpBar() {
		final TextureAtlas skinAtlas = Utility.getSkinTextureAtlas();
		final NinePatch xpBarBackgroundPatch = new NinePatch(skinAtlas.findRegion("progress-bar"), 5, 5, 4, 4);
		final NinePatch xpBarPatch = new NinePatch(skinAtlas.findRegion("progress-bar"), 5, 5, 4, 4);
		xpBarPatch.setColor(Color.BLACK);
		xpBar = new Image(xpBarPatch);
		xpBarBackground = new Image(xpBarBackgroundPatch);
	}

	private void createGroups() {
		group = new WidgetGroup();
		group2 = new WidgetGroup();
		group.addActor(hpBarBackground);
		group.addActor(hpBar);
		group.setFillParent(true);
		group2.addActor(xpBar);
		group2.addActor(xpBarBackground);
		group2.setFillParent(true);

		defaults().expand().fill();
	}

	@Override
	protected void addWidgets() {
		this.add(heroName).colspan(3);
		row();

		this.add(hpLabel).align(Align.left).colspan(1).expandX();
		this.add(hp).align(Align.left).colspan(1).expandX();
		this.add(group).colspan(3).expandX().center().padBottom(BAR_BOTTOM_PAD);
		row();

		this.add(apLabel).align(Align.left).colspan(1).expandX();
		this.add(ap).align(Align.left).colspan(1).expandX();
		row();

		this.add(levelLabel).align(Align.left).colspan(1).expandX();
		this.add(levelValLabel).align(Align.left).colspan(1).expandX();
		row();

		this.add(iniLabel).align(Align.left).colspan(1).expandX();
		this.add(iniValLabel).align(Align.left).colspan(1).expandX();
		row();

		this.add(xpLabel).align(Align.left).colspan(1).expandX();
		this.add(xp).align(Align.left).colspan(1).expandX();
		this.add(group2).colspan(3).expandX().center().padBottom(BAR_BOTTOM_PAD);
		row();

		pack();
	}

	@Override
	public void update() {
		super.update();
		statsUIOffsetX = Gdx.graphics.getWidth() / (float) BattleScreen.VISIBLE_WIDTH;
		statsUIOffsetY = Gdx.graphics.getHeight() / (float) BattleScreen.VISIBLE_HEIGHT;

		updateStats();
		updateLabels();
		updateSizeElements();

		if (Boolean.TRUE.equals(linkedEntity.getEntityactor().getIsHovering())) {
			setVisible(true);
		}

		updatePos();
	}

	@Override
	protected void updatePos() {
		this.setPosition((linkedEntity.getCurrentPosition().getCameraX()) + statsUIOffsetX, (linkedEntity.getCurrentPosition().getCameraY()) + statsUIOffsetY);
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
		iniValLabel.setText(String.valueOf(iniVal));
	}

	private void updateSizeElements() {
		setSize(WIDTH_TILES * tileWidthPixel, HEIGHT_TILES * tileHeightPixel);
		final float barWidth = BAR_WIDTH * tileWidthPixel;
		final float barHeight = BAR_HEIGHT * tileHeightPixel;

		hpBar.setWidth(((float) linkedEntity.getHp() / (float) linkedEntity.getEntityData().getMaxHP()) * barWidth);
		hpBarBackground.setWidth(barWidth);

		hpBar.setHeight(barHeight);
		hpBarBackground.setHeight(barHeight);

		xpBar.setWidth(((float) linkedEntity.getEntityData().getXp() / (float) linkedEntity.getEntityData().getMaxXP()) * barWidth);
		xpBarBackground.setWidth(barWidth);

		xpBar.setHeight(barHeight);
		xpBarBackground.setHeight(barHeight);

		invalidate();
	}
}
