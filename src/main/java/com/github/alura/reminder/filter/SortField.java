package com.github.alura.reminder.filter;

public enum SortField {
    TITLE("title"),
    DATE("remind");

    private final String fieldName;

    SortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
