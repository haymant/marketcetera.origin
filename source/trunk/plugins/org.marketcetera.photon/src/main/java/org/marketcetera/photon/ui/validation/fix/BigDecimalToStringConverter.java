package org.marketcetera.photon.ui.validation.fix;

import java.math.BigDecimal;
import java.text.NumberFormat;

import org.eclipse.core.databinding.conversion.Converter;

public class BigDecimalToStringConverter extends Converter {

	private NumberFormat numberFormat;

	public BigDecimalToStringConverter(){
		// todo: Shouldn't we call super(BigDecimal.class, String.class);
		this(NumberFormat.getNumberInstance());
	}
	
	public BigDecimalToStringConverter(NumberFormat numberFormat) {
		super(BigDecimal.class, String.class);
		
		this.numberFormat = numberFormat;
	}
	
	public Object convert(Object fromObject) {
		if (fromObject == null){
			return null;
		}
		Number number = (BigDecimal) fromObject;
		String result = null;
		synchronized (numberFormat) {
			result = numberFormat.format(number);
		}

		return result;
	}

}
