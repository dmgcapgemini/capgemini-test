package com.capgemini.test.code.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import feign.okhttp.OkHttpClient;

@FeignClient(
    name = "checkDniClient",
    url = "${external.service.url}",
    configuration = DniClient.FeignConfig.class)
public interface DniClient {

  @Configuration
  class FeignConfig {

    @Bean
    OkHttpClient feignOkHttpClient() {
      return new OkHttpClient();
    }
  }

  @PatchMapping(value = "/check-dni")
  ResponseEntity<CheckDniResponse> check(@RequestBody CheckDniRequest request);
}
