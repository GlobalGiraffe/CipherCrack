package mnh.game.ciphercrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.CrackResult;

import java.util.List;

/**
 * An activity representing a list of ComputeResults. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ResultActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ComputeResultListActivity extends AppCompatActivity {

    public static final int RESULT_LIST_REQUEST_CODE = 2000;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private SimpleItemRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compute_result_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.compute_result_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        // update the List view to contain the CURRENT set of CrackResults
        updateView();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "View Refreshed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                updateView();
            }
        });


    }

    // set the List View to be the current set of results from the Home Activity
    private void updateView() {
        RecyclerView recyclerView = findViewById(R.id.compute_result_list);
        assert recyclerView != null;
        List<CrackResult> results = CrackResults.crackResults;
        mAdapter = new SimpleItemRecyclerViewAdapter(this, results, mTwoPane);
        recyclerView.setAdapter(mAdapter);

        recyclerView.invalidate();

    }
    /**
     * Adapter for showing the list of crack results
     */
    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ComputeResultListActivity mParentActivity;
        private final List<CrackResult> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CrackResult item = (CrackResult) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ComputeResultDetailFragment.ARG_ITEM_ID, String.valueOf(item.getId()));
                    ComputeResultDetailFragment fragment = new ComputeResultDetailFragment(CrackResults.crackResults);
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.compute_result_detail_container, fragment)
                            .commit();
                } else {
                    // show a result on the Result Activity
                    Intent i = ResultActivity.getResultIntent(mParentActivity, item.getCipherText(), item.getPlainText(), item.getExplain(), item.getCipher(), item.getDirectives());
                    mParentActivity.startActivityForResult(i, ResultActivity.RESULTS_REQUEST_CODE);
                }
            }
        };

        private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CrackResult crackResult = (CrackResult) view.getTag();
                PopupMenu popup = new PopupMenu(mParentActivity, view);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_list_delete:
                                CrackResults.removeCrackResult(crackResult);
                                mParentActivity.mAdapter.notifyDataSetChanged();
                                return true;

                            case R.id.menu_list_cancel:
                                // TODO: implement cancel of a running crack
                                return true;

                            case R.id.menu_list_details:
                                AlertDialog.Builder alert = new AlertDialog.Builder(mParentActivity);
                                alert.setTitle("Cipher: "+crackResult.getCipher().getCipherName());
                                alert.setMessage("Details");

                                // Create TextView to show the main description
                                final TextView viewWithText = new TextView(mParentActivity);
                                String details = crackResult.getCipher().getInstanceDescription() + "\n"
                                        + "Id: " + crackResult.getId() + "\n"
                                        + "Percent Complete: " + crackResult.getPercentComplete() + "\n"
                                        + "Status: " + crackResult.isSuccess() + "\n"
                                        + "Seconds: " + crackResult.getMilliseconds() + "\n"
                                        + crackResult.getExplain();
                                viewWithText.setText(details);
                                viewWithText.setPadding(6,6,6,6);

                                final ScrollView scroller = new ScrollView(mParentActivity);
                                scroller.addView(viewWithText);
                                alert.setView(scroller);
                                alert.setPositiveButton(mParentActivity.getString(R.string.close), null);
                                alert.show();
                                return true;
                        }
                        return false;
                    }
                });
                popup.inflate(R.menu.list_context_menu);
                // only allow cancel on running cracks, only allow delete on completed cracks
                if (crackResult.getPercentComplete() < 100) {
                    popup.getMenu().findItem(R.id.menu_list_delete).setVisible(false);
                } else {
                    popup.getMenu().findItem(R.id.menu_list_cancel).setVisible(false);
                }
                popup.show();
                return true;
            }
        };

        SimpleItemRecyclerViewAdapter(ComputeResultListActivity parent,
                                      List<CrackResult> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        @NotNull
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.compute_result_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            CrackResult result = mValues.get(position);
            Cipher cipher = result.getCipher();
            holder.mIdView.setText(cipher.getInstanceDescription());
            String status = (result.getPercentComplete() == 0)
                    ? "Not Started"
                    : ((result.getPercentComplete() < 100)
                            ? ("Running: "+result.getPercentComplete()+"%")
                            : (result.isSuccess()? "Success":"Failure"));
            holder.mContentView.setText(status);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
            holder.itemView.setOnLongClickListener(mOnLongClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = view.findViewById(R.id.id_text);
                mContentView = view.findViewById(R.id.content);
            }
        }
    }

    // child activity has finished and perhaps sent result back
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ResultActivity.RESULTS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra("RESULT_TEXT");
                Intent resultIntent = new Intent();
                resultIntent.putExtra("RESULT_TEXT", result);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }
    }
}
