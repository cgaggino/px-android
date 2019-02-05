package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.repository.IdentificationRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Payer;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.ArrayList;
import java.util.List;

public class PayerInformationPresenter extends BasePresenter<PayerInformationView> {

    @NonNull
    private final PaymentSettingRepository paymentSettings;
    @NonNull
    private final IdentificationRepository identificationRepository;

    //Payer info
    private String mIdentificationNumber;
    private String mIdentificationName;
    private String mIdentificationLastName;
    private String mIdentificationBusinessName;
    private Identification mIdentification;
    private IdentificationType mIdentificationType;
    private List<IdentificationType> mIdentificationTypes;

    private FailureRecovery mFailureRecovery;

    private static final int DEFAULT_IDENTIFICATION_NUMBER_LENGTH = 12;
    private static final String IDENTIFICATION_TYPE_CPF = "CPF";

    public PayerInformationPresenter(@NonNull final PaymentSettingRepository paymentSettings,
        @NonNull final IdentificationRepository identificationRepository) {
        this.paymentSettings = paymentSettings;
        this.identificationRepository = identificationRepository;
        mIdentification = new Identification();
    }

    public void initialize() {
        getIdentificationTypesAsync();
    }

    private void getIdentificationTypesAsync() {
        getView().showProgressBar();

        identificationRepository.getIdentificationTypes().enqueue(
            new TaggedCallback<List<IdentificationType>>(ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES) {
                @Override
                public void onSuccess(final List<IdentificationType> identificationTypes) {
                    resolveIdentificationTypes(identificationTypes);
                    getView().hideProgressBar();
                    getView().showInputContainer();
                }

                @Override
                public void onFailure(MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().showError(error, ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES);
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                getIdentificationTypesAsync();
                            }
                        });
                    }
                }
            });
    }

    private void resolveIdentificationTypes(List<IdentificationType> identificationTypes) {
        if (identificationTypes.isEmpty()) {
            getView().showMissingIdentificationTypesError();
        } else {
            mIdentificationType = identificationTypes.get(0);
            getView().initializeIdentificationTypes(identificationTypes);
            mIdentificationTypes = getCPFIdentificationTypes(identificationTypes);
        }
    }

    private List<IdentificationType> getCPFIdentificationTypes(List<IdentificationType> identificationTypes) {
        final List<IdentificationType> identificationTypesList = new ArrayList<>();

        for (final IdentificationType identificationType : identificationTypes) {
            if (identificationType.getId().equals(IDENTIFICATION_TYPE_CPF)) {
                identificationTypesList.add(identificationType);
            }
        }

        return identificationTypesList;
    }

    public void saveIdentificationNumber(final String identificationNumber) {
        mIdentificationNumber = identificationNumber;
        mIdentification.setNumber(identificationNumber);
    }

    public void saveIdentificationName(final String identificationName) {
        mIdentificationName = identificationName;
    }

    public void saveIdentificationLastName(final String identificationLastName) {
        mIdentificationLastName = identificationLastName;
    }

    public int getIdentificationNumberMaxLength() {
        int maxLength = DEFAULT_IDENTIFICATION_NUMBER_LENGTH;

        if (mIdentificationType != null) {
            maxLength = mIdentificationType.getMaxLength();
        }
        return maxLength;
    }

    public void saveIdentificationType(final IdentificationType identificationType) {
        mIdentificationType = identificationType;
        if (identificationType != null) {
            mIdentification.setType(identificationType.getId());
            getView().setIdentificationNumberRestrictions(identificationType.getType());
        }
    }

    public void createPayer() {
        //Get current payer
        final CheckoutPreference checkoutPreference = paymentSettings.getCheckoutPreference();
        final Payer payer = checkoutPreference.getPayer();
        // add collected information.
        payer.setFirstName(mIdentificationName);
        payer.setLastName(mIdentificationLastName);
        payer.setIdentification(mIdentification);
        // reconfigure
        paymentSettings.configure(checkoutPreference);
    }

    public FailureRecovery getFailureRecovery() {
        return mFailureRecovery;
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        mFailureRecovery = failureRecovery;
    }

    public void recoverFromFailure() {
        if (mFailureRecovery != null) {
            mFailureRecovery.recover();
        }
    }

    public boolean validateIdentificationNumber() {
        final boolean isIdentificationNumberValid = validateIdentificationNumberLength();
        if (isIdentificationNumberValid) {
            getView().clearErrorView();
            getView().clearErrorIdentificationNumber();
        } else {
            getView().setInvalidIdentificationNumberErrorView();
            getView().setErrorIdentificationNumber();
        }

        return isIdentificationNumberValid;
    }

    private boolean validateIdentificationNumberLength() {
        if (mIdentificationType != null) {
            if ((mIdentification != null) &&
                (mIdentification.getNumber() != null)) {
                final int len = mIdentification.getNumber().length();
                final Integer min = mIdentificationType.getMinLength();
                final Integer max = mIdentificationType.getMaxLength();
                if ((min != null) && (max != null)) {
                    return ((len <= max) && (len >= min));
                } else {
                    return validateNumber();
                }
            } else {
                return false;
            }
        } else {
            return validateNumber();
        }
    }

    private boolean validateNumber() {
        return mIdentification != null && validateIdentificationType() &&
            !TextUtil.isEmpty(mIdentification.getNumber());
    }

    private boolean validateIdentificationType() {
        return mIdentification != null && !TextUtil.isEmpty(mIdentification.getType());
    }

    public boolean checkIsEmptyOrValidName() {
        return TextUtil.isEmpty(mIdentificationName) || validateName();
    }

    public boolean checkIsEmptyOrValidLastName() {
        return TextUtil.isEmpty(mIdentificationLastName) || validateLastName();
    }

    public boolean validateName() {
        final boolean isNameValid = validateString(mIdentificationName);

        if (isNameValid) {
            getView().clearErrorView();
            getView().clearErrorName();
        } else {
            getView().setInvalidIdentificationNameErrorView();
            getView().setErrorName();
        }

        return isNameValid;
    }

    public boolean validateLastName() {
        final boolean isLastNameValid = validateString(mIdentificationLastName);

        if (isLastNameValid) {
            getView().clearErrorView();
            getView().clearErrorLastName();
        } else {
            getView().setInvalidIdentificationLastNameErrorView();
            getView().setErrorLastName();
        }

        return isLastNameValid;
    }

    public boolean validateBusinessName() {
        final boolean isBusinessNameValid = validateString(mIdentificationBusinessName);

        if (isBusinessNameValid) {
            getView().clearErrorView();
            //TODO fix when cnpj is available
            getView().clearErrorName();
        } else {
            getView().setInvalidIdentificationBusinessNameErrorView();
            //TODO fix when cnpj is available
            getView().setErrorName();
        }

        return isBusinessNameValid;
    }

    private boolean validateString(String string) {
        return !TextUtil.isEmpty(string);
    }

    public IdentificationType getIdentificationType() {
        return mIdentificationType;
    }

    public String getIdentificationNumber() {
        return mIdentificationNumber;
    }

    public String getIdentificationName() {
        return mIdentificationName;
    }

    public String getIdentificationLastName() {
        return mIdentificationLastName;
    }

    public Identification getIdentification() {
        return mIdentification;
    }

    public List<IdentificationType> getIdentificationTypes() {
        return mIdentificationTypes;
    }

    public void setIdentificationType(IdentificationType mIdentificationType) {
        this.mIdentificationType = mIdentificationType;
    }

    public void setIdentificationNumber(String mIdentificationNumber) {
        this.mIdentificationNumber = mIdentificationNumber;
    }

    public void setIdentificationName(String mIdentificationName) {
        this.mIdentificationName = mIdentificationName;
    }

    public void setIdentificationLastName(String mIdentificationLastName) {
        this.mIdentificationLastName = mIdentificationLastName;
    }

    public void setIdentification(Identification mIdentification) {
        this.mIdentification = mIdentification;
    }

    public void setIdentificationTypesList(List<IdentificationType> mIdentificationTypes) {
        this.mIdentificationTypes = mIdentificationTypes;
    }
}
