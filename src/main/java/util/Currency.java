package util;

public class Currency {
    public static final String RUB_NAME = "rub";
    public static final String USD_NAME = "usd";
    public static final String EUR_NAME = "eur";

    public static final int RUB = 643;
    public static final int USD = 840;
    public static final int EUR = 978;

    public static int currencyNameToCurrency(String currencyName) {
        switch (currencyName) {
            case RUB_NAME:
                return RUB;
            case USD_NAME:
                return USD;
            case EUR_NAME:
                return EUR;
            default:
                return 0;
        }
    }
}
