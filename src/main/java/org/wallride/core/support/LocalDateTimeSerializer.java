package org.wallride.core.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.LocalDateTime;

import java.io.IOException;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

	@Override
	public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException, JsonProcessingException {
		jsonGenerator.writeString(localDateTime.toString("yyyy/MM/dd HH:mm"));
	}
}
