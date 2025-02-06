package ru.t1.java.demo.service;

import ru.t1.java.demo.entity.DataSourceErrorLog;

import java.util.List;

public interface DataSourceErrorLogService {
    DataSourceErrorLog logError(String stackTrace, String message, String methodSignature);

    List<DataSourceErrorLog> getAllErrorLogs();

    DataSourceErrorLog getErrorLogById(Long id);

    void deleteErrorLog(Long id);
}
