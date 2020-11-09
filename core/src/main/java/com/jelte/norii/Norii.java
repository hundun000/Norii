package com.jelte.norii;

import com.badlogic.gdx.Game;
import com.jelte.norii.screen.ScreenEnum;
import com.jelte.norii.screen.ScreenManager;

public class Norii extends Game {
	public static final String GAME_IDENTIFIER = "com.jelte.norii";

	@Override
	public void create() {
		ScreenManager.getInstance().initialize(this);
		ScreenManager.getInstance().showScreen(ScreenEnum.MAIN_MENU);
	}

	@Override
	public void dispose() {
		this.getScreen().dispose();
	}
}