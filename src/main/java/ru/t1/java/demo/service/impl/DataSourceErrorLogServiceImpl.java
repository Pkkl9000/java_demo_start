package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.entity.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;
import ru.t1.java.demo.service.DataSourceErrorLogService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSourceErrorLogServiceImpl implements DataSourceErrorLogService {

    private DataSourceErrorLogRepository errorLogRepository;

    @Override
    public DataSourceErrorLog logError(String stackTrace, String message, String methodSignature) {
        DataSourceErrorLog errorLog = new DataSourceErrorLog();
        errorLog.setStackTrace(stackTrace);
        errorLog.setMessage(message);
        errorLog.setMethodSignature(methodSignature);
        return errorLogRepository.save(errorLog);
    }

    @Override
    public List<DataSourceErrorLog> getAllErrorLogs() {
        return errorLogRepository.findAll();
    }

    @Override
    public DataSourceErrorLog getErrorLogById(Long id) {
        return errorLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error log not found"));
    }

    @Override
    public void deleteErrorLog(Long id) {
        errorLogRepository.deleteById(id);
    }
}
