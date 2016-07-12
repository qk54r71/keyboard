package com.example.user.myapplication;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

/**
 * Created by USER on 2016-07-11.
 */
public class CustomKoreanKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    /**
     * 단계별로 처리하기 위한 변수
     * mDepth == 0 자음
     * mDepth == 1 자음+모음
     * mDepth == 2 자음+모음+받침
     */
    private Integer mDepth = 0;
    /**
     * 단계중 자음 화면
     */
    private final int DEPTH_FIRST = 0;
    /**
     * 단계중 자음 + 모음 화면
     */
    private final int DEPTH_SECOND = 1;
    /**
     * 단계중 자음 + 모음 + 받침 화면
     */
    private final int DEPTH_THIRD = 2;
    /**
     * 공백값 코드
     */
    private final int KEYCODE_SPACE = -7;

    /**
     * 구성된 키보드 화면 매칭
     *
     * @return 매칭된 키보드
     */
    @Override
    public View onCreateInputView() {

        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(CustomKoreanKeyboard.this, R.xml.qwerty_base);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(CustomKoreanKeyboard.this);

        return kv;
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    /**
     * 화면에서 눌리는 키 이벤트 처리
     *
     * @param primaryCode : xml에 명시된 code 값
     * @param keyCodes    : xml에 명시된 code 값을 배열에 담음
     */
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        Log.i("CustomKey", "primaryCode : " + primaryCode);

        InputConnection ic = getCurrentInputConnection();

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case KEYCODE_SPACE:
                ic.commitText(" ", 1);
                break;
            default:
                char code = (char) primaryCode;

                Log.i("CustomKey", "mDepth : " + mDepth);

                switch (mDepth) {
                    case DEPTH_FIRST:
                        ic.commitText(String.valueOf(code), 1);
                        mDepth++;
                        break;
                    case DEPTH_SECOND:
                        ic.deleteSurroundingText(1, 0);
                        ic.commitText(String.valueOf(code), 1);
                        mDepth++;
                        break;
                    case DEPTH_THIRD:
                        mDepth = DEPTH_FIRST;
                        break;
                }
        }

    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
