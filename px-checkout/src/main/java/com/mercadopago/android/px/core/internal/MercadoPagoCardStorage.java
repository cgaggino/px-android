package com.mercadopago.android.px.core.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivity;

/**
 * Provides access to card storage flow
 */
@SuppressWarnings("unused")
public final class MercadoPagoCardStorage {

    private MercadoPagoCardStorage() {
    }

    /**
     * Starts card storage flow, walking the user through the card details form,
     * with the purpose of storing the card to "My Cards" section, allowing the user to
     * use it anytime he/she wants.
     * <p>
     * The flow can end with a success, returning {@link Activity#RESULT_OK} to the caller,
     * informing that the card association succeeded.
     * <p>
     * In case the flow experiences an error of any kind, the flow will return a {@link Activity#RESULT_CANCELED}
     * informing that the card association failed.
     *
     * @param context the context that calls the flow
     * @param accessToken logged in user access token
     * @param requestCode it's the number that identifies the checkout flow request for
     * {@link Activity#onActivityResult(int, int, Intent)}
     */
    public static void startCardStorageFlow(@NonNull final Context context, @NonNull final String accessToken,
        final int requestCode) {
        GuessingCardActivity.startGuessingCardActivityForStorage(context, accessToken, requestCode);
    }
}
