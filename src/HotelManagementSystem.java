import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;

public class HotelManagementSystem {
    // Предполагается, что DatabaseHandler реализован как синглтон с методом getInstance()
    private static DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    register();
                    break;
                case 3:
                    System.out.println("Exiting the system. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ----------------------- АВТОРИЗАЦИЯ -----------------------
    private static void login() {
        System.out.print("Enter your user ID: ");
        String input = scanner.nextLine().trim();
        int userId;
        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid user ID. Please enter a number.");
            return;
        }
        User user = getUserFromDatabase(userId);
        if (user == null) {
            System.out.println("User not found. Please register first.");
            return;
        }
        if ("Admin".equalsIgnoreCase(user.getRole())) {
            System.out.print("Enter admin password: ");
            String password = scanner.nextLine();
            // Пароль для входа администратора – "KOlovorot1"
            if ("admin123".equals(password)) {
                adminMenu();
            } else {
                System.out.println("Invalid admin password.");
            }
        } else {
            customerMenu(user);
        }
    }

    private static void register() {
        System.out.print("Enter user ID: ");
        String input = scanner.nextLine().trim();
        int userId;
        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid user ID. Please enter a number.");
            return;
        }

        // Проверка наличия пользователя с таким ID
        if (getUserFromDatabase(userId) != null) {
            System.out.println("User ID already exists. Registration failed.");
            return;
        }

