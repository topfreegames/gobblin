package com.tfgco.eventsgateway.gobblin;

import com.tfgco.eventsgateway.Event;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import gobblin.configuration.State;


import java.io.IOException;
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

        // This timestamp corresponds to 2015/01/01
        genericRecordBuilder.set("id", "A72840BD-447D-4383-B11B-711C992887BC");
        genericRecordBuilder.set("name", "MyEvent");
        genericRecordBuilder.set("serverTimestamp", 1420099200L);
        GenericRecord result = this.partitioner.partitionForRecord(genericRecordBuilder.build());
        String path = (String) result.get("partitionedPath");
        assertEquals("daily/year=2015/month=01/day=01", path);

    }

    @Test
    public void partitionForRecordJsonWithTimestamp() throws Exception {
        byte[] json = "{\"test\":\"test\", \"timestamp\":1420099200}".getBytes();
        GenericRecord result = this.partitioner.partitionForRecord(json);
        String path = (String) result.get("partitionedPath");
        assertEquals("daily/year=2015/month=01/day=01", path);

    }

    @Test
    public void partitionForRecordJsonWithoutTimestamp() throws Exception {
        byte[] json = "{\"test\":\"test\"}".getBytes();
        DateTimeUtils.setCurrentMillisFixed(1420099200000L);
        GenericRecord result = this.partitioner.partitionForRecord(json);
        String path = (String) result.get("partitionedPath");
        assertEquals("daily/year=2015/month=01/day=01", path);

    }

}