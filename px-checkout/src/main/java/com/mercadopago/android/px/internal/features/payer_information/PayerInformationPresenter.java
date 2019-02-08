package com.mercadopago.android.px.internal.features.payer_information;

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
import java.util.List;

/* default */ class PayerInformationPresenter extends BasePresenter<PayerInformation.View>
    implements PayerInformation.Actions {

    @NonNull /* default */ final PayerInformationStateModel state;
    @NonNull
    private final PaymentSettingRepository paymentSettings;

    @NonNull
    private final IdentificationRepository identificationRepository;

    private FailureRecovery mFailureRecovery;

    private static final int DEFAULT_IDENTIFICATION_NUMBER_LENGTH = 12;

    /* default */ PayerInformationPresenter(@NonNull final PayerInformationStateModel state,
        @NonNull final PaymentSettingRepository paymentSettings,
        @NonNull final IdentificationRepository identificationRepository) {
        this.state = state;
        this.paymentSettings = paymentSettings;
        this.identificationRepository = identificationRepository;
    }

    @Override
    public void attachView(final PayerInformation.View view) {
        super.attachView(view);

        if (state.hasIdentificationTypes()) {
            view.hideProgressBar();
        } else {
            view.showProgressBar();
            getIdentificationTypesAsync();
        }

        if (state.hasFilledInfo()) {
            view.initializeIdentificationTypes(state.getIdentificationTypeList(), state.getIdentificationType());
            view.setName(state.getIdentificationName());
            view.setLastName(state.getIdentificationLastName());
            view.setNumber(state.getIdentificationNumber());
            view.identificationDraw();
        }
    }

    private void getIdentificationTypesAsync() {
        identificationRepository.getIdentificationTypes().enqueue(
            new TaggedCallback<List<IdentificationType>>(ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES) {
                @Override
                public void onSuccess(final List<IdentificationType> identificationTypes) {
                    resolveIdentificationTypes(identificationTypes);
                    getView().hideProgressBar();
                    getView().requestIdentificationNumberFocus();
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().showError(error, ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES);
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                getView().showProgressBar();
                                getIdentificationTypesAsync();
                            }
                        });
                    }
                }
            });
    }

    private void resolveIdentificationTypes(final List<IdentificationType> identificationTypes) {
        if (identificationTypes.isEmpty()) {
            getView().showMissingIdentificationTypesError();
        } else {
            state.setIdentificationTypes(identificationTypes);
            getView().initializeIdentificationTypes(identificationTypes, state.getIdentificationType());
        }
    }

    public void saveIdentificationNumber(final String identificationNumber) {
        state.setIdentificationNumber(identificationNumber);
    }

    public void saveIdentificationName(final String identificationName) {
        state.setIdentificationName(identificationName);
    }

    public void saveIdentificationLastName(final String identificationLastName) {
        state.setIdentificationLastName(identificationLastName);
    }

    public int getIdentificationNumberMaxLength() {
        int maxLength = DEFAULT_IDENTIFICATION_NUMBER_LENGTH;

        if (state.getIdentificationType() != null) {
            maxLength = state.getIdentificationType().getMaxLength();
        }
        return maxLength;
    }

    public void saveIdentificationType(final IdentificationType identificationType) {
        state.setIdentificationType(identificationType);
        if (identificationType != null) {
            state.getIdentification().setType(identificationType.getId());
            getView().setIdentificationNumberRestrictions(identificationType.getType());
        }
    }

    public void createPayer() {
        //Get current payer
        final CheckoutPreference checkoutPreference = paymentSettings.getCheckoutPreference();
        final Payer payer = checkoutPreference.getPayer();
        // add collected information.
        payer.setFirstName(state.getIdentificationName());
        payer.setLastName(state.getIdentificationLastName());
        payer.setIdentification(state.getIdentification());
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
        final IdentificationType identificationType = state.getIdentificationType();
        if (identificationType != null) {
            final Identification identification = state.getIdentification();
            if ((identification != null) &&
                (identification.getNumber() != null)) {
                final int len = identification.getNumber().length();
                final Integer min = identificationType.getMinLength();
                final Integer max = identificationType.getMaxLength();
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
        final Identification identification = state.getIdentification();
        return identification != null && validateIdentificationType() &&
            !TextUtil.isEmpty(identification.getNumber());
    }

    private boolean validateIdentificationType() {
        final Identification identification = state.getIdentification();
        return identification != null && !TextUtil.isEmpty(identification.getType());
    }

    public boolean checkIsEmptyOrValidName() {
        return TextUtil.isEmpty(state.getIdentificationName()) || validateName();
    }

    public boolean checkIsEmptyOrValidLastName() {
        return TextUtil.isEmpty(state.getIdentificationLastName()) || validateLastName();
    }

    @Override
    public boolean validateName() {
        final boolean isNameValid = TextUtil.isNotEmpty(state.getIdentificationName());

        if (isNameValid) {
            getView().clearErrorView();
            getView().clearErrorName();
        } else {
            getView().setInvalidIdentificationNameErrorView();
            getView().setErrorName();
        }

        return isNameValid;
    }

    @Override
    public boolean validateLastName() {
        final boolean isLastNameValid = TextUtil.isNotEmpty(state.getIdentificationLastName());

        if (isLastNameValid) {
            getView().clearErrorView();
            getView().clearErrorLastName();
        } else {
            getView().setInvalidIdentificationLastNameErrorView();
            getView().setErrorLastName();
        }

        return isLastNameValid;
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

    @Override
    public void focus(final String currentFocusType) {
        state.setFocus(currentFocusType);
    }
}
