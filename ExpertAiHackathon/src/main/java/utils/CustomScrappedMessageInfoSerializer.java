package utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import datamodels.ScrappedMessageInfo;

public class CustomScrappedMessageInfoSerializer extends StdSerializer<ScrappedMessageInfo> {

    public CustomScrappedMessageInfoSerializer() {
        this(null);
    }

    public CustomScrappedMessageInfoSerializer(Class<ScrappedMessageInfo> t) {
        super(t);
    }

    @Override
    public void serialize(ScrappedMessageInfo smi, JsonGenerator jsonGenerator, SerializerProvider serializer) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("text", smi.getText());
        jsonGenerator.writeStringField("published_date", smi.getPublished_date());
        jsonGenerator.writeStringField("user", smi.getUser());
        jsonGenerator.writeStringField("score", smi.getScore());
        jsonGenerator.writeStringField("replies", smi.getComments());
        jsonGenerator.writeStringField("partialSource", smi.getPartialSource());
        jsonGenerator.writeStringField("fullSource", smi.getFullSource());
        jsonGenerator.writeStringField("extracted_date", smi.getExtracted_date());
        jsonGenerator.writeStringField("uuid", smi.getUuid().toString());
        jsonGenerator.writeStringField("parent_uuid", !smi.getParent_uuid().isPresent()? "UNKNOWN": smi.getParent_uuid().get().toString());
        
        jsonGenerator.writeStringField("analyzedText", smi.getAnalyzedText());
        jsonGenerator.writeEndObject();
    }
	
}
