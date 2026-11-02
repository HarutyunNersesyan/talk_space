package com.talk_space.matching;

import java.util.List;

/** Scores are interest similarities, never probabilities of a successful relationship. */
public record MatchEvaluation(String algorithmVersion, String mode, double score,
        Double hobbyScore, Double specialityScore, double hobbyWeight, double specialityWeight,
        boolean exactPrimaryMatch, List<String> commonHobbies, List<String> commonSpecialities,
        List<String> relatedHobbyCategories, List<String> relatedSpecialityCategories) {
    public static double percent(double score) { return Math.round(score * 1000.0) / 10.0; }
}
