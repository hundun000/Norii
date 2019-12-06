package com.mygdx.game.UI;

import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Map.Map;

import Utility.Utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class StatusUI extends Window {
	private static final String TAG = StatusUI.class.getSimpleName();
	
    private int levelVal;
    private int hpVal;
    private int mpVal;
    private int xpVal;
    private int iniVal;
	
    private Image hpBar;
    private Image xpBar;
    private Image bar;
    private Image bar3;
    private WidgetGroup group;
    private WidgetGroup group2;
    private WidgetGroup group3;
    
    private Image loadingBarBackground;
    private Image loadingBar;
    
    private Label hp;
    private Label mp;
    private Label xp;
    private Label levelValLabel;
    private Label iniValLabel;
    
    private Label hpLabel;
    private Label mpLabel;
    private Label xpLabel;
    private Label levelLabel;
    private Label iniLabel;

    private Entity linkedEntity;
    
	private int statsUIOffsetX = 32;
	private int statsUIOffsetY = 32;
	
	private static final int WIDTH_TILES = 7;
	private static final int HEIGHT_TILES = 7; 

    
    public StatusUI(Entity entity){
        super(entity.getName(), Utility.getStatusUISkin());
        this.setVisible(false); //have to set this false and true on hover
        this.linkedEntity = entity;
        this.setResizable(true);
        entity.setStatusui(this);
        
        initiateHeroStats();
        createElementsForUI();
        configureElements();
        addElementsToWindow();
    }
    
    private void initiateHeroStats() {
        levelVal = this.linkedEntity.getLevel();
        hpVal = this.linkedEntity.getHp();
        mpVal = this.linkedEntity.getMp();
        xpVal = this.linkedEntity.getXp();
        iniVal = this.linkedEntity.getBaseInitiative();
    }
    
    private void createElementsForUI() {
    	TextureAtlas statusUITextureAtlas = Utility.getStatusUITextureAtlas();
    	Skin statusUISkin = Utility.getStatusUISkin();
        
        group = new WidgetGroup();
        group2 = new WidgetGroup();
        group3 = new WidgetGroup();

        hpBar = new Image(statusUITextureAtlas.findRegion("HP_Bar"));
        bar = new Image(statusUITextureAtlas.findRegion("Bar"));
        xpBar = new Image(statusUITextureAtlas.findRegion("XP_Bar"));
        bar3 = new Image(statusUITextureAtlas.findRegion("Bar"));

        hpLabel = new Label(" hp:", statusUISkin);
        hp = new Label(String.valueOf(hpVal), statusUISkin);
        mpLabel = new Label(" mp:", statusUISkin);
        mp = new Label(String.valueOf(mpVal), statusUISkin);
        xpLabel = new Label(" xp:", statusUISkin);
        xp = new Label(String.valueOf(xpVal), statusUISkin);
        levelLabel = new Label(" lv:", statusUISkin);
        levelValLabel = new Label(String.valueOf(levelVal), statusUISkin);
        iniLabel = new Label(" ini:", statusUISkin);
        iniValLabel = new Label(String.valueOf(iniVal), statusUISkin);
        
        //dynamic hp bar
        TextureAtlas skinAtlas = new TextureAtlas(Gdx.files.internal("skins/uiskin.atlas"));
        NinePatch loadingBarBackgroundPatch = new NinePatch(skinAtlas.findRegion("default-round"), 5, 5, 4, 4);
        NinePatch loadingBarPatch = new NinePatch(skinAtlas.findRegion("default-round-down"), 5, 5, 4, 4);
        loadingBar = new Image(loadingBarPatch);
        loadingBarBackground = new Image(loadingBarBackgroundPatch);
    }
    
    private void configureElements() {
        hpBar.setPosition(3, 6);
        xpBar.setPosition(3, 6);
        loadingBar.setPosition(3, 6);
        loadingBar.setWidth((linkedEntity.getHp() / linkedEntity.getMaxHp()) * bar.getWidth());
        loadingBarBackground.setPosition(3, 6);
        loadingBarBackground.setWidth(bar.getWidth());

        group.addActor(bar);
        group.addActor(hpBar);
        group2.addActor(loadingBarBackground);
        group2.addActor(loadingBar);
        group2.addActor(bar);
        group3.addActor(bar3);
        group3.addActor(xpBar);

        defaults().expand().fill();
    }
    
    private void addElementsToWindow() {
        //account for the title padding
        this.pad(this.getPadTop() + 10, 10, 10, 10);

        this.add(hpLabel);
        this.add(hp);
        this.add(group2).size(bar.getWidth(), bar.getHeight());
        this.row();

        this.add(mpLabel);
        this.add(mp).align(Align.left);
        this.row();

        this.add(levelLabel).align(Align.left);
        this.add(levelValLabel).align(Align.left);
        this.row();
        
        this.add(iniLabel).align(Align.left);
        this.add(iniValLabel).align(Align.left);
        this.row();
        
        this.add(xpLabel);
        this.add(xp);
        this.add(group3).size(bar3.getWidth(), bar3.getHeight());
        this.row();

        this.pack();
    }
    
    public void update() {
        updateStats();
        updateLabels();
        updateSize();
        
        if(linkedEntity.getEntityactor().getIsHovering()) {
        	this.setVisible(true);
        }
        
        //we offset the position a little bit to make it look better
        this.setPosition((linkedEntity.getCurrentPosition().getRealScreenX()) + statsUIOffsetX, (linkedEntity.getCurrentPosition().getRealScreenY()) + statsUIOffsetY);

    }

	private void updateLabels() {
		hp.setText(String.valueOf(hpVal));
        mp.setText(String.valueOf(mpVal));
        xp.setText(String.valueOf(xpVal));
        levelValLabel.setText(String.valueOf(levelVal));
        iniValLabel.setText(String.valueOf(iniVal));
	}

	private void updateStats() {
		levelVal = linkedEntity.getLevel();
        hpVal = linkedEntity.getHp();
        mpVal = linkedEntity.getMp();
        xpVal = linkedEntity.getXp();
	}
	
	private void updateSize() {
		this.setSize(WIDTH_TILES * Map.TILE_WIDTH_PIXEL, HEIGHT_TILES * Map.TILE_HEIGHT_PIXEL);
		loadingBar.setWidth(((float)linkedEntity.getHp() / (float)linkedEntity.getMaxHp()) * bar.getWidth());
		loadingBarBackground.setWidth(bar.getWidth());
	}
}

