package com.ues.service;

import com.ues.dto.AccountRequestDTO;
import com.ues.dto.UserDTO;
import com.ues.model.AccountRequest;
import com.ues.model.Location;
import com.ues.model.Manages;
import com.ues.model.User;
import com.ues.model.enums.RequestStatus;
import com.ues.model.enums.UserRole;
import com.ues.repository.AccountRequestRepository;
import com.ues.repository.LocationRepository;
import com.ues.repository.ManagesRepository;
import com.ues.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AccountRequestRepository accountRequestRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ManagesRepository managesRepository;
    private final EmailService emailService;

    public AdminService(AccountRequestRepository accountRequestRepository,
                        UserRepository userRepository,
                        LocationRepository locationRepository,
                        ManagesRepository managesRepository,
                        EmailService emailService) {
        this.accountRequestRepository = accountRequestRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;

        this.managesRepository = managesRepository;
        this.emailService = emailService;
    }

    public List<AccountRequestDTO> getPendingRequests() {
        return accountRequestRepository.findByStatus(RequestStatus.PENDING)
                .stream().map(this::toRequestDTO).collect(Collectors.toList());
    }

    public List<AccountRequestDTO> getAllRequests() {
        return accountRequestRepository.findAll()
                .stream().map(this::toRequestDTO).collect(Collectors.toList());
    }

    @Transactional
    public void approveRequest(Long requestId) {
        AccountRequest request = accountRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .role(UserRole.ROLE_USER)
                .active(true)
                .build();
        userRepository.save(user);

        request.setStatus(RequestStatus.APPROVED);
        accountRequestRepository.save(request);

        emailService.sendRegistrationApproved(request.getEmail(), request.getFirstName());
    }

    @Transactional
    public void rejectRequest(Long requestId) {
        AccountRequest request = accountRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        request.setStatus(RequestStatus.REJECTED);
        accountRequestRepository.save(request);

        emailService.sendRegistrationRejected(request.getEmail(), request.getFirstName());
    }

    @Transactional
    public void addManager(Long locationId, Long userId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (managesRepository.existsByUserAndLocation(user, location)) {
            throw new RuntimeException("User is already a manager of this location");
        }

        Manages manages = Manages.builder().user(user).location(location).build();
        managesRepository.save(manages);

        if (user.getRole() != UserRole.ROLE_ADMIN) {
            user.setRole(UserRole.ROLE_MANAGER);
            userRepository.save(user);
        }
    }

    @Transactional
    public void removeManager(Long locationId, Long userId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Manages manages = managesRepository.findByUserAndLocation(user, location)
                .orElseThrow(() -> new RuntimeException("User is not a manager of this location"));

        managesRepository.delete(manages);

        if (managesRepository.findByUser(user).isEmpty() && user.getRole() != UserRole.ROLE_ADMIN) {
            user.setRole(UserRole.ROLE_USER);
            userRepository.save(user);
        }
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(u -> {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setEmail(u.getEmail());
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
            dto.setUsername(u.getUsername());
            dto.setRole(u.getRole().name());
            return dto;
        }).collect(Collectors.toList());
    }

    private AccountRequestDTO toRequestDTO(AccountRequest request) {
        AccountRequestDTO dto = new AccountRequestDTO();
        dto.setId(request.getId());
        dto.setEmail(request.getEmail());
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setUsername(request.getUsername());
        dto.setStatus(request.getStatus().name());
        dto.setRequestDate(request.getRequestDate());
        return dto;
    }
}
