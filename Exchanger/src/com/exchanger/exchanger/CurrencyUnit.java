package com.exchanger.exchanger;

import android.os.Parcel;
import android.os.Parcelable;

public final class CurrencyUnit implements Parcelable 
{
	private String currency;
	private float rate;
	
	public final String getCurrency()
	{
		return this.currency;
	}
	
	public final float getRate()
	{
		return this.rate;
	}
	
	public CurrencyUnit(final String currency, final float rate) 
	{
		super();
		this.currency = currency;
		this.rate = rate;
	}

	private CurrencyUnit(Parcel in)
	{
		this.currency = in.readString();
		this.rate = in.readFloat();
	}
	
	@Override
	public int describeContents() 
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) 
	{
		arg0.writeString(this.currency);
		arg0.writeFloat(this.rate);
	}
	
	public static final Parcelable.Creator<CurrencyUnit> CREATOR = 
			new Creator<CurrencyUnit>() {
		
		@Override
		public CurrencyUnit[] newArray(int size) {
			return new CurrencyUnit[size];
		}
		
		@Override
		public CurrencyUnit createFromParcel(Parcel parcel) {
			return new CurrencyUnit(parcel);
		}
	};
}
