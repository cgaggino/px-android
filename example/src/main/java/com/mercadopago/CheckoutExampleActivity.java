package com.mercadopago;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.mercadopago.android.px.internal.view.MPButton;
import com.mercadopago.android.px.utils.ExamplesUtils;
import com.mercadopago.example.R;

import static com.mercadopago.android.px.utils.ExamplesUtils.resolveCheckoutResult;

public class CheckoutExampleActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private View mRegularLayout;
    private MPButton continueSimpleCheckout;
    private static final int REQ_CODE_CHECKOUT = 1;
    private static final int REQ_CODE_JSON = 2;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog()
            .build());

        setContentView(R.layout.activity_checkout_example);

        mRegularLayout = findViewById(R.id.regularLayout);

        final View jsonConfigurationButton = findViewById(R.id.jsonConfigButton);
        continueSimpleCheckout = findViewById(R.id.continueButton);

        final View selectCheckoutButton = findViewById(R.id.select_checkout);

        jsonConfigurationButton.setOnClickListener(v -> startJsonInput());

        selectCheckoutButton.setOnClickListener(
            v -> startActivity(new Intent(CheckoutExampleActivity.this, SelectCheckoutActivity.class)));

        continueSimpleCheckout.setOnClickListener(
            v -> ExamplesUtils.createBase().build().startPayment(CheckoutExampleActivity.this, REQUEST_CODE));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        resolveCheckoutResult(this, requestCode, resultCode, data, REQ_CODE_CHECKOUT);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        showRegularLayout();
        continueSimpleCheckout.setEnabled(true);
    }

    private void showRegularLayout() {

        mRegularLayout.setVisibility(View.VISIBLE);
    }

    private void startJsonInput() {
        final Intent intent = new Intent(this, JsonSetupActivity.class);
        startActivityForResult(intent, REQ_CODE_JSON);
    }
}