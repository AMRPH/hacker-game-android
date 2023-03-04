package com.xlab13.playhacker.games.game2048;

import java.util.ArrayList;

public class Grid2048 {

    public final Tile2048[][] field;
    public final Tile2048[][] undoField;
    private final Tile2048[][] bufferField;

    public Grid2048(int sizeX, int sizeY) {
        field = new Tile2048[sizeX][sizeY];
        undoField = new Tile2048[sizeX][sizeY];
        bufferField = new Tile2048[sizeX][sizeY];
        clearGrid();
        clearUndoGrid();
    }

    public Cell2048 randomAvailableCell() {
        ArrayList<Cell2048> availableCells = getAvailableCells();
        if (availableCells.size() >= 1) {
            return availableCells.get((int) Math.floor(Math.random() * availableCells.size()));
        }
        return null;
    }

    private ArrayList<Cell2048> getAvailableCells() {
        ArrayList<Cell2048> availableCells = new ArrayList<>();
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] == null) {
                    availableCells.add(new Cell2048(xx, yy));
                }
            }
        }
        return availableCells;
    }

    public boolean isCellsAvailable() {
        return (getAvailableCells().size() >= 1);
    }

    public boolean isCellAvailable(Cell2048 cell) {
        return !isCellOccupied(cell);
    }

    public boolean isCellOccupied(Cell2048 cell) {
        return (getCellContent(cell) != null);
    }

    public Tile2048 getCellContent(Cell2048 cell) {
        if (cell != null && isCellWithinBounds(cell)) {
            return field[cell.getX()][cell.getY()];
        } else {
            return null;
        }
    }

    public Tile2048 getCellContent(int x, int y) {
        if (isCellWithinBounds(x, y)) {
            return field[x][y];
        } else {
            return null;
        }
    }

    public boolean isCellWithinBounds(Cell2048 cell) {
        return 0 <= cell.getX() && cell.getX() < field.length
                && 0 <= cell.getY() && cell.getY() < field[0].length;
    }

    private boolean isCellWithinBounds(int x, int y) {
        return 0 <= x && x < field.length
                && 0 <= y && y < field[0].length;
    }

    public void insertTile(Tile2048 tile) {
        field[tile.getX()][tile.getY()] = tile;
    }

    public void removeTile(Tile2048 tile) {
        field[tile.getX()][tile.getY()] = null;
    }

    public void saveTiles() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] == null) {
                    undoField[xx][yy] = null;
                } else {
                    undoField[xx][yy] = new Tile2048(xx, yy, field[xx][yy].getValue());
                }
            }
        }
    }

    public void clearGrid() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                field[xx][yy] = null;
            }
        }
    }

    private void clearUndoGrid() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                undoField[xx][yy] = null;
            }
        }
    }
}
