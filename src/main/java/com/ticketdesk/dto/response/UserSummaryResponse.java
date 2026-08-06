package com.ticketdesk.dto.response;

import com.ticketdesk.enums.Role;
import lombok.*;

/**
 * Lightweight user summary embedded in ticket/comment responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private Role role;
}
