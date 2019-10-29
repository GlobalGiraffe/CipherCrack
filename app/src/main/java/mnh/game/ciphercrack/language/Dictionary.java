package mnh.game.ciphercrack.language;

import java.util.HashSet;

public class Dictionary extends HashSet<String> {

    private static final int DEFAULT_INITIAL_SIZE = 3500;
    static final String NONE = ""; // no dictionary available

    // a dictionary will have thousands of entries, so ensure we ALWAYS specify size
    Dictionary() { super(DEFAULT_INITIAL_SIZE); }
}
