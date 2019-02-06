package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.BankDeal;
import java.util.List;

public interface BankDeals {
    interface Actions{
        void trackView();
        void getBankDeals();
        void solveBankDeals(@NonNull final List<BankDeal> bankDeals);
        void recoverFromFailure();
    }
}
