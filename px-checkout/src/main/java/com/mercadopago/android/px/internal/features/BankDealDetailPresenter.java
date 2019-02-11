package com.mercadopago.android.px.internal.features;

import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.tracking.internal.views.BankDealsDetailViewTracker;
import com.squareup.picasso.Callback;

public class BankDealDetailPresenter extends BasePresenter<BankDealDetail.View> implements Callback {

    @Override
    public void attachView(final BankDealDetail.View view) {
        super.attachView(view);
        setCurrentViewTracker(new BankDealsDetailViewTracker());
    }

    @Override
    public void onSuccess() {
        getView().hideLogoName();
    }

    @Override
    public void onError() {
        getView().hideLogo();
    }
}
