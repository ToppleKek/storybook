package xyz.topplekek.storybook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * The main activity Class.
 * @author Braeden Hong
 * @since 12-06-2020
 */
public class MainActivity extends AppCompatActivity implements NewStoryDialogFragment.NewStoryDialogListener {
    final private int LOAD_STORY_CODE = 2;
    final private int EDIT_STORY_CODE = 3;

    /**
     * On create Override for main activity.
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * New story button onClick handler.
     * @param view The button that was clicked
     */
    public void onNewStoryButtonClick(View view) {
        DialogFragment dialog = new NewStoryDialogFragment();
        dialog.show(getSupportFragmentManager(), "NewStoryDialogFragment");

    }

    /**
     * Load story button onClick handler.
     * @param view The button that was clicked
     */
    public void onLoadStoryButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/storybook");

        getStoragePermission();
        startActivityForResult(Intent.createChooser(intent, "Select storybook file"), LOAD_STORY_CODE);
    }

    /**
     * Edit story button onClick handler.
     * @param view The button that was clicked
     */
    public void onEditStoryButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/storybook");

        getStoragePermission();
        startActivityForResult(Intent.createChooser(intent, "Select storybook file"), EDIT_STORY_CODE);
    }

    /**
     * On activity result handler override.
     * @param requestCode The request code of the intent
     * @param resultCode The result code of the intent
     * @param data The data returned
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == LOAD_STORY_CODE) {
            Intent intent = new Intent(this, StoryViewerActivity.class);
            intent.setData(data.getData());

            // Start the viewer
            startActivity(intent);
        } else if (requestCode == EDIT_STORY_CODE) {
            Intent intent = new Intent(this, StoryEditorActivity.class);
            intent.setData(data.getData());

            // Start the editor with this story loaded
            startActivity(intent);
        }
    }

    /**
     * New story dialog positive click handler.
     * @param dialog The dialog that the event was fired from
     * @param title The title entered
     * @param author The author entered
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String title, String author) {
        Intent intent = new Intent(this, StoryEditorActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("author", author);

        // Start the editor
        startActivity(intent);
    }

    /**
     * New story dialog negative click handler.
     * @param dialog The dialog that the event was fired from
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i("Story", "NEGATIVE CLICK: CANCELLED");
    }

    /**
     * Attempt to get storage permission if not already acquired.
     */
    private void getStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 143);
    }
}
