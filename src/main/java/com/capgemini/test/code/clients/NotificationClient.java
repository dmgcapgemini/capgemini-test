package com.capgemini.test.code.clients;

import com.capgemini.test.code.dto.EmailNotificationRequest;
import com.capgemini.test.code.dto.SmsNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificationClient", url = "${external.service.url}")
public interface NotificationClient {

  @PostMapping("/email")
  void sendEmail(@RequestBody EmailNotificationRequest request);

  @PostMapping("/sms")
  void sendSms(@RequestBody SmsNotificationRequest request);
}
