package com.mercadopago.android.px.internal.features.bank_deals;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.repository.BankDealsRepository;
import com.mercadopago.android.px.mocks.BankDealsUtils;
import com.mercadopago.android.px.model.BankDeal;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankDealsPresenterTest {

    private BankDealsPresenter presenter;

    @Mock private BankDealsRepository bankDealsRepository;
    @Mock private BankDeals.View view;

    @Before
    public void setUp() {
        presenter = getPresenter();
    }

    @NonNull
    private BankDealsPresenter getBasePresenter(final BankDeals.View view) {
        final BankDealsPresenter presenter = new BankDealsPresenter(bankDealsRepository);
        presenter.attachView(view);
        return presenter;
    }

    @NonNull
    private BankDealsPresenter getPresenter() {
        return getBasePresenter(view);
    }

    @Test
    public void whenGetBankDealsWithSuccessThenShowThem() {
        final List<BankDeal> bankDealsListMLA = BankDealsUtils.getBankDealsListMLA();
        when(bankDealsRepository.getBankDealsAsync())
            .thenReturn(new StubSuccessMpCall<>(bankDealsListMLA));

        presenter.initialize();

        verify(view).showLoadingView();
        verify(view).showBankDeals(eq(bankDealsListMLA), any(OnSelectedCallback.class));
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenGetBankDealsFailsThenShowError() {
        final ApiException apiException = mock(ApiException.class);
        when(bankDealsRepository.getBankDealsAsync())
            .thenReturn(new StubFailMpCall<>(apiException));

        presenter.initialize();

        verify(view).showLoadingView();
        verify(view).showApiExceptionError(any(MercadoPagoError.class));
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenGetBankDealsFailsAndRecoverThenShowBankDeals() {
        final ApiException apiException = mock(ApiException.class);
        final List<BankDeal> bankDealsListMLA = BankDealsUtils.getBankDealsListMLA();

        when(bankDealsRepository.getBankDealsAsync())
            .thenReturn(new StubFailMpCall<>(apiException));

        presenter.initialize();

        when(bankDealsRepository.getBankDealsAsync())
            .thenReturn(new StubSuccessMpCall<>(bankDealsListMLA));

        presenter.recoverFromFailure();

        verify(view, atLeast(2)).showLoadingView();
        verify(view).showApiExceptionError(any(MercadoPagoError.class));
        verify(view).showBankDeals(eq(bankDealsListMLA), any(OnSelectedCallback.class));
        verifyNoMoreInteractions(view);
    }
}
