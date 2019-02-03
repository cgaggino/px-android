package com.mercadopago.android.px.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.mercadopago.android.px.R;
import java.io.Serializable;

public final class CustomStringConfiguration implements Serializable {

    @StringRes private final int mainVerbStringResourceId;

    /* default */ CustomStringConfiguration(@NonNull final Builder builder) {
        mainVerbStringResourceId = builder.mainVerbStringResourceId;
    }

    /**
     * Let us know what the main verb is
     *
     * @return custom main verb or the default one
     */
    @StringRes
    public int getMainVerbStringResourceId() {
        return mainVerbStringResourceId;
    }

    public static class Builder {
        /* default */ int mainVerbStringResourceId;

        public Builder() {
            mainVerbStringResourceId = R.string.px_main_verb;
        }

        /**
         * Used to replace the main verb in Payment Methods screen
         *
         * @param mainVerbStringResId the string resource that will be used
         * @return builder to keep operating
         */
        @SuppressWarnings("unused")
        public Builder setMainVerbStringResourceId(@StringRes final int mainVerbStringResId) {
            mainVerbStringResourceId = mainVerbStringResId;
            return this;
        }

        public CustomStringConfiguration build() {
            return new CustomStringConfiguration(this);
        }
    }
}
