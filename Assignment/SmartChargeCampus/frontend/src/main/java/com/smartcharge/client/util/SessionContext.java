package com.smartcharge.client.util;

import com.smartcharge.client.model.UserDto;

public class SessionContext {
    private static UserDto currentUser;

    public static UserDto getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserDto user) {
        currentUser = user;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static void clear() {
        currentUser = null;
    }
}
