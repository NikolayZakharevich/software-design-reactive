package model;

import org.bson.Document;

public class User {

    public final static String FIELD_USER_ID = "userId";
    public final static String FIELD_NAME = "name";
    public final static String FIELD_CURRENCY = "currency";

    public final int userId;
    public final String name;
    public final int currency;

    public User(Document doc) {
        this(doc.getInteger(FIELD_USER_ID), doc.getString(FIELD_NAME), Integer.parseInt(doc.getString(FIELD_CURRENCY)));
    }

    public User(int userId, String name, int currency) {
        this.userId = userId;
        this.name = name;
        this.currency = currency;
    }

    public Document getDocument() {
        return new Document(FIELD_USER_ID, userId).append(FIELD_NAME, name).append(FIELD_CURRENCY, currency);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", currency=" + currency +
                '}';
    }
}
