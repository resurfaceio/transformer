// © 2016-2024 Graylog, Inc.

package io.resurface.transformer;

import io.resurface.ndjson.HttpMessage;
import io.resurface.ndjson.MessageFileReader;
import io.resurface.ndjson.MessageFileWriter;

import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

/**
 * Transforms messages stored in compressed NDJSON files.
 */
public class Main {

    /**
     * Runs transformer as command-line program.
     */
    public static void main(String[] args) {
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
                    messages_read++;
                    if (transform(message)) {
                        writer.write(message);
                        if (messages_written++ % 1000 == 0) status();
                    }
                });
            }
        }

        status();  // show final status
    }

    /**
     * Transform message before writing, returning false if message should not be written.
     */
    private boolean transform(HttpMessage message) {
        try {
            // calculate digest for specified message
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            digestUpdate(d, message.host());
            digestUpdate(d, message.interval_millis());
            digestUpdate(d, message.request_body());
            digestUpdate(d, message.request_content_type());
            digestUpdate(d, message.request_headers_json());
            digestUpdate(d, message.request_method());
            digestUpdate(d, message.request_params_json());
            digestUpdate(d, message.request_url());
            digestUpdate(d, message.request_user_agent());
            digestUpdate(d, message.response_body());
            digestUpdate(d, message.response_code());
            digestUpdate(d, message.response_content_type());
            digestUpdate(d, message.response_headers_json());
            digestUpdate(d, message.response_time_millis());
            digestUpdate(d, message.custom_fields_json());
            digestUpdate(d, message.request_address());
            digestUpdate(d, message.session_fields_json());

            // skip processing for any duplicates detected
            String digest = bytesToHex(d.digest());
            if (hashes.contains(digest)) return false;
            hashes.add(digest);

            // add interval if none exists
            if (message.interval_millis() == 0) message.set_interval_millis((int) (Math.random() * 15000));

            // reset response time to now
            message.set_response_time_millis(0);

            return true;
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            return false;
        }
    }

    /**
     * Returns string representation of hash digest.
     */
    private static String bytesToHex(byte[] digest) {
        StringBuilder s = new StringBuilder(2 * digest.length);
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) s.append('0');
            s.append(hex);
        }
        return s.toString();
    }

    /**
     * Updates hash digest for specified string value.
     */
    private static void digestUpdate(MessageDigest digest, String value) {
        digest.update(value == null ? "".getBytes() : value.getBytes());
    }

    /**
     * Update hash digest for specified long value.
     */
    private static void digestUpdate(MessageDigest digest, long value) {
        digestUpdate(digest, value == 0 ? "" : String.valueOf(value));
    }

    /**
     * Print status summary.
     */
    private void status() {
        long elapsed = System.currentTimeMillis() - started;
        long rate = (messages_read * 1000 / elapsed);
        System.out.println("Messages read: " + messages_read + ", messages written: " + messages_written + ", Elapsed time: " + elapsed + " ms, Rate: " + rate + " msg/sec");
    }

    final private Set<String> hashes = new HashSet<>();
    private long messages_read = 0;
    private long messages_written = 0;
    private final long started = System.currentTimeMillis();

}