package com.scamera.hp.scamera;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class PinBox  implements View.OnFocusChangeListener, View.OnKeyListener, TextWatcher {
    private EditText mPinFirstDigitEditText;
    private EditText mPinSecondDigitEditText;
    private EditText mPinThirdDigitEditText;
    private EditText mPinForthDigitEditText;
    private EditText mPinHiddenEditText;

    private Context mContext;

    public PinBox (Context context) {
        mContext = context;
        init();
        setPINListeners();
    }
    /**
     * Initialize EditText fields.
     */
    private void init() {
        View rootView = ((Activity) mContext).getWindow().getDecorView().findViewById(android.R.id.content);
        mPinFirstDigitEditText = (EditText) rootView.findViewById(R.id.pin_first_edittext);
        mPinSecondDigitEditText = (EditText) rootView.findViewById(R.id.pin_second_edittext);
        mPinThirdDigitEditText = (EditText) rootView.findViewById(R.id.pin_third_edittext);
        mPinForthDigitEditText = (EditText) rootView.findViewById(R.id.pin_forth_edittext);
        mPinHiddenEditText = (EditText) rootView.findViewById(R.id.pin_hidden_edittext);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        final int id = v.getId();
        switch (id) {
            case R.id.pin_first_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_second_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_third_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_forth_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            final int id = v.getId();
            switch (id) {
                case R.id.pin_hidden_edittext:
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        if (mPinHiddenEditText.getText().length() == 4)
                            mPinForthDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 3)
                            mPinThirdDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 2)
                            mPinSecondDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 1)
                            mPinFirstDigitEditText.setText("");

                        if (mPinHiddenEditText.length() > 0)
                            mPinHiddenEditText.setText(mPinHiddenEditText.getText().subSequence(0, mPinHiddenEditText.length() - 1));
                        return true;
                    }
                    break;

                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //setDefaultPinBackground(mPinFirstDigitEditText);
        //setDefaultPinBackground(mPinSecondDigitEditText);
        //setDefaultPinBackground(mPinThirdDigitEditText);
        //setDefaultPinBackground(mPinForthDigitEditText);

        if (s.length() == 0) {
            //setFocusedPinBackground(mPinFirstDigitEditText);
            mPinFirstDigitEditText.setText("");
        } else if (s.length() == 1) {
            //setFocusedPinBackground(mPinSecondDigitEditText);
            mPinFirstDigitEditText.setText(s.charAt(0) + "");
            mPinSecondDigitEditText.setText("");
            mPinThirdDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
        } else if (s.length() == 2) {
            //setFocusedPinBackground(mPinThirdDigitEditText);
            mPinSecondDigitEditText.setText(s.charAt(1) + "");
            mPinThirdDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
        } else if (s.length() == 3) {
            //setFocusedPinBackground(mPinForthDigitEditText);
            mPinThirdDigitEditText.setText(s.charAt(2) + "");
            mPinForthDigitEditText.setText("");
        } else if (s.length() == 4) {
            //setFocusedPinBackground(mPinForthDigitEditText);
            mPinForthDigitEditText.setText(s.charAt(3) + "");
            //TODO check if correct if not toast err i clear, jesli ok to button enabled
            hideSoftKeyboard(mPinForthDigitEditText);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    /**
     * Sets focus on a specific EditText field.
     */
    public static void setFocus(EditText editText) {
        if (editText == null)
            return;

        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
    }

    /**
     * Sets listeners for EditText fields.
     */
    private void setPINListeners() {
        mPinHiddenEditText.addTextChangedListener(this);

        mPinFirstDigitEditText.setOnFocusChangeListener(this);
        mPinSecondDigitEditText.setOnFocusChangeListener(this);
        mPinThirdDigitEditText.setOnFocusChangeListener(this);
        mPinForthDigitEditText.setOnFocusChangeListener(this);

        mPinFirstDigitEditText.setOnKeyListener(this);
        mPinSecondDigitEditText.setOnKeyListener(this);
        mPinThirdDigitEditText.setOnKeyListener(this);
        mPinForthDigitEditText.setOnKeyListener(this);
        mPinHiddenEditText.setOnKeyListener(this);
    }
    /**
     * Shows soft keyboard.
     *
     * @param editText EditText which has focus
     */
    public void showSoftKeyboard(EditText editText) {
        if (editText == null)
            return;

        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }
    /**
     * Hides soft keyboard.
     *
     * @param editText EditText which has focus
     */
    public void hideSoftKeyboard(EditText editText) {
        if (editText == null)
            return;

        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
    /**
     * Custom LinearLayout with overridden onMeasure() method
     * for handling software keyboard show and hide events.
     */
//    public class MainLayout extends LinearLayout {
//
//        public MainLayout(Context context, AttributeSet attributeSet) {
//            super(context, attributeSet);
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            inflater.inflate(R.layout.activity_finger_print_test, this);
//        }
//
//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            final int proposedHeight = MeasureSpec.getSize(heightMeasureSpec);
//            final int actualHeight = getHeight();
//
//            Log.d("TAG", "proposed: " + proposedHeight + ", actual: " + actualHeight);
//
//            if (actualHeight >= proposedHeight) {
//                // Keyboard is shown
//                if (mPinHiddenEditText.length() == 0)
//                    setFocusedPinBackground(mPinFirstDigitEditText);
//                else
//                    setDefaultPinBackground(mPinFirstDigitEditText);
//            }
//
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }
//    }
}
