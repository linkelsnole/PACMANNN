package my.snole.pacmannn.model.pacman;

import javafx.geometry.Point2D;
import my.snole.pacmannn.core.PacManModel;
import my.snole.pacmannn.model.ghost.Ghost;

import java.util.*;

/**
 * Класс для управления ботами Pac-Man.
 */
public class BotPacMan extends PacMan {
    private PacManModel.Direction lastDirection; // последнее направление движения
    private static final double AVOID_GHOST_RADIUS = 3.0; // радиус избегания привидений

    public BotPacMan(Point2D location, Point2D velocity) {
        super(location, velocity);
        this.lastDirection = PacManModel.Direction.NONE; // инициализация последнего направления как NONE
    }

    /**
     * Метод движения бота Pac-Man.
     * @param grid игровая сетка
     * @param ghosts список привидений
     * @param bots список ботов Pac-Man
     */
    public void move(PacManModel.CellValue[][] grid, List<Ghost> ghosts, List<BotPacMan> bots) {
        Point2D closestFood = findClosestFood(location, grid); // Находим ближайшую еду

        if (isGhostNearby(location, ghosts, AVOID_GHOST_RADIUS, grid)) {
            // логика избегания привидений
            moveAwayFromGhosts(grid, ghosts, bots);
        } else if (closestFood != null) {
            // логика для поиска пути к еде с избеганием привидений
            moveTowardsFoodWithAStar(grid, closestFood, ghosts, bots);
        } else {
            // случайное движение в направлении еды, если нет ближайшей еды или путь заблокирован
            moveRandomly(grid, ghosts, bots);
        }

        this.location = setGoingOffscreenNewLocation(this.location, grid.length, grid[0].length); // обновляем местоположение
    }

    /**
     * Проверка наличия привидения поблизости.
     * @param location текущая позиция
     * @param ghosts список привидений
     * @param radius радиус проверки
     * @return true, если привидение находится в радиусе, иначе false
     */
    private boolean isGhostNearby(Point2D location, List<Ghost> ghosts, double radius, PacManModel.CellValue[][] grid) {
        for (Ghost ghost : ghosts) {
            if (location.distance(ghost.getLocation()) <= radius && hasLineOfSight(location, ghost.getLocation(), grid)) {
                return true; // если привидение находится в радиусе и нет стены между ними
            }
        }
        return false; // если привидений в радиусе нет или они за стеной
    }

    /**
     * Движение бота в противоположную сторону от привидений.
     * @param grid игровая сетка
     * @param ghosts список привидений
     * @param bots список ботов Pac-Man
     */
    private void moveAwayFromGhosts(PacManModel.CellValue[][] grid, List<Ghost> ghosts, List<BotPacMan> bots) {
        Point2D oppositeDirection = calculateOppositeDirection(ghosts); //  противоположное направление

        List<Point2D> possibleDirections = Arrays.asList(
                new Point2D(Math.signum(oppositeDirection.getX()), 0),
                new Point2D(0, Math.signum(oppositeDirection.getY())),
                new Point2D(Math.signum(oppositeDirection.getX()), Math.signum(oppositeDirection.getY())),
                new Point2D(-Math.signum(oppositeDirection.getX()), 0),
                new Point2D(0, -Math.signum(oppositeDirection.getY()))
        );

        // перебираем возможные направления, чтобы найти доступный путь
        for (Point2D direction : possibleDirections) {
            Point2D newLocation = location.add(direction);
            if (isValidMove(newLocation, grid, bots) && !isGhostNearby(newLocation, ghosts, 1.0, grid)) {
                moveInDirection(grid, direction, ghosts, bots);
                return;
            }
        }

        // если нет допустимых направлений, ищем случайное направление
        moveRandomly(grid, ghosts, bots);
    }

    /**
     * Вычисление противоположного направления от привидений.
     * @param ghosts список привидений
     * @return направление в виде Point2D
     */
    private Point2D calculateOppositeDirection(List<Ghost> ghosts) {
        Point2D oppositeDirection = new Point2D(0, 0); // инициализация начального значения
        double minDistance = Double.MAX_VALUE;

        for (Ghost ghost : ghosts) {
            double distance = location.distance(ghost.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                oppositeDirection = location.subtract(ghost.getLocation()); // Вычисляем противоположное направление
            }
        }

        // Нормализуем направление
        return new Point2D(
                oppositeDirection.getX() / minDistance,
                oppositeDirection.getY() / minDistance
        );
    }

    /**
     * Движение в указанном направлении.
     * @param grid игровая сетка
     * @param direction направление движения
     * @param ghosts список привидений
     * @param bots список ботов Pac-Man
     */
    private void moveInDirection(PacManModel.CellValue[][] grid, Point2D direction, List<Ghost> ghosts, List<BotPacMan> bots) {
        Point2D potentialLocation = location.add(direction);
        potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);

        if (isValidMove(potentialLocation, grid, bots) && !isGhostNearby(potentialLocation, ghosts, 1.0, grid)) {
            this.velocity = direction;
            this.location = potentialLocation;
            updateLastDirection();
        } else {
            // Перебираем возможные направления, чтобы найти доступный путь
            List<Point2D> alternativeDirections = Arrays.asList(
                    new Point2D(-1, 0), new Point2D(1, 0),
                    new Point2D(0, -1), new Point2D(0, 1)
            );
            Collections.shuffle(alternativeDirections);
            for (Point2D dir : alternativeDirections) {
                potentialLocation = location.add(dir);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);
                if (isValidMove(potentialLocation, grid, bots) && !isGhostNearby(potentialLocation, ghosts, 1.0, grid)) {
                    this.velocity = dir;
                    this.location = potentialLocation;
                    updateLastDirection();
                    break;
                }
            }
        }
    }

    /**
     * Проверка, является ли движение в указанное местоположение допустимым.
     * @param location новое местоположение
     * @param grid игровая сетка
     * @param bots список ботов Pac-Man
     * @return true, если движение допустимо, иначе false
     */
    private boolean isValidMove(Point2D location, PacManModel.CellValue[][] grid, List<BotPacMan> bots) {
        int x = (int) location.getX();
        int y = (int) location.getY();
        if (x < 0 || x >= grid.length || y < 0 || y >= grid[0].length || grid[x][y] == PacManModel.CellValue.WALL) {
            return false;
        }

        // Избегаем других ботов
        for (BotPacMan bot : bots) {
            if (bot != this && bot.getLocation().equals(location)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Движение к еде с использованием алгоритма A*.
     * @param grid игровая сетка
     * @param closestFood ближайшая еда
     * @param ghosts список привидений
     * @param bots список ботов Pac-Man
     */
    private void moveTowardsFoodWithAStar(PacManModel.CellValue[][] grid, Point2D closestFood, List<Ghost> ghosts, List<BotPacMan> bots) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
        Map<Point2D, Node> allNodes = new HashMap<>();

        Node startNode = new Node(location, null, 0, heuristic(location, closestFood));
        openSet.add(startNode);
        allNodes.put(location, startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();

            if (currentNode.getLocation().equals(closestFood)) {
                reconstructPath(currentNode); // Реконструируем путь к еде
                return;
            }

            for (Point2D direction : new Point2D[]{new Point2D(-1, 0), new Point2D(1, 0), new Point2D(0, -1), new Point2D(0, 1)}) {
                Point2D neighborLocation = currentNode.getLocation().add(direction);
                neighborLocation = setGoingOffscreenNewLocation(neighborLocation, grid.length, grid[0].length);

                if (isValidMove(neighborLocation, grid, bots) && !isGhostNearby(neighborLocation, ghosts, 1.0, grid)) {
                    double tentativeG = currentNode.getG() + 1;
                    Node neighborNode = allNodes.getOrDefault(neighborLocation, new Node(neighborLocation));
                    if (tentativeG < neighborNode.getG()) {
                        neighborNode.setPrevious(currentNode);
                        neighborNode.setG(tentativeG);
                        neighborNode.setF(tentativeG + heuristic(neighborLocation, closestFood));
                        openSet.add(neighborNode);
                        allNodes.put(neighborLocation, neighborNode);
                    }
                }
            }
        }

        moveRandomly(grid, ghosts, bots);
    }

    /**
     * Эвристическая функция для алгоритма A*.
     * @param a начальная точка
     * @param b конечная точка
     * @return эвристическое значение
     */
    private double heuristic(Point2D a, Point2D b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()); // манхэттенское расстояние
    }

    /**
     * Реконструкция пути от начальной точки до целевой.
     * @param node конечная точка
     */
    private void reconstructPath(Node node) {
        List<Point2D> path = new ArrayList<>();
        while (node.getPrevious() != null) {
            path.add(node.getLocation());
            node = node.getPrevious();
        }
        Collections.reverse(path);

        if (!path.isEmpty()) {
            Point2D nextStep = path.get(0);
            this.velocity = nextStep.subtract(location);
            this.location = nextStep;
            updateLastDirection();
        }
    }

    /**
     * Поиск ближайшей еды.
     * @param start начальная точка
     * @param grid игровая сетка
     * @return местоположение ближайшей еды
     */
    private Point2D findClosestFood(Point2D start, PacManModel.CellValue[][] grid) {
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        Queue<Point2D> queue = new LinkedList<>();
        queue.add(start);
        visited[(int) start.getX()][(int) start.getY()] = true;

        while (!queue.isEmpty()) {
            Point2D current = queue.poll();
            for (Point2D dir : new Point2D[]{new Point2D(-1, 0), new Point2D(1, 0), new Point2D(0, -1), new Point2D(0, 1)}) {
                Point2D neighbor = current.add(dir);
                if (neighbor.getX() >= 0 && neighbor.getX() < grid.length && neighbor.getY() >= 0 && neighbor.getY() < grid[0].length) {
                    if (!visited[(int) neighbor.getX()][(int) neighbor.getY()]) {
                        visited[(int) neighbor.getX()][(int) neighbor.getY()] = true;
                        if (grid[(int) neighbor.getX()][(int) neighbor.getY()] == PacManModel.CellValue.SMALLDOT ||
                                grid[(int) neighbor.getX()][(int) neighbor.getY()] == PacManModel.CellValue.BIGDOT) {
                            return neighbor;
                        }
                        queue.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Случайное движение, если другие способы не подходят.
     * @param grid игровая сетка
     * @param ghosts список привидений
     * @param bots список ботов Pac-Man
     */
    private void moveRandomly(PacManModel.CellValue[][] grid, List<Ghost> ghosts, List<BotPacMan> bots) {
        List<Point2D> possibleDirections = Arrays.asList(
                new Point2D(-1, 0), new Point2D(1, 0),
                new Point2D(0, -1), new Point2D(0, 1)
        );
        Collections.shuffle(possibleDirections);
        for (Point2D direction : possibleDirections) {
            if (isValidMove(location.add(direction), grid, bots) && !isGhostNearby(location.add(direction), ghosts, 1.0, grid)) {
                moveInDirection(grid, direction, ghosts, bots); // Передаем ghosts
                return;
            }
        }

        // Если нет допустимых направлений, перемещаемся в любое направление
        for (Point2D direction : possibleDirections) {
            moveInDirection(grid, direction, ghosts, bots); // Передаем ghosts
        }
    }

    /**
     * Обновление последнего направления движения.
     */
    private void updateLastDirection() {
        if (velocity.getX() == 1) {
            this.lastDirection = PacManModel.Direction.DOWN;
        } else if (velocity.getX() == -1) {
            this.lastDirection = PacManModel.Direction.UP;
        } else if (velocity.getY() == 1) {
            this.lastDirection = PacManModel.Direction.RIGHT;
        } else if (velocity.getY() == -1) {
            this.lastDirection = PacManModel.Direction.LEFT;
        }
    }

    /**
     * Получение последнего направления движения.
     * @return последнее направление движения
     */
    public PacManModel.Direction getLastDirection() {
        return lastDirection;
    }

    @Override
    public Point2D getLocation() {
        return this.location;
    }

    /**
     * Проверка на наличие прямой видимости между двумя точками.
     * @param start начальная точка
     * @param end конечная точка
     * @param grid игровая сетка
     * @return true, если есть прямая видимость, иначе false
     */
    public boolean hasLineOfSight(Point2D start, Point2D end, PacManModel.CellValue[][] grid) {
        int x1 = (int) start.getX();
        int y1 = (int) start.getY();
        int x2 = (int) end.getX();
        int y2 = (int) end.getY();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (grid[x1][y1] == PacManModel.CellValue.WALL) {
                return false;
            }
            if (x1 == x2 && y1 == y2) {
                break;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
        return true;
    }

    /**
     * Внутренний класс для узлов алгоритма A*.
     */
    private class Node {
        private Point2D location; // местоположение узла
        private Node previous; // предыдущий узел
        private double g; // стоимость пути от начальной точки
        private double f; // общая стоимость (g + эвристическая стоимость)

        public Node(Point2D location) {
            this(location, null, Double.MAX_VALUE, Double.MAX_VALUE);
        }

        public Node(Point2D location, Node previous, double g, double f) {
            this.location = location;
            this.previous = previous;
            this.g = g;
            this.f = f;
        }

        public Point2D getLocation() {
            return location;
        }

        public Node getPrevious() {
            return previous;
        }

        public void setPrevious(Node previous) {
            this.previous = previous;
        }

        public double getG() {
            return g;
        }

        public void setG(double g) {
            this.g = g;
        }

        public double getF() {
            return f;
        }

        public void setF(double f) {
            this.f = f;
        }
    }
}
