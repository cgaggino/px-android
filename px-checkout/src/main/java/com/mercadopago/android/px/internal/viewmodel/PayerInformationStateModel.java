package com.mercadopago.android.px.internal.viewmodel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.IdentificationType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class PayerInformationStateModel {

    public static final String IDENTIFICATION_NUMBER_BUNDLE = "identificationNumber";
    public static final String IDENTIFICATION_NAME_BUNDLE = "identificationName";
    public static final String IDENTIFICATION_LAST_NAME_BUNDLE = "identificationLastName";
    public static final String IDENTIFICATION_BUNDLE = "identification";
    public static final String IDENTIFICATION_TYPE_BUNDLE = "identificationType";
    public static final String IDENTIFICATION_TYPES_LIST_BUNDLE = "identificationTypeList";

    public String identificationNumber;
    public String identificationName;
    public String identificationLastName;
    public Identification identification;
    public IdentificationType identificationType;
    public List<IdentificationType> identificationTypeList;

    public void toBundle(@NonNull final Bundle bundle) {
        bundle.putString(IDENTIFICATION_NUMBER_BUNDLE, identificationNumber);
        bundle.putString(IDENTIFICATION_NAME_BUNDLE, identificationName);
        bundle.putString(IDENTIFICATION_LAST_NAME_BUNDLE, identificationLastName);
        bundle.putString(IDENTIFICATION_BUNDLE, JsonUtil.getInstance().toJson(identification));
        bundle
            .putString(IDENTIFICATION_TYPE_BUNDLE, JsonUtil.getInstance().toJson(identificationType));
        bundle.putString(IDENTIFICATION_TYPES_LIST_BUNDLE,
            JsonUtil.getInstance().toJson(identificationTypeList));
    }

    public static PayerInformationStateModel fromBundle(@NonNull final Bundle bundle) {
        final PayerInformationStateModel stateModel = new PayerInformationStateModel();
        stateModel.identificationNumber = bundle.getString(IDENTIFICATION_NUMBER_BUNDLE);
        stateModel.identificationName = bundle.getString(IDENTIFICATION_NAME_BUNDLE);
        stateModel.identificationLastName = bundle.getString(IDENTIFICATION_LAST_NAME_BUNDLE);
        stateModel.identification = JsonUtil.getInstance()
            .fromJson(bundle.getString(IDENTIFICATION_BUNDLE), Identification.class);
        stateModel.identificationType = JsonUtil.getInstance()
            .fromJson(bundle.getString(IDENTIFICATION_TYPE_BUNDLE), IdentificationType.class);
        try {
            Type listType = new TypeToken<List<IdentificationType>>() {
            }.getType();
            stateModel.identificationTypeList = JsonUtil.getInstance().getGson().fromJson(
                bundle.getString(IDENTIFICATION_TYPES_LIST_BUNDLE), listType);
        } catch (Exception ex) {
            stateModel.identificationTypeList = new ArrayList<>();
        }
        return stateModel;
    }
}
