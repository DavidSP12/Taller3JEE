package com.taller3jee.controller;

import com.taller3jee.dto.SimulacionRequest;
import com.taller3jee.service.SimulacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JobLauncher jobLauncher;
    private final Job cargarCursoJob;
    private final JobExplorer jobExplorer;
    private final SimulacionService simulacionService;

    @PostMapping("/batch/cargar-curso")
    public ResponseEntity<Map<String, Object>> cargarCurso() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(cargarCursoJob, params);
            return ResponseEntity.ok(Map.of(
                    "jobId", execution.getId(),
                    "status", execution.getStatus().toString(),
                    "startTime", execution.getStartTime() != null ? execution.getStartTime().toString() : "N/A"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/batch/jobs/{jobId}/status")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long jobId) {
        JobExecution execution = jobExplorer.getJobExecution(jobId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "jobId", execution.getId(),
                "status", execution.getStatus().toString(),
                "startTime", execution.getStartTime() != null ? execution.getStartTime().toString() : "N/A",
                "endTime", execution.getEndTime() != null ? execution.getEndTime().toString() : "N/A",
                "exitStatus", execution.getExitStatus().getExitCode()
        ));
    }

    @PostMapping("/simulacion")
    public ResponseEntity<Map<String, Object>> ejecutarSimulacion(@RequestBody SimulacionRequest request) {
        Map<String, Object> result = simulacionService.ejecutarSimulacion(request);
        return ResponseEntity.ok(result);
    }
}
