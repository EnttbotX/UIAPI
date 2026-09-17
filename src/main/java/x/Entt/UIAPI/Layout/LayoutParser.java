package x.Entt.UIAPI.Layout;

import x.Entt.UIAPI.GUI.GuiLayout;

import java.util.ArrayList;
import java.util.List;

public class LayoutParser {
    public static GuiLayout parse(String layout) {
        List<String> lines = new ArrayList<>();
        for (String line : layout.split("\n", -1)) {
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("UI layout cannot be empty.");
        }
        if (lines.size() > 6) {
            throw new IllegalArgumentException("UI layout cannot have more than 6 rows.");
        }
        int columns = lines.get(0).length();
        if (columns > 9) {
            throw new IllegalArgumentException("UI layout cannot have more than 9 columns.");
        }
        char[][] grid = new char[lines.size()][columns];
        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            if (line.length() != columns) {
                throw new IllegalArgumentException("UI layout row " + (row + 1) + " has " + line.length() + " columns, expected " + columns + ".");
            }
            grid[row] = line.toCharArray();
        }
        return new GuiLayout(lines.size(), columns, grid);
    }
}