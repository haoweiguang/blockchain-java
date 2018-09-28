package me.light.blockchain.util;


import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jcajce.provider.digest.RIPEMD160;

import java.util.Arrays;

/**
 * 比特币地址工具类
 *
 * @author light.hao
 * @create 2018-09-28-23:02
 */
public class BitcoinAddressUtils {

	/**
	 * 双hash
	 *
	 * @param data
	 * @return
	 */
	public static byte[] doubleHash(byte[] data) {
		return DigestUtils.sha256(DigestUtils.sha256(data));
	}

	/**
	 * 计算公钥的 RIPEMD160 Hash值
	 *
	 * @param publicKey 公钥
	 * @return
	 */
	public static byte[] ripeMD160Hash(byte[] publicKey) {
		//1. 先对公钥做 sha256 处理
		byte[] publicKeyOfsha256 = DigestUtils.sha256(publicKey);
		RIPEMD160Digest digest = new RIPEMD160Digest();
		digest.update(publicKeyOfsha256, 0, publicKeyOfsha256.length);
		byte[] output = new byte[digest.getDigestSize()];
		digest.doFinal(output, 0);
		return output;
	}

	/**
	 * 生成公钥的校检码
	 *
	 * @param payload
	 * @return
	 */
	public static byte[] checksum(byte[] payload) {
		return Arrays.copyOfRange(doubleHash(payload), 0, 4);
	}

}
