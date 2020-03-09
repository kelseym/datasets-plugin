package org.nrg.xnatx.plugins.collection.resolvers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ExpressionResolver {
    public static Iterable<String> arrayNodeToStrings(final JsonNode node) {
        return Iterables.transform(node, new Function<JsonNode, String>() {
            @Override
            public String apply(final JsonNode node) {
                return node.textValue();
            }
        });
    }

    public String joinClauses(final List<String> clauses) {
        return "(" + StringUtils.join(clauses, ") AND (") + ")";
    }

    public String getExpressions(final List<String> attributes, final Iterable<String> values) {
        return StringUtils.join(Iterables.transform(values, new Function<String, String>() {
            @Override
            public String apply(final String value) {
                return getExpression(attributes, value);
            }
        }), " OR ");
    }

    public String getExpression(final List<String> attributes, final String value) {
        return getClauses(attributes, getRegexType(value));
    }

    protected String getClauses(final List<String> attributes, final Pair<RegexType, String> criteria) {
        return StringUtils.join(Lists.transform(attributes, new Function<String, String>() {
            @Override
            public String apply(final String attribute) {
                return StringUtils.joinWith(" ", attribute, criteria.getKey().operator(), StringUtils.wrap(criteria.getValue(), "'"));
            }
        }), " OR ");
    }

    private Pair<RegexType, String> getRegexType(final String payload) {
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
