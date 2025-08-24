package constants;

public class ID implements Comparable<ID> {
    public long milliseconds;
    public int sequenceNumber;

    public ID() {}

    public ID(long milliseconds, int sequenceNumber) {
        this.milliseconds = milliseconds;
        this.sequenceNumber = sequenceNumber;
    }

    public static ID parse(String id) {
        String[] parts = id.split("-");
        try {
            return new ID(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            System.err.println("ID: \"" + id + "\" is not a valid ID");
        }
        return null;
    }

    public String toString() {
        return this.milliseconds + "-" + this.sequenceNumber;
    }

    @Override
    public int compareTo(ID o) {
        if (this.milliseconds > o.milliseconds) {
            return 1;
        }
        if (this.milliseconds < o.milliseconds) {
            return -1;
        }
        return Integer.compare(this.sequenceNumber, o.sequenceNumber);
    }
}
