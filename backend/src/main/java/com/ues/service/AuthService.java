package com.ues.service;

import com.ues.dto.LoginRequest;
import com.ues.dto.LoginResponse;
import com.ues.dto.RegisterRequest;
import com.ues.model.AccountRequest;
import com.ues.model.User;
import com.ues.model.enums.RequestStatus;
import com.ues.model.enums.UserRole;
import com.ues.repository.AccountRequestRepository;
import com.ues.repository.UserRepository;
import com.ues.security.JwtTokenProvider;
import com.ues.security.UserDetailsImpl;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger logger = LogManager.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       AccountRequestRepository accountRequestRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.accountRequestRepository = accountRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostConstruct
    public void initAdmin() {
        if (!userRepository.existsByEmail("admin@ues.com")) {
            User admin = User.builder()
                    .email("admin@ues.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("System")
                    .username("admin")
                    .role(UserRole.ROLE_ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            logger.info("Default admin user created: admin@ues.com / admin123");
        }
    }

    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for: {}", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail()).orElseThrow();
        logger.info("User logged in successfully: {}", request.getEmail());
        return new LoginResponse(jwt, user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole().name());
    }

    @Transactional
    public void register(RegisterRequest request) {
        logger.info("Registration request from: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail()) ||
                accountRequestRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername()) ||
                accountRequestRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        AccountRequest accountRequest = AccountRequest.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .status(RequestStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build();

        accountRequestRepository.save(accountRequest);
        logger.info("Registration request saved for: {}", request.getEmail());
    }
}
