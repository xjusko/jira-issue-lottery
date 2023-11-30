package org.jboss.jql;

import jakarta.annotation.Nonnull;

import java.util.Collection;

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
    EMPTY("IS EMPTY", false),
    NON_EMPTY("IS NOT EMPTY", false);

    private final String value;
    private final boolean binary;

    Predicate(String value) {
        this(value, true);
    }

    Predicate(String value, boolean binary) {
        this.value = value;
        this.binary = binary;
    }

    public String apply(@Nonnull String lhs) {
        if (binary) {
            throw new UnsupportedOperationException(
                    "Predicate %s can't be used only with a single operand".formatted(this.value));
        }

        return " %s %s ".formatted(lhs, value);
    }

    public String apply(@Nonnull String lhs, @Nonnull String rhs) {
        if (!binary) {
            throw new UnsupportedOperationException(
                    "Predicate %s can't be used with two operands".formatted(this.value));
        }

        return " %s %s %s ".formatted(lhs, value, rhs.startsWith("(") ? rhs : Predicate.wrapLiteral(rhs));
    }

    public String apply(@Nonnull String lhs, @Nonnull Collection<?> rhs) {
        if (this != IN) {
            throw new UnsupportedOperationException(
                    "Right hand side expression cant be Collection only for %s".formatted(value));
        }

        if (rhs.size() == 1) {
            return EQUAL.apply(lhs, rhs.stream().findFirst().get().toString());
        }

        return " %s %s (%s) ".formatted(lhs, value, String.join(", ", rhs.stream().map(Predicate::wrapLiteral).toList()));
    }

    private static String wrapLiteral(Object o) {
        return "'%s'".formatted(o);
    }
}
