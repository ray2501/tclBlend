/*
 * TdbcHelper.java --
 *
 *	Implements a helper class to support retrieving
 *	result sets in tdbc::jdbc
 *
 * Copyright (c) 2020 chw at ch minus werner dot de
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package tcl.lang;

import java.util.Vector;
import java.sql.Types;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class TdbcHelper {

    private ResultSet rs;

    public TdbcHelper(ResultSet rs) {
	this.rs = rs;
    }

    public Vector getColumns() throws SQLException {
	ResultSetMetaData md = rs.getMetaData();
	int i, ncols = md.getColumnCount();
	Vector ret = new Vector(ncols);
	for (i = 1; i <= ncols; i++) {
	    ret.addElement(md.getColumnLabel(i));
	}
	return ret;
    }

    public Vector getRowData() throws SQLException {
	ResultSetMetaData md = rs.getMetaData();
	int i, ncols = md.getColumnCount();
	Vector ret = new Vector(ncols);
	for (i = 1; i <= ncols; i++) {
	    String str = null;
	    switch (md.getColumnType(i)) {
	    case Types.BLOB:
	    case Types.BINARY:
	    case Types.VARBINARY:
	    case Types.LONGVARBINARY:
		byte[] b = rs.getBytes(i);
		if (b != null) {
		    try {
			str = new String(b, "UTF-8");
		    } catch(Exception e) {
		    }
		}
		break;
	    default:
		str = rs.getString(i);
		break;
	    }
	    if (str == null || rs.wasNull()) {
		str = "";
	    }
	    ret.addElement(str);
	}
	return ret;
    }

}
