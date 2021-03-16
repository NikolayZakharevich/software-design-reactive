package repository;

import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.Success;
import model.Product;
import model.User;
import org.bson.Document;
import rx.Observable;

import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

public class ReactiveMongoDriver {

    public static MongoClient client = MongoClients.create("mongodb://localhost:27017");

    private final static String DATABASE_RXTEXT = "rxtest";

    private final static String COLLECTION_USER = "user";
    private final static String COLLECTION_PRODUCT = "product";

    private final static int TIMEOUT = 10;

    public static Success createUser(User user) {
        return addToCollection(COLLECTION_USER, user.getDocument());
    }

    public static Success createProduct(Product product) {
        return addToCollection(COLLECTION_PRODUCT, product.getDocument());
    }

    public static Observable<User> getUserById(Integer userId) {
        return client
                .getDatabase(DATABASE_RXTEXT)
                .getCollection(COLLECTION_USER)
                .find(eq(User.FIELD_USER_ID, userId))
                .first()
                .map(User::new);
    }

    public static Observable<String> getProducts(Integer userId) {
        return getUserById(userId)
                .flatMap(user -> client.getDatabase(DATABASE_RXTEXT)
                        .getCollection(COLLECTION_PRODUCT)
                        .find()
                        .toObservable()
                        .map(doc -> new Product(doc).toString(user.currency))
                        .reduce((lhs, rhs) -> lhs + ", " + rhs)
                );
    }

    private static Success addToCollection(String collection, Document document) {
        return client
                .getDatabase(DATABASE_RXTEXT)
                .getCollection(collection)
                .insertOne(document)
                .timeout(TIMEOUT, TimeUnit.SECONDS)
                .toBlocking()
                .single();
    }
}