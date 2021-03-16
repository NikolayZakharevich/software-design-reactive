package model;

import org.bson.Document;

import java.util.*;

import static util.Currency.*;

public class Product {
    private static final String FIELD_PRODUCT_ID = "productId";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_USD = USD_NAME;
    private static final String FIELD_EUR = EUR_NAME;
    private static final String FIELD_RUB = RUB_NAME;

    private final int productId;
    private final String name;
    private final Map<Integer, String> currencyMap;

    public Product(Document doc) {
        this(doc.getInteger(FIELD_PRODUCT_ID),
                doc.getString(FIELD_NAME),
                doc.getString(FIELD_USD),
                doc.getString(FIELD_EUR),
                doc.getString(FIELD_RUB)
                );
    }

    public Product(int productId, String name, String usd, String eur, String rub) {
        this.productId = productId;
        this.name = name;
        this.currencyMap = new HashMap<>();
        currencyMap.put(USD, usd);
        currencyMap.put(EUR, eur);
        currencyMap.put(RUB, rub);
    }

    public Document getDocument() {
        return new Document(FIELD_PRODUCT_ID, productId)
                .append(FIELD_NAME, name)
                .append(FIELD_USD, currencyMap.get(USD))
                .append(FIELD_EUR, currencyMap.get(EUR))
                .append(FIELD_RUB, currencyMap.get(RUB));
    }

    public String toString(int currency) {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", price=" + currencyMap.get(currency) +
                '}';
    }
}
