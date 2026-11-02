package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.repository.AvatarRepository;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvatarController.class)
class AvatarControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvatarRepository avatarRepository;

    @Test
    void returnsRequestedPage() throws Exception {
        PageRequest pageRequest = PageRequest.of(1, 2, Sort.by("id"));
        when(avatarRepository.findAll(pageRequest))
                .thenReturn(new PageImpl<>(List.of(new Avatar("third.png", "image/png")), pageRequest, 3));

        mockMvc.perform(get("/avatar").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].filePath").value("third.png"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(1));
        verify(avatarRepository).findAll(pageRequest);
    }

    @Test
    void rejectsInvalidPageOrSize() throws Exception {
        mockMvc.perform(get("/avatar").param("page", "-1").param("size", "2"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/avatar").param("page", "0").param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
