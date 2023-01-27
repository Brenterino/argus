package dev.zygon.argus.client.test;

import java.util.regex.Pattern;

public class OverlayPatternTest {

    private static final String PATTERN_TEXT = ".{2}(Location:)\\s.{2}\\((\\w+)\\)\\s\\[(-?\\d+)\\s(-?\\d+)\\s(-?\\d+)]\\s.{2}(Name:)\\s.{2}(\\w+)\\s.{2}(Group:)\\s.{2}(\\w+)";

    private static final Pattern PATTERN = Pattern.compile(PATTERN_TEXT);

    public static void main(String[] args) {
        var text = """
                §6Location: §b(world) [3082 -69 2249]
                §6Name: §bComradeNickHouseMtA
                §6Group: §bGlobal""";

        System.out.println("Pattern = " + PATTERN_TEXT);
        System.out.println();
        System.out.println("Text = " + text);
        System.out.println();
        var matcher = PATTERN.matcher(text);
        if (matcher.matches()) {
            System.out.println("Dimension: " + matcher.group(2));
            System.out.println("X: " + matcher.group(3));
            System.out.println("Y: " + matcher.group(4));
            System.out.println("Z: " + matcher.group(5));
            System.out.println("Snitch: " + matcher.group(7));
            System.out.println("Group: " + matcher.group(9));
        } else {
            System.out.println("Pattern did not match.");
        }
    }
}
