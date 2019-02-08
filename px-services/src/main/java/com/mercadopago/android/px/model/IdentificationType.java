package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;

public class IdentificationType implements Parcelable {

    private String id;
    private String name;
    private String type;
    private Integer minLength;
    private Integer maxLength;

    public IdentificationType() {
    }

    public IdentificationType(String id, String name, String type,
        Integer minLength, Integer maxLength) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    protected IdentificationType(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readString();
        if (in.readByte() == 0) {
            minLength = null;
        } else {
            minLength = in.readInt();
        }
        if (in.readByte() == 0) {
            maxLength = null;
        } else {
            maxLength = in.readInt();
        }
    }

    public static final Creator<IdentificationType> CREATOR = new Creator<IdentificationType>() {
        @Override
        public IdentificationType createFromParcel(Parcel in) {
            return new IdentificationType(in);
        }

        @Override
        public IdentificationType[] newArray(int size) {
            return new IdentificationType[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String Id) {
        id = Id;
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        name = Name;
    }

    public String getType() {
        return type;
    }

    public void setType(String Type) {
        type = Type;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(type);
        if (minLength == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(minLength);
        }
        if (maxLength == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(maxLength);
        }
    }
}
