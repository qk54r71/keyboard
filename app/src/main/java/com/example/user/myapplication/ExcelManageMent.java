package com.example.user.myapplication;

import android.content.Context;

import com.example.user.myapplication.Common.CommonJava;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ListIterator;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * Created by park on 2016-07-21.
 */
public class ExcelManageMent {
    Workbook mWorkbook;
    Sheet mSheet;
    InputStream mIs;
    Context mContext;
    ArrayList<String> textKey;

    public ExcelManageMent(Context context) {
        this.mContext = context;
        init();
    }

    /**
     * 초기값 지정
     * 엑셀 위치 및 이름
     */
    private void init() {
        try {
            mIs = mContext.getResources().getAssets().open("korea_key.xls");
            mWorkbook = Workbook.getWorkbook(mIs);
            mSheet = mWorkbook.getSheet(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }

        int initRow = 1;
        int initRowMax = mSheet.getRows();

        textKey = new ArrayList<String>();

        for (int nRow = initRow - 1; nRow < initRowMax; nRow++) {

            String textContent = mSheet.getCell(0, nRow).getContents();

            textKey.add(nRow, textContent);

            CommonJava.Loging.i("ExcelManage", "textContent : " + textContent);

        }

    }

    /**
     * 키 값으로 엑셀 데이터 검색
     *
     * @param text_key : 키값
     * @return : 데이터 목록
     */
    public String[] searchData(String text_key) {


        int key_index = textKey.indexOf(text_key);

        int serchCol = 1;
        int serchColMax = mSheet.getRow(key_index).length-1;
        String[] key_content = new String[serchColMax];

        for (int nCol = serchCol-1; nCol < serchColMax; nCol++) {

            key_content[nCol] = mSheet.getCell(nCol+1, key_index).getContents();
            CommonJava.Loging.i("ExcelManage", "key_content[" + nCol + "] : " + key_content[nCol]);
        }

        return key_content;
    }
}
