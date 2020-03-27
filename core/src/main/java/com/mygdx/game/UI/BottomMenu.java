package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.Entities.Entity;

import Utility.Utility;

public class BottomMenu extends Window {
	private final String unknownHeroImageLocation = "sprites/gui/portraits/unknown.png";

	private Label hpLabel;
	private Label apLabel;
	private Label xpLabel;
	private Label levelLabel;
	private Label iniLabel;

	private Label hp;
	private Label ap;
	private Label xp;
	private Label levelVal;
	private Label iniVal;
	private Label heroNameLabel;

	private int heroLevel;
	private int heroHP;
	private int heroAP;
	private int heroXP;
	private int heroINI;

	private Image heroImage;
	private Entity linkedEntity;

	private float tileWidthPixel;
	private float tileHeightPixel;

	private static final int BOTTOM_MENU_HEIGHT_TILES = 3;
	private static final int HERO_PORTRAIT_WIDTH_TILES = 3;

	private static final int STATS_MENU_WIDTH_TILES = 17;
	private static final int STATS_MENU_ELEMENT_PADDING = 20;
	private static final int STATS_MENU_TOP_PADDING = 0;

	private static final int HP_LABEL_WIDTH = 50;

	private static final int TILE_TO_PIXEL_RATIO = 25;

	private static final int ALPHA = 90;

	private HorizontalGroup bottomMenuTable;
	private Window statsGroup;
	private Container<Table> statsGroupContainer;

	public BottomMenu(final Entity[] entities) {
		super("", Utility.getStatusUISkin());
		initVariables();
		linkUnitsToMenu(entities);
		initElementsForUI();
		addElementsToWindow();
	}

	private void initVariables() {
		tileWidthPixel = Gdx.graphics.getWidth() / (float) TILE_TO_PIXEL_RATIO;
		tileHeightPixel = Gdx.graphics.getHeight() / (float) TILE_TO_PIXEL_RATIO;
	}

	private void linkUnitsToMenu(final Entity[] entities) {
		for (final Entity entity : entities) {
			entity.setbottomMenu(this);
		}
	}

	private void initElementsForUI() {
		initMainContainer();
		changeHeroImage(unknownHeroImageLocation);
		initStatsMenu();
	}

	private void initMainContainer() {
		initBottomMenuTable();
		initWindow();
		applyAlphaFilter();
	}

	private void initBottomMenuTable() {
		bottomMenuTable = new HorizontalGroup();
		bottomMenuTable.setFillParent(true);
		bottomMenuTable.pad(0);
	}

	private void initWindow() {
		this.pad(0);
		setTransform(true);
		this.setPosition(0, 0);
	}

	private void applyAlphaFilter() {
		final Color newColor = getColor();
		newColor.a = ALPHA;
		final Color tableColor = bottomMenuTable.getColor();
		tableColor.a = ALPHA;
	}

	private void changeHeroImage(final String heroImageLink) {
		Utility.loadTextureAsset(heroImageLink);
		final TextureRegion tr = new TextureRegion(Utility.getTextureAsset(heroImageLink));
		final TextureRegionDrawable trd = new TextureRegionDrawable(tr);
		trd.setMinHeight(BOTTOM_MENU_HEIGHT_TILES * tileHeightPixel);
		trd.setMinWidth(HERO_PORTRAIT_WIDTH_TILES * tileWidthPixel);
		if (heroImage != null) {
			heroImage.setDrawable(trd);
		} else {
			heroImage = new Image(trd);
			heroImage.setAlign(Align.center);
		}
	}

	private void initStatsMenu() {
		final Skin statusUISkin = Utility.getStatusUISkin();

		statsGroup = new Window("", statusUISkin);

		heroNameLabel = new Label("", statusUISkin);
		hpLabel = new Label(" hp:", statusUISkin);
		hp = new Label("", statusUISkin);
		apLabel = new Label(" ap:", statusUISkin);
		ap = new Label("", statusUISkin);
		xpLabel = new Label(" xp:", statusUISkin);
		xp = new Label("", statusUISkin);
		levelLabel = new Label(" lv:", statusUISkin);
		levelVal = new Label("", statusUISkin);
		iniLabel = new Label(" ini:", statusUISkin);
		iniVal = new Label("", statusUISkin);

		final Color newStatsGroupColor = statsGroup.getColor();
		newStatsGroupColor.a = ALPHA;
	}

	private void addElementsToWindow() {
		addActor(bottomMenuTable);

		populateHeroImage();
		populateStatsGroup();
	}

	private void populateHeroImage() {
		bottomMenuTable.addActor(heroImage);
	}

	private void populateStatsGroup() {
		final float statsWidth = STATS_MENU_WIDTH_TILES * tileWidthPixel;
		final float statsHeight = BOTTOM_MENU_HEIGHT_TILES;

		statsGroup.setHeight(statsHeight);
		statsGroup.setWidth(statsWidth);
		statsGroup.align(Align.left);

		addLabelsToStatsGroup();

		statsGroupContainer = new Container<Table>(statsGroup);
		bottomMenuTable.addActor(statsGroupContainer.prefSize(statsWidth, statsHeight));
		statsGroup.setFillParent(true);
	}

