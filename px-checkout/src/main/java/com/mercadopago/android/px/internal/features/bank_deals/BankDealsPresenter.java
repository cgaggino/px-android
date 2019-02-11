package com.mercadopago.android.px.internal.features.bank_deals;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.repository.BankDealsRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.views.BankDealsViewTracker;
import java.util.List;

/* default */ class BankDealsPresenter extends BasePresenter<BankDeals.View>
    implements BankDeals.Actions, OnSelectedCallback<BankDeal> {

    private FailureRecovery failureRecovery;
    private BankDealsRepository bankDealsRepository;

    /* package */ BankDealsPresenter(final BankDealsRepository bankDealsRepository) {
        this.bankDealsRepository = bankDealsRepository;
    }

    @Override
    public void initialize() {
        trackView();
        getBankDeals();
    }

    @Override
    public void trackView() {
        final BankDealsViewTracker bankDealsViewTracker = new BankDealsViewTracker();
        setCurrentViewTracker(bankDealsViewTracker);
    }

    @Override
    public void getBankDeals() {
        getView().showLoadingView();
        bankDealsRepository.getBankDealsAsync()
            .enqueue(new TaggedCallback<List<BankDeal>>(ApiUtil.RequestOrigin.GET_BANK_DEALS) {

                @Override
                public void onSuccess(final List<BankDeal> bankDeals) {
                    if (isViewAttached()) {
                        solveBankDeals(bankDeals);
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        failureRecovery = new FailureRecovery() {
                            @Override
                            public void recover() {
                                getBankDeals();
                            }
                        };
                        getView().showApiExceptionError(error);
                    }
                }
            });
    }

    private void solveBankDeals(@NonNull final List<BankDeal> bankDeals) {
        getView().showBankDeals(bankDeals, this);
    }

    @Override
    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }

    @Override
    public void onSelected(final BankDeal bankDeal) {
        getView().showBankDealDetail(bankDeal);
    }
}
