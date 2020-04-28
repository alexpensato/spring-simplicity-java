/*
 * Copyright 2017-2020 Alex Magalhaes <alex@pensatocode.org>
 * Copyright 2012-2014 Tomasz Nurkiewicz <nurkiewicz@gmail.com>.
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
package org.pensatocode.simplicity.jdbc;

import org.pensatocode.simplicity.jdbc.exception.NoRecordUpdatedException;
import org.pensatocode.simplicity.jdbc.sql.SqlGenerator;
import org.pensatocode.simplicity.jdbc.sql.SqlGeneratorFactory;
import org.pensatocode.simplicity.jdbc.mapper.TransactionalRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pensatocode.simplicity.util.IterableUtil.toList;
import static org.pensatocode.simplicity.util.ArrayUtil.wrapToArray;
import static org.pensatocode.simplicity.util.StringUtil.convertToSnakeCase;

/**
 * Implementation of {@link PagingAndSortingRepository} using {@link JdbcTemplate}
 */
public abstract class AbstractJdbcRepository<T, ID extends Serializable> implements JdbcRepository<T, ID>, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcRepository.class);

    protected TransactionalRowMapper<T> rowMapper;
    protected JdbcTemplate jdbcTemplate;
    protected final TableDescription tableDesc;
    protected SqlGenerator sqlGenerator;
    protected String idName;

    private Boolean initialized = false;
    private LocalDateTime lastUpdated = LocalDateTime.now();
    Long counter = -1L;

    public AbstractJdbcRepository(@Autowired JdbcTemplate jdbcTemplate, TransactionalRowMapper<T> rowMapper, String tableName, String fromClause, Class<T> jClass, String idName) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.idName = idName;
        Field[] allProperties = jClass.getDeclaredFields();
        List<String> columns = new ArrayList<>(allProperties.length-1);
        int i = 0;
        for (Field f: allProperties) {
            if(f.getName().equals(idName)) {
                continue;
            }
            columns.add( convertToSnakeCase(f.getName()) );
        }
        String selectClause = null;
        if (columns.size() > 0) {
            selectClause = Stream
            .concat(List.of(idName).stream(), columns.stream())
            .map(String::valueOf)
            .collect( Collectors.joining( ", " ) );
        }

        String[] ids = { idName };
        this.tableDesc = new TableDescription(tableName, columns, selectClause, fromClause, ids);
        this.sqlGenerator = SqlGeneratorFactory.getInstance().getGenerator(jdbcTemplate);
        log.info("SqlGenerator in " + this.getClass().getSimpleName() +
                " is " + this.sqlGenerator.getClass().getSimpleName());
    }

    public AbstractJdbcRepository(@Autowired JdbcTemplate jdbcTemplate, TransactionalRowMapper<T> rowMapper, String tableName, Class<T> jClass, String idName) {
        this(jdbcTemplate, rowMapper, tableName, null, jClass, idName);
    }

    public AbstractJdbcRepository(@Autowired JdbcTemplate jdbcTemplate, TransactionalRowMapper<T> rowMapper, String fromClause, Class<T> jClass) {
        this(jdbcTemplate, rowMapper, convertToSnakeCase(jClass.getSimpleName()), fromClause, jClass, "id");
    }

    public AbstractJdbcRepository(@Autowired JdbcTemplate jdbcTemplate, TransactionalRowMapper<T> rowMapper, Class<T> jClass) {
        this(jdbcTemplate, rowMapper, convertToSnakeCase(jClass.getSimpleName()), null, jClass, "id");
    }

    @Override
    public void afterPropertiesSet() {
        initialized = true;
    }

    ////////// Repository methods //////////

    @Override
    @Transactional(readOnly=true)
    public Long count() {
        Long count = jdbcTemplate.queryForObject(sqlGenerator.count(tableDesc), Long.class);
        resetCounter(count);
        return count;
    }


    @Override
    @Transactional(readOnly=true)
    public List<T> findAll() {
        return jdbcTemplate.query(sqlGenerator.selectAll(tableDesc), rowMapper);
    }

    @Override
    @Transactional(readOnly=true)
    public List<T> findAll(Iterable<ID> ids) {
        List<ID> idsList = toList(ids);
        if (idsList.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcTemplate.query(sqlGenerator.selectByPK(tableDesc), rowMapper, idsList);
    }

    @Override
    @Transactional(readOnly=true)
    public List<T> findAll(Sort sort) {
        return jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, sort), rowMapper);
    }

    @Override
    @Transactional(readOnly=true)
    public Page<T> findAll(Pageable pageable) {
        List<T> list = jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, pageable), rowMapper);
        if (counter == -1L) {
            count();
        }
        return new PageImpl<>(list, pageable, counter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<T> findAll(String whereClause) {
        return jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, whereClause), rowMapper);
    }

    @Override
    @Transactional(readOnly=true)
    public List<T> findAll(String whereClause, Sort sort) {
        return jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, whereClause, sort), rowMapper);
    }

    @Override
    @Transactional(readOnly=true)
    public Page<T> findAll(String whereClause, Pageable pageable) {
        List<T> list = jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, whereClause, pageable), rowMapper);
        if (counter == -1L) {
            count();
        }
        return new PageImpl<>(list, pageable, counter);
    }

    @Override
    @Transactional(readOnly=true)
    public T findOne(ID id) {
        List<T> resultList =  jdbcTemplate.query(sqlGenerator.selectByPK(tableDesc), wrapToArray(id), rowMapper);
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @Override
    @Transactional(readOnly=true)
    public Boolean exists(ID id) {
        return !jdbcTemplate.queryForList(
            sqlGenerator.existsByPK(tableDesc), wrapToArray(id), Integer.class).isEmpty();
    }

    @Override
    public Integer delete(ID id) {
        Integer lineCount = jdbcTemplate.update(sqlGenerator.deleteByPK(tableDesc), id);
        decreaseCounter();
        return lineCount;
    }

    @Override
    public <S extends T> S save(S entity) {
        ID id = idFromEntity(entity);
        if (id == null || "0".equals(id.toString())) {
            ID result = create(entity);
            setIdToEntity(entity, result);
        } else {
            update(entity);
        }
        return entity;
    }

    @Override
    public <S extends T> List<S> save(Iterable<S> entities) {
        List<S> ret = new ArrayList<>();
        for (S s : entities) {
            ret.add(save(s));
        }
        return ret;
    }

    @Override
    public <S extends T> Integer update(S entity, ID id) {
        ID entityId = idFromEntity(entity);
        Assert.state(entityId == id, "The item you are trying to update is not the same as the pointed repository location.");
        return this.update(entity);
    }

    @Override
    public <S extends T> Integer update(S entity) {
        String updateQuery = sqlGenerator.update(tableDesc);
        Object[] paramValues;
        int[] paramTypes;
        ID idValue = idFromEntity(entity);
        try {
            String[] columnNames = wrapToArray(String.class, tableDesc.getColumns(), idName);
            paramValues = rowMapper.columnsValues(entity, columnNames);
            paramTypes = rowMapper.columnsTypes(columnNames);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
        int rowsAffected = jdbcTemplate.update(updateQuery, paramValues, paramTypes);
        if (rowsAffected < 1) {
            throw new NoRecordUpdatedException(tableDesc.getTableName(), idValue);
        }
        if (rowsAffected > 1) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(updateQuery, 1, rowsAffected);
        }
        return rowsAffected;
    }

    @Override
    public <S extends T> ID create(S entity) {
        Object[] paramValues;
        int[] paramTypes;
        try {
            String[] columnNames = wrapToArray(String.class, tableDesc.getColumns());
            paramValues = rowMapper.columnsValues(entity, columnNames);
            paramTypes = rowMapper.columnsTypes(columnNames);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
        ID id = idFromEntity(entity);
        if (id == null) {
            return null;
        }
        if ("0".equals(id.toString())) {
            id = insertWithAutoGeneratedKey(paramValues, paramTypes, id.getClass());
        } else {
            id = insertWithManuallyAssignedKey(entity, paramValues, paramTypes);
        }
        increaseCounter();
        return id;
    }

    ////////// Couting methods //////////

    public void increaseCounter() {
        counter += 1;
    }

    public void decreaseCounter() {
        counter -= 1;
    }

    private void resetCounter(Long value) {
        this.counter = value;
        this.lastUpdated = LocalDateTime.now();
    }

    ////////// Extra methods //////////

    @SuppressWarnings("unchecked")
    private ID idFromEntity(T entity) {
        PropertyDescriptor propertyDescriptor;
        try {
            propertyDescriptor = new PropertyDescriptor(idName, entity.getClass());
        } catch (IntrospectionException e) {
            e. printStackTrace();
            return null;
        }
        Method getterMethod = propertyDescriptor.getReadMethod();
        try {
            return (ID) getterMethod.invoke(entity);

        } catch (InvocationTargetException | IllegalAccessException | ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setIdToEntity(T entity, ID keyHolder) {
        PropertyDescriptor propertyDescriptor;
        try {
            propertyDescriptor = new PropertyDescriptor(idName, entity.getClass());
        } catch (IntrospectionException e) {
            e. printStackTrace();
            return;
        }
        Method setterMethod = propertyDescriptor.getWriteMethod();
        try {
            setterMethod.invoke(entity, keyHolder);

        } catch (InvocationTargetException | IllegalAccessException | ClassCastException e) {
            e.printStackTrace();
        }
    }

    private <S extends T> ID insertWithManuallyAssignedKey(S entity, Object[] values, int[] types) {
        String insertQuery = sqlGenerator.insert(tableDesc, false);
        jdbcTemplate.update(insertQuery, values, types);
        return idFromEntity(entity);
    }

    private ID insertWithAutoGeneratedKey(Object[] values, int[] types, Class<? extends Serializable> clazz) {
        final String insertQuery = sqlGenerator.insert(tableDesc, true);
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            String idColumnName = tableDesc.getPkColumns().get(0);
            PreparedStatement ps = con.prepareStatement(insertQuery, new String[]{idColumnName});
            for (int i = 0; i < values.length; ++i) {
                ps.setObject(i + 1, values[i], types[i]);
            }
            return ps;
        }, keyHolder);

        return extractValueWithCorrectIdClassType(keyHolder, clazz);
    }

    @SuppressWarnings("unchecked")
    private ID extractValueWithCorrectIdClassType(GeneratedKeyHolder keyHolder, Class<? extends Serializable> clazz) {
        if (keyHolder.getKey() == null) {
            return null;
        }
        if (clazz.getTypeName().equals(Long.class.getTypeName())) {
            return (ID)(Long)keyHolder.getKey().longValue();
        }
        if (clazz.getTypeName().equals(Integer.class.getTypeName())) {
            return (ID)(Integer)keyHolder.getKey().intValue();
        }
        if (clazz.getTypeName().equals(Short.class.getTypeName())) {
            return (ID)(Short)keyHolder.getKey().shortValue();
        }
        if (clazz.getTypeName().equals(Byte.class.getTypeName())) {
            return (ID)(Byte)keyHolder.getKey().byteValue();
        }
        return (ID) keyHolder.getKey();
    }

}
