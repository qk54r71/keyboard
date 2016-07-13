package com.example.user.myapplication;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;

/**
 * Created by USER on 2016-07-11.
 */
public class CustomKoreanKeyboard extends InputMethodService implements View.OnClickListener {

    private Button[] mButton = new Button[25];

    private final String KEYBOARD_DELETE = "삭제";
    private final String KEYBOARD_SPACE = "공백";
    private final String KETBOARD_DONE = "엔터";

    /**
     * TableLayout 을 사용한 CustomKeyboard 화면을 Match 시킨다.
     *
     * @return Custom된 화면
     */
    @Override
    public View onCreateInputView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.key, null);

        setFindView(view);
        setOnClick();

        return view;
    }

    /**
     * Custom 된 화면의 Button 을 Match
     *
     * @param view : Current View
     */
    private void setFindView(View view) {
        mButton[0] = (Button) view.findViewById(R.id.button1);
        mButton[1] = (Button) view.findViewById(R.id.button2);
        mButton[2] = (Button) view.findViewById(R.id.button3);
        mButton[3] = (Button) view.findViewById(R.id.button4);
        mButton[4] = (Button) view.findViewById(R.id.button5);
        mButton[5] = (Button) view.findViewById(R.id.button6);
        mButton[6] = (Button) view.findViewById(R.id.button7);
        mButton[7] = (Button) view.findViewById(R.id.button8);
        mButton[8] = (Button) view.findViewById(R.id.button9);
        mButton[9] = (Button) view.findViewById(R.id.button10);
        mButton[10] = (Button) view.findViewById(R.id.button11);
        mButton[11] = (Button) view.findViewById(R.id.button12);
        mButton[12] = (Button) view.findViewById(R.id.button13);
        mButton[13] = (Button) view.findViewById(R.id.button14);
        mButton[14] = (Button) view.findViewById(R.id.button15);
        mButton[15] = (Button) view.findViewById(R.id.button16);
        mButton[16] = (Button) view.findViewById(R.id.button17);
        mButton[17] = (Button) view.findViewById(R.id.button18);
        mButton[18] = (Button) view.findViewById(R.id.button19);
        mButton[19] = (Button) view.findViewById(R.id.button20);
        mButton[20] = (Button) view.findViewById(R.id.button21);
        mButton[21] = (Button) view.findViewById(R.id.button22);
        mButton[22] = (Button) view.findViewById(R.id.button23);
        mButton[23] = (Button) view.findViewById(R.id.button24);
        mButton[24] = (Button) view.findViewById(R.id.button25);
    }

    /**
     * Button 에 Click 설정
     */
    private void setOnClick() {
        for (Button button : mButton) {
            button.setOnClickListener(this);
        }
    }

    /**
     * Click Event 설정
     *
     * @param view : Current View
     */
    @Override
    public void onClick(View view) {
        Log.i("CustomKey", "View : " + view);

        InputConnection ic = getCurrentInputConnection();

        String strSwitch = (String) ((Button) view).getText();

        switch (strSwitch) {
            case KEYBOARD_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case KETBOARD_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case KEYBOARD_SPACE:
                ic.commitText(" ", 1);
                break;
            default:
                Log.i("CustomKey", "Test : " + ((Button) view).getText());

                CharSequence charText = ((Button) view).getText();

                ic.commitText(String.valueOf(charText.charAt(0)), 1);




        }
    }

    /**
     * 눌리는 버튼에 따라서 키보드의 텍스트 값 변경
     *
     * @param strButton :  Click Button Text
     */
    private void setButtonText(String strButton) {
        switch(){

        }
    }

}
