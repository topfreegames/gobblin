package com.tfgco.eventsgateway.gobblin;

import com.google.common.base.Optional;
import gobblin.util.AvroUtils;
import gobblin.util.ForkOperatorUtils;
import gobblin.writer.partitioner.TimeBasedAvroWriterPartitioner;
import gobblin.writer.partitioner.WriterPartitioner;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import gobblin.configuration.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by cscatolini on 04/02/18.
 */
public class GobblinDailyPartitioner<D> implements WriterPartitioner<D> {
    private DateTimeFormatter outputDateFormatter = null;
    private final DateTimeZone timeZone;
    private final Schema schema;
    public static final String PARTITIONED_PATH = "partitionedPath";

    public GobblinDailyPartitioner(State state, int numBranches, int branchId) {
        this.timeZone = getTimeZone(state, numBranches, branchId);
        this.schema = getSchema();
    }

    @Override
    public Schema partitionSchema() {
        return this.schema;
    }

    private static DateTimeZone getTimeZone(State state, int numBranches, int branchId) {
        String propName = ForkOperatorUtils.getPropertyNameForBranch("writer.partition.timezone", numBranches, branchId);
        return DateTimeZone.forID(state.getProp(propName, "UTC"));
    }

    private Schema getSchema() {
        SchemaBuilder.FieldAssembler<Schema> assembler =
                SchemaBuilder.record("EventsgatewayGobblin").namespace("eventsgateway.gobblin.partitioner").fields();

        assembler = assembler.name(PARTITIONED_PATH).type(Schema.create(Schema.Type.STRING)).noDefault();

        return assembler.endRecord();
    }


    private Optional<Object> getRecordTimestamp(GenericRecord record) {
        Optional<Object> fieldValue = AvroUtils.getFieldValue(record, "serverTimestamp");
        if (fieldValue.isPresent()) {
            return fieldValue;
        }
        fieldValue = AvroUtils.getFieldValue(record, "metadata");
        if (fieldValue.isPresent()) {
            HashMap<String, String> m = (HashMap<String, String>) fieldValue.get();
            if (m.containsKey("pushTime")){
                // PushNotification is sending pushTime as seconds
                return Optional.of(Long.parseLong(m.get("pushTime")) * 1000);
            }
        }
        return Optional.absent();
    }

    private Long getTimestamp(Optional<Object> recordTimestamp) {
        return recordTimestamp.orNull() instanceof Long ? (Long) recordTimestamp.get() : DateTime.now().getMillis();
    }

    @Override
    public GenericRecord partitionForRecord(D record) {
        GenericRecord partition = new GenericData.Record(this.schema);
        if (outputDateFormatter == null) {
            outputDateFormatter = new DateTimeFormatterBuilder()
                    .appendLiteral("/year=").appendYear(4,4)
                    .appendLiteral("/month=").appendMonthOfYear(2)
                    .appendLiteral("/day=").appendDayOfMonth(2)
                    .toFormatter().withZone(DateTimeZone.forID(this.timeZone.toString()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("daily");
        DateTime bucket = new DateTime(getTimestamp(getRecordTimestamp((GenericRecord) record)));
        sb.append(bucket.toString(outputDateFormatter));
        partition.put(PARTITIONED_PATH, sb.toString());
        return partition;
    }
}
