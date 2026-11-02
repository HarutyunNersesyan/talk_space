package com.talk_space.matching;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/** Explainable, symmetric similarity. No demographic or private profile data is used. */
@Component @RequiredArgsConstructor
public class SimilarityCalculator {
    private final MatchingProperties properties;
    public record Trait(long id, Long parentId) {}
    public record Profile(List<Trait> hobbies, List<Trait> specialities) {}
    public record Evaluation(double score, Double hobbies, Double specialities,
                             double hobbyWeight, double specialityWeight, boolean primaryExact) {}

    public Evaluation evaluate(Profile a, Profile b, MatchMode mode) {
        Double hobbies = component(a.hobbies(), b.hobbies());
        Double specialities = component(a.specialities(), b.specialities());
        Double primary = mode == MatchMode.HOBBIES ? hobbies : specialities;
        Double secondary = mode == MatchMode.HOBBIES ? specialities : hobbies;
        double weight = secondary == null ? 1 : properties.getPrimaryWeight();
        double result = primary == null || primary == 0 ? 0
            : weight * primary + (secondary == null ? 0 : (1 - weight) * secondary);
        boolean exact = !commonIds(mode == MatchMode.HOBBIES ? a.hobbies() : a.specialities(),
            mode == MatchMode.HOBBIES ? b.hobbies() : b.specialities()).isEmpty();
        return new Evaluation(result, hobbies, specialities,
            mode == MatchMode.HOBBIES ? weight : 1 - weight,
            mode == MatchMode.SPECIALITIES ? weight : 1 - weight, exact);
    }
    public double score(Profile a, Profile b, MatchMode mode) { return evaluate(a,b,mode).score(); }
    private Double component(List<Trait> a, List<Trait> b) {
        return a.isEmpty() || b.isEmpty() ? null : similarity(a,b);
    }
    public double similarity(List<Trait> a, List<Trait> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        double exact = jaccard(ids(a), ids(b));
        // Keep the existing discounted-parent model as the category component.
        var x = features(a); var y = features(b);
        Set<Long> keys = new HashSet<>(x.keySet()); keys.addAll(y.keySet());
        double intersection = 0, union = 0;
        for (long key : keys) {
            intersection += Math.min(x.getOrDefault(key,0.0), y.getOrDefault(key,0.0));
            union += Math.max(x.getOrDefault(key,0.0), y.getOrDefault(key,0.0));
        }
        double category = union == 0 ? 0 : intersection / union;
        return properties.getExactWeight() * exact + (1 - properties.getExactWeight()) * category;
    }
    private double jaccard(Set<Long> a, Set<Long> b) {
        Set<Long> union = new HashSet<>(a); union.addAll(b);
        Set<Long> intersection = new HashSet<>(a); intersection.retainAll(b);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }
    private Set<Long> ids(List<Trait> traits) { return traits.stream().map(Trait::id).collect(Collectors.toSet()); }
    public Set<Long> commonIds(List<Trait> a, List<Trait> b) {
        Set<Long> common = ids(a); common.retainAll(ids(b)); return common;
    }
    public Set<Long> commonCategories(List<Trait> a, List<Trait> b) {
        Set<Long> common = new HashSet<>(features(a).keySet()); common.retainAll(features(b).keySet());
        common.removeAll(commonIds(a,b)); return common;
    }
    private Map<Long,Double> features(List<Trait> traits) {
        Map<Long,Double> result = new HashMap<>();
        for (Trait t : traits) {
            result.merge(t.id(),1.0,Math::max);
            if (t.parentId()!=null && properties.getParentWeight()>0)
                result.merge(t.parentId(),properties.getParentWeight(),Math::max);
        }
        return result;
    }
}
