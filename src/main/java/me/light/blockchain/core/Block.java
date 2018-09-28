package me.light.blockchain.core;

import me.light.blockchain.util.ByteUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;

/**
 * 区块(超简化版的)
 *
 * @author light.hao
 * @create 2018-08-07-16:42
 */
public class Block implements Serializable {

	private static final String ZERO_HASH = Hex.encodeHexString(new byte[32]);

	/**
	 * 区块的hash值
	 */
	private String hash;

	/**
	 * 前一个区块的hash值
	 */
	private String previousHash;

	/**
	 * 区块中的交易
	 */
	private Transaction[] transactions;

	/**
	 * 区块创建的时间
	 */
	private long timeStamp;

	/**
	 * 工作量证明计数器
	 */
	private long nonce;

	public Block() {

	}

	public Block(String hash, String previousHash, Transaction[] transactions, long timeStamp) {
		this();
		this.hash = hash;
		this.previousHash = previousHash;
		this.transactions = transactions;
		this.timeStamp = timeStamp;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}

	public Transaction[] getTransactions() {
		return transactions;
	}

	public void setTransactions(Transaction[] transactions) {
		this.transactions = transactions;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getNonce() {
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}


	/**
	 * 创建区块
	 *
	 * @param previousHash
	 * @param transactions
	 * @return
	 */
	public static Block newBlock(String previousHash, Transaction[] transactions) {
		Block block = new Block(ZERO_HASH, previousHash, transactions, Instant.now().getEpochSecond());
		ProofOfWork proofOfWork = ProofOfWork.newProofOfWork(block);
		PowResult powResult = proofOfWork.run();
		block.setHash(powResult.getHash());
		block.setNonce(powResult.getNonce());
		return block;
	}

	/**
	 * 计算区块hash值
	 */
	private void setHash() {
		byte[] prevBlockHashBytes = {};
		if (StringUtils.isNoneBlank(this.getPreviousHash())) {
			prevBlockHashBytes = new BigInteger(this.getPreviousHash(), 16).toByteArray();
		}

		byte[] headers = ByteUtils.merge(prevBlockHashBytes,
				this.hashTransaction(),
				ByteUtils.toBytes(this.getTimeStamp()));

		this.setHash(DigestUtils.sha256Hex(headers));
	}


	/**
	 * 对区块中的交易信息进行Hash计算
	 *
	 * @return
	 */
	public byte[] hashTransaction() {

		byte[][] transactionIdArrays = new byte[this.getTransactions().length][];
		for (int i = 0; i < this.getTransactions().length; i++) {
			transactionIdArrays[i] = this.getTransactions()[i].getTransactionId();
		}
		return DigestUtils.sha256(ByteUtils.merge(transactionIdArrays));
	}

	/**
	 * 创世块
	 *
	 * @return
	 */
	public static Block newGenesisBlock(Transaction coinbase) {

		return Block.newBlock("", new Transaction[]{coinbase});
	}

}
