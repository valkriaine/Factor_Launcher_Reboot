package com.factor.chips.chipslayoutmanager.anchor;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * represents View, which is highest visible left view
 */

public class AnchorViewState implements Parcelable {
    private Integer position = 0;
    private Rect anchorViewRect;

    private AnchorViewState() {
    }

    static AnchorViewState getNotFoundState() {
        return new AnchorViewState();
    }

    AnchorViewState(int position, @NonNull Rect anchorViewRect) {
        this.position = position;
        this.anchorViewRect = anchorViewRect;
    }

    public boolean isNotFoundState() {
        return anchorViewRect == null;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Rect getAnchorViewRect() {
        return anchorViewRect;
    }

    public void setAnchorViewRect(Rect anchorViewRect) {
        this.anchorViewRect = anchorViewRect;
    }

    public boolean isRemoving() {
        return getPosition() == -1;
    }

    //parcelable logic below

    private AnchorViewState(Parcel parcel) {
        int parcelPosition = parcel.readInt();
        position = parcelPosition == -1? null : parcelPosition;
        anchorViewRect = parcel.readParcelable(AnchorViewState.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(position == null? -1 : position);
        parcel.writeParcelable(anchorViewRect, 0);
    }

    public static final Creator<AnchorViewState> CREATOR = new Creator<AnchorViewState>() {
        // unpack Object from Parcel
        public AnchorViewState createFromParcel(Parcel in) {
            return new AnchorViewState(in);
        }

        public AnchorViewState[] newArray(int size) {
            return new AnchorViewState[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "AnchorState. Position = %d, Rect = %s", position, anchorViewRect);
    }
}
