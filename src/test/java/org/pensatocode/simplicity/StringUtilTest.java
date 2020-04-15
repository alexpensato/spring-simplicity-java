/*
 * Copyright 2020 Alex Magalhaes <alex@pensatocode.org>
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
package org.pensatocode.simplicity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.pensatocode.simplicity.util.StringUtil.convertToCamelCase;
import static org.pensatocode.simplicity.util.StringUtil.convertToSnakeCase;

public class StringUtilTest {

    @ParameterizedTest
    @MethodSource("correctConversionFromCamelCaseToSnakeCase")
    @DisplayName("Successful conversion from camel case to snake case")
    public void testConversioToSnakeCase(TestValues<String> input) {
        Assertions.assertEquals(input.getExpected(), convertToSnakeCase(input.getCurrent()));
    }

    @ParameterizedTest
    @MethodSource("correctConversionFromSnakeCaseToCamelCase")
    @DisplayName("Successful conversion from snake case to camel case")
    public void testConversioToCamelCase(TestValues<String> input) {
        Assertions.assertEquals(input.getExpected(), convertToCamelCase(input.getCurrent()));
    }

    private static List<TestValues<String>> correctConversionFromSnakeCaseToCamelCase() {
        final List<TestValues<String>> source = new ArrayList<>();
        source.add(new TestValues<>("camelCase","camel_case"));
        source.add(new TestValues<>("snakeCase","snake_case"));
        source.add(new TestValues<>("longerSentenceShouldWork","longer_sentence_should_work"));
        source.add(new TestValues<>("This","This"));
        source.add(new TestValues<>("this","this"));
        return source;
    }

    private static List<TestValues<String>> correctConversionFromCamelCaseToSnakeCase() {
        final List<TestValues<String>> source = new ArrayList<>();
        source.add(new TestValues<>("camel_case","camelCase"));
        source.add(new TestValues<>("snake_case","snakeCase"));
        source.add(new TestValues<>("longer_sentence_should_work","longerSentenceShouldWork"));
        source.add(new TestValues<>("this","This"));
        source.add(new TestValues<>("this","this"));
        return source;
    }
}
