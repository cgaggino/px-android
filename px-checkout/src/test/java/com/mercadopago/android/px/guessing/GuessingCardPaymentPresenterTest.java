package com.mercadopago.android.px.guessing;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivityView;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardPaymentPresenter;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.repository.BankDealsRepository;
import com.mercadopago.android.px.internal.repository.CardTokenRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.IdentificationRepository;
import com.mercadopago.android.px.internal.repository.IssuersRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.mocks.BankDealsUtils;
import com.mercadopago.android.px.mocks.Cards;
import com.mercadopago.android.px.mocks.DummyCard;
import com.mercadopago.android.px.mocks.IdentificationTypes;
import com.mercadopago.android.px.mocks.Issuers;
import com.mercadopago.android.px.mocks.PaymentMethods;
import com.mercadopago.android.px.mocks.Tokens;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Cardholder;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.utils.CardTestUtils;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.ExcessiveClassLength")
@RunWith(MockitoJUnitRunner.class)
public class GuessingCardPaymentPresenterTest {

    private GuessingCardPaymentPresenter presenter;

    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private GroupsRepository groupsRepository;
    @Mock private IssuersRepository issuersRepository;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private CardTokenRepository cardTokenRepository;
    @Mock private BankDealsRepository bankDealsRepository;
    @Mock private IdentificationRepository identificationRepository;

    @Mock private CheckoutPreference checkoutPreference;
    @Mock private PaymentPreference paymentPreference;

    @Mock private PaymentMethodSearch paymentMethodSearch;
    @Mock private AdvancedConfiguration advancedConfiguration;
    @Mock private Site site;
    @Mock private List<IdentificationType> identificationTypes;

    @Mock private GuessingCardActivityView view;

