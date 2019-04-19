package com.example.eric.wishare.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.eric.wishare.R;

public class WiEditTextPermHint extends LinearLayout {
    private EditText editText;
    private TextView permHint;
    private Spinner spinner;
    private TextView label;
    private LinearLayout editTextPermHint;

    public WiEditTextPermHint(Context context) {
        super(context);

        init();
    }

    private int mEntries;
    private String mPermHint;
    private String mTempHint;
    private String mLabel;

    public WiEditTextPermHint(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.WiEditTextPermHint,
                0, 0);

        mEntries = a.getResourceId(R.styleable.WiEditTextPermHint_entries, -1);
        mPermHint = a.getString(R.styleable.WiEditTextPermHint_permHint);
        mTempHint = a.getString(R.styleable.WiEditTextPermHint_tempHint);
        mLabel = a.getString(R.styleable.WiEditTextPermHint_label);

        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_edit_text_hint, this);

        editText = findViewById(R.id.edit_text);
        permHint = findViewById(R.id.text_view);
        spinner = findViewById(R.id.spinner);
        label = findViewById(R.id.label);
        editTextPermHint = findViewById(R.id.edit_text_perm_hint);

        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_DEL && editText.getText().length() == 0){
                    editTextPermHint.setVisibility(View.GONE);
                    spinner.setVisibility(View.VISIBLE);
                }

                return false;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).toString().equals("Custom")) {
                    spinner.setVisibility(View.GONE);
                    editTextPermHint.setVisibility(View.VISIBLE);
                    spinner.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setLabel(mLabel);
        setTempHint(mTempHint);
        setPermHint(mPermHint);
        setItems(mEntries);
    }

    public void setLabel(String label){
        this.label.setText(label);
    }

    public void setItems(int resource_id){
        ArrayAdapter<String> spinnerCountShoesArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(mEntries));
        spinner.setAdapter(spinnerCountShoesArrayAdapter);
    }

    public void setPermHint(String hint){
        permHint.setText(hint);
    }

    public void setTempHint(String hint){
        editText.setHint(hint);
    }

    public String getCurrentValue() {
        if (editTextPermHint.getVisibility() == View.VISIBLE) {
            return editText.getText().toString();
        }
        return spinner.getSelectedItem().toString();
    }
}
