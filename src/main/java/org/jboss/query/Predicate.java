package org.jboss.query;

import jakarta.annotation.Nonnull;

public enum Predicate {
    AND("AND"),
    OR("OR"),
    IN("IN"),
    EQUAL("="),
    NOT_EQUAL("!="),
    G_THAN(">"),
    GE_THAN(">="),
    L_THAN("<"),
    LE_THAN("<="),
    NOT("~", false),
    EMPTY("IS EMPTY", false);

    private final String value;
    private final boolean binary;

    private Predicate(String value) {
        this(value, true);
    }

    private Predicate(String value, boolean binary) {
        this.value = value;
        this.binary = binary;
    }

    public String apply(@Nonnull String lhs) {
        if (binary) {
            throw new UnsupportedOperationException(
                    "Predicate %s can't be used only with a single operand".formatted(this.value.strip()));
        }

        return "%s %s ".formatted(lhs, value);
    }

    /**
     * @implNote Wraps {@code rhs} into quotation marks '...' if literal, i.e. not a set as (value1, ...)
     */
    public String apply(@Nonnull String lhs, @Nonnull String rhs) {
        if (!binary) {
            throw new UnsupportedOperationException(
                    "Predicate %s can't be used with two operands".formatted(this.value.strip()));
        }

        return " %s %s %s ".formatted(lhs, value, rhs.startsWith("(") ? rhs : "'%s'".formatted(rhs));
    }
}
