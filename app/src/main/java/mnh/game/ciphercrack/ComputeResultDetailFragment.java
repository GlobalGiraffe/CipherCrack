package mnh.game.ciphercrack;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.fragment.app.Fragment;
import mnh.game.ciphercrack.util.CrackResult;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * A fragment representing a single ComputeResult detail screen.
 * This fragment is contained in a {@link ComputeResultListActivity}
 * on handsets.
 */
public class ComputeResultDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private CrackResult mItem;

    private final List<CrackResult> crackResults;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ComputeResultDetailFragment(List<CrackResult> results) {
        crackResults = results;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            int idToShow = Integer.parseInt(getArguments().getString(ARG_ITEM_ID));
            CrackResult item = null;
            for(CrackResult result : crackResults) {
                if (result.getId() == idToShow) {
                    item = result;
                }
            }
            mItem = item;

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getCipher().getInstanceDescription());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.compute_result_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.compute_result_detail)).setText(mItem.getExplain());
        }

        return rootView;
    }
}
