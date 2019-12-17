package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.CrackState;
import mnh.game.ciphercrack.util.Directives;

/**
 * Class that contains methods to assist with Hill Cipher operations
 * This poly-graphic substitution cipher uses matrix multiplication to encrypt messages
 */
public class Hill extends Cipher {

    // maximum length of a Hill keyword
    private static final int MAX_KEYWORD_LENGTH = 50;

    // only allow A-Z, 0-9 and ',' in the keyword field
    private static final InputFilter MATRIX_INPUT_FILTER = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            boolean hasAlpha = false;
            boolean hasDigit = false;
            boolean hasComma = false;
            // check what chars the buffer to be added has
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != ',') {
                    return "";
                }
                hasAlpha = (hasAlpha || Character.isAlphabetic(c));
                hasDigit = (hasDigit || Character.isDigit(c));
                hasComma = (hasComma || c == ',');
            }
            // if we are adding alpha and the dest already contains digit or ',', don't allow add
            if (hasAlpha && dest.length() > 0
                    && (Character.isDigit(dest.charAt(dest.length()-1))
                    || dest.charAt(dest.length()-1) == ','))
                return "";
            // if we're adding digit or comma and dest already has alpha, don't allow the add
            if ((hasDigit || hasComma) && dest.length() > 0
                    && Character.isAlphabetic(dest.charAt(dest.length()-1)))
                return "";
            return null;
        }
    };

    // delete the relevant field if 'X' is pressed
    private static final View.OnClickListener MATRIX_ON_CLICK_DELETE = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.extra_hill_keyword_delete:
                    EditText k = v.getRootView().findViewById(R.id.extra_hill_keyword);
                    k.setText("");
                    break;
                case R.id.extra_hill_crack_rows_cols_delete:
                    EditText s = v.getRootView().findViewById(R.id.extra_hill_crack_rows_cols);
                    s.setText("");
                    break;
                case R.id.extra_hill_crack_crib_drag_delete:
                    EditText c = v.getRootView().findViewById(R.id.extra_hill_crack_crib_drag);
                    c.setText("");
                    break;
            }
        }
    };

    // reassess which fields to see when crack methods chosen
    private static final View.OnClickListener CRACK_METHOD_ASSESSOR = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LinearLayout lSize = v.getRootView().findViewById(R.id.extra_hill_crack_layout_rows_cols);
            LinearLayout lCribs = v.getRootView().findViewById(R.id.extra_hill_crack_layout_crib_drag);
            switch (v.getId()) {
                case R.id.crack_button_dictionary:
                    lSize.setVisibility(View.GONE);
                    lCribs.setVisibility(View.GONE);
                    break;
                case R.id.crack_button_brute_force:
                    lSize.setVisibility(View.VISIBLE);
                    lCribs.setVisibility(View.GONE);
                    break;
                case R.id.crack_button_crib_drag:
                    lSize.setVisibility(View.VISIBLE);
                    lCribs.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    /**
     * Given a keyword, convert to a square matrix, e.g. BETA => [1,4,19,0]
     * @param keyword the keyword to convert to matrix, e.g. ZEBR => [25,4,1,17]
     * @param alphabet gives the ordinal number of each letter
     * @param across if true then fill across the matrix, else fill down
     * @return the matrix represented by the keyword, or null if any errors
     */
    static int[] convertKeywordToMatrix(String keyword, String alphabet, boolean across) {
        // example BETA => [1,4,19,0] when across = true
        // example BETA => [1,19,4,0] when across = false
        if (keyword == null)
            return null;
        if (keyword.length() != 4 && keyword.length() != 9 && keyword.length() != 16)
            return null;
        int cols = (int)(Math.sqrt(keyword.length())+0.1);
        keyword = keyword.toUpperCase();
        int[] result = new int[keyword.length()];
        for (int i=0; i<keyword.length(); i++) {
            char keyChar = keyword.charAt(i);
            int posInAlphabet = alphabet.indexOf(keyChar);
            if (posInAlphabet < 0) // not a letter in our alphabet
                return null;
            if (across) { // fill across and then down
                result[i] = posInAlphabet;
            } else { // fill down and then across
                // for 2x2: 0=>0, 1=>2, 2=>1, 3=>3
                // for 3x3: 0=>0, 1=>3, 2=>6, 3=>1, 4=>4, 5=>7, 6=>2, 7=>5, 8=>8
                result[(i % cols)*cols + (i / cols)] = posInAlphabet;
            }
        }
        return result;
    }

    /**
     * Static method to convert a matrix array to a string
     * @param matrix group of integers, e.g. [1,2,0,3] representing a matrix
     * @return the equivalent matrix as a comma-separated string, e.g. "1,2,0,3"
     */
    @NotNull
    static String matrixToString(int[] matrix) {
        return numbersToString(matrix);
    }

    /**
     * Static method to convert a matrix array to a string of alphabetic letters
     * @param matrix group of integers, e.g. [7,8,11,11] representing a matrix
     * @param alphabet the alphabet whose letters are index by the matrix elements as ordinals
     * @return the equivalent matrix as the letters of the alphabet, e.g. "HILL"
     */
    @NotNull
    private static String matrixToKeyword(int[] matrix, String alphabet) {
        StringBuilder sb = new StringBuilder(matrix.length);
        for(Integer element : matrix) {
            if (element < 0 || element >= alphabet.length())
                sb.append("*");
            else
                sb.append(alphabet.charAt(element));
        }
        return sb.toString();
    }

    /**
     * Perform a matrix multiplication modulus a certain number
     * @param matrix square matrix to be multiplied
     * @param vector vector to multiply by
     * @param modulus the modulus to take to keep numbers under this
     * @param result where to place the result
     */
    static void multiply(int[] matrix, int[] vector, int modulus, int[] result) {
        int size = (int)(Math.sqrt(matrix.length)+0.001);
        int vectorCols = (vector.length == matrix.length)? size : vector.length/size;
        for (int vRow = 0; vRow < size; vRow++) {
            for (int vCol = 0; vCol < vectorCols; vCol++) {
                int sum = 0;
                for (int col = 0; col < size; col++) {
                    sum += matrix[vRow*size + col] * vector[col*vectorCols + vCol];
                }
                result[vRow*vectorCols+vCol] = sum % modulus;
            }
        }
    }

    /**
     * Calculate the determinant of a 2x2 or 3x3 matrix,
     * For 4x4, see https://en.wikipedia.org/wiki/Determinant#Definition
     * @param m the matrix whose determinant is needed
     * @return the determinant of the matrix, or 0 if matrix is not 2x2 or 3x3
     */
    private static int getDeterminant(int[] m) {
        if (m.length == 4) {
            // det |A| = ad - bc
            return m[0]*m[3] - m[1]*m[2];
        } else {
            if (m.length == 9) {
                // for 3x3 matrix, det |A| = a * |[efhi]| - b * |[dfgi]| + c * |[degh]|
                int[] sub = new int[4];
                sub[0] = m[4]; sub[1] = m[5]; sub[2] = m[7]; sub[3] = m[8];
                int detA = getDeterminant(sub);
                sub[0] = m[3]; sub[1] = m[5]; sub[2] = m[6]; sub[3] = m[8];
                int detB = getDeterminant(sub);
                sub[0] = m[3]; sub[1] = m[4]; sub[2] = m[6]; sub[3] = m[7];
                int detC = getDeterminant(sub);
                return m[0] * detA - m[1] * detB + m[2] * detC;
            } else {
                // TODO: calculate determinant of 5x5 matrix
                return 0;
            }
        }
    }

    /**
     * Calculate the determinant of the matrix, modulus the length of the alphabet
     * @param m the matrix whose determinant is required
     * @param modulus the number to modulus by
     * @return the determinant
     */
    private static int getDeterminant(int[] m, int modulus) {
        int d = getDeterminant(m);
        d = d % modulus;
        if (d < 0)
            d += modulus;
        return d;
    }

    /**
     * Helper method to calculate the inverse, modulus some number, of a value
     * @param value the value to be used
     * @param modulus the modulus to apply
     * @param inverse the inverse number to multiply by
     * @return the calculated result
     */
    private static int applyInverseWithModulus(int value, int modulus, int inverse) {
        // take the modulus, but also make sure it is in the positive range 0 .. (modulus-1)
        value %= modulus;
        if (value < 0)
            value += modulus;
        // apply the inverse but also ensure in range 0 .. (modulus - 1)
        return (value * inverse) % modulus;
    }

    /**
     * Invert a square matrix using the determinant and modulus, e.g. 26
     * https://en.wikipedia.org/wiki/Invertible_matrix#Inversion_of_2_%C3%97_2_matrices
     * @param m the matrix to be inverted
     * @param modulus the modulus to keep numbers under
     * @param result where the result will be put
     * @return true if the inverse can be calculated, false otherwise
     */
    static boolean invertMatrix(int[] m, int modulus, int[] result, boolean checkCoPrimes) {
        // Calculate the positive determinant modulus alphabet length
        int d = getDeterminant(m, modulus);
        // not invertible, matrix is 'singular'
        if (d == 0)
            return false;
        // inverse matrix does exist, but does not uniquely encode/decode
        if (checkCoPrimes && !areCoPrimes(d, modulus))
            return false;

        // calculate the modular multiplicative inverse of the determinant
        // we're looking for m such that d*m mod (alphabet-length) = 1
        //  see https://en.wikipedia.org/wiki/Hill_cipher#Example
        //  and https://en.wikipedia.org/wiki/Modular_multiplicative_inverse
        int inverse = -1;
        for (int i=1; i < modulus; i++) {
            if ((d*i % modulus) == 1) {
                inverse = i;
                break;
            }
        }
        if (inverse == -1) // no inverse of the determinant could be found!
            return false;

        // now calculate the inverse matrix
        // for a 2x2 matrix
        if (m.length == 4) {
            result[0] = m[3] % modulus;
            result[0] = ((result[0] + (result[0] < 0 ? modulus:0)) * inverse) % modulus;
            result[1] = (-m[1]) % modulus;
            result[1] = ((result[1] + (result[1] < 0 ? modulus:0)) * inverse) % modulus;
            result[2] = (-m[2]) % modulus;
            result[2] = ((result[2] + (result[2] < 0 ? modulus:0)) * inverse) % modulus;
            result[3] = m[0] % modulus;
            result[3] = ((result[3] + (result[3] < 0 ? modulus:0)) * inverse) % modulus;
            return true;
        }
        // for a 3x3 matrix
        if (m.length == 9) {
            result[0] = applyInverseWithModulus(m[4]*m[8]-m[5]*m[7], modulus, inverse);
            result[1] = applyInverseWithModulus(-(m[1]*m[8]-m[2]*m[7]), modulus, inverse);
            result[2] = applyInverseWithModulus(m[1]*m[5]-m[2]*m[4], modulus, inverse);
            result[3] = applyInverseWithModulus(-(m[3]*m[8]-m[5]*m[6]), modulus, inverse);
            result[4] = applyInverseWithModulus(m[0]*m[8]-m[2]*m[6], modulus, inverse);
            result[5] = applyInverseWithModulus(-(m[0]*m[5]-m[2]*m[3]), modulus, inverse);
            result[6] = applyInverseWithModulus(m[3]*m[7]-m[4]*m[6], modulus, inverse);
            result[7] = applyInverseWithModulus(-(m[0]*m[7]-m[1]*m[6]), modulus, inverse);
            result[8] = applyInverseWithModulus(m[0]*m[4]-m[1]*m[3], modulus, inverse);
            return true;
        }

        return false; // some other calc should have been done, e.g. 5x5 matrix
    }

    private int[] matrix = null;

    Hill(Context context) { super(context, "Hill"); }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (matrix == null) {
            dest.writeInt(0);
            dest.writeInt(0);
        } else {
            dest.writeInt(matrix.length);
            dest.writeIntArray(matrix);
        }
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        int matrixSize = in.readInt();
        matrix = new int[matrixSize];
        in.readIntArray(matrix);
    }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getCipherDescription() {
        return "The Hill cipher is a poly-graphic substitution cipher where groups of N letters are taken in turn, converted to a numeric vector, multiplied by an NxN matrix (mod 26) to produce a new vector that is converted to cipher text. " +
                "For example, using N=2, a pair of plain letters (HE) are changed to a vector using 7, 4, and multiplied by a suitable key matrix (using keyword=HILL = [7, 8, 11, 11]) in the following way:\n" +
                " ( 7,  8 ) x ( 7 ) =  ( 7x7+ 8x4) = (  81 ) mod 26 = ( 3  ) => cipher text 'DR'" +
                " (11, 11 )   ( 4 )    (11x7+11x4)   ( 121 )          ( 17 )" +
                "To decipher, use the same process with the inverse matrix. Not all matrices are suitable: " +
                "to be usable in Hill, a matrix has to have a non-zero determinant which must not share factors with the alphabet length.\n\n"+
                "To crack the cipher with brute force is challenging since even for an 2x2 matrix there are 400k+ matrices to check (though many are not suitable). A dictionary check can sometimes yield a key for the matrix. If enough plain text is known then linear algebra can be used to 'solve' the decryption. A hill climb may also yield some results.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher ("+matrixToString(matrix)+")";
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
        int[] matrix = dirs.getMatrix();
        String alphabet = dirs.getAlphabet();
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            if (matrix == null || matrix.length == 0)
                return "Matrix is not valid";
            double sqrt = Math.sqrt(matrix.length);
            if ((int)sqrt != sqrt)
                return "Matrix must be square";
            if (matrix.length > 9)
                return "Cannot invert 4x4 or higher matrices yet";
            for (int element : matrix) {
                if (element < 0)
                    return "Matrix element "+element+" is negative";
                if (element >= alphabet.length())
                    return "Matrix element "+element+" is too large";
            }
            // we can't use this matrix if determinant is not co-prime with alphabet length
            // i.e. d can't be 2 or 13 for a 26-letter alphabet
            int determinant = getDeterminant(matrix, alphabet.length());
            if (determinant == 0)
                return "Determinant is 0, matrix is singular and cannot decode";
            if (!areCoPrimes(determinant, alphabet.length()))
                return "Matrix determinant is "+determinant+", cannot decode uniquely";

        } else {
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
            if (crackMethod != CrackMethod.DICTIONARY && crackMethod != CrackMethod.BRUTE_FORCE && crackMethod != CrackMethod.CRIB_DRAG)
                return "Invalid crack method";
            if (crackMethod == CrackMethod.DICTIONARY) {
                // for the dictionary
                Language lang = dirs.getLanguage();
                if (lang == null)
                    return "Language must be provided";
                if (lang.getDictionary() == null) {
                    return "No "+lang.getName()+" dictionary is defined";
                }
            }
            int rowsAndCols = dirs.getNumberSize();
            if (crackMethod == CrackMethod.CRIB_DRAG) {
                if (rowsAndCols != 22 && rowsAndCols != 33 && rowsAndCols != 44)
                    return "Invalid Rows and Cols: "+rowsAndCols;
                // ensure the crib is long enough, but not too long
                String cribToDrag = dirs.getCribsToDrag();
                if (cribToDrag == null)
                    return "Crib to drag is missing";
                if ((rowsAndCols == 22 && cribToDrag.length() < 5)
                    || (rowsAndCols == 33 && cribToDrag.length() < 11)
                    || (rowsAndCols == 44 && cribToDrag.length() < 19))
                    return "Crib to drag is too short";
                if (cribToDrag.length() > 30)
                    return "Crib to drag is too long";
                for (int i=0; i < cribToDrag.length(); i++) {
                    if (alphabet.indexOf(Character.toUpperCase(cribToDrag.charAt(i))) < 0)
                        return "Non-letter ("+cribToDrag.charAt(i)+") in crib to drag";
                }
            }
            if (crackMethod == CrackMethod.BRUTE_FORCE) {
                // ensure the rows and columns are appropriate
                if (rowsAndCols != 22 && rowsAndCols != 33)
                    return "invalid Rows and Cols: "+rowsAndCols;
            }
        }
        this.matrix = matrix;
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_hill);

        // ensure caps, max size and is suitable for a matrix
        Cipher.addInputFilters(layout, R.id.extra_hill_keyword, true, MAX_KEYWORD_LENGTH, MATRIX_INPUT_FILTER);

        // ensure we 'delete' the keyword text when the delete button is pressed
        Button keywordDelete = layout.findViewById(R.id.extra_hill_keyword_delete);
        keywordDelete.setOnClickListener(MATRIX_ON_CLICK_DELETE);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText keywordView = layout.findViewById(R.id.extra_hill_keyword);
        String entry = keywordView.getText().toString();
        int[] matrix;
        if (entry.contains(",")) {
            String[] entries = entry.split(",");
            matrix = new int[entries.length];
            int i = 0;
            for(String element : entries) {
                try {
                    matrix[i++] = Integer.valueOf(element);
                } catch (NumberFormatException ex) {
                    matrix = new int[0];
                    break;
                }
            }
        } else {
            matrix = convertKeywordToMatrix(entry, dirs.getAlphabet(), true);
        }
        dirs.setMatrix(matrix);
    }

    // add 3 buttons, one for dictionary crack, one for brute-force, one for crib-drag
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String cipherText,
                                    Language language, String alphabet, String paddingChars) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_hill_crack);

        // ensure drag crib is in capitals
        Cipher.addInputFilters(layout, R.id.extra_hill_crack_crib_drag, true, 0);

        // ensure we 'delete' the field when the delete button is pressed
        Button sizeDelete = layout.findViewById(R.id.extra_hill_crack_rows_cols_delete);
        sizeDelete.setOnClickListener(MATRIX_ON_CLICK_DELETE);
        Button cribsDragDelete = layout.findViewById(R.id.extra_hill_crack_crib_drag_delete);
        cribsDragDelete.setOnClickListener(MATRIX_ON_CLICK_DELETE);

        // assess when radio buttons pressed to show what fields needed for each type of crack
        RadioGroup group = layout.findViewById(R.id.extra_hill_crack_radio_group);
        for (int child = 0; child < group.getChildCount(); child++) {
            RadioButton button = (RadioButton)group.getChildAt(child);
            button.setOnClickListener(CRACK_METHOD_ASSESSOR);
        }
        CRACK_METHOD_ASSESSOR.onClick(layout.findViewById(group.getCheckedRadioButtonId()));
        return true;
    }

    /**
     * Fetch the details of the extra crack controls for this cipher
     * @param layout the layout that could contains some crack controls
     * @param dirs the directives to add to
     * @return the crack method to be used
     */
    @Override
    public CrackMethod fetchCrackControls(LinearLayout layout, Directives dirs) {
        EditText rowsColsField = layout.findViewById(R.id.extra_hill_crack_rows_cols);
        String rowsColsStr = rowsColsField.getText().toString();
        try {
            int rowsCols = Integer.parseInt(rowsColsStr);
            dirs.setNumberSize(rowsCols);
        } catch (NumberFormatException ex) {
            dirs.setNumberSize(0);
        }

        EditText cribDragField = layout.findViewById(R.id.extra_hill_crack_crib_drag);
        String cribDragStr = cribDragField.getText().toString();
        dirs.setCribsToDrag(cribDragStr);  // override this method

        // locate the kind of crack we've been asked to do
        RadioButton dictButton = layout.findViewById(R.id.crack_button_dictionary);
        RadioButton bruteForceButton = layout.findViewById(R.id.crack_button_brute_force);
        return (dictButton.isChecked())
                ? CrackMethod.DICTIONARY
                : (bruteForceButton.isChecked()) ? CrackMethod.BRUTE_FORCE : CrackMethod.CRIB_DRAG;
    }

    private String applyHill(String plainText, int[] matrix, String alphabet) {
        // convert to upper case and remove gaps/punctuation in the input text
        plainText = plainText.toUpperCase().replaceAll("[^"+alphabet+"]","");

        // add text to the end to make the right size for multiplying by the matrix
        int vectorSize = (int)Math.sqrt(matrix.length);
        StringBuilder sb = new StringBuilder(plainText);
        while (sb.length() % vectorSize != 0) {
            sb.append("X");
        }
        plainText = sb.toString();

        char[] plainChars = plainText.toCharArray();
        char[] cipherChars = new char[plainText.length()];

        // now multiply in correct sizes
        int[] vector = new int[vectorSize];
        int[] result = new int[vectorSize];
        for (int pos=0; pos < plainChars.length; pos += vectorSize) {
            // create the vector to multiply with, by taking next set of chars as ordinals
            for (int v=0; v < vectorSize; v++) {
                vector[v] = alphabet.indexOf(plainChars[pos+v]);
            }

            // multiply mod alpha length, e.g. 26, answer into 'result'
            multiply(matrix, vector, alphabet.length(), result);

            // convert integer results back to (cipher) chars
            for (int v=0; v < vectorSize; v++) {
                cipherChars[pos+v] = alphabet.charAt(result[v]);
            }
        }
        return String.valueOf(cipherChars);
    }

    /**
     * Encode a text using Hill cipher with the given key matrix
     * @param plainText the text to be encoded
     * @param dirs a group of directives, we need MATRIX (int[]) only
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        int[] matrix = dirs.getMatrix();

        // do the char sequencing, matrix multiply, modulus and convert back to chars
        return applyHill(plainText, matrix, dirs.getAlphabet());
    }

    /**
     * Decode a cipher text using Hill cipher by inverting the matrix and applying same algorithm
     * @param cipherText the text to be decoded
     * @param dirs a group of directives, we need MATRIX (int[]) only
     * @return the decoded string, or error message if inverse matrix does not exist
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        int[] matrix = dirs.getMatrix();
        String alphabet = dirs.getAlphabet();

        // calculate the inverse matrix
        int[] inverseMatrix = new int[matrix.length];
        boolean inverseExists = invertMatrix(matrix, alphabet.length(), inverseMatrix, true);
        if (inverseExists) {
            // do the char sequencing, matrix multiply, modulus and convert back to chars
            return applyHill(cipherText, inverseMatrix, alphabet);
        } else {
            return "Unable to decode: no inverse exists for "+matrixToString(matrix);
        }
    }

    /**
     * Crack a Hill cipher by either Brute Force or Dictionary check
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    @NotNull
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        CrackMethod method = dirs.getCrackMethod();
        if (method == CrackMethod.DICTIONARY) {
            return crackDictionary(cipherText, dirs, crackId);
        }
        if (method == CrackMethod.CRIB_DRAG) {
            return crackCribDrag(cipherText, dirs, crackId);
        }
        // must be brute force
        if (dirs.getNumberSize() == 22) {
            return crackBruteForce2x2(cipherText, dirs, crackId);
        } else {
            if (dirs.getNumberSize() == 33) {
                return crackBruteForce3x3(cipherText, dirs, crackId);
            } else {
                return new CrackResult(method, this, cipherText, "Unable to crack a Hill cipher of this size");
            }
        }
    }

    /**
     * Crack a Hill cipher by dragging a crib along the text, assuming at each stop that the crib
     * is there, deciphering with the specific reverse matrix and then applying along entire text
     * to see if this is English.
     * See: http://practicalcryptography.com/cryptanalysis/stochastic-searching/cryptanalysis-hill-cipher/
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    @NotNull
    private CrackResult crackCribDrag(String cipherText, Directives dirs, int crackId) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        String alphabet = dirs.getAlphabet();
        CrackMethod crackMethod = dirs.getCrackMethod();
        String cribDragText = dirs.getCribsToDrag().toUpperCase();
        int rowsAndCols = dirs.getNumberSize();
        int rows = rowsAndCols / 10;
        int cols = rowsAndCols % 10;
        int sizeOfCribToUse = (rows * cols);

        // set up stuff we will use inside the loops
        Directives crackDirs = new Directives();
        crackDirs.setNumberSize(rowsAndCols);
        crackDirs.setAlphabet(alphabet);
        int[] inverseCipherTextMatrix = new int[rows*cols];
        int[] decryptMatrix = new int[rows*cols];
        int[] encryptMatrix = new int[rows*cols];

        int attempts = 0, matchesFound = 0;
        int modulus = alphabet.length();
        StringBuilder successResult = new StringBuilder()
                .append("Success: Crib Drag scan: tried dragging the supplied crib (")
                .append(cribDragText)
                .append(") and look for cribs [")
                .append(cribString)
                .append("] in the decoded text.\n");
        String foundPlainText = "";
        String foundKeyword = null;
        int[] foundMatrix = null;
        for (int textPos=0; textPos < cipherText.length()-sizeOfCribToUse-rows; textPos += rows) {
            if (CrackResults.isCancelled(crackId))
                return new CrackResult(crackMethod, this, cipherText, "Crack cancelled", CrackState.CANCELLED);
            Log.i("CipherCrack", "Cracking Hill Drag: " + textPos + " positions tried, found="+matchesFound);
            CrackResults.updateProgressDirectly(crackId, textPos+" positions: "+100*textPos/cipherText.length()+"% complete, found="+matchesFound);

            String textToMatch = cipherText.substring(textPos, sizeOfCribToUse+textPos);
            int[] cipherTextMatrix = convertKeywordToMatrix(textToMatch, alphabet, false);
            // may not be invertible
            if (cipherTextMatrix != null && invertMatrix(cipherTextMatrix, modulus, inverseCipherTextMatrix, false)) {

                // once we have found a piece that is invertible, check as much of the crib as possible
                for (int cribPos = 0; cribPos < cribDragText.length()-sizeOfCribToUse; cribPos++) {
                    attempts++;
                    String cribToUse = cribDragText.substring(cribPos, sizeOfCribToUse+cribPos);
                    int[] cribMatrix = convertKeywordToMatrix(cribToUse, alphabet, false);
                    multiply(cribMatrix, inverseCipherTextMatrix, modulus, decryptMatrix);

                    // we 'encrypt' since we've calculated the decrypt matrix, not the original encrypt one
                    crackDirs.setMatrix(decryptMatrix);
                    String plainText = encode(cipherText, crackDirs);
                    if (containsAllCribs(plainText, cribSet)) {
                        if (invertMatrix(decryptMatrix, modulus, encryptMatrix, true)) {
                            foundKeyword = matrixToKeyword(encryptMatrix, alphabet);
                            successResult.append("After ")
                                    .append(attempts)
                                    .append(" attempts, positioned drag-crib (")
                                    .append(cribToUse)
                                    .append(")")
                                    .append(" and found cribs at position ")
                                    .append((textPos + 2) / 2)
                                    .append(" with inverse [")
                                    .append(matrixToString(decryptMatrix))
                                    .append("] and encryption matrix [")
                                    .append(matrixToString(encryptMatrix))
                                    .append("], which is keyword ")
                                    .append(foundKeyword)
                                    .append(".\n");
                            if (dirs.stopAtFirst()) {
                                return new CrackResult(crackMethod, this, dirs, cipherText, plainText, successResult.toString());
                            } else {
                                foundPlainText = plainText;
                                foundMatrix = Arrays.copyOf(encryptMatrix, encryptMatrix.length);
                                matchesFound++;
                            }
                        }
                    }
                }
            }
        }
        // see if we found anything
        if (foundPlainText.length() > 0) {
            this.matrix = foundMatrix;
            dirs.setKeyword(foundKeyword);
            dirs.setMatrix(foundMatrix);
            return new CrackResult(crackMethod, this, dirs, cipherText, foundPlainText, successResult.toString());
        } else {
            matrix = null;
            dirs.setMatrix(null);
            String explainFailed = "Fail: Crib Drag scan: tried using "
                    + attempts
                    + " positions and matrix inverses with drag-crib ("
                    + cribDragText
                    + "), looking for cribs ["
                    + cribString
                    + "] in the decoded text but did not find them.\n";
            return new CrackResult(crackMethod, this, cipherText, explainFailed);
        }
    }

    /**
     * Crack a Hill cipher by using words in the dictionary as possible inputs to for the matrix
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    @NotNull
    private CrackResult crackDictionary(String cipherText, Directives dirs, int crackId) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        Dictionary dict = dirs.getLanguage().getDictionary();
        String alphabet = dirs.getAlphabet();
        StringBuilder reverser = new StringBuilder(cipherText.length());
        CrackMethod crackMethod = dirs.getCrackMethod();
        int wordsChecked = 0, wordsRead = 0, matchesFound = 0;
        String foundWord = null, foundPlainText = "";
        int[] foundMatrix = null;
        StringBuilder successResult = new StringBuilder()
                .append("Success: Dictionary scan: tried using acceptable words in the dictionary as keys to form matrices and look for cribs [")
                .append(cribString)
                .append("] in the decoded text.\n");
        for (String word : dict) {
            if (wordsRead++ % 500 == 499) {
                if (CrackResults.isCancelled(crackId))
                    return new CrackResult(crackMethod, this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                Log.i("CipherCrack", "Cracking Hill Dict: " + wordsRead + " words tried, found="+matchesFound);
                CrackResults.updateProgressDirectly(crackId, wordsRead+" words of "+dict.size()+": "+100*wordsRead/dict.size()+"% complete, found="+matchesFound);
            }
            // skip words that don't make a square matrix
            if (word.length() == 4 || word.length() == 9 || word.length() == 16) {
                String wordUpper = word.toUpperCase();
                int[] possibleMatrix = convertKeywordToMatrix(wordUpper, alphabet, true);
                if (possibleMatrix != null) {
                    wordsChecked++;
                    dirs.setMatrix(possibleMatrix);
                    String plainText = decode(cipherText, dirs);
                    if (containsAllCribs(plainText, cribSet)) {
                        successResult.append("Found cribs with word ")
                                .append(wordsChecked)
                                .append("=")
                                .append(wordUpper)
                                .append(", matrix: [")
                                .append(matrixToString(possibleMatrix))
                                .append("], giving: ")
                                .append(plainText.substring(0, Math.min(Cipher.CRACK_PLAIN_LENGTH, plainText.length())))
                                .append("\n");
                        if (dirs.stopAtFirst()) {
                            matrix = possibleMatrix;
                            dirs.setKeyword(wordUpper);
                            return new CrackResult(crackMethod, this, dirs, cipherText, plainText, successResult.toString());
                        } else {
                            matchesFound++;
                            foundWord = wordUpper;
                            foundMatrix = Arrays.copyOf(possibleMatrix, possibleMatrix.length);
                            foundPlainText = plainText;
                        }
                    }
                    if (dirs.considerReverse()) {
                        reverser.setLength(0);
                        String reversePlainText = reverser.append(plainText).reverse().toString();
                        if (containsAllCribs(reversePlainText, cribSet)) {
                            successResult.append("Found cribs with word ")
                                    .append(wordsChecked)
                                    .append("=")
                                    .append(wordUpper)
                                    .append(", matrix: [")
                                    .append(matrixToString(possibleMatrix))
                                    .append("], in REVERSE decoded text giving: ")
                                    .append(reversePlainText.substring(0, Math.min(Cipher.CRACK_PLAIN_LENGTH, reversePlainText.length())))
                                    .append("\n");
                            if (dirs.stopAtFirst()) {
                                matrix = possibleMatrix;
                                dirs.setKeyword(wordUpper);
                                return new CrackResult(crackMethod, this, dirs, cipherText, reversePlainText, successResult.toString());
                            } else {
                                matchesFound++;
                                foundWord = wordUpper;
                                foundMatrix = Arrays.copyOf(possibleMatrix, possibleMatrix.length);
                                foundPlainText = reversePlainText;
                            }
                        }
                    }
                }
            }
        }
        // see if we found anything
        if (foundPlainText.length() > 0) {
            matrix = foundMatrix;
            dirs.setKeyword(foundWord);
            dirs.setMatrix(foundMatrix);
            return new CrackResult(crackMethod, this, dirs, cipherText, foundPlainText, successResult.toString());
        } else {
            matrix = null;
            dirs.setMatrix(null);
            String explainFailed = "Fail: Dictionary scan: tried using "
                    + wordsChecked
                    + " acceptable words in the dictionary of "
                    + dict.size()
                    + " words as keys to form matrices and look for cribs ["
                    + cribString
                    + "] in the decoded text but did not find them.\n";
            return new CrackResult(crackMethod, this, cipherText, explainFailed);
        }
    }

    /**
     * Crack a Hill cipher by using all possible permutations of 2x2 matrices
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    @NotNull
    private CrackResult crackBruteForce2x2(String cipherText, Directives dirs, int crackId) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        String alphabet = dirs.getAlphabet();
        CrackMethod crackMethod = dirs.getCrackMethod();
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();
        int modulus = alphabet.length();
        int maxTries = modulus*modulus*modulus*modulus;
        int triesCompleted = 0;
        int validMatrices = 0;
        int matchesFound = 0;
        int[] matrixFound = null;
        StringBuilder successResult = new StringBuilder()
                .append("Success: Brute Force: tried all possible matrices looking for cribs [")
                .append(cribString)
                .append("] in the decoded text\n");
        String plainTextFound = "";
        int[] m = new int[4];
        for (int a = 0; a < alphabet.length(); a++) {
            m[0] = a;
            for (int b = 0; b < alphabet.length(); b++) {
                m[1] = b;
                for (int c = 0; c < alphabet.length(); c++) {
                    m[2] = c;
                    for (int d = 0; d < alphabet.length(); d++) {
                        m[3] = d;
                        if (triesCompleted++ % 500 == 499) {
                            if (CrackResults.isCancelled(crackId))
                                return new CrackResult(crackMethod, this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                            Log.i("CipherCrack", "Cracking Hill Brute Force: " + triesCompleted + " matrices tried, " + validMatrices + " were valid, found="+matchesFound);
                            CrackResults.updateProgressDirectly(crackId, "Scanned " + triesCompleted + " matrices of " + maxTries + " possible, of these " + validMatrices + " were invertible: " + 100 * triesCompleted / maxTries + "% complete, found="+matchesFound);
                        }
                        // matrix is only usable if invertible
                        // and the determinant has to be co-prime with alpha length
                        // else 2 symbols could encode to the same char and so not be decode-able
                        int determinant = getDeterminant(m, modulus);
                        if (determinant != 0 && areCoPrimes(determinant, modulus)) {
                            validMatrices++;
                            dirs.setMatrix(m);
                            String plainText = decode(cipherText, dirs);
                            if (containsAllCribs(plainText, cribSet)) {
                                successResult.append("Found cribs after ")
                                        .append(triesCompleted)
                                        .append(" matrices scanned (")
                                        .append(validMatrices)
                                        .append(" invertible) with matrix: [")
                                        .append(matrixToString(m))
                                        .append("], keyword ")
                                        .append(matrixToKeyword(m, alphabet))
                                        .append(".\n");
                                if (dirs.stopAtFirst()) {
                                    return new CrackResult(crackMethod, this, dirs, cipherText, plainText, successResult.toString());
                                } else {
                                    matchesFound++;
                                    matrixFound = Arrays.copyOf(m, m.length);
                                    plainTextFound = plainText;
                                }
                            }
                            if (dirs.considerReverse()) {
                                plainText = decode(reverseCipherText, dirs);
                                if (containsAllCribs(plainText, cribSet)) {
                                    successResult.append("Found cribs in REVERSE text after ")
                                            .append(triesCompleted)
                                            .append(" matrices scanned (")
                                            .append(validMatrices)
                                            .append(" invertible) with matrix: [")
                                            .append(matrixToString(m))
                                            .append("], keyword ")
                                            .append(matrixToKeyword(m, alphabet))
                                            .append(".\n");
                                    if (dirs.stopAtFirst()) {
                                        return new CrackResult(crackMethod, this, dirs, cipherText, plainText, successResult.toString());
                                    } else {
                                        matchesFound++;
                                        matrixFound = Arrays.copyOf(m, m.length);
                                        plainTextFound = plainText;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (plainTextFound.length() > 0) {
            matrix = matrixFound;
            dirs.setMatrix(matrix);
            return new CrackResult(crackMethod, this, dirs, cipherText, plainTextFound, successResult.toString());
        }
        matrix = null;
        dirs.setMatrix(null);
        String explainFail = "Fail: Brute Force: tried using "
                + triesCompleted
                + " matrices of which "
                + validMatrices
                + " were invertible, looked for cribs ["
                + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(crackMethod, this, cipherText, explainFail);
    }

    /**
     * Crack a Hill cipher by using all possible permutations of 3x3 matrices
     * TAKES A VERY LONG TIME TO RUN
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    @NotNull
    private CrackResult crackBruteForce3x3(String cipherText, Directives dirs, int crackId) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        String alphabet = dirs.getAlphabet();
        CrackMethod crackMethod = dirs.getCrackMethod();
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();
        int modulus = alphabet.length();

        // this keeping track is just using 26^6 rather than 26^9
        // and tracks how many of the 3 inner loops we've done
        int maxTries6 = modulus*modulus*modulus*modulus*modulus*modulus;
        int triesCompleted = 0;
        int alphaLength = alphabet.length();
        int[] x = new int[9];
        for (int a = 0; a < alphaLength; a++) {
            x[0] = a;
            for (int b = 0; b < alphaLength; b++) {
                x[1] = b;
                for (int c = 0; c < alphaLength; c++) {
                    x[2] = c;
                    for (int d = 0; d < alphaLength; d++) {
                        x[3] = d;
                        for (int e = 0; e < alphaLength; e++) {
                            x[4] = e;
                            for (int f = 0; f < alphaLength; f++) {
                                x[5] = f;
                                if (triesCompleted++ % 400 == 399) {
                                    if (CrackResults.isCancelled(crackId))
                                        return new CrackResult(crackMethod, this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                                    Log.i("CipherCrack", "Cracking Hill Brute Force: " + triesCompleted + " matrices tried");
                                    CrackResults.updateProgressDirectly(crackId, "Scanned " + triesCompleted + " matrices of " + maxTries6 + " possible: " + 100 * triesCompleted / maxTries6 + "% complete");
                                }
                                for (int g = 0; g < alphaLength; g++) {
                                    x[6] = g;
                                    for (int h = 0; h < alphaLength; h++) {
                                        x[7] = h;
                                        for (int i = 0; i < alphaLength; i++) {
                                            x[8] = i;
                                            // matrix is only usable if invertible
                                            // and the determinant has to be co-prime with alpha length
                                            // else 2 symbols could encode to the same char and so not be decode-able
                                            int determinant = getDeterminant(x, modulus);
                                            if (determinant != 0 && areCoPrimes(determinant, modulus)) {
                                                dirs.setMatrix(x);
                                                String plainText = decode(cipherText, dirs);
                                                if (containsAllCribs(plainText, cribSet)) {
                                                    matrix = x;
                                                    String explain = "Success: Brute Force: tried all possible matrices looking for cribs ["
                                                            + cribString
                                                            + "] in the decoded text and found them after "
                                                            + triesCompleted
                                                            + " scanned with matrix: ["
                                                            + matrixToString(x)
                                                            + "], keyword "
                                                            + matrixToKeyword(x, alphabet)
                                                            + ".\n";
                                                    return new CrackResult(crackMethod, this, dirs, cipherText, plainText, explain);
                                                }
                                                plainText = decode(reverseCipherText, dirs);
                                                if (containsAllCribs(plainText, cribSet)) {
                                                    matrix = x;
                                                    String explain = "Success: Brute Force REVERSE: tried all possible matrices looking for cribs ["
                                                            + cribString
                                                            + "] in the decoded REVERSE text and found them after "
                                                            + triesCompleted
                                                            + " scanned with matrix: ["
                                                            + matrixToString(x)
                                                            + "], keyword "
                                                            + matrixToKeyword(x, alphabet)
                                                            + ".\n";
                                                    return new CrackResult(crackMethod, this, dirs, cipherText, plainText, explain);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        dirs.setMatrix(null);
        matrix = null;
        String explain = "Fail: Brute Force: tried using "
                + triesCompleted
                + " matrices, looked for cribs ["
                + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(crackMethod, this, cipherText, explain);
    }
}