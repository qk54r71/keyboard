package com.example.user.myapplication;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;

import com.example.user.myapplication.Common.CommonJava;

import java.io.IOException;
import java.io.InputStream;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


/**
 * Created by USER on 2016-07-11.
 * <p/>
 * 참조
 * http://here4you.tistory.com/49
 */
public class CustomKoreanKeyboard extends InputMethodService implements View.OnClickListener {

    private Button[] mButton = new Button[30];

    private final String KOREA_CONSONANT = "자음";
    private final String KOREA_PREVIOUS = "이전";
    private final String KOREA_NEWINPUT = "새글";
    private final String KOREA_SETTING = "설정";
    private final String KOREA_DELETE = "삭제";
    private final String KOREA_REPEAT = "반복";
    private final String KOREA_SPACE = "공백";
    private final String KOREA_KOREA = "한글";
    private final String KOREA_FIRST = "처음";
    private final String KOREA_DONE = "엔터";
    private final String KOREA_SIGN = "기호";
    private final String KOREA_ENG = "영어";
    private final String KOREA_NUM = "숫자";

    /**
     * 단계
     * <p/>
     * DEPTH_FIRST = 0
     * 한글 : 자음
     * <p/>
     * DEPTH_SECOND = 1
     * 한글 : 자음 + 모음 ,영어 : 대문자(한번만)
     * <p/>
     * DEPTH_THIRD = 2
     * 한글 : 자음 + 모음 + 받침 ,영어 : 대문자 (상시)
     */
    private int mDepth;
    private final int DEPTH_FIRST = 0;
    private final int DEPTH_SECOND = 1;
    private final int DEPTH_THIRD = 2;

    /**
     * 현재 상태를 기록하는 변수
     */
    private String mCurrentStrText;
    private String[] mCurrentStrTextArray;

    /**
     * 이전 상태를 기록하는 변수
     */
    private int mPreDepth;
    private String mPreStrText;
    private String[] mPreStrTextArray;

    /**
     * 현재 값과 이전 값을 바꾸기 위해 필요한 중간 저장용 변수
     */
    private int mSaveDepth;
    private String mSaveStrText;
    private String[] mSaveStrTextArray;

    /**
     * 모드
     */
    private String mode;
    private final String KOREA_KEY = "korea_key";
    private final String ENGLISH_KEY = "english_key";
    private final String CHINA_KEY = "china_key";

    private DBManageMent dbManageMent;

