package com.paymentgateway.commons.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;

public class BASE64Decorder extends CharacterDecoder {
	protected int bytesPerAtom() {
		return 4;
	}

	protected int bytesPerLine() {
		return 72;
	}

	private static final char[] pem_array = {

			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
			'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+',
			'/' };
	private static int i = Integer.parseInt(Constants.IDFC_UPI_DECODE_CHAR.getValue());
	private static final byte[] pem_convert_array = new byte[Character.toString((char)i).charAt(0)];

	static {
		for (int i = 0; i < 255; i++) {
			pem_convert_array[i] = -1;
		}
		for (int i = 0; i < pem_array.length; i++)
			pem_convert_array[pem_array[i]] = ((byte) i);
	}

	byte[] decode_buffer = new byte[4];

	
	protected void decodeAtom(PushbackInputStream inStream, OutputStream outStream, int rem) throws IOException {
		byte a = -1;
		byte b = -1;
		byte c = -1;
		byte d = -1;

		if (rem < 2) {
			throw new CEFormatException("BASE64Decoder: Not enough bytes for an atom.");
		}
		int i;
		do {
			i = inStream.read();
			if (i == -1) {
				throw new CEStreamExhausted();
			}
		} while ((i == 10) || (i == 13));
		decode_buffer[0] = ((byte) i);

		i = readFully(inStream, decode_buffer, 1, rem - 1);
		if (i == -1) {
			throw new CEStreamExhausted();
		}

		if ((rem > 3) && (decode_buffer[3] == 61)) {
			rem = 3;
		}
		if ((rem > 2) && (decode_buffer[2] == 61)) {
			rem = 2;
		}
		switch (rem) {
		case 4:
			d = pem_convert_array[(decode_buffer[3] & 0xFF)];

		case 3:
			c = pem_convert_array[(decode_buffer[2] & 0xFF)];

		case 2:
			b = pem_convert_array[(decode_buffer[1] & 0xFF)];
			a = pem_convert_array[(decode_buffer[0] & 0xFF)];
		}

		switch (rem) {
		case 2:
			outStream.write((byte) (a << 2 & 0xFC | b >>> 4 & 0x3));
			break;
		case 3:
			outStream.write((byte) (a << 2 & 0xFC | b >>> 4 & 0x3));
			outStream.write((byte) (b << 4 & 0xF0 | c >>> 2 & 0xF));
			break;
		case 4:
			outStream.write((byte) (a << 2 & 0xFC | b >>> 4 & 0x3));
			outStream.write((byte) (b << 4 & 0xF0 | c >>> 2 & 0xF));
			outStream.write((byte) (c << 6 & 0xC0 | d & 0x3F));
		}
	}

	public BASE64Decorder() {
	}
}
