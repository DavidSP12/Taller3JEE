package com.taller3jee.batch;

import com.taller3jee.domain.*;
import com.taller3jee.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CursoManifestReader manifestReader;
    private final CursoRepository cursoRepository;
    private final ClaseRepository claseRepository;
    private final ContenidoRepository contenidoRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final PreguntaRepository preguntaRepository;
    private final ClaseItemProcessor claseItemProcessor;
    private final ContenidoItemProcessor contenidoItemProcessor;

    @Bean
    public Job cargarCursoJob(Step step1SaveCurso, Step step2SaveClases,
                               Step step3SaveContenidos, Step step4SaveEvaluaciones,
                               Step step5Report) {
        return new JobBuilder("cargarCursoEstructurasDatos", jobRepository)
                .start(step1SaveCurso)
                .next(step2SaveClases)
                .next(step3SaveContenidos)
                .next(step4SaveEvaluaciones)
                .next(step5Report)
                .build();
    }

    @Bean
    public Step step1SaveCurso() {
        return new StepBuilder("step1SaveCurso", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Map<String, Object> manifest = manifestReader.readManifest();
                    Map<String, Object> cursoData = (Map<String, Object>) manifest.get("curso");

                    Curso curso = new Curso();
                    curso.setNombre((String) cursoData.get("nombre"));
                    curso.setDescripcion((String) cursoData.getOrDefault("descripcion", ""));
                    curso.setVersion((String) cursoData.getOrDefault("version", "1.0"));
                    curso.setEstado(EstadoCurso.ACTIVO);
                    curso.setFechaCreacion(LocalDateTime.now());

                    Curso saved = cursoRepository.save(curso);
                    chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext()
                            .putLong("cursoId", saved.getId());

                    log.info("Step1: Saved curso id={} nombre={}", saved.getId(), saved.getNombre());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step2SaveClases() {
        return new StepBuilder("step2SaveClases", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Long cursoId = chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext().getLong("cursoId");
                    Curso curso = cursoRepository.findById(cursoId)
                            .orElseThrow(() -> new RuntimeException("Curso not found: " + cursoId));

                    Map<String, Object> manifest = manifestReader.readManifest();
                    List<Map<String, Object>> clasesData = (List<Map<String, Object>>) manifest.get("clases");

                    Map<Integer, Long> claseNumeroToId = new LinkedHashMap<>();
                    for (Map<String, Object> claseData : clasesData) {
                        Clase clase = claseItemProcessor.process(claseData);
                        if (clase != null) {
                            clase.setCurso(curso);
                            Clase saved = claseRepository.save(clase);
                            claseNumeroToId.put(saved.getNumero(), saved.getId());
                            log.debug("Step2: Saved clase numero={} titulo={}", saved.getNumero(), saved.getTitulo());
                        }
                    }

                    // Store mapping as comma-separated key=value pairs
                    StringBuilder sb = new StringBuilder();
                    claseNumeroToId.forEach((num, id) -> sb.append(num).append("=").append(id).append(","));
                    chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext()
                            .putString("claseMap", sb.toString());

                    log.info("Step2: Saved {} clases", claseNumeroToId.size());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step3SaveContenidos() {
        return new StepBuilder("step3SaveContenidos", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String claseMapStr = chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext().getString("claseMap");
                    Map<Integer, Long> claseMap = parseClaseMap(claseMapStr);

                    Map<String, Object> manifest = manifestReader.readManifest();
                    List<Map<String, Object>> clasesData = (List<Map<String, Object>>) manifest.get("clases");

                    int totalContenidos = 0;
                    for (Map<String, Object> claseData : clasesData) {
                        int numero = ((Number) claseData.get("numero")).intValue();
                        Long claseId = claseMap.get(numero);
                        if (claseId == null) continue;

                        Clase clase = claseRepository.findById(claseId).orElse(null);
                        if (clase == null) continue;

                        List<Map<String, Object>> contenidosData =
                                (List<Map<String, Object>>) claseData.getOrDefault("contenidos", List.of());
                        for (Map<String, Object> cData : contenidosData) {
                            Contenido contenido = contenidoItemProcessor.process(cData);
                            if (contenido != null) {
                                contenido.setClase(clase);
                                contenidoRepository.save(contenido);
                                totalContenidos++;
                            }
                        }
                    }

                    log.info("Step3: Saved {} contenidos", totalContenidos);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step4SaveEvaluaciones() {
        return new StepBuilder("step4SaveEvaluaciones", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String claseMapStr = chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext().getString("claseMap");
                    Map<Integer, Long> claseMap = parseClaseMap(claseMapStr);

                    Map<String, Object> manifest = manifestReader.readManifest();
                    List<Map<String, Object>> clasesData = (List<Map<String, Object>>) manifest.get("clases");

                    int totalEvals = 0;
                    int totalPreguntas = 0;
                    for (Map<String, Object> claseData : clasesData) {
                        int numero = ((Number) claseData.get("numero")).intValue();
                        Long claseId = claseMap.get(numero);
                        if (claseId == null) continue;

                        Clase clase = claseRepository.findById(claseId).orElse(null);
                        if (clase == null) continue;

                        Map<String, Object> evalData = (Map<String, Object>) claseData.get("evaluacion");
                        if (evalData == null) continue;

                        Evaluacion eval = new Evaluacion();
                        eval.setClase(clase);
                        eval.setTitulo((String) evalData.getOrDefault("titulo", "Quiz - " + clase.getTitulo()));
                        eval.setPuntajeMaximo(((Number) evalData.getOrDefault("puntajeMaximo", 100)).intValue());
                        Evaluacion savedEval = evaluacionRepository.save(eval);
                        totalEvals++;

                        List<Map<String, Object>> preguntasData =
                                (List<Map<String, Object>>) evalData.getOrDefault("preguntas", List.of());
                        int orden = 1;
                        for (Map<String, Object> pData : preguntasData) {
                            Pregunta pregunta = new Pregunta();
                            pregunta.setEvaluacion(savedEval);
                            pregunta.setEnunciado((String) pData.get("enunciado"));
                            pregunta.setTipo(TipoPregunta.valueOf(
                                    ((String) pData.getOrDefault("tipo", "MULTIPLE_CHOICE")).toUpperCase()));
                            pregunta.setOrden(orden++);
                            pregunta.setOpciones((List<String>) pData.getOrDefault("opciones", List.of()));
                            pregunta.setRespuestaCorrecta((String) pData.get("respuestaCorrecta"));
                            preguntaRepository.save(pregunta);
                            totalPreguntas++;
                        }
                    }

                    log.info("Step4: Saved {} evaluaciones and {} preguntas", totalEvals, totalPreguntas);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step5Report() {
        return new StepBuilder("step5Report", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    long cursoCount = cursoRepository.count();
                    long claseCount = claseRepository.count();
                    long contenidoCount = contenidoRepository.count();
                    long evalCount = evaluacionRepository.count();
                    long preguntaCount = preguntaRepository.count();

                    log.info("=== BATCH REPORT ===");
                    log.info("Cursos: {}", cursoCount);
                    log.info("Clases: {}", claseCount);
                    log.info("Contenidos: {}", contenidoCount);
                    log.info("Evaluaciones: {}", evalCount);
                    log.info("Preguntas: {}", preguntaCount);
                    log.info("====================");

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Map<Integer, Long> parseClaseMap(String claseMapStr) {
        Map<Integer, Long> map = new LinkedHashMap<>();
        if (claseMapStr != null && !claseMapStr.isBlank()) {
            String[] entries = claseMapStr.split(",");
            for (String entry : entries) {
                if (entry.contains("=")) {
                    String[] kv = entry.split("=");
                    map.put(Integer.parseInt(kv[0].trim()), Long.parseLong(kv[1].trim()));
                }
            }
        }
        return map;
    }
}
