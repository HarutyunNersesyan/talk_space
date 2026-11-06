package com.talk_space.matching;
import com.talk_space.modules.discovery.internal.matching.MatchingProperties;
import com.talk_space.modules.discovery.api.MatchMode;
import com.talk_space.modules.discovery.internal.matching.SimilarityCalculator;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static com.talk_space.modules.discovery.internal.matching.SimilarityCalculator.*;
class SimilarityCalculatorTest {
    MatchingProperties properties=new MatchingProperties();
    SimilarityCalculator calculator=new SimilarityCalculator(properties);
    List<Trait> traits(Trait... values) { return List.of(values); }
    @Test void exactMatchIsOne() {
        var a=traits(new Trait(1,null)); assertEquals(1,calculator.similarity(a,a));
    }
    @Test void unrelatedProfilesAreZero() {
        assertEquals(0,calculator.similarity(traits(new Trait(1,null)),traits(new Trait(2,null))));
    }
    @Test void siblingCategoriesReceivePartialCredit() {
        double value=calculator.similarity(traits(new Trait(1,10L)),traits(new Trait(2,10L)));
        assertTrue(value>0 && value<1); assertEquals(0.3 * 0.35/2.35,value,1e-10);
    }
    @Test void parentAndChildAreRelated() {
        assertTrue(calculator.similarity(traits(new Trait(10,null)),traits(new Trait(2,10L)))>0);
    }
    @Test void duplicatesDoNotInflateScore() {
        var a=traits(new Trait(1,10L)); assertEquals(calculator.similarity(a,a),calculator.similarity(a,traits(a.get(0),a.get(0))));
    }
    @Test void addingUnrelatedInterestsDoesNotImproveScore() {
        var a=traits(new Trait(1,null));
        assertTrue(calculator.similarity(a,a)>calculator.similarity(a,traits(new Trait(1,null),new Trait(2,null))));
    }
    @Test void missingSecondaryFeaturesAreRenormalized() {
        var a=new Profile(traits(new Trait(1,null)),List.of());
        assertEquals(1,calculator.score(a,a,MatchMode.HOBBIES));
    }
    @Test void primaryModeControlsWeights() {
        var a=new Profile(traits(new Trait(1,null)),traits(new Trait(2,null)));
        var b=new Profile(traits(new Trait(1,null)),traits(new Trait(3,null)));
        assertEquals(0.8,calculator.score(a,b,MatchMode.HOBBIES),1e-10);
        assertEquals(0,calculator.score(a,b,MatchMode.SPECIALITIES));
    }
    @Test void emptyProfilesHaveNoMatch() { assertEquals(0,calculator.similarity(List.of(),List.of())); }
    @Test void missingSecondaryIsNotReportedAsZeroSimilarity() {
        var a=new Profile(traits(new Trait(1,null)),List.of());
        var b=new Profile(traits(new Trait(1,null)),traits(new Trait(8,null)));
        var result=calculator.evaluate(a,b,MatchMode.HOBBIES);
        assertNull(result.specialities()); assertEquals(1,result.hobbyWeight());
        assertEquals(0,result.specialityWeight()); assertEquals(1,result.score());
    }
    @Test void explanationsSeparateExactTraitsAndRelatedCategories() {
        var a=traits(new Trait(1,10L)); var b=traits(new Trait(2,10L));
        assertTrue(calculator.commonIds(a,b).isEmpty());
        assertEquals(java.util.Set.of(10L),calculator.commonCategories(a,b));
        assertTrue(calculator.commonCategories(traits(new Trait(1,null)),traits(new Trait(2,null))).isEmpty());
    }
    @Test void exactOverlapOutscoresCategoryOnlyOverlap() {
        var a=traits(new Trait(1,10L));
        assertTrue(calculator.similarity(a,traits(new Trait(1,10L),new Trait(3,null)))
            >calculator.similarity(a,traits(new Trait(2,10L))));
    }
    @Test void similarityIsSymmetricAndBounded() {
        var a=traits(new Trait(1,10L),new Trait(2,10L)); var b=traits(new Trait(3,10L),new Trait(1,10L));
        double result=calculator.similarity(a,b);
        assertEquals(result,calculator.similarity(b,a)); assertTrue(result>=0 && result<=1);
    }
}
