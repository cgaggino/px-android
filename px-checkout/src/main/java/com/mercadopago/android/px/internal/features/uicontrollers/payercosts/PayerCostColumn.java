package com.mercadopago.android.px.internal.features.uicontrollers.payercosts;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.CountyInstallmentsUtils;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.internal.util.CurrenciesUtil;
import java.math.BigDecimal;

/**
 * Created by vaserber on 10/25/16.
 */

public class PayerCostColumn {

    private final String mSiteId;
    private final String mCurrencyId;

    private final Context mContext;
    private View mView;
    private MPTextView mInstallmentsTextView;
    private MPTextView mZeroRateText;
    private MPTextView mTotalText;

    private final BigDecimal installmentsRate;
    private final BigDecimal payerCostTotalAmount;
    private final BigDecimal installmentsAmount;
    private final Integer installments;

    public PayerCostColumn(Context context, String currencyId, String siteId,
        BigDecimal installmentsRate, Integer installments,
        BigDecimal payerCostTotalAmount, BigDecimal installmentsAmount) {
        mContext = context;
        mCurrencyId = currencyId;
        mSiteId = siteId;
        this.installmentsRate = installmentsRate;
        this.installments = installments;
        this.payerCostTotalAmount = payerCostTotalAmount;
        this.installmentsAmount = installmentsAmount;
    }

    public void drawPayerCostWithoutTotal() {
        drawBasicPayerCost();
        hideTotalAmount();
        alignRight();
    }

    private void drawBasicPayerCost() {
        setInstallmentsText();

        if (!CountyInstallmentsUtils.shouldWarnAboutBankInterests(mSiteId)) {
            if (installmentsRate.compareTo(BigDecimal.ZERO) == 0) {
                if (installments > 1) {
                    mZeroRateText.setVisibility(View.VISIBLE);
                } else {
                    mZeroRateText.setVisibility(View.GONE);
                }
            }
        }
    }

    public void drawPayerCost() {
        drawBasicPayerCost();
        setAmountWithRateText();
        alignCenter();
    }

    private void setAmountWithRateText() {
        mTotalText.setVisibility(View.VISIBLE);
        final Spanned spannedInstallmentsText =
            CurrenciesUtil.getSpannedAmountWithCurrencySymbol(payerCostTotalAmount, mCurrencyId);
        mTotalText.setText(TextUtils.concat("(", spannedInstallmentsText, ")"));
    }

    private void setInstallmentsText() {
        final Spanned spannedInstallmentsText =
            CurrenciesUtil.getSpannedAmountWithCurrencySymbol(installmentsAmount, mCurrencyId);
        final String x = mInstallmentsTextView.getContext().getString(R.string.px_installments_by);
        mInstallmentsTextView
            .setText(new SpannableStringBuilder(installments.toString()).append(x).append(" ")
                .append(spannedInstallmentsText));
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mView.setOnClickListener(listener);
    }

    public void initializeControls() {
        mInstallmentsTextView = mView.findViewById(R.id.mpsdkInstallmentsText);
        mZeroRateText = mView.findViewById(R.id.mpsdkInstallmentsZeroRate);
        mTotalText = mView.findViewById(R.id.mpsdkInstallmentsTotalAmount);
    }

    public View inflateInParent(ViewGroup parent, boolean attachToRoot) {
        mView = LayoutInflater.from(mContext)
            .inflate(R.layout.px_column_payer_cost, parent, attachToRoot);
        return mView;
    }

    public View getView() {
        return mView;
    }

    private void hideTotalAmount() {
        mTotalText.setVisibility(View.GONE);
    }

    private void alignRight() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        mZeroRateText.setLayoutParams(params);
    }

    private void alignCenter() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mZeroRateText.setLayoutParams(params);
    }
}
