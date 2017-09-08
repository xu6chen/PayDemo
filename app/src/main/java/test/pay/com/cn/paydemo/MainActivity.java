package test.pay.com.cn.paydemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      Button wxPay = (Button) findViewById(R.id.btn_wx);
      Button aliPay = (Button) findViewById(R.id.btn_ali);



        wxPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    PayUtil.WxBuilder builder = new PayUtil.WxBuilder().with(activity)
                        .setAppId("")
                        .setNonceStr("")
                        .setSign("")
                        .execut();*/
            }
        });




        aliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // PayUtil.startAliPay();
            }
        });

    }
}
