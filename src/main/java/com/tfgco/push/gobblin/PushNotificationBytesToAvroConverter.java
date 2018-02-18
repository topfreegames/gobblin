package com.tfgco.push.gobblin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tfgco.pushnotification.PushNotification;
import gobblin.configuration.WorkUnitState;
import gobblin.converter.DataConversionException;
import gobblin.converter.SchemaConversionException;
import gobblin.converter.SingleRecordIterable;
import gobblin.converter.ToAvroConverterBase;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Felipe Cavalcanti on 18/02/18.
 */
public class PushNotificationBytesToAvroConverter extends ToAvroConverterBase<String, byte[]> {
    private static final Logger log = LoggerFactory.getLogger(PushNotificationBytesToAvroConverter.class);

    public PushNotificationBytesToAvroConverter() {
    }

    public Schema convertSchema(String s, WorkUnitState workUnitState) throws SchemaConversionException {
        return PushNotification.getClassSchema();
    }

    public Iterable<GenericRecord> convertRecord(Schema schema, byte[] bytes, WorkUnitState workUnitState) throws DataConversionException {
        try {
            JSONObject js = JSON.parseObject(new String(bytes));
            PushNotification pushNotification = new PushNotification();

            if (js.containsKey("to")){
                pushNotification.setToken(js.getString("to"));
            } else {
                pushNotification.setToken(js.getString("DeviceToken"));
            }

            pushNotification.setDryRun(js.getBooleanValue("dry_run"));
            pushNotification.setPushExpiry(js.getLongValue("push_expiry"));
            Map<String, Object> data;
            if (js.containsKey("data")){
                data = js.getJSONObject("data").getInnerMap();
            } else {
                data = js.getJSONObject("Payload").getJSONObject("aps").getInnerMap();
            }

            pushNotification.setData(new HashMap<>());
            for (String key : data.keySet()){
                pushNotification.getData().put(key, String.valueOf(data.get(key)));
            }

            pushNotification.setMetadata(new HashMap<>());
            Map<String, Object> metadata = js.getJSONObject("metadata").getInnerMap();
            for (String key: metadata.keySet()){
                pushNotification.getMetadata().put(key, String.valueOf(metadata.get(key)));
            }

            return new SingleRecordIterable<>(pushNotification);
        } catch (Exception e) {
            throw new SerializationException("Error during Deserialization", e);
        }
    }
}
