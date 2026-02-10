package com.planotech.plano.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BadgeFilterRequest {

    private List<Filter> filters = new ArrayList<>();

    @Data
    public static class Filter {
        private String fieldKey;
        private Operator operator;
        private String value;
    }

    public enum Operator {
        EQUALS,
        CONTAINS
    }
}

