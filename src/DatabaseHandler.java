
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private Connection connection;

    // Используем параметры для вашего проекта с отелем
    private static final String URL = "jdbc:postgresql://localhost:5432/hotel_management";  // Имя вашей базы данных
    private static final String USER = "postgres";  // Имя пользователя базы данных
    private static final String PASSWORD = "KOlovorot1";  // Пароль для подключения

    static {
        try {
            Class.forName("org.postgresql.Driver"); // Убедимся, что драйвер PostgreSQL загружен
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver Not Found!", e);
        }
    }

    // Конструктор, инициирующий подключение
    private DatabaseHandler() {
        connect();
    }

    // Метод для подключения к базе данных
    private void connect() {
        try {
            System.out.println("Attempting to connect to the database...");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to connect to the database!");
            e.printStackTrace();
        }
    }

    // Синглтон для получения единственного экземпляра DatabaseHandler
    public static synchronized DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    // Получение подключения
    public Connection getConnection() {
        try {
            // Проверка на закрытое соединение и попытка переподключиться
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to reconnect to the database!", e);
        }
        return connection;
    }
}
