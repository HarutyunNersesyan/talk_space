package com.talk_space.monitoring;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class PerformanceMonitor {

    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;
    private static final long VERY_SLOW_QUERY_THRESHOLD_MS = 5000;

    @Pointcut("execution(* com.talk_space.repository..*(..))")
    public void repositoryMethods() {
    }

    @Pointcut("execution(* com.talk_space.service..*(..))")
    public void serviceMethods() {
    }

    @Pointcut("execution(* com.talk_space.api.controller..*(..))")
    public void controllerMethods() {
    }

    @Around("repositoryMethods() || serviceMethods() || controllerMethods()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();

            String methodName = joinPoint.getSignature().toShortString();
            logPerformance(methodName, executionTime);
        }
    }

    private void logPerformance(String methodName, long executionTime) {
        if (executionTime > VERY_SLOW_QUERY_THRESHOLD_MS) {
            log.warn("🚨 VERY SLOW METHOD: {} took {} ms", methodName, executionTime);
        } else if (executionTime > SLOW_QUERY_THRESHOLD_MS) {
            log.warn("⚠️ SLOW METHOD: {} took {} ms", methodName, executionTime);
        } else if (log.isDebugEnabled()) {
            log.debug("✅ Method: {} took {} ms", methodName, executionTime);
        }
    }
}
