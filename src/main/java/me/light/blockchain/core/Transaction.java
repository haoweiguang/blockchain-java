package me.light.blockchain.core;


import me.light.blockchain.util.BitcoinAddressUtils;
import me.light.blockchain.util.SerializeUtils;
import me.light.blockchain.util.WalletUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.Arrays;
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

	/**
	 * 计算交易信息的Hash值
	 *
	 * @return
	 */
	public byte[] hash() {
		// 使用序列化的方式对Transaction对象进行深度复制
		byte[] serializeBytes = SerializeUtils.serialize(this);
		Transaction copyTx = (Transaction) SerializeUtils.deserialize(serializeBytes);
		copyTx.setTransactionId(new byte[]{});
		return DigestUtils.sha256(SerializeUtils.serialize(copyTx));
	}


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
			TransactionInput input4Sign = transaction4Sign.getInputs()[i];
			//获取交易输入TransactionId对应的交易数据
			Transaction transaction = transactions.get(Hex.encodeHexString(input4Sign.getTransactionId()));
			//获取交易输入所对应的上一笔交易中的交易输出
			TransactionOutput output4Sgin = transaction.getOutputs()[input4Sign.getTransactionOutputIndex()];
			input4Sign.setPublicKey(output4Sgin.getPublicKeyHash());
			input4Sign.setSignature(null);
			//得到签名的数据，即交易ID
			transaction4Sign.setTransactionId(transaction4Sign.hash());
			input4Sign.setPublicKey(null);

			//对整个交易信息进行签名，即对交易ID进行签名
			ecdsaSign.update(transaction4Sign.getTransactionId());
			byte[] signature = ecdsaSign.sign();


			// 将整个交易数据的签名赋值给交易输入，因为交易输入需要包含整个交易信息的签名
			// 注意是将得到的签名赋值给原交易信息中的交易输入
			this.getInputs()[i].setSignature(signature);
		}


	}

	/**
	 * 验证交易信息
	 *
	 * @param prevTxMap 前面多笔交易集合
	 * @return
	 */
	public boolean verify(Map<String, Transaction> prevTxMap) throws Exception {
		//coinbase 交易信息不需要签名，也就无需验证
		if (this.isCoinBase()) {
			return true;
		}

		// 再次验证一下交易信息中的交易输入是否正确，也就是能否查找对应的交易数据
		for (TransactionInput txInput : this.getInputs()) {
			if (prevTxMap.get(Hex.encodeHexString(txInput.getTransactionId())) == null) {
				throw new Exception("ERROR: Previous transaction is not correct");
			}
		}

		// 创建用于签名验证的交易信息的副本
		Transaction txCopy = this.trimmedCopy();

		Security.addProvider(new BouncyCastleProvider());
		ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("secp256k1");
		KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
		Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);

		for (int i = 0; i < this.getInputs().length; i++) {
			TransactionInput txInput = this.getInputs()[i];
			// 获取交易输入TxID对应的交易数据
			Transaction prevTx = prevTxMap.get(Hex.encodeHexString(txInput.getTransactionId()));
			// 获取交易输入所对应的上一笔交易中的交易输出
			TransactionOutput prevTxOutput = prevTx.getOutputs()[txInput.getTransactionOutputIndex()];

			TransactionInput txInputCopy = txCopy.getInputs()[i];
			txInputCopy.setSignature(null);
			txInputCopy.setPublicKey(prevTxOutput.getPublicKeyHash());
			// 得到要签名的数据，即交易ID
			txCopy.setTransactionId(txCopy.hash());
			txInputCopy.setPublicKey(null);

			// 使用椭圆曲线 x,y 点去生成公钥Key
			BigInteger x = new BigInteger(1, Arrays.copyOfRange(txInput.getPublicKey(), 1, 33));
			BigInteger y = new BigInteger(1, Arrays.copyOfRange(txInput.getPublicKey(), 33, 65));
			ECPoint ecPoint = ecParameters.getCurve().createPoint(x, y);

			ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ecParameters);
			PublicKey publicKey = keyFactory.generatePublic(keySpec);
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(txCopy.getTransactionId());
			if (!ecdsaVerify.verify(txInput.getSignature())) {
				return false;
			}
		}
		return true;
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
