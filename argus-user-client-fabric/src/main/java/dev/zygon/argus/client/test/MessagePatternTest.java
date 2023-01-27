package dev.zygon.argus.client.test;

import java.util.regex.Pattern;

public class MessagePatternTest {

    private static final String PATTERN_TEXT = ".{2}(Login|Logout|Enter)\\s{2}.{2}(\\w+)\\s{2}.{2}(\\w+)\\s{2}.{2}\\[(-?\\d+)\\s(-?\\d+)\\s(-?\\d+)]\\s{2}.{2}\\[(\\d.+)m\\s.{2}(\\w+\\s\\w+).{2}]";

    private static final Pattern PATTERN = Pattern.compile(PATTERN_TEXT);

    public static void main(String[] args) {
        var text = "§6Enter  §ajbblocker  §bComradeNickHouseMtA  §e[-3082 69 2249]  §a[1594m §cSouth East§a]";

        System.out.println("Pattern = " + PATTERN_TEXT);
        System.out.println();
        System.out.println("Text = " + text);
        System.out.println();
        var matcher = PATTERN.matcher(text);
        if (matcher.matches()) {
            System.out.println("Action: " + matcher.group(1));
            System.out.println("Player: " + matcher.group(2));
            System.out.println("Snitch: " + matcher.group(3));
            System.out.println("X: " + matcher.group(4));
            System.out.println("Y: " + matcher.group(5));
            System.out.println("Z: " + matcher.group(6));
            System.out.println("Distance: " + matcher.group(7));
            System.out.println("Cardinality: " + matcher.group(8));
        } else {
            System.out.println("Pattern did not match.");
        }
    }
}
