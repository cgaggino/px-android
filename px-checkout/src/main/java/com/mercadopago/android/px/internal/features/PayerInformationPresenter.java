package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.repository.IdentificationRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.viewmodel.PayerInformationStateModel;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Payer;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.tracking.internal.views.CPFViewTracker;
import com.mercadopago.android.px.tracking.internal.views.LastNameViewTracker;
import com.mercadopago.android.px.tracking.internal.views.NameViewTracker;
import java.util.ArrayList;
import java.util.List;

public class PayerInformationPresenter extends BasePresenter<PayerInformationView> implements PayerInformation.Actions {

    @NonNull /* default */ final PayerInformationStateModel state;
    @NonNull
    private final PaymentSettingRepository paymentSettings;
    @NonNull
    private final IdentificationRepository identificationRepository;

    //Payer info
    private String mIdentificationBusinessName;

    private FailureRecovery mFailureRecovery;

    private static final int DEFAULT_IDENTIFICATION_NUMBER_LENGTH = 12;
    private static final String IDENTIFICATION_TYPE_CPF = "CPF";

    public PayerInformationPresenter(
        @NonNull final PayerInformationStateModel state,
        @NonNull final PaymentSettingRepository paymentSettings,
        @NonNull final IdentificationRepository identificationRepository) {
        this.state = state;
        this.paymentSettings = paymentSettings;
        this.identificationRepository = identificationRepository;
        if (state.identification == null) {
            state.identification = new Identification();
        }
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
            state.identificationType = identificationTypes.get(0);
            getView().initializeIdentificationTypes(identificationTypes);
            state.identificationTypeList = getCPFIdentificationTypes(identificationTypes);
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
        state.identificationNumber = identificationNumber;
        state.identification.setNumber(identificationNumber);
    }

    public void saveIdentificationName(final String identificationName) {
        state.identificationName = identificationName;
    }

    public void saveIdentificationLastName(final String identificationLastName) {
        state.identificationLastName = identificationLastName;
    }

    public int getIdentificationNumberMaxLength() {
        int maxLength = DEFAULT_IDENTIFICATION_NUMBER_LENGTH;

        if (state.identificationType != null) {
            maxLength = state.identificationType.getMaxLength();
        }
        return maxLength;
    }

    public void saveIdentificationType(final IdentificationType identificationType) {
        state.identificationType = identificationType;
        if (identificationType != null) {
            state.identification.setType(identificationType.getId());
            getView().setIdentificationNumberRestrictions(identificationType.getType());
        }
    }

    public void createPayer() {
        //Get current payer
        final CheckoutPreference checkoutPreference = paymentSettings.getCheckoutPreference();
        final Payer payer = checkoutPreference.getPayer();
        // add collected information.
        payer.setFirstName(state.identificationName);
        payer.setLastName(state.identificationLastName);
        payer.setIdentification(state.identification);
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
        if (state.identificationType != null) {
            if ((state.identification != null) &&
                (state.identification.getNumber() != null)) {
                final int len = state.identification.getNumber().length();
                final Integer min = state.identificationType.getMinLength();
                final Integer max = state.identificationType.getMaxLength();
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
        return state.identification != null && validateIdentificationType() &&
            !TextUtil.isEmpty(state.identification.getNumber());
    }

    private boolean validateIdentificationType() {
        return state.identification != null && !TextUtil.isEmpty(state.identification.getType());
    }

    public boolean checkIsEmptyOrValidName() {
        return TextUtil.isEmpty(state.identificationName) || validateName();
    }

    public boolean checkIsEmptyOrValidLastName() {
        return TextUtil.isEmpty(state.identificationLastName) || validateLastName();
    }

    public boolean validateName() {
        final boolean isNameValid = validateString(state.identificationName);

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
        final boolean isLastNameValid = validateString(state.identificationLastName);

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

    @Override
    public void trackIdentificationNumberView() {
        final CPFViewTracker cpfViewTracker = new CPFViewTracker();
        setCurrentViewTracker(cpfViewTracker);
    }

    @Override
    public void trackIdentificationNameView() {
        final NameViewTracker nameViewTracker = new NameViewTracker();
        setCurrentViewTracker(nameViewTracker);
    }

    @Override
    public void trackIdentificationLastNameView() {
        final LastNameViewTracker lastNameViewTracker = new LastNameViewTracker();
        setCurrentViewTracker(lastNameViewTracker);
    }

    @Override
    public void trackAbort() {
        tracker.trackAbort();
    }

    @Override
    public void trackBack() {
        tracker.trackBack();
    }

    @NonNull
    @Override
    public PayerInformationStateModel getState() {
        return state;
    }
}
