package com.samyak.job_intelligence.job.service.requirement;

import com.samyak.job_intelligence.common.normalization.TextNormalizationSupport;
import com.samyak.job_intelligence.job.domain.RequirementType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleBasedRequirementExtractor implements RequirementExtractor{
    private static final Pattern MANDATORY_TECHNOLOGY_PATTERN =
            Pattern.compile(
                    "(?i)^(?:must\\s+have|required|required\\s+skills?|requirements?)\\s*:?\\s+(.+)$"
            );

    private static final Pattern MANDATORY_PREFIX_PATTERN =
            Pattern.compile(
                    "(?i)^(?:must\\s+have|required|required\\s+skills?|requirements?)\\s*:?\\s+(.+)$"
            );

    private static final Pattern MANDATORY_SHORT_PATTERN =
            Pattern.compile(
                    "(?i)^(.+?)\\s+required$"
            );

    private static final Pattern NON_MANDATORY_PATTERN =
            Pattern.compile(
                    "(?i)^\\s*" +
                            "(.+?)" +
                            "\\s+(?:is|are)\\s+" +
                            "(?:a\\s+)?" +
                            "(?:plus|preferred|preferable|desired|recommended|" +
                            "nice\\s+to\\s+have|good\\s+to\\s+have|a\\s+bonus|beneficial)" +
                            "\\s*$"
            );

    private static final Pattern NON_MANDATORY_SHORT_PATTERN =
            Pattern.compile(
                    "(?i)^\\s*" +
                            "(.+?)" +
                            "\\s+(?:optional|nice\\s+to\\s+have|preferred|desired)" +
                            "\\s*$"
            );
    private static final Pattern MANDATORY_SUFFIX_PATTERN =
            Pattern.compile(
                    "(?i)^(.+?)\\s+(?:is|are)\\s+(?:required|mandatory)$"
            );

    private static final Pattern NON_MANDATORY_TECHNOLOGY_PATTERN =
            Pattern.compile(
                    "(?i)(.+?)\\s+(?:is|are)\\s+(?:a\\s+)?(?:plus|preferred|nice to have|good to have)"
            );

    @Override
    public List<ExtractedJobRequirement> extract(String jobDescription){
        if(jobDescription == null || jobDescription.isBlank()) return List.of();
        List<ExtractedJobRequirement> requirements = new ArrayList<>();
        String[] sentences = jobDescription.split("[.!?\\n]");
        for(String sentence : sentences ){
            String trimmedSentence = sentence.trim();

            if (trimmedSentence.isBlank()) {
                continue;
            }
            extractMandatory(sentence,requirements);
            extractNonMandatory(sentence,requirements);
        }
        return requirements;

    }

    public void extractMandatory(String sentence,List<ExtractedJobRequirement> requirements){
        String requirementSection = null;
        Matcher prefixMatcher = MANDATORY_PREFIX_PATTERN.matcher(sentence);
        if(prefixMatcher.find()) {
            requirementSection = prefixMatcher.group(1);
        }

        if (requirementSection == null) {
            Matcher shortMatcher =
                    MANDATORY_SHORT_PATTERN.matcher(sentence);

            if (shortMatcher.find()) {
                requirementSection = shortMatcher.group(1);
            }
        }


        if (requirementSection == null) {
            return;
        }

        for(String value : splitTechnologies(requirementSection)){
            String normalizedValue = TextNormalizationSupport.normalizeWhitespaceAndCase(value);
            if (normalizedValue.isBlank()) {
                continue;
            }
            requirements.add(new ExtractedJobRequirement(RequirementType.TECHNOLOGY,
                    value.trim(),
                    normalizedValue,
                    true,
                    null,
                    sentence.trim()
                    ));
        }

    }

    public void extractNonMandatory(String sentence,List<ExtractedJobRequirement> requirements){

        String requirementSection = null;
        Matcher matcher = NON_MANDATORY_PATTERN.matcher(sentence);
        if(matcher.find()) {
            requirementSection = matcher.group(1);
        }
        if (requirementSection == null) {
            Matcher shortMatcher =
                    NON_MANDATORY_SHORT_PATTERN.matcher(sentence);

            if (shortMatcher.find()) {
                requirementSection = shortMatcher.group(1);
            }
        }


        if (requirementSection == null) {
            return;
        }

        for(String value : splitTechnologies(requirementSection)){
            String normalizedValue = TextNormalizationSupport.normalizeWhitespaceAndCase(value);
            if(normalizedValue.isBlank()) continue;

            requirements.add(new ExtractedJobRequirement(RequirementType.TECHNOLOGY,
                    value.trim(),
                    normalizedValue,
                    false,
                    null,
                    sentence.trim()
                    ));
        }

    }
    private List<String> splitTechnologies(String value) {

        String normalized = value
                .replaceAll("(?i)\\band\\b", ",")
                .replaceAll("(?i)\\bor\\b", ",");

        return List.of(normalized.split(","));
    }


}
