package xyz.topplekek.storybook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * New story dialog fragment.
 * @author Braeden Hong
 * @since 12-06-2020
 */
public class NewStoryDialogFragment extends DialogFragment {
    /**
     * Button listener.
     */
    public interface NewStoryDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, String title, String author);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    private View view;
    private NewStoryDialogListener listener;

    /**
     * onAttach override.
     * @param context The context that created this.
     */
    @Override
    public void onAttach(@NonNull Context context) throws ClassCastException {
        super.onAttach(context);

        listener = (NewStoryDialogListener) context;
    }

    /**
     * onCreateDialog override.
     * @param savedInstanceState The saved instance state.
     * @return The constructed Dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.dialog_new_story, null);

        builder.setView(view)
                .setMessage(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
                    /**
                     * Positive button onClick listener.
                     * @param dialog The dialog that fired the event
                     * @param which The button that was clicked
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText titleEditText = view.findViewById(R.id.titleEditText);
                        EditText authorEditText = view.findViewById(R.id.authorEditText);

                        listener.onDialogPositiveClick(NewStoryDialogFragment.this,
                                titleEditText.getText().toString(),
                                authorEditText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.dialog_negative_button, new DialogInterface.OnClickListener() {
                    /**
                     * Negative button onClick listener.
                     * @param dialog The dialog that fired this event
                     * @param which The button that was clicked
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(NewStoryDialogFragment.this);
                    }
                });

        return builder.create();
    }
}
