/*
 * Copyright 2017-2020 Alex Magalhaes <alex@pensatocode.org>
 * Copyright 2016 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pensatocode.simplicity.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.max;

public final class StringUtil {

    private StringUtil() {
    }

    /**
     * Repeats the given String {@code count}-times to form a new String, with
     * the {@code separator} injected between.
     *
     * @param str       The string to repeat.
     * @param separator The string to inject between.
     * @param count     Number of times to repeat {@code str}; negative treated
     *                  as zero.
     * @return A new String.
     */
    public static String repeat(String str, String separator, int count) {
        StringBuilder sb = new StringBuilder((str.length() + separator.length()) * max(count, 0));

        for (int n = 0; n < count; n++) {
            if (n > 0) sb.append(separator);
            sb.append(str);
        }
        return sb.toString();
    }

    public static String convertToCamelCase(String input) {
        return convertToCamelCase(input, false);
    }

    public static String convertToCamelCase(String input, boolean capitalize) {
        final Pattern pattern = Pattern.compile("(_)([a-z])");
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(2).toUpperCase());
        }
        matcher.appendTail(sb);
        if (capitalize) {
            return capitalize(sb.toString());
        }
        return sb.toString();
    }

    public static String convertToSnakeCase(String input) {
        final Pattern pattern = Pattern.compile("(.)(\\p{Upper})");
        final String replacementPattern = "$1_$2";
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll(replacementPattern).toLowerCase();
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
