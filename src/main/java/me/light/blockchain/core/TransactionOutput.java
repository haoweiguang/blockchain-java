package me.light.blockchain.core;

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
	 * 锁定脚本
	 */
	private String scriptPubKey;


	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getScriptPubKey() {
		return scriptPubKey;
	}

	public void setScriptPubKey(String scriptPubKey) {
		this.scriptPubKey = scriptPubKey;
	}

	public TransactionOutput() {
	}

	public TransactionOutput(int value, String scriptPubKey) {
		this.value = value;
		this.scriptPubKey = scriptPubKey;
	}

	/**
	 * 判断解锁数据能解锁交易输出
	 *
	 * @param unlockingData
	 * @return
	 */
	public boolean canUnlockOutputWith(String unlockingData) {
		return this.getScriptPubKey().endsWith(unlockingData);
	}
}
