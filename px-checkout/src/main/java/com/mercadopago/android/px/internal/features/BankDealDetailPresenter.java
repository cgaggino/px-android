package com.mercadopago.android.px.internal.features;

import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.tracking.internal.views.BankDealsDetailViewTracker;
import com.squareup.picasso.Callback;

public class BankDealDetailPresenter extends BasePresenter<BankDealDetail.View> implements BankDealDetail.Actions {

    @Override
    public Callback getViewCallBack() {
        return new Callback() {
            @Override
            public void onSuccess() {
                getView().hideLogoName();
            }

            @Override
            public void onError() {
                getView().hideLogo();
            }
        };
    }

    @Override
    public void initialize() {
        setCurrentViewTracker(new BankDealsDetailViewTracker());
    }
}
