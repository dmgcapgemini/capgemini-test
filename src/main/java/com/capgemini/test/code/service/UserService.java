package com.capgemini.test.code.service;

import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.clients.CheckDniRequest;
import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.dto.CreatedUserResponse;
import com.capgemini.test.code.dto.EmailNotificationRequest;
import com.capgemini.test.code.dto.SmsNotificationRequest;
import com.capgemini.test.code.dto.UserResponse;
import com.capgemini.test.code.entity.Room;
import com.capgemini.test.code.entity.User;
import com.capgemini.test.code.repository.RoomRepository;
import com.capgemini.test.code.repository.UserRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private static final long ROOM_ID = 1L;
  private static final String SAVED_MESSAGE = "usuario guardado";

  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final DniClient dniClient;
  private final NotificationClient notificationClient;

  public UserService(
      UserRepository userRepository,
      RoomRepository roomRepository,
      DniClient dniClient,
      NotificationClient notificationClient) {
    this.userRepository = userRepository;
    this.roomRepository = roomRepository;
    this.dniClient = dniClient;
    this.notificationClient = notificationClient;
  }

  public CreatedUserResponse createUser(CreateUserRequest request) {
    validate(request);

    if (userRepository.existsByEmail(request.email())) {
      throw new UserValidationException("email");
    }

    validateDni(request.dni());

    Room room = roomRepository.findById(ROOM_ID)
        .orElseThrow(() -> new IllegalStateException("No existe la sala 1"));
    User user = new User(
        request.name(),
        request.email(),
        request.dni(),
        request.phone(),
        request.rol(),
        room);
    User savedUser = userRepository.saveAndFlush(user);

    if ("admin".equals(request.rol())) {
      notificationClient.sendEmail(new EmailNotificationRequest(request.email(), SAVED_MESSAGE));
    } else {
      notificationClient.sendSms(new SmsNotificationRequest(request.phone(), SAVED_MESSAGE));
    }

    return new CreatedUserResponse(savedUser.getId());
  }

  public UserResponse getUserFromRoomOne(Long id) {
    User user = userRepository.findByIdAndRoom_Id(id, ROOM_ID)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

    return new UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getPhone(),
        user.getRol(),
        user.getDni());
  }

  private void validate(CreateUserRequest request) {
    if (request == null || request.name() == null || request.name().isBlank()
        || request.name().length() > 6) {
      throw new UserValidationException("userName");
    }
    if (request.email() == null || !request.email().contains("@") || !request.email().contains(".")) {
      throw new UserValidationException("email");
    }
    if (request.phone() == null || request.phone().isBlank()) {
      throw new UserValidationException("phone");
    }
    if (!"admin".equals(request.rol()) && !"superadmin".equals(request.rol())) {
      throw new UserValidationException("role");
    }
    if (request.dni() == null || request.dni().isBlank()) {
      throw new UserValidationException("dni");
    }
  }

  private void validateDni(String dni) {
    try {
      dniClient.check(new CheckDniRequest(dni));
    } catch (FeignException exception) {
      if (exception.status() == HttpStatus.CONFLICT.value()) {
        throw new UserValidationException("dni");
      }
      logger.warn("DNI validation request failed with HTTP status {}", exception.status(), exception);
      throw new ExternalServiceException("No se pudo validar el DNI", exception);
    }
  }
}
