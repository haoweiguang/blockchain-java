package me.light.blockchain.core;


import me.light.blockchain.util.BitcoinAddressUtils;
import me.light.blockchain.util.SerializeUtils;
import me.light.blockchain.util.WalletUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.security.Signature;
import java.util.Iterator;
import java.util.Map;

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


	public Transaction() {
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
	public static Transaction newCoinbaseTransaction(String to, String data) {

		if (StringUtils.isBlank(data)) {
			data = String.format("Reward to '%s'", to);
		}

		//创建交易输入
		TransactionInput input = new TransactionInput(new byte[]{}, -1, null, data.getBytes());

		//创建交易输出
		TransactionOutput output = TransactionOutput.newTransactionOutput(SUBSIDY, to);

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


	/**
	 * 创建一笔交易
	 *
	 * @param from       支付地址
	 * @param to         收款地址
	 * @param amount     交易金额
	 * @param blockchain 区块链
	 * @return
	 */
	public static Transaction newTransaction(String from, String to, int amount, Blockchain blockchain) throws Exception {

		//获取钱包
		Wallet wallet = WalletUtils.getInstance().getWallet(from);
		byte[] publicKey = wallet.getPublicKey();
		byte[] publicKeyHash = BitcoinAddressUtils.ripeMD160Hash(publicKey);

		SpendableOutputResult outputResult = blockchain.findSpendableOutputs(publicKeyHash, amount);
		int accumulated = outputResult.getAccumulated();
		Map<String, int[]> unspentOutputs = outputResult.getUnspentOutputs();

		if (accumulated < amount) {
			throw new Exception("ERROR: Not enough funds");
		}

		Iterator<Map.Entry<String, int[]>> iterator = unspentOutputs.entrySet().iterator();

		TransactionInput[] inputs = {};
		while (iterator.hasNext()) {
			Map.Entry<String, int[]> entry = iterator.next();
			String transactionIdStr = entry.getKey();
			int[] outputIds = entry.getValue();
			byte[] transactionId = Hex.decodeHex(transactionIdStr.toCharArray());
			for (int outputIndex : outputIds) {
				inputs = ArrayUtils.add(inputs, new TransactionInput(transactionId, outputIndex, null, publicKeyHash));
			}
		}

		TransactionOutput[] outputs = {};
		outputs = ArrayUtils.add(outputs, TransactionOutput.newTransactionOutput(amount, to));
		if (accumulated > amount) {
			outputs = ArrayUtils.add(outputs, TransactionOutput.newTransactionOutput((accumulated - amount), from));
		}

		Transaction transaction = new Transaction(null, inputs, outputs);
		transaction.setTransactionId();

		return transaction;
	}


	/**
	 * 签名交易
	 *
	 * @param privateKey   私钥
	 * @param transactions 交易集合
	 */
	public void sign(BCECPrivateKey privateKey, Map<String, Transaction> transactions) throws Exception {
		//coinbase交易不需要签名
		if (this.isCoinBase()) {
			return;
		}

		//再次验证交易信息中的交易输入是否正确，也就是能否查找对应的交易数据
		for (TransactionInput input : this.getInputs()) {
			if (transactions.get(Hex.encodeHexString(input.getTransactionId())) == null) {
				throw new Exception("ERROR: Previous transaction is not correct");
			}
		}

		//创建用于签名的交易数据副本
		Transaction transaction4Sign = this.trimmedCopy();

		Security.addProvider(new BouncyCastleProvider());
		Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
		ecdsaSign.initSign(privateKey);

		for (int i = 0; i < transaction4Sign.getInputs().length; i++) {
			TransactionInput input = transaction4Sign.getInputs()[i];
			//获取交易输入TransactionId对应的交易数据
			Transaction transaction = transactions.get(Hex.encodeHexString(input.getTransactionId()));
			//获取交易输入所对应的上一笔交易中的交易输出
		}


	}


	/**
	 * 创建用于签名的交易数据副本
	 *
	 * @return
	 */
	private Transaction trimmedCopy() {
		TransactionInput[] inputs = new TransactionInput[this.getInputs().length];
		for (int i = 0; i < this.getInputs().length; i++) {
			TransactionInput input = this.getInputs()[i];
			inputs[i] = new TransactionInput(input.getTransactionId(), input.getTransactionOutputIndex(), null, null);
		}

		TransactionOutput[] outputs = new TransactionOutput[this.getOutputs().length];
		for (int i = 0; i < this.getOutputs().length; i++) {
			TransactionOutput output = this.getOutputs()[i];
			outputs[i] = new TransactionOutput(output.getValue(), output.getPublicKeyHash());
		}

		return new Transaction(this.getTransactionId(), inputs, outputs);
	}


}
