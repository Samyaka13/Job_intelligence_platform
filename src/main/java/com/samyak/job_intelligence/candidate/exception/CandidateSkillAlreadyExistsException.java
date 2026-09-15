package com.samyak.job_intelligence.candidate.exception;

public class CandidateSkillAlreadyExistsException
        extends RuntimeException {

    public CandidateSkillAlreadyExistsException(String skill) {
        super("Candidate already has skill: " + skill);
    }
}
