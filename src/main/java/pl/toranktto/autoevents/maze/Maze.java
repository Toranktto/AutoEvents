package pl.toranktto.autoevents.maze;

public class Maze {

    private final int x;
    private final int y;
    private final byte[][] bitMaskMatrix;

    public Maze(int x, int y) {
        this.x = x;
        this.y = y;

        /*
         * 0 - cell has wall
         * 1 - cell hasn't wall
         */
        bitMaskMatrix = new byte[x][y];
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void removeWall(int x, int y, Direction direction) {
        bitMaskMatrix[x][y] |= (1 << direction.getBit());
    }

    public void setWall(int x, int y, Direction direction) {
        bitMaskMatrix[x][y] &= ~(1 << direction.getBit());
    }

    public boolean hasWall(int x, int y, Direction direction) {
        return (bitMaskMatrix[x][y] & (1 << direction.getBit())) == 0;
    }

    public boolean hasAllWalls(int x, int y) {
        return bitMaskMatrix[x][y] == 0 ||
                (hasWall(x, y, Direction.UP)
                        && hasWall(x, y, Direction.DOWN)
                        && hasWall(x, y, Direction.LEFT)
                        && hasWall(x, y, Direction.RIGHT));
    }

    public char[][] toCharMatrix(char wall, char cell) {
        int gridDimensionX = x * 2 + 1;
        int gridDimensionY = y * 2 + 1;
        char[][] grid = new char[gridDimensionX][gridDimensionY];

        for (int i = 0; i < gridDimensionY; i++) {
            for (int j = 0; j < gridDimensionX; j++) {
                grid[j][i] = wall;
            }
        }

        for (int i = 0; i < y; i++) {
            int cellGridY = i * 2 + 1;
            for (int j = 0; j < x; j++) {
                int cellGridX = j * 2 + 1;

                grid[cellGridX][cellGridY] = cell;

                for (Direction k : Direction.values()) {
                    int wallGridX = cellGridX + k.getX();
                    int wallGridY = cellGridY + k.getY();

                    if (!hasWall(j, i, k)) {
                        grid[wallGridX][wallGridY] = cell;
                    }
                }
            }
        }

        return grid;
    }

    public enum Direction {
        UP((byte) 7, 0, -1),
        DOWN((byte) 6, 0, 1),
        RIGHT((byte) 5, 1, 0),
        LEFT((byte) 4, -1, 0);

        private final byte bit;
        private final int x;
        private final int y;

        Direction(byte bit, int x, int y) {
            this.bit = bit;
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        private byte getBit() {
            return bit;
        }

        public Direction reverse() {
            switch (this) {
                case UP:
                    return DOWN;
                case DOWN:
                    return UP;
                case LEFT:
                    return RIGHT;
                case RIGHT:
                    return LEFT;
                default:
                    return this;
            }
        }
    }
}
