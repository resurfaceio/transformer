// © 2016-2024 Graylog, Inc.

package io.resurface.transformer;

import io.resurface.ndjson.HttpMessage;
import io.resurface.ndjson.MessageFileReader;
import io.resurface.ndjson.MessageFileWriter;

import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String files_in = System.getProperty("FILES_IN");
        if (files_in == null) throw new IllegalArgumentException("Missing FILES_IN");
        System.out.println("FILES_IN=" + files_in);
        String file_out = System.getProperty("FILE_OUT");
        if (file_out == null) throw new IllegalArgumentException("Missing FILE_OUT");
        System.out.println("FILE_OUT=" + file_out);

        // read & parse transforms
        transform_duplicates = new ParsedOperation("TRANSFORM_DUPLICATES", "(keep$|drop$)");
        transform_interval_millis = new ParsedOperation("TRANSFORM_INTERVAL_MILLIS", "(keep$|drop$|(randomize:.*)$)");
        transform_response_time_millis = new ParsedOperation("TRANSFORM_RESPONSE_TIME_MILLIS", "(keep$|drop$|(add:.*)$|(subtract:.*)$|(shuffle:.*)$)");

        // transform all messages
        try (MessageFileWriter writer = new MessageFileWriter(file_out)) {
            String[] files = files_in.contains(",") ? files_in.split(",") : new String[]{files_in};
            for (String file : files) {
                try (MessageFileReader reader = new MessageFileReader(file)) {
                    reader.parse((HttpMessage message) -> {
                        messages_read++;
                        if (transform(message)) {
                            writer.write(message);
                            if (messages_written++ % 1000 == 0) status();
                        }
                    });
                }
            }
        }

        status();  // show final status
    }

    /**
     * Transform message before writing, returning false if message should not be written.
     */
    private boolean transform(HttpMessage message) {
        try {
            // filter out duplicate calls if configured
            if (transform_duplicates.name.equals("drop")) {
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
                String digest = bytesToHex(d.digest());
                if (hashes.contains(digest)) return false;
                hashes.add(digest);
            }

            // transform interval as configured
            switch (transform_interval_millis.name) {
                case "drop":
                    message.set_interval_millis(0);
                    break;
                case "randomize":
                    message.set_interval_millis((int) (Math.random() * transform_interval_millis.amount));
                    break;
            }

            // transform response time as configured
            long current = message.response_time_millis() == 0 ? started : message.response_time_millis();
            switch (transform_response_time_millis.name) {
                case "drop":
                    message.set_response_time_millis(0);
                    break;
                case "add":
                    message.set_response_time_millis(current + transform_response_time_millis.amount);
                    break;
                case "subtract":
                    message.set_response_time_millis(current - transform_response_time_millis.amount);
                    break;
                case "shuffle":
                    message.set_response_time_millis(started - (long) (Math.random() * transform_response_time_millis.amount));
                    break;
            }

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

    private final Set<String> hashes = new HashSet<>();
    private long messages_read = 0;
    private long messages_written = 0;
    private final long started = System.currentTimeMillis();
    private final ParsedOperation transform_duplicates;
    private final ParsedOperation transform_interval_millis;
    private final ParsedOperation transform_response_time_millis;

    /**
     * Helper class for parsed transformer operations.
     */
    static class ParsedOperation {

        /**
         * Parsing constructor using regex for verification.
         */
        ParsedOperation(String key, String regex) {
            String value = System.getProperty(key, "keep");
            System.out.println(key + "=" + value);

            Matcher m = Pattern.compile(regex).matcher(value);
            if (!m.matches()) throw new RuntimeException("Invalid operation: " + value);

            value = m.group(1);
            if (value.contains(":")) {
                String[] x = value.split(":");
                name = x[0];
                amount = parseAmount(x[1]);
            } else {
                name = value;
                amount = null;
            }
        }

        /**
         * Parse amount as milliseconds.
         */
        private static long parseAmount(String amount) {
            int lastIdx = amount.length() - 1;
            char lastChar = amount.charAt(lastIdx);
            long conversionFactor = 1;
            if (!Character.isDigit(lastChar)) {
                amount = amount.substring(0, lastIdx);
                switch (lastChar) {
                    case 'y': // year
                        conversionFactor *= 12;
                    case 'm': // month
                        conversionFactor *= 4;
                    case 'w': // week
                        conversionFactor *= 7;
                    case 'd': // day
                        conversionFactor *= 24;
                    case 'h': // hour
                        conversionFactor *= 60;
                    case 'n': // minute
                        conversionFactor *= 60;
                    case 's': // second
                        conversionFactor *= 1000;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid amount unit: " + amount);
                }
            }
            return Long.parseLong(amount) * conversionFactor;
        }

        final String name;
        final Long amount;
    }

}