package server;

import com.mongodb.rx.client.Success;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import model.Product;
import repository.ReactiveMongoDriver;
import model.User;
import rx.Observable;
import util.Currency;
import util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RxHttpServer {

    public static final String METHOD_REGISTER_USER = "registerUser";
    public static final String METHOD_ADD_PRODUCT = "addProduct";
    public static final String METHOD_GET_PRODUCTS = "getProducts";

    public static final String PARAM_USER_ID = "userId";
    public static final String PARAM_CURRENCY = "currency";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PRODUCT_ID = "productId";
    public static final String PARAM_USD = Currency.USD_NAME;
    public static final String PARAM_EUR = Currency.EUR_NAME;
    public static final String PARAM_RUB = Currency.RUB_NAME;

    public static final String MESSAGE_REGISTER_USER_SUCCESS = "Successfully registered new user";
    public static final String MESSAGE_REGISTER_USER_FAIL = "Failed to save new user to database";
    public static final String MESSAGE_ADD_PRODUCT_SUCCESS = "Successfully added new product";
    public static final String MESSAGE_ADD_PRODUCT_FAIL = "Failed to add product to database";

    public static final String MESSAGE_METHOD_NOT_FOUND = "Method `%s` not found";
    public static final String MESSAGE_MISSING_REQUIRED_PARAMS = "Missing required parameters: `%s`";

    public static final int PORT = 8080;

    public static void main(final String[] args) {
        HttpServer
                .newServer(PORT)
                .start((req, resp) -> {
                    Pair<Observable<String>, HttpResponseStatus> messageAndStatus;

                    String name = req.getDecodedPath().substring(1);
                    Map<String, List<String>> queryParams = req.getQueryParameters();
                    switch (name) {
                        case METHOD_REGISTER_USER:
                            messageAndStatus = registerUser(queryParams);
                            break;
                        case METHOD_ADD_PRODUCT:
                            messageAndStatus = addProduct(queryParams);
                            break;
                        case METHOD_GET_PRODUCTS:
                            messageAndStatus = getProducts(queryParams);
                            break;
                        default:
                            messageAndStatus = new Pair<>(
                                    Observable.just(String.format(MESSAGE_METHOD_NOT_FOUND, name)),
                                    HttpResponseStatus.NOT_FOUND
                            );
                    }
                    resp.setStatus(messageAndStatus.second);
                    return resp.writeString(messageAndStatus.first);
                })
                .awaitShutdown();
    }

    public static Pair<Observable<String>, HttpResponseStatus> registerUser(Map<String, List<String>> queryParams) {
        List<String> missingParams = getMissingRequiredParams(queryParams,
                Stream.of(PARAM_USER_ID, PARAM_CURRENCY, PARAM_NAME));
        if (!missingParams.isEmpty()) {
            return wrapErrorMissingRequired(missingParams);
        }

        int id = Integer.parseInt(queryParams.get(PARAM_USER_ID).get(0));
        String name = queryParams.get(PARAM_NAME).get(0);
        int currency = Currency.currencyNameToCurrency(queryParams.get(PARAM_CURRENCY).get(0));

        Success success = ReactiveMongoDriver.createUser(new User(id, name, currency));
        return wrapSuccess(success, MESSAGE_REGISTER_USER_SUCCESS, MESSAGE_REGISTER_USER_FAIL);
    }

    public static Pair<Observable<String>, HttpResponseStatus> addProduct(Map<String, List<String>> queryParams) {
        List<String> missingParams = getMissingRequiredParams(queryParams,
                Stream.of(PARAM_PRODUCT_ID, PARAM_NAME, PARAM_RUB, PARAM_USD, PARAM_EUR));
        if (!missingParams.isEmpty()) {
            return wrapErrorMissingRequired(missingParams);
        }

        int productId = Integer.parseInt(queryParams.get(PARAM_PRODUCT_ID).get(0));
        String name = queryParams.get(PARAM_NAME).get(0);
        String usd = queryParams.get(PARAM_USD).get(0);
        String eur = queryParams.get(PARAM_EUR).get(0);
        String rub = queryParams.get(PARAM_RUB).get(0);

        Success success = ReactiveMongoDriver.createProduct(new Product(productId, name, usd, eur, rub));
        return wrapSuccess(success, MESSAGE_ADD_PRODUCT_SUCCESS, MESSAGE_ADD_PRODUCT_FAIL);
    }

    public static Pair<Observable<String>, HttpResponseStatus> getProducts(Map<String, List<String>> queryParams) {
        List<String> missingParams = getMissingRequiredParams(queryParams, Stream.of(PARAM_USER_ID));
        if (!missingParams.isEmpty()) {
            return wrapErrorMissingRequired(missingParams);
        }

        Integer userId = Integer.valueOf(queryParams.get(PARAM_USER_ID).get(0));
        Observable<String> products = ReactiveMongoDriver.getProducts(userId);

        return new Pair<>(
                Observable.just("{ userId = " + userId + ", products = [")
                        .concatWith(products)
                        .concatWith(Observable.just("]}")),
                HttpResponseStatus.OK
        );
    }

    private static List<String> getMissingRequiredParams(Map<String, List<String>> queryParams, Stream<String> required) {
        return required
                .filter(param -> !queryParams.containsKey(param))
                .collect(Collectors.toList());
    }

    private static Pair<Observable<String>, HttpResponseStatus> wrapSuccess(Success success, String okMessage, String errorMessage) {
        return success == Success.SUCCESS
                ? new Pair<>(Observable.just(okMessage), HttpResponseStatus.OK)
                : new Pair<>(Observable.just(errorMessage), HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    private static Pair<Observable<String>, HttpResponseStatus> wrapErrorMissingRequired(List<String> params) {
        return new Pair<>(
                Observable.just(String.format(MESSAGE_MISSING_REQUIRED_PARAMS, String.join(", ", params))),
                HttpResponseStatus.BAD_REQUEST
        );
    }
}
