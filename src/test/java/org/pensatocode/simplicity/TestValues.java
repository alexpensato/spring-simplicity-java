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

import java.io.Serializable;

public class TestValues<T> implements Serializable {
    private T expected;
    private T current;

    public T getExpected() {
        return this.expected;
    }

    public T getCurrent() {
        return this.current;
    }

    public TestValues(T expected, T current) {
        this.expected = expected;
        this.current = current;
    }

    @Override
    public String toString() {
        return "TestValues[" +
            "expected=" + expected +
            ", current=" + current +
            ']';
    }

    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.expected != null ? this.expected.hashCode() : 0);
        hash = 31 * hash + (this.current != null ? this.current.hashCode() : 0);
        return hash;
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TestValues)) {
            return false;
        } else {
            TestValues<T> values = (TestValues<T>)o;
            if (this.expected != null) {
                if (!this.expected.equals(values.expected)) {
                    return false;
                }
            } else if (values.expected != null) {
                return false;
            }

            if (this.current != null) {
                return this.current.equals(values.current);
            } else return values.current == null;
        }
    }
}
