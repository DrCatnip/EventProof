package com.eventproof.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDAO<T> {
    T add(T entity);
    Optional<T> findById(long id);
    List<T> getAll();
    T update(T entity);
    boolean delete(long id);
}
