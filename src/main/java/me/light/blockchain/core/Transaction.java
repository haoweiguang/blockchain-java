package me.light.blockchain.core;


import me.light.blockchain.util.SerializeUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 交易
 *
 * @author light.hao
 * @create 2018-09-25-16:05
 */
public class Transaction {

	private static final int SUBSIDY = 10;

	/**
	 * 交易id的hash值
	 */
	private byte[] transactionId;

	/**
	 * 交易输入
	 */
	private TransactionInput[] inputs;

	/**
	 * 交易输出
	 */
	private TransactionOutput[] outputs;


	public byte[] getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(byte[] transactionId) {
		this.transactionId = transactionId;
	}

	public TransactionInput[] getInputs() {
		return inputs;
	}

	public void setInputs(TransactionInput[] inputs) {
		this.inputs = inputs;
	}

	public TransactionOutput[] getOutputs() {
		return outputs;
	}

	public void setOutputs(TransactionOutput[] outputs) {
		this.outputs = outputs;
	}

	public Transaction(byte[] transactionId, TransactionInput[] inputs, TransactionOutput[] outputs) {
		this.transactionId = transactionId;
		this.inputs = inputs;
		this.outputs = outputs;
	}

	private void setTransactionId() {
		this.setTransactionId(DigestUtils.sha256(SerializeUtils.serialize(this)));
	}

	/**
	 * 创建coinbase交易
	 *
	 * @param data 解锁脚本数据
	 * @param to   收账的钱包地址
	 * @return
	 */
	public static Transaction newCoinbaseTransaction(String data, String to) {

		if (StringUtils.isBlank(data)) {
			data = String.format("Reward to '%s'", to);
		}

		//创建交易输入
		TransactionInput input = new TransactionInput(new byte[]{}, -1, data);

		//创建交易输出
		TransactionOutput output = new TransactionOutput(SUBSIDY, to);

		//创建交易
		Transaction transaction = new Transaction(null, new TransactionInput[]{input}, new TransactionOutput[]{output});

		//设置交易ID
		transaction.setTransactionId();

		return transaction;
	}

	/**
	 * 判断是不是coinbase交易
	 *
	 * @return
	 */
	public boolean isCoinBase() {
		return this.getInputs().length == 1
				&& this.getInputs()[0].getTransactionId().length == 0
				&& this.getInputs()[0].getTransactionOutputIndex() == -1;
	}


}
