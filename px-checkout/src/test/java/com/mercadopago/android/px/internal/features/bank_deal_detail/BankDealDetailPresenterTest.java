package com.mercadopago.android.px.internal.features.bank_deal_detail;

import android.support.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class BankDealDetailPresenterTest {
    private BankDealDetailPresenter presenter;

    @Mock private BankDealDetail.View view;

    @Before
    public void setUp() {
        presenter = getPresenter();
    }

    @NonNull
    private BankDealDetailPresenter getBasePresenter(final BankDealDetail.View view) {
        final BankDealDetailPresenter presenter = new BankDealDetailPresenter();
        presenter.attachView(view);
        return presenter;
    }

    @NonNull
    private BankDealDetailPresenter getPresenter() {
        return getBasePresenter(view);
    }

    @Test
    public void whenGetViewCallbackOnSuccessThenHideLogoName(){
        presenter.onSuccess();

        verify(view).hideLogoName();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenGetViewCallbackOnErrorThenHideLogo(){
        presenter.onError();

        verify(view).hideLogo();
        verifyNoMoreInteractions(view);
    }

}
