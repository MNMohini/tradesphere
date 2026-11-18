package com.bnagritech.tradesphere.auth.repository;

import com.bnagritech.tradesphere.auth.model.User;

import java.util.List;

public interface UserRepositoryCustom {

    /** Users matching the filter, sorted by full name, at most filter.limit(). */
    List<User> search(UserFilter filter);
}
