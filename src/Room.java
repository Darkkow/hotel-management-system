abstract class Room {
    protected int roomNumber;
    protected String category;
    protected double price;
    protected boolean isBooked;

    public Room(int roomNumber, String category, double price) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.price = price;
        this.isBooked = false;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public int getRoomNumber() {
        return roomNumber;
    }
}