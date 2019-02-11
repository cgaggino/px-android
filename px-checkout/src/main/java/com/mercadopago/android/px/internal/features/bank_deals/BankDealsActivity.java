package com.mercadopago.android.px.internal.features.bank_deals;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.adapters.BankDealsAdapter;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.bank_deal_detail.BankDealDetailActivity;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

public class BankDealsActivity extends PXActivity<BankDealsPresenter>
    implements BankDeals.View {

    protected RecyclerView bankDealsRecyclerView;
    protected Toolbar toolbar;
    private BankDealsAdapter bankDealsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.px_activity_bank_deals);
        initializeControls();
        createPresenter();
        presenter.initialize();
    }

    private void createPresenter() {
        final Session session = Session.getSession(this);
        presenter = new BankDealsPresenter(session
            .getBankDealsRepository());
        presenter.attachView(this);
    }

    protected void initializeControls() {
        initializeToolbar();
        bankDealsRecyclerView = findViewById(R.id.mpsdkBankDealsList);
        bankDealsRecyclerView.setHasFixedSize(true);
        bankDealsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void initializeToolbar() {
        toolbar = findViewById(R.id.mpsdkToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            presenter.attachView(this);
            if (resultCode == Activity.RESULT_OK) {
                presenter.recoverFromFailure();
            } else {
                finishWithCancelResult();
            }
        }
    }

    private void finishWithCancelResult() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void showApiExceptionError(@NonNull final MercadoPagoError error) {
        ErrorUtil.showApiExceptionError(this,
            error.getApiException(),
            ApiUtil.RequestOrigin.GET_BANK_DEALS);
    }

    @Override
    public void showLoadingView() {
        ViewUtils.showProgressLayout(this);
    }

    @Override
    public void showBankDeals(@NonNull final List<BankDeal> bankDeals,
        @NonNull final OnSelectedCallback<BankDeal> onSelectedCallback) {
        initializeAdapter(bankDeals, onSelectedCallback);
        ViewUtils.showRegularLayout(this);
    }

    private void initializeAdapter(final @NonNull List<BankDeal> bankDeals,
        final @NonNull OnSelectedCallback<BankDeal> onSelectedCallback) {
        bankDealsAdapter = new BankDealsAdapter(bankDeals, onSelectedCallback);
        bankDealsRecyclerView.setAdapter(bankDealsAdapter);
    }

    @Override
    public void showBankDealDetail(@NonNull final BankDeal bankDeal) {
        BankDealDetailActivity.startWithBankDealLegals(this, bankDeal);
    }
}