        System.out.print("Enter user name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty. Registration failed.");
            return;
        }

        System.out.print("Enter user email: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("Email cannot be empty. Registration failed.");
            return;
        }
        if (isEmailAlreadyExists(email)) {
            System.out.println("Email already exists. Registration failed.");
            return;
        }

        System.out.print("Enter user role (Admin/Customer): ");
        String role = scanner.nextLine().trim();
        // Проверка корректности введённой роли
        if (!("Admin".equalsIgnoreCase(role) || "Customer".equalsIgnoreCase(role))) {
            System.out.println("Invalid role. Please enter Admin or Customer.");
            return;
        }

        // Для регистрации администратора требуется специальный пароль
        if ("Admin".equalsIgnoreCase(role)) {
            System.out.print("Enter admin registration password: ");
            String password = scanner.nextLine();
            if (!"admin123".equals(password)) {
                System.out.println("Invalid admin password. Registration failed.");
                return;
            }
        }

        // Вставка нового пользователя в базу данных
        String query = "INSERT INTO users (user_id, name, email, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, role);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("User registered successfully!");
            } else {
                System.out.println("Failed to register user.");
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
    }

    private static boolean isEmailAlreadyExists(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = databaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next(); // true, если email найден
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
            return false;
        }
    }

    private static User getUserFromDatabase(int userId) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (Connection connection = databaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                User user = new User();
                user.setUserId(userId);
                user.setName(resultSet.getString("name"));
                user.setEmail(resultSet.getString("email"));
                user.setRole(resultSet.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    // ----------------------- Меню для администратора (управление отелем) -----------------------
    private static void adminMenu() {
        while (true) {
            System.out.println("\n=== Admin Menu (Hotel Management) ===");
            System.out.println("1. Add Room");
            System.out.println("2. Delete Room");
            System.out.println("3. View All Rooms");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    addRoom();
                    break;
                case 2:
                    deleteRoom();
                    break;
                case 3:
                    viewAllRooms();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ----------------------- Функции управления отелем -----------------------
    private static void addRoom() {
        System.out.print("Enter room category: ");
        String category = scanner.nextLine();
        System.out.print("Enter room number: ");
        int roomNumber;
        try {
            roomNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid room number.");
            return;
        }
        System.out.print("Enter price: ");
        double price;
        try {
            price = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price.");
            return;
        }
        String query = "INSERT INTO rooms (room_number, category, price) VALUES (?, ?, ?)";
        try (Connection connection = databaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, roomNumber);
            statement.setString(2, category);
            statement.setDouble(3, price);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Room added successfully!");
            } else {
                System.out.println("Failed to add room.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding room: " + e.getMessage());
        }
    }

    private static void deleteRoom() {
        System.out.print("Enter room number to delete: ");
        int roomNumber;
        try {
            roomNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid room number.");
            return;
        }
        String query = "DELETE FROM rooms WHERE room_number = ?";
        try (Connection connection = databaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, roomNumber);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Room deleted successfully!");
            } else {
                System.out.println("No room found with that number.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
        }
    }

    private static void viewAllRooms() {
        String query = "SELECT * FROM rooms";
        try (Connection connection = databaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("\n=== List of Rooms ===");
            while (resultSet.next()) {
                int roomNumber = resultSet.getInt("room_number");
                String category = resultSet.getString("category");
                double price = resultSet.getDouble("price");
                System.out.printf("Room Number: %d, Category: %s, Price: %.2f%n",
                        roomNumber, category, price);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving rooms: " + e.getMessage());
        }
    }

    // ----------------------- Меню для пользователя (просмотр и покупка) -----------------------
    private static void customerMenu(User customer) {
        while (true) {
            System.out.println("\n=== Customer Menu (Hotel) ===");
            System.out.println("1. View All Rooms");
            System.out.println("2. Purchase Room");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    viewAllRooms();
                    break;
                case 2:
                    purchaseRoom(customer);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ----------------------- Функция покупки комнаты с записью транзакции -----------------------
    private static void purchaseRoom(User customer) {
        System.out.println("\nAvailable Rooms for Purchase:");
        viewAllRooms(); // Показываем все доступные комнаты
        System.out.print("Enter the room number you wish to purchase: ");
        int roomNumber;
        try {
            roomNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid room number.");
            return;
        }

        String query = "SELECT * FROM rooms WHERE room_number = ?";
        try (Connection connection = databaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, roomNumber);
            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                System.out.println("Room not found or already purchased.");
                return;
            }

            double price = rs.getDouble("price");
            java.util.Date currentDateUtil = new java.util.Date(); // Текущая дата для старта бронирования
            java.sql.Date currentDate = new java.sql.Date(currentDateUtil.getTime()); // Преобразуем в java.sql.Date
            // Пример: покупатель хочет забронировать комнату на 7 дней
            java.util.Date endDateUtil = new java.util.Date(currentDateUtil.getTime() + 7L * 24 * 60 * 60 * 1000); // Через 7 дней
            java.sql.Date endDate = new java.sql.Date(endDateUtil.getTime()); // Преобразуем в java.sql.Date

            // Начинаем транзакцию
            connection.setAutoCommit(false);

            try {
                // Запись транзакции покупки комнаты
                String insertQuery = "INSERT INTO transactions (user_id, room_number, price, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, customer.getUserId());
                    insertStmt.setInt(2, roomNumber);
                    insertStmt.setDouble(3, price);
                    insertStmt.setDate(4, currentDate); // Дата начала бронирования
                    insertStmt.setDate(5, endDate); // Дата окончания бронирования

                    int rowsInserted = insertStmt.executeUpdate();
                    if (rowsInserted == 0) {
                        throw new SQLException("Failed to record the transaction.");
                    }
                }

                // Обновляем статус комнаты на "занята"
                String updateRoomQuery = "UPDATE rooms SET is_available = FALSE WHERE room_number = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateRoomQuery)) {
                    updateStmt.setInt(1, roomNumber);
                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new SQLException("Failed to update room availability.");
                    }
                }

                // Коммитим изменения
                connection.commit();
                System.out.println("Room purchased successfully! Your booking will be from " + currentDate + " to " + endDate);
            } catch (SQLException e) {
                // Если возникла ошибка, откатываем транзакцию
                connection.rollback();
                System.err.println("Error during room purchase: " + e.getMessage());
            } finally {
                // Восстановление состояния авто-коммита
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room details: " + e.getMessage());
        }
    }
}
