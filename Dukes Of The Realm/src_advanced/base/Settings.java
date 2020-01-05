package base;

public class Settings {
	static final public int statusBarHeight = 100;

	static final public int nbMaxActiveDukes = 5;
	static final public int initialTreasure = 0;
	
	static final public int windowWidth = 1200;
	static final public int windowHeight = 800;

	static final public int gridCellsCountX = windowWidth;
	static final public int gridCellsCountY = (windowHeight - statusBarHeight);
	
	static final public int cellSize = 10;

	static final public int castleSize = 50; // In grid cells
	static final public int knightSize = 15; // In grid cells

	static final public int minimumCastleDistance = 200;

	static public final int nbMaxCastles = 10;
	static public final int nbMinCastles = 8;
	
	static public final int surround = 24;
	
	static public final int ostSize = 3;
	
	// Knight, onager and pikeman
	static public final int nbTroopTypes = 3;
	
	static public final int nbMinInitTroops = 4;
	static public final int nbMaxInitTroops = 8;

	static public final int knightProdCost = 500;
	static public final int knightProdTime = 5;
	static public final int knightHP = 3;
	static public final int knightDamage = 5;
	static public final int knightSpeed = 6;

	static public final int onagerProdCost = 1000;
	static public final int onagerProdTime = 50;
	static public final int onagerHP = 5;
	static public final int onagerDamage = 10;
	static public final int onagerSpeed = 1;

	static public final int pikemanProdCost = 100;
	static public final int pikemanProdTime = 5;
	static public final int pikemanHP = 1;
	static public final int pikemanDamage = 1;
	static public final int pikemanSpeed = 2;

	static public final int camelProdTime = 0;
	static public final int camelHP = 1;
	static public final int camelDamage = 0;
	static public final int camelSpeed = 1;
}