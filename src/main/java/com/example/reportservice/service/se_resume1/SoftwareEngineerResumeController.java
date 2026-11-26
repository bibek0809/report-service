package com.example.reportservice.service.se_resume1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * REST controller for software engineer resume generation.
 */
@Slf4j
@RestController
@RequestMapping("/api/resume/software-engineer")
@RequiredArgsConstructor
public class SoftwareEngineerResumeController {

    private final SoftwareEngineerResumeService resumeService;

    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<Void> generateResume(@Valid @RequestBody Mono<ResumeRequestNew> requestMono,
                                     ServerHttpResponse response) {

        String requestId = UUID.randomUUID().toString();

        return requestMono
                .flatMap(request ->
                        resumeService.generateResumePdf(request)
                                .contextWrite(Context.of("requestId", requestId))
                                .flatMap(bytes -> {
                                    response.getHeaders().setContentType(MediaType.APPLICATION_PDF);
                                    response.getHeaders().setContentDispositionFormData(
                                            "attachment",
                                            resumeService.getOutputFilename(request));
                                    response.getHeaders().set("X-Request-Id", requestId);

                                    DataBuffer buffer = new DefaultDataBufferFactory().wrap(bytes);
                                    return response.writeWith(Flux.just(buffer));
                                })
                )
                .onErrorResume(error -> {
                    log.error("[{}] SE Resume generation error: {}", requestId, error.getMessage());
                    response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return response.setComplete();
                });
    }
}
