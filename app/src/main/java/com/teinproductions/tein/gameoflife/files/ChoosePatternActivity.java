package com.teinproductions.tein.gameoflife.files;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.teinproductions.tein.gameoflife.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChoosePatternActivity extends AppCompatActivity implements PatternAdapter.OnClickPatternListener {
    public static final String LIFE_MODEL_EXTRA = "life";

    private FirebaseAnalytics firebaseAnalytics;
    private RecyclerView recyclerView;
    private PatternAdapter adapter;
    private String searchQuery;
    private List<RLEPattern> allSavedPatterns; // Will be refreshed each time the activity restarts and getSavedPatternsList is called

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pattern);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Remove the 'save'-FAB when there is nothing to save
        findViewById(R.id.save_fab).setVisibility(
                getIntent().getSerializableExtra(CELLS_ARRAY_EXTRA) != null ? View.VISIBLE : View.GONE);

        // Get searchQuery from savedInstanceState
        if (savedInstanceState != null)
            searchQuery = savedInstanceState.getString(SEARCH_QUERY, null);

        // Init recyclerView and adapter
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatternAdapter(this, this, null);
        loadAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadAdapter() {
        // Get all patterns
        List<PatternListable> items = new ArrayList<>();
        List<RLEPattern> preloaded = RLEPattern.getList(getResources());
        if (preloaded == null) preloaded = new ArrayList<>();
        List<RLEPattern> saved = getSavedPatternsList();

        // Execute search if necessary
        if (searchQuery == null) searchQuery = "";
        if (!searchQuery.isEmpty()) {
            // Make copies so the original arrays (RLEPattern.list and ChoosePatternActivity.allSavedPatterns) aren't affected
            preloaded = new ArrayList<>(preloaded);
            saved = new ArrayList<>(saved);
            filterBySearchQuery(preloaded);
            filterBySearchQuery(saved);
        }

        // Check if there are any results
        if ((saved == null || saved.isEmpty()) && preloaded.isEmpty()) {
            items.add(new NoResultsItem());
            adapter.setData(items);
            return;
        }

        // Create headers and add everything together
        if (saved != null && !saved.isEmpty()) {
            items.add(new Header(getString(R.string.saved_patterns)));
            items.addAll(saved);
        }
        if (!preloaded.isEmpty()) {
            items.add(new Header(getString(R.string.preloaded_patterns)));
            items.addAll(preloaded);
        }
        adapter.setData(items);
    }

    private List<RLEPattern> getSavedPatternsList() {
        if (allSavedPatterns != null) return allSavedPatterns;

        allSavedPatterns = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(ChoosePatternActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File dir = Environment.getExternalStoragePublicDirectory("GameOfLife");
            File[] savedFiles = dir.listFiles();
            if (savedFiles != null)
                for (File file : savedFiles) {
                    if (file.isFile() && (file.getName().endsWith(".rle") || file.getName().endsWith(".RLE")))
                        allSavedPatterns.add(new RLEPattern(file.getName().substring(0, file.getName().length() - 4), file.getName(), false));
                }
        }
        return allSavedPatterns;
    }

    private void filterBySearchQuery(List<RLEPattern> list) {
        for (Iterator<RLEPattern> iter = list.iterator(); iter.hasNext(); )
            if (!iter.next().getName().toLowerCase().contains(searchQuery.toLowerCase()))
                iter.remove(); // TODO: 4-8-17 Better search mechanism?
    }

    private boolean cancelled = false;

    @Override
    public void onClick(RLEPattern pattern) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.parsing_progress_dialog_message));
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelled = true;
            }
        });
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        // Log Firebase Analytics event
        Bundle event = new Bundle();
        event.putString("pattern_name", pattern.getName());
        event.putString("pattern_type", pattern.isPreloaded() ? "preloaded" : "saved");
        firebaseAnalytics.logEvent("open_pattern", event);

        new AsyncTask<RLEPattern, Void, Life>() {
            @Override
            protected Life doInBackground(RLEPattern... values) {
                try {
                    InputStream in;
                    if (values[0].isPreloaded()) {
                        in = getResources().getAssets().open("patterns/" + values[0].getFilename());
                    } else {
                        if (ContextCompat.checkSelfPermission(ChoosePatternActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            File dir = Environment.getExternalStoragePublicDirectory("GameOfLife");
                            File file = new File(dir, values[0].getFilename());
                            in = new FileInputStream(file);
                        } else {
                            return null;
                        }
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        sb.append(line).append("\n");
                    Life life = RLEReader.parse(sb.toString()); // TODO: 22-8-2016 Listen to isCancelled() in RLEReader.parse()
                    LifeUtils.documentCells(life.getCells());

                    return life;
                } catch (IOException | FileParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Life life) {
                if (!cancelled) {
                    try {
                        dialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                    if (life == null) new AlertDialog.Builder(ChoosePatternActivity.this)
                            .setMessage(R.string.something_went_wrong)
                            .setPositiveButton(R.string.ok, null)
                            .create().show();
                    else {
                        setResult(RESULT_OK, new Intent().putExtra(LIFE_MODEL_EXTRA, life));
                        finish();
                    }
                } else {
                    // Reset member variable
                    cancelled = false;
                }
            }
        }.execute(pattern);
    }

    @Override
    public boolean onLongClick(final RLEPattern pattern) {
        if (pattern.isPreloaded()) return false;

        if (ContextCompat.checkSelfPermission(ChoosePatternActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return false;

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.delete_pattern_message, pattern.getName()))
                .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            File dir = Environment.getExternalStoragePublicDirectory("GameOfLife");
                            File file = new File(dir, pattern.getFilename());
                            if (!file.delete())
                                throw new SecurityException(); // In order to show the deletion failed message
                            Snackbar.make(recyclerView, R.string.file_deleted, Snackbar.LENGTH_LONG).show();
                            loadAdapter();
                        } catch (SecurityException e) {
                            Snackbar.make(recyclerView, R.string.file_deletion_failed, Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Used to pass a List<short[]> from MainActivity to ChoosePatternActivity
     */
    public static final String CELLS_ARRAY_EXTRA = "cells_array";
    /**
     * Used to return a Life object from SaveActivity to ChoosePatternActivity
     */
    public static final String LIFE_INFO_EXTRA = "save_info";
    private static final int SAVE_ACTIVITY_RQ = 42;

    public void onClickSave(View view) {
        if (!getIntent().getExtras().containsKey(CELLS_ARRAY_EXTRA)) {
            Snackbar.make(recyclerView, R.string.cells_array_save_error, Snackbar.LENGTH_LONG).show();
            return;
        }

        startActivityForResult(new Intent(this, SaveActivity.class), SAVE_ACTIVITY_RQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_ACTIVITY_RQ && resultCode == RESULT_OK) {
            Life info = (Life) data.getSerializableExtra(LIFE_INFO_EXTRA);
            if (info == null) {
                Snackbar.make(recyclerView, R.string.something_went_wrong, Snackbar.LENGTH_LONG);
                return;
            }
            saveLife(info);
        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE_RQ = 43;
    /**
     * Temporary member variable to keep the String to save, whilst storage permission is requested.
     */
    private String tempFile;
    private String tempFilename;

    private void saveLife(final Life info) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.parsing_progress_dialog_message));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        // Log Firebase Analytics event
        Bundle event = new Bundle();
        event.putString("pattern_name", info.getName());
        firebaseAnalytics.logEvent("save_pattern", event);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return RLEEncoder.constructFile(info,
                            (List<short[]>) getIntent().getSerializableExtra(CELLS_ARRAY_EXTRA));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String file) {
                // Dismiss dialog
                try {
                    dialog.dismiss();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                if (file == null) {
                    Snackbar.make(recyclerView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                } else {
                    // Request permission to external storage, if necessary
                    if (ContextCompat.checkSelfPermission(ChoosePatternActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChoosePatternActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_EXTERNAL_STORAGE_RQ);
                        tempFile = file;
                        tempFilename = info.getName() + ".rle";
                    } else {
                        saveFile(info.getName() + ".rle", file);
                    }
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_RQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted!
                saveFile(tempFilename, tempFile);
            } else {
                // Permission denied
                Snackbar.make(recyclerView, R.string.permission_denied_save_error, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void saveFile(String filename, String contents) {
        try {
            File dir = Environment.getExternalStoragePublicDirectory("GameOfLife");
            dir.mkdirs();
            File file = new File(dir, filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(contents.getBytes());
            fos.close();
            Snackbar.make(recyclerView, R.string.pattern_saved, Snackbar.LENGTH_LONG).show();
            loadAdapter();
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(recyclerView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQueryHint(getString(R.string.search));

        // If searchQuery was saved in savedInstanceState and reloaded in onCreate:
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchView.setQuery(searchQuery, false);
            searchView.setIconified(false);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Log FirebaseAnalytics event
                Bundle params = new Bundle();
                params.putString(FirebaseAnalytics.Param.SEARCH_TERM, query);
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, params);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText.trim();
                loadAdapter();
                return true;
            }
        });

        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            // Hide soft keyboard
            View focus = getCurrentFocus();
            if (focus != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }

            invalidateOptionsMenu();
            searchQuery = "";
            loadAdapter();
        } else {
            super.onBackPressed();
        }
    }

    private static final String SEARCH_QUERY = "search_query";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_QUERY, searchQuery);
    }
}
