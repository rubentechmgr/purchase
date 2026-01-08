package com.example.purchase.domain;

import lombok.Getter;

/**
 * Enum representing supported treasury currencies with their descriptions and ISO 4217 codes.
 */
@Getter
public enum TreasuryCurrency {

    UNITED_STATES_DOLLAR("United-States-Dollar", "USD"),
    AFGHANISTAN_AFGHANI("Afghanistan-Afghani", "AFN"),
    ALBANIA_LEK("Albania-Lek", "ALL"),
    ALGERIA_DINAR("Algeria-Dinar", "DZD"),
    ANGOLA_KWANZA("Angola-Kwanza", "AOA"),
    ARGENTINA_PESO("Argentina-Peso", "ARS"),
    ARMENIA_DRAM("Armenia-Dram", "AMD"),
    AUSTRALIA_DOLLAR("Australia-Dollar", "AUD"),
    AUSTRIA_EURO("Austria-Euro", "EUR"),
    BAHAMAS_DOLLAR("Bahamas-Dollar", "BSD"),
    BAHRAIN_DINAR("Bahrain-Dinar", "BHD"),
    BANGLADESH_TAKA("Bangladesh-Taka", "BDT"),
    BARBADOS_DOLLAR("Barbados-Dollar", "BBD"),
    BELARUS_NEW_RUBLE("Belarus-New Ruble", "BYN"),
    BELIZE_DOLLAR("Belize-Dollar", "BZD"),
    BERMUDA_DOLLAR("Bermuda-Dollar", "BMD"),
    BOLIVIA_BOLIVIANO("Bolivia-Boliviano", "BOB"),
    BOSNIA_MARKA("Bosnia-Marka", "BAM"),
    BOTSWANA_PULA("Botswana-Pula", "BWP"),
    BRAZIL_REAL("Brazil-Real", "BRL"),
    BRUNEI_DOLLAR("Brunei-Dollar", "BND"),
    BULGARIA_LEV("Bulgaria-Lev New", "BGN"),
    CANADA_DOLLAR("Canada-Dollar", "CAD"),
    CHILE_PESO("Chile-Peso", "CLP"),
    CHINA_RENMINBI("China-Renminbi", "CNY"),
    COLOMBIA_PESO("Colombia-Peso", "COP"),
    COSTA_RICA_COLON("Costa Rica-Colon", "CRC"),
    CUBA_PESO("Cuba-Peso", "CUP"),
    CZECH_REPUBLIC_KORUNA("Czech Republic-Koruna", "CZK"),
    DENMARK_KRONE("Denmark-Krone", "DKK"),
    DOMINICAN_PESO("Dominican Rep.-Peso", "DOP"),
    EGYPT_POUND("Egypt-Pound", "EGP"),
    EURO_ZONE_EURO("Euro-Zone-Euro", "EUR"),
    GHANA_CEDI("Ghana-Cedi", "GHS"),
    HONG_KONG_DOLLAR("Hong-Kong-Dollar", "HKD"),
    INDIA_RUPEE("India-Rupee", "INR"),
    JAPAN_YEN("Japan-Yen", "JPY"),
    MEXICO_PESO("Mexico-Peso", "MXN"),
    NEW_ZEALAND_DOLLAR("New-Zealand-Dollar", "NZD"),
    NORWAY_KRONE("Norway-Krone", "NOK"),
    SOUTH_AFRICA_RAND("South-Africa-Rand", "ZAR"),
    SOUTH_KOREA_WON("South-Korea-Won", "KRW"),
    SWEDEN_KRONA("Sweden-Krona", "SEK"),
    SWITZERLAND_FRANC("Switzerland-Franc", "CHF"),
    TAIWAN_DOLLAR("Taiwan-Dollar", "TWD"),
    THAILAND_BAHT("Thailand-Baht", "THB"),
    UNITED_KINGDOM_POUND("United-Kingdom-Pound", "GBP");

    private final String desc;
    private final String currencyCode; // ISO 4217 alpha

    /**
     * Constructor for TreasuryCurrency enum.
     * @param desc Description of the currency.
     * @param currencyCode ISO 4217 currency code.
     */
    TreasuryCurrency(String desc, String currencyCode) {
        this.desc = desc;
        this.currencyCode = currencyCode;
    }

    /**
     * Returns the TreasuryCurrency enum constant matching the given description (case-insensitive).
     * @param desc Description to match.
     * @return TreasuryCurrency or null if not found.
     */
    public static TreasuryCurrency fromDesc(String desc) {
        if (desc == null) return null;
        for (TreasuryCurrency c : values()) {
            if (c.desc.equalsIgnoreCase(desc)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Returns the TreasuryCurrency enum constant matching the given ISO 4217 code (case-insensitive).
     * @param code ISO 4217 code to match.
     * @return TreasuryCurrency or null if not found.
     */
    public static TreasuryCurrency fromCode(String code) {
        if (code == null) return null;
        for (TreasuryCurrency c : values()) {
            if (c.currencyCode.equalsIgnoreCase(code)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Returns the treasury description for a given 3-letter ISO code, or null if not found.
     * @param code ISO 4217 code.
     * @return Description string or null.
     */
    public static String descForCode(String code) {
        TreasuryCurrency c = fromCode(code);
        return c == null ? null : c.getDesc();
    }
}
