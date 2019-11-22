package pl.toranktto.autoevents.maze;

import java.util.Arrays;
import java.util.Collections;

/*
 * http://weblog.jamisbuck.org/2010/12/27/maze-generation-recursive-backtracking
 */
public class MazeRandom {

    public Maze nextMaze(int x, int y) {
        Maze maze = new Maze(x, y);
        generate(maze);
        maze.removeWall(0, 0, Maze.Direction.UP);
        maze.removeWall(maze.getX() - 1, maze.getY() - 1, Maze.Direction.DOWN);

        return maze;
    }

    private void generate(Maze maze) {
        generate(maze, 0, 0);
    }

    private void generate(Maze maze, int x, int y) {
        Maze.Direction[] directions = Maze.Direction.values();
        Collections.shuffle(Arrays.asList(directions));

        for (Maze.Direction i : directions) {
            int nx = x + i.getX();
            int ny = y + i.getY();

            if (((nx >= 0) && (nx < maze.getX()))
                    && ((ny >= 0) && (ny < maze.getY()))
                    && maze.hasAllWalls(nx, ny)) {

                maze.removeWall(x, y, i);
                maze.removeWall(nx, ny, i.reverse());

                generate(maze, nx, ny);
            }
        }
    }
}
