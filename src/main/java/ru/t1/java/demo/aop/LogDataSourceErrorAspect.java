package ru.t1.java.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.service.DataSourceErrorLogService;

@Aspect
@Component
public class LogDataSourceErrorAspect {

    @Autowired
    private DataSourceErrorLogService errorLogService;

    @AfterThrowing(pointcut = "@annotation(ru.t1.java.demo.aop.annotation.LogDataSourceError)", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String stackTrace = getStackTraceAsString(ex);
        String message = ex.getMessage();
        String methodName = methodSignature.toShortString();

        errorLogService.logError(stackTrace, message, methodName);
    }

    private String getStackTraceAsString(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}