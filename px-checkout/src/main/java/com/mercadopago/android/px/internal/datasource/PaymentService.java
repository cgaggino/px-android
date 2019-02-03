package com.mercadopago.android.px.internal.datasource;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.core.SplitPaymentProcessor;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandlerWrapper;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.EscManager;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.InstructionsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.TokenRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.mappers.AccountMoneyMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.CardMapper;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;
import java.util.ArrayList;
import java.util.List;

public class PaymentService implements PaymentRepository {

    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final DiscountRepository discountRepository;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final SplitPaymentProcessor paymentProcessor;
    @NonNull private final Context context;
    @NonNull private final TokenRepository tokenRepository;
    @NonNull private final GroupsRepository groupsRepository;
    @NonNull private final EscManager escManager;

    @NonNull /* default */ final PaymentServiceHandlerWrapper handlerWrapper;
    @NonNull /* default */ final AmountConfigurationRepository amountConfigurationRepository;
    @NonNull /* default */ final UserSelectionRepository userSelectionRepository;
    @NonNull /* default */ final PluginRepository pluginRepository;

    @Nullable private IPaymentDescriptor payment;

    public PaymentService(@NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final PluginRepository pluginRepository,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final AmountRepository amountRepository,
        @NonNull final SplitPaymentProcessor paymentProcessor,
        @NonNull final Context context,
        @NonNull final EscManager escManager,
        @NonNull final TokenRepository tokenRepository,
        @NonNull final InstructionsRepository instructionsRepository,
        @NonNull final GroupsRepository groupsRepository,
        @NonNull final AmountConfigurationRepository amountConfigurationRepository) {
        this.amountConfigurationRepository = amountConfigurationRepository;
        this.escManager = escManager;
        this.userSelectionRepository = userSelectionRepository;
        this.pluginRepository = pluginRepository;
        this.paymentSettingRepository = paymentSettingRepository;
        this.discountRepository = discountRepository;
        this.amountRepository = amountRepository;
        this.paymentProcessor = paymentProcessor;
        this.context = context;
        this.tokenRepository = tokenRepository;
        this.groupsRepository = groupsRepository;

        handlerWrapper = new PaymentServiceHandlerWrapper(this, escManager, instructionsRepository);
    }

    @Override
    public void attach(@NonNull final PaymentServiceHandler handler) {
        handlerWrapper.setHandler(handler);
        handlerWrapper.processMessages();
    }

    @Override
    public void detach(@NonNull final PaymentServiceHandler handler) {
        handlerWrapper.detach(handler);
    }

    @Override
    public void storePayment(@NonNull final IPaymentDescriptor iPayment) {
        payment = iPayment;
    }

    @Nullable
    @Override
    public IPaymentDescriptor getPayment() {
        return payment;
    }

    @Override
    public boolean hasPayment() {
        return payment != null;
    }

    @NonNull
    @Override
    public PaymentRecovery createRecoveryForInvalidESC() {
        return new PaymentRecovery(Payment.StatusDetail.STATUS_DETAIL_INVALID_ESC);
    }

    @NonNull
    @Override
    public PaymentRecovery createPaymentRecovery() {
        return new PaymentRecovery(getPayment().getPaymentStatusDetail());
    }

