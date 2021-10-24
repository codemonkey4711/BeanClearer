package at.codemonkey.beanclearer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonMapper {

    private static ObjectMapper om = new ObjectMapper();

    static {
        om.setSerializationInclusion(Include.NON_NULL);
//        om.setSerializationInclusion(Include.NON_EMPTY);
    }


    public static String toJson(Object rto) {
        try {
            return om.writeValueAsString(rto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
