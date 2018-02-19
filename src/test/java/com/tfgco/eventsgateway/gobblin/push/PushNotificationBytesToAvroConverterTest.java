package com.tfgco.eventsgateway.gobblin.push;

import com.tfgco.push.gobblin.PushNotificationBytesToAvroConverter;
import com.tfgco.pushnotification.PushNotification;
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
public class PushNotificationBytesToAvroConverterTest {
    private PushNotificationBytesToAvroConverter subject;
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    @Before
    public void setUp() throws Exception {
        subject = new PushNotificationBytesToAvroConverter();
    }

    @org.junit.Test
    public void convertSchema() throws Exception {
        Schema schema = subject.convertSchema("Anything", new WorkUnitState());
        assertEquals(PushNotification.getClassSchema(), schema);
    }

    @org.junit.Test
    public void convertAPNSPush() throws Exception {
        String apnsPushJson = "{\"push_expiry\":100,\"metadata\":{\"pushType\":\"single\",\"userId\":\"1455fb05-cb39-488c-97d1-aa77d6520792\",\"tokenCreatedAt\":1518822452,\"muid\":\"56b8cd63-ddd5-495b-9af9-565a779dd268\",\"templateName\":\"battletanksbeta-mention\"},\"Payload\":{\"aps\":{\"alert\":\"POLACO said: @Fokitramp no discutimos, solo le damos punto de vista diferentes...tranki :* \",\"title\":\"POLACO said\",\"subtitle\":\"@Fokitramp no discutimos, solo le damos punto de vista diferentes...tranki :* \"},\"templateName\":\"battletanksbeta-mention\"},\"DeviceToken\":\"2dc9bc07e109e1c3c2d2993dae00866cd7c61192247c47bb8c092278aae7817b\"}";
        byte[] push = apnsPushJson.getBytes();
        GenericRecord record = subject.convertRecord(
                PushNotification.getClassSchema(), push, new WorkUnitState()
        ).iterator().next();

        assertEquals(record.get("token").toString(), "2dc9bc07e109e1c3c2d2993dae00866cd7c61192247c47bb8c092278aae7817b");
        assertEquals(((HashMap<String, String>)record.get("data")).get("title"), "POLACO said");
        assertEquals(((HashMap<String, String>)record.get("metadata")).get("userId"), "1455fb05-cb39-488c-97d1-aa77d6520792");
        assertEquals(record.get("push_expiry"), 100L);
    }

    @org.junit.Test
    public void convertGCMPush() throws Exception {
        String gcmPushJson = "{\"push_expiry\":0,\"metadata\":{\"pushType\":\"single\",\"userId\":\"e921893c-18d3-4d95-8274-1c5132c9cd8a\",\"tokenCreatedAt\":1518878015,\"muid\":\"9f437862-89e6-4564-8c66-0beacdb51c7f\",\"templateName\":\"battletanksbeta-custom-message\"},\"to\":\"eAFeZbdjU2Y:APA91bHjXBShQf0r0gDJg5kSRjkQU3FPqw8gYk4D7nxFaE--p7yk2UultdEpy09IuDN_NvzjhfrkZIPeerZT92W-kaZELwHBfYirqdelPWs8Z74OraYiygm6kRFFOMNfUOcYLTCD1XTH\",\"data\":{\"title\":\"War Machines\",\"subtitle\":\"Clan Battle Lobby is open! Come join the fight\",\"alert\":\"Clan Battle Lobby is open! Come join the fight\",\"templateName\":\"battletanksbeta-custom-message\"},\"dry_run\":true}";
        byte[] push = gcmPushJson.getBytes();
        GenericRecord record = subject.convertRecord(
                PushNotification.getClassSchema(), push, new WorkUnitState()
        ).iterator().next();

        assertEquals(record.get("token").toString(), "eAFeZbdjU2Y:APA91bHjXBShQf0r0gDJg5kSRjkQU3FPqw8gYk4D7nxFaE--p7yk2UultdEpy09IuDN_NvzjhfrkZIPeerZT92W-kaZELwHBfYirqdelPWs8Z74OraYiygm6kRFFOMNfUOcYLTCD1XTH");
        assertEquals(((HashMap<String, String>)record.get("data")).get("title"), "War Machines");
        assertEquals(record.get("dry_run"), true);
        assertEquals(((HashMap<String, String>)record.get("metadata")).get("userId"), "e921893c-18d3-4d95-8274-1c5132c9cd8a");
    }
}