    @Before
    public void setUp() {
        // No charge initialization.
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreference.getSite()).thenReturn(site);
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);
        final List<PaymentMethod> pm = PaymentMethods.getPaymentMethodListMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(paymentMethodSearch.getPaymentMethods()).thenReturn(pm);
        when(advancedConfiguration.isBankDealsEnabled()).thenReturn(true);
        whenGetBankDealsAsync();
        identificationTypes = whenGetIdentificationTypesAsyncWithoutAccessToken();
        presenter = getPresenter();
    }

    @NonNull
    private GuessingCardPaymentPresenter getBasePresenter(
        final GuessingCardActivityView view) {
        final GuessingCardPaymentPresenter presenter =
            new GuessingCardPaymentPresenter(userSelectionRepository, paymentSettingRepository,
                groupsRepository, issuersRepository, cardTokenRepository, bankDealsRepository,
                identificationRepository, advancedConfiguration,
                new PaymentRecovery(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE)
            );
        presenter.attachView(view);
        return presenter;
    }

    @NonNull
    private GuessingCardPaymentPresenter getPresenter() {
        return getBasePresenter(view);
    }

    @Test
    public void whenPublicKeySetThenCheckValidStart() {
        presenter.initialize();
        verify(view).onValidStart();
    }

    @Test
    public void whenIdentificationTypesNotGetThenShowError() {
        final ApiException apiException = mock(ApiException.class);

        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubFailMpCall<List<IdentificationType>>(apiException));

        presenter.getIdentificationTypesAsync();

        verify(view).showError(any(MercadoPagoError.class), anyString());
    }

    @Test
    public void whenPaymentRecoverySetThenSaveCardholderNameAndIdentification() {

        final Cardholder cardHolder = mock(Cardholder.class);
        final Token token = mock(Token.class);
        when(paymentSettingRepository.getToken()).thenReturn(token);
        when(token.getCardHolder()).thenReturn(cardHolder);
        when(cardHolder.getIdentification()).thenReturn(mock(Identification.class));

        presenter
            .setPaymentRecovery(new PaymentRecovery(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE));

        presenter.initialize();

        verify(view).setCardholderName(cardHolder.getName());
        verify(view).setIdentificationNumber(cardHolder.getIdentification().getNumber());
    }

    @Test
    public void whenPaymentMethodListSetWithOnePaymentMethodThenSelectIt() {
        presenter.initialize();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());
        final PaymentMethod paymentMethod = mockedGuessedPaymentMethods.get(0);

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(view).setPaymentMethod(paymentMethod);
        verify(view).resolvePaymentMethodSet(paymentMethod);
    }

    @Test
    public void whenPaymentMethodListSetIsEmptyThenShowError() {

        presenter.initialize();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(view).setCardNumberInputMaxLength(anyInt());
        verify(view).setInvalidCardMultipleErrorView();
    }

    @Test
    public void whenPaymentMethodListSetWithTwoOptionsAndCheckFinishWithCardTokenThenAskForPaymentType() {
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnDebit());
        when(paymentPreference.getSupportedPaymentMethods(paymentMethodSearch.getPaymentMethods()))
            .thenReturn(mockedGuessedPaymentMethods);

        presenter.initialize();

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, anyString());

        assertTrue(presenter.shouldAskPaymentType(mockedGuessedPaymentMethods));
    }

    @Test
    public void whenPaymentMethodListSetWithTwoOptionsThenChooseFirstOne() {

        presenter.initialize();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        final PaymentMethod paymentMethodOnVisa = PaymentMethods.getPaymentMethodOnVisa();
        mockedGuessedPaymentMethods.add(paymentMethodOnVisa);
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnDebit());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethodOnVisa);

        assertNotNull(presenter.getPaymentMethod());
        assertEquals(presenter.getPaymentMethod().getId(), mockedGuessedPaymentMethods.get(0).getId());
    }

    @Test
    public void whenPaymentMethodSetAndDeletedThenClearConfiguration() {

        presenter.initialize();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        presenter.setPaymentMethod(null);

        assertEquals(Card.CARD_DEFAULT_SECURITY_CODE_LENGTH, presenter.getSecurityCodeLength());
        assertEquals(CardView.CARD_SIDE_BACK, presenter.getSecurityCodeLocation());
        assertTrue(presenter.isSecurityCodeRequired());
        assertEquals(0, presenter.getSavedBin().length());
    }

    @Test
    public void whenPaymentMethodSetAndDeletedThenClearViews() {
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());
        final PaymentMethod paymentMethod = mockedGuessedPaymentMethods.get(0);

        presenter.initialize();

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(view).setPaymentMethod(paymentMethod);
        verify(view).resolvePaymentMethodSet(paymentMethod);

        presenter.resolvePaymentMethodCleared();

        verify(view).clearErrorView();
        verify(view).hideRedErrorContainerView(true);
        verify(view).restoreBlackInfoContainerView();
        verify(view).clearCardNumberInputLength();
        verify(view).clearSecurityCodeEditText();
        verify(view).checkClearCardView();
    }

    @Test
    public void whenPaymentMethodSetHasIdentificationTypeRequiredThenShowIdentificationView() {
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        presenter.initialize();

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(view).initializeIdentificationTypes(identificationTypes);
    }

    @Test
    public void whenPaymentMethodSetDoNotHaveIdentificationTypeRequiredThenHideIdentificationView() {

        presenter.initialize();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodWithIdNotRequired());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_CORDIAL);

        verify(view).hideIdentificationInput();
    }

    @Test
    public void whenInitializePresenterThenStartGuessingForm() {
        presenter.initialize();

        verify(view).initializeTitle();
        verify(view).setCardNumberListeners(any(PaymentMethodGuessingController.class));
        verify(view).setCardholderNameListeners();
        verify(view).setExpiryDateListeners();
        verify(view).setSecurityCodeListeners();
        verify(view).setIdentificationTypeListeners();
        verify(view).setIdentificationNumberListeners();
        verify(view).setNextButtonListeners();
        verify(view).setBackButtonListeners();
        verify(view).setErrorContainerListener();
        verify(view).setContainerAnimationListeners();
    }

    @Test
    public void whenBankDealsNotEnabledThenHideBankDeals() {
        when(advancedConfiguration.isBankDealsEnabled()).thenReturn(false);
        presenter.initialize();
        verify(view).hideBankDeals();
    }

    @Test
    public void whenBankDealsAreEmptyThenHideBankDeals() {
        final List<BankDeal> bankDeals = new ArrayList<>();
        when(bankDealsRepository.getBankDealsAsync()).thenReturn(new StubSuccessMpCall<>(bankDeals));
        presenter.initialize();
        verify(view).hideBankDeals();
    }

    @Test
    public void whenGetPaymentMethodFailsThenHideProgress() {
        final ApiException apiException = mock(ApiException.class);
        when(groupsRepository.getGroups()).thenReturn(new StubFailMpCall<PaymentMethodSearch>(apiException));

        presenter.initialize();

        verify(view).showProgress();
        verify(view).hideProgress();
    }

    @Test
    public void whenPaymentTypeSetAndTwoPaymentMethodsThenChooseByPaymentType() {

        final List<PaymentMethod> paymentMethodList = PaymentMethods.getPaymentMethodListMLM();
        when(userSelectionRepository.getPaymentType()).thenReturn(PaymentTypes.DEBIT_CARD);

        presenter.initialize();

        final PaymentMethodGuessingController controller = new PaymentMethodGuessingController(
            paymentMethodList, PaymentTypes.DEBIT_CARD, null);

        final List<PaymentMethod> paymentMethodsWithExclusionsList =
            controller.guessPaymentMethodsByBin(Cards.MOCKED_BIN_MASTER);

        presenter.resolvePaymentMethodListSet(paymentMethodsWithExclusionsList, Cards.MOCKED_BIN_MASTER);

        when(userSelectionRepository.getPaymentMethod()).thenReturn(controller.getGuessedPaymentMethods().get(0));
        assertEquals(1, paymentMethodsWithExclusionsList.size());
        assertNotNull(presenter.getPaymentMethod());
        assertEquals("debmaster", presenter.getPaymentMethod().getId());
        assertFalse(presenter.shouldAskPaymentType(paymentMethodsWithExclusionsList));
    }

    @Test
    public void whenSecurityCodeSettingsAreWrongThenHideSecurityCodeView() {
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodWithWrongSecurityCodeSettings());
        when(userSelectionRepository.getPaymentMethod()).thenReturn(null);

        presenter.initialize();
        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(view).hideSecurityCodeInput();
    }

    @Test
    public void whenPaymentMethodSettingsAreEmptyThenShowErrorMessage() {
        presenter.initialize();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        mockedPaymentMethod.setSettings(null);
        mockedGuessedPaymentMethods.add(mockedPaymentMethod);

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(view).showSettingNotFoundForBinError();
    }

    @Test
    public void whenGetIdentificationTypesIsEmptyThenShowErrorMessage() {
        final List<IdentificationType> identificationTypes = new ArrayList<>();
        when(identificationRepository.getIdentificationTypes())
            .thenReturn(new StubSuccessMpCall<>(identificationTypes));

        when(userSelectionRepository.getPaymentMethod()).thenReturn(null);

        final List<PaymentMethod> paymentMethodList = PaymentMethods.getPaymentMethodListMLA();

        presenter.initialize();
        presenter.resolvePaymentMethodListSet(paymentMethodList, Cards.MOCKED_BIN_VISA);
        verify(view).showMissingIdentificationTypesError(anyBoolean(), anyString());
    }

    @Test
    public void whenBankDealsNotEmptyThenShowThem() {
        presenter.initialize();
        verify(view).showBankDeals();
    }

    @Test
    public void whenCardNumberSetThenValidateItAndSaveItInCardToken() {

        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnMaster();
        when(userSelectionRepository.getPaymentMethod()).thenReturn(mockedPaymentMethod);

        presenter.initialize();

        final DummyCard card = CardTestUtils.getDummyCard("master");
        assertNotNull(card);
        presenter.saveCardNumber(card.getCardNumber());
        presenter.setPaymentMethod(mockedPaymentMethod);

        final boolean valid = presenter.validateCardNumber();

        assertTrue(valid);
        assertEquals(presenter.getCardToken().getCardNumber(), card.getCardNumber());
    }

    @Test
    public void whenCardholderNameSetThenValidateItAndSaveItInCardToken() {

        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnMaster();

        presenter.initialize();

        final DummyCard card = CardTestUtils.getDummyCard("master");
        assertNotNull(card);
        presenter.saveCardNumber(card.getCardNumber());
        presenter.setPaymentMethod(mockedPaymentMethod);
        presenter.saveCardholderName(CardTestUtils.DUMMY_CARDHOLDER_NAME);

        assertTrue(presenter.validateCardName());
        assertEquals(CardTestUtils.DUMMY_CARDHOLDER_NAME, presenter.getCardToken().getCardholder().getName());
    }

    @Test
    public void whenCardExpiryDateSetThenValidateItAndSaveItInCardToken() {

        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnMaster();

        presenter.initialize();

        final DummyCard card = CardTestUtils.getDummyCard("master");
        assertNotNull(card);
        presenter.saveCardNumber(card.getCardNumber());
        presenter.setPaymentMethod(mockedPaymentMethod);
        presenter.saveCardholderName(CardTestUtils.DUMMY_CARDHOLDER_NAME);
        presenter.saveExpiryMonth(CardTestUtils.DUMMY_EXPIRY_MONTH);
        presenter.saveExpiryYear(CardTestUtils.DUMMY_EXPIRY_YEAR_SHORT);

        assertTrue(presenter.validateExpiryDate());
        assertEquals(presenter.getCardToken().getExpirationMonth(), Integer.valueOf(CardTestUtils.DUMMY_EXPIRY_MONTH));
        assertEquals(presenter.getCardToken().getExpirationYear(),
            Integer.valueOf(CardTestUtils.DUMMY_EXPIRY_YEAR_LONG));
    }

    @Test
    public void whenCardSecurityCodeSetThenValidateItAndSaveItInCardToken() {

        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnMaster();

        when(userSelectionRepository.getPaymentMethod()).thenReturn(mockedPaymentMethod);

        presenter.initialize();

        final DummyCard card = CardTestUtils.getDummyCard("master");
        assertNotNull(card);
        presenter.saveCardNumber(card.getCardNumber());
        presenter.setPaymentMethod(mockedPaymentMethod);
        presenter.saveCardholderName(CardTestUtils.DUMMY_CARDHOLDER_NAME);
        presenter.saveExpiryMonth(CardTestUtils.DUMMY_EXPIRY_MONTH);
        presenter.saveExpiryYear(CardTestUtils.DUMMY_EXPIRY_YEAR_SHORT);
        presenter.saveSecurityCode(card.getSecurityCode());
        final boolean validCardNumber = presenter.validateCardNumber();
        final boolean validSecurityCode = presenter.validateSecurityCode();

        assertTrue(validCardNumber && validSecurityCode);
        assertEquals(presenter.getCardToken().getSecurityCode(), card.getSecurityCode());
    }

    @Test
    public void whenIdentificationNumberSetThenValidateItAndSaveItInCardToken() {

        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnMaster();

        final Identification identification = new Identification();
        presenter.setIdentification(identification);

        presenter.initialize();

        final DummyCard card = CardTestUtils.getDummyCard("master");
        assertNotNull(card);
        presenter.saveCardNumber(card.getCardNumber());
        presenter.setPaymentMethod(mockedPaymentMethod);
        presenter.saveCardholderName(CardTestUtils.DUMMY_CARDHOLDER_NAME);
        presenter.saveExpiryMonth(CardTestUtils.DUMMY_EXPIRY_MONTH);
        presenter.saveExpiryYear(CardTestUtils.DUMMY_EXPIRY_YEAR_SHORT);
        presenter.saveSecurityCode(card.getSecurityCode());
        presenter.saveIdentificationNumber(CardTestUtils.DUMMY_IDENTIFICATION_NUMBER_DNI);
        presenter.saveIdentificationType(IdentificationTypes.getIdentificationType());

        assertTrue(presenter.validateIdentificationNumber());
        assertEquals(CardTestUtils.DUMMY_IDENTIFICATION_NUMBER_DNI,
            presenter.getCardToken().getCardholder().getIdentification().getNumber());
    }

    @Test
    public void whenCardDataSetAndValidThenCreateToken() {

        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnMaster();

        final Token mockedToken = Tokens.getToken();

        final Identification identification = new Identification();
        presenter.setIdentification(identification);

        when(userSelectionRepository.getPaymentMethod()).thenReturn(mockedPaymentMethod);

        presenter.initialize();

        final DummyCard card = CardTestUtils.getDummyCard("master");
        assertNotNull(card);
        presenter.saveCardNumber(card.getCardNumber());
        presenter.setPaymentMethod(mockedPaymentMethod);
        presenter.saveCardholderName(CardTestUtils.DUMMY_CARDHOLDER_NAME);
        presenter.saveExpiryMonth(CardTestUtils.DUMMY_EXPIRY_MONTH);
        presenter.saveExpiryYear(CardTestUtils.DUMMY_EXPIRY_YEAR_SHORT);
        presenter.saveSecurityCode(card.getSecurityCode());
        presenter.saveIdentificationNumber(CardTestUtils.DUMMY_IDENTIFICATION_NUMBER_DNI);
        presenter.saveIdentificationType(IdentificationTypes.getIdentificationType());

        final boolean valid =
            presenter.validateCardNumber() && presenter.validateCardName() && presenter.validateExpiryDate()
                && presenter.validateSecurityCode() && presenter.validateIdentificationNumber();

        when(issuersRepository.getIssuers(mockedPaymentMethod.getId(), presenter.getSavedBin()))
            .thenReturn(new StubSuccessMpCall<>(Issuers.getIssuersListMLA()));

        assertTrue(valid);
        presenter.checkFinishWithCardToken();
        presenter.resolveTokenRequest(mockedToken);
        assertEquals(presenter.getToken(), mockedToken);
    }

    @Test
    public void whenPaymentMethodExclusionSetAndUserSelectsItWithOnlyOnePMAvailableThenShowInfoMessage() {

        //We only have visa and master
        final List<PaymentMethod> paymentMethodList = PaymentMethods.getPaymentMethodListWithTwoOptions();
        final PaymentMethodSearch paymentMethodSearch = mock(PaymentMethodSearch.class);
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(paymentMethodSearch.getPaymentMethods()).thenReturn(paymentMethodList);

        //We exclude master
        final Collection<String> excludedPaymentMethodIds = new ArrayList<>();
        excludedPaymentMethodIds.add("master");

        when(userSelectionRepository.getPaymentType()).thenReturn(PaymentTypes.CREDIT_CARD);

        when(paymentPreference.getSupportedPaymentMethods(paymentMethodSearch.getPaymentMethods()))
            .thenReturn(Collections.singletonList(paymentMethodList.get(0)));

        presenter.initialize();

        final PaymentMethodGuessingController controller = presenter.getGuessingController();
        final List<PaymentMethod> guessedPaymentMethods = controller.guessPaymentMethodsByBin(Cards.MOCKED_BIN_MASTER);
        presenter.resolvePaymentMethodListSet(guessedPaymentMethods, Cards.MOCKED_BIN_MASTER);

        //When the user enters a master bin the container turns red
        verify(view).setInvalidCardOnePaymentMethodErrorView();

        presenter.setPaymentMethod(null);
        presenter.resolvePaymentMethodCleared();

        //When the user deletes the input the container turns black again
        verify(view).restoreBlackInfoContainerView();
    }

    @Test
    public void whenAllGuessedPaymentMethodsShareTypeThenDoNotAskForPaymentType() {
        final PaymentMethod creditCard1 = new PaymentMethod();
        creditCard1.setPaymentTypeId(PaymentTypes.CREDIT_CARD);

        final PaymentMethod creditCard2 = new PaymentMethod();
        creditCard2.setPaymentTypeId(PaymentTypes.CREDIT_CARD);

        final List<PaymentMethod> paymentMethodList = new ArrayList<>();
        paymentMethodList.add(creditCard1);
        paymentMethodList.add(creditCard2);

        final boolean shouldAskPaymentType = presenter.shouldAskPaymentType(paymentMethodList);
        assertFalse(shouldAskPaymentType);
    }

    @Test
    public void whenNotAllGuessedPaymentMethodsShareTypeThenDoAskForPaymentType() {
        final PaymentMethod creditCard = new PaymentMethod();
        creditCard.setPaymentTypeId(PaymentTypes.CREDIT_CARD);

        final PaymentMethod debitCard = new PaymentMethod();
        debitCard.setPaymentTypeId(PaymentTypes.DEBIT_CARD);

        final List<PaymentMethod> paymentMethodList = new ArrayList<>();
        paymentMethodList.add(creditCard);
        paymentMethodList.add(debitCard);

        final boolean shouldAskPaymentType = presenter.shouldAskPaymentType(paymentMethodList);
        assertTrue(shouldAskPaymentType);
    }

    @Test
    public void whenUniquePaymentMethodGuessedThenPaymentMethodShouldDefined() {

        final PaymentMethod creditCard = new PaymentMethod();
        creditCard.setPaymentTypeId(PaymentTypes.CREDIT_CARD);

        final List<PaymentMethod> paymentMethodList = new ArrayList<>();
        paymentMethodList.add(creditCard);
        assertFalse(presenter.shouldAskPaymentType(paymentMethodList));
    }

    // --------- Helper methods ----------- //

    private void whenGetBankDealsAsync() {
        final List<BankDeal> bankDeals = BankDealsUtils.getBankDealsListMLA();
        when(bankDealsRepository.getBankDealsAsync()).thenReturn(new StubSuccessMpCall<>(bankDeals));
    }

    private List<IdentificationType> whenGetIdentificationTypesAsyncWithoutAccessToken() {
        final List<IdentificationType> identificationTypes = IdentificationTypes.getIdentificationTypes();

        when(identificationRepository.getIdentificationTypes()).thenReturn(new StubSuccessMpCall<>
            (identificationTypes));
        return identificationTypes;
    }
}
