package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;

import java.util.Map;
import java.util.Optional;

public class MaskConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;

    MaskConcern() {

    }

    MaskConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return Record.Property.Point.DELIVERY == point;
    }

    @Override
    public void effect(Record record, String propertyPath, Record.Property.Point point) {
        final Optional<Record.Property> property = record.property(propertyPath);

        property.ifPresent(prop -> {
            prop.value(mask(String.valueOf(property.get().value())));
        });
    }

    private int getTrailingDigits() {
        return Integer.valueOf(String.valueOf(((Map<String, Object>) configuration).get("trailing")));
    }

    private String mask(String origin) {
        final int digits = getTrailingDigits();

        if (origin.length() <= digits) {
            return star(origin.length());
        }

        return origin.substring(0, origin.length() - digits) + star(digits);
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
        return configuration();
    }
}
