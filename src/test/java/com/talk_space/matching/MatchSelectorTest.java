package com.talk_space.matching;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class MatchSelectorTest {
    MatchingProperties props=new MatchingProperties();
    MatchSelector.Candidate candidate(String name,double score,boolean exact) {
        return new MatchSelector.Candidate(name,new SimilarityCalculator.Evaluation(score,score,null,1,0,exact));
    }
    @Test void categoryOnlyCannotDisplaceExactMatch() {
        var exact=candidate("exact",0.4,true); var category=candidate("category",0.9,false);
        assertEquals(exact,MatchSelector.choose(List.of(category,exact),props,0.99));
    }
    @Test void diversityNeverSelectsLowSimilarityOutlier() {
        var best=candidate("best",1,true);var weak=candidate("weak",0.5,true);
        assertEquals(best,MatchSelector.choose(List.of(weak,best),props,0.999));
    }
    @Test void similarCandidatesBothHaveAChance() {
        var a=candidate("a",0.9,true);var b=candidate("b",0.88,true);
        assertEquals(a,MatchSelector.choose(List.of(a,b),props,0));
        assertEquals(b,MatchSelector.choose(List.of(a,b),props,0.999));
    }
    @Test void emptyAndZeroScorePoolsHaveNoCandidate() {
        assertNull(MatchSelector.choose(List.of(),props,0));
        assertNull(MatchSelector.choose(List.of(candidate("zero",0,false)),props,0));
    }
}
