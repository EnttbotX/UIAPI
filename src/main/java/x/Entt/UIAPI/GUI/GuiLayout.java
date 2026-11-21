package x.Entt.UIAPI.GUI;

import x.Entt.UIAPI.Inventories.InvType;

import java.util.ArrayList;
import java.util.List;

public class GuiLayout {

    private final InvType type;
    private final int rows;
    private final int columns;
    private final char[][] grid;
    private final int physicalSize;
    private final char[] keyBySlot;

    public GuiLayout(InvType type, int rows, int columns, char[][] grid) {
        this.type = type;
        this.rows = rows;
        this.columns = columns;
        this.grid = grid;
        this.physicalSize = type.size(rows);
        this.keyBySlot = new char[physicalSize];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slot = type.physicalSlot(row, column);
                if (slot >= 0 && slot < physicalSize) {
                    keyBySlot[slot] = grid[row][column];
                }
            }
        }
    }

    public GuiLayout(int rows, int columns, char[][] grid) {
        this(InvType.CHEST, rows, columns, grid);
    }

    public static GuiLayout blank(InvType type) {
        int rows = type.maxRows();
        int columns = type.maxColumns();
        char[][] grid = new char[rows][columns];
        for (char[] line : grid) {
            java.util.Arrays.fill(line, ' ');
        }
        return new GuiLayout(type, rows, columns, grid);
    }

    public InvType getType() {
        return type;
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

    public int physicalSlot(int row, int column) {
        return type.physicalSlot(row, column);
    }

    public char keyAt(int physicalSlot) {
        if (physicalSlot < 0 || physicalSlot >= keyBySlot.length) {
            return 0;
        }
        return keyBySlot[physicalSlot];
    }

    public boolean contains(char key) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (grid[row][column] == key && physicalSlot(row, column) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public int count(char key) {
        return physicalSlots(key).size();
    }

    public List<Integer> physicalSlots(char key) {
        List<Integer> slots = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (grid[row][column] == key) {
                    int slot = physicalSlot(row, column);
                    if (slot >= 0) {
                        slots.add(slot);
                    }
                }
            }
        }

        return slots;
    }
}