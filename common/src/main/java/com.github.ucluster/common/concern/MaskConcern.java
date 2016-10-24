package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MaskConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;
    private Parser parser;

    MaskConcern() {
    }

    MaskConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.parser = configuration instanceof Map ? new MapParser(configuration) : new ListParser(configuration);
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return Record.Property.Point.DELIVERY == point;
    }

    @Override
    public void effect(Record record, String path, Record.Property.Point point) {
        final Optional<Record.Property> property = record.property(path);

        property.ifPresent(prop -> {
            prop.value(mask(String.valueOf(property.get().value())));
        });
    }

    private String mask(String origin) {
        final int from = consolidatedFrom(origin);
        final int end = consolidatedEnd(origin);

        if (from > end || from < 0 || end > origin.length()) {
            return origin;
        }

        return origin.substring(0, from) + star(end - from + 1) + origin.substring(end + 1);
    }

    private int consolidatedFrom(String origin) {
        if (parser.from() >= 0) {
            return parser.from() >= origin.length() ? origin.length() - 1 : parser.from();
        } else {
            return origin.length() + parser.from() < 0 ? 0 : origin.length() + parser.from();
        }
    }

    private int consolidatedEnd(String origin) {
        if (parser.end() > 0) {
            return parser.end();
        } else {
            return origin.length() + parser.end() < 0 ? 0 : origin.length() + parser.end();
        }
    }

    private String star(int length) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int count = 0; count < length; count++) {
            stringBuffer.append("*");
        }
        return stringBuffer.toString();
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Object configuration() {
        return configuration;
    }

    interface Parser {

        int from();

        int end();
    }

    private static class MapParser implements Parser {
        private Map<String, Object> configuration;

        MapParser(Object configuration) {
            this.configuration = (Map<String, Object>) configuration;
        }

        @Override
        public int from() {
            if (isLeading()) {
                return 0;
            } else if (isTrailing()) {
                return -trailingDigits();
            } else {
                return fromDigits();
            }
        }

        @Override
        public int end() {
            if (isLeading()) {
                return leadingDigits() - 1;
            } else if (isTrailing()) {
                return -1;
            } else {
                return toDigits();
            }
        }

        private boolean isLeading() {
            return configuration.containsKey("leading");
        }

        private boolean isTrailing() {
            return configuration.containsKey("trailing");
        }

        private int fromDigits() {
            return Integer.valueOf(String.valueOf(configuration.get("from")));
        }

        private int toDigits() {
            return Integer.valueOf(String.valueOf(configuration.get("to")));
        }

        private int trailingDigits() {
            return Integer.valueOf(String.valueOf(configuration.get("trailing")));
        }

        private int leadingDigits() {
            return Integer.valueOf(String.valueOf(configuration.get("leading")));
        }
    }

    private static class ListParser implements Parser {

        private List<Integer> configuration;

        ListParser(Object configuration) {
            this.configuration = (List<Integer>) configuration;
        }

        @Override
        public int from() {
            return configuration.get(0);
        }

        @Override
        public int end() {
            return configuration.get(1);
        }
    }
}
