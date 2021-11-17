package xyz.topplekek.storybook;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Story editor activity class.
 * @author Braeden Hong
 * @since 12-06-2020
 */
public class StoryEditorActivity extends AppCompatActivity implements EditorRecyclerViewAdapter.SetImageListener {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;
    private TextView windowTitleTextView;
    private Story story;
    final private int SELECT_OUTPUT_CODE = 456;
    final private int SELECT_IMAGE_CODE = 789;
    final private String SELECT_IMAGE_POS_KEY = "position";

    /**
     * On set image button clicked listener implementation.
     * @param view The view that was clicked
     * @param pos The position of the ViewHolder that fired this
     */
    @Override
    public void onSetImageButtonClicked(View view, int pos) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(SELECT_IMAGE_POS_KEY, pos);

        setIntent(intent);
        startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_IMAGE_CODE);
    }

    /**
     * onCreate override.
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_editor);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        Uri storyFileUri = intent.getData();

        if (title == null || title.length() == 0)
            title = "Unammed";
        if (author == null || author.length() == 0)
            author = "Unknown";

        // Attempt to load the file if there is one
        if (storyFileUri != null) {
            try {
                story = new Story((FileInputStream) getContentResolver().openInputStream(storyFileUri));
            } catch (IOException e) {
                Toast.makeText(this, "Failed to open storybook: IOException", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else
            story = new Story(title, author);

        windowTitleTextView = findViewById(R.id.windowTitleTextView);
        windowTitleTextView.setText(String.format("%s - By: %s", story.getTitle(), story.getAuthor()));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = findViewById(R.id.editorRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new EditorRecyclerViewAdapter(this, story);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /**
             * Callback method to be invoked when RecyclerView's scroll state changes.
             * Drops the software keyboard if the RecyclerView is being scrolled.
             *
             * @param recyclerView The RecyclerView whose scroll state has changed.
             * @param newState     The updated scroll state. One of {@code SCROLL_STATE_IDLE},
             *                     {@code SCROLL_STATE_DRAGGING} or {@code SCROLL_STATE_SETTLING}.
             */
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    InputMethodManager i = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (i != null)
                        i.hideSoftInputFromWindow(recyclerView.getWindowToken(), 0);
                }
            }
        });
    }

    /**
     * On create options menu handler.
     * @param menu The menu to be inflated.
     * @return Always true to show the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_save, menu);
        return true;
    }

    /**
     * Option item selected handler.
     * @param item The item that was selected
     * @return The result of the super method
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            getStoragePermission();

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("application/storybook");

            startActivityForResult(Intent.createChooser(intent, "Select output"), SELECT_OUTPUT_CODE);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * On activity result handler override.
     * @param requestCode The request code of the intent
     * @param resultCode The result code of the intent
     * @param data The data returned
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == SELECT_OUTPUT_CODE) {
            getStoragePermission();

            try {
                story.saveStory((FileOutputStream) getContentResolver().openOutputStream(data.getData()));
            } catch (IOException e) {
                Toast.makeText(this, "Error: File not found", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == SELECT_IMAGE_CODE) {
            try {
                // Attempt to encode the selected image
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), data.getData());
                Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                int pos = getIntent().getIntExtra(SELECT_IMAGE_POS_KEY, 0);
                Page p = story.getPageAt(pos);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                p.setImage(new String(Base64.encode(outputStream.toByteArray(), Base64.DEFAULT)));
                adapter.notifyItemChanged(pos);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to get image: IOException", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    /**
     * Floating Action Button (FAB) onClick handler.
     * @param view The view that was clicked
     */
    public void onFABClicked(View view) {
        ((EditorRecyclerViewAdapter) adapter).addStoryPage(new Page());
    }

    /**
     * Attempt to get storage permission if not already acquired.
     */
    private void getStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
    }
}
