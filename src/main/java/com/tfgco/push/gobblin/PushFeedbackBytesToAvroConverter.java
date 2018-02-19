package com.tfgco.push.gobblin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tfgco.pushnotification.PushFeedback;
import gobblin.configuration.WorkUnitState;
import gobblin.converter.DataConversionException;
import gobblin.converter.SchemaConversionException;
import gobblin.converter.SingleRecordIterable;
import gobblin.converter.ToAvroConverterBase;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Felipe Cavalcanti on 18/02/18.
 */
public class PushFeedbackBytesToAvroConverter extends ToAvroConverterBase<String, byte[]> {
    private static final Logger log = LoggerFactory.getLogger(PushFeedbackBytesToAvroConverter.class);

    public PushFeedbackBytesToAvroConverter() {
    }

    public Schema convertSchema(String s, WorkUnitState workUnitState) throws SchemaConversionException {
        return PushFeedback.getClassSchema();
    }

    public Iterable<GenericRecord> convertRecord(Schema schema, byte[] bytes, WorkUnitState workUnitState) throws DataConversionException {
        try {
            JSONObject js = JSON.parseObject(new String(bytes));
            GenericRecord pushFeedback = new GenericData.Record(schema);

            if (js.containsKey("from")){
                pushFeedback.put("to",js.getString("from"));
            } else {
                pushFeedback.put("to",js.getString("DeviceToken"));
            }

            pushFeedback.put("timestamp", (js.getLongValue("timestamp")));


            HashMap<String, String> pushNotificationMetadata = new HashMap<>();
            Map<String, Object> metadata = js.getJSONObject("metadata").getInnerMap();
            for (String key: metadata.keySet()){
                pushNotificationMetadata.put(key, String.valueOf(metadata.get(key)));
            }

            pushFeedback.put("metadata", pushNotificationMetadata);

            if (js.containsKey("Sent")){
                boolean sent = js.getBooleanValue("Sent");
                pushFeedback.put("sent", sent);
                if(!sent){
                    pushFeedback.put("reason", js.getString("Reason"));
                }
            } else {
                boolean sent = js.getString("message_type").equals("ack") ? true : false;
                pushFeedback.put("sent", sent);
                if (!sent){
                    pushFeedback.put("reason", js.getString("error"));
                }
            }

            return new SingleRecordIterable<>(pushFeedback);
        } catch (Exception e) {
            throw new SerializationException("Error during Deserialization", e);
        }
    }
}
