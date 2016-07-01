package org.solmix.datax.jdbc.dialect;

import org.solmix.datax.jdbc.sql.SQLGenerationException;

public class ColumnNotFondException extends SQLGenerationException {

	private static final long serialVersionUID = -683399825829228086L;

		public ColumnNotFondException(String string, Throwable e)
	    {
	        super(string, e);
	    }

	    public ColumnNotFondException(String string)
	    {
	        super(string);
	    }
}
