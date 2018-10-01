package me.light.blockchain.core;

import me.light.blockchain.util.BitcoinAddressUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * 交易输入
 *
 * @author light.hao
 * @create 2018-09-25-15:55
 */
public class TransactionInput {

	/**
	 * 交易id的hash值
	 */
	private byte[] transactionId;

	/**
	 * 交易输出索引
	 */
	private int transactionOutputIndex;

	/**
	 * 签名
	 */
	private byte[] signature;

	/**
	 * 公钥
	 */
	private byte[] publicKey;


	public byte[] getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(byte[] transactionId) {
		this.transactionId = transactionId;
	}

	public int getTransactionOutputIndex() {
		return transactionOutputIndex;
	}

	public void setTransactionOutputIndex(int transactionOutputIndex) {
		this.transactionOutputIndex = transactionOutputIndex;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	public TransactionInput() {
	}

	public TransactionInput(byte[] transactionId, int transactionOutputIndex, byte[] signature, byte[] publicKey) {
		this.transactionId = transactionId;
		this.transactionOutputIndex = transactionOutputIndex;
		this.signature = signature;
		this.publicKey = publicKey;
	}

	/**
	 * 检查公钥hash是否用于交易输入
	 *
	 * @param publicKeyHash
	 * @return
	 */
	public boolean usesKey(byte[] publicKeyHash) {
		byte[] lockingHash = BitcoinAddressUtils.ripeMD160Hash(this.getPublicKey());
		return Arrays.equals(lockingHash, publicKeyHash);
	}
}
