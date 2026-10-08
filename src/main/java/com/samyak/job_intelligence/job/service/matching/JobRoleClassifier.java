package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.job.domain.Job;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class JobRoleClassifier {

    public JobRoleFamily classify(Job job) {
        String text = (job.getTitle() + " " + job.getDescription()).toLowerCase(Locale.ROOT);
        String title = job.getTitle().toLowerCase(Locale.ROOT);
        if (contains(title, "value engineer", "solution consultant", "transformation", "process optimisation", "process optimization", "management consultant")) return JobRoleFamily.CONSULTING_VALUE_ENGINEERING;
        if (contains(title, "sales engineer", "solutions engineer", "pre-sales", "presales")) return JobRoleFamily.SALES_ENGINEERING;
        if (contains(title, "data scientist", "quantitative analyst", "machine learning scientist")) return JobRoleFamily.DATA_SCIENCE;
        if (contains(title, "data engineer", "analytics engineer")) return JobRoleFamily.DATA_ENGINEERING;
        if (contains(title, "devops", "site reliability", "platform engineer", "sre")) return JobRoleFamily.PLATFORM_DEVOPS;
        if (contains(title, "frontend", "front end", "ui engineer")) return JobRoleFamily.FRONTEND_ENGINEERING;
        if (contains(title, "full stack", "fullstack")) return JobRoleFamily.FULL_STACK_ENGINEERING;
        if (contains(title, "ai engineer", "llm engineer", "applied ai")) return JobRoleFamily.AI_APPLICATION_ENGINEERING;
        if (contains(title, "backend", "back end", "software engineer", "software developer")
                && !contains(text, "value journey", "customer workshops", "sales teams")) return JobRoleFamily.BACKEND_ENGINEERING;
        return JobRoleFamily.UNKNOWN;
    }

    private boolean contains(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }
}
