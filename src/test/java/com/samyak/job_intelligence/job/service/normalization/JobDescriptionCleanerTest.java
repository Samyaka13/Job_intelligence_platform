package com.samyak.job_intelligence.llm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobDescriptionCleanerTest {

    @Test
    void shouldReturnPlainTextUnchanged() {

        String description = """
                8+ years experience in Sales, Business Operations, or Business Development.
                """;

        String result = JobDescriptionCleaner.clean(description);

        assertEquals(description, result);
    }

    @Test
    void shouldRemoveLiteralHtml() {

        String description = """
                <p>Candidates should have <strong>8+ years</strong> of experience.</p>
                """;

        String result = JobDescriptionCleaner.clean(description);

        assertEquals(
                "Candidates should have 8+ years of experience.",
                result
        );
    }

    @Test
    void shouldCleanEscapedHtmlFromAirbnbDescription() {

        String description = """
                &lt;div class=&quot;content-intro&quot;&gt;
                &lt;p&gt;
                Airbnb was born in 2007 when two hosts welcomed three guests to their San Francisco home.
                &lt;/p&gt;

                &lt;p&gt;&lt;strong&gt;Your expertise:&lt;/strong&gt;&lt;/p&gt;

                &lt;ul&gt;
                &lt;li&gt;&lt;strong&gt;8+&lt;/strong&gt; years experience in Sales, Business Operations, or Business Development in Tech, Real Estate/Property Management, or Travel&amp;nbsp;&lt;/li&gt;
                &lt;/ul&gt;

                &lt;/div&gt;
                """;

        String result = JobDescriptionCleaner.clean(description);

        assertTrue(result.contains("8+ years experience"));

        assertFalse(result.contains("&lt;"));
        assertFalse(result.contains("&gt;"));
        assertFalse(result.contains("&amp;"));

        assertFalse(result.contains("<strong>"));
        assertFalse(result.contains("</strong>"));
        assertFalse(result.contains("<li>"));
        assertFalse(result.contains("</li>"));
    }
}