package com.mercadopago.android.px.internal.features;

import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

public interface PayerInformationView extends MvpView {

    void initializeIdentificationTypes(List<IdentificationType> identificationTypes);

    void setIdentificationNumberRestrictions(String type);

    void clearErrorIdentificationNumber();

    void clearErrorName();

    void clearErrorLastName();

    void setErrorIdentificationNumber();

    void setErrorName();

    void setErrorLastName();

    void setErrorView(String message);

    void clearErrorView();

    void showInputContainer();

    void showError(MercadoPagoError error, String requestOrigin);

    void showProgressBar();

    void hideProgressBar();

    void showMissingIdentificationTypesError();

    void setInvalidIdentificationNumberErrorView();

    void setInvalidIdentificationNameErrorView();

    void setInvalidIdentificationLastNameErrorView();

    void setInvalidIdentificationBusinessNameErrorView();
}
