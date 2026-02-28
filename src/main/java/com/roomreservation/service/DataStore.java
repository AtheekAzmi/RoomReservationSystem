package com.roomreservation.service;

import com.roomreservation.model.User;

public interface DataStore {
    User fetchUser(String username);
}
