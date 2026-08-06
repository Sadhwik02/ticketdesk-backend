package com.ticketdesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketdesk.dto.request.CreateTicketRequest;
import com.ticketdesk.dto.response.TicketResponse;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import com.ticketdesk.response.ApiResponse;
import com.ticketdesk.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@DisplayName("TicketController Integration Tests")
class TicketControllerTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;
    @MockBean  private TicketService ticketService;

    @Test
    @DisplayName("POST /tickets — should create ticket and return 201")
    @WithMockUser(username = "employee1", roles = "EMPLOYEE")
    void createTicket_ShouldReturn201_WhenValidRequest() throws Exception {
        final CreateTicketRequest request = CreateTicketRequest.builder()
                .title("My Issue Title")
                .description("Detailed description of the issue that is at least 10 chars")
                .category(TicketCategory.SOFTWARE)
                .priority(TicketPriority.HIGH)
                .build();

        final TicketResponse response = TicketResponse.builder()
                .id(1L).ticketNumber("TKT-2026-000001")
                .title("My Issue Title")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.HIGH)
                .category(TicketCategory.SOFTWARE)
                .build();

        given(ticketService.createTicket(any(CreateTicketRequest.class))).willReturn(response);

        mockMvc.perform(post("/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Ticket created successfully"))
                .andExpect(jsonPath("$.data.ticketNumber").value("TKT-2026-000001"));
    }

    @Test
    @DisplayName("POST /tickets — should return 400 when title is blank")
    @WithMockUser(username = "employee1", roles = "EMPLOYEE")
    void createTicket_ShouldReturn400_WhenTitleBlank() throws Exception {
        final CreateTicketRequest request = CreateTicketRequest.builder()
                .title("").description("description here").category(TicketCategory.SOFTWARE)
                .priority(TicketPriority.HIGH).build();

        mockMvc.perform(post("/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /tickets/{id} — should return 200 when ticket found")
    @WithMockUser(username = "employee1", roles = "EMPLOYEE")
    void getTicketById_ShouldReturn200() throws Exception {
        final TicketResponse response = TicketResponse.builder()
                .id(1L).ticketNumber("TKT-2026-000001").build();

        given(ticketService.getTicketById(1L)).willReturn(response);

        mockMvc.perform(get("/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /tickets/{id} — should return 401 when not authenticated")
    void deleteTicket_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/tickets/1"))
                .andExpect(status().isUnauthorized());
    }
}
