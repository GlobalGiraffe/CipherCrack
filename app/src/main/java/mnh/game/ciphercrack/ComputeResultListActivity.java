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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.CrackState;
import mnh.game.ciphercrack.util.Directives;

import java.util.Iterator;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.result_list_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_result_list_clear_all);
        if (item != null) {
            item.setEnabled(CrackResults.crackResults.size() != 0);
        }
        return true;
    }

    /**
     * Called when someone clicks on an Options menu item
     * @param item the menu that was clicked
     * @return true if this method dealt with the call
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // user wants to remove all completed items
            case R.id.action_result_list_clear_all:
                Iterator<CrackResult> iterator = CrackResults.crackResults.listIterator();
                while (iterator.hasNext()) {
                    CrackResult crackResult = iterator.next();
                    if (crackResult.getCrackState() == CrackState.COMPLETE)
                        iterator.remove();
                }
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
    }

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
                CrackResult crackResult = (CrackResult) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ComputeResultDetailFragment.ARG_ITEM_ID, String.valueOf(crackResult.getId()));
                    ComputeResultDetailFragment fragment = new ComputeResultDetailFragment(CrackResults.crackResults);
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.compute_result_detail_container, fragment)
                            .commit();
                } else {
                    // show a result on the Result Activity
                    Intent i = ResultActivity.getResultIntent(mParentActivity, crackResult.getCipherText(), crackResult.getPlainText(), crackResult.getExplain(), crackResult.getCipher(), crackResult.getDirectives());
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
                                CrackResults.cancelCrack(crackResult.getId());
                                return true;

                            case R.id.menu_list_details:
                                AlertDialog.Builder alert = new AlertDialog.Builder(mParentActivity);
                                alert.setTitle(crackResult.getCipher().getCipherName()+" crack attempt");
                                alert.setMessage("Id: "+crackResult.getId());

                                // Create TextView to show the main description
                                final TextView viewWithText = new TextView(mParentActivity);
                                Directives dirs = crackResult.getDirectives();
                                String details = crackResult.getCipher().getInstanceDescription() + "\n"
                                        + "Method: " + crackResult.getCrackMethod().toString() + "\n"
                                        + "StopAtFirst: " + (dirs == null ? "Unknown" : String.valueOf(dirs.stopAtFirst())) + "\n"
                                        + "ConsiderReverse: " + (dirs == null ? "Unknown" : String.valueOf(dirs.considerReverse())) + "\n"
                                        + "Cribs: " + (dirs == null ? "Unknown" : dirs.getCribs()) + "\n"
                                        + "Progress: " + crackResult.getProgress() + "\n"
                                        + "Status: " + crackResult.getCrackState().toString() + "\n"
                                        + "Successful: " + (crackResult.getCrackState() == CrackState.COMPLETE ? crackResult.isSuccess() : "Unknown") + "\n"
                                        + "Seconds to complete: " + (crackResult.getCrackState() == CrackState.COMPLETE ? crackResult.getMilliseconds()/1000.0 : "Unknown") + "\n"
                                        + "Explain: " + crackResult.getExplain();
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
                if (crackResult.getCrackState() == CrackState.COMPLETE || crackResult.getCrackState() == CrackState.CANCELLED) {
                    popup.getMenu().findItem(R.id.menu_list_cancel).setVisible(false);
                } else {
                    popup.getMenu().findItem(R.id.menu_list_delete).setVisible(false);
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
            String firstLine = result.getCrackMethod().toString() +
                    ": " +
                    (result.isSuccess() ? cipher.getInstanceDescription() : cipher.getCipherName());
            holder.mIdView.setText(firstLine);
            CrackState state = result.getCrackState();
            String secondLine = "Unknown";
            switch (state) {
                case QUEUED:    secondLine = "Queued"; break;
                case RUNNING:   secondLine = "Running: "+result.getProgress(); break;
                case COMPLETE:  secondLine = "Complete: "+ (result.isSuccess()? "Successful":"Unsuccessful"); break;
                case CANCELLED: secondLine = "Cancelled"; break;
            }
            holder.mContentView.setText(secondLine);

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
}
