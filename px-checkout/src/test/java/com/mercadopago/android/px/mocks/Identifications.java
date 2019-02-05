package com.mercadopago.android.px.mocks;

import com.mercadopago.android.px.model.Identification;

public class Identifications {
    public static Identification getIdentificationCPF() {
        String type = "CPF";
        String identificationNumber = "89898989898";

        Identification identification = new Identification();
        identification.setNumber(identificationNumber);
        identification.setType(type);

        return identification;
    }

    public static Identification getIdentificationWithWrongNumberCPF() {
        String type = "CPF";
        String identificationNumber = "";

        Identification identification = new Identification();
        identification.setNumber(identificationNumber);
        identification.setType(type);

        return identification;
    }
}
