package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.mygdx.game.Screen.BattleScreen;

import Utility.Utility;

public abstract class UIWindow extends Window{
    protected float tileWidthPixel;
    protected float tileHeightPixel;
    
    private float windowWidth;
    private float windowHeight;
    
    private static final int TILE_TO_PIXEL_RATIO = 20;
    private static final float FONT_SCALE_FACTOR = 0.0015f;
    protected static final int ALPHA = 80;
    protected static final float BUTTON_WIDTH_FACTOR = 20;
    protected static final float BUTTON_HEIGHT_FACTOR = 20;
    
    protected UIWindow(String name,float width,float height) {
    	super(name,Utility.getStatusUISkin());
    	
    	initVariables(width,height);
    	setFadeBackgroundEffect();
    }
    
    protected void initVariables(float width,float height) {
    	tileWidthPixel = Gdx.graphics.getWidth() / (float) TILE_TO_PIXEL_RATIO;
    	tileHeightPixel = Gdx.graphics.getHeight() / (float) TILE_TO_PIXEL_RATIO;
    	this.windowWidth = width;
    	this.windowHeight = height;
    }
    
	private void setFadeBackgroundEffect() {
		Color newColor = this.getColor();
        newColor.a = ALPHA;
	}
	
    public void update() {
    	if(this.isVisible()) {
        	tileWidthPixel = Gdx.graphics.getWidth() / (float) TILE_TO_PIXEL_RATIO;
        	tileHeightPixel = Gdx.graphics.getHeight() / (float) TILE_TO_PIXEL_RATIO;
        	this.setSize(windowWidth * tileWidthPixel, windowHeight * tileHeightPixel);
        	updatePos();
        	updateSize();
    	}
    }
    
    protected void updateSize() {
    	for(Actor actor : this.getChildren()) {
    		if(actor.getClass() == Label.class) {
    			updateSizeLabels(actor);
    		}
    		
    		if(actor.getClass() == ImageButton.class) {
    			updateSizeImageButtons(actor);
    		}
    	}
    }
    
    private void updateSizeLabels(Actor actor) {
    	Label label = (Label) actor;
    	label.setFontScale(Gdx.graphics.getWidth() * FONT_SCALE_FACTOR, Gdx.graphics.getHeight() * FONT_SCALE_FACTOR);
    }
    
    private void updateSizeImageButtons(Actor actor) {
    	ImageButton button = (ImageButton) actor;
    	if(button.getImage() != null) {
    		this.getCell(actor).size(Gdx.graphics.getWidth() / BUTTON_WIDTH_FACTOR,Gdx.graphics.getHeight() / BUTTON_HEIGHT_FACTOR);
    	}
    }
    
    protected abstract void configureMainWindow();
    protected abstract void createWidgets();
    protected abstract void addWidgets();
    protected abstract void updatePos();
}
