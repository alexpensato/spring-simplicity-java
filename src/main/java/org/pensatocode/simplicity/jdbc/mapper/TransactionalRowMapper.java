/*
 * Copyright 2017-2020 Alex Magalhaes <alex@pensatocode.org>
 * Copyright 2012-2014 Tomasz Nurkiewicz <nurkiewicz@gmail.com>.
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
package org.pensatocode.simplicity.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TransactionalRowMapper<T> implements RowMapper<T> {

    public abstract Map<String, Object> mapColumns(T t);

    public abstract Map<String, Integer> mapTypes();

    public List<Object> columnsValues(T entity, List<String> columns) throws NoSuchFieldException {
        Map<String, Object> entityMap = this.mapColumns(entity);
        List<Object> result = new ArrayList<>(entityMap.size());
        for(String columnName: columns) {
            Object columnValue = entityMap.get(columnName);
            if (columnValue != null) {
                result.add(columnValue);
            } else {
                throw new NoSuchFieldException("Column name not found in entity: " + columnName);
            }
        }
        return result;
    }

    public Object[] columnsValues(T entity, String[] columns) throws NoSuchFieldException {
        Map<String, Object> entityMap = this.mapColumns(entity);
        Object[] result = new Object[columns.length];
        int i=0;
        for(String columnName: columns) {
            Object columnValue = entityMap.get(columnName);
            if (columnValue != null) {
                result[i++] = columnValue;
            } else {
                throw new NoSuchFieldException("Column name not found in entity: " + columnName);
            }
        }
        return result;
    }

    public int[] columnsTypes(String... columns) throws NoSuchFieldException {
        Map<String, Integer> entityMap = this.mapTypes();
        int[] result = new int[columns.length];
        int i = 0;
        for(String columnName: columns) {
            Integer columnType = entityMap.get(columnName);
            if (columnType != null) {
                result[i++] = columnType;
            } else {
                throw new NoSuchFieldException("Column name not found in entity: " + columnName);
            }
        }
        return result;
    }

    protected java.util.Date convertToDate(java.sql.Timestamp timestamp) {
        if(timestamp == null) {
            return null;
        }
        return new java.util.Date(timestamp.getNanos());
    }

    protected char convertToChar(String str) {
        if(str == null) {
            return '\u0000';
        }
        char[] array = str.toCharArray();
        if(array.length == 0) {
            return '\u0000';
        }
        return array[0];
    }

    protected LocalDate convertToLocalDate(java.sql.Date date) {
        if(date == null) {
            return null;
        }
        return date.toLocalDate();
    }

    protected LocalTime convertToLocalTime(java.sql.Time time) {
        if(time == null) {
            return null;
        }
        return time.toLocalTime();
    }

    protected LocalDateTime convertToLocalDateTime(java.sql.Timestamp timestamp) {
        if(timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
