package com.taller3jee.controller;

import com.taller3jee.dto.AuthRequest;
import com.taller3jee.dto.AuthResponse;
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
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("ESTUDIANTE");
        return ResponseEntity.ok(new AuthResponse(token, request.getEmail(), rol));
    }
}
