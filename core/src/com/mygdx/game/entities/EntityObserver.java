package com.mygdx.game.entities;

import com.mygdx.game.magic.Ability;

public interface EntityObserver {

	public enum EntityCommand {
		IN_MOVEMENT, IN_ATTACK_PHASE, IN_SPELL_PHASE, UNIT_ACTIVE, CLICKED, SKIP, AI_ACT, DIED, INIT_POSIBILITIES, AI_FINISHED_TURN, UNIT_LOCKED, FOCUS_CAMERA
	}

	void onEntityNotify(EntityCommand command, Entity unit);

	void onEntityNotify(EntityCommand command, AiEntity unit);

	void onEntityNotify(EntityCommand command, Entity unit, Ability ability);
}
