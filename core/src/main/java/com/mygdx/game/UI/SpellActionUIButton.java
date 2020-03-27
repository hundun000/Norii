package com.mygdx.game.UI;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Magic.Ability;

public class SpellActionUIButton extends ActionUIButton {
	private final Ability ability;

	public SpellActionUIButton(final String imageFileName, final Entity linkedUnit, final Ability ability) {
		super(imageFileName);
		active = true;
		this.ability = ability;

		button.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				if (linkedUnit.canMove()) {
					linkedUnit.setInSpellPhase(true, ability);
				}
			}
		});
	}
}
