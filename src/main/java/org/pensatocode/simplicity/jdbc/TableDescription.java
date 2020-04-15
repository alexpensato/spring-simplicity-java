/*
 * Copyright 2017-2020 Alex Magalhaes <alex@pensatocode.org>
 * Copyright 2016 Jakub Jirutka <jakub@jirutka.cz>.
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
package org.pensatocode.simplicity.jdbc;

import java.util.List;

import static java.util.Collections.singletonList;

public class TableDescription {

    private String tableName;
    private String selectClause = "*";
    private String fromClause;
    private List<String> pkColumns = singletonList("id");
    private List<String> columns;


    public TableDescription() {
    }

    public TableDescription(String tableName, List<String> columns, String selectClause, String fromClause, String... pkColumns) {
        this.tableName = tableName;
        this.columns = columns;
        this.selectClause = (selectClause!=null)? selectClause : "*";
        this.fromClause = (fromClause!=null)? fromClause : tableName;
        this.pkColumns = (pkColumns != null && pkColumns.length > 0)? List.of(pkColumns) : List.of("id");
    }

    public TableDescription(String tableName, List<String> columns, String fromClause, String... pkColumns) {
        this(tableName, columns, null, fromClause, pkColumns);
    }

    public TableDescription(String tableName, List<String> columns, String... pkColumns) {
        this(tableName, columns, null, null, pkColumns);
    }

    public TableDescription(String tableName, List<String> columns) {
        this(tableName, columns, null, null, new String[0]);
    }

    public String getTableName() {
        return tableName;
    }

    public String getSelectClause() {
        return selectClause;
    }

    public String getFromClause() {
        return fromClause;
    }

    public List<String> getPkColumns() {
        return pkColumns;
    }

    public List<String> getColumns() {
        return columns;
    }
}
