package com.mercadopago.android.px.internal.features.payer_information;

import com.mercadopago.android.px.internal.repository.IdentificationRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.viewmodel.PayerInformationStateModel;
import com.mercadopago.android.px.mocks.IdentificationTypes;
import com.mercadopago.android.px.mocks.IdentificationUtils;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PayerInformationPresenterTest {

    public static final String DUMMY_NAME = "Name";

    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private IdentificationRepository identificationRepository;

    @Mock private PayerInformation.View view;
    @Mock private PayerInformationStateModel stateModel;

    private PayerInformationPresenter presenter;

    @Before
    public void setUp() {
        presenter = new PayerInformationPresenter(stateModel, paymentSettingRepository, identificationRepository);
    }

    @Test
    public void whenInitializePresenterAndHasFilledInfoThenSetIt() {
        when(stateModel.hasFilledInfo()).thenReturn(true);
        when(stateModel.hasIdentificationTypes()).thenReturn(true);

        presenter.attachView(view);

        verify(view).hideProgressBar();
        verify(view)
            .initializeIdentificationTypes(stateModel.getIdentificationTypeList(), stateModel.getIdentificationType());
        verify(view).setName(stateModel.getIdentificationName());
        verify(view).setLastName(stateModel.getIdentificationLastName());
        verify(view).setNumber(stateModel.getIdentificationNumber());
        verify(view).identificationDraw();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenInitializePresenterThenInitializeIdentificationTypes() {
        final List<IdentificationType> stubIdentificationTypes = IdentificationTypes.getIdentificationTypes();
        when(stateModel.hasIdentificationTypes()).thenReturn(false);
        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubSuccessMpCall<>(stubIdentificationTypes));

        presenter.attachView(view);

        verify(view).showProgressBar();
        verify(view).initializeIdentificationTypes(stubIdentificationTypes, stateModel.getIdentificationType());
        verify(view).hideProgressBar();
        verify(view).requestIdentificationNumberFocus();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenInitializePresenterAndIdentificationTypesAreEmptyThenShowMissingIdentificationTypesError() {
        final List<IdentificationType> stubIdentificationTypes = new ArrayList<>();
        when(stateModel.hasIdentificationTypes()).thenReturn(false);
        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubSuccessMpCall<>(stubIdentificationTypes));

        presenter.attachView(view);

        verify(view).showProgressBar();
        verify(view).showMissingIdentificationTypesError();
        verify(view).hideProgressBar();
        verify(view).requestIdentificationNumberFocus();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenGetIdentificationTypesFailThenShowError() {
        final ApiException apiException = mock(ApiException.class);

        when(stateModel.hasIdentificationTypes()).thenReturn(false);
        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubFailMpCall<>(apiException));

        presenter.attachView(view);

        verify(view).showProgressBar();
        verify(view).showError(any(MercadoPagoError.class), anyString());
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenGetIdentificationTypesFailAndRecoverThenShowThem() {
        final ApiException apiException = mock(ApiException.class);
        final List<IdentificationType> stubIdentificationTypes = IdentificationTypes.getIdentificationTypes();

        when(stateModel.hasIdentificationTypes()).thenReturn(false);
        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubFailMpCall<>(apiException));

        presenter.attachView(view);

        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubSuccessMpCall<>(stubIdentificationTypes));

        presenter.recoverFromFailure();

        verify(view, atLeast(2)).showProgressBar();
        verify(view).showError(any(MercadoPagoError.class), anyString());
        verify(view).initializeIdentificationTypes(stubIdentificationTypes, stateModel.getIdentificationType());
        verify(view).hideProgressBar();
        verify(view).requestIdentificationNumberFocus();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenNameIsValidThenClearError() {
        when(stateModel.hasIdentificationTypes()).thenReturn(true);
        presenter.attachView(view);

        when(stateModel.getIdentificationName()).thenReturn(DUMMY_NAME);
        presenter.checkIsEmptyOrValidName();

        verify(view).clearErrorView();
        verify(view).clearErrorName();
    }

    @Test
    public void whenNameIsNotValidThenSetErrorView() {
        when(stateModel.hasIdentificationTypes()).thenReturn(true);
        when(stateModel.getIdentificationName()).thenReturn(null);
        presenter.attachView(view);

        presenter.validateName();

        verify(view).setInvalidIdentificationNameErrorView();
        verify(view).setErrorName();
    }

    @Test
    public void whenLastNameIsValidThenClearError() {
        when(stateModel.hasIdentificationTypes()).thenReturn(true);
        when(stateModel.getIdentificationLastName()).thenReturn(DUMMY_NAME);
        presenter.attachView(view);

        presenter.checkIsEmptyOrValidLastName();

        verify(view).clearErrorView();
        verify(view).clearErrorLastName();
    }

    @Test
    public void whenLastNameIsNotValidThenSetErrorView() {

        when(stateModel.hasIdentificationTypes()).thenReturn(true);
        when(stateModel.getIdentificationLastName()).thenReturn(null);
        presenter.attachView(view);

        presenter.validateLastName();

        verify(view).setInvalidIdentificationLastNameErrorView();
        verify(view).setErrorLastName();
    }

    //"APP_USR-f3f035a2-d343-4a6f-bd3b-fc3c3cb72416", "245099733-8771f469-d68e-4863-b8cb-9402e22c6bb2"
    @Test
    public void whenNumberIsNotValidThenSetErrorView() {
        final IdentificationType identificationType = IdentificationTypes.getIdentificationTypeCPF();
        final Identification identification = IdentificationUtils.getIdentificationWithWrongNumberCPF();
        when(stateModel.getIdentificationType()).thenReturn(identificationType);
        when(stateModel.hasIdentificationTypes()).thenReturn(true);
        when(stateModel.getIdentification()).thenReturn(identification);
        presenter.attachView(view);

        presenter.validateIdentificationNumber();

        verify(view).setInvalidIdentificationNumberErrorView();
        verify(view).setErrorIdentificationNumber();
    }

    @Test
    public void whenNumberIsValidThenClearError() {
        final IdentificationType identificationType = IdentificationTypes.getIdentificationTypeCPF();
        final Identification identification = IdentificationUtils.getIdentificationCPF();
        when(stateModel.getIdentificationType()).thenReturn(identificationType);
        when(stateModel.hasIdentificationTypes()).thenReturn(true);
        when(stateModel.getIdentification()).thenReturn(identification);
        presenter.attachView(view);

        presenter.validateIdentificationNumber();

        verify(view).clearErrorView();
        verify(view).clearErrorIdentificationNumber();
    }

    @Test
    public void whenSavedIdentificationTypeIsNotNullThenSetIdentificationNumberRestrictions() {
        final List<IdentificationType> stubIdentificationTypes = IdentificationTypes.getIdentificationTypes();
        final IdentificationType identificationType = stubIdentificationTypes.get(0);

        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubSuccessMpCall<>(stubIdentificationTypes));
        when(stateModel.getIdentification()).thenReturn(new Identification());

        presenter.attachView(view);
        presenter.saveIdentificationType(identificationType);

        verify(view).showProgressBar();
        verify(view).initializeIdentificationTypes(stubIdentificationTypes, stateModel.getIdentificationType());
        verify(view).hideProgressBar();
        verify(view).requestIdentificationNumberFocus();
        verify(view).setIdentificationNumberRestrictions(identificationType.getType());
        verifyNoMoreInteractions(view);
    }
}
