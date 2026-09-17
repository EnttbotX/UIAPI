package x.Entt.UIAPI.GUI;

import java.util.ArrayList;
import java.util.List;

import x.Entt.UIAPI.Layout.SlotMapper;

public class GuiLayout {

    private final int rows;
    private final int columns;
    private final char[][] grid;
    private final int physicalSize;

    public GuiLayout(int rows, int columns, char[][] grid) {
        this.rows = rows;
        this.columns = columns;
        this.grid = grid;
        this.physicalSize = rows * 9;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getPhysicalSize() {
        return physicalSize;
    }

    public char charAt(int row, int column) {
        return grid[row][column];
    }

    public boolean contains(char key) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (grid[row][column] == key) {
                    return true;
                }
            }
        }
        return false;
    }

    public int count(char key) {
        int count = 0;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (charAt(row, column) == key) {
                    count++;
                }
            }
        }

        return count;
    }

    public List<Integer> physicalSlots(char key) {
        List<Integer> slots = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (grid[row][column] == key) {
                    slots.add(SlotMapper.toPhysical(row, column));
                }
            }
        }

        return slots;
    }
}