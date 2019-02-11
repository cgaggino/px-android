package com.mercadopago.android.px.internal.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.tracking.internal.TrackingContract;
import com.mercadopago.android.px.tracking.internal.events.AbortEvent;
import com.mercadopago.android.px.tracking.internal.events.BackEvent;
import com.mercadopago.android.px.tracking.internal.views.ViewTracker;
import java.lang.ref.WeakReference;

/**
 * Base class for all <code>BasePresenter</code> implementations.
 */

public class BasePresenter<V extends MvpView> {

    private transient WeakReference<V> mView;

    @Nullable private transient ViewTracker viewTracker;

    protected final void setCurrentViewTracker(@NonNull final ViewTracker viewTracker) {
        this.viewTracker = viewTracker;
        viewTracker.track();
    }

    protected final transient TrackingContract tracker = new TrackingContract() {

        @Override
        public void trackAbort() {
            if (viewTracker != null) {
                new AbortEvent(viewTracker).track();
            }
        }

        @Override
        public void trackBack() {
            if (viewTracker != null) {
                new BackEvent(viewTracker).track();
            }
        }
    };

    public void recoverFromBundle(@NonNull final Bundle bundle) {
    }

    @NonNull
    public Bundle storeInBundle(@NonNull final Bundle bundle) {
        return bundle;
    }

    public void attachView(final V view) {
        mView = new WeakReference<>(view);
    }

    public void detachView() {
        if (mView != null) {
            mView.clear();
            mView = null;
        }
    }

    public boolean isViewAttached() {
        return mView != null && mView.get() != null;
    }

    @NonNull
    public V getView() {
        if (mView == null) {
            throw new IllegalStateException("view not attached");
        } else {
            return mView.get();
        }
    }
}