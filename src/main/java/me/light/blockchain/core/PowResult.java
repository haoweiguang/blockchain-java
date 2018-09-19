package me.light.blockchain.core;

/**
 * pow计算结果
 *
 * @author light.hao
 * @create 2018-08-08-16:32
 */
public class PowResult {

	/**
	 * 计数器
	 */
	private long nonce;

	/**
	 * hash值
	 */
	private String hash;

	public PowResult(long nonce, String hash) {
		this.nonce = nonce;
		this.hash = hash;
	}

	public long getNonce() {
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}
