package com.samyak.job_intelligence.llm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class JobDescriptionCleaner {

    public String clean(String description) {

        if (description == null || description.isBlank()) {
            return description;
        }

        if (!containsHtml(description)) {
            return description;
        }

        Document document = Jsoup.parseBodyFragment(description);

        /*
         * Remove elements that should never be sent to the LLM.
         */
        document.select("script, style, noscript").remove();

        /*
         * Add line breaks around block-level elements so that
         * the resulting text remains readable.
         */
        document.select("br").before("\n");

        Elements blockElements = document.select("p, div, li, h1, h2, h3, h4, h5, h6");
        for (Element element : blockElements) {
            element.prepend("\n");
            element.append("\n");
        }

        String cleaned = document.body().text();

        return cleaned
                .replace('\u00A0', ' ')
                .replaceAll("[ \\t]+", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private boolean containsHtml(String description) {
        return description.matches("(?s).*<\\s*[a-zA-Z][^>]*>.*");
    }
}