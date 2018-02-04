package com.tfgco.eventsgateway.gobblin;

import com.tfgco.eventsgateway.Event;
import gobblin.configuration.WorkUnitState;
import gobblin.converter.DataConversionException;
import gobblin.converter.SchemaConversionException;
import gobblin.converter.SingleRecordIterable;
import gobblin.converter.ToAvroConverterBase;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.commons.lang.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * Created by cscatolini on 04/02/18.
 */
public class GobblinBytesToAvroConverter extends ToAvroConverterBase<String, byte[]> {
    private GenericDatumReader<GenericData.Record> _datumReader;
    private static final Logger log = LoggerFactory.getLogger(GobblinBytesToAvroConverter.class);

    public GobblinBytesToAvroConverter() {
        _datumReader = new GenericDatumReader<>();
    }

    public Schema convertSchema(String s, WorkUnitState workUnitState) throws SchemaConversionException {
        return Event.getClassSchema();
    }

    public Iterable<GenericRecord> convertRecord(Schema schema, byte[] bytes, WorkUnitState workUnitState) throws DataConversionException {
        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
            _datumReader.setSchema(schema);
            try {
                GenericRecord record = _datumReader.read(null, decoder);
                return new SingleRecordIterable<>(record);
            } catch (IOException e) {
                log.error(String.format("Error during decoding record with schema %s: ", schema));
                throw e;
            }
        } catch (IOException e) {
            throw new SerializationException("Error during Deserialization", e);
        }
    }
}
