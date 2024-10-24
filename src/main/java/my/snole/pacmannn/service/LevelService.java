package my.snole.pacmannn.service;

public class LevelService {
    private static final String[] levelFiles = {
            "level1.txt",
            "level2.txt",
            "level3.txt"
    };

    /**
     * Возвращает имя файла уровня по индексу.
     *
     * @param index индекс уровня
     * @return имя файла уровня
     */
    public static String getLevelFile(int index) {
        if (index >= 0 && index < levelFiles.length) {
            return levelFiles[index];
        }
        throw new IllegalArgumentException("Invalid level index: " + index);
    }

    /**
     * Возвращает общее количество уровней.
     *
     * @return количество уровней
     */
    public static int getTotalLevels() {
        return levelFiles.length;
    }
}

