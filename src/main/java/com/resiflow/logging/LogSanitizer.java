package com.resiflow.logging;

import com.resiflow.security.AuthenticatedUser;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class LogSanitizer {

    private static final int MAX_VALUE_LENGTH = 120;

    private LogSanitizer() {
    }

    public static String summarizeArguments(final String[] parameterNames, final Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (int index = 0; index < arguments.length; index++) {
            String parameterName = parameterNames != null && index < parameterNames.length
                    ? parameterNames[index]
                    : "arg" + index;
            joiner.add(parameterName + "=" + summarizeValue(parameterName, arguments[index]));
        }
        return joiner.toString();
    }

    public static String summarizeValue(final String name, final Object value) {
        if (isSensitive(name)) {
            return "<redacted>";
        }
        return summarizeValue(value);
    }

    public static String summarizeValue(final Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof AuthenticatedUser authenticatedUser) {
            return "AuthenticatedUser{userId=%s, residenceId=%s, role=%s}".formatted(
                    authenticatedUser.userId(),
                    authenticatedUser.residenceId(),
                    authenticatedUser.role()
            );
        }
        if (value instanceof CharSequence
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof Temporal) {
            return truncate(value.toString());
        }
        if (value instanceof Collection<?> collection) {
            return value.getClass().getSimpleName() + "(size=" + collection.size() + ")";
        }
        if (value instanceof Map<?, ?> map) {
            return value.getClass().getSimpleName() + "(size=" + map.size() + ")";
        }
        if (value.getClass().isArray()) {
            return value.getClass().getComponentType().getSimpleName() + "[]"
                    + "(size=" + Array.getLength(value) + ")";
        }

        Map<String, Object> identifiers = extractIdentifiers(value);
        if (!identifiers.isEmpty()) {
            return value.getClass().getSimpleName() + identifiers;
        }
        return value.getClass().getSimpleName();
    }

    private static Map<String, Object> extractIdentifiers(final Object value) {
        Map<String, Object> identifiers = new LinkedHashMap<>();
        addIfPresent(identifiers, "id", invokeGetter(value, "getId"));
        addIfPresent(identifiers, "email", invokeGetter(value, "getEmail"));
        addIfPresent(identifiers, "residenceId", invokeGetter(value, "getResidenceId"));
        addIfPresent(identifiers, "logementId", invokeGetter(value, "getLogementId"));
        addIfPresent(identifiers, "status", invokeGetter(value, "getStatus"));
        return identifiers;
    }

    private static void addIfPresent(final Map<String, Object> target, final String key, final Object value) {
        if (value != null) {
            target.put(key, truncate(value.toString()));
        }
    }

    private static Object invokeGetter(final Object value, final String methodName) {
        try {
            Method method = value.getClass().getMethod(methodName);
            return method.invoke(value);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static boolean isSensitive(final String name) {
        if (name == null) {
            return false;
        }
        String normalized = name.toLowerCase();
        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("authorization")
                || normalized.contains("captcha");
    }

    private static String truncate(final String value) {
        if (value == null || value.length() <= MAX_VALUE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_VALUE_LENGTH) + "...";
    }
}
