package com.example.gossips;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class dialogue extends AppCompatDialogFragment {

    EditText status;
    public DialogueListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialogue_layout,null);

        status=view.findViewById(R.id.set_status_txt);

        builder.setView(view).setTitle("Set Status")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String status_txt=status.getText().toString();
                listener.applyText(status_txt);
            }
        });
        return builder.create();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener=(DialogueListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+"no");
        }
    }

    public interface DialogueListener{
        void applyText(String st);
    }
}
