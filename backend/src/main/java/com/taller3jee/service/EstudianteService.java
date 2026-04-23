package com.taller3jee.service;

import com.taller3jee.domain.Estudiante;
import com.taller3jee.domain.EstadoEstudiante;
import com.taller3jee.domain.RolUsuario;
import com.taller3jee.dto.EstudianteDTO;
import com.taller3jee.repository.EstudianteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstudianteService implements UserDetailsService {

    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Estudiante estudiante = estudianteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new User(
                estudiante.getEmail(),
                estudiante.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + estudiante.getRol().name()))
        );
    }

    @Transactional(readOnly = true)
    public Estudiante getEstudiante(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estudiante not found: " + id));
    }

    @Transactional
    public Estudiante register(EstudianteDTO dto) {
        if (estudianteRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        Estudiante e = new Estudiante();
        e.setNombre(dto.getNombre());
        e.setApellido(dto.getApellido());
        e.setEmail(dto.getEmail());
        e.setPassword(passwordEncoder.encode(dto.getPassword()));
        e.setFechaRegistro(LocalDateTime.now());
        e.setEstado(EstadoEstudiante.ACTIVO);
        e.setRol(RolUsuario.ESTUDIANTE);
        return estudianteRepository.save(e);
    }
}
