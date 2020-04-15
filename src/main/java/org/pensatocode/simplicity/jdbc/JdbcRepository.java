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
package org.pensatocode.simplicity.jdbc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

/**
 * JDBC specific extension of {@link org.springframework.data.repository.Repository}.
 *
 * @param <T> the domain type the repository manages.
 * @param <ID> the type of the id of the entity the repository manages.
 */
@NoRepositoryBean
public interface JdbcRepository<T, ID extends Serializable> {

    Long count();

    Boolean exists(ID id);

    List<T> findAll();

    List<T> findAll(Sort sort);

    Page<T> findAll(Pageable pageable);

    List<T> findAll(String whereClause);

    List<T> findAll(String whereClause, Sort sort);

    Page<T> findAll(String whereClause, Pageable pageable);

    List<T> findAll(Iterable<ID> ids);

    T findOne(ID id);

    Integer delete(ID id);

    <S extends T> S save(S entity);

    <S extends T> List<S> save(Iterable<S> entities);

    <S extends T> ID create(S entity);

    <S extends T> Integer update(S entity);

    <S extends T> Integer update(S entity, ID id);
}
