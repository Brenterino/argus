package dev.zygon.argus.client.test;

import java.util.regex.Pattern;

public class PearlPatternMatch {

    // 66 steps, 0.1ms
    private static final String PATTERN_TEXT = "(.{2}\\[(\\w+)]\\s)?.{2}(Your pearl is held by|The pearl of .{2}(\\w+) is held by)\\s.{2}(\\w+)\\s.{2}\\[(-?\\d+)\\s(-?\\d+)\\s(-?\\d+)\\s(\\w+)]";

    private static final Pattern PATTERN = Pattern.compile(PATTERN_TEXT);

    public static void main(String[] args) {
        var pearled = "§oYour pearl is held by §6Zygon §7[5000 -6000 2000 world]";
        var singleBroadcast = "§oThe pearl of §bS4NTA is held by §6Zygon §7[-1000 2000 5000 world]";
        var groupBroadcast = "§b[Estalia] §oThe pearl of §bS4NTA is held by §6Zygon §7[-10 500 -1000 world]";

        System.out.println("Pattern = " + PATTERN_TEXT);
        System.out.println();
        doMatch(pearled);
        doMatch(singleBroadcast);
        doMatch(groupBroadcast);
    }

    private static void doMatch(String text) {
        System.out.println("Text = " + text);
        System.out.println();
        var matcher = PATTERN.matcher(text);
        if (matcher.matches()) {
            System.out.println("Group: " + matcher.group(2));
            System.out.println("Pearled: " + matcher.group(4));
            System.out.println("Holder: " + matcher.group(5));
            System.out.println("X: " + matcher.group(6));
            System.out.println("Y: " + matcher.group(7));
            System.out.println("Z: " + matcher.group(8));
            System.out.println("Dimension: " + matcher.group(9));
        } else {
            System.out.println("Pattern did not match.");
        }
        System.out.println();
    }
}
