/*
 * This file come from FunnyGuilds project.
 * https://github.com/FunnyGuilds/FunnyGuilds
 * Licensed under Apache License 2.0.
 */
package pl.toranktto.autoevents.util;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static long parseTime(String string) {
        if (string == null || string.isEmpty()) {
            throw new RuntimeException("string cannot be null or empty.");
        }

        Stack<Character> type = new Stack<>();
        StringBuilder value = new StringBuilder();

        boolean calc = false;
        long time = 0;

        for (char c : string.toCharArray()) {
            switch (c) {
                case 'd':
                case 'h':
                case 'm':
                case 's':
                    if (!calc) {
                        type.push(c);
                    }

                    try {
                        long i = Integer.parseInt(value.toString());
                        switch (type.pop()) {
                            case 'd':
                                time += i * 86400L;
                                break;
                            case 'h':
                                time += i * 3600L;
                                break;
                            case 'm':
                                time += i * 60L;
                                break;
                            case 's':
                                time += i;
                                break;
                        }
                    } catch (NumberFormatException e) {
                        return time;
                    }

                    type.push(c);
                    calc = true;
                    break;
                default:
                    value.append(c);
                    break;
            }
        }

        return time;
    }

    public static String getDurationBreakdown(long time) {
        if (time == 0) {
            return "now";
        }

        long days = TimeUnit.SECONDS.toDays(time);
        if (days > 0) {
            time -= TimeUnit.DAYS.toSeconds(days);
        }

        long hours = TimeUnit.SECONDS.toHours(time);
        if (hours > 0) {
            time -= TimeUnit.HOURS.toSeconds(hours);
        }

        long minutes = TimeUnit.SECONDS.toMinutes(time);
        if (minutes > 0) {
            time -= TimeUnit.MINUTES.toSeconds(minutes);
        }

        long seconds = TimeUnit.SECONDS.toSeconds(time);

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days);
            sb.append(" d ");
        }

        if (hours > 0) {
            sb.append(hours);
            sb.append(" h ");
        }

        if (minutes > 0) {
            sb.append(minutes);
            sb.append(" min ");
        }

        if (seconds > 0) {
            sb.append(seconds);
            sb.append(" s ");
        }

        return sb.substring(0, sb.length() - 1);
    }
}