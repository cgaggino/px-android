package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

public class Identification implements Serializable, Parcelable {

    private String number;
    private String type;

    public Identification() {
    }

    protected Identification(final Parcel in) {
        number = in.readString();
        type = in.readString();
    }

    public static final Creator<Identification> CREATOR = new Creator<Identification>() {
        @Override
        public Identification createFromParcel(Parcel in) {
            return new Identification(in);
        }

        @Override
        public Identification[] newArray(int size) {
            return new Identification[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(number);
        dest.writeString(type);
    }
}
