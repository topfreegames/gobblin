package com.tfgco.eventsgateway.gobblin;

import com.tfgco.eventsgateway.Event;
import gobblin.configuration.WorkUnitState;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;


/**
 * Created by cscatolini on 04/02/18.
 */
public class GobblinBytesToAvroConverterTest {
    private GobblinBytesToAvroConverter subject;
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    @Before
    public void setUp() throws Exception {
        subject = new GobblinBytesToAvroConverter();
    }

    @org.junit.Test
    public void convertSchema() throws Exception {
        Schema schema = subject.convertSchema("Anything", new WorkUnitState());
        assertEquals(Event.getClassSchema(), schema);
    }

    @org.junit.Test
    public void convertRecord() throws Exception {
        Event u = new Event();
        u.setId("A72840BD-447D-4383-B11B-711C992887BC");
        u.setName("MyEvent");
        u.setServerTimestamp(42L);
        u.setClientTimestamp(43L);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        org.apache.avro.io.Encoder binaryEncoder = encoderFactory.directBinaryEncoder(out, null);
        DatumWriter<Event> writer = new SpecificDatumWriter<>(u.getSchema());
        writer.write(u, binaryEncoder);
        binaryEncoder.flush();
        byte[] event = out.toByteArray();
        GenericRecord record = subject.convertRecord(
                Event.getClassSchema(), event, new WorkUnitState()
        ).iterator().next();

        assertEquals(record.get("id").toString(), "A72840BD-447D-4383-B11B-711C992887BC");
        assertEquals(record.get("name").toString(), "MyEvent");
        assertEquals(record.get("serverTimestamp"), 42L);
        assertEquals(record.get("clientTimestamp"), 43L);
    }

}