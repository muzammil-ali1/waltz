/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
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
 * See the License for the specific
 *
 */

package com.khartec.waltz.common;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkAll;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toList;


public class StringUtilities {

    /**
     * Determines if the given string is null or empty (after trimming).
     * <code>notEmpty</code> provides the inverse.
     * @param x the String to check
     * @return true iff x is null or x.trim is empty
     */
    public static boolean isEmpty(String x) {
        return x == null || x.trim().equals("");
    }


    /**
     * Convenience method for <code> ! isEmpty(x) </code>
     * @param x the String to check
     * @return true iff x.trim is non empty
     */
    public static boolean notEmpty(String x) {
        return ! isEmpty(x);
    }


    public static boolean isEmpty(Optional<String> maybeString) {
        return OptionalUtilities
                .ofNullableOptional(maybeString)
                .map(StringUtilities::isEmpty)
                .orElse(true);
    }


    public static String ifEmpty(String x, String defaultValue) {
        return isEmpty(x) ? defaultValue : x;
    }


    public static Long parseLong(String value, Long dflt) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            return dflt;
        }
    }


    public static Integer parseInteger(String value, Integer dflt) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return dflt;
        }
    }


    public static boolean isNumericLong(String value) {
        return ! isEmpty(value)
                && parseLong(value, null) != null;
    }


    public static String mkSafe(String str) {
        return str == null
                ? ""
                : str;
    }


    public static String limit(String str, int maxLength) {
        if (str == null) return null;
        int howMuch = Math.min(maxLength, str.length());
        return str.substring(0, howMuch);
    }


    public static int length(String str) {
        return str == null
                ? 0
                : str.length();
    }


    public static String join(Collection<?> values, String separator) {
        return values.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(separator));
    }


    public static <T> String joinUsing(Collection<T> values, Function<T, String> toStringFn, String separator) {
        return values.stream()
                .map(toStringFn)
                .collect(Collectors.joining(separator));
    }


    public static <T> List<T> splitThenMap(String str, String separator, Function<String, T> itemTransformer) {
        checkNotNull(itemTransformer, "itemTransformer cannot be null");
        if (isEmpty(str) || isEmpty(separator)) { return Collections.emptyList(); }

        return Arrays
                .stream(str.split(separator))
                .map(itemTransformer)
                .collect(toList());
    }


    public static List<String> tokenise(String value) {
        return tokenise(value, " ");
    }


    public static List<String> tokenise(String value, String regex) {
        checkNotNull(value, "value cannot be null");

        String[] split = value.split(regex);

        return Stream.of(split)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    public static String lower(String value) {
        checkNotNull(value, "value cannot be null");
        return value
                .toLowerCase()
                .trim();
    }


    public static char firstChar(String str, char dflt) {
        return mkSafe(str).length() > 0
                ? str.charAt(0)
                : dflt;
    }


    public static Optional<String> toOptional(String str) {
        return isEmpty(str)
                ? Optional.empty()
                : Optional.of(str);
    }


    /**
     * Given a vararg/array of path segments will join them
     * to make a string representing the path.  No starting or trailing
     * slashes are added to the resultant path string.
     *
     * @param segs Segments to join
     * @return String representing the path produced by joining the segments
     * @throws IllegalArgumentException If any of the segments are null
     */
    public static String mkPath(Object... segs) {
        checkAll(
                segs,
                d -> d != null && notEmpty(d.toString()),
                "Cannot convert empty segments to path");

        return Stream
                .of(segs)
                .map(Object::toString)
                .collect(Collectors.joining("/"))
                .replaceAll("/+", "/");

    }
}
