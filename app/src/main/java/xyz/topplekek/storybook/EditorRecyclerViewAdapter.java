package xyz.topplekek.storybook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

/**
 * RecyclerView Adapter implementation.
 * @author Braeden Hong
 * @since 12-06-2020
 */
public class EditorRecyclerViewAdapter extends RecyclerView.Adapter<EditorRecyclerViewAdapter.ViewHolder> {
    /**
     * Custom listener interface for the editor to implement.
     */
    public interface SetImageListener {
        void onSetImageButtonClicked(View view, int pos);
    }

    private Story dataset;
    private SetImageListener listener;

    /**
     * RecyclerView ViewHolder implementation.
     * @author Braeden Hong
     * @since 12-06-2020
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public EditText pageEditText, choice1EditText, choice2EditText;
        public Button setImageButton;
        public TextView pageNumberTextView;
        public TextInputLayout choice1TextInputLayout, choice2TextInputLayout;

        /**
         * Construct a new ViewHolder.
         * @param view The view it is holding
         */
        public ViewHolder(View view) {
            super(view);
            pageEditText = view.findViewById(R.id.pageEditText);
            choice1EditText = view.findViewById(R.id.choice1EditText);
            choice2EditText = view.findViewById(R.id.choice2EditText);
            setImageButton = view.findViewById(R.id.setImageButton);
            pageNumberTextView = view.findViewById(R.id.editorPageNumberTextView);
            choice1TextInputLayout = view.findViewById(R.id.choice1TextInputLayout);
            choice2TextInputLayout = view.findViewById(R.id.choice2TextInputLayout);


            pageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                /**
                 * Focus change override to save contents upon leaving the text box.
                 * @param view The view that changed focus
                 * @param b Whether the view has focus or not
                 */
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        Page p = EditorRecyclerViewAdapter.this.dataset.getPageAt(ViewHolder.this.getAdapterPosition());
                        p.setText(ViewHolder.this.pageEditText.getText().toString());
                    }
                }
            });

            choice1EditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                /**
                 * Focus change override to save contents upon leaving the text box.
                 * @param view The view that changed focus
                 * @param b Whether the view has focus or not
                 */
                @Override
                public void onFocusChange(View view, boolean b) {
                    ViewHolder.this.choice1TextInputLayout.setError(null);

                    Page p = EditorRecyclerViewAdapter.this.dataset.getPageAt(ViewHolder.this.getAdapterPosition());
                    int choice = 0;

                    try {
                        choice = Integer.parseInt(ViewHolder.this.choice1EditText.getText().toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    if (EditorRecyclerViewAdapter.this.dataset.size() < choice) {
                        ViewHolder.this.choice1TextInputLayout.setError("Page index out of bounds");
                        p.setChoice1(0);
                    }
                    else if (!b)
                        p.setChoice1(choice);
                }
            });

            choice2EditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                /**
                 * Focus change override to save contents upon leaving the text box.
                 * @param view The view that changed focus
                 * @param b Whether the view has focus or not
                 */
                @Override
                public void onFocusChange(View view, boolean b) {
                    ViewHolder.this.choice2TextInputLayout.setError(null);

                    Page p = EditorRecyclerViewAdapter.this.dataset.getPageAt(ViewHolder.this.getAdapterPosition());
                    int choice = 0;

                    try {
                        choice = Integer.parseInt(ViewHolder.this.choice2EditText.getText().toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    if (EditorRecyclerViewAdapter.this.dataset.size() < choice) {
                        ViewHolder.this.choice2TextInputLayout.setError("Page index out of bounds");
                        p.setChoice2(0);
                    }
                    else if (!b)
                        p.setChoice2(choice);
                }
            });

            setImageButton.setOnClickListener(new View.OnClickListener() {
                /**
                 * Image button onClick override to pass it on to the listener.
                 * @param view The view that was clicked
                 */
                @Override
                public void onClick(View view) {
                    listener.onSetImageButtonClicked(view, ViewHolder.this.getAdapterPosition());
                }
            });
        }
    }

    /**
     * Construct a new ViewAdapter.
     * @param context The context that created this object. It must implement SetImageListener.
     * @param dataset The dataset to use.
     */
    public EditorRecyclerViewAdapter(@NonNull Context context, Story dataset) {
        listener = (SetImageListener) context;
        this.dataset = dataset;
    }

    /**
     * onCreateViewHolder override. Called when creating a new ViewHolder.
     * @param parent The parent.
     * @param viewType The type of view to create.
     * @return The ViewHolder created.
     */
    @Override
    public EditorRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * onBindViewHolder override. Called when an existing view holder must rebind its values.
     * @param holder The view holder that is having its values rebound.
     * @param position The position of the holder in the RecyclerView.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (dataset.size() <= position)
            return;

        Page p = dataset.getPageAt(position);

        holder.choice1TextInputLayout.setError(null);
        holder.choice2TextInputLayout.setError(null);
        holder.pageEditText.setText(p.getText());
        holder.choice1EditText.setText(Integer.toString(p.getChoice1()));
        holder.choice2EditText.setText(Integer.toString(p.getChoice2()));
        holder.setImageButton.setText(p.getImage() != null ? R.string.editor_change_image_button_text : R.string.editor_set_image_button_text);
        holder.pageNumberTextView.setText(String.format("Page: %d", position + 1));

        if (dataset.size() < p.getChoice1())
            holder.choice1TextInputLayout.setError("Page index out of bounds");

        if (dataset.size() < p.getChoice2())
            holder.choice2TextInputLayout.setError("Page index out of bounds");
    }

    /**
     * Get the item count in the dataset.
     * @return The size of the dataset (number of pages).
     */
    @Override
    public int getItemCount() {
        return dataset.size();
    }

    /**
     * Add a new page to the story.
     * @param p The page to add
     */
    public void addStoryPage(Page p) {
        dataset.addPage(p);
        notifyItemInserted(dataset.size());
    }
}
