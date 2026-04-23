package com.taller3jee.controller;

import com.taller3jee.domain.Estudiante;
import com.taller3jee.domain.Inscripcion;
import com.taller3jee.dto.AuthRequest;
import com.taller3jee.dto.AuthResponse;
import com.taller3jee.dto.EstudianteDTO;
import com.taller3jee.repository.EstudianteRepository;
import com.taller3jee.repository.InscripcionRepository;
import com.taller3jee.security.JwtUtil;
import com.taller3jee.service.EstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EstudianteService estudianteService;
    private final EstudianteRepository estudianteRepository;
    private final InscripcionRepository inscripcionRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails userDetails = estudianteService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        String rol = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_ESTUDIANTE");

        Estudiante estudiante = estudianteRepository.findByEmail(request.getEmail()).orElseThrow();
        Long inscripcionId = inscripcionRepository.findByEstudianteId(estudiante.getId())
                .stream().findFirst().map(Inscripcion::getId).orElse(null);

        return ResponseEntity.ok(new AuthResponse(token, request.getEmail(), rol, estudiante.getId(), inscripcionId));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody EstudianteDTO request) {
        Estudiante estudiante = estudianteService.register(request);
        UserDetails userDetails = estudianteService.loadUserByUsername(estudiante.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        String rol = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_ESTUDIANTE");
        return ResponseEntity.ok(new AuthResponse(token, estudiante.getEmail(), rol, estudiante.getId(), null));
    }
}

