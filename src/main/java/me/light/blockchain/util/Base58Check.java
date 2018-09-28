package me.light.blockchain.util;

import java.math.BigInteger;

/**
 * Base58转换工具
 *
 * @author light.hao
 * @create 2018-09-28-23:13
 */
public class Base58Check {


	private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	private static final BigInteger ALPHABET_SIZE = BigInteger.valueOf(ALPHABET.length());

	/**
	 * 转化为 Base58 字符串
	 *
	 * @param data
	 * @return
	 */
	public static String rawBytesToBase58(byte[] data) {
		// Convert to base-58 string
		StringBuilder sb = new StringBuilder();
		BigInteger num = new BigInteger(1, data);
		while (num.signum() != 0) {
			BigInteger[] quotrem = num.divideAndRemainder(ALPHABET_SIZE);
			sb.append(ALPHABET.charAt(quotrem[1].intValue()));
			num = quotrem[0];
		}

		// Add '1' characters for leading 0-value bytes
		for (int i = 0; i < data.length && data[i] == 0; i++) {
			sb.append(ALPHABET.charAt(0));
		}
		return sb.reverse().toString();
	}
}