    /**
     * TableLayout 을 사용한 CustomKeyboard 화면을 Match 시킨다.
     *
     * @return Custom된 화면
     */
    @Override
    public View onCreateInputView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.key, null);
        CommonJava.Loging.i("CustomKey", "onCreateInputView()");

        setFindView(view);
        setOnClick();
        setDB();
        copyExcelDataToDatabase();

        init();

        return view;
    }

    /**
     * 값 초기화
     */
    private void init() {
        mDepth = DEPTH_FIRST;
        mCurrentStrText = null;
        mCurrentStrTextArray = null;

        setBtnColor(mDepth);
        String[] initStrArray = dbManageMent.serchKey("first");
        setButtonText(initStrArray);

    }

    /**
     * Custom 한 DB 생성
     */
    private void setDB() {
        dbManageMent = new DBManageMent(CustomKoreanKeyboard.this);
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
        mButton[25] = (Button) view.findViewById(R.id.button26);
        mButton[26] = (Button) view.findViewById(R.id.button27);
        mButton[27] = (Button) view.findViewById(R.id.button28);
        mButton[28] = (Button) view.findViewById(R.id.button29);
        mButton[29] = (Button) view.findViewById(R.id.button30);
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
        CommonJava.Loging.i("CustomKey", "View : " + view);

        InputConnection ic = getCurrentInputConnection();

        String strSwitch = (String) ((Button) view).getText();
        CommonJava.Loging.i("CustomKey", "strSwitch : " + strSwitch);


        switch (strSwitch) {
            case KOREA_DELETE: // 삭제
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                break;
            case KOREA_DONE: // 엔터
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case KOREA_SPACE: // 공백
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
                break;
            case KOREA_PREVIOUS: // 이전
                CommonJava.Loging.i("CustomKey", "KOREA_PREVIOUS");

                setBtnColor(mPreDepth);
                setButtonText(mPreStrTextArray);

                mSaveDepth = mDepth;
                mDepth = mPreDepth;
                mPreDepth = mSaveDepth;

                mSaveStrText = mCurrentStrText;
                mCurrentStrText = mPreStrText;
                mPreStrText = mSaveStrText;

                mSaveStrTextArray = mCurrentStrTextArray;
                mCurrentStrTextArray = mPreStrTextArray;
                mPreStrTextArray = mSaveStrTextArray;

                CommonJava.Loging.i("CustomKey", "mPreDepth : " + mPreDepth);
                CommonJava.Loging.i("CustomKey", "mPreStrTextArray : " + mPreStrTextArray[1]);

                break;
            case KOREA_FIRST: // 처음

                mPreDepth = DEPTH_SECOND;
                init();

                break;
            default:
                CommonJava.Loging.i("CustomKey", "Test : " + ((Button) view).getText());

                CharSequence charText = ((Button) view).getText();
                String strText = String.valueOf(charText.charAt(0));

                switchDepth(mDepth, strText);
        }
    }

    /**
     * mDepth 값에 따른 이벤트 switch
     *
     * @param depth
     */
    private void switchDepth(int depth, String strText) {

        CommonJava.Loging.i("CustomKey", "strText : " + strText);
        CommonJava.Loging.i("CustomKey", "depth : " + depth);

        InputConnection ic = getCurrentInputConnection();

        if (mCurrentStrText == null) {
            mPreStrText = "first";
        } else {
            mSaveStrText = mPreStrText;
            mPreStrText = mCurrentStrText;
        }
        mCurrentStrText = strText;
        mSaveDepth = mPreDepth;
        mPreDepth = mDepth;

        switch (depth) {
            case DEPTH_FIRST:

                if (mSaveDepth == DEPTH_SECOND) {
                    ic.deleteSurroundingText(1, 0);
                }

                mDepth++;

                break;
            case DEPTH_SECOND:

                ic.deleteSurroundingText(1, 0);
                mDepth++;

                break;
            case DEPTH_THIRD:

                ic.deleteSurroundingText(1, 0);
                mCurrentStrText = "first";
                mDepth = 0;

                break;
        }

        ic.commitText(strText, 1);

        if (mCurrentStrTextArray == null) {
            mPreStrTextArray = dbManageMent.serchKey("first");
        } else {
            mSaveStrTextArray = mPreStrTextArray;
            mPreStrTextArray = mCurrentStrTextArray;
        }
        mCurrentStrTextArray = dbManageMent.serchKey(mCurrentStrText);

        setBtnColor(mDepth);
        setButtonText(mCurrentStrTextArray);

        CommonJava.Loging.i("CustomKey", "mPreDepth : " + mPreDepth);
        CommonJava.Loging.i("CustomKey", "mPreStrText : " + mPreStrText);
        CommonJava.Loging.i("CustomKey", "mPreStrTextArray : " + mPreStrTextArray);

    }

    /**
     * 키보드의 텍스트 값 변경 함수
     *
     * @param strTextArray :  변경할 문자열 배열
     */
    private void setButtonText(String[] strTextArray) {
        for (int i = 0; i < strTextArray.length; i++) {
            mButton[i].setText(strTextArray[i]);
        }
    }

    /**
     * 키보드의 색상 변경 함수
     *
     * @param currentDepth : 현재 depth
     */
    private void setBtnColor(Integer currentDepth) {

        CommonJava.Loging.i("CustomKey", "setBtnColor()");
        CommonJava.Loging.i("CustomKey", "currentDepth : " + currentDepth);

        for (Button nBtn : mButton) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                nBtn.setBackground(getResources().getDrawable(R.drawable.selector_round_button));
            }
        }

        switch (currentDepth) {
            case DEPTH_FIRST:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mButton[4].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[5].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[10].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[11].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[16].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[17].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[22].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[23].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[28].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[29].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                }
                break;
            case DEPTH_SECOND:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mButton[4].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[5].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[10].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[11].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[17].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[23].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[28].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[29].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));

                    mButton[2].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[3].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[8].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[9].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[14].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[15].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[16].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[20].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[21].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[22].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[26].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[27].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                }
                break;
            case DEPTH_THIRD:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mButton[4].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[5].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[10].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[11].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[17].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[23].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[28].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[29].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));

                    mButton[3].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[9].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[15].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[16].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[21].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[22].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[27].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                }

                break;
        }
    }

    /**
     * assets 폴더에 존재하는 엑셀 파일을 db 에 넣음
     */
    private void copyExcelDataToDatabase() {

        CommonJava.Loging.i("CustomKey", "copyExcelDataToDatabase()");

        Workbook workbook = null;
        Sheet sheet = null;

        try {
            InputStream is = getBaseContext().getResources().getAssets().open("korea_key.xls");

            try {
                workbook = workbook.getWorkbook(is);

                if (workbook != null) {
                    sheet = workbook.getSheet(0);

                    if (sheet != null) {

                        int nMaxColumn = 31;
                        int nRowStartIndex = 1;
                        int nRowEndIndex = sheet.getColumn(nMaxColumn - 1).length - 1;
                        int nColumnStartIndex = 0;
                        int nColumnEndIndex = sheet.getRow(2).length - 1;

                        dbManageMent.open();
                        for (int nRow = nRowStartIndex; nRow <= nRowEndIndex; nRow++) {

                            String text_key = sheet.getCell(nColumnStartIndex, nRow).getContents();
                            String text_content = "";

                            for (int nColumn = nColumnStartIndex + 1; nColumn <= nColumnEndIndex; nColumn++) {
                                if (nColumn != 1) {
                                    text_content += ";";
                                }
                                text_content += sheet.getCell(nColumn, nRow).getContents();
                            }

                            CommonJava.Loging.i("CustomKey", "text_key : " + text_key + " text_content : " + text_content);

                            dbManageMent.createNote(text_key, text_content);
                        }

                    } else {
                        CommonJava.Loging.e("CustomKey", "sheet is null");
                    }
                } else {
                    CommonJava.Loging.e("CustomKey", "workbook is null");
                }

            } catch (BiffException e) {
                e.printStackTrace();
                CommonJava.Loging.e("CustomKey", "Error : " + e.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
            CommonJava.Loging.e("CustomKey", "Error : " + e.toString());
        }

        if (workbook != null) {
            workbook.close();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbManageMent != null) {
            dbManageMent.close();
        }
    }
}
