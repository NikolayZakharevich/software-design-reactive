package model;

import org.bson.Document;

import java.util.*;

import static util.Currency.*;

public class Good {
    private static final String FIELD_GOOD_ID = "goodId";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_RUB = RUB_NAME;
    private static final String FIELD_USD = USD_NAME;
    private static final String FIELD_EUR = EUR_NAME;

    private final int goodId;
    private final String name;
    private final Map<Integer, String> currencyMap;

    public Good(Document doc) {
        this(doc.getInteger(FIELD_GOOD_ID),
                doc.getString(FIELD_NAME),
                doc.getString(FIELD_RUB),
                doc.getString(FIELD_USD),
                doc.getString(FIELD_EUR)
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
        return new Document(FIELD_GOOD_ID, goodId)
                .append(FIELD_NAME, name)
                .append(FIELD_RUB, currencyMap.get(RUB))
                .append(FIELD_USD, currencyMap.get(USD))
                .append(FIELD_EUR, currencyMap.get(EUR));
    }

    public String toString(int currency) {
        return "Good{" +
                "goodId=" + goodId +
                ", name='" + name + '\'' +
                ", price=" + currencyMap.get(currency) +
                '}';
    }
}
