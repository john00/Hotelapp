
package personal.john.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MemoActivity extends Activity implements OnClickListener {
    // 画面オブジェクト
    private CheckBox mChkboxArrived;

    private EditText mEditText;

    private Button mButtonRegist;

    // DB用オブジェクト
    private GeoSearcherDB mDatabaseObject;

    // メイン画面情報
    private String mHotelID = "";

    private int mArrived = 0;

    private String mMemo = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo);

        // DB作成
        mDatabaseObject = new GeoSearcherDB(this);

        // 画面オブジェクト作成
        mChkboxArrived = (CheckBox) findViewById(R.id.checkbox_id);
        mEditText = (EditText) findViewById(R.id.et_memo);
        mButtonRegist = (Button) findViewById(R.id.bt_memo_regist);
        mButtonRegist.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent != null) {
            String[] strCheck = intent.getStringArrayExtra("personal.john.app.Arrived");
            mHotelID = strCheck[0];
            mArrived = Integer.parseInt(strCheck[1]);
            mMemo = strCheck[2];

            if (mArrived == 1)
                mChkboxArrived.setChecked(true);
            mEditText.setText(mMemo);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_memo_regist) {
                int iArraived = 0;
                String strMemo = "";

                if (mChkboxArrived.isChecked())
                    iArraived = 1;
                strMemo = mEditText.getText().toString();

                mDatabaseObject.openGeoSearcherDB();
                mDatabaseObject.writeArrivedData(mHotelID, iArraived);
                mDatabaseObject.writeMemoData(mHotelID, strMemo);
                mDatabaseObject.closeGeoSearcherDB();
                this.finish();
        }
    }

}
