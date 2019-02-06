package com.mercadopago.android.px.internal.features;

import com.mercadopago.android.px.internal.viewmodel.PayerInformationStateModel;

public interface PayerInformation {

    interface Actions {
        void trackIdentificationNumberView();

        void trackIdentificationNameView();

        void trackIdentificationLastNameView();

        void trackAbort();

        void trackBack();

        PayerInformationStateModel getState();
    }
}
