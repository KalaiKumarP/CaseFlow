package com.example.Appeal_review.client;

import com.example.Appeal_review.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    UserDTO getUserById(@PathVariable Long userId);
}