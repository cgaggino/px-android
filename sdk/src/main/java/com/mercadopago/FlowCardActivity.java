package com.mercadopago;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mercadopago.core.MercadoPago;
import com.mercadopago.model.Cardholder;
import com.mercadopago.model.Installment;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentPreference;
import com.mercadopago.model.PaymentType;
import com.mercadopago.model.Token;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.LayoutUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class FlowCardActivity extends StaticFrontCardActivity {

    private Activity mActivity;

    private PayerCost mPayerCost;
    private PaymentPreference mPaymentPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        fadeInFormActivity();
    }

    @Override
    protected void getActivityParameters() {
        mKey = getIntent().getStringExtra("key");
        mSecurityCodeLocation = CardInterface.CARD_SIDE_BACK;
        mAmount = new BigDecimal(getIntent().getStringExtra("amount"));
        mPaymentPreference = (PaymentPreference) this.getIntent().getSerializableExtra("paymentPreference");
        if (mPaymentPreference == null) {
            mPaymentPreference = new PaymentPreference();
        }
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_flow_card);
    }

    private void fadeInFormActivity() {
        runOnUiThread(new Runnable() {
            public void run() {
                new MercadoPago.StartActivityBuilder()
                        .setActivity(mActivity)
                        .setPublicKey(mKey)
                        .setAmount(new BigDecimal(100))
                        .setPaymentPreference(mPaymentPreference)
                        .startGuessingCardActivity();
                overridePendingTransition(R.anim.fade_in_seamless, R.anim.fade_out_seamless);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if(requestCode == MercadoPago.GUESSING_CARD_REQUEST_CODE) {
            resolveGuessingCardRequest(resultCode, data);
       } else if (requestCode == MercadoPago.INSTALLMENTS_REQUEST_CODE) {
           resolveInstallmentsRequest(resultCode, data);
       }
    }

    protected void resolveInstallmentsRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            mPayerCost = (PayerCost) bundle.getSerializable("payerCost");
            finishWithResult();
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    protected void resolveGuessingCardRequest(int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            mCurrentPaymentMethod = (PaymentMethod) data.getSerializableExtra("paymentMethod");
            mToken = (Token) data.getSerializableExtra("token");
            mSelectedIssuer = (Issuer) data.getSerializableExtra("issuer");
            String cardHolderName = data.getStringExtra("cardHolderName");
            if (mToken != null) {
                mBin = mToken.getFirstSixDigits();
            }
            mCardholder = new Cardholder();
            mCardholder.setName(cardHolderName);
            mSecurityCodeLocation = data.getStringExtra("securityCodeLocation");
            initializeCard();
            checkStartInstallmentsActivity();

        } else if (resultCode == RESULT_CANCELED || ((data != null) &&
                (data.getSerializableExtra("apiException") != null))) {
            finish();
        }
    }

    public void checkStartInstallmentsActivity() {
        if (!mCurrentPaymentMethod.getPaymentTypeId().equals(PaymentType.CREDIT_CARD)) {
            finishWithResult();
        }

        mMercadoPago.getInstallments(mBin, mAmount, mSelectedIssuer.getId(), mCurrentPaymentMethod.getId(),
                new Callback<List<Installment>>() {
                    @Override
                    public void success(List<Installment> installments, Response response) {
                        if (installments.size() == 0) {
                            //TODO
                        } else if (installments.size() == 1) {
                            resolvePayerCosts(installments.get(0).getPayerCosts());
                        } else if (installments.size() > 1) {
                            //TODO
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        ApiUtil.finishWithApiException(getParent(), error);
                    }
                });
    }

    public void startInstallmentsActivity(final List<PayerCost> payerCosts) {
        runOnUiThread(new Runnable() {
            public void run() {
                new MercadoPago.StartActivityBuilder()
                        .setActivity(mActivity)
                        .setPublicKey(mKey)
                        .setPaymentMethod(mCurrentPaymentMethod)
                        .setAmount(mAmount)
                        .setToken(mToken)
                        .setPayerCosts(payerCosts)
                        .setIssuer(mSelectedIssuer)
                        .setPaymentPreference(mPaymentPreference)
                        .startCardInstallmentsActivity();
                overridePendingTransition(R.anim.fade_in_seamless, R.anim.fade_out_seamless);
            }
        });
    }

    public void fadeInInstallmentsActivity(final List<PayerCost> payerCosts) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(500);
                        startInstallmentsActivity(payerCosts);
                    }
                } catch (InterruptedException ex) {
                    //TODO
                }
            }
        };
        thread.start();
    }

    private void resolvePayerCosts(List<PayerCost> payerCosts) {
        PayerCost defaultPayerCost = mPaymentPreference.getDefaultInstallments(payerCosts);
        List<PayerCost> supportedPayerCosts = mPaymentPreference.getInstallmentsBelowMax(payerCosts);

        if (defaultPayerCost != null) {
            mPayerCost = defaultPayerCost;
            finishWithResult();
        } else if(supportedPayerCosts.isEmpty()) {
            //TODO tirarle error
        } else if (supportedPayerCosts.size() == 1) {
            mPayerCost = payerCosts.get(0);
            finishWithResult();
        } else if (payerCosts.size() > 1) {
            fadeInInstallmentsActivity(supportedPayerCosts);
        }
    }

    @Override
    protected void setLayout() {

    }

    @Override
    protected void initializeToolbar() {

    }

    @Override
    protected void finishWithResult() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("payerCost", mPayerCost);
        returnIntent.putExtra("paymentMethod", mCurrentPaymentMethod);
        returnIntent.putExtra("token", mToken);
        returnIntent.putExtra("issuer", mSelectedIssuer);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onItemSelected(View view, int position) {

    }

    @Override
    protected void initializeAdapter() {

    }

    @Override
    public void checkChangeErrorView() {

    }
}
