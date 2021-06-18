package utils;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import datamodels.ScrappedMessageInfo;

public class CustomScrappedMessageInfoDeserializer extends StdDeserializer<ScrappedMessageInfo> {
    
    public CustomScrappedMessageInfoDeserializer() {
        this(null);
    }

    public CustomScrappedMessageInfoDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ScrappedMessageInfo deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);
        
        // try catch block
        String text = node.get("text").asText();
        String published_date = node.get("published_date").asText();
        String user = node.get("user").asText();
        String score = node.get("score").asText();
        String replies = node.get("replies").asText();
        String partialSource = node.get("partialSource").asText();
        String fullSource = node.get("fullSource").asText();
        String extracted_date = node.get("extracted_date").asText();
        UUID uuid = UUID.fromString(node.get("uuid").asText());
        String parent_uuid_str = node.get("parent_uuid").asText();
        Optional<UUID> parent_uuid = Optional.empty();
        if(!parent_uuid_str.equals("UNKNOWN")) {
        	parent_uuid = Optional.of(UUID.fromString(parent_uuid_str));
        }
        String analyzedText = "";
        if(node.get("analyzedText") != null) {
        	analyzedText = node.get("analyzedText").asText();
        }
        return new ScrappedMessageInfo(text, published_date, user, score, replies, partialSource,
    			fullSource, extracted_date,  uuid, parent_uuid, analyzedText);
    }

}
