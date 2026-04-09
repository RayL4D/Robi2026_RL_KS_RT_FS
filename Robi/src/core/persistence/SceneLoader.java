package core.persistence;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserialises a Robi scene JSON file and generates the S-Expressions
 * needed to recreate the scene.
 *
 * <p>Strategy: S-Expressions are generated for the interpreter to execute,
 * avoiding duplication of element-creation logic.
 */
public class SceneLoader {

    /**
     * Converts a scene JSON string into a list of S-Expressions that,
     * once executed, recreate the scene.
     *
     * @param json the JSON scene description
     * @return the ordered list of S-Expressions
     */
    public static List<String> jsonToSExpressions(String json) {
        List<String> commands = new ArrayList<>();

        // Extract space information
        int[] spaceColor = extractColorArray(json, "\"space\"");
        int spaceWidth = extractInt(json, "\"width\"", json.indexOf("\"space\""));
        int spaceHeight = extractInt(json, "\"height\"", json.indexOf("\"space\""));

        commands.add("(space clear)");
        commands.add("(space setColor " + colorToName(spaceColor) + ")");
        if (spaceWidth > 0 && spaceHeight > 0) {
            commands.add("(space setDim " + spaceWidth + " " + spaceHeight + ")");
        }

        // Extract elements
        int elementsStart = json.indexOf("\"elements\"");
        if (elementsStart < 0) {
            return commands;
        }

        // Parse each element object in the array
        int searchFrom = elementsStart;
        while (true) {
            int objStart = json.indexOf("{", searchFrom + 1);
            if (objStart < 0) {
                break;
            }

            // Ensure we are still inside the elements array
            int arrayEnd = findMatchingBracket(json, json.indexOf("[", elementsStart));
            if (objStart >= arrayEnd) {
                break;
            }

            int objEnd = json.indexOf("}", objStart);
            if (objEnd < 0) {
                break;
            }

            String elementJson = json.substring(objStart, objEnd + 1);

            String parent = extractString(elementJson, "\"parent\"");
            String localName = extractString(elementJson, "\"localName\"");
            String type = extractString(elementJson, "\"type\"");
            int[] color = extractColorArray(elementJson, "\"color\"");
            int x = extractInt(elementJson, "\"x\"", 0);
            int y = extractInt(elementJson, "\"y\"", 0);
            int w = extractInt(elementJson, "\"width\"", 20);
            int h = extractInt(elementJson, "\"height\"", 20);

            if (parent != null && localName != null && type != null) {
                // Skip Image elements — they need the file path which is not stored in JSON
                boolean isImage = "Image".equals(type);
                if (!isImage) {
                    int isImgIdx = elementJson.indexOf("\"isImage\"");
                    if (isImgIdx >= 0) {
                        String afterKey = elementJson.substring(isImgIdx);
                        isImage = afterKey.contains("true");
                    }
                }

                if (isImage) {
                    // Images will be handled separately (from command history)
                    continue;
                }

                String fullName = parent + "." + localName;
                commands.add("(" + parent + " add " + localName + " (" + type + " new))");
                commands.add("(" + fullName + " setColor " + colorToName(color) + ")");
                commands.add("(" + fullName + " setDim " + w + " " + h + ")");
                if (x != 0 || y != 0) {
                    commands.add("(" + fullName + " translate " + x + " " + y + ")");
                }
            }

            searchFrom = objEnd;
        }

        return commands;
    }

    // ========================================================================
    // Minimal JSON parser (no external dependencies)
    // ========================================================================

    /**
     * Extracts a string value for the given JSON key.
     *
     * @param json the JSON fragment to search
     * @param key  the quoted key name (e.g. {@code "\"parent\""})
     * @return the extracted string value, or {@code null} if not found
     */
    private static String extractString(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) {
            return null;
        }
        int colonIdx = json.indexOf(":", keyIdx);
        int quoteStart = json.indexOf("\"", colonIdx + 1);
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteStart < 0 || quoteEnd < 0) {
            return null;
        }
        return json.substring(quoteStart + 1, quoteEnd);
    }

    /**
     * Extracts an integer value for the given JSON key, searching from the
     * specified position.
     *
     * @param json       the JSON fragment
     * @param key        the quoted key name
     * @param searchFrom the index to start searching from
     * @return the parsed integer, or {@code 0} on failure
     */
    private static int extractInt(String json, String key, int searchFrom) {
        int keyIdx = json.indexOf(key, searchFrom);
        if (keyIdx < 0) {
            return 0;
        }
        int colonIdx = json.indexOf(":", keyIdx);
        if (colonIdx < 0) {
            return 0;
        }

        // Read digits after the colon
        StringBuilder sb = new StringBuilder();
        for (int i = colonIdx + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '-' || Character.isDigit(c)) {
                sb.append(c);
            } else if (sb.length() > 0) {
                break;
            }
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Extracts a colour array {@code [r, g, b]} from the JSON after the given key.
     *
     * @param json the JSON fragment
     * @param key  the quoted key name preceding the array
     * @return an {@code int[3]} with RGB values, defaulting to blue on failure
     */
    private static int[] extractColorArray(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) {
            return new int[]{0, 0, 255};
        }
        int bracketStart = json.indexOf("[", keyIdx);
        int bracketEnd = json.indexOf("]", bracketStart);
        if (bracketStart < 0 || bracketEnd < 0) {
            return new int[]{0, 0, 255};
        }

        String arrayContent = json.substring(bracketStart + 1, bracketEnd);
        String[] parts = arrayContent.split(",");
        if (parts.length < 3) {
            return new int[]{0, 0, 255};
        }

        try {
            return new int[]{
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim())
            };
        } catch (NumberFormatException e) {
            return new int[]{0, 0, 255};
        }
    }

    /**
     * Finds the index of the closing bracket matching the opening bracket
     * at the given position.
     *
     * @param json    the JSON string
     * @param openIdx the index of the opening {@code [}
     * @return the index of the matching {@code ]}, or the string length if not found
     */
    private static int findMatchingBracket(String json, int openIdx) {
        int depth = 0;
        for (int i = openIdx; i < json.length(); i++) {
            if (json.charAt(i) == '[') {
                depth++;
            } else if (json.charAt(i) == ']') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return json.length();
    }

    /**
     * Converts an RGB array to the closest named colour string.
     *
     * @param rgb an {@code int[3]} with red, green, blue values
     * @return the closest colour name
     */
    private static String colorToName(int[] rgb) {
        return SceneSaver.colorToName(new Color(rgb[0], rgb[1], rgb[2]));
    }
}
