package cn.iocoder.yudao.module.wujin.search;

import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchRuleConfigDO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WujinSearchRuleEngine {

    public static final String RULE_TYPE_GRANULARITY_LIMIT = "GRANULARITY_LIMIT";
    public static final String RULE_TYPE_LANE_OVERRIDE = "LANE_OVERRIDE";
    public static final String RULE_TYPE_RISK_WARNING = "RISK_WARNING";

    private final Map<String, Condition> conditionCache = new HashMap<>();
    private final List<RuleHandler> handlers = new ArrayList<>();

    public WujinSearchRuleEngine() {
        handlers.add(new GranularityLimitRuleHandler());
        handlers.add(new LaneOverrideRuleHandler());
        handlers.add(new RiskWarningRuleHandler());
    }

    public WujinSearchRuleResult apply(WujinSearchRuleContext context, int granularity, WujinLane lane,
                                       boolean riskWarningRequired, String riskWarningText, String explanation,
                                       List<WujinSearchRuleConfigDO> rules) {
        WujinSearchRuleResult current = new WujinSearchRuleResult(granularity, lane, riskWarningRequired,
                riskWarningText, explanation);
        List<WujinSearchRuleConfigDO> sortedRules = sortRules(rules);
        for (WujinSearchRuleConfigDO rule : sortedRules) {
            if (!matchesBaseScope(rule, context)) {
                continue;
            }
            String expression = option(rule.getRuleValue()).get("when");
            if (!isBlank(expression) && !compile(expression).matches(context)) {
                continue;
            }
            RuleHandler handler = findHandler(rule.getRuleType());
            if (handler != null) {
                current = handler.apply(context, current, rule);
            }
        }
        return current;
    }

    public int getCompiledConditionCacheSize() {
        return conditionCache.size();
    }

    private List<WujinSearchRuleConfigDO> sortRules(List<WujinSearchRuleConfigDO> rules) {
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }
        List<WujinSearchRuleConfigDO> sorted = new ArrayList<>(rules);
        Collections.sort(sorted, (left, right) -> safeWeight(right) - safeWeight(left));
        return sorted;
    }

    private RuleHandler findHandler(String ruleType) {
        for (RuleHandler handler : handlers) {
            if (handler.supports(ruleType)) {
                return handler;
            }
        }
        return null;
    }

    private boolean matchesBaseScope(WujinSearchRuleConfigDO rule, WujinSearchRuleContext context) {
        if (!isBlank(rule.getLane()) && (context.getLane() == null || !rule.getLane().equals(context.getLane().name()))) {
            return false;
        }
        return isBlank(rule.getIndustryCode()) || rule.getIndustryCode().equals(context.getIndustry());
    }

    private Condition compile(String expression) {
        Condition cached = conditionCache.get(expression);
        if (cached != null) {
            return cached;
        }
        Condition compiled = new ConditionParser(expression).parse();
        conditionCache.put(expression, compiled);
        return compiled;
    }

    private static int safeWeight(WujinSearchRuleConfigDO rule) {
        return rule.getWeight() == null ? 0 : rule.getWeight();
    }

    private static Integer parseInt(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static Map<String, String> option(String ruleValue) {
        Map<String, String> values = new HashMap<>();
        if (ruleValue == null) {
            return values;
        }
        String[] parts = ruleValue.split(";");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i] == null ? "" : parts[i].trim();
            if (part.isEmpty()) {
                continue;
            }
            int index = part.indexOf('=');
            if (index > 0) {
                values.put(part.substring(0, index).trim(), trimQuote(part.substring(index + 1).trim()));
            } else if (!values.containsKey("value")) {
                values.put("value", trimQuote(part));
            }
        }
        return values;
    }

    private static String trimQuote(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private interface RuleHandler {

        boolean supports(String ruleType);

        WujinSearchRuleResult apply(WujinSearchRuleContext context, WujinSearchRuleResult current,
                                    WujinSearchRuleConfigDO rule);
    }

    private static class GranularityLimitRuleHandler implements RuleHandler {

        @Override
        public boolean supports(String ruleType) {
            return RULE_TYPE_GRANULARITY_LIMIT.equals(ruleType);
        }

        @Override
        public WujinSearchRuleResult apply(WujinSearchRuleContext context, WujinSearchRuleResult current,
                                           WujinSearchRuleConfigDO rule) {
            Integer granularity = parseInt(option(rule.getRuleValue()).get("value"));
            if (granularity == null) {
                return current;
            }
            int value = Math.max(1, Math.min(3, granularity));
            return new WujinSearchRuleResult(value, current.getLane(), current.isRiskWarningRequired(),
                    current.getRiskWarningText(), appendExplanation(current.getExplanation(), rule));
        }
    }

    private static class LaneOverrideRuleHandler implements RuleHandler {

        @Override
        public boolean supports(String ruleType) {
            return RULE_TYPE_LANE_OVERRIDE.equals(ruleType);
        }

        @Override
        public WujinSearchRuleResult apply(WujinSearchRuleContext context, WujinSearchRuleResult current,
                                           WujinSearchRuleConfigDO rule) {
            String laneValue = option(rule.getRuleValue()).get("lane");
            WujinLane lane = parseLane(laneValue, current.getLane());
            return new WujinSearchRuleResult(current.getGranularity(), lane, current.isRiskWarningRequired(),
                    current.getRiskWarningText(), appendExplanation(current.getExplanation(), rule));
        }
    }

    private static class RiskWarningRuleHandler implements RuleHandler {

        @Override
        public boolean supports(String ruleType) {
            return RULE_TYPE_RISK_WARNING.equals(ruleType);
        }

        @Override
        public WujinSearchRuleResult apply(WujinSearchRuleContext context, WujinSearchRuleResult current,
                                           WujinSearchRuleConfigDO rule) {
            String text = option(rule.getRuleValue()).get("text");
            return new WujinSearchRuleResult(current.getGranularity(), current.getLane(), true,
                    isBlank(text) ? current.getRiskWarningText() : text,
                    appendExplanation(current.getExplanation(), rule));
        }
    }

    private static WujinLane parseLane(String laneValue, WujinLane defaultValue) {
        if (isBlank(laneValue)) {
            return defaultValue;
        }
        try {
            return WujinLane.valueOf(laneValue);
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }

    private static String appendExplanation(String explanation, WujinSearchRuleConfigDO rule) {
        return explanation + "；搜索规则配置命中：" + rule.getRuleType();
    }

    private interface Condition {

        boolean matches(WujinSearchRuleContext context);
    }

    private static class ConditionParser {

        private final List<String> tokens;
        private int index;

        ConditionParser(String expression) {
            this.tokens = tokenize(expression);
        }

        Condition parse() {
            Condition condition = parseOr();
            return condition == null ? context -> true : condition;
        }

        private Condition parseOr() {
            Condition left = parseAnd();
            while (match("or")) {
                Condition right = parseAnd();
                left = new OrCondition(left, right);
            }
            return left;
        }

        private Condition parseAnd() {
            Condition left = parseFactor();
            while (match("and")) {
                Condition right = parseFactor();
                left = new AndCondition(left, right);
            }
            return left;
        }

        private Condition parseFactor() {
            if (match("(")) {
                Condition condition = parseOr();
                match(")");
                return condition;
            }
            return parseComparison();
        }

        private Condition parseComparison() {
            String field = next();
            String operator = next();
            String value = next();
            if (field == null || operator == null || value == null) {
                return context -> false;
            }
            return new CompareCondition(field, operator, value);
        }

        private boolean match(String expected) {
            if (index < tokens.size() && expected.equalsIgnoreCase(tokens.get(index))) {
                index++;
                return true;
            }
            return false;
        }

        private String next() {
            if (index >= tokens.size()) {
                return null;
            }
            return tokens.get(index++);
        }

        private static List<String> tokenize(String expression) {
            List<String> tokens = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean inQuote = false;
            char quote = 0;
            for (int i = 0; i < expression.length(); i++) {
                char ch = expression.charAt(i);
                if (inQuote) {
                    if (ch == quote) {
                        tokens.add(current.toString());
                        current.setLength(0);
                        inQuote = false;
                    } else {
                        current.append(ch);
                    }
                    continue;
                }
                if (ch == '\'' || ch == '"') {
                    flush(tokens, current);
                    inQuote = true;
                    quote = ch;
                    continue;
                }
                if (Character.isWhitespace(ch)) {
                    flush(tokens, current);
                    continue;
                }
                if (ch == '(' || ch == ')') {
                    flush(tokens, current);
                    tokens.add(String.valueOf(ch));
                    continue;
                }
                if ((ch == '=' || ch == '!') && i + 1 < expression.length() && expression.charAt(i + 1) == '=') {
                    flush(tokens, current);
                    tokens.add(expression.substring(i, i + 2));
                    i++;
                    continue;
                }
                current.append(ch);
            }
            flush(tokens, current);
            return tokens;
        }

        private static void flush(List<String> tokens, StringBuilder current) {
            if (current.length() > 0) {
                tokens.add(current.toString());
                current.setLength(0);
            }
        }
    }

    private static class AndCondition implements Condition {
        private final Condition left;
        private final Condition right;

        AndCondition(Condition left, Condition right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean matches(WujinSearchRuleContext context) {
            return left.matches(context) && right.matches(context);
        }
    }

    private static class OrCondition implements Condition {
        private final Condition left;
        private final Condition right;

        OrCondition(Condition left, Condition right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean matches(WujinSearchRuleContext context) {
            return left.matches(context) || right.matches(context);
        }
    }

    private static class CompareCondition implements Condition {
        private final String field;
        private final String operator;
        private final String expected;

        CompareCondition(String field, String operator, String expected) {
            this.field = field;
            this.operator = operator;
            this.expected = expected;
        }

        @Override
        public boolean matches(WujinSearchRuleContext context) {
            String actual = context.valueOf(field);
            if ("contains".equalsIgnoreCase(operator)) {
                return actual != null && actual.contains(expected);
            }
            if ("==".equals(operator)) {
                return expected.equals(actual);
            }
            if ("!=".equals(operator)) {
                return actual == null || !expected.equals(actual);
            }
            return false;
        }
    }
}
