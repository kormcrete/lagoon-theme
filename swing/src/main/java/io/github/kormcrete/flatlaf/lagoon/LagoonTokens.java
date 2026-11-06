package io.github.kormcrete.flatlaf.lagoon;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Lagoon design tokens: {@code tokens/lagoon.tokens.json} of this repository, the file the Compose theme is
 * generated from too, carried in the jar next to this class. {@link FlatLagoonModernLaf} takes its colours,
 * corner radii, sizes and font family from here.
 *
 * <p>The file has an {@code identity} (colours, tile colours, radii, font) shared by every platform and
 * {@code metrics} per platform; this class reads {@code metrics.desktop}. Colours are {@code #RRGGBB} or
 * {@code #AARRGGBB}; lengths carry {@code dp}, text sizes {@code sp}, both read as unscaled pixels.
 */
public final class LagoonTokens {

    /** The resource name, in this class's package. */
    public static final String RESOURCE = "lagoon.tokens.json";

    private static volatile LagoonTokens instance;

    private final Map<String, Color> colors;
    private final List<Color[]> tiles;
    private final Map<String, Integer> radii;
    private final Map<String, Integer> lengths;
    private final Map<String, Integer> textSizes;
    private final String fontFamily;

    private LagoonTokens(Map<String, Object> root) {
        Map<String, Object> identity = object(root, "identity");
        Map<String, Object> desktop = object(object(root, "metrics"), "desktop");

        Map<String, Color> colors = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : object(identity, "color").entrySet()) {
            colors.put(e.getKey(), parseColor((String) e.getValue()));
        }
        List<Color[]> tiles = new ArrayList<>();
        for (Object pair : (List<?>) identity.get("tiles")) {
            List<?> p = (List<?>) pair;
            tiles.add(new Color[] {parseColor((String) p.get(0)), parseColor((String) p.get(1))});
        }
        Map<String, Integer> radii = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : object(identity, "radius").entrySet()) {
            radii.put(e.getKey(), size((String) e.getValue(), "dp"));
        }
        Map<String, Integer> lengths = new LinkedHashMap<>();
        Map<String, Integer> textSizes = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : desktop.entrySet()) {
            String value = (String) e.getValue();
            if (value.endsWith("sp")) {
                textSizes.put(e.getKey(), size(value, "sp"));
            } else {
                lengths.put(e.getKey(), size(value, "dp"));
            }
        }
        this.colors = Collections.unmodifiableMap(colors);
        this.tiles = Collections.unmodifiableList(tiles);
        this.radii = Collections.unmodifiableMap(radii);
        this.lengths = Collections.unmodifiableMap(lengths);
        this.textSizes = Collections.unmodifiableMap(textSizes);
        this.fontFamily = (String) object(identity, "font").get("family");
    }

    /** The tokens carried in the jar, read once. */
    public static LagoonTokens get() {
        LagoonTokens tokens = instance;
        if (tokens == null) {
            try (InputStream in = LagoonTokens.class.getResourceAsStream(RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException(RESOURCE + " is missing from the classpath");
                }
                instance = tokens = parse(read(in));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return tokens;
    }

    /** Tokens from the text of a tokens file. */
    public static LagoonTokens parse(String json) {
        Object root = new Json(json).document();
        if (!(root instanceof Map)) {
            throw new IllegalArgumentException("a tokens file is a JSON object");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) root;
        return new LagoonTokens(map);
    }

    /** {@code identity.color}, by name, in the file's order. */
    public Map<String, Color> colors() { return colors; }

    /** One colour of {@code identity.color}, e.g. {@code "primary"}. */
    public Color color(String name) { return require(colors, name); }

    /** {@code identity.tiles}: background and ink pairs for avatar and category tiles. */
    public List<Color[]> tiles() { return tiles; }

    /** {@code identity.radius} in unscaled pixels, by name: the corner radius, half of a FlatLaf {@code arc}. */
    public Map<String, Integer> radii() { return radii; }

    public int radius(String name) { return require(radii, name); }

    /** The {@code dp} values of {@code metrics.desktop} in unscaled pixels, e.g. {@code controlHeight}. */
    public Map<String, Integer> lengths() { return lengths; }

    public int length(String name) { return require(lengths, name); }

    /** The {@code sp} values of {@code metrics.desktop}, e.g. {@code bodySize}. */
    public Map<String, Integer> textSizes() { return textSizes; }

    public int textSize(String name) { return require(textSizes, name); }

    /** {@code identity.font.family}. */
    public String fontFamily() { return fontFamily; }

    private static <V> V require(Map<String, V> map, String name) {
        V value = map.get(name);
        if (value == null) {
            throw new IllegalArgumentException("no token '" + name + "'");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Map<String, Object> parent, String name) {
        Object value = parent.get(name);
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("tokens: '" + name + "' is not an object");
        }
        return (Map<String, Object>) value;
    }

    /** {@code #RRGGBB} or {@code #AARRGGBB}, alpha first as in Compose. */
    static Color parseColor(String value) {
        if (value.startsWith("#") && (value.length() == 7 || value.length() == 9)) {
            long v = Long.parseLong(value.substring(1), 16);
            return value.length() == 7 ? new Color((int) v) : new Color((int) v, true);
        }
        throw new IllegalArgumentException("not a colour: " + value);
    }

    private static int size(String value, String unit) {
        if (!value.endsWith(unit)) {
            throw new IllegalArgumentException("'" + value + "' is not in " + unit);
        }
        double size = Double.parseDouble(value.substring(0, value.length() - unit.length()));
        return (int) Math.round(size);
    }

    private static String read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int n; (n = in.read(buffer)) > 0; ) {
            out.write(buffer, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    /** Just enough JSON: objects (in order), arrays, strings, numbers, true, false, null. */
    static final class Json {
        private final String s;
        private int i;

        Json(String s) { this.s = s; }

        Object document() {
            Object value = value();
            space();
            if (i < s.length()) {
                throw error("unexpected text after the document");
            }
            return value;
        }

        private Object value() {
            space();
            if (i >= s.length()) {
                throw error("unexpected end");
            }
            char c = s.charAt(i);
            switch (c) {
                case '{': return object();
                case '[': return array();
                case '"': return string();
                case 't': return literal("true", Boolean.TRUE);
                case 'f': return literal("false", Boolean.FALSE);
                case 'n': return literal("null", null);
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return number();
                    }
                    throw error("unexpected '" + c + "'");
            }
        }

        private Map<String, Object> object() {
            Map<String, Object> map = new LinkedHashMap<>();
            i++;
            space();
            if (peek() == '}') {
                i++;
                return map;
            }
            while (true) {
                space();
                if (peek() != '"') {
                    throw error("expected a name");
                }
                String name = string();
                space();
                expect(':');
                map.put(name, value());
                space();
                if (peek() == ',') {
                    i++;
                } else {
                    expect('}');
                    return map;
                }
            }
        }

        private List<Object> array() {
            List<Object> list = new ArrayList<>();
            i++;
            space();
            if (peek() == ']') {
                i++;
                return list;
            }
            while (true) {
                list.add(value());
                space();
                if (peek() == ',') {
                    i++;
                } else {
                    expect(']');
                    return list;
                }
            }
        }

        private String string() {
            StringBuilder b = new StringBuilder();
            i++;
            while (true) {
                if (i >= s.length()) {
                    throw error("unterminated string");
                }
                char c = s.charAt(i++);
                if (c == '"') {
                    return b.toString();
                }
                if (c != '\\') {
                    b.append(c);
                    continue;
                }
                if (i >= s.length()) {
                    throw error("unterminated string");
                }
                char e = s.charAt(i++);
                switch (e) {
                    case '"': case '\\': case '/': b.append(e); break;
                    case 'b': b.append('\b'); break;
                    case 'f': b.append('\f'); break;
                    case 'n': b.append('\n'); break;
                    case 'r': b.append('\r'); break;
                    case 't': b.append('\t'); break;
                    case 'u':
                        if (i + 4 > s.length()) {
                            throw error("short \\u escape");
                        }
                        b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default: throw error("bad escape \\" + e);
                }
            }
        }

        private Number number() {
            int start = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
                i++;
            }
            String text = s.substring(start, i);
            try {
                return text.matches("-?\\d+") ? (Number) Long.valueOf(text) : (Number) Double.valueOf(text);
            } catch (NumberFormatException e) {
                throw error("bad number " + text);
            }
        }

        private Object literal(String word, Object value) {
            if (!s.startsWith(word, i)) {
                throw error("unexpected text");
            }
            i += word.length();
            return value;
        }

        private void space() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        private char peek() {
            if (i >= s.length()) {
                throw error("unexpected end");
            }
            return s.charAt(i);
        }

        private void expect(char c) {
            if (peek() != c) {
                throw error("expected '" + c + "'");
            }
            i++;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException("tokens JSON, at " + i + ": " + message);
        }
    }
}
