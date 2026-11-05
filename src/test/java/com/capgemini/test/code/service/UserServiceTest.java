package com.capgemini.test.code.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.clients.CheckDniRequest;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.clients.NotificationClient;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private DniClient dniClient;

  @Mock
  private NotificationClient notificationClient;

  @Mock
  private Room room;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, roomRepository, dniClient, notificationClient);
  }

  @Test
  void createAdminSavesUserAndSendsEmail() {
    CreateUserRequest request = validRequest("admin");
    User savedUser = org.mockito.Mockito.mock(User.class);
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(userRepository.existsByDni(request.dni())).thenReturn(false);
    when(userRepository.existsByPhone(request.phone())).thenReturn(false);
    when(dniClient.check(any(CheckDniRequest.class))).thenReturn(ResponseEntity.ok().build());
    when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
    when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);
    when(savedUser.getId()).thenReturn(42L);

    CreatedUserResponse response = userService.createUser(request);

    assertEquals(42L, response.id());
    verify(notificationClient).sendEmail(new EmailNotificationRequest(request.email(), "usuario guardado"));
    verify(notificationClient, never()).sendSms(any(SmsNotificationRequest.class));
  }

  @Test
  void createSuperadminSendsSmsInsteadOfEmail() {
    CreateUserRequest request = validRequest("superadmin");
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(userRepository.existsByDni(request.dni())).thenReturn(false);
    when(userRepository.existsByPhone(request.phone())).thenReturn(false);
    when(dniClient.check(any(CheckDniRequest.class))).thenReturn(ResponseEntity.ok().build());
    when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
    when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    userService.createUser(request);

    verify(notificationClient).sendSms(new SmsNotificationRequest(request.phone(), "usuario guardado"));
    verify(notificationClient, never()).sendEmail(any(EmailNotificationRequest.class));
  }

  @Test
  void duplicateEmailIsRejectedBeforeExternalCalls() {
    CreateUserRequest request = validRequest("admin");
    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    UserValidationException exception = assertThrows(
        UserValidationException.class,
        () -> userService.createUser(request));

    assertEquals("error validation email", exception.getMessage());
    verify(dniClient, never()).check(any(CheckDniRequest.class));
    verify(userRepository, never()).saveAndFlush(any(User.class));
  }

  @Test
  void duplicateDniIsRejectedBeforeExternalCalls() {
    CreateUserRequest request = validRequest("admin");
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(userRepository.existsByDni(request.dni())).thenReturn(true);

    UserValidationException exception = assertThrows(
        UserValidationException.class,
        () -> userService.createUser(request));

    assertEquals("error validation dni", exception.getMessage());
    verify(dniClient, never()).check(any(CheckDniRequest.class));
    verify(userRepository, never()).saveAndFlush(any(User.class));
  }

  @Test
  void duplicatePhoneIsRejectedBeforeExternalCalls() {
    CreateUserRequest request = validRequest("admin");
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(userRepository.existsByDni(request.dni())).thenReturn(false);
    when(userRepository.existsByPhone(request.phone())).thenReturn(true);

    UserValidationException exception = assertThrows(
        UserValidationException.class,
        () -> userService.createUser(request));

    assertEquals("error validation phone", exception.getMessage());
    verify(dniClient, never()).check(any(CheckDniRequest.class));
    verify(userRepository, never()).saveAndFlush(any(User.class));
  }

  @Test
  void invalidDniIsReturnedAsValidationError() {
    CreateUserRequest request = validRequest("admin");
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(userRepository.existsByDni(request.dni())).thenReturn(false);
    when(userRepository.existsByPhone(request.phone())).thenReturn(false);
    FeignException conflict = org.mockito.Mockito.mock(FeignException.class);
    when(conflict.status()).thenReturn(HttpStatus.CONFLICT.value());
    when(dniClient.check(any(CheckDniRequest.class))).thenThrow(conflict);

    UserValidationException exception = assertThrows(
        UserValidationException.class,
        () -> userService.createUser(request));

    assertEquals("error validation dni", exception.getMessage());
    verify(userRepository, never()).saveAndFlush(any(User.class));
    verify(notificationClient, never()).sendEmail(any(EmailNotificationRequest.class));
  }

  @Test
  void invalidNameIsRejectedWithoutRepositoryOrClientCalls() {
    CreateUserRequest request = new CreateUserRequest(
        "NombreLargo", "name@example.com", "600000001", "admin", "12345678A");

    UserValidationException exception = assertThrows(
        UserValidationException.class,
        () -> userService.createUser(request));

    assertEquals("error validation userName", exception.getMessage());
    verify(userRepository, never()).existsByEmail(any(String.class));
    verify(dniClient, never()).check(any(CheckDniRequest.class));
  }

  @Test
  void missingUserFromRoomOneReturnsNotFound() {
    when(userRepository.findByIdAndRoom_Id(9L, 1L)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> userService.getUserFromRoomOne(9L));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void getUserReturnsUserFromRoomOne() {
    User user = org.mockito.Mockito.mock(User.class);
    when(userRepository.findByIdAndRoom_Id(9L, 1L)).thenReturn(Optional.of(user));
    when(user.getId()).thenReturn(9L);
    when(user.getName()).thenReturn("Pablo");
    when(user.getEmail()).thenReturn("pablo@example.com");
    when(user.getPhone()).thenReturn("600000001");
    when(user.getRol()).thenReturn("admin");
    when(user.getDni()).thenReturn("12345678A");

    UserResponse response = userService.getUserFromRoomOne(9L);

    assertEquals(9L, response.id());
    assertEquals("Pablo", response.name());
    assertEquals("pablo@example.com", response.email());
    assertEquals("600000001", response.phone());
    assertEquals("admin", response.rol());
    assertEquals("12345678A", response.dni());
  }

  private CreateUserRequest validRequest(String role) {
    return new CreateUserRequest(
        "Pablo",
        "user@example.com",
        "600000001",
        role,
        "12345678A");
  }
}
