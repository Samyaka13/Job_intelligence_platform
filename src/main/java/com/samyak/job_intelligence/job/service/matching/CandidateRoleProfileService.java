package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CandidateRoleProfileService {
    private final CandidateSkillRepository candidateSkillRepository;

    public CandidateRoleProfileService(CandidateSkillRepository candidateSkillRepository) {
        this.candidateSkillRepository = candidateSkillRepository;
    }

    public Map<JobRoleFamily, Integer> affinities(CandidateProfile candidate) {
        Map<JobRoleFamily, Integer> result = new EnumMap<>(JobRoleFamily.class);
        for (JobRoleFamily family : JobRoleFamily.values()) result.put(family, 0);
        List<CandidateSkill> skills = candidateSkillRepository.findByCandidateProfileId(candidate.getId());
        for (CandidateSkill skill : skills) {
            String value = skill.getNormalizedSkill().toLowerCase(Locale.ROOT);
            int weight = weight(skill);
            add(result, JobRoleFamily.BACKEND_ENGINEERING, weight, value, "java", "spring", "hibernate", "sql", "postgresql", "rest api", "docker", "maven", "junit", "mockito");
            add(result, JobRoleFamily.FULL_STACK_ENGINEERING, weight, value, "javascript", "typescript", "react", "next.js", "node.js", "express.js", "react native");
            add(result, JobRoleFamily.FRONTEND_ENGINEERING, weight, value, "javascript", "typescript", "react", "next.js", "react native");
            add(result, JobRoleFamily.AI_APPLICATION_ENGINEERING, weight, value, "langchain", "langgraph", "ai agents", "llm integration", "gemini api", "api integration");
            // SQL and AI application tooling make data science adjacent, but do not
            // claim the candidate has ML/statistics experience they never provided.
            add(result, JobRoleFamily.DATA_SCIENCE, Math.min(10, weight), value, "sql", "ai agents", "llm integration");
            add(result, JobRoleFamily.DATA_ENGINEERING, Math.min(12, weight), value, "sql", "postgresql", "database design");
        }
        result.replaceAll((family, score) -> Math.min(100, score));
        return result;
    }

    private int weight(CandidateSkill skill) {
        int weight = skill.isPrimary() ? 20 : 12;
        if (skill.getYearsExperience() != null && skill.getYearsExperience().compareTo(BigDecimal.ONE) >= 0) weight += 8;
        return weight;
    }

    private void add(Map<JobRoleFamily, Integer> scores, JobRoleFamily family, int weight, String value, String... markers) {
        for (String marker : markers) {
            if (value.equals(marker)) {
                scores.merge(family, weight, Integer::sum);
                return;
            }
        }
    }
}
