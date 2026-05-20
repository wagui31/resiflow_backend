package com.resiflow.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ApplicationLoggingAspect {

    @Around(
            "execution(public * com.resiflow.controller..*(..))"
                    + " || execution(public * com.resiflow.service..*(..))"
                    + " || execution(public * com.resiflow.security.JwtAuthenticationFilter.*(..))"
                    + " || execution(public * com.resiflow.security.JwtService.*(..))"
                    + " || execution(public * com.resiflow.security.RestAuthenticationEntryPoint.*(..))"
    )
    public Object logMethodExecution(final ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String arguments = LogSanitizer.summarizeArguments(signature.getParameterNames(), joinPoint.getArgs());
        long startedAt = System.nanoTime();

        if (logger.isTraceEnabled()) {
            logger.trace("Entering {} with {}", methodName, arguments.isBlank() ? "no arguments" : arguments);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Executing {}", methodName);
        }

        try {
            Object result = joinPoint.proceed();
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Completed {} in {} ms with result={}",
                        methodName,
                        durationMs,
                        LogSanitizer.summarizeValue(result)
                );
            } else if (logger.isDebugEnabled()) {
                logger.debug("Completed {} in {} ms", methodName, durationMs);
            }

            return result;
        } catch (IllegalArgumentException | IllegalStateException | AccessDeniedException exception) {
            logger.warn(
                    "Business exception in {} after {} ms: {}",
                    methodName,
                    (System.nanoTime() - startedAt) / 1_000_000,
                    exception.getMessage()
            );
            throw exception;
        } catch (RuntimeException exception) {
            logger.error(
                    "Unexpected exception in {} after {} ms. arguments={}",
                    methodName,
                    (System.nanoTime() - startedAt) / 1_000_000,
                    arguments,
                    exception
            );
            throw exception;
        }
    }
}
