package com.example.user.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Handler;
import android.text.method.Touch;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.TextView;

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
public class CustomKoreanKeyboard extends InputMethodService implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

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
    private final String KOREA_NUM = "숫자";
    private final String KOREA_NULL = "";

    private final String KOREA_ENG = "영어";
    private final String KOREA_ENG_UPPER = " a ";
    private final String KOREA_ENG_LOWER = " A ";

    private final String KOREA_SIGN = "기호";
    private final String KOREA_SIGN_FIRST = "3/3"; // 입력 순서가 약간 엉킴
    private final String KOREA_SIGN_SECOND = "1/3"; // 입력 순서가 약간 엉킴
    private final String KOREA_SIGN_THIRD = "2/3"; // 입력 순서가 약간 엉킴


    /**
     * 단계
     * <p/>
     * DEPTH_KOR_FIRST = 0
     * 한글 : 자음
     * <p/>
     * DEPTH_KOR_SECOND = 1
     * 한글 : 자음 + 모음
     * <p/>
     * DEPTH_KOR_THIRD = 2
     * 한글 : 자음 + 모음 + 받침
     * <p/>
     * DEPTH_ENG_LOWER = 10
     * 영어 : 소문자
     * <p/>
     * DEPTH_ENG_UPPER = 11;
     * 영어 : 대문자 (한번만)
     * <p/>
     * DEPTH_ENG_UPPER_ALWAYS = 12
     * 영어 : 대문자 (항상)
     * <p/>
     * DEPTH_NUM = 20
     * 숫자
     * <p/>
     * DEPTH_SIGN = 30
     * 특수기호 (첫화면)
     * <p/>
     * DEPTH_SIGN_FIRST = 31
     * 특수기호 (두번째 화면)
     * <p/>
     * DEPTH_SIGN_SECOND = 32
     * 특수기호 (세번째 화면)
     * <p/>
     * DEPTH_SIGN_THIRD = 33
     * 특수기호 (첫번째 화면)
     */
    private int mDepth;
    private final int DEPTH_KOR_FIRST = 0;
    private final int DEPTH_KOR_SECOND = 1;
    private final int DEPTH_KOR_THIRD = 2;
    private final int DEPTH_ENG_LOWER = 10;
    private final int DEPTH_ENG_UPPER = 11;
    private final int DEPTH_NUM = 20;
    private final int DEPTH_SIGN = 30;
    private final int DEPTH_SIGN_FIRST = 31;
    private final int DEPTH_SIGN_SECOND = 32;
    private final int DEPTH_SIGN_THIRD = 33;


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
     * 한글의 자음을 저장하고 있는 변수
     */
    private String mKorConsonant;

    /**
     * 롱클릭 후 다음 화면에서 바로 입력이 되지 않게 하기 위한 변수
     */
    private Boolean delayLongClick = false;

    /**
     * 현재 눌려저 있는 걸 표현하기 위해 텍스트 색을 바꿀 버튼
     */
    private Button mCurrentBtnTextColor_btn;
    /**
     * 전에 눌려진 버튼의 텍스트 색을 바꿀 버튼
     */
    private Button mPreBtnTextColor_btn;

    //private DBManageMent dbManageMent;
    private ExcelManageMent excelManageMent;

    /**
     * 삭제 롱클릭시 반복 삭제하기 위한 핸들러
     */
    private Handler onLongHandler = new Handler();

    /**
     * 최초 생성시에 엑셀 데이터 DB에 기록
     */
    @Override
    public void onCreate() {
        super.onCreate();

        //setDB();
        //copyExcelDataToDatabase();
        //copyXmlDataToDatabase();
        excelManageMent = new ExcelManageMent(getApplicationContext());

    }

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

        init();

        return view;
    }

    /**
     * 값 초기화
     */
    private void init() {
        mDepth = DEPTH_KOR_FIRST;
        mCurrentStrText = null;
        mCurrentStrTextArray = null;

        mPreDepth = DEPTH_KOR_FIRST;
        mPreStrText = null;
        mPreStrTextArray = null;

        mSaveDepth = DEPTH_KOR_FIRST;
        mSaveStrText = null;
        mSaveStrTextArray = null;

        setBtnColor(mDepth);
        setBtnTextSize(mDepth);
        setBtnTextColor(mButton[5]);
        //String[] initStrArray = dbManageMent.serchKey(KOREA_KOREA);
        String[] initStrArray = excelManageMent.searchData(KOREA_KOREA);
        setButtonText(initStrArray);
        setEnableBtn();

    }

    /**
     * Custom 한 DB 생성
     */
    private void setDB() {
        //dbManageMent = new DBManageMent(CustomKoreanKeyboard.this);
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
            button.setOnLongClickListener(this);
            button.setOnTouchListener(this);
        }
    }

    /**
     * Click Event 설정
     *
     * @param view : Current View
     */
    @Override
    public void onClick(View view) {
        CommonJava.Loging.i("CustomKey", "onClick View : " + view);

        if (delayLongClick) { // 0.1초후에 눌려지게 만듦
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    delayLongClick = false;
                }
            }, 100);
        } else {
            InputConnection ic = getCurrentInputConnection();

            String strSwitch = (String) ((Button) view).getText();
            CommonJava.Loging.i("CustomKey", "strSwitch : " + strSwitch);

            switch (strSwitch) {
                case KOREA_DELETE: // 삭제

                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    break;

                case KOREA_DONE: // 엔터

                    ic.performEditorAction(EditorInfo.IME_ACTION_GO);
                    //ic.performEditorAction(EditorInfo.IME_ACTION_SEARCH); 적용됨
                    //ic.performEditorAction(EditorInfo.IME_ACTION_SEND); 적용됨

                    //ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)); // 적용안됨
                    break;

                case KOREA_SPACE: // 공백

                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
                    break;

                case KOREA_PREVIOUS: // 이전

                    CommonJava.Loging.i("CustomKey", "KOREA_PREVIOUS");

                    setBtnColor(mPreDepth);
                    setButtonText(mPreStrTextArray);
                    setEnableBtn();

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

                    init();
                    mPreDepth = DEPTH_KOR_SECOND;
                    break;

                case KOREA_KOREA: // 한글

                    Button currentPressKoreaBtn = (Button) view;

                    mDepth = DEPTH_KOR_FIRST;

                    mCurrentStrText = null;
                    mCurrentStrTextArray = null;

                    String[] initKoreaStrArray = excelManageMent.searchData(KOREA_KOREA);
                    setLayout(currentPressKoreaBtn, mDepth, initKoreaStrArray);

                    break;

                case KOREA_CONSONANT: // 자음

                    init();

                    ic.commitText(mKorConsonant, 1);
                    mKorConsonant = null;
                    break;

                case KOREA_NEWINPUT: // 새글

                    break;

                case KOREA_ENG: // 영어

                    Button currentPressEngBtn = (Button) view;

                    mDepth = DEPTH_ENG_LOWER;

                    mCurrentStrText = null;
                    mCurrentStrTextArray = null;

                    String[] initEngStrArray = excelManageMent.searchData(KOREA_ENG);

                    setLayout(currentPressEngBtn, mDepth, initEngStrArray);
                    break;

                case KOREA_ENG_UPPER: // 영어 대문자

                    mDepth = DEPTH_ENG_UPPER;

                    Button currentPressEngUpperBtn = (Button) view;

                    String[] initEngUpperStrArray = excelManageMent.searchData(KOREA_ENG_UPPER);

                    setLayout(currentPressEngUpperBtn, mDepth, initEngUpperStrArray);

                    break;
                case KOREA_ENG_LOWER: // 영어 소문자

                    mDepth = DEPTH_ENG_LOWER;

                    Button currentPressEngLowerBtn = (Button) view;

                    String[] initEngLowerStrArray = excelManageMent.searchData(KOREA_ENG_LOWER);

                    setLayout(currentPressEngLowerBtn, mDepth, initEngLowerStrArray);


                    break;

                case KOREA_NUM: // 숫자

                    Button currentPressNumBtn = (Button) view;
                    mDepth = DEPTH_NUM;
                    mCurrentStrText = null;
                    mCurrentStrTextArray = null;

                    String[] initNumStrArray = excelManageMent.searchData(KOREA_NUM);

                    setLayout(currentPressNumBtn, mDepth, initNumStrArray);


                    break;

                case KOREA_SIGN: // 기호

                    Button currentPressSignBtn = (Button) view;
                    mDepth = DEPTH_SIGN;
                    mCurrentStrText = null;
                    mCurrentStrTextArray = null;
                    String[] initSignStrArray = excelManageMent.searchData(KOREA_SIGN);

                    setLayout(currentPressSignBtn, mDepth, initSignStrArray);

                    break;

                /* 사용안함 2016-08-02 :: 기호 화면 하나로 통일
                case KOREA_SIGN_FIRST: // 1/3 이 눌렸을 때

                    mDepth = DEPTH_SIGN_FIRST;

                    setBtnColor(mDepth);
                    setBtnTextSize(mDepth);
                    String[] initSignFirstStrArray = excelManageMent.searchData(KOREA_SIGN_FIRST);
                    setButtonText(initSignFirstStrArray);
                    setEnableBtn();

                    break;*/
                /* 사용안함 2016-08-02 :: 기호 화면 하나로 통일
                case KOREA_SIGN_SECOND: // 2/3 이 눌렸을 때

                    mDepth = DEPTH_SIGN_SECOND;

                    setBtnColor(mDepth);
                    setBtnTextSize(mDepth);
                    String[] initSignSecondStrArray = excelManageMent.searchData(KOREA_SIGN_SECOND);
                    setButtonText(initSignSecondStrArray);
                    setEnableBtn();

                    break;*/
                /* 사용안함 2016-08-02 :: 기호 화면 하나로 통일
                case KOREA_SIGN_THIRD: // 3/3 이 눌렸을 때

                    mDepth = DEPTH_SIGN_THIRD;

                    setBtnColor(mDepth);
                    setBtnTextSize(mDepth);
                    String[] initSignThirdStrArray = excelManageMent.searchData(KOREA_SIGN_THIRD);
                    setButtonText(initSignThirdStrArray);
                    setEnableBtn();


                    break;*/

                case KOREA_REPEAT: // 반복

                    CharSequence preChar = ic.getTextBeforeCursor(1, InputConnection.GET_TEXT_WITH_STYLES);
                    ic.commitText(preChar, 1);

                    break;

                case KOREA_NULL: // 널 문자
                    break;

                case KOREA_SETTING: // 설정
                    break;

                default:
                    CommonJava.Loging.i("CustomKey", "Test : " + ((Button) view).getText());

                    CharSequence charText = ((Button) view).getText();
                    String strText = String.valueOf(charText.charAt(0));
                    switch (mDepth) {
                        case DEPTH_KOR_FIRST:
                        case DEPTH_KOR_SECOND:
                        case DEPTH_KOR_THIRD:

                            try {
                                switchDepth(mDepth, strText);
                            } catch (IndexOutOfBoundsException e) { // db안에 데이터가 없는 경우, 2번째 스텝에서 입력값이 끝나는 경우이다.

                                mPreDepth = DEPTH_KOR_FIRST;
                                ic.deleteSurroundingText(1, 0);
                                ic.commitText(strText, 1);
                                init();
                            }
                            break;
                        case DEPTH_ENG_LOWER:
                        case DEPTH_ENG_UPPER:
                        case DEPTH_NUM:
                        case DEPTH_SIGN:
                        case DEPTH_SIGN_FIRST:
                        case DEPTH_SIGN_SECOND:
                        case DEPTH_SIGN_THIRD:
                            ic.commitText(strText, 1);
                            break;
                    }
            }
        }
    }


    /**
     * 롱클릭 이벤트
     *
     * @param viewLong : 눌리는 버튼
     * @return
     */

    @Override
    public boolean onLongClick(final View viewLong) {

        CommonJava.Loging.i("CustomKey", "onLongClick View : " + viewLong);

        final InputConnection ic = getCurrentInputConnection();

        String strSwitch = (String) ((Button) viewLong).getText();
        CommonJava.Loging.i("CustomKey", "strSwitch : " + strSwitch);

        switch (strSwitch) {
            case KOREA_DELETE: // 삭제
                //TODO: 삭제 롱클릭시 순차적으로 삭제 기능 넣기
                onLongHandler.postDelayed(onLongDelete, 0);
                break;

            case KOREA_DONE: // 엔터
                break;

            case KOREA_SPACE: // 공백
                break;

            case KOREA_PREVIOUS: // 이전
                break;

            case KOREA_FIRST: // 처음
                break;

            case KOREA_KOREA: // 한글
                break;

            case KOREA_CONSONANT: // 자음
                break;

            case KOREA_NEWINPUT: // 새글

                break;

            case KOREA_ENG: // 영어
                break;

            case KOREA_ENG_UPPER: // 영어 대문자

                break;
            case KOREA_ENG_LOWER: // 영어 소문자

                break;

            case KOREA_NUM: // 숫자

                break;

            case KOREA_SIGN: // 기호

                break;

            case KOREA_SIGN_FIRST: // 1/3 이 눌렸을 때

                break;
            case KOREA_SIGN_SECOND: // 2/3 이 눌렸을 때

                break;
            case KOREA_SIGN_THIRD: // 3/3 이 눌렸을 때

                break;

            case KOREA_REPEAT: // 반복

                break;

            case KOREA_NULL: // 널 문자
                break;

            default:
                CommonJava.Loging.i("CustomKey", "Long Test : " + ((Button) viewLong).getText());

                CharSequence charText = ((Button) viewLong).getText();
                String strText = String.valueOf(charText.charAt(0));
                switch (mDepth) {
                    case DEPTH_KOR_FIRST:
                        break;
                    case DEPTH_KOR_SECOND:// 롱클릭 시 무 받침 글자로 바로 입력

                        ic.commitText(strSwitch, 1);
                        init();
                        delayLongClick = true;
                        break;

                    case DEPTH_KOR_THIRD:
                    case DEPTH_ENG_LOWER:
                    case DEPTH_ENG_UPPER:
                    case DEPTH_NUM:
                    case DEPTH_SIGN:
                    case DEPTH_SIGN_FIRST:
                    case DEPTH_SIGN_SECOND:
                    case DEPTH_SIGN_THIRD:
                        break;
                }
        }

        return false;
    }

    /**
     * 터치 이벤트
     *
     * @param v
     * @param event
     * @return
     */

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_UP:
                onLongHandler.removeCallbacks(onLongDelete);
                break;


            case MotionEvent.ACTION_CANCEL:
                onLongHandler.removeCallbacks(onLongDelete);
                break;
        }
        return false;
    }

    /**
     * 삭제 롱 클릭 시 반복 삭제 작업
     */
    private Runnable onLongDelete = new Runnable() {
        @Override
        public void run() {
            getCurrentInputConnection().deleteSurroundingText(1, 0);
            onLongHandler.postDelayed(this, 500);
        }
    };

    /**
     * mDepth 값에 따른 한글 이벤트 switch
     *
     * @param depth
     */
    private void switchDepth(int depth, String strText) {

        CommonJava.Loging.i("CustomKey", "strText : " + strText);
        CommonJava.Loging.i("CustomKey", "depth : " + depth);

        InputConnection ic = getCurrentInputConnection();

        if (mCurrentStrText == null) {
            mPreStrText = "한글";
        } else {
            mSaveStrText = mPreStrText;
            mPreStrText = mCurrentStrText;
        }
        mCurrentStrText = strText;
        mSaveDepth = mPreDepth;
        mPreDepth = mDepth;

        switch (depth) {
            case DEPTH_KOR_FIRST:

                mKorConsonant = strText;
                mDepth++;

                break;
            case DEPTH_KOR_SECOND:

                mDepth++;

                break;
            case DEPTH_KOR_THIRD:

                mCurrentStrText = KOREA_KOREA;
                mDepth = 0;
                ic.commitText(strText, 1);

                break;
        }

        if (mCurrentStrTextArray == null) {
            //mPreStrTextArray = dbManageMent.serchKey(KOREA_KOREA);
            mPreStrTextArray = excelManageMent.searchData(KOREA_KOREA);
        } else {
            mSaveStrTextArray = mPreStrTextArray;
            mPreStrTextArray = mCurrentStrTextArray;
        }
        //mCurrentStrTextArray = dbManageMent.serchKey(mCurrentStrText);
        mCurrentStrTextArray = excelManageMent.searchData(mCurrentStrText);

        setLayout(null, mDepth, mCurrentStrTextArray);

        CommonJava.Loging.i("CustomKey", "mPreDepth : " + mPreDepth);
        CommonJava.Loging.i("CustomKey", "mPreStrText : " + mPreStrText);
        CommonJava.Loging.i("CustomKey", "mPreStrTextArray : " + mPreStrTextArray);

    }

    /**
     * 화면 구성을 위한 총괄 함수
     *
     * @param currentBtn   : 현재 눌려진 상태의 버튼
     * @param depth        : 현재의 depth
     * @param strTextArray : 적용할 텍스트 배열
     */
    private void setLayout(Button currentBtn, int depth, String[] strTextArray) {

        if (currentBtn != null) {
            setBtnTextColor(currentBtn);
        }

        switch (depth) {
            case DEPTH_ENG_UPPER:
                setENG_UPPER();
                break;
            case DEPTH_ENG_LOWER:
                setENG_LOWER();
                break;
        }

        setBtnColor(depth);
        setButtonText(strTextArray);
        setBtnTextSize(depth);
        setEnableBtn();
        setLayoutColumn(depth);

    }

    /**
     * 키보드의 텍스트 값 변경 함수
     *
     * @param strTextArray :  변경할 문자열 배열
     */
    private void setButtonText(String[] strTextArray) {
        for (int i = 0; i < strTextArray.length; i++) {

            String setBtnTxt = null;
            switch (mDepth) {
                case DEPTH_ENG_LOWER:
                    setBtnTxt = strTextArray[i].toLowerCase();
                    break;
                case DEPTH_ENG_UPPER:
                    setBtnTxt = strTextArray[i].toUpperCase();
                    break;

                default:
                    setBtnTxt = strTextArray[i];
            }
            mButton[i].setText(setBtnTxt);
        }
    }

    /**
     * 버튼에 보이는 영문자를 대문자로 바꿈
     */
    private void setENG_UPPER() {
        for (Button button : mButton) {
            button.setAllCaps(true);
        }
    }

    /**
     * 버튼에 보이는 영문자를 소문자로 바꿈
     */
    private void setENG_LOWER() {
        for (Button button : mButton) {
            button.setAllCaps(false);
        }

    }

    /**
     * 비어있는 버튼 비활성화 및 텍스트 사이즈 조절
     */
    private void setEnableBtn() {

        for (Button button : mButton) {
            if (button.getText().equals("")) {
                button.setEnabled(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    button.setBackground(getResources().getDrawable(R.drawable.round_button_disable));
                }
            } else {
                button.setEnabled(true);
            }
        }
    }

    /**
     * 현재 버튼 화면의 텍스트 사이즈 조절
     *
     * @param currentDepth
     */
    private void setBtnTextSize(Integer currentDepth) {
        CommonJava.Loging.i("CustomKey", "setBtnTextSize()");
        CommonJava.Loging.i("CustomKey", "currentDepth : " + currentDepth);


        int setInitTextSize = 23; // 기본 사이즈
        int setFuncTextSize = 18;

        for (Button nBtn : mButton) {
            nBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, setInitTextSize);
        }

        switch (currentDepth) {
            case DEPTH_KOR_FIRST:
                mButton[4].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[5].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[10].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[11].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[16].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[17].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[22].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[23].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[28].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[29].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);

                break;
            case DEPTH_KOR_SECOND:
                mButton[4].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[5].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[10].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[11].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[17].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[23].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[28].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[29].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);

                break;
            case DEPTH_KOR_THIRD:
                mButton[4].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[5].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[10].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[11].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[17].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[23].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[28].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[29].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);

                mButton[16].setTextSize(TypedValue.COMPLEX_UNIT_SP, setInitTextSize);
                mButton[22].setTextSize(TypedValue.COMPLEX_UNIT_SP, setInitTextSize);

                break;
            case DEPTH_ENG_LOWER:
                mButton[5].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[11].setTextSize(TypedValue.COMPLEX_UNIT_SP, 23); // 영어 대소문자를 구분하기 위한 화살표
                mButton[17].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[23].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);

                break;
            case DEPTH_NUM:
                mButton[4].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[5].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[10].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[11].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[16].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[17].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[23].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[29].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);

                break;
            case DEPTH_SIGN:
            case DEPTH_SIGN_FIRST:
            case DEPTH_SIGN_SECOND:
            case DEPTH_SIGN_THIRD:

                mButton[4].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[5].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[10].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[11].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[16].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[17].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[23].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);
                mButton[29].setTextSize(TypedValue.COMPLEX_UNIT_SP, setFuncTextSize);

        }

    }

    /**
     * 현재 눌린 버튼의 텍스트 변경 함수
     *
     * @param currentBtn : 현재 눌린 버튼
     */
    private void setBtnTextColor(Button currentBtn) {


        int defultColor = Color.BLACK;
        int pressColor = Color.argb(255, 253, 178, 65);

        if (mPreBtnTextColor_btn != null) {
            mPreBtnTextColor_btn.setTextColor(defultColor);
        }
        mPreBtnTextColor_btn = currentBtn;
        mCurrentBtnTextColor_btn = currentBtn;

        currentBtn.setTextColor(pressColor);
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
            case DEPTH_KOR_FIRST:
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
            case DEPTH_KOR_SECOND:
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
            case DEPTH_KOR_THIRD:
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
                    mButton[21].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));
                    mButton[27].setBackground(getResources().getDrawable(R.drawable.selector_round_button_support));

                    mButton[16].setBackground(getResources().getDrawable(R.drawable.selector_round_button));
                    mButton[22].setBackground(getResources().getDrawable(R.drawable.selector_round_button));
                }
                break;
            case DEPTH_ENG_LOWER:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mButton[5].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[11].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[17].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[23].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                }
                break;
            case DEPTH_NUM:
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
            case DEPTH_SIGN:
                /* 사용안함 2016-08-02 :: 기호 화면 하나로 통일
            case DEPTH_SIGN_FIRST:
            case DEPTH_SIGN_SECOND:
            case DEPTH_SIGN_THIRD:*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mButton[5].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[11].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[17].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[23].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                    mButton[29].setBackground(getResources().getDrawable(R.drawable.selector_round_button_func));
                }

                break;

        }
    }

    /**
     * 현재 화면의 열 개수 조절
     *
     * @param currentDepth : 현재 화면의 depth
     */
    private void setLayoutColumn(int currentDepth) {

        switch (currentDepth) {
            case DEPTH_NUM:

                mButton[4].setVisibility(View.GONE);
                mButton[10].setVisibility(View.GONE);
                mButton[16].setVisibility(View.GONE);
                mButton[22].setVisibility(View.GONE);
                mButton[28].setVisibility(View.GONE);

                break;
            default:

                for (Button button : mButton) {
                    button.setVisibility(View.VISIBLE);
                }

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

                        //dbManageMent.open();
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

                            //dbManageMent.createNote(text_key, text_content);
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

    /**
     * xml의 데이터를 DB로 옮기는 함수
     *
     * 2016-07-21 사용안함
     * 엑셀 데이터 -> db 옮기면 시간이 오래걸림
     * 엑셀에서 바로 읽는 방식으로 변경
     *//*
    private void copyXmlDataToDatabase() {

        CommonJava.Loging.i("CustomKey", "copyXmlDataToDatabase()");
        //dbManageMent.open();
        String[] key = getResources().getStringArray(R.array.key);

        String[] key_name_array = key[0].split(";");

        int i = 1;
        for (String key_name : key_name_array) {
            String key_content = key[i++];
            //CommonJava.Loging.i("CustomKey", "key_name : " + key_name + " key_content : " + key_content);
            //dbManageMent.createNote(key_name, key_content);
        }

    }*/

    /**
     * 서비스 죽을 시에 db권한 종료
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
       /* if (dbManageMent != null) {
            dbManageMent.close();
        }*/
    }
}
