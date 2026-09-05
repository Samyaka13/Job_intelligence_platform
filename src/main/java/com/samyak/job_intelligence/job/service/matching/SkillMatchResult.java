package com.samyak.job_intelligence.job.service.matching;

import java.util.List;

public record SkillMatchResult(
        List<String> matchedSkill,
        List<String> missingSkill,
        List<String> missingMandatorySkills,
        int totalRequirements,
        int mandatoryRequirements

) {
}
