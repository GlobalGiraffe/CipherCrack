package mnh.game.ciphercrack.transform;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

/**
 * Swap rows for columns and vice-versa, so this:
 * ABCDE
 * FGHIJ
 * KLMNO
 * becomes:
 * AFK
 * BGL
 * CHM
 * DIN
 * EJO
 */
public class SwapRowsAndCols implements Transform {

    @Override
    public String apply(Context context, String text) {
        if (text == null)
            return null;

        // Each row will be a LinkedList of letters, so we'll have a list of LinkedLists
        // we collect in this form and then cycle down the first list,
        // pulling an item from each LinkedList in turn
        List<LinkedList<Character>> listOfLists= new LinkedList<>();
        LinkedList<Character> currentLine = null;
        for(int pos=0; pos < text.length(); pos++) {
            if (currentLine == null) {
                currentLine = new LinkedList<>();
            }
            if (text.charAt(pos) == '\n') {
                listOfLists.add(currentLine);
                currentLine = null;
            } else {
                currentLine.add(text.charAt(pos));
            }
        }
        // could be residual, no CR on last line
        if (currentLine != null && currentLine.size() > 0)
            listOfLists.add(currentLine);

        // hold the overall text result
        StringBuilder result = new StringBuilder(text.length());
        StringBuilder resultLine = new StringBuilder();

        // now scoot through down the rows, taking first letter each time to add to result string
        // and add /n at end of each loop before taking next char from each original line
        boolean finished = false;
        while (!finished) {
            finished = true;
            resultLine.setLength(0);
            for (int line=0; line < listOfLists.size(); line++) {
                LinkedList<Character> lineOfChars = listOfLists.get(line);
                if (lineOfChars.size() > 0) {
                    finished = false;
                    char c = lineOfChars.removeFirst();
                    resultLine.append(c);
                } else {
                    resultLine.append(" ");
                }
            }
            if (!finished)
                result.append(resultLine).append("\n");
        }
        return result.toString();
    }
}
