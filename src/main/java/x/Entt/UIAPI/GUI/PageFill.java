package x.Entt.UIAPI.GUI;

public enum PageFill {
    LEFT {
        @Override
        public int[] assign(int slotCount, int itemCount) {
            int[] positions = new int[itemCount];
            for (int i = 0; i < itemCount; i++) {
                positions[i] = i;
            }
            return positions;
        }
    },

    RIGHT {
        @Override
        public int[] assign(int slotCount, int itemCount) {
            int[] positions = new int[itemCount];
            int offset = slotCount - itemCount;
            for (int i = 0; i < itemCount; i++) {
                positions[i] = offset + i;
            }
            return positions;
        }
    },

    JUSTIFY {
        @Override
        public int[] assign(int slotCount, int itemCount) {
            int[] positions = new int[itemCount];
            if (itemCount == 0) {
                return positions;
            }
            if (itemCount >= slotCount) {
                for (int i = 0; i < itemCount; i++) {
                    positions[i] = Math.min(i, slotCount - 1);
                }
                return positions;
            }
            for (int i = 0; i < itemCount; i++) {
                positions[i] = (int) Math.floor((i + 0.5) * slotCount / itemCount);
            }
            return positions;
        }
    };

    public abstract int[] assign(int slotCount, int itemCount);
}