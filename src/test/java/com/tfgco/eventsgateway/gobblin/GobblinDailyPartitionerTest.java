package com.tfgco.eventsgateway.gobblin;

import com.tfgco.eventsgateway.Event;
import com.tfgco.pushnotification.PushNotification;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import gobblin.configuration.State;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.Assert.*;

/**
 * Created by cscatolini on 04/02/18.
 */
public class GobblinDailyPartitionerTest {
    private GobblinDailyPartitioner partitioner;

    @Before
    public void setUp() throws IOException {
        State properties = new State();
        this.partitioner = new GobblinDailyPartitioner(properties, 0, 0);
    }

    @Test
    public void partitionForRecord() throws Exception {
        GenericRecordBuilder genericRecordBuilder = new GenericRecordBuilder(Event.getClassSchema());

        // This timestamp corresponds to 2018/02/04
        genericRecordBuilder.set("id", "A72840BD-447D-4383-B11B-711C992887BC");
        genericRecordBuilder.set("name", "MyEvent");
        genericRecordBuilder.set("serverTimestamp", 1517765613000L);
        genericRecordBuilder.set("clientTimestamp", 1517765613000L);
        GenericRecord result = this.partitioner.partitionForRecord(genericRecordBuilder.build());
        String path = (String) result.get("partitionedPath");
        assertEquals("daily/year=2018/month=02/day=04", path);

    }

    @Test
    public void partitionForPushRecord() throws Exception {
        GenericRecordBuilder genericRecordBuilder = new GenericRecordBuilder(PushNotification.getClassSchema());

        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("pushTime", String.valueOf(1517765613));
        genericRecordBuilder.set("token", "sometoken");
        genericRecordBuilder.set("metadata", metadata);

        GenericRecord result = this.partitioner.partitionForRecord(genericRecordBuilder.build());
        String path = (String) result.get("partitionedPath");
        assertEquals("daily/year=2018/month=02/day=04", path);
    }
}