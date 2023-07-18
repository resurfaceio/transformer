// © 2016-2023 Graylog, Inc.

package io.resurface.transformer;

import io.resurface.ndjson.HttpMessage;
import io.resurface.ndjson.MessageFileReader;
import io.resurface.ndjson.MessageFileWriter;

/**
 * Transforms messages stored in compressed NDJSON files.
 */
public class Main {

    /**
     * Runs transformer as command-line program.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("\n>>> Transformer starting");
        new Main();
        System.out.println(">>> Transformer finished!\n");
    }

    /**
     * Parses every message and writes a transformed version.
     */
    public Main() {
        // read configuration
        String file_in = System.getProperty("FILE_IN");
        if (file_in == null) throw new IllegalArgumentException("Missing FILE_IN");
        System.out.println("FILE_IN=" + file_in);
        String file_out = System.getProperty("FILE_OUT");
        if (file_out == null) throw new IllegalArgumentException("Missing FILE_OUT");
        System.out.println("FILE_OUT=" + file_out);

        // transform all messages
        try (MessageFileWriter writer = new MessageFileWriter(file_out)) {
            try (MessageFileReader reader = new MessageFileReader(file_in)) {
                reader.parse((HttpMessage message) -> {
                    transform(message);
                    writer.write(message);
                    if (messages_written++ % 100 == 0) status();
                });
            }
        }

        status();  // show final status
    }

    /**
     * Transform message before writing.
     */
    private void transform(HttpMessage message) {
        // add interval if none exists
        // if (message.interval_millis() == 0) message.set_interval_millis((int) (Math.random() * 15000));

        // reset response time to now
        message.set_response_time_millis(0);
    }

    /**
     * Print status summary.
     */
    private void status() {
        long elapsed = System.currentTimeMillis() - started;
        long rate = (messages_written * 1000 / elapsed);
        System.out.println("Messages: " + messages_written + ", Elapsed time: " + elapsed + " ms, Rate: " + rate + " msg/sec");
    }

    private long messages_written = 0;
    private final long started = System.currentTimeMillis();
    private final long millis_per_year = 365L * 24 * 60 * 60 * 1000;
    private final long a_year_ago = started - millis_per_year;

}