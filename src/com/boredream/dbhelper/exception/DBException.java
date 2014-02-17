package com.boredream.dbhelper.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

public class DBException extends Exception {

	private static final long serialVersionUID = 1L;

	@Override
	public void printStackTrace() {
		super.printStackTrace();
	}

	@Override
	public void printStackTrace(PrintStream err) {
		super.printStackTrace(err);
	}

	@Override
	public void printStackTrace(PrintWriter err) {
		super.printStackTrace(err);
	}

}
