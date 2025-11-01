package com.dhbw.broker.bff.util;

import java.text.Normalizer;
import java.util.Locale;

public final class AssetNameNormalizer {

    private AssetNameNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }

        String v = raw.trim();
        if (v.isEmpty()) {
            return null;
        }

        String lower = toAscii(v).toLowerCase(Locale.ROOT);

        lower = lower
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");

        lower = stripTail(lower, "preis");
        lower = stripTail(lower, "kurs");
        lower = stripTail(lower, "wert");
        lower = stripTail(lower, "rate");
        lower = stripTail(lower, "value");

        if (isDirectSymbol(lower)) {
            return lower.toUpperCase(Locale.ROOT);
        }

        switch (lower) {
            case "bitcoin":
            case "bitcoins":
            case "btcprice":
            case "btccurrent":
                return "BTC";

            case "ethereum":
            case "ether":
            case "ethereums":
            case "ethprice":
                return "ETH";

            case "gold":
            case "golds":
            case "goldspot":
                return "XAU";

            case "silber":
            case "silver":
            case "silbers":
            case "silvers":
                return "XAG";

            case "palladium":
            case "palladiums":
                return "XPD";

            case "kupfer":
            case "copper":
            case "coppers":
                return "HG";
        }

        if (lower.startsWith("bitcoin")) {
            return "BTC";
        }
        if (lower.startsWith("ether")) {
            return "ETH";
        }
        if (lower.startsWith("gold")) {
            return "XAU";
        }
        if (lower.startsWith("silber") || lower.startsWith("silver")) {
            return "XAG";
        }
        if (lower.startsWith("palladium")) {
            return "XPD";
        }
        if (lower.startsWith("kupfer") || lower.startsWith("copper")) {
            return "HG";
        }

        if (lower.length() <= 5) {
            return lower.toUpperCase(Locale.ROOT);
        }

        return null;
    }

    private static boolean isDirectSymbol(String lower) {
        return "btc".equals(lower)
                || "eth".equals(lower)
                || "xau".equals(lower)
                || "xag".equals(lower)
                || "xpd".equals(lower)
                || "hg".equals(lower);
    }

    private static String toAscii(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private static String stripTail(String value, String tail) {
        if (value.endsWith(tail)) {
            return value.substring(0, value.length() - tail.length());
        }
        return value;
    }
}
