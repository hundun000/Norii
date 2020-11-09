package com.jelte.norii.magic;

public class Ability {
	private final SpellData spellData;
	private final AbilitiesEnum abilityEnum;

	public enum LineOfSight {
		LINE, CIRCLE, CROSS
	}

	public enum AreaOfEffect {
		CELL, LINE, CIRCLE, CROSS, SQUARE
	}

	public enum AffectedTeams {
		FRIENDLY, ENEMY, BOTH
	}

	public enum Target {
		UNIT, CELL, BOTH
	}

	public Ability(final AbilitiesEnum abilityEnum) {
		this.abilityEnum = abilityEnum;
		spellData = SpellFileReader.getSpellData().get(abilityEnum.ordinal());
	}

	public SpellData getSpellData() {
		return spellData;
	}

	public String getName() {
		return spellData.getName();
	}

	public String getSpellInfo() {
		return spellData.getInfoText();
	}

	public int getId() {
		return spellData.getId();
	}

	public LineOfSight getLineOfSight() {
		return LineOfSight.valueOf(spellData.getLineOfSight());
	}

	public AreaOfEffect getAreaOfEffect() {
		return AreaOfEffect.valueOf(spellData.getAreaOfEffect());
	}

	public AffectedTeams getAffectedTeams() {
		return AffectedTeams.valueOf(spellData.getAffectsTeam());
	}

	public Target getTarget() {
		return Target.valueOf(spellData.getTarget());
	}

	public AbilitiesEnum getAbilityEnum() {
		return abilityEnum;
	}

	@Override
	public String toString() {
		return "ability name : " + spellData.getName();
	}

}