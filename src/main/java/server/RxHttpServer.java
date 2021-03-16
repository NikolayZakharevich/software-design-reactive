package server;

import com.mongodb.rx.client.Success;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import model.Good;
import repository.ReactiveMongoDriver;
import model.User;
import rx.Observable;
import util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class RxHttpServer {

    public static final String METHOD_CREATE_USER = "registerUser";
    public static final String METHOD_CREATE_GOOD = "addGood";
    public static final String METHOD_GET_GOODS = "getGoods";

    public static final String PARAM_USER_ID = "userId";
    public static final String PARAM_CURRENCY = "currency";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_GOOD_ID = "goodId";
    public static final String PARAM_RUB = "rub";
    public static final String PARAM_USD = "usd";
    public static final String PARAM_EUR = "eur";

    public static final String MESSAGE_REGISTER_USER_SUCCESS = "Successfully registered new user";
    public static final String MESSAGE_REGISTER_USER_FAIL = "Failed to save new user to database";
    public static final String MESSAGE_ADD_GOOD_SUCCESS = "Successfully added new good";
    public static final String MESSAGE_ADD_GOOD_FAIL = "Failed to add good to database";

    public static final String MESSAGE_MISSING_REQUIRED_PARAMS = "Missing required parameters: `%s`";
    public static final String MESSAGE_METHOD_NOT_FOUND = "Method `%s` not found";

    public static final int PORT = 8080;

    public static void main(final String[] args) {
        HttpServer
                .newServer(PORT)
                .start((req, resp) -> {
                    Pair<Observable<String>, HttpResponseStatus> messageAndStatus;

                    String name = req.getDecodedPath().substring(1);
                    Map<String, List<String>> queryParams = req.getQueryParameters();
                    switch (name) {
                        case METHOD_CREATE_USER:
                            messageAndStatus = registerUser(queryParams);
                            break;
                        case METHOD_CREATE_GOOD:
                            messageAndStatus = addGood(queryParams);
                            break;
                        case METHOD_GET_GOODS:
                            messageAndStatus = getGoods(queryParams);
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


    public static Pair<Observable<String>, HttpResponseStatus> registerUser(Map<String, List<String>> queryParam) {
        List<String> required = Arrays.asList(PARAM_USER_ID, PARAM_CURRENCY, PARAM_NAME);
        if (areRequiredParamsMissing(queryParam, required)) {
            return wrapErrorMissingRequired(queryParam, required);
        }

        int id = Integer.parseInt(queryParam.get(PARAM_USER_ID).get(0));
        String name = queryParam.get(PARAM_NAME).get(0);
        int currency = Integer.parseInt(queryParam.get(PARAM_CURRENCY).get(0));

        Success success = ReactiveMongoDriver.createUser(new User(id, name, currency));
        return wrapSuccess(success, MESSAGE_REGISTER_USER_SUCCESS, MESSAGE_REGISTER_USER_FAIL);
    }

    public static Pair<Observable<String>, HttpResponseStatus> addGood(Map<String, List<String>> queryParam) {
        List<String> required = Arrays.asList(PARAM_GOOD_ID, PARAM_NAME, PARAM_RUB, PARAM_USD, PARAM_EUR);
        if (areRequiredParamsMissing(queryParam, required)) {
            return wrapErrorMissingRequired(queryParam, required);
        }

        int goodId = Integer.parseInt(queryParam.get(PARAM_GOOD_ID).get(0));
        String name = queryParam.get(PARAM_NAME).get(0);
        String rub = queryParam.get(PARAM_RUB).get(0);
        String usd = queryParam.get(PARAM_USD).get(0);
        String eur = queryParam.get(PARAM_EUR).get(0);

        Success success = ReactiveMongoDriver.createGood(new Good(goodId, name, rub, usd, eur));
        return wrapSuccess(success, MESSAGE_ADD_GOOD_SUCCESS, MESSAGE_ADD_GOOD_FAIL);
    }

    public static Pair<Observable<String>, HttpResponseStatus> getGoods(Map<String, List<String>> queryParam) {
        List<String> required = Collections.singletonList(PARAM_USER_ID);
        if (areRequiredParamsMissing(queryParam, required)) {
            return wrapErrorMissingRequired(queryParam, required);
        }

        Integer userId = Integer.valueOf(queryParam.get(PARAM_USER_ID).get(0));
        Observable<String> goods = ReactiveMongoDriver.getGoods(userId);

        return new Pair<>(
                Observable.just("{ user_id = " + userId + ", goods = [")
                        .concatWith(goods)
                        .concatWith(Observable.just("]}")),
                HttpResponseStatus.OK
        );
    }

    private static boolean areRequiredParamsMissing(Map<String, List<String>> queryParam, List<String> required) {
        return required.stream().anyMatch(value -> !queryParam.containsKey(value));
    }

    private static Pair<Observable<String>, HttpResponseStatus> wrapSuccess(Success success, String okMessage, String errorMessage) {
        return success == Success.SUCCESS
                ? new Pair<>(Observable.just(okMessage), HttpResponseStatus.OK)
                : new Pair<>(Observable.just(errorMessage), HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    private static Pair<Observable<String>, HttpResponseStatus> wrapErrorMissingRequired(Map<String, List<String>> queryParam, List<String> required) {
        List<String> missingAttributes = required
                .stream()
                .filter(val -> !queryParam.containsKey(val))
                .collect(Collectors.toList());
        return new Pair<>(
                Observable.just(String.format(MESSAGE_MISSING_REQUIRED_PARAMS, String.join(", ", missingAttributes))),
                HttpResponseStatus.BAD_REQUEST
        );
    }
}