	private void addLabelsToStatsGroup() {
		statsGroup.add(heroNameLabel).padTop(STATS_MENU_TOP_PADDING).align(Align.topLeft).colspan(3);
		statsGroup.row();

		statsGroup.add(hpLabel).align(Align.topLeft).expandX().width(HP_LABEL_WIDTH);
		statsGroup.add(hp).align(Align.topLeft).padRight(STATS_MENU_ELEMENT_PADDING).expandX();

		statsGroup.add(levelLabel).align(Align.left).expandX();
		statsGroup.add(levelVal).align(Align.left).padRight(STATS_MENU_ELEMENT_PADDING).expandX();

		statsGroup.add(apLabel).align(Align.left).expandX();
		statsGroup.add(ap).align(Align.left).padRight(STATS_MENU_ELEMENT_PADDING).expandX();

		statsGroup.add(iniLabel).align(Align.left).expandX();
		statsGroup.add(iniVal).align(Align.left).padRight(STATS_MENU_ELEMENT_PADDING).expandX();

		statsGroup.add(xpLabel).align(Align.left).expandX();
		statsGroup.add(xp).align(Align.left).padRight(STATS_MENU_ELEMENT_PADDING).expandX();
	}

	public void setHero(final Entity entity) {
		if (entity != null) {
			if (!entity.getName().equalsIgnoreCase(heroNameLabel.getText().toString())) {
				linkedEntity = entity;
				initiateHeroStats();
				populateElementsForUI(entity);
			}
		} else {
			resetStats();
		}
	}

	private void initiateHeroStats() {
		heroLevel = linkedEntity.getLevel();
		heroHP = linkedEntity.getHp();
		heroAP = linkedEntity.getAp();
		heroXP = linkedEntity.getXp();
		heroINI = linkedEntity.getBaseInitiative();
	}

	private void populateElementsForUI(final Entity entity) {
		heroNameLabel.setText(entity.getName());
		changeHeroImage(entity.getPortraitPath());
		updateLabels();
	}

	private void resetStats() {
		heroNameLabel.setText("");
		hp.setText("");
		ap.setText("");
		xp.setText("");
		levelVal.setText("");
		iniVal.setText("");
		changeHeroImage(unknownHeroImageLocation);
	}

	public void update() {
		updateStats();
		updateLabels();
		updateSize();
	}

	private void updateLabels() {
		hp.setText(String.valueOf(heroHP));
		ap.setText(String.valueOf(heroAP));
		xp.setText(String.valueOf(heroXP));
		levelVal.setText(String.valueOf(heroLevel));
		iniVal.setText(String.valueOf(heroINI));

		if (heroAP == 0) {
			ap.setColor(Color.RED);
		} else {
			ap.setColor(Color.WHITE);
		}
	}

	private void updateStats() {
		if (linkedEntity != null) {
			heroLevel = linkedEntity.getLevel();
			heroHP = linkedEntity.getHp();
			heroAP = linkedEntity.getAp();
			heroXP = linkedEntity.getXp();

			if (linkedEntity.getEntityactor().getIsHovering()) {
				setVisible(true);
			}
		}
	}

	private void updateSize() {
		tileWidthPixel = Gdx.graphics.getWidth() / (float) TILE_TO_PIXEL_RATIO;
		tileHeightPixel = Gdx.graphics.getHeight() / (float) TILE_TO_PIXEL_RATIO;
		updateMainTable();
		updateHeroImage();
		updateStatsMenu();
		updateContainers();
	}

	private void updateMainTable() {
		final float scaledWidth = Gdx.graphics.getWidth();
		final float scaledHeight = BOTTOM_MENU_HEIGHT_TILES * tileHeightPixel;
		setSize(scaledWidth, scaledHeight);
		bottomMenuTable.setSize(scaledWidth, scaledHeight);
	}

	private void updateHeroImage() {
		heroImage.getDrawable().setMinHeight(BOTTOM_MENU_HEIGHT_TILES * tileHeightPixel);
		heroImage.getDrawable().setMinWidth(HERO_PORTRAIT_WIDTH_TILES * tileWidthPixel);
	}

	private void updateStatsMenu() {
		final float statsWidth = Gdx.graphics.getWidth() - (HERO_PORTRAIT_WIDTH_TILES * tileWidthPixel);
		final float statsHeight = BOTTOM_MENU_HEIGHT_TILES * tileHeightPixel;

		statsGroup.setHeight(statsHeight);
		statsGroup.setWidth(statsWidth);
		final LabelStyle labelStyle = createLabelStyle();
		for (final Actor actor : statsGroup.getChildren()) {
			if (actor.getClass() == Label.class) {
				final Label label = (Label) actor;
				label.setStyle(labelStyle);
				label.setFontScale(Gdx.graphics.getWidth() * 0.00028f, Gdx.graphics.getHeight() * 0.00028f);
			}
		}
		statsGroup.setPosition(HERO_PORTRAIT_WIDTH_TILES * tileWidthPixel, 0);
	}

	private LabelStyle createLabelStyle() {
		final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/BLKCHCRY.ttf"));
		final FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 105;
		parameter.borderWidth = 1;
		parameter.color = Color.LIGHT_GRAY;
		parameter.shadowOffsetX = 1;
		parameter.shadowOffsetY = 1;
		final BitmapFont font = generator.generateFont(parameter);
		final LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = font;
		return labelStyle;
	}

	private void updateContainers() {
		setSize(Gdx.graphics.getWidth(), BOTTOM_MENU_HEIGHT_TILES * tileHeightPixel);
		statsGroupContainer.setPosition(HERO_PORTRAIT_WIDTH_TILES * tileWidthPixel, 0);
		statsGroupContainer.setSize(Gdx.graphics.getWidth() - (HERO_PORTRAIT_WIDTH_TILES * tileWidthPixel), BOTTOM_MENU_HEIGHT_TILES * tileHeightPixel);
		statsGroupContainer.fill().prefSize(Gdx.graphics.getWidth() - (HERO_PORTRAIT_WIDTH_TILES * tileWidthPixel), BOTTOM_MENU_HEIGHT_TILES * tileHeightPixel);
	}
}
