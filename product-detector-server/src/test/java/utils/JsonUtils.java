package utils;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Optional;

public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Optional<String> objToJson(Object obj) {
        try {
            return Optional.of(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static <T> Optional<T> jsonToObj(String json, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

}
