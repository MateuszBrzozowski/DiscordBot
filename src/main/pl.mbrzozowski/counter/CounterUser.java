package counter;

class CounterUser {

    private final String userID;
    private int countMsgAll = 0;

    public CounterUser(String userID) {
        this.userID = userID;
    }

    /**
     * @param userID      ID użytkownika
     * @param countMsgAll liczba wszytskich wiadomości wysłanych przez uzytkownika
     */
    public CounterUser(String userID, int countMsgAll) {
        this.userID = userID;
        this.countMsgAll = countMsgAll;
    }

    public void plusOne() {
        countMsgAll++;
    }

    public String getUserID() {
        return userID;
    }

    public int getCountMsgAll() {
        return countMsgAll;
    }

    public void addUserToDatabase() {
        CounterDatabase cdb = new CounterDatabase();
        cdb.addNewUser(userID);
    }
}
