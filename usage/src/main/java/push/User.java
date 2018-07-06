package push;

public abstract class User {
    String id;

    public static User giovanni = new Giovanni();
    public static User jack = new Jack();

    public String getId() {
        return id;
    }

    private static class Giovanni extends User {
        public Giovanni() {
            this.id = "e26d45be-7250-42a6-b68b-634bf6b2e2bc";
        }
    }

    private static class Jack extends User {
        public Jack() {
            this.id = "7ff03c80-932d-428f-95a1-755e12161296";
        }
    }



}
