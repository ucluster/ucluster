package com.github.ucluster.session;

import org.slf4j.helpers.MessageFormatter;

public class LogMessageTemplate {
    public static String template(String format, Object... args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }
}
