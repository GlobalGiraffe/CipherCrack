package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

/**
 * Class that contains methods to assist with Asmco Cipher operations
 * This transposition cipher places letters in columns, usually 1 or 2 at a time
 * and then reads the columns vertically
 */
public class Amsco extends Cipher {

    // delete the keyword / heading if their 'X' is pressed
    private static final View.OnClickListener AMSCO_ON_CLICK_DELETE = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.extra_amsco_keyword_delete:
                    EditText k = v.getRootView().findViewById(R.id.extra_amsco_keyword);
                    k.setText("");
                    break;
                case R.id.extra_amsco_chars_per_cell_delete:
                    EditText ch = v.getRootView().findViewById(R.id.extra_amsco_chars_per_cell);
                    ch.setText("");
                    break;
            }
        }
    };

    private int[] permutation = null;    // e.g. 2,1,3 for "KEY"
    private int[] charsPerCell = null;   // e.g. 1,2

    Amsco(Context context) { super(context, "Amsco"); }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (permutation == null) {
            dest.writeInt(0);
            dest.writeInt(0);
        } else {
            dest.writeInt(permutation.length);
            dest.writeIntArray(permutation);
        }
        if (charsPerCell == null) {
            dest.writeInt(0);
            dest.writeInt(0);
        } else {
            dest.writeInt(charsPerCell.length);
            dest.writeIntArray(charsPerCell);
        }
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        int permutationSize = in.readInt();
        permutation = new int[permutationSize];
        in.readIntArray(permutation);
        int charsPerCellSize = in.readInt();
        charsPerCell = new int[charsPerCellSize];
        in.readIntArray(charsPerCell);
    }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getCipherDescription() {
        return "TODO: The Amsco cipher is a transposition cipher where letters are written out in a zig-zag across multiple railfences and then cipher text read along the rails. " +
                "For example, if we have 3 rails then 'THISISASECRETMESSAGE' is encoded as:\n" +
                "T   I   E   T   S\n" +
                " H S S S C E M S A E\n" +
                "  I   A   R   E   G\n" +
                "To decipher, the number of rails is known and the above is reconstructed from the cipher text.\n\n"+
                "This can be broken by trying with a number of different rails and looking for cribs - there are only a limited number of rails possible.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher ("+numbersToString(permutation)+":"+numbersToString(charsPerCell)+")";
    }

    /**
     * Determine whether the directives are valid for this cipher type, and sets if they are
     * @param dirs the directives to be checked and set
     * @return return the reason for being invalid, or null if the directives ARE valid (and set)
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String reason = super.canParametersBeSet(dirs);
        if (reason != null)
            return reason;
        int[] perm = dirs.getPermutation();
        int[] cpc = dirs.getCharsPerCell();
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            if (perm == null || perm.length == 0)
                return "Permutation is not valid";
            Set<Integer> permSet = new HashSet<>(perm.length*2);
            for (int p : perm) {
                if (p >= perm.length)
                    return "Permutation element "+p+" is too large";
                if (permSet.contains(p)) {
                    return "Permutation element "+p+" is repeated";
                }
                permSet.add(p);
            }
            if (cpc == null || cpc.length == 0)
                return "Chars per Cell is not valid";
            Set<Integer> cpcSet = new HashSet<>(cpc.length*2);
            for (int p : cpc) {
                if (p <= 0)
                    return "Chars per Cell element "+p+" is too small";
                if (cpcSet.contains(p)) {
                    return "Chars per Cell element "+p+" is repeated";
                }
                cpcSet.add(p);
            }
        } else {
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
            if (crackMethod != CrackMethod.BRUTE_FORCE)
                return "Invalid crack method";
        }
        this.permutation = perm;
        this.charsPerCell = cpc;
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_amsco);

        Cipher.addInputFilters(layout, R.id.extra_amsco_keyword, true, 30, PERM_INPUT_FILTER);
        Cipher.addInputFilters(layout, R.id.extra_amsco_chars_per_cell, true, 30, PERM_INPUT_FILTER);

        // ensure we 'delete' the keyword text when the delete button is pressed
        Button keywordDelete = layout.findViewById(R.id.extra_amsco_chars_per_cell_delete);
        keywordDelete.setOnClickListener(AMSCO_ON_CLICK_DELETE);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText keywordView = layout.findViewById(R.id.extra_amsco_keyword);
        String entry = keywordView.getText().toString();
        int[] columns = convertKeywordToColumns(entry,0);
        dirs.setPermutation(columns);

        EditText charsPerCellView = layout.findViewById(R.id.extra_amsco_chars_per_cell);
        entry = charsPerCellView.getText().toString();
        columns = convertKeywordToColumns(entry,1);
        dirs.setCharsPerCell(columns);
    }

    // we don't add any extra controls, but we allow change of cribs
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String cipherText,
                                    Language language, String alphabet, String paddingChars) {
        return true;
    }

    /**
     * Encode a text using Amsco cipher with the given parameters
     * @param plainText the text to be encoded
     * @param dirs a group of directives, we need PERMUTATION (int[]) and CHARSPERCELL (int[])
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        int[] perm = dirs.getPermutation();
        int[] cpc = dirs.getCharsPerCell();

        // build up some lists of arrays with text in
        String plainTextNoPad = plainText.replaceAll("\\W","");
        List<String[]> cells = new LinkedList<>();
        int rowNo = 0;
        for(int pos=0; pos < plainTextNoPad.length(); rowNo++) {
            String[] row = new String[perm.length];
            int charsPerCellPos = rowNo % cpc.length;
            for(int col=0; col < perm.length; col++) {
                if (pos < plainTextNoPad.length()) {
                    int charsToAddToCell = cpc[charsPerCellPos]; // 1 or 2
                    while (pos+charsToAddToCell > plainTextNoPad.length()) {
                        charsToAddToCell--;
                    }
                    row[col] = plainTextNoPad.substring(pos, pos + charsToAddToCell);
                    pos += charsToAddToCell;
                } else {
                    row[col] = "";
                }
                charsPerCellPos = (charsPerCellPos+1)%cpc.length;
            }
            cells.add(row);
        }

        // now read the cells we've built up in correct column order
        // e.g. if 2,1,3 : read down column 2, then down column 1, then down column 3
        StringBuilder result = new StringBuilder(plainText.length());
        for (int col : perm) {
            for (String[] row : cells) {
                result.append(row[col]);
            }
        }
        return result.toString();
    }

    /**
     * Decode a cipher text that was encoded with Amsco and the given permutation and chars-per-cell
     * @param cipherText the text to be decoded
     * @param dirs a group of directives, we need PERM (int[]) and CHARS-PER-CELL (int[])
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        int[] perm = dirs.getPermutation();
        int[] cpc = dirs.getCharsPerCell();
        String cipherTextNoPad = cipherText.replaceAll("\\W","");

        // first work out how long each column would be...
        int rowNo = 0;
        List<int[]> table = new LinkedList<>();
        for (int pos=0; pos < cipherTextNoPad.length(); rowNo++) {
            int[] row = new int[perm.length];
            for (int col=0; col < perm.length; col++) {
                int charsToAddPos = ((rowNo % cpc.length) + col) % cpc.length;
                int charsToAdd = cpc[charsToAddPos];
                while (pos+charsToAdd > cipherTextNoPad.length()) {
                    charsToAdd--;
                }
                row[col] = charsToAdd;
                pos += charsToAdd;
            }
            table.add(row);
        }

        // now put the cipher text in each column in turn...
        String[][] grid = new String[table.size()][perm.length];
        int pos = 0;
        for (int permCol : perm) {
            for (int row=0; row < table.size(); row++) {
                int charsAtThisPos = table.get(row)[permCol];
                if (charsAtThisPos > 0) {
                    grid[row][permCol] = cipherTextNoPad.substring(pos, pos + charsAtThisPos);
                    pos += charsAtThisPos;
                } else {
                    grid[row][permCol] = "";
                }
            }
        }

        // now just read through the grid row by row and col by col...
        StringBuilder result = new StringBuilder(cipherText.length());
        for (String[] row : grid) {
            for (String cell : row) {
                result.append(cell);
            }
        }

        return result.toString();
    }

    /**
     * Crack an Amsco cipher by brute force - trying several column permutations and chars-per-cell
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    public CrackResult crackBruteForce(String cipherText, Directives dirs, int crackId) {
        // TODO : Amsco crack with different permutations, different chars-per-cell (1,2 & 2,1)
        // and forward/reverse
        return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Amsco Brute Force crack has not yet been implemented");
    }

    /**
     * Crack an Amsco cipher
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        return crackBruteForce(cipherText, dirs, crackId);
    }
}
