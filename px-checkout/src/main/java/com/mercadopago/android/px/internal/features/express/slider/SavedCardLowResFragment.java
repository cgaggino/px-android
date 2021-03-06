package com.mercadopago.android.px.internal.features.express.slider;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.viewmodel.drawables.SavedCardDrawableFragmentItem;

public class SavedCardLowResFragment extends SavedCardFragment {

    @SuppressWarnings("TypeMayBeWeakened")
    @NonNull
    public static Fragment getInstance(final SavedCardDrawableFragmentItem savedCard) {
        final SavedCardLowResFragment savedCardFragment = new SavedCardLowResFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_CARD, savedCard);
        savedCardFragment.setArguments(bundle);
        return savedCardFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_saved_card_low_res, container, false);
    }

    @Override
    protected void setIssuerIcon(@NonNull final View view, @NonNull final SavedCardDrawableFragmentItem drawableCard) {
        //Do nothing - low res has not issuer icon.
    }

    @Override
    protected void setCardNumber(@NonNull final View view,
        @NonNull final SavedCardDrawableFragmentItem drawableCard, final int fontColor){
        final TextView cardNumber = view.findViewById(R.id.card_number);
        cardNumber.setTextColor(fontColor);
        cardNumber.setText(drawableCard.card.getCardPattern());
    }
}
