package com.rolandoamarillo.otp;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Main {
	Timer timer;
	int count;

	public static void main(String[] paramArrayOfString) throws IOException {
		System.out.println("\nAuthenticator Started!");
		System.out.println(":----------------------------:--------:");
		System.out.println(":       Code Wait Time       :  Code  :");
		System.out.println(":----------------------------:--------:");
		Main localMain = new Main();
		localMain.reminder("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");
	}

	public void reminder(String paramString) {
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new TimedPin(paramString), 0L, 1000L);
	}

	public Main() {
		this.count = 1;
	}

	class TimedPin extends TimerTask {
		private String secret;

		public TimedPin(String paramString) {
			this.secret = paramString;
		}

		String previouscode = "";

		public void run() {
			String str = Main.computePin(this.secret, null);
			if (this.previouscode.equals(str)) {
				System.out.print(".");
			} else {
				if (Main.this.count <= 30) {
					for (int i = Main.this.count + 1; i <= 30; i++) {
						System.out.print("+");
					}
				}
				System.out.println(": " + str + " :");
				Main.this.count = 0;
			}
			this.previouscode = str;
			Main.this.count += 1;
		}
	}

	public static String computePin(String paramString, Long paramLong) {
		if ((paramString == null) || (paramString.length() == 0)) {
			return "Null or empty secret";
		}
		try {
			byte[] arrayOfByte = Base32String.decode(paramString);
			Mac localMac = Mac.getInstance("HMACSHA1");
			localMac.init(new SecretKeySpec(arrayOfByte, ""));
			PasscodeGenerator localPasscodeGenerator = new PasscodeGenerator(localMac);
			return localPasscodeGenerator.generateTimeoutCode();
		} catch (GeneralSecurityException localGeneralSecurityException) {
			return "General security exception";
		} catch (Base32String.DecodingException localDecodingException) {
		}
		return "Decoding exception";
	}
}
