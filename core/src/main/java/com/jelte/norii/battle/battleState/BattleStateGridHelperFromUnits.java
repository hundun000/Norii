package com.jelte.norii.battle.battleState;

import java.awt.Point;

import com.badlogic.gdx.utils.Array;
import com.jelte.norii.magic.Ability;

public class BattleStateGridHelperFromUnits {

	public Array<Point> getTargetPositionsInRangeAbility(Point casterPos, Ability ability, Array<Point> targetPositions) {
		Array<Point> results = new Array<>();
		for (Point targetPos : targetPositions) {
			if (isUnitInAbilityRange(casterPos, ability, targetPos)) {
				results.add(targetPos);
			}
		}
		return results;
	}

	private boolean isUnitInAbilityRange(Point casterPos, Ability ability, Point targetPos) {
		switch (ability.getLineOfSight()) {
		case LINE:
			return getLineOptions(casterPos, ability, targetPos);
		case CIRCLE:
			return getCircleOptions(casterPos, ability, targetPos);
		case CROSS:
			return getLineOptions(casterPos, ability, targetPos);
		case SQUARE:
			return getSquareOptions(casterPos, ability, targetPos);
		case DIAGONAL_RIGHT:
			return getDiagonalRightOptions(casterPos, ability, targetPos);
		case DIAGONAL_LEFT:
			return getDiagonalLeftOptions(casterPos, ability, targetPos);
		case SQUARE_BORDER:
			return getSquareBorderOptions(casterPos, ability, targetPos);
		case CIRCLE_BORDER:
			return getCircleBorderOptions(casterPos, ability, targetPos);
		default:
			return false;
		}
	}

	private boolean getLineOptions(Point casterPos, Ability ability, Point targetPos) {
		final int range = ability.getSpellData().getRange();
		final int areaOfEffectRange = ability.getSpellData().getAreaOfEffectRange();
		switch (ability.getAreaOfEffect()) {
		case CELL:
			return checkCross(casterPos, targetPos, range);
		case HORIZONTAL_LINE:
			return checkLineHorizontalLine(casterPos, targetPos, range, areaOfEffectRange) || checkLineHorizontalLine(casterPos, targetPos, areaOfEffectRange, range);
		case VERTICAL_LINE:
			return checkCross(casterPos, targetPos, range + areaOfEffectRange);
		case CIRCLE:
			return checkLineCircle(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE:
			return checkLineSquare(casterPos, targetPos, range, areaOfEffectRange);
		case CROSS:
			return checkLineHorizontalLine(casterPos, targetPos, range, areaOfEffectRange) || checkLineHorizontalLine(casterPos, targetPos, areaOfEffectRange, range) || checkCross(casterPos, targetPos, range + areaOfEffectRange);
		case DIAGONAL:
			return checkLineDiagonal(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE_BORDER:
			return checkLineSquareBorder(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE_BORDER:
			return checkLineCircleBorder(casterPos, targetPos, range, areaOfEffectRange);
		default:
			return false;
		}
	}

	private boolean checkLineCircleBorder(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		return ((deltaX + deltaY) <= (range + areaOfEffectRange)) && !((deltaX == 0) && (deltaY == areaOfEffectRange));
	}

	private boolean checkLineSquareBorder(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > max)) {
			return false;
		}

		final int limit = (areaOfEffectRange * 2) + 1;
		return !((deltaX > limit) && (deltaY > limit));
	}

	private boolean checkLineDiagonal(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > max)) {
			return false;
		}