    /**
     * This method presets all user information ahead before the payment is processed.
     *
     * @param expressMetadata model
     * @param splitPayment
     */
    @Override
    public void startExpressPayment(@NonNull final ExpressMetadata expressMetadata,
        @Nullable final PayerCost payerCost,
        final boolean splitPayment) {

        groupsRepository.getGroups().enqueue(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                final String paymentTypeId = expressMetadata.getPaymentTypeId();

                if (PaymentTypes.isCardPaymentType(paymentTypeId)) {
                    // Saved card.
                    final Card card = new CardMapper(paymentMethodSearch).map(expressMetadata);
                    if (splitPayment) {
                        //TODO refactor
                        final String secondaryPaymentMethodId =
                            amountConfigurationRepository
                                .getConfigurationFor(card.getId())
                                .getSplitConfiguration().secondaryPaymentMethod.paymentMethodId;
                        userSelectionRepository
                            .select(card, paymentMethodSearch.getPaymentMethodById(secondaryPaymentMethodId));
                    } else {
                        userSelectionRepository.select(card, null);
                    }

                    userSelectionRepository.select(payerCost);
                } else if (PaymentTypes.isPlugin(paymentTypeId)) {
                    userSelectionRepository.select(pluginRepository
                            .getPluginAsPaymentMethod(expressMetadata.getPaymentMethodId(), paymentTypeId),
                        null);
                } else if (PaymentTypes.isAccountMoney(paymentTypeId)) {
                    final PaymentMethod paymentMethod =
                        new AccountMoneyMapper(paymentMethodSearch).map(expressMetadata);

                    userSelectionRepository.select(paymentMethod, null);
                } else {
                    throw new IllegalStateException("payment method selected can not be used for express payment");
                }

                startPayment();
            }

            @Override
            public void failure(final ApiException apiException) {
                throw new IllegalStateException("empty payment methods");
            }
        });
    }

    @Override
    public void startPayment() {
        //Wrapping the callback is important to assure ESC handling.
        checkPaymentMethod();
    }

    /* default */ void checkPaymentMethod() {
        final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();
        if (paymentMethod != null) {
            processPaymentMethod(paymentMethod);
        } else {
            handlerWrapper.onPaymentError(getError());
        }
    }

    private void processPaymentMethod(final PaymentMethod paymentMethod) {
        if (PaymentTypes.isCardPaymentType(paymentMethod.getPaymentTypeId())) {
            payWithCard();
        } else {
            pay();
        }
    }

    private void payWithCard() {
        if (hasValidSavedCardInfo()) {
            if (paymentSettingRepository.hasToken()) { // Paying with saved card with token
                pay();
            } else { // Token does not exists - must generate one or ask for CVV.
                checkEscAvailability();
            }
        } else if (hasValidNewCardInfo()) { // New card payment
            pay();
        } else { // Guessing card could not tokenize or obtain card information.
            handlerWrapper.onPaymentError(getError());
        }
    }

    private void checkEscAvailability() {
        //Paying with saved card without token
        final Card card = userSelectionRepository.getCard();

        if (escManager.hasEsc(card)) {
            //Saved card has ESC - Try to tokenize
            tokenRepository.createToken(card).enqueue(new Callback<Token>() {
                @Override
                public void success(final Token token) {
                    checkPaymentMethod();
                }

                @Override
                public void failure(final ApiException apiException) {
                    //Start CVV screen if fail
                    handlerWrapper.onCvvRequired(card);
                }
            });
        } else {
            //Saved card has no ESC saved - CVV is requiered.
            handlerWrapper.onCvvRequired(card);
        }
    }

    private boolean hasValidSavedCardInfo() {
        return userSelectionRepository.hasCardSelected()
            && userSelectionRepository.getPayerCost() != null;
    }

    private boolean hasValidNewCardInfo() {
        return userSelectionRepository.getPaymentMethod() != null
            && userSelectionRepository.getIssuer() != null
            && userSelectionRepository.getPayerCost() != null
            && paymentSettingRepository.hasToken();
    }

    private void pay() {
        final CheckoutPreference checkoutPreference = paymentSettingRepository.getCheckoutPreference();
        if (paymentProcessor.shouldShowFragmentOnPayment(checkoutPreference)) {
            handlerWrapper.onVisualPayment();
        } else {
            final SplitPaymentProcessor.CheckoutData checkoutData =
                new SplitPaymentProcessor.CheckoutData(getPaymentDataList(), checkoutPreference);
            paymentProcessor.startPayment(context, checkoutData, handlerWrapper);
        }
    }

    @Override
    public boolean isExplodingAnimationCompatible() {
        return !paymentProcessor.shouldShowFragmentOnPayment(paymentSettingRepository.getCheckoutPreference());
    }

    /**
     * Payment data is a dynamic non-mutable object that represents the payment state of the checkout exp.
     *
     * @return payment data at the moment is called.
     */
    @NonNull
    @Override
    public List<PaymentData> getPaymentDataList() {
        final List<PaymentData> paymentDataList = new ArrayList<>();

        //TODO tokens
        final DiscountConfigurationModel discountModel = discountRepository.getCurrentConfiguration();
        final PaymentMethod secondaryPaymentMethod = userSelectionRepository.getSecondaryPaymentMethod();

        final PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(userSelectionRepository.getPaymentMethod());
        paymentData.setPayerCost(userSelectionRepository.getPayerCost());
        paymentData.setToken(paymentSettingRepository.getToken());
        paymentData.setIssuer(userSelectionRepository.getIssuer());
        paymentData.setPayer(paymentSettingRepository.getCheckoutPreference().getPayer());
        paymentData.setTransactionAmount(amountRepository.getAmountToPay());

        paymentData.setCampaign(discountModel.getCampaign());

        paymentDataList.add(paymentData);

        if (secondaryPaymentMethod != null) {

            final AmountConfiguration currentConfiguration = amountConfigurationRepository.getCurrentConfiguration();

            final PaymentData secondaryPaymentData = new PaymentData();
            secondaryPaymentData.setPaymentMethod(secondaryPaymentMethod);
            secondaryPaymentData.setPayer(paymentSettingRepository.getCheckoutPreference().getPayer());
            secondaryPaymentData
                .setTransactionAmount(currentConfiguration.getSplitConfiguration().secondaryPaymentMethod.amount);
            secondaryPaymentData.setCampaign(discountModel.getCampaign());
            paymentData.setDiscount(currentConfiguration.getSplitConfiguration().primaryPaymentMethod.discount);
            secondaryPaymentData
                .setDiscount(currentConfiguration.getSplitConfiguration().secondaryPaymentMethod.discount);
            paymentDataList.add(secondaryPaymentData);
        } else {
            final Discount discount = userSelectionRepository.getCard() == null ? discountModel.getDiscount()
                : Discount.replaceWith(discountModel.getDiscount(),
                    amountConfigurationRepository.getCurrentConfiguration().getDiscountToken());
            paymentData.setDiscount(discount);
        }

        return paymentDataList;
    }

    /**
     * Transforms IPayment into a {@link PaymentResult}
     *
     * @param payment The payment model
     * @return The transformed {@link PaymentResult}
     */
    @NonNull
    @Override
    public PaymentResult createPaymentResult(@NonNull final IPaymentDescriptor payment) {
        return new PaymentResult.Builder()
            .setPaymentData(getPaymentDataList())
            .setPaymentId(payment.getId())
            .setPaymentMethodId(payment.getPaymentMethodId())
            .setPaymentStatus(payment.getPaymentStatus())
            .setStatementDescription(payment.getStatementDescription())
            .setPaymentStatusDetail(payment.getPaymentStatusDetail())
            .build();
    }

    @Override
    public int getPaymentTimeout() {
        return paymentProcessor.getPaymentTimeout(paymentSettingRepository.getCheckoutPreference());
    }

    public MercadoPagoError getError() {
        return new MercadoPagoError("Something went wrong", false);
    }
}
