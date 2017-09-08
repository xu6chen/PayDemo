package pay.xuchen.paylibrary;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.Map;


/**
 * Created by xuchen on 2017/8/29 19:02
 * 支付类
 */
public class PayUtil implements IWXAPIEventHandler {

    private static final String TAG = "PayUtil";
    private static final int SDK_PAY_FLAG = 1;
    private static final int PAYMODE = 0;
    private static final int WX_PAY = 0;
    private static final int ALI_PAY = 2;


    private static final Object mLock = new Object();
    private static PayUtil mInstance;


    public static PayUtil getInstance() {

        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new PayUtil();
                }
            }
        }
        return mInstance;
    }

    public void execute(Context context) {
        switch (PAYMODE) {
            case WX_PAY:
                // doWxPay(context);
                break;
            case ALI_PAY:
                //  doAliPay(context);
                break;
        }
    }


    public static class WxBuilder {
        private Activity activity;

        private String appId;
        private String partnerId;
        private String prepayId;
        private String nonceStr;
        private String timeStamp;
        private String packageValue;
        private String sign;

        public WxBuilder with(Activity a) {
            this.activity = a;
            return this;
        }

        /**
         * @param appId 微信开放平台审核通过的应用APPID
         * @return
         */
        public WxBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        /**
         * @param partnerId 微信支付分配的商户号
         * @return
         */
        public WxBuilder setPartnerId(String partnerId) {
            this.partnerId = partnerId;
            return this;
        }


        /**
         * 此单号里面包含money
         *
         * @param prepayId 预支付订单号，app服务器调用“统一下单”接口获取
         * @return
         */
        public WxBuilder setPrepayId(String prepayId) {
            this.prepayId = prepayId;
            return this;
        }


        /**
         * @param nonceStr 随机字符串，不长于32位，服务器小哥会给咱生成
         * @return
         */
        public WxBuilder setNonceStr(String nonceStr) {
            this.nonceStr = nonceStr;
            return this;
        }


        /**
         * @param timeStamp 时间戳
         * @return
         */
        public WxBuilder setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }


        /**
         * @param packageValue 固定值Sign=WXPay，可以直接写死，服务器返回的也是这个固定值
         * @return
         */

        public WxBuilder setPackageValue(String packageValue) {
            this.packageValue = packageValue;
            return this;
        }


        /**
         * 服务器小哥给出，他会根据：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=4_3指导得到这个
         *
         * @param sign 签名
         * @return
         */
        public WxBuilder setSign(String sign) {
            this.sign = sign;
            return this;
        }


        public WxBuilder execut() {
            IWXAPI mWxApi = WXAPIFactory.createWXAPI(activity, appId);   //wx134c5ea7e793961b
            PayReq req = new PayReq();
            req.appId = this.appId;//info.appid;// 微信开放平台审核通过的应用APPID
            req.partnerId = this.partnerId;// 微信支付分配的商户号
            req.prepayId = this.prepayId;// 预支付订单号，app服务器调用“统一下单”接口获取
            req.nonceStr = this.nonceStr;// 随机字符串，不长于32位，服务器小哥会给咱生成
            req.timeStamp = this.timeStamp;// 时间戳，app服务器小哥给出
            req.packageValue = "Sign=WXPay";// 固定值Sign=WXPay，可以直接写死，服务器返回的也是这个固定值
            req.sign = this.sign;// 签名，服务器小哥给出，他会根据：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=4_3指导得到这个
            mWxApi.sendReq(req);
            return this;

        }
    }


    /**
     * 开始alipay
     *
     * @param orderInfo
     */
    public static void startAliPay(final String orderInfo, final Activity activity) {

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(activity);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    private static Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();

                    Log.d(TAG, resultStatus);
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        if (payListener != null) {
                            payListener.paySuccess();
                        }
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。

                        if (payListener != null) {

                            payListener.payError();
                        }
                    }
                    break;
                }

                default:
                    break;
            }
        }

    };


    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {

            if (resp.errCode == 0) {
                if (payListener != null) {

                    payListener.paySuccess();
                }
            } else {
                if (payListener != null) {

                    payListener.payError();
                }
            }

        }
    }

    private static PayListener payListener;

    interface PayListener {
        void paySuccess();

        void payError();
    }

    /**
     * 注册支付监听
     *
     * @param listener
     */
    public static void registerPayListener(PayListener listener) {
        PayUtil.payListener = listener;
    }
}
