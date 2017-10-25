package com.voipgrid.vialer.t9;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Class to match a T9Query to a name.
 */
public class T9NameMatcher {

    /**
     * Function to check if a t9 query matches a display name.
     * @param query T9 query to match.
     * @param displayName Display name to match.
     * @return Whether the t9 query matched the display name.
     */
    public static boolean T9QueryMatchesName(String query, String displayName) {
        ArrayList<String> possibleQueries = T9Query.generateT9NameQueries(displayName);

        Collections.sort(possibleQueries, new T9QueryComparator());

        for (int i = 0; i < possibleQueries.size(); i++) {
            if (possibleQueries.get(i).startsWith(query)){
                return true;
            }
        }
        return false;
    }

    /**
     * Function that surrounds the matched part in the name with <b></b>
     * @param t9Query The query that is matched on.
     * @param displayName The name.
     * @return Name with <b></b> tags.
     */
    public static String highlightMatchedPart(String t9Query, String displayName) {
        ArrayList<String> possibleQueries = T9Query.generateT9NameQueries(displayName);
        String queryOfWholeName = possibleQueries.get(0);

        String SpacelessDisplayName = displayName.replaceAll(" ", "");

        int start = queryOfWholeName.indexOf(t9Query);
        int end = start + t9Query.length();

        // Create a string with the highlighted part without whitespace.
        StringBuilder builder = new StringBuilder();
        builder.append(SpacelessDisplayName.substring(0, start));
        builder.append("<b>" + SpacelessDisplayName.substring(start, end) + "</b>");
        builder.append(SpacelessDisplayName.substring(end, SpacelessDisplayName.length()));

        String matchedWithoutSpaces = builder.toString();

        // Clear builder.
        builder.setLength(0);

        // Loop over indexes of existing spaces.
        int index = displayName.indexOf(" ");
        int previousIndex = 0;
        int count = 0;
        while (index >= 0) {
            // Start of the substring.
            int subStart = previousIndex;
            int subEnd;

            if (index > (start + count) && index < (end + count)) {
                // The whitespace is between the <b> tags so add 3 for the first <b>.
                subEnd = index + 3;
            } else if (index >= end){
                // The whitespace is after the <b> tags so add 7 for the <b> and </b>.
                subEnd = index + 7;
            } else {
                // The whitespace is before the <b> tags.
                subEnd = index;
            }

            // Subtract the amount of whitespace added.
            subEnd -= count;

            // Create a substring with trailing whitespace.
            builder.append(matchedWithoutSpaces.substring(subStart, subEnd) + " ");

            previousIndex = subEnd;
            count++;
            index = displayName.indexOf(" ", index + 1);
        }

        builder.append(matchedWithoutSpaces.substring(previousIndex, matchedWithoutSpaces.length()));
        String fixedResult = dealWithSpecialCharacter(builder.toString(), start, end);
        return fixedResult;
    }

    /**
     * Fixes inaccuracies in bolding when special chars are in the string.
     * This method moves the missing chars from outside the tags to the inside.
     */
    private static String dealWithSpecialCharacter(String boldedText, int start, int end) {
        String boldString = boldedText.split("<b>")[1].split("</b>")[0];
        // Keeping a count of the number of special chars
        int specialCharcount = boldString.length() - boldString.replaceAll("[^A-Za-z0-9 ]","").length();

        // With no special chars, no need to work on the string any more.
        if (specialCharcount < 1) {
            return boldedText;
        }

        String beforeFirstTag = "";
        if (boldedText.startsWith("<b>")) {
            beforeFirstTag = boldedText.split("<b>")[0];
        }
        String inbetweenTags = boldedText.split("<b>")[1].split("</b>")[0];
        String afterEndTag = "";
        if (!boldedText.endsWith("<b>")) {
            afterEndTag = boldedText.split("</b>")[1];
        }
        int specialCharcountInCut = inbetweenTags.length() - inbetweenTags.replaceAll("[^A-Za-z0-9 ]","").length();
        int spacesInCut = inbetweenTags.length() - inbetweenTags.replaceAll(" ", "").length();
        if (specialCharcountInCut > 0) {

            String firstCut = afterEndTag.substring(0, specialCharcountInCut+spacesInCut+1);
            int unExpectedSpecialChars = firstCut.length() - firstCut.replaceAll("[^A-Za-z0-9 ]","").length();
            if (unExpectedSpecialChars > 0) {
                firstCut = afterEndTag.substring(0, specialCharcountInCut+spacesInCut+1+unExpectedSpecialChars);
            }
            String secondCut = afterEndTag.substring(firstCut.length(), afterEndTag.length());
            return beforeFirstTag + "<b>" + inbetweenTags + firstCut + "</b>" + secondCut;
        }
        return boldedText;
    }

    /**
     * Custom Comparator to sort the list based on string length DESC.
     */
    private static class T9QueryComparator implements Comparator<String> {

        public int compare(String s1, String s2) {
            return s2.length() - s1.length();
        }
    }

}
