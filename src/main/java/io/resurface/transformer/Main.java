// © 2016-2023 Graylog, Inc.

package io.resurface.transformer;

import java.util.Random;

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
        String operation = System.getProperty("OPERATION");
        String amount = System.getProperty("AMOUNT");
        if (operation == null && amount != null) throw new IllegalArgumentException("Must specify OPERATION");
        long millis = amount == null ? 0 : parseAmount(amount);

        String[] files_in = file_in.contains(",") ? file_in.split(",") : new String[]{ file_in };

        // transform all messages
        try (MessageFileWriter writer = new MessageFileWriter(file_out)) {
            for (String file : files_in) {
                try (MessageFileReader reader = new MessageFileReader(file)) {
                    reader.parse((HttpMessage message) -> {
                        messages_read++;
                        if (transform(message, operation, millis)) {
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
    private boolean transform(HttpMessage message, String operation, long millis) {
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

            if (operation == null) operation = "";
            long current = message.response_time_millis() == 0 ? started : message.response_time_millis();
            switch (operation) {
                case "add":
                    message.set_response_time_millis(current + millis);
                    break;
                case "sub":
                    message.set_response_time_millis(current - millis);
                    break;
                case "span1y":
                    message.set_response_time_millis((random.nextLong() % millis_per_year) + a_year_ago);
                    break;
                case "span3m":
                    message.set_response_time_millis((random.nextLong() % (3 * millis_per_month)) + three_months_ago);
                    break;
                case "join":
                    // TODO join two datasets
                    //break;
                default:
                    // reset response time to now
                    message.set_response_time_millis(0);
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
     * Parse time to millis
     */
    private static long parseAmount(String amount) {
        int lastIdx = amount.length() - 1;
        char lastChar = amount.charAt(lastIdx);
        int conversionFactor = 1;
        if (!Character.isDigit(lastChar)) {
            amount = amount.substring(0, lastIdx);
            switch (lastChar) {
                case 'y':
                    // year
                    conversionFactor *= 12;
                case 'm':
                    // month
                    conversionFactor *= 4;
                case 'w':
                    // week
                    conversionFactor *= 7;
                case 'd':
                    // day
                    conversionFactor *= 24;
                case 'h':
                    // hour
                    conversionFactor *= 60;
                case 'n':
                    // minute
                    conversionFactor *= 60;
                case 's':
                    // second
                    conversionFactor *= 1000;
                    break;
                default:
                    throw new IllegalArgumentException("Wrong AMOUNT unit");
            }
        }
        return Long.parseLong(amount) * conversionFactor;
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
    private final long millis_per_year = 365L * 24 * 60 * 60 * 1000;
    private final long millis_per_month = 30L * 24 * 60 * 60 * 1000;
    private final long a_year_ago = started - millis_per_year;
    private final long three_months_ago = started - 3 * millis_per_month;
    private final Random random = new Random();
}