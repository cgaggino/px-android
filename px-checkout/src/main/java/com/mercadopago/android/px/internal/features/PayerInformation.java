package com.mercadopago.android.px.internal.features;

public interface PayerInformation {

    interface Actions {
        void trackIdentificationNumberView();

        void trackIdentificationNameView();

        void trackIdentificationLastNameView();

        void trackAbort();

        void trackBack();
    }
}
