package xyz.topplekek.storybook;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Story viewer activity class.
 * @author Braeden Hong
 * @since 13-06-2020
 */
public class StoryViewerActivity extends AppCompatActivity {
    private Story story;
    private TextView pageTextView, pageNumberTextView;
    private ImageView pageImageView;
    private Button gotoChoice1Button, gotoChoice2Button;

    /**
     * Activity onCreate override.
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_viewer);

        Intent intent = getIntent();

        // Try loading the story provided
        try {
            story = new Story((FileInputStream) getContentResolver().openInputStream(intent.getData()));
        } catch (IOException e) {
            Toast.makeText(this, "Failed to open storybook file", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        pageTextView = findViewById(R.id.pageTextView);
        pageNumberTextView = findViewById(R.id.pageNumberTextView);
        pageImageView = findViewById(R.id.pageImageView);
        gotoChoice1Button = findViewById(R.id.gotoChoice1Button);
        gotoChoice2Button = findViewById(R.id.gotoChoice2Button);

        getSupportActionBar().setTitle(String.format("%s - By: %s", story.getTitle(), story.getAuthor()));

        loadPage(story.turnToPage(0));
    }

    /**
     * Choice button onClick handler.
     * @param view The button that was clicked
     */
    public void onChoiceButtonClicked(View view) {
        Page p = story.getPageAt(story.getCurrentPageIndex());
        if (view == gotoChoice1Button)
            loadPage(story.turnToPage(p.getChoice1() - 1));
        else if (view == gotoChoice2Button)
            loadPage(story.turnToPage(p.getChoice2() - 1));
    }

    /**
     * Restart button onClick handler.
     * @param view The button that was clicked
     */
    public void onRestartButtonClicked(View view) {
        loadPage(story.turnToPage(0));
    }

    /**
     * Attempt to load a page onto the viewer window.
     * @param p The page to load
     */
    private void loadPage(Page p) {
        // Reset visibilities
        pageImageView.setVisibility(View.VISIBLE);
        gotoChoice1Button.setVisibility(View.VISIBLE);
        gotoChoice2Button.setVisibility(View.VISIBLE);

        pageTextView.setText(p.getText());
        pageNumberTextView.setText(String.format("Page: %d", story.getCurrentPageIndex() + 1));

        if (p.getImage() != null) {
            // Decode the image and display it
            byte[] base64 = Base64.decode(p.getImage(), Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(base64, 0, base64.length);
            pageImageView.setImageBitmap(image);
        } else
            pageImageView.setVisibility(View.INVISIBLE);

        if (p.getChoice1() > 0)
            gotoChoice1Button.setText(String.format("Turn to page: %d", p.getChoice1()));
        else
            gotoChoice1Button.setVisibility(View.INVISIBLE);

        if (p.getChoice2() > 0)
            gotoChoice2Button.setText(String.format("Turn to page: %d", p.getChoice2()));
        else
            gotoChoice2Button.setVisibility(View.INVISIBLE);
    }
}
