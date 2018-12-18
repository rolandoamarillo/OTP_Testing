package com.rolandoamarillo.otp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import javax.crypto.Mac;

public class PasscodeGenerator {
	private static final int PASS_CODE_LENGTH = 6;
	private static final int INTERVAL = 30;
	private static final int ADJACENT_INTERVALS = 1;
	private static final int PIN_MODULO = (int) Math.pow(10.0D, 6.0D);
	private final Signer signer;
	private final int codeLength;
	private final int intervalPeriod;

	public PasscodeGenerator(Mac paramMac) {
		this(paramMac, PASS_CODE_LENGTH, INTERVAL);
	}

	public PasscodeGenerator(Mac paramMac, int codeLength, int intervalPeriod) {
		this(new Signer() {
			public byte[] sign(byte[] paramAnonymousArrayOfByte) {
				return paramMac.doFinal(paramAnonymousArrayOfByte);
			}
		}, codeLength, intervalPeriod);
	}

	public PasscodeGenerator(Signer signer, int codeLength, int intervalPeriod) {
		this.signer = signer;
		this.codeLength = codeLength;
		this.intervalPeriod = intervalPeriod;
	}

	private String padOutput(int paramInt) {
		String str = Integer.toString(paramInt);
		for (int i = str.length(); i < this.codeLength; i++) {
			str = "0" + str;
		}
		return str;
	}

	public String generateTimeoutCode() throws GeneralSecurityException {
		return generateResponseCode(this.clock.getCurrentInterval());
	}

	public String generateResponseCode(long currentInterval) throws GeneralSecurityException {
		byte[] arrayOfByte = ByteBuffer.allocate(8).putLong(currentInterval).array();
		return generateResponseCode(arrayOfByte);
	}

	public String generateResponseCode(byte[] paramArrayOfByte) throws GeneralSecurityException {
		byte[] arrayOfByte = this.signer.sign(paramArrayOfByte);

		int i = arrayOfByte[(arrayOfByte.length - 1)] & 0xF;

		int j = hashToInt(arrayOfByte, i) & 0x7FFFFFFF;
		int k = j % PIN_MODULO;
		return padOutput(k);
	}

	private int hashToInt(byte[] paramArrayOfByte, int paramInt) {
		DataInputStream localDataInputStream = new DataInputStream(
				new ByteArrayInputStream(paramArrayOfByte, paramInt, paramArrayOfByte.length - paramInt));
		int i;
		try {
			i = localDataInputStream.readInt();
		} catch (IOException localIOException) {
			throw new IllegalStateException(localIOException);
		}
		return i;
	}

	public boolean verifyResponseCode(long paramLong, String paramString) throws GeneralSecurityException {
		String str = generateResponseCode(paramLong);
		return str.equals(paramString);
	}

	public boolean verifyTimeoutCode(String paramString) throws GeneralSecurityException {
		return verifyTimeoutCode(paramString, 1, 1);
	}

	public boolean verifyTimeoutCode(String paramString, int paramInt1, int paramInt2) throws GeneralSecurityException {
		long l = this.clock.getCurrentInterval();
		String str1 = generateResponseCode(l);
		if (str1.equals(paramString)) {
			return true;
		}
		String str2;
		for (int i = 1; i <= paramInt1; i++) {
			str2 = generateResponseCode(l - i);
			if (str2.equals(paramString)) {
				return true;
			}
		}
		for (int i = 1; i <= paramInt2; i++) {
			str2 = generateResponseCode(l + i);
			if (str2.equals(paramString)) {
				return true;
			}
		}
		return false;
	}

	private IntervalClock clock = new IntervalClock() {
		public long getCurrentInterval() {
			long l = System.currentTimeMillis() / 1000L;
			return l / getIntervalPeriod();
		}

		public int getIntervalPeriod() {
			return PasscodeGenerator.this.intervalPeriod;
		}
	};

	interface IntervalClock {
		public abstract int getIntervalPeriod();

		public abstract long getCurrentInterval();
	}

	interface Signer {
		public abstract byte[] sign(byte[] paramArrayOfByte) throws GeneralSecurityException;
	}
}
