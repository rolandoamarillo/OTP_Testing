package com.rolandoamarillo.otp;

import java.util.HashMap;

public class Base32String {
	private static final Base32String INSTANCE = new Base32String("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567");
	private String ALPHABET;
	private char[] DIGITS;
	private int MASK;
	private int SHIFT;
	private HashMap<Character, Integer> CHAR_MAP;
	static final String SEPARATOR = "-";

	static Base32String getInstance() {
		return INSTANCE;
	}

	protected Base32String(String paramString) {
		this.ALPHABET = paramString;
		this.DIGITS = this.ALPHABET.toCharArray();
		this.MASK = (this.DIGITS.length - 1);
		this.SHIFT = Integer.numberOfTrailingZeros(this.DIGITS.length);
		this.CHAR_MAP = new HashMap<>();
		for (int i = 0; i < this.DIGITS.length; i++) {
			this.CHAR_MAP.put(Character.valueOf(this.DIGITS[i]), Integer.valueOf(i));
		}
	}

	public static byte[] decode(String paramString) throws Base32String.DecodingException {
		return getInstance().decodeInternal(paramString);
	}

	protected byte[] decodeInternal(String paramString) throws Base32String.DecodingException {
		paramString = paramString.trim().replaceAll("-", "").replaceAll(" ", "");

		paramString = paramString.toUpperCase();
		if (paramString.length() == 0) {
			return new byte[0];
		}
		int i = paramString.length();
		int j = i * this.SHIFT / 8;
		byte[] arrayOfByte = new byte[j];
		int k = 0;
		int m = 0;
		int n = 0;
		for (char c : paramString.toCharArray()) {
			if (!this.CHAR_MAP.containsKey(Character.valueOf(c))) {
				throw new DecodingException("Illegal character: " + c);
			}
			k <<= this.SHIFT;
			k |= ((Integer) this.CHAR_MAP.get(Character.valueOf(c))).intValue() & this.MASK;
			n += this.SHIFT;
			if (n >= 8) {
				arrayOfByte[(m++)] = ((byte) (k >> n - 8));
				n -= 8;
			}
		}
		return arrayOfByte;
	}

	public static String encode(byte[] paramArrayOfByte) {
		return getInstance().encodeInternal(paramArrayOfByte);
	}

	protected String encodeInternal(byte[] paramArrayOfByte) {
		if (paramArrayOfByte.length == 0) {
			return "";
		}
		if (paramArrayOfByte.length >= 268435456) {
			throw new IllegalArgumentException();
		}
		int i = (paramArrayOfByte.length * 8 + this.SHIFT - 1) / this.SHIFT;
		StringBuilder localStringBuilder = new StringBuilder(i);

		int j = paramArrayOfByte[0];
		int k = 1;
		int m = 8;
		while ((m > 0) || (k < paramArrayOfByte.length)) {
			if (m < this.SHIFT) {
				if (k < paramArrayOfByte.length) {
					j <<= 8;
					j |= paramArrayOfByte[(k++)] & 0xFF;
					m += 8;
				} else {
					int n = this.SHIFT - m;
					j <<= n;
					m += n;
				}
			}
			int n = this.MASK & j >> m - this.SHIFT;
			m -= this.SHIFT;
			localStringBuilder.append(this.DIGITS[n]);
		}
		return localStringBuilder.toString();
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	static class DecodingException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DecodingException(String paramString) {
			super();
		}
	}
}
