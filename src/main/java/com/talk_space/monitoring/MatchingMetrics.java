package com.talk_space.monitoring;
import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
@Aspect @Component @RequiredArgsConstructor
public class MatchingMetrics {
    private final MeterRegistry metrics;
    @Around("execution(* com.talk_space.matching.MatchingService.next(..))")
    public Object timed(ProceedingJoinPoint point) throws Throwable {
        Timer.Sample sample = Timer.start(metrics); String outcome = "success";
        try {
            Object result=point.proceed();
            if (result instanceof com.talk_space.model.dto.SearchUser profile && profile.getMatching()!=null) {
                var evaluation=profile.getMatching();
                DistributionSummary.builder("talkspace.matching.similarity").baseUnit("percent")
                    .tag("mode",evaluation.mode()).tag("algorithm",evaluation.algorithmVersion())
                    .register(metrics).record(evaluation.score());
                metrics.counter("talkspace.matching.shown","mode",evaluation.mode(),
                    "kind",evaluation.exactPrimaryMatch() ? "exact" : "category").increment();
            }
            return result;
        }
        catch (Throwable ex) { outcome="failure"; throw ex; }
        finally { sample.stop(Timer.builder("talkspace.matching.duration")
            .tag("mode", point.getArgs()[1].toString()).tag("result", outcome).register(metrics)); }
    }
}
