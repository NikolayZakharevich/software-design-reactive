package model;

import org.bson.Document;

import java.util.*;

import static util.Currency.*;

public class Good {
    private static final String FIELD_GOOD_ID = "goodId";
    private static final String FIELD_NAME = "name";

    private final int goodId;
    private final String name;
    private final Map<Integer, String> currencyMap;

    public Good(Document doc) {
        this(doc.getInteger(FIELD_GOOD_ID),
                doc.getString(FIELD_NAME),
                doc.getString(RUB),
                doc.getString(USD),
                doc.getString(EUR)
        );
    }

    public Good(int goodId, String name, String rub, String usd, String eur) {
        this.goodId = goodId;
        this.name = name;
        this.currencyMap = new HashMap<>();
        currencyMap.put(RUB, rub);
        currencyMap.put(USD, usd);
        currencyMap.put(EUR, eur);
    }

    public Document getDocument() {
        Document document = new Document(FIELD_GOOD_ID, goodId).append(FIELD_NAME, name);
        for (int currency : CURRENCIES) {
            document.append(String.valueOf(currency), currencyMap.get(currency));
        }
        return document;
    }

    public String toString(int currency) {
        return "Good{" +
                "goodId=" + goodId +
                ", name='" + name + '\'' +
                ", price=" + currencyMap.get(currency) +
                '}';
    }
}
