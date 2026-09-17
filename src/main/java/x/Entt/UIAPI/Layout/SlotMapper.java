package x.Entt.UIAPI.Layout;

public class SlotMapper {

    public static int toPhysical(int row, int column) {
        return row * 9 + column;
    }

    public static int physicalRow(int physicalSlot) {
        return physicalSlot / 9;
    }

    public static int physicalColumn(int physicalSlot) {
        return physicalSlot % 9;
    }
}