import java.util.*;

/*
 Maze Generator - Java Mini Project
 
 Generates a perfect maze (no loops, exactly one path between any two cells)
 on a 2D array using Depth-First Search (Recursive Backtracker).
 
 Two versions of the same algorithm are included:
  1. Iterative  -> uses an explicit Stack
  2. Recursive  -> uses the call stack
 
  Grid representation:
  A maze with R x C cells is stored in an int[2R+1][2C+1] array.
  Cells sit at odd (row, col) positions; even positions are walls
  that get "knocked down" to connect neighbouring cells.
  0 = path, 1 = wall, 2 = solution path
 */
public class MazeGenerator {

    static final int PATH = 0, WALL = 1, SOLUTION = 2;

    // Direction offsets: up, down, left, right
    static final int[] DR = {-1, 1, 0, 0};
    static final int[] DC = {0, 0, -1, 1};

    private final int height, width;
    private final int[][] grid;
    private final Random rand;

    public MazeGenerator(int rows, int cols, long seed) {
        this.height = 2 * rows + 1;
        this.width = 2 * cols + 1;
        this.grid = new int[height][width];
        this.rand = new Random(seed);
        reset();
    }

    /*Fill the whole grid with walls. */
    private void reset() {
        for (int[] row : grid) Arrays.fill(row, WALL);
    }

    /*True if (r, c) is an interior cell position inside the border. */
    private boolean inBounds(int r, int c) {
        return r > 0 && r < height - 1 && c > 0 && c < width - 1;
    }

    
    // 1) ITERATIVE generation using an explicit Stack
    
    public void generateWithStack() {
        reset();
        Deque<int[]> stack = new ArrayDeque<>();

        grid[1][1] = PATH;          // start cell
        stack.push(new int[]{1, 1});

        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            List<Integer> options = unvisitedNeighbours(cur[0], cur[1]);

            if (options.isEmpty()) {
                stack.pop();        // dead end -> backtrack
            } else {
                int d = options.get(rand.nextInt(options.size()));
                int wr = cur[0] + DR[d], wc = cur[1] + DC[d];          // wall between
                int nr = cur[0] + 2 * DR[d], nc = cur[1] + 2 * DC[d];  // next cell
                grid[wr][wc] = PATH;   // knock down wall
                grid[nr][nc] = PATH;   // visit neighbour
                stack.push(new int[]{nr, nc});
            }
        }
        addEntranceAndExit();
    }

    /* Directions whose cell two steps away is still unvisited (a wall). */
    private List<Integer> unvisitedNeighbours(int r, int c) {
        List<Integer> list = new ArrayList<>();
        for (int d = 0; d < 4; d++) {
            int nr = r + 2 * DR[d], nc = c + 2 * DC[d];
            if (inBounds(nr, nc) && grid[nr][nc] == WALL) list.add(d);
        }
        return list;
    }

    
    // 2) RECURSIVE generation (call stack does the backtracking)
    
    public void generateRecursive() {
        reset();
        carve(1, 1);
        addEntranceAndExit();
    }

    private void carve(int r, int c) {
        grid[r][c] = PATH;

        // Try the 4 directions in random order
        List<Integer> dirs = new ArrayList<>(List.of(0, 1, 2, 3));
        Collections.shuffle(dirs, rand);

        for (int d : dirs) {
            int nr = r + 2 * DR[d], nc = c + 2 * DC[d];
            if (inBounds(nr, nc) && grid[nr][nc] == WALL) {
                grid[r + DR[d]][c + DC[d]] = PATH;  // knock down wall
                carve(nr, nc);                       // recurse
            }
        }
        // returning from the method = backtracking
    }

   
    // Entrance (top-left) and exit (bottom-right)
   
    private void addEntranceAndExit() {
        grid[1][0] = PATH;
        grid[height - 2][width - 1] = PATH;
    }

    
    // Solver (recursive DFS with backtracking)
    
    public boolean solve() {
        clearSolution();
        boolean[][] visited = new boolean[height][width];
        return solveFrom(1, 0, visited);
    }

    private boolean solveFrom(int r, int c, boolean[][] visited) {
        if (r < 0 || r >= height || c < 0 || c >= width) return false;
        if (grid[r][c] == WALL || visited[r][c]) return false;

        visited[r][c] = true;
        grid[r][c] = SOLUTION;

        if (r == height - 2 && c == width - 1) return true;  // reached exit

        for (int d = 0; d < 4; d++) {
            if (solveFrom(r + DR[d], c + DC[d], visited)) return true;
        }

        grid[r][c] = PATH;   // backtrack: not part of the solution
        return false;
    }

    private void clearSolution() {
        for (int r = 0; r < height; r++)
            for (int c = 0; c < width; c++)
                if (grid[r][c] == SOLUTION) grid[r][c] = PATH;
    }

    
    // Display
    
    public void print() {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int cell : row) {
                switch (cell) {
                    case WALL:     sb.append("██"); break;
                    case SOLUTION: sb.append("• "); break;
                    default:       sb.append("  ");
                }
            }
            sb.append('\n');
        }
        System.out.println(sb);
    }

    
    // Main: simple console menu
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== MAZE GENERATOR ===");

        System.out.print("Rows (e.g. 10): ");
        int rows = Math.max(2, sc.nextInt());
        System.out.print("Columns (e.g. 20): ");
        int cols = Math.max(2, sc.nextInt());

        MazeGenerator maze = new MazeGenerator(rows, cols, System.nanoTime());

        int choice;
        do {
            System.out.println("\n1. Generate maze (Stack / iterative)");
            System.out.println("2. Generate maze (Recursion)");
            System.out.println("3. Solve current maze");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    maze.generateWithStack();
                    maze.print();
                    break;
                case 2:
                    try {
                        maze.generateRecursive();
                        maze.print();
                    } catch (StackOverflowError e) {
                        System.out.println("Maze too large for recursion. Use the Stack version.");
                    }
                    break;
                case 3:
                    System.out.println(maze.solve() ? "Solution found:" : "No solution!");
                    maze.print();
                    break;
                case 0:
                    System.out.println("Bye!");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 0);

        sc.close();
    }
}
