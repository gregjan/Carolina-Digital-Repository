package edu.unc.lib.dl.update;

public class UIPException extends Exception {
	private static final long serialVersionUID = -7491079063778888042L;

	public UIPException(final String msg) {
		super(msg);
	}

	public UIPException(final String msg, final Throwable e) {
		super(msg, e);
	}
}