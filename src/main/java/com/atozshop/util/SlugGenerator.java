package com.atozshop.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugGenerator {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-|-$)");

    public static String generateSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String normalized = Normalizer.normalize(input.toLowerCase(Locale.ENGLISH), Normalizer.Form.NFD);
        String withDashes = WHITESPACE.matcher(normalized).replaceAll("-");
        String cleaned = NON_LATIN.matcher(withDashes).replaceAll("");
        String trimmed = EDGE_DASHES.matcher(cleaned).replaceAll("");

        return trimmed;
    }
}
