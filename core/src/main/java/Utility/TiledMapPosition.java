package Utility;

import java.awt.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mygdx.game.Map.Map;
import com.mygdx.game.Screen.BattleScreen;

public class TiledMapPosition {
	private Point tileCoordinates;
	
	
	public TiledMapPosition() {
		tileCoordinates = new Point(0,0);
	}
	
	public TiledMapPosition setPositionFromTiled(float x, float y){
		tileCoordinates = new Point(Math.round(x  / Map.TILE_WIDTH),Math.round(y / Map.TILE_HEIGHT));
		return this;
	}
	
	public TiledMapPosition setPositionFromTiles(int x, int y){
		tileCoordinates = new Point(x,y);
		return this;
	}
	
	public TiledMapPosition setPositionFromScreen(float x, float y){
		tileCoordinates = new Point(Math.round(x  / (Gdx.graphics.getWidth() / (float) BattleScreen.VISIBLE_WIDTH)),Math.round(y / (Gdx.graphics.getHeight() / (float) BattleScreen.VISIBLE_HEIGHT)));
		return this;
	}
	
	public boolean isTileEqualTo(TiledMapPosition pos) {
		return((pos.tileCoordinates.x == tileCoordinates.x) && (pos.tileCoordinates.y == tileCoordinates.y));
	}
	
	public float getCameraX() {
		OrthographicCamera cam = BattleScreen.getCamera();
		float xDifference = cam.position.x - (cam.viewportWidth / 2);
		return (tileCoordinates.x - xDifference) * (Gdx.graphics.getWidth() / (float) BattleScreen.VISIBLE_WIDTH);
	}
	
	public float getCameraY() {
		OrthographicCamera cam = BattleScreen.getCamera();
		float yDifference = cam.position.y - (cam.viewportHeight / 2);
		return (tileCoordinates.y - yDifference) * (Gdx.graphics.getHeight() / (float) BattleScreen.VISIBLE_HEIGHT);
	}
	
	public float getRealTiledX() {
		return (float) (tileCoordinates.x * Map.TILE_WIDTH);
	}
	
	public float getRealTiledY() {
		return (float) (tileCoordinates.y * Map.TILE_HEIGHT);
	}
	
	public int getTileX() {
		return tileCoordinates.x;
	}
	
	public int getTileY() {
		return tileCoordinates.y;
	}
}

