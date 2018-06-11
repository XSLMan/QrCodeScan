package com.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.api.RequestByPost;
import com.api.RequestUrl;
import com.bean.Customer;
import com.google.gson.Gson;
import com.google.zxing.WriterException;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.encoding.EncodingHandler;
import com.qrcodescan.R;
import com.utils.CommonUtil;
import com.utils.JsonUtil;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.openQrCodeScan)
    Button openQrCodeScan;
    @BindView(R.id.text)
    EditText text;
    @BindView(R.id.CreateQrCode)
    Button CreateQrCode;
    @BindView(R.id.QrCode)
    ImageView QrCode;
    @BindView(R.id.qrCodeText)
    TextView qrCodeText;

    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    //扫描成功返回码
    private int RESULT_OK = 0xA1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.openQrCodeScan, R.id.CreateQrCode})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openQrCodeScan:
                //打开二维码扫描界面
                if(CommonUtil.isCameraCanUse()){
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    qrCodeText.setText("");
//                    getData("7975649845");
                }else{
                    Toast.makeText(this,"请打开此应用的摄像头权限！",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.CreateQrCode:
                try {
                    //获取输入的文本信息
                    String str = text.getText().toString().trim();
                    if(str != null && !"".equals(str.trim())){
                        //根据输入的文本生成对应的二维码并且显示出来
                        Bitmap mBitmap = EncodingHandler.createQRCode(text.getText().toString(), 500);
                        if(mBitmap != null){
                            Toast.makeText(this,"二维码生成成功！",Toast.LENGTH_SHORT).show();
                            QrCode.setImageBitmap(mBitmap);
                        }
                    }else{
                        Toast.makeText(this,"文本信息不能为空！",Toast.LENGTH_SHORT).show();
                    }
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (resultCode == RESULT_OK) { //RESULT_OK = -1
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("qr_scan_result");
            //将扫描出的信息显示出来
//            qrCodeText.setText(scanResult);
            String[] rst = scanResult.split("---");
            if(rst.length != 2){
                qrCodeText.setText(scanResult);
            }else{
                getData(rst[1]);
            }
        }
    }

    private void getData(String email){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RequestUrl.HOST)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RequestByPost post = retrofit.create(RequestByPost.class);
        Call<String> call = post.updateUser(email);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try{
                    Map<String, Object> map = JsonUtil.GsonToMaps(response.body());
                    if(Integer.parseInt(map.get("status").toString()) == 1){
                        Customer customer = JsonUtil.GsonToBean(map.get("data").toString(), Customer.class);
                        qrCodeText.setText("桌號："  + customer.getSeat());
                    }else if(Integer.parseInt(map.get("status").toString()) == 0 || Integer.parseInt(map.get("status").toString()) == -1){
                        Toast.makeText(MainActivity.this, "為查詢到資料", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "數據異常", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "数据异常", Toast.LENGTH_SHORT).show();
                    qrCodeText.setText(response.body() + e.getMessage());
                    Log.e("decode error", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
