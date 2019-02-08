package com.mercadopago.android.px.internal.features.payer_information;

import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.viewmodel.PayerInformationStateModel;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

public interface PayerInformation {

    interface View extends MvpView {

        void setIdentificationNumberRestrictions(String type);

        void clearErrorIdentificationNumber();

        void clearErrorName();

        void clearErrorLastName();

        void setErrorIdentificationNumber();

        void setErrorName();

        void setErrorLastName();

        void setErrorView(String message);

        void clearErrorView();


        void requestIdentificationNumberFocus();

        void showError(MercadoPagoError error, String requestOrigin);

        void initializeIdentificationTypes(List<IdentificationType> identificationTypes,
            IdentificationType current);

        void showProgressBar();

        void hideProgressBar();

        void showMissingIdentificationTypesError();

        void setInvalidIdentificationNumberErrorView();

        void setInvalidIdentificationNameErrorView();

        void setInvalidIdentificationLastNameErrorView();

        void setName(final String identificationName);

        void setLastName(final String identificationLastName);

        void setNumber(final String identificationNumber);

        void identificationDraw();
    }

    interface Actions {
        boolean validateName();

        boolean validateLastName();

        void trackIdentificationNumberView();

        void trackIdentificationNameView();

        void trackIdentificationLastNameView();

        void trackAbort();

        void trackBack();

        PayerInformationStateModel getState();

        void focus(final String currentFocusType);
    }
}
