package com.mercadopago.android.px.internal.features.bank_deal_detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.model.BankDeal;

public class BankDealDetailActivity extends PXActivity<BankDealDetailPresenter> implements BankDealDetail.View {

    private static final String EXTRA_MODEL = "extra_model";

    private ImageView logo;
    private TextView logoName;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.px_activity_bank_deal_detail);
        createPresenter();
        initializeControls();
    }

    public static void startWithBankDealLegals(@NonNull final Context context,
        @NonNull final BankDeal bankDeal) {
        final Intent intent = new Intent(context, BankDealDetailActivity.class);
        intent.putExtra(EXTRA_MODEL, BankDealDetailModel.createWith(bankDeal));
        context.startActivity(intent);
    }

    private void initializeControls() {
        final BankDealDetailModel model = getIntent().getParcelableExtra(EXTRA_MODEL);
        initView(model);
    }

    private void createPresenter() {
        presenter = new BankDealDetailPresenter();
        presenter.attachView(this);
    }

    private void initView(final BankDealDetailModel model) {
        initToolbar();
        logo = findViewById(R.id.logo);
        final TextView title = findViewById(R.id.title);
        logoName = findViewById(R.id.logo_name);
        final TextView expDate = findViewById(R.id.exp_date);
        final TextView legals = findViewById(R.id.legals);
        expDate.setText(getString(R.string.bank_deal_details_date_format, model.formattedExpirationDate));
        ViewUtils.loadOrGone(Html.fromHtml(model.dealTitle), title);
        ViewUtils.loadOrGone(model.legal, legals);
        logoName.setText(model.issuerName);
        ViewUtils.loadOrCallError(model.imgUrl, logo, presenter);
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.mpsdkToolbar);
        final TextView titleToolbar = toolbar.findViewById(R.id.mpsdkTitle);
        setSupportActionBar(toolbar);
        final ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayShowTitleEnabled(false);
        supportActionBar.setDisplayShowTitleEnabled(false);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setDisplayShowHomeEnabled(true);
        titleToolbar.setText(R.string.bank_deal_details_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void hideLogoName() {
        logoName.setVisibility(View.GONE);
    }

    @Override
    public void hideLogo() {
        logo.setVisibility(View.GONE);
    }

    private static class BankDealDetailModel implements Parcelable {

        private final String imgUrl;
        private final String legal;
        private final String formattedExpirationDate;
        private final String dealTitle;
        private final String issuerName;

        private static BankDealDetailModel createWith(final BankDeal bankDeal) {
            final String imageUrl = bankDeal.hasPictureUrl() ? bankDeal.getPicture().getUrl() : "";
            final String issuerName = bankDeal.getIssuer() != null ? bankDeal.getIssuer().getName() : "";
            final String formattedTitle = bankDeal.getRecommendedMessage();

            return new BankDealDetailModel(imageUrl,
                bankDeal.getLegals(),
                bankDeal.getPrettyExpirationDate(),
                formattedTitle,
                issuerName);
        }

        private BankDealDetailModel(final String imgUrl,
            final String legal,
            final String formattedExpirationDate,
            final String dealTitle,
            final String issuerName) {
            this.imgUrl = imgUrl;
            this.legal = legal;
            this.formattedExpirationDate = formattedExpirationDate;
            this.dealTitle = dealTitle;
            this.issuerName = issuerName;
        }

        protected BankDealDetailModel(Parcel in) {
            imgUrl = in.readString();
            legal = in.readString();
            formattedExpirationDate = in.readString();
            dealTitle = in.readString();
            issuerName = in.readString();
        }

        public static final Creator<BankDealDetailModel> CREATOR = new Creator<BankDealDetailModel>() {
            @Override
            public BankDealDetailModel createFromParcel(final Parcel in) {
                return new BankDealDetailModel(in);
            }

            @Override
            public BankDealDetailModel[] newArray(final int size) {
                return new BankDealDetailModel[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeString(imgUrl);
            dest.writeString(legal);
            dest.writeString(formattedExpirationDate);
            dest.writeString(dealTitle);
            dest.writeString(issuerName);
        }
    }
}
