package com.example.Appeal_review.client;

import com.example.Appeal_review.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MockUserServiceClient implements UserServiceClient {

    // ----------------------------------------------------------------
    // Hardcoded test users — covers all role validation scenarios
    // ----------------------------------------------------------------
    // userId 55  → JUDGE   (valid for review assignment)
    // userId 56  → JUDGE   (second judge for multi-review testing)
    // userId 77  → LAWYER  (should trigger 409 — not a judge)
    // userId 88  → CLERK   (should trigger 409 — not a judge)
    // userId 99  → ADMIN   (should trigger 409 — not a judge)
    // ----------------------------------------------------------------

    @Override
    public UserDTO getUserById(Long userId) {
        log.warn("[MOCK] UserServiceClient.getUserById called — userId={}", userId);

        return switch (userId.intValue()) {
            case 55 -> new UserDTO(55L, "Hon. Justice Arjun Mehta", "JUDGE", "ACTIVE");
            case 56 -> new UserDTO(56L, "Hon. Justice Kavitha Nair", "JUDGE", "ACTIVE");
            case 77 -> new UserDTO(77L, "Adv. Suresh Babu", "LAWYER", "ACTIVE");
            case 88 -> new UserDTO(88L, "Clerk Divya Sharma", "CLERK", "ACTIVE");
            case 99 -> new UserDTO(99L, "Admin Rajan", "ADMIN", "ACTIVE");
            default -> null; // triggers ResourceNotFoundException in service
        };
    }
}