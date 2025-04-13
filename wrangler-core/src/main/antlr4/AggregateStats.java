package io.cdap.directives;

import co.cask.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.parser.*;
import io.cdap.wrangler.api.row.Row;

import java.util.*;

/**
 * A directive that aggregates byte sizes and time durations.
 */
@Directive(name = "aggregate-stats", usage = "Aggregates byte sizes and time durations across rows")
public class AggregateStats implements Directive {
    private String sourceSizeCol;
    private String sourceTimeCol;
    private String targetSizeCol;
    private String targetTimeCol;
    private String sizeUnit;
    private String timeUnit;
    private String aggregationType;

    private static final String TOTAL_SIZE_KEY = "agg_total_size";
    private static final String TOTAL_TIME_KEY = "agg_total_time";
    private static final String COUNT_KEY = "agg_count";

    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder("aggregate-stats")
                .addRequiredArg("sourceSizeCol")
                .addRequiredArg("sourceTimeCol")
                .addRequiredArg("targetSizeCol")
                .addRequiredArg("targetTimeCol")
                .addOptionalArg("sizeUnit", "Target unit for byte size. Default is B.")
                .addOptionalArg("timeUnit", "Target unit for time duration. Default is ns.")
                .addOptionalArg("aggregationType", "total or average. Default is total.")
                .build();
    }

    @Override
    public void initialize(Arguments arguments) {
        sourceSizeCol = arguments.value("sourceSizeCol");
        sourceTimeCol = arguments.value("sourceTimeCol");
        targetSizeCol = arguments.value("targetSizeCol");
        targetTimeCol = arguments.value("targetTimeCol");
        sizeUnit = arguments.valueOrDefault("sizeUnit", "B").toUpperCase();
        timeUnit = arguments.valueOrDefault("timeUnit", "ns").toLowerCase();
        aggregationType = arguments.valueOrDefault("aggregationType", "total").toLowerCase();
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecuteException {
        long totalSize = context.getOrDefault(TOTAL_SIZE_KEY, 0L);
        long totalTime = context.getOrDefault(TOTAL_TIME_KEY, 0L);
        int count = context.getOrDefault(COUNT_KEY, 0);

        for (Row row : rows) {
            Object sizeVal = row.getValue(sourceSizeCol);
            Object timeVal = row.getValue(sourceTimeCol);

            long sizeInBytes = UnitParser.parseByteSize(sizeVal.toString());
            long timeInNanos = UnitParser.parseTimeDuration(timeVal.toString());

            totalSize += sizeInBytes;
            totalTime += timeInNanos;
            count++;
        }

        context.set(TOTAL_SIZE_KEY, totalSize);
        context.set(TOTAL_TIME_KEY, totalTime);
        context.set(COUNT_KEY, count);

        return Collections.emptyList(); // interim pass
    }

    @Override
    public List<Row> terminate(ExecutorContext context) throws DirectiveExecuteException {
        long totalSize = context.getOrDefault(TOTAL_SIZE_KEY, 0L);
        long totalTime = context.getOrDefault(TOTAL_TIME_KEY, 0L);
        int count = context.getOrDefault(COUNT_KEY, 1);

        double sizeValue;
        double timeValue;

        if (aggregationType.equals("average")) {
            sizeValue = totalSize / (double) count;
            timeValue = totalTime / (double) count;
        } else {
            sizeValue = totalSize;
            timeValue = totalTime;
        }

        long convertedSize = UnitParser.convertByteSize((long) sizeValue, sizeUnit);
        long convertedTime = UnitParser.convertTimeDuration((long) timeValue, timeUnit);

        Row result = new Row();
        result.add(targetSizeCol, convertedSize + sizeUnit);
        result.add(targetTimeCol, convertedTime + timeUnit);

        return Collections.singletonList(result);
    }
}
