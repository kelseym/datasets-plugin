/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.resolvers.ExpressionResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.resolvers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ExpressionResolver {
    private ExpressionResolver() {
        // No public constructor...
    }

    public static Iterable<String> arrayNodeToStrings(final JsonNode node) {
        return StreamSupport.stream(node.spliterator(), false).map(JsonNode::textValue).collect(Collectors.toList());
    }

    public static String joinClauses(final List<List<String>> clauses) {
        return "(" + clauses.stream().map(expressions -> StringUtils.join(expressions, " OR ")).collect(Collectors.joining(") AND (")) + ")";
    }

    public static List<String> getExpressions(final List<String> attributes, final Iterable<String> values) {
        final List<String> expression = new ArrayList<>();
        for (final List<String> clause : StreamSupport.stream(values.spliterator(), false).map(value -> getExpression(attributes, value)).collect(Collectors.toList())) {
            expression.addAll(clause);
        }
        return expression;
    }

    public static List<String> getExpression(final List<String> attributes, final String value) {
        return getClauses(attributes, getRegexType(value));
    }

    protected static List<String> getClauses(final List<String> attributes, final Pair<RegexType, String> criteria) {
        return attributes.stream().map(attribute -> StringUtils.joinWith(" ", attribute, criteria.getKey().operator(), StringUtils.wrap(criteria.getValue(), "'"))).collect(Collectors.toList());
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
