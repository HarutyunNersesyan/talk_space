package com.talk_space.modules.discovery.internal.matching;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Randomness changes exposure order, never the score displayed to the user. */
public final class MatchSelector {
    private MatchSelector() {}
    public record Candidate(String name, SimilarityCalculator.Evaluation evaluation) {}
    public static Candidate choose(List<Candidate> candidates, MatchingProperties settings) {
        var shuffled=new ArrayList<>(candidates);
        Collections.shuffle(shuffled,ThreadLocalRandom.current());
        return choose(shuffled,settings,ThreadLocalRandom.current().nextDouble());
    }
    static Candidate choose(List<Candidate> candidates, MatchingProperties settings, double draw) {
        boolean hasExact = candidates.stream().anyMatch(c -> c.evaluation().score()>0 && c.evaluation().primaryExact());
        var ranked = candidates.stream().filter(c -> c.evaluation().score()>0)
            .filter(c -> !hasExact || c.evaluation().primaryExact())
            .sorted(Comparator.comparingDouble((Candidate c) -> c.evaluation().score()).reversed()).toList();
        if (ranked.isEmpty()) return null;
        double minimum = ranked.get(0).evaluation().score() - settings.getDiversityWindow();
        var pool = ranked.stream().filter(c -> c.evaluation().score() >= minimum)
            .limit(settings.getDiversityPool()).toList();
        double total = pool.stream().mapToDouble(c -> c.evaluation().score()).sum();
        double remaining = Math.max(0,Math.min(Math.nextDown(1.0),draw)) * total;
        for (var c : pool) { remaining -= c.evaluation().score(); if (remaining < 0) return c; }
        return pool.get(pool.size()-1);
    }
}
