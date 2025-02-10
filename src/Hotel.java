import java.util.ArrayList;
import java.util.List;

class Hotel {
    private String name;
    private String address;
    private int rating;
    private List<Room> rooms;

    public Hotel(String name, String address, int rating) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.rooms = new ArrayList<>();
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void removeRoom(int roomNumber) {
        rooms.removeIf(room -> room.getRoomNumber() == roomNumber);
    }
}