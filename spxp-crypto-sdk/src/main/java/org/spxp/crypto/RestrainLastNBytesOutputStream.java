package org.spxp.crypto;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RestrainLastNBytesOutputStream extends FilterOutputStream {
	
	private int n = 0;
	
	private byte[] ringBuffer = null;
	
	private int writePos = 0;
	
	private boolean wrappedAround = false;

	public RestrainLastNBytesOutputStream(OutputStream out, int n) {
		super(out);
		this.n = n;
		this.ringBuffer = new byte[n];
	}

	public void write(int b) throws IOException {
		if(wrappedAround) {
	        out.write(ringBuffer[writePos]);
		}
		ringBuffer[writePos] = (byte) b;
		writePos++;
		if(writePos >= n) {
			writePos = 0;
			wrappedAround = true;
		}
    }
	
	public byte[] getRestrainedBytes() {
		if(wrappedAround) {
			byte[] result = new byte[n];
			int x = n - writePos;
			System.arraycopy(ringBuffer, writePos, result, 0, x);
			System.arraycopy(ringBuffer, 0, result, x, n - x);
			return result;
		} else {
			byte[] result = new byte[writePos];
			System.arraycopy(ringBuffer, 0, result, 0, writePos);
			return result;
		}
	}

}
