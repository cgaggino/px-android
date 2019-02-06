package com.mercadopago.android.px.mocks;

import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.Identification;

public final class IdentificationUtils {

    public static final String IDENTIFICATION_TYPE_CPF = "CPF";
    public static final String IDENTIFICATION_NUMBER = "89898989898";

    private IdentificationUtils() {
    }

    public static Identification getIdentificationCPF() {
        String type = IDENTIFICATION_TYPE_CPF;
        String identificationNumber = IDENTIFICATION_NUMBER;

        Identification identification = new Identification();
        identification.setNumber(identificationNumber);
        identification.setType(type);

        return identification;
    }

    public static Identification getIdentificationWithWrongNumberCPF() {
        String type = IDENTIFICATION_TYPE_CPF;
        String identificationNumber = TextUtil.EMPTY;

        Identification identification = new Identification();
        identification.setNumber(identificationNumber);
        identification.setType(type);

        return identification;
    }
}
