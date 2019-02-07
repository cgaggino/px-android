package com.mercadopago.android.px.internal.features;

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

public class BankDealsPresenter extends BasePresenter<BankDealsView> implements BankDeals.Actions {

    private FailureRecovery failureRecovery;
    private BankDealsRepository bankDealsRepository;

    public BankDealsPresenter(final BankDealsRepository bankDealsRepository) {
        this.bankDealsRepository = bankDealsRepository;
    }

    @Override
    public void trackView() {
        final BankDealsViewTracker bankDealsViewTracker = new BankDealsViewTracker();
        setCurrentViewTracker(bankDealsViewTracker);
    }

    private OnSelectedCallback<BankDeal> getOnSelectedCallback() {
        return new OnSelectedCallback<BankDeal>() {
            @Override
            public void onSelected(final BankDeal bankDeal) {
                getView().showBankDealDetail(bankDeal);
            }
        };
    }

    @Override
    public void getBankDeals() {
        getView().showLoadingView();
        bankDealsRepository.getBankDealsAsync()
            .enqueue(new TaggedCallback<List<BankDeal>>(ApiUtil.RequestOrigin.GET_BANK_DEALS) {

                @Override
                public void onSuccess(final List<BankDeal> bankDeals) {
                    solveBankDeals(bankDeals);
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    failureRecovery = new FailureRecovery() {
                        @Override
                        public void recover() {
                            getBankDeals();
                        }
                    };
                    getView().showApiExceptionError(error);
                }
            });
    }

    @Override
    public void solveBankDeals(@NonNull final List<BankDeal> bankDeals) {
        getView().showBankDeals(bankDeals, getOnSelectedCallback());
    }

    @Override
    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }
}
