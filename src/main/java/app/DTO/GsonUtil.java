package app.DTO;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;

public class GsonUtil {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();

    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate fecha, Type tipo, JsonSerializationContext contexto) {
            return new JsonPrimitive(fecha.toString());          // "2026-11-05"
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type tipo, JsonDeserializationContext contexto) {
            return LocalDate.parse(json.getAsString());
        }
    }

    private static class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
        @Override
        public JsonElement serialize(LocalTime hora, Type tipo, JsonSerializationContext contexto) {
            return new JsonPrimitive(hora.toString());           // "09:30"
        }

        @Override
        public LocalTime deserialize(JsonElement json, Type tipo, JsonDeserializationContext contexto) {
            return LocalTime.parse(json.getAsString());
        }
    }
}