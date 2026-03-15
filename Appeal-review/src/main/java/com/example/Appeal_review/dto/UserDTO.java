package com.example.Appeal_review.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String name;
    private String role;   // LITIGANT, LAWYER, JUDGE, CLERK, ADMIN
    private String status;
}