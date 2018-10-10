package me.light.blockchain.core;

import me.light.blockchain.util.Base58Check;

import java.util.Arrays;

/**
 * 交易输出
 *
 * @author light.hao
 * @create 2018-09-25-16:03
 */
public class TransactionOutput {

	/**
	 * 数值
	 */
	private int value;

	/**
	 * 公钥hash
	 */
	private byte[] publicKeyHash;


	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public byte[] getPublicKeyHash() {
		return publicKeyHash;
	}

	public void setPublicKeyHash(byte[] publicKeyHash) {
		this.publicKeyHash = publicKeyHash;
	}

	public TransactionOutput() {
	}

	public TransactionOutput(int value, byte[] publicKeyHash) {
		this.value = value;
		this.publicKeyHash = publicKeyHash;
	}

	/**
	 * 创建新的交易输出
	 *
	 * @param value
	 * @param address
	 * @return
	 */
	public static TransactionOutput newTransactionOutput(int value, String address) {
		byte[] versionedPayload = Base58Check.base58ToRawBytes(address);
		byte[] publicKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
		return new TransactionOutput(value, publicKeyHash);
	}

	/**
	 * 检查交易输出是否能够使用指定的公钥
	 *
	 * @param publicKeyHash
	 * @return
	 */
	public boolean isLockedWithKey(byte[] publicKeyHash) {
		return Arrays.equals(this.getPublicKeyHash(), publicKeyHash);
	}


}
