package me.light.blockchain.core;

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
	 * 解锁脚本
	 */
	private String scriptSig;

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

	public String getScriptSig() {
		return scriptSig;
	}

	public void setScriptSig(String scriptSig) {
		this.scriptSig = scriptSig;
	}


	public TransactionInput(byte[] transactionId, int transactionOutputIndex, String scriptSig) {
		this.transactionId = transactionId;
		this.transactionOutputIndex = transactionOutputIndex;
		this.scriptSig = scriptSig;
	}

	/**
	 * 判断解锁数据能解锁交易输入
	 * @param unlockingData
	 * @return
	 */
	public boolean canUnlockInputWith(String unlockingData) {
		return this.getScriptSig().endsWith(unlockingData);
	}

}
