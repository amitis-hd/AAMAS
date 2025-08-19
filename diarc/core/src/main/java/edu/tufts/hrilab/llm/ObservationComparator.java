package edu.tufts.hrilab.llm;

import java.util.*;
import java.util.regex.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObservationComparator {

    private static final ObjectMapper mapper = new ObjectMapper();

    // Parse the expected observation string (e.g., {lightsOff: true, robotPos: _, ...})
    public static Map<String, Object> parseExpectedObservation(String expectedRaw) {
        Map<String, Object> result = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("(\\w+)\\s*:\\s*([^,{}]+)");
        Matcher matcher = pattern.matcher(expectedRaw);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();

            if (value.equals("null")) {
                result.put(key, "null");  // Wildcard
            } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                result.put(key, Boolean.parseBoolean(value));
            } else {
                try {
                    result.put(key, mapper.readValue(value, Object.class)); // Parse number/array/etc
                } catch (Exception e) {
                    result.put(key, value); // fallback to string
                }
            }
        }
        return result;
    }

    /**
     * Compares expected observation string with actual JSON string.
     * @param expectedStr the expected string (e.g. from LLM, with _ as wildcards)
     * @param actualJsonStr raw JSON string returned by GetObservation()
     * @return list of mismatched keys in format: "key: expected=X, actual=Y", or empty list if no mismatch
     */
    public static List<String> getMismatches(String expectedStr, String actualJsonStr) {
        List<String> mismatches = new ArrayList<>();
        try {
            // Option 1: Fix expectedStr to valid JSON if needed
            String fixedExpectedJson = fixToValidJson(expectedStr);

            Map<String, Object> expected = mapper.readValue(fixedExpectedJson, Map.class);
            Map<String, Object> actual = mapper.readValue(actualJsonStr, Map.class);

            if (actual.containsKey("observation") && actual.get("observation") instanceof Map) {
                actual = (Map<String, Object>) actual.get("observation");
            }

            for (Map.Entry<String, Object> entry : expected.entrySet()) {
                String key = entry.getKey();
                Object expectedValue = entry.getValue();
                if (expectedValue == null) continue; 

                Object actualValue = actual.get(key);
                if (!Objects.equals(expectedValue, actualValue)) {
                    mismatches.add("EXPECTED (WRONG): " + key + " = " + expectedValue + "\n OBSERVED (REALITY): " + key + " = " + actualValue + "\n");
                }
            }

        } catch (Exception e) {
            mismatches.add("Error comparing observations: " + e.getMessage());
        }

        return mismatches;
    }

    public static String fixToValidJson(String input) {
        // Add quotes around keys
        String quoted = input.replaceAll("([{,]\\s*)([a-zA-Z0-9_]+)(\\s*:)", "$1\"$2\"$3");
        return quoted;
    }

}
