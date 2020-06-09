package edu.hnu.mail.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlUtil {

    public static final String header = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=0.5, maximum-scale=2.0, user-scalable=yes\" />\n" +
            "</head>\n" +
            "<body>" +
            "<div style=\"font-size:20px\">";

    public static final String footer = "</div></body>\n" +
            "</html>";

    public static String getNewContent(String html) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByTag("img");
        for (Element element : elements) {
            element.attr("width", "100%").attr("height", "auto");
        }
        return doc.toString();
    }
}
