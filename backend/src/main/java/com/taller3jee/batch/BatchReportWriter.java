package com.taller3jee.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BatchReportWriter implements ItemWriter<String> {

    private int successCount = 0;
    private int errorCount = 0;

    @Override
    public void write(Chunk<? extends String> items) {
        for (String message : items) {
            if (message.startsWith("ERROR:")) {
                errorCount++;
                log.error("[Batch Report] {}", message);
            } else {
                successCount++;
                log.info("[Batch Report] {}", message);
            }
        }
        log.info("[Batch Summary] Processed {} successes, {} errors", successCount, errorCount);
    }

    public void reset() {
        successCount = 0;
        errorCount = 0;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }
}
