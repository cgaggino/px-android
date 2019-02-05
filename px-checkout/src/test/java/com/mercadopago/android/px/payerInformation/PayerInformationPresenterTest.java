package com.mercadopago.android.px.payerInformation;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.features.PayerInformationPresenter;
import com.mercadopago.android.px.internal.features.PayerInformationView;
import com.mercadopago.android.px.internal.repository.IdentificationRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.mocks.IdentificationTypes;
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

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PayerInformationPresenterTest {

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
    public void clearErrorNameWhenNameIsValid() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

        List<IdentificationType> identificationTypesList = IdentificationTypes.getIdentificationTypes();
        provider.setIdentificationTypesResponse(identificationTypesList);

        PayerInformationPresenter presenter = new PayerInformationPresenter(
            paymentSettingRepository, identificationRepository);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        presenter.setIdentificationName("Name");

        assertTrue(presenter.validateName());
        assertTrue(mockedView.clearErrorName);
        assertTrue(mockedView.clearErrorView);
    }

    @Test
    public void setErrorViewWhenNameIsNotValid() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

        List<IdentificationType> identificationTypesList = IdentificationTypes.getIdentificationTypes();
        provider.setIdentificationTypesResponse(identificationTypesList);

        PayerInformationPresenter presenter = new PayerInformationPresenter(
            paymentSettingRepository, identificationRepository);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        presenter.setIdentificationName("");

        assertFalse(presenter.validateName());
        assertTrue(mockedView.setErrorName);
        assertTrue(mockedView.setErrorView);
    }

    @Test
    public void setErrorViewWhenNumberIsNotValid() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

        List<IdentificationType> identificationTypesList = IdentificationTypes.getIdentificationTypes();
        provider.setIdentificationTypesResponse(identificationTypesList);

        IdentificationType identificationType = getIdentificationTypeCPF();
        Identification identification = getIdentificationWithWrongNumberCPF();

        PayerInformationPresenter presenter = new PayerInformationPresenter(
            paymentSettingRepository, identificationRepository);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);
        presenter.setIdentificationType(identificationType);
        presenter.setIdentification(identification);

        assertFalse(presenter.validateIdentificationNumber());
        assertTrue(mockedView.setErrorIdentificationNumber);
        assertTrue(mockedView.setErrorView);
    }

    @Test
    public void clearErrorNumberWhenNumberIsValid() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

        List<IdentificationType> identificationTypesList = IdentificationTypes.getIdentificationTypes();
        provider.setIdentificationTypesResponse(identificationTypesList);

        IdentificationType identificationType = getIdentificationTypeCPF();
        Identification identification = getIdentificationCPF();

        PayerInformationPresenter presenter = new PayerInformationPresenter(
            paymentSettingRepository, identificationRepository);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);
        presenter.setIdentificationType(identificationType);
        presenter.setIdentification(identification);

        assertTrue(presenter.validateIdentificationNumber());
        assertTrue(mockedView.clearErrorIdentificationNumber);
        assertTrue(mockedView.clearErrorView);
    }

    private Identification getIdentificationCPF() {
        String type = "CPF";
        String identificationNumber = "89898989898";

        Identification identification = new Identification();
        identification.setNumber(identificationNumber);
        identification.setType(type);

        return identification;
    }

    private Identification getIdentificationWithWrongNumberCPF() {
        String type = "CPF";
        String identificationNumber = "";

        Identification identification = new Identification();
        identification.setNumber(identificationNumber);
        identification.setType(type);

        return identification;
    }

    private IdentificationType getIdentificationTypeCPF() {
        String type = "number";
        String id = "CPF";
        String name = "CPF";

        IdentificationType identificationType = new IdentificationType();
        identificationType.setType(type);
        identificationType.setId(id);
        identificationType.setMaxLength(11);
        identificationType.setMinLength(11);
        identificationType.setName(name);

        return identificationType;
    }

    private class MockedView implements PayerInformationView {

        private boolean initializeIdentificationTypes;
        private boolean showError;
        private boolean clearErrorView;
        private boolean clearErrorName;
        private boolean setErrorName;
        private boolean setErrorView;
        private boolean clearErrorIdentificationNumber;
        private boolean setErrorIdentificationNumber;
        private String errorMessage;

        private MercadoPagoError mercadoPagoError;

        @Override
        public void initializeIdentificationTypes(List<IdentificationType> identificationTypes) {
            this.initializeIdentificationTypes = true;
        }

        @Override
        public void setIdentificationNumberRestrictions(String type) {
            //Add test
        }

        @Override
        public void clearErrorIdentificationNumber() {
            this.clearErrorIdentificationNumber = true;
        }

        @Override
        public void clearErrorName() {
            this.clearErrorName = true;
        }

        @Override
        public void clearErrorLastName() {
            //Add test
        }

        @Override
        public void setErrorIdentificationNumber() {
            this.setErrorIdentificationNumber = true;
        }

        @Override
        public void setErrorName() {
            this.setErrorName = true;
        }

        @Override
        public void setErrorLastName() {
            //Add test
        }

        @Override
        public void setErrorView(String message) {
            this.errorMessage = message;
            this.setErrorView = true;
        }

        @Override
        public void clearErrorView() {
            this.clearErrorView = true;
        }

        @Override
        public void showInputContainer() {
            //Add test
        }

        @Override
        public void showError(MercadoPagoError error, String requestOrigin) {
            this.mercadoPagoError = error;
            this.showError = true;
        }

        @Override
        public void showProgressBar() {
            //Add test
        }

        @Override
        public void hideProgressBar() {
            //Add test
        }
    }

    private class MockedProvider implements PayerInformationProvider {

        private boolean shouldFail;
        private MercadoPagoError failedResponse;
        private List<IdentificationType> successfulIdentificationTypesResponse;

        @Override
        public String getInvalidIdentificationNumberErrorMessage() {
            return null;
        }

        @Override
        public String getInvalidIdentificationNameErrorMessage() {
            return null;
        }

        @Override
        public String getInvalidIdentificationLastNameErrorMessage() {
            return null;
        }

        @Override
        public String getInvalidIdentificationBusinessNameErrorMessage() {
            return null;
        }

        @Override
        public String getMissingPublicKeyErrorMessage() {
            return null;
        }

        @Override
        public String getMissingIdentificationTypesErrorMessage() {
            return null;
        }

        public void setIdentificationTypesResponse(List<IdentificationType> identificationTypes) {
            shouldFail = false;
            successfulIdentificationTypesResponse = identificationTypes;
        }

        public void setIdentificationTypesResponse(MercadoPagoError exception) {
            shouldFail = true;
            failedResponse = exception;
        }
    }
}
