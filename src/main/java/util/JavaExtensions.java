package util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension
public class JavaExtensions {

    private static final Parser MARKDOWN_PARSER;
    private static final HtmlRenderer HTML_RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        MARKDOWN_PARSER = Parser.builder(options).build();
        HTML_RENDERER = HtmlRenderer.builder(options).build();
    }

    public static String renderMarkdown(String string) {
        if (string == null || string.isBlank()) return "";
        return HTML_RENDERER.render(MARKDOWN_PARSER.parse(string));
    }
    /**
     * This registers the String.capitalise extension method
     */
    public static String capitalise(String string) {
        StringBuilder sb = new StringBuilder();
        for (String part : string.split("\\s+")) {
            if(sb.length() > 0) {
                sb.append(" ");
            }
            if(part.length() > 0) {
            sb.append(part.substring(0, 1).toUpperCase());
            sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    public static String slugify(String string) {
        if (string == null || string.isBlank()) return "";
        return string.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
