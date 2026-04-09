package core.persistence;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import core.Environment;
import core.Reference;
import graphicLayer.GBounded;
import graphicLayer.GElement;
import graphicLayer.GImage;
import graphicLayer.GString;
import graphicLayer.GSpace;

/**
 * Serialises the current Robi scene state into JSON format.
 *
 * <p>Produced JSON structure:
 * <pre>
 * {
 *   "space": { "color": [r,g,b], "width": w, "height": h },
 *   "elements": [
 *     { "name": "space.robi", "parent": "space", "localName": "robi",
 *       "type": "Rect", "color": [r,g,b], "x": x, "y": y,
 *       "width": w, "height": h }
 *   ]
 * }
 * </pre>
 */
public class SceneSaver {

    /**
     * Serialises the complete scene to a JSON string.
     *
     * @param env   the environment containing all references
     * @param space the root GSpace
     * @return the JSON representation of the scene
     */
    public static String saveToJson(Environment env, GSpace space) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // Save space state
        Color spaceBg = space.getBackground();
        json.append("  \"space\": {\n");
        json.append("    \"color\": ").append(colorToJson(spaceBg)).append(",\n");
        json.append("    \"width\": ").append(space.getWidth()).append(",\n");
        json.append("    \"height\": ").append(space.getHeight()).append("\n");
        json.append("  },\n");

        // Collect graphic elements whose name starts with "space."
        List<ElementInfo> elements = new ArrayList<>();
        Map<String, Reference> all = env.getAll();

        for (Map.Entry<String, Reference> entry : all.entrySet()) {
            String name = entry.getKey();
            Object receiver = entry.getValue().getReceiver();

            if (name.startsWith("space.") && receiver instanceof GElement) {
                elements.add(extractElementInfo(name, receiver));
            }
        }

        // Sort by depth (space.a before space.a.b) for correct recreation order
        elements.sort(Comparator.comparingInt(e -> e.name.split("\\.").length));

        json.append("  \"elements\": [\n");
        for (int i = 0; i < elements.size(); i++) {
            json.append(elements.get(i).toJson());
            if (i < elements.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]\n");

        json.append("}");
        return json.toString();
    }

    /**
     * Extracts element information from a named graphic object.
     *
     * @param fullName the fully-qualified element name
     * @param receiver the graphic object
     * @return the extracted element information
     */
    private static ElementInfo extractElementInfo(String fullName, Object receiver) {
        ElementInfo info = new ElementInfo();
        info.name = fullName;

        // Parent and local name: "space.robi.inner" -> parent="space.robi", localName="inner"
        int lastDot = fullName.lastIndexOf('.');
        info.parent = fullName.substring(0, lastDot);
        info.localName = fullName.substring(lastDot + 1);

        // Type: GRect -> Rect, GOval -> Oval, GString -> Label, GImage -> Image
        String rawType = receiver.getClass().getSimpleName().substring(1); // remove 'G'
        if (receiver instanceof GString) {
            info.type = "Label";
        } else {
            info.type = rawType;
        }

        // Colour (protected field of GElement, accessed via reflection)
        info.color = getElementColor(receiver);

        // Position and dimensions
        if (receiver instanceof GBounded) {
            GBounded bounded = (GBounded) receiver;
            info.x = bounded.getPosition().x;
            info.y = bounded.getPosition().y;
            info.width = bounded.getWidth();
            info.height = bounded.getHeight();
        } else if (receiver instanceof GImage) {
            GImage image = (GImage) receiver;
            info.x = image.getPosition().x;
            info.y = image.getPosition().y;
            info.isImage = true;
            // Image dimensions from the raw Image
            info.width = image.getRawImage().getWidth(null);
            info.height = image.getRawImage().getHeight(null);
        }

        return info;
    }

    /**
     * Reads the colour of a graphic element via reflection.
     *
     * @param element the graphic element
     * @return the element colour, or {@link Color#BLUE} as a fallback
     */
    private static Color getElementColor(Object element) {
        try {
            Field colorField = GElement.class.getDeclaredField("color");
            colorField.setAccessible(true);
            return (Color) colorField.get(element);
        } catch (Exception e) {
            return Color.BLUE;
        }
    }

    /**
     * Converts a {@link Color} to a JSON array string.
     *
     * @param c the colour
     * @return a string of the form {@code [r, g, b]}
     */
    private static String colorToJson(Color c) {
        return "[" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + "]";
    }

    /**
     * Finds the closest named {@link java.awt.Color} constant for the given colour.
     *
     * @param target the colour to match
     * @return the name of the closest standard colour
     */
    public static String colorToName(Color target) {
        String[] names = {
            "black", "blue", "cyan", "darkGray", "gray", "green",
            "lightGray", "magenta", "orange", "pink", "red", "white", "yellow"
        };
        Color[] colors = {
            Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY,
            Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE,
            Color.PINK, Color.RED, Color.WHITE, Color.YELLOW
        };

        int minDist = Integer.MAX_VALUE;
        String closest = "blue";
        for (int i = 0; i < colors.length; i++) {
            int dr = target.getRed() - colors[i].getRed();
            int dg = target.getGreen() - colors[i].getGreen();
            int db = target.getBlue() - colors[i].getBlue();
            int dist = dr * dr + dg * dg + db * db;
            if (dist < minDist) {
                minDist = dist;
                closest = names[i];
            }
        }
        return closest;
    }

    /**
     * Internal data holder for a single graphic element's properties.
     */
    private static class ElementInfo {

        String name;
        String parent;
        String localName;
        String type;
        Color color = Color.BLUE;
        int x;
        int y;
        int width = 20;
        int height = 20;
        boolean isImage = false;

        /**
         * Serialises this element info to a JSON object string.
         *
         * @return the JSON representation
         */
        String toJson() {
            return "    { \"name\": \"" + name + "\", "
                    + "\"parent\": \"" + parent + "\", "
                    + "\"localName\": \"" + localName + "\", "
                    + "\"type\": \"" + type + "\", "
                    + "\"isImage\": " + isImage + ", "
                    + "\"color\": " + SceneSaver.colorToJson(color) + ", "
                    + "\"x\": " + x + ", \"y\": " + y + ", "
                    + "\"width\": " + width + ", \"height\": " + height + " }";
        }
    }
}
