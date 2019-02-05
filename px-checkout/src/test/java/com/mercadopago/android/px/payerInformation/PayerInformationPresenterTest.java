package com.mercadopago.android.px.payerInformation;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.features.PayerInformationPresenter;
import com.mercadopago.android.px.internal.features.PayerInformationView;
import com.mercadopago.android.px.internal.repository.IdentificationRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.mocks.IdentificationTypes;
import com.mercadopago.android.px.mocks.Identifications;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PayerInformationPresenterTest {

    public static final String DUMMY_NAME = "Name";
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private IdentificationRepository identificationRepository;

    @Mock private PayerInformationView view;

    private PayerInformationPresenter presenter;

    @Before
    public void setUp() {
        presenter = getPresenter();
    }

    @NonNull
    private PayerInformationPresenter getBasePresenter(
        final PayerInformationView view) {

        PayerInformationPresenter presenter = new PayerInformationPresenter(
            paymentSettingRepository, identificationRepository);

        presenter.attachView(view);
        return presenter;
    }

    @NonNull
    private PayerInformationPresenter getPresenter() {
        return getBasePresenter(view);
    }

    @Test
    public void whenInitializePresenterThenInitializeIdentificationTypes() {
        final List<IdentificationType> stubIdentificationTypes = IdentificationTypes.getIdentificationTypes();

        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubSuccessMpCall<>(stubIdentificationTypes));

        presenter.initialize();

        verify(view).showProgressBar();
        verify(view).initializeIdentificationTypes(stubIdentificationTypes);
        verify(view).hideProgressBar();
        verify(view).showInputContainer();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenGetIdentificationTypesFailThenShowError() {
        final ApiException apiException = mock(ApiException.class);

        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubFailMpCall<List<IdentificationType>>(apiException));

        presenter.initialize();

        verify(view).showProgressBar();
        verify(view).showError(any(MercadoPagoError.class), anyString());
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenNameIsValidThenClearError() {
        presenter.setIdentificationName(DUMMY_NAME);
        presenter.checkIsEmptyOrValidName();

        verify(view).clearErrorView();
        verify(view).clearErrorName();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenNameIsNotValidThenSetErrorView() {
        presenter.validateName();

        verify(view).setInvalidIdentificationNameErrorView();
        verify(view).setErrorName();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenLastNameIsValidThenClearError() {
        presenter.setIdentificationLastName(DUMMY_NAME);
        presenter.checkIsEmptyOrValidLastName();

        verify(view).clearErrorView();
        verify(view).clearErrorLastName();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenLastNameIsNotValidThenSetErrorView() {
        presenter.validateLastName();

        verify(view).setInvalidIdentificationLastNameErrorView();
        verify(view).setErrorLastName();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenNumberIsNotValidThenSetErrorView() {
        final IdentificationType identificationType = IdentificationTypes.getIdentificationTypeCPF();
        final Identification identification = Identifications.getIdentificationWithWrongNumberCPF();

        presenter.setIdentificationType(identificationType);
        presenter.setIdentification(identification);

        presenter.validateIdentificationNumber();

        verify(view).setInvalidIdentificationNumberErrorView();
        verify(view).setErrorIdentificationNumber();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenNumberIsValidThenClearError() {
        IdentificationType identificationType = IdentificationTypes.getIdentificationTypeCPF();
        Identification identification = Identifications.getIdentificationCPF();

        presenter.setIdentificationType(identificationType);
        presenter.setIdentification(identification);

        presenter.validateIdentificationNumber();

        verify(view).clearErrorView();
        verify(view).clearErrorIdentificationNumber();
        verifyNoMoreInteractions(view);
    }

}
