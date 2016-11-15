package com.github.ucluster.mongo;

import com.github.ucluster.core.User;

public class Keys {
    public static String user_token(User user) {
        return user.uuid() + ":TOKENS";
    }

    public static String user_code(User user) {
        return user.uuid() + ":CONFIRMATION_CODE";
    }
}