		return !(((deltaX == deltaY) && (deltaX >= range)) || (((deltaX == 0) && (deltaY > range)) || ((deltaY == 0) && (deltaX > range))) || checkLShapesDiagonal(deltaX, deltaY, range, areaOfEffectRange)
				|| checkCorners(deltaX, deltaY, areaOfEffectRange));
	}

	private boolean checkCorners(int deltaX, int deltaY, int areaOfEffectRange) {
		return ((deltaX > areaOfEffectRange) && (deltaY > areaOfEffectRange));
	}

	private boolean checkLShapesDiagonal(int deltaX, int deltaY, int range, int areaOfEffectRange) {
		final int max = range + areaOfEffectRange;
		while (areaOfEffectRange > 1) {
			for (int i = 1; i < areaOfEffectRange; i++) {
				if (((deltaX == max) && (deltaY == i)) || ((deltaY == max) && (deltaX == i))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkLineSquare(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		return (Math.abs(casterPos.y - targetPos.y) <= (range + areaOfEffectRange)) && (Math.abs(casterPos.x - targetPos.x) <= (range + areaOfEffectRange));
	}

	private boolean checkLineCircle(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		return ((Math.abs(casterPos.y - targetPos.y) + (Math.abs(casterPos.x - targetPos.x))) <= (range + areaOfEffectRange));
	}

	private boolean checkLineHorizontalLine(Point casterPos, Point targetPos, final int range, final int areaOfEffectRange) {
		return (Math.abs(casterPos.y - targetPos.y) <= range) && (Math.abs(casterPos.x - targetPos.x) <= areaOfEffectRange);
	}

	private boolean checkCross(Point casterPos, Point targetPos, final int range) {
		return ((casterPos.x == targetPos.x) && (Math.abs(casterPos.y - targetPos.y) <= range)) || ((casterPos.y == targetPos.y) && (Math.abs(casterPos.x - targetPos.x) <= range));
	}

	private boolean getCircleOptions(Point casterPos, Ability ability, Point targetPos) {
		final int range = ability.getSpellData().getRange();
		final int areaOfEffectRange = ability.getSpellData().getAreaOfEffectRange();
		switch (ability.getAreaOfEffect()) {
		case CELL:
			return checkCircle(casterPos, targetPos, range);
		case HORIZONTAL_LINE:
			return checkCircleHorizontalLine(casterPos, targetPos, range, areaOfEffectRange);
		case VERTICAL_LINE:
			return checkCircleVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE:
			return checkCircle(casterPos, targetPos, range + areaOfEffectRange);
		case SQUARE:
			return checkCircleSquare(casterPos, targetPos, range, areaOfEffectRange);
		case CROSS:
			return checkCircleHorizontalLine(casterPos, targetPos, range, areaOfEffectRange) || checkCircleVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case DIAGONAL:
			return checkLineDiagonal(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE_BORDER:
			return checkCircleSquare(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE_BORDER:
			return checkCircle(casterPos, targetPos, range + areaOfEffectRange);
		default:
			return false;
		}
	}

	private boolean checkCircleSquare(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > max)) {
			return false;
		}

		return (deltaX + deltaY) <= (range + (2 * areaOfEffectRange));
	}

	private boolean checkCircleVerticalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > range) || (Math.abs(casterPos.y - targetPos.y) > max) || ((deltaY == 0) && (deltaX == range))) {
			return false;
		}

		return (deltaX + deltaY) <= max;
	}

	private boolean checkCircleHorizontalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > range) || ((deltaX == 0) && (deltaY == range))) {
			return false;
		}

		return (deltaX + deltaY) <= max;
	}

	private boolean checkCircle(Point casterPos, Point targetPos, int range) {
		return ((Math.abs(casterPos.y - targetPos.y) + (Math.abs(casterPos.x - targetPos.x))) <= range);
	}

	private boolean getSquareOptions(Point casterPos, Ability ability, Point targetPos) {
		final int range = ability.getSpellData().getRange();
		final int areaOfEffectRange = ability.getSpellData().getAreaOfEffectRange();
		switch (ability.getAreaOfEffect()) {
		case CELL:
			return checkSquare(casterPos, targetPos, range);
		case HORIZONTAL_LINE:
			return checkSquareHorizontalLine(casterPos, targetPos, range, areaOfEffectRange);
		case VERTICAL_LINE:
			return checkSquareVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE:
			return checkCircleSquare(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE:
			return checkSquare(casterPos, targetPos, range + areaOfEffectRange);
		case CROSS:
			return checkSquareHorizontalLine(casterPos, targetPos, range, areaOfEffectRange) || checkSquareVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case DIAGONAL:
			return checkSquare(casterPos, targetPos, range + areaOfEffectRange);
		case SQUARE_BORDER:
			return checkSquare(casterPos, targetPos, range + areaOfEffectRange);
		case CIRCLE_BORDER:
			return checkCircleSquare(casterPos, targetPos, range, areaOfEffectRange);
		default:
			return false;
		}
	}

	private boolean checkSquareVerticalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);

		return (deltaY <= (range + areaOfEffectRange)) && (deltaX <= range);
	}

	private boolean checkSquareHorizontalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);

		return (deltaX <= (range + areaOfEffectRange)) && (deltaY <= range);
	}

	private boolean checkSquare(Point casterPos, Point targetPos, int range) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);

		return (deltaX <= range) && (deltaY <= range);
	}

	private boolean getDiagonalRightOptions(Point casterPos, Ability ability, Point targetPos) {
		final int range = ability.getSpellData().getRange();
		final int areaOfEffectRange = ability.getSpellData().getAreaOfEffectRange();
		switch (ability.getAreaOfEffect()) {
		case CELL:
			return checkDiagonalRight(casterPos, targetPos, range);
		case HORIZONTAL_LINE:
			return checkDiagonalRightHorizontalLine(casterPos, targetPos, range, areaOfEffectRange);
		case VERTICAL_LINE:
			return checkDiagonalRightVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE:
			return checkDiagonalRightCircle(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE:
			return checkDiagonalRightSquare(casterPos, targetPos, range, areaOfEffectRange);
		case CROSS:
			return checkDiagonalRightHorizontalLine(casterPos, targetPos, range, areaOfEffectRange) || checkDiagonalRightVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case DIAGONAL:
			return checkDiagonalDiagonal(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE_BORDER:
			return checkDiagonalRightSquare(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE_BORDER:
			return checkDiagonalRightCircle(casterPos, targetPos, range, areaOfEffectRange);
		default:
			return false;
		}
	}

	private boolean checkDiagonalDiagonal(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > max)) {
			return false;
		}

		if (deltaX == deltaY) {
			return true;
		}
		return ((Math.abs(deltaX - deltaY) == 4) || (Math.abs(deltaX - deltaY) == 2));
	}

	private boolean checkDiagonalRightSquare(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > range)) {
			return false;
		}

		return Math.abs(deltaX - deltaY) <= (2 * areaOfEffectRange);
	}

	private boolean checkDiagonalRightCircle(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > max)) {
			return false;
		}

		return (Math.abs(deltaX - deltaY) < areaOfEffectRange) && ((deltaX + deltaY) < (areaOfEffectRange + (2 * range)));
	}

	private boolean checkDiagonalRightHorizontalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > range)) {
			return false;
		}

		if (deltaX == deltaY) {
			return false;
		}

		return Math.abs(deltaX - deltaY) < areaOfEffectRange;
	}

	private boolean checkDiagonalRightVerticalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > range) || (Math.abs(casterPos.y - targetPos.y) > max)) {
			return false;
		}

		if (deltaX == deltaY) {
			return false;
		}

		return Math.abs(deltaY - deltaX) < areaOfEffectRange;
	}

	private boolean checkDiagonalRight(Point casterPos, Point targetPos, int range) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		return ((deltaX == deltaY) && (deltaX <= range) && (deltaX != 0));
	}

	private boolean getDiagonalLeftOptions(Point casterPos, Ability ability, Point targetPos) {
		final int range = ability.getSpellData().getRange();
		final int areaOfEffectRange = ability.getSpellData().getAreaOfEffectRange();
		switch (ability.getAreaOfEffect()) {
		case CELL:
			return checkDiagonalLeft(casterPos, targetPos, range);
		case HORIZONTAL_LINE:
			return checkDiagonalLeftHorizontalLine(casterPos, targetPos, range, areaOfEffectRange);
		case VERTICAL_LINE:
			return checkDiagonalLeftVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE:
			return checkDiagonalLeftCircle(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE:
			return checkDiagonalLeftSquare(casterPos, targetPos, range, areaOfEffectRange);
		case CROSS:
			return checkDiagonalLeftHorizontalLine(casterPos, targetPos, range, areaOfEffectRange) || checkDiagonalLeftVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case DIAGONAL:
			return checkDiagonalLeftDiagonal(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE_BORDER:
			return checkDiagonalLeftSquare(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE_BORDER:
			return checkDiagonalLeftCircle(casterPos, targetPos, range, areaOfEffectRange);
		default:
			return false;
		}
	}

	private boolean checkDiagonalLeft(Point casterPos, Point targetPos, int range) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		return ((deltaX == (deltaY * -1)) && (deltaX <= range) && (deltaX != 0));
	}

	private boolean checkDiagonalLeftHorizontalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > range)) {
			return false;
		}

		if (deltaX == deltaY) {
			return false;
		}

		return Math.abs(deltaX + deltaY) < areaOfEffectRange;
	}

	private boolean checkDiagonalLeftVerticalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > range) || (Math.abs(casterPos.y - targetPos.y) > max)) {
			return false;
		}

		if (deltaX == deltaY) {
			return false;
		}

		return Math.abs(deltaY + deltaX) < areaOfEffectRange;
	}

	private boolean checkDiagonalLeftDiagonal(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > max)) {
			return false;
		}

		if (deltaX == (deltaY * -1)) {
			return true;
		}
		return ((Math.abs(deltaX - deltaY) == 4) || (Math.abs(deltaX - deltaY) == 2));
	}

	private boolean checkDiagonalLeftSquare(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > range)) {
			return false;
		}

		return Math.abs(deltaX + deltaY) <= (2 * areaOfEffectRange);
	}

	private boolean checkDiagonalLeftCircle(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = casterPos.x - targetPos.x;
		final int deltaY = casterPos.y - targetPos.y;
		final int max = range + areaOfEffectRange;
		if ((Math.abs(casterPos.x - targetPos.x) > max) || (Math.abs(casterPos.y - targetPos.y) > max)) {
			return false;
		}

		return (Math.abs(deltaX + deltaY) < areaOfEffectRange) && ((deltaX + deltaY) < (areaOfEffectRange + (2 * range)));
	}

	private boolean getSquareBorderOptions(Point casterPos, Ability ability, Point targetPos) {
		final int range = ability.getSpellData().getRange();
		final int areaOfEffectRange = ability.getSpellData().getAreaOfEffectRange();
		switch (ability.getAreaOfEffect()) {
		case CELL:
			return checkSquareBorder(casterPos, targetPos, range);
		case HORIZONTAL_LINE:
			return checkSquareBorderHorizontalLine(casterPos, targetPos, range, areaOfEffectRange);
		case VERTICAL_LINE:
			return checkSquareBorderVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE:
			return checkCircleSquare(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE:
			return checkSquare(casterPos, targetPos, range + areaOfEffectRange);
		case CROSS:
			return checkSquareBorderHorizontalLine(casterPos, targetPos, range, areaOfEffectRange) || checkSquareBorderVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case DIAGONAL:
			return checkSquare(casterPos, targetPos, range + areaOfEffectRange);
		case SQUARE_BORDER:
			return checkSquare(casterPos, targetPos, range + areaOfEffectRange);
		case CIRCLE_BORDER:
			return checkCircleSquare(casterPos, targetPos, range, areaOfEffectRange);
		default:
			return false;
		}
	}

	private boolean checkSquareBorderVerticalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((deltaX > range) || (deltaY > max)) {
			return false;
		}

		return ((deltaX == range) || (targetPos.y >= ((casterPos.y + range) - areaOfEffectRange)) || (targetPos.y <= (casterPos.y + range + areaOfEffectRange)));
	}

	private boolean checkSquareBorderHorizontalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((deltaX > max) || (deltaY > range)) {
			return false;
		}

		return ((deltaY == range) || (targetPos.x >= ((casterPos.x + range) - areaOfEffectRange)) || (targetPos.x <= (casterPos.x + range + areaOfEffectRange)));
	}

	private boolean checkSquareBorder(Point casterPos, Point targetPos, int range) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		return ((deltaX == range) || (deltaY == range));
	}

	private boolean getCircleBorderOptions(Point casterPos, Ability ability, Point targetPos) {
		final int range = ability.getSpellData().getRange();
		final int areaOfEffectRange = ability.getSpellData().getAreaOfEffectRange();
		switch (ability.getAreaOfEffect()) {
		case CELL:
			return checkCircleBorder(casterPos, targetPos, range);
		case HORIZONTAL_LINE:
			return checkCircleBorderHorizontalLine(casterPos, targetPos, range, areaOfEffectRange);
		case VERTICAL_LINE:
			return checkCircleBorderVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE:
			return checkCircle(casterPos, targetPos, range + areaOfEffectRange);
		case SQUARE:
			return checkCircleBorderSquare(casterPos, targetPos, range, areaOfEffectRange);
		case CROSS:
			return checkCircleBorderHorizontalLine(casterPos, targetPos, range, areaOfEffectRange) || checkCircleBorderVerticalLine(casterPos, targetPos, range, areaOfEffectRange);
		case DIAGONAL:
			return checkCircleBorderDiagonal(casterPos, targetPos, range, areaOfEffectRange);
		case SQUARE_BORDER:
			return checkCircleBorderSquare(casterPos, targetPos, range, areaOfEffectRange);
		case CIRCLE_BORDER:
			return checkCircle(casterPos, targetPos, range + areaOfEffectRange);
		default:
			return false;
		}
	}

	private boolean checkCircleBorderDiagonal(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((deltaX > max) || (deltaY > max)) {
			return false;
		}

		return ((deltaX + deltaY) % 2) == 0;
	}

	private boolean checkCircleBorderSquare(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((deltaX > max) || (deltaY > max)) {
			return false;
		}

		return (deltaX + deltaY) <= (range + (2 * areaOfEffectRange));
	}

	private boolean checkCircleBorder(Point casterPos, Point targetPos, int range) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		return ((deltaX + deltaY) == range);
	}

	private boolean checkCircleBorderVerticalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((deltaX > range) || (deltaY > max) || ((deltaY == 0) && (deltaX == range))) {
			return false;
		}

		return ((targetPos.y >= ((casterPos.y + range) - areaOfEffectRange)) || (targetPos.y <= ((casterPos.y + range + areaOfEffectRange) - deltaX)));
	}

	private boolean checkCircleBorderHorizontalLine(Point casterPos, Point targetPos, int range, int areaOfEffectRange) {
		final int deltaX = Math.abs(casterPos.x - targetPos.x);
		final int deltaY = Math.abs(casterPos.y - targetPos.y);
		final int max = range + areaOfEffectRange;
		if ((deltaX > max) || (deltaY > range) || ((deltaX == 0) && (deltaY == range))) {
			return false;
		}

		return ((targetPos.x >= ((casterPos.x + range) - areaOfEffectRange)) || (targetPos.x <= ((casterPos.x + range + areaOfEffectRange) - deltaY)));
	}
}
