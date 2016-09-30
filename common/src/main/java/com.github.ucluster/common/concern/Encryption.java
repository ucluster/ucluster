package com.github.ucluster.common.concern;

import org.mindrot.jbcrypt.BCrypt;

public enum Encryption {
    BCRYPT {
        public String encrypt(String content) {
            return BCrypt.hashpw(content, BCrypt.gensalt(4));
        }

        public boolean check(String input, String hashContent) {
            return BCrypt.checkpw(input, hashContent);
        }
    };

    Encryption() {
    }

    public abstract String encrypt(String content);

    public abstract boolean check(String input, String hashContent);
}
