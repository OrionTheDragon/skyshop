package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SchoolControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    static Stream<Arguments> entities() {
        return Stream.of(
                Arguments.of("student", "Harry", "age", 11, 12),
                Arguments.of("faculty", "Gryffindor", "color", "Red", "Blue")
        );
    }

    @ParameterizedTest
    @MethodSource("entities")
    void fullCrudReturnsPersistedJson(String path, String name, String field, Object value, Object updatedValue)
            throws Exception {
        ObjectNode body = body(name, field, value);
        JsonNode created = create(path, body);
        long id = created.get("id").asLong();
        assertThat(id).isPositive();

        mvc.perform(get("/" + path + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$." + field).value(value));

        body.put("id", id);
        body.put("name", "Updated");
        body.set(field, objectMapper.valueToTree(updatedValue));
        mvc.perform(put("/" + path).contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$." + field).value(updatedValue));
        mvc.perform(get("/" + path + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + field).value(updatedValue));

        mvc.perform(delete("/" + path + "/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/" + path + "/" + id)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("entities")
    void filtersAndListReturnOnlyMatchingEntities(String path, String name, String field, Object value,
                                                  Object otherValue) throws Exception {
        JsonNode created = create(path, body(name, field, value));
        create(path, body("Other", field, otherValue));
        mvc.perform(get("/" + path))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));

        String filter = value instanceof String ? value.toString().toLowerCase() : value.toString();
        mvc.perform(get("/" + path).param(field, filter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(created.get("id").asLong()));
        mvc.perform(get("/" + path).param(field, field.equals("age") ? "100" : "Unknown"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @ParameterizedTest
    @MethodSource("entities")
    void unknownIdReturns404AndDoesNotInsertOnPut(String path, String name, String field, Object value,
                                                  Object ignored) throws Exception {
        ObjectNode body = body(name, field, value);
        body.put("id", Long.MAX_VALUE);
        mvc.perform(get("/" + path + "/" + Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(put("/" + path).contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/" + path + "/" + Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(get("/" + path)).andExpect(jsonPath("$.length()").value(0));
    }

    @ParameterizedTest
    @MethodSource("entities")
    void updateWithoutIdReturns400(String path, String name, String field, Object value, Object ignored)
            throws Exception {
        mvc.perform(put("/" + path).contentType(MediaType.APPLICATION_JSON)
                        .content(body(name, field, value).toString()))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("entities")
    void postWithSuppliedIdCreatesNewEntityWithoutOverwritingOldOne(String path, String name, String field,
                                                                  Object value, Object otherValue) throws Exception {
        JsonNode original = create(path, body(name, field, value));
        ObjectNode body = body("Other", field, otherValue);
        body.put("id", original.get("id").asLong());

        JsonNode created = create(path, body);
        assertThat(created.get("id").asLong()).isNotEqualTo(original.get("id").asLong());
        mvc.perform(get("/" + path + "/" + original.get("id").asLong()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"student", "faculty"})
    void emptyStorageReturnsEmptyJsonArray(String path) throws Exception {
        mvc.perform(get("/" + path)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"student", "faculty"})
    void adviceReturnsNotFoundMessageAsJson(String path) throws Exception {
        String entity = path.equals("student") ? "Student" : "Faculty";
        mvc.perform(get("/" + path + "/" + Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(entity + " with id " + Long.MAX_VALUE + " not found"));
    }

    @Test
    void testControllerConfirmsSchoolIsRunning() throws Exception {
        mvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("School application is running"));
    }

    private ObjectNode body(String name, String field, Object value) {
        ObjectNode body = objectMapper.createObjectNode().put("name", name);
        body.set(field, objectMapper.valueToTree(value));
        return body;
    }

    private JsonNode create(String path, ObjectNode body) throws Exception {
        String json = mvc.perform(post("/" + path).contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json);
    }
}
