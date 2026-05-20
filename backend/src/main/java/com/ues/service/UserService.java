package com.ues.service;

import com.ues.dto.ChangePasswordRequest;
import com.ues.dto.LocationDTO;
import com.ues.dto.ReviewDTO;
import com.ues.dto.UserDTO;
import com.ues.model.Location;
import com.ues.model.Manages;
import com.ues.model.User;
import com.ues.repository.ManagesRepository;
import com.ues.repository.ReviewRepository;
import com.ues.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ManagesRepository managesRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private final LocationService locationService;
    private final ReviewService reviewService;

    public UserService(UserRepository userRepository, ReviewRepository reviewRepository,
                       ManagesRepository managesRepository, PasswordEncoder passwordEncoder,
                       EmailService emailService, FileStorageService fileStorageService,
                       LocationService locationService, ReviewService reviewService) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.managesRepository = managesRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
        this.locationService = locationService;
        this.reviewService = reviewService;
    }

    public UserDTO getProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return toDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(Long userId, String firstName, String lastName,
                                  MultipartFile profileImage) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (profileImage != null && !profileImage.isEmpty()) {
            if (user.getProfileImage() != null) {
                fileStorageService.deleteFile(user.getProfileImage());
            }
            user.setProfileImage(fileStorageService.storeFile(profileImage));
        }
        user = userRepository.save(user);
        logger.info("Profile updated for user: {}", userId);
        return toDTO(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        emailService.sendPasswordChanged(user.getEmail(), user.getFirstName());
        logger.info("Password changed for user: {}", userId);
    }

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setProfileImage(user.getProfileImage());
        dto.setRole(user.getRole().name());

        List<ReviewDTO> reviews = reviewRepository.findByUserNotDeleted(user)
                .stream().map(reviewService::toDTO).collect(Collectors.toList());
        dto.setReviews(reviews);

        List<LocationDTO> managed = managesRepository.findByUser(user)
                .stream().map(m -> locationService.toDTO(m.getLocation())).collect(Collectors.toList());
        dto.setManagedLocations(managed);

        return dto;
    }
}
