package com.mygdx.game.Map;

import java.util.ArrayList;
import java.util.List;

import org.xguzm.pathfinding.NavigationNode;
import org.xguzm.pathfinding.grid.GridCell;
import org.xguzm.pathfinding.grid.NavigationGridGraph;
import org.xguzm.pathfinding.grid.NavigationGridGraphNode;
import org.xguzm.pathfinding.grid.finders.AStarGridFinder;
import org.xguzm.pathfinding.grid.finders.GridFinderOptions;

import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Entities.EntityAnimation.Direction;

import Utility.TiledMapPosition;

public class MyPathFinder {
	private NavigationGridGraph<GridCell> navGrid;
	private GridFinderOptions opt;
	private AStarGridFinder<GridCell> finder;
	private final Map linkedMap;

	public MyPathFinder(final Map map) {
		linkedMap = map;
		navGrid = linkedMap.getNavLayer().navGrid;
		opt = new GridFinderOptions();
		opt.allowDiagonal = false;
		finder = new AStarGridFinder<GridCell>(GridCell.class, opt); // implement singleton
	}

	public List<GridCell> getLineOfSightWithinCircle(final int x, final int y, final int range, final ArrayList<TiledMapPosition> positions) {
		final List<GridCell> cells = new ArrayList<GridCell>();
		final GridCell center = navGrid.getCell(x, y);

		for (final GridCell[] gridcells : navGrid.getNodes()) {
			for (final GridCell gridcell : gridcells) {
				if (checkIfIsPathAndCloseEnough(center, gridcell, range) && lineOfSight(center, gridcell, positions)) {
					cells.add(gridcell);
				}
			}
		}
		return cells;
	}

	public List<GridCell> getLineOfSightWithinLine(final int x, final int y, final int range, final Direction direction, final ArrayList<TiledMapPosition> positions) {
		final List<GridCell> cells = new ArrayList<GridCell>();
		final GridCell center = navGrid.getCell(x, y);

		for (final GridCell[] gridcells : navGrid.getNodes()) {
			for (final GridCell gridcell : gridcells) {
				if (checkIfInLine(center, gridcell, range, direction) && lineOfSight(center, gridcell, positions)) {
					cells.add(gridcell);
				}
			}
		}
		return cells;
	}

	public List<GridCell> getCellsWithinCircle(final int x, final int y, final int range) {
		final List<GridCell> cells = new ArrayList<GridCell>();
		final GridCell center = navGrid.getCell(x, y);

		for (final GridCell[] gridcells : navGrid.getNodes()) {
			for (final GridCell gridcell : gridcells) {
				if (checkIfIsPathAndCloseEnough(center, gridcell, range)) {
					cells.add(gridcell);
				}
			}
		}
		return cells;
	}

	public List<GridCell> getCellsWithinLine(final int x, final int y, final int range, final Direction direction) {
		final List<GridCell> cells = new ArrayList<GridCell>();
		final GridCell center = navGrid.getCell(x, y);

		for (final GridCell[] gridcells : navGrid.getNodes()) {
			for (final GridCell gridcell : gridcells) {
				if (checkIfInLine(center, gridcell, range, direction)) {
					cells.add(gridcell);
				}
			}
		}
		return cells;
	}

	private boolean checkIfInLine(final GridCell center, final GridCell gridcell, final int range, final Direction direction) {
		if ((Math.abs(center.x - gridcell.x) + (Math.abs(center.y - gridcell.y))) <= range) {
			final List<GridCell> path = finder.findPath(center.x, center.y, gridcell.x, gridcell.y, navGrid);
			if ((path != null) && (path.size() <= range) && (!path.isEmpty())) {
				switch (direction) {
				case UP:
					return (center.x == gridcell.x) && (center.y <= gridcell.y);
				case DOWN:
					return (center.x == gridcell.x) && (center.y >= gridcell.y);
				case LEFT:
					return (center.x >= gridcell.x) && (center.y == gridcell.y);
				case RIGHT:
					return (center.x <= gridcell.x) && (center.y == gridcell.y);
				default:
					return false;
				}
			}
			return false;
		}
		return false;
	}

	private boolean checkIfIsPathAndCloseEnough(final GridCell center, final GridCell gridcell, final int range) {
		if ((Math.abs(center.x - gridcell.x) + (Math.abs(center.y - gridcell.y))) <= range) {
			final List<GridCell> path = finder.findPath(center.x, center.y, gridcell.x, gridcell.y, navGrid);
			if ((path != null) && (path.size() <= range) && (!path.isEmpty())) {
				return true;
			}
		}
		return false;
	}

	public boolean lineOfSight(final NavigationNode from, final NavigationNode to, final ArrayList<TiledMapPosition> positions) {
		if (from == null || to == null) {
			return false;
		}

		final NavigationGridGraphNode node = (NavigationGridGraphNode) from;
		final NavigationGridGraphNode neigh = (NavigationGridGraphNode) to;

		int x1 = node.getX(), y1 = node.getY();
		final int x2 = neigh.getX(), y2 = neigh.getY();
		final int dx = Math.abs(x1 - x2);
		final int dy = Math.abs(y1 - y2);
		final int xinc = (x1 < x2) ? 1 : -1;
		final int yinc = (y1 < y2) ? 1 : -1;

		int error = dx - dy;

		for (int n = dx + dy; n > 0; n--) {
			final int e2 = 2 * error;
			if (e2 > -dy) {
				error -= dy;
				x1 += xinc;
			}
			if (e2 < dx) {
				error += dx;
				y1 += yinc;
			}
			if (!navGrid.isWalkable(x1, y1) || isUnitOnCell(x1, y1, x2, y2, positions)) {
				return false;
			}
		}
		return true;
	}

	public boolean canUnitWalkTo(Entity unit, GridCell cell) {
		final GridCell center = navGrid.getCell(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY());
		return checkIfIsPathAndCloseEnough(center, cell, unit.getAp());
	}

	public boolean canUnitWalkTo(Entity unit, TiledMapPosition pos) {
		final GridCell center = navGrid.getCell(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY());
		final GridCell target = navGrid.getCell(pos.getTileX(), pos.getTileY());
		return checkIfIsPathAndCloseEnough(center, target, unit.getAp());
	}

	private boolean isUnitOnCell(final int x2, final int y2, final int targetX, final int targetY, final ArrayList<TiledMapPosition> positions) {
		for (final TiledMapPosition pos : positions) {
			if (pos.getTileX() == x2 && pos.getTileY() == y2 && !(pos.getTileX() == targetX && pos.getTileY() == targetY)) {
				return true;
			}
		}
		return false;
	}

	public AStarGridFinder<GridCell> getFinder() {
		return finder;
	}

	public void dispose() {
		finder = null;
		opt = null;
		navGrid = null;
	}
}
