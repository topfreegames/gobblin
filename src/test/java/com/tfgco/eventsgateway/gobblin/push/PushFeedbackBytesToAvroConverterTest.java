package com.tfgco.eventsgateway.gobblin.push;

import com.tfgco.push.gobblin.PushFeedbackBytesToAvroConverter;
import com.tfgco.pushnotification.PushFeedback;
import gobblin.configuration.WorkUnitState;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.EncoderFactory;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;


/**
 * Created by cscatolini on 04/02/18.
 */
public class PushFeedbackBytesToAvroConverterTest {
    private PushFeedbackBytesToAvroConverter subject;
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    @Before
    public void setUp() throws Exception {
        subject = new PushFeedbackBytesToAvroConverter();
    }

    @org.junit.Test
    public void convertSchema() throws Exception {
        Schema schema = subject.convertSchema("Anything", new WorkUnitState());
        assertEquals(PushFeedback.getClassSchema(), schema);
    }

    @org.junit.Test
    public void convertAPNSPushFeedbackWithoutError() throws Exception {
        String apnsPushFeedbackJson = "{\"Sent\":true,\"StatusCode\":200,\"Reason\":\"\",\"ApnsID\":\"0f8d7827-a075-4a8b-91ba-3a0b24428d6d\",\"Err\":null,\"DeviceToken\":\"539ccbd00fa5f5fc8491ff88315d210edc874ed974886fa956a99dd891103026\",\"timestamp\":1519056261,\"metadata\":{\"deviceToken\":\"539ccbd00fa5f5fc8491ff88315d210edc874ed974886fa956a99dd891103026\",\"game\":\"blockcraft\",\"hostname\":\"apns-f4b58bd5f-m6kkf\",\"muid\":\"55a21df7-08b2-47cd-a7d4-6b59e2dd53bf\",\"platform\":\"apns\",\"pushTime\":1519056260,\"pushType\":\"single\",\"templateName\":\"blockcraft-like\",\"tokenCreatedAt\":1472424910,\"userId\":\"A6A574C1-4C92-40B8-9121-9946DE5B2CA5\"}}";
        byte[] pushFeedback = apnsPushFeedbackJson.getBytes();
        GenericRecord record = subject.convertRecord(
                PushFeedback.getClassSchema(), pushFeedback, new WorkUnitState()
        ).iterator().next();

        assertEquals(record.get("to").toString(), "539ccbd00fa5f5fc8491ff88315d210edc874ed974886fa956a99dd891103026");
        assertEquals(((HashMap<String, String>)record.get("metadata")).get("userId"), "A6A574C1-4C92-40B8-9121-9946DE5B2CA5");
        assertEquals(record.get("timestamp"), 1519056261L);
        assertEquals(record.get("sent"), true);
        assertEquals(record.get("reason"), null);
    }

    @org.junit.Test
    public void convertAPNSPushFeedbackWithError() throws Exception {
        String apnsPushFeedbackJson = "{\"Sent\":false,\"StatusCode\":410,\"Reason\":\"Unregistered\",\"ApnsID\":\"b7df3933-07a5-4ab6-98aa-85dd42ebf0e5\",\"Err\":{\"Key\":\"unregistered\",\"Description\":\"Unregistered\"},\"DeviceToken\":\"898ed69ded7c49f7801efd24bea0d88fff7c112a347800b349de1e9428e52afa\",\"timestamp\":1519056851,\"metadata\":{\"deleteToken\":true,\"deviceToken\":\"898ed69ded7c49f7801efd24bea0d88fff7c112a347800b349de1e9428e52afa\",\"game\":\"blockcraft\",\"hostname\":\"apns-f4b58bd5f-blr5t\",\"muid\":\"634369d7-c5ae-4d2b-b247-d53d5e7f6232\",\"platform\":\"apns\",\"pushTime\":1519056851,\"pushType\":\"single\",\"templateName\":\"blockcraft-building-like\",\"tokenCreatedAt\":1515770332,\"userId\":\"77CBAA9D-7441-452F-8F05-0B8C35472867\"}}";
        byte[] pushFeedback = apnsPushFeedbackJson.getBytes();
        GenericRecord record = subject.convertRecord(
                PushFeedback.getClassSchema(), pushFeedback, new WorkUnitState()
        ).iterator().next();

        assertEquals(record.get("to").toString(), "898ed69ded7c49f7801efd24bea0d88fff7c112a347800b349de1e9428e52afa");
        assertEquals(((HashMap<String, String>)record.get("metadata")).get("userId"), "77CBAA9D-7441-452F-8F05-0B8C35472867");
        assertEquals(record.get("timestamp"),1519056851L);
        assertEquals(record.get("sent"), false);
        assertEquals(record.get("reason"), "Unregistered");
    }

    @org.junit.Test
    public void convertGCMPushFeedbackWithoutError() throws Exception {
        String gcmPushFeedbackJson = "{\"from\":\"fw7eV_wdw-c:APA91bEw1wXo4jMRwcAZE4PQi3b_p-nAYpRtSpMPyoQaVAiC5hovqNTTL66n7dQywONk8bIxBRiiD7u_mD4JhEz4OSFptYYPkAnlqpxj6w1vh24WNOwC99VFTrEbnKL9uQJ4Mlo8cdSK\",\"message_id\":\"f943f032-d0d5-4beb-b74a-390f78d8a5f7\",\"message_type\":\"ack\",\"category\":\"\",\"timestamp\":1519056196,\"metadata\":{\"game\":\"blockcraft\",\"hostname\":\"gcm-57db98689f-pqcqm\",\"muid\":\"c81526a8-a440-4cc3-bb22-c2d3ff0f06d2\",\"platform\":\"gcm\",\"pushTime\":1519056195,\"pushType\":\"single\",\"templateName\":\"blockcraft-like\",\"tokenCreatedAt\":1512569046,\"userId\":\"974AE08E-C6E7-4755-9399-9FD66D5EC9C2\"}}";
        byte[] pushFeedback = gcmPushFeedbackJson.getBytes();
        GenericRecord record = subject.convertRecord(
                PushFeedback.getClassSchema(), pushFeedback, new WorkUnitState()
        ).iterator().next();

        assertEquals(record.get("to").toString(), "fw7eV_wdw-c:APA91bEw1wXo4jMRwcAZE4PQi3b_p-nAYpRtSpMPyoQaVAiC5hovqNTTL66n7dQywONk8bIxBRiiD7u_mD4JhEz4OSFptYYPkAnlqpxj6w1vh24WNOwC99VFTrEbnKL9uQJ4Mlo8cdSK");
        assertEquals(((HashMap<String, String>)record.get("metadata")).get("userId"), "974AE08E-C6E7-4755-9399-9FD66D5EC9C2");
        assertEquals(record.get("timestamp"), 1519056196L);
        assertEquals(record.get("sent"), true);
        assertEquals(record.get("reason"), null);
    }

    @org.junit.Test
    public void convertGCMPushFeedbackWith() throws Exception {
        String gcmPushFeedbackJson = "{\"from\":\"erNN7Ays0R0:APA91bEtf_Vntg3k_FrQS9BB-adRzO4-DDNi9lerVS3fDsGWcowG6ne-qs_qlUJeaYkFKxfUT7mohsr2fc9xccDluO0yHZgQB7U6AKnrsuBpqwxSpS7vRVMEq6Yyo6kWmXmKhHCReT6z\",\"message_id\":\"75455253-d8f9-405d-971a-23a32d56c1ee\",\"message_type\":\"nack\",\"error\":\"DEVICE_UNREGISTERED\",\"category\":\"\",\"timestamp\":1519057957,\"metadata\":{\"deleteToken\":true,\"game\":\"blockcraft\",\"hostname\":\"gcm-57db98689f-m9n7h\",\"muid\":\"2617a99a-bf55-40d9-8404-6fa2f93081c1\",\"platform\":\"gcm\",\"pushTime\":1519057956,\"pushType\":\"single\",\"templateName\":\"blockcraft-like\",\"tokenCreatedAt\":1489842024,\"userId\":\"4B50DC29-26E1-4F2C-81CA-3C0573210AE3\"}}";
        byte[] pushFeedback = gcmPushFeedbackJson.getBytes();
        GenericRecord record = subject.convertRecord(
                PushFeedback.getClassSchema(), pushFeedback, new WorkUnitState()
        ).iterator().next();

        assertEquals(record.get("to").toString(), "erNN7Ays0R0:APA91bEtf_Vntg3k_FrQS9BB-adRzO4-DDNi9lerVS3fDsGWcowG6ne-qs_qlUJeaYkFKxfUT7mohsr2fc9xccDluO0yHZgQB7U6AKnrsuBpqwxSpS7vRVMEq6Yyo6kWmXmKhHCReT6z");
        assertEquals(((HashMap<String, String>)record.get("metadata")).get("userId"), "4B50DC29-26E1-4F2C-81CA-3C0573210AE3");
        assertEquals(record.get("timestamp"), 1519057957L);
        assertEquals(record.get("sent"), false);
        assertEquals(record.get("reason"),"DEVICE_UNREGISTERED");
    }
}