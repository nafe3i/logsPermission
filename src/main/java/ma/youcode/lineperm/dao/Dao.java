package ma.youcode.lineperm.dao;

import java.util.Optional;

interface Dao<T> {

    T save(T entity);

    Optional<T> findById(int id);

    boolean delete(int id);
}
