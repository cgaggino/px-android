package com.mercadopago.android.px.internal.features;

import com.mercadopago.android.px.internal.base.MvpView;
import com.squareup.picasso.Callback;

public interface BankDealDetail {

    interface View extends MvpView {
        void hideLogoName();
        void hideLogo();
    }

    interface Actions {
        Callback getViewCallBack();
        void initialize();
    }
}
