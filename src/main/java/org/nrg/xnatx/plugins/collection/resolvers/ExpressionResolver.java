package org.nrg.xnatx.plugins.collection.resolvers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ExpressionResolver {
    private ExpressionResolver() {
        // No public constructor...
    }

    public static Iterable<String> arrayNodeToStrings(final JsonNode node) {
        return Iterables.transform(node, new Function<JsonNode, String>() {
            @Override
            public String apply(final JsonNode node) {
                return node.textValue();
            }
        });
    }

    public static String joinClauses(final List<List<String>> clauses) {
        return "(" + StringUtils.join(Lists.transform(clauses, new Function<List<String>, String>() {
            @Override
            public String apply(final List<String> expressions) {
                return StringUtils.join(expressions, " OR ");
            }
        }), ") AND (") + ")";
    }

    public static List<List<String>> getExpressions(final List<String> attributes, final Iterable<String> values) {
        return Lists.newArrayList(Iterables.transform(values, new Function<String, List<String>>() {
            @Override
            public List<String> apply(final String value) {
                return getExpression(attributes, value);
            }
        }));
    }

    public static List<String> getExpression(final List<String> attributes, final String value) {
        return getClauses(attributes, getRegexType(value));
    }

    public static List<String> getMatchAttributes(final List<String> attributes, final Iterable<String> values) {
        final List<String> matches = new ArrayList<>();
        for (final List<String> clause : getExpressions(attributes, values)) {
            matches.addAll(clause);
        }
        return matches;
    }

    protected static List<String> getClauses(final List<String> attributes, final Pair<RegexType, String> criteria) {
        return Lists.transform(attributes, new Function<String, String>() {
            @Override
            public String apply(final String attribute) {
                return StringUtils.joinWith(" ", attribute, criteria.getKey().operator(), StringUtils.wrap(criteria.getValue(), "'"));
            }
        });
    }

    private static Pair<RegexType, String> getRegexType(final String payload) {
        final boolean startsWithSlash = StringUtils.startsWith(payload, "/");
        final boolean endsWithSlash   = StringUtils.endsWith(payload, "/");
        final boolean endsWithSlashI  = StringUtils.endsWith(payload, "/i");
        final boolean containsPercent = StringUtils.contains(StringUtils.remove(payload, "%%"), "%");
        if ((!startsWithSlash || !endsWithSlash && !endsWithSlashI) && !containsPercent) {
            return Pair.of(RegexType.Plain, payload);
        }
        if (startsWithSlash) {
            return Pair.of(endsWithSlash ? RegexType.CaseSensitive : RegexType.CaseInsensitive, StringUtils.unwrap(endsWithSlash ? payload : StringUtils.removeEnd(payload, "i"), '/'));
        }
        return Pair.of(RegexType.Like, payload);
    }

    private enum RegexType {
        Plain("="),
        CaseInsensitive("~*"),
        CaseSensitive("~"),
        Like("LIKE");

        String operator() {
            return _operator;
        }

        RegexType(final String operator) {
            _operator = operator;
        }

        private final String _operator;
    }
}
