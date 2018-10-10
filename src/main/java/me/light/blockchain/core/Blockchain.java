package me.light.blockchain.core;

import com.google.common.collect.Maps;
import me.light.blockchain.util.RocksDBUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

import java.util.*;

/**
 * 区块链
 *
 * @author light.hao
 * @create 2018-08-07-17:12
 */
public class Blockchain {

	private String lastBlockHash;


	private Blockchain(String lastBlockHash) {
		this.lastBlockHash = lastBlockHash;
	}

	public String getLastBlockHash() {
		return lastBlockHash;
	}

	/**
	 * 从DB恢复区块链数据
	 *
	 * @return
	 * @throws Exception
	 */
	public static Blockchain initBlockchainFromDB() throws Exception {
		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		if (lastBlockHash == null) {
			throw new Exception("ERROR: Fail to init blockchain from db. ");
		}
		return new Blockchain(lastBlockHash);
	}

	/**
	 * 生成区块链
	 *
	 * @return
	 */
	public static Blockchain newBlockChain(String address) {
		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		if (StringUtils.isBlank(lastBlockHash)) {
			//创建coinbase交易
			Transaction coinbaseTransaction = Transaction.newCoinbaseTransaction(address, "");
			Block genesisBlock = Block.newGenesisBlock(coinbaseTransaction);
			lastBlockHash = genesisBlock.getHash();
			RocksDBUtils.getInstance().putBlock(genesisBlock);
			RocksDBUtils.getInstance().putLastBlockHash(lastBlockHash);
		}
		return new Blockchain(lastBlockHash);
	}

	/**
	 * 添加新区块
	 *
	 * @param transactions
	 * @throws Exception
	 */
	public void addBlock(Transaction[] transactions) throws Exception {
		/**
		 * 每次挖矿完成后，我们也需要将最新的区块信息保存下来，并且更新最新区块链Hash值
		 */
		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		if (StringUtils.isBlank(lastBlockHash)) {
			throw new Exception("Fail to add block into blockchain ! ");
		}
		this.addBlock(Block.newBlock(lastBlockHash, transactions));
	}

	/**
	 * 打包交易，进行挖矿
	 *
	 * @param transactions
	 */
	public void mineBlock(Transaction[] transactions) throws Exception {
		//挖矿前，先验证交易记录
		for (Transaction transaction : transactions) {
			if (!this.verifyTransaction(transaction)) {
				throw new Exception("ERROR: Fail to mine block ! Invalid transaction !");
			}
		}

		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		if (lastBlockHash == null) {
			throw new Exception("ERROR: Fail to get last block hash ! ");
		}
		Block block = Block.newBlock(lastBlockHash, transactions);
		this.addBlock(block);
	}

	/**
	 * 添加新的区块
	 *
	 * @param block
	 */
	public void addBlock(Block block) {
		RocksDBUtils.getInstance().putLastBlockHash(block.getHash());
		RocksDBUtils.getInstance().putBlock(block);
		this.lastBlockHash = block.getHash();
	}

	/**
	 * 区块链迭代器
	 */
	public class BlockchainIterator {

		private String currentBlockHash;

		public BlockchainIterator(String currentBlockHash) {
			this.currentBlockHash = currentBlockHash;
		}

		/**
		 * 是否有下一个区块
		 *
		 * @return
		 */
		public boolean hashNext() throws Exception {
			if (StringUtils.isBlank(currentBlockHash)) {
				return false;
			}
			Block lastBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
			if (lastBlock == null) {
				return false;
			}
			// 创世区块直接放行
			if (lastBlock.getPreviousHash().length() == 0) {
				return true;
			}
			return RocksDBUtils.getInstance().getBlock(lastBlock.getPreviousHash()) != null;
		}


		/**
		 * 返回区块
		 *
		 * @return
		 */
		public Block next() throws Exception {
			Block currentBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
			if (currentBlock != null) {
				this.currentBlockHash = currentBlock.getPreviousHash();
				return currentBlock;
			}
			return null;
		}
	}

	public BlockchainIterator getBlockchainIterator() {
		return new BlockchainIterator(lastBlockHash);
	}


	/**
	 * 从交易输入中查询区块链中所有已被花费了的交易输出
	 *
	 * @param publicKeyHash 钱包公钥hash
	 * @return 交易ID以及对应的交易输出下标地址
	 * @throws Exception
	 */
	private Map<String, int[]> getAllSpentTransactionInputs(byte[] publicKeyHash) throws Exception {
		Map<String, int[]> spentTransactionInputs = new HashMap<>();
		for (BlockchainIterator blockchainIterator = this.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
			Block block = blockchainIterator.next();
			for (Transaction transaction : block.getTransactions()) {
				if (transaction.isCoinBase()) {
					continue;
				}

				for (TransactionInput input : transaction.getInputs()) {
					if (input.usesKey(publicKeyHash)) {
						String inTransactionId = Hex.encodeHexString(input.getTransactionId());
						int[] spentOutIndexArray = spentTransactionInputs.get(inTransactionId);
						if (spentOutIndexArray == null) {
							spentTransactionInputs.put(inTransactionId, new int[]{input.getTransactionOutputIndex()});
						} else {
							spentOutIndexArray = ArrayUtils.add(spentOutIndexArray, input.getTransactionOutputIndex());
							spentTransactionInputs.put(inTransactionId, spentOutIndexArray);
						}
					}
				}
			}
		}
		return spentTransactionInputs;
	}


	/**
	 * 查找钱包地址对应的所有未花费的交易
	 *
	 * @param publicKeyHash
	 * @return
	 * @throws Exception
	 */
	private Transaction[] findUnspentTransactions(byte[] publicKeyHash) throws Exception {

		Map<String, int[]> allSpentTransactionInputs = this.getAllSpentTransactionInputs(publicKeyHash);
		Transaction[] unspentTransactions = {};

		//再次遍历所有区块中的交易输出
		for (BlockchainIterator blockchainIterator = this.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
			Block block = blockchainIterator.next();
			for (Transaction transaction : block.getTransactions()) {
				String transactionId = Hex.encodeHexString(transaction.getTransactionId());

				int[] spentOutIndexArray = allSpentTransactionInputs.get(transactionId);
				for (int outIndex = 0; outIndex < transaction.getOutputs().length; outIndex++) {
					if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray, outIndex)) {
						continue;
					}

					if (transaction.getOutputs()[outIndex].isLockedWithKey(publicKeyHash)) {
						unspentTransactions = ArrayUtils.add(unspentTransactions, transaction);
					}

				}
			}
		}
		return unspentTransactions;
	}


	/**
	 * 查找钱包地址对应的所有UTXO
	 *
	 * @param publicKeyHash
	 * @return
	 * @throws Exception
	 */
	public TransactionOutput[] findUTXO(byte[] publicKeyHash) throws Exception {
		Transaction[] unspentTransactions = this.findUnspentTransactions(publicKeyHash);

		TransactionOutput[] utxos = {};

		if (unspentTransactions == null || unspentTransactions.length == 0) {
			return utxos;
		}

		for (Transaction transaction : unspentTransactions) {
			for (TransactionOutput output : transaction.getOutputs()) {
				if (output.isLockedWithKey(publicKeyHash)) {
					utxos = ArrayUtils.add(utxos, output);
				}
			}
		}
		return utxos;
	}

	/**
	 * 查找能够花费的交易输出
	 *
	 * @param publicKeyHash 钱包地址hash
	 * @param amount        支付金额
	 * @return
	 */
	public SpendableOutputResult findSpendableOutputs(byte[] publicKeyHash, int amount) throws Exception {
		Transaction[] unspentTransactions = this.findUnspentTransactions(publicKeyHash);
		int accumulated = 0;

		Map<String, int[]> unspentOutputs = new HashMap<>();
		for (Transaction transaction : unspentTransactions) {
			String transactionId = Hex.encodeHexString(transaction.getTransactionId());
			for (int outputIndex = 0; outputIndex < transaction.getOutputs().length; outputIndex++) {
				TransactionOutput output = transaction.getOutputs()[outputIndex];
				if (output.isLockedWithKey(publicKeyHash) && accumulated < amount) {
					accumulated += output.getValue();
				}

				int[] outIndexs = unspentOutputs.get(transactionId);
				if (outIndexs == null) {
					outIndexs = new int[]{outputIndex};
				} else {
					outIndexs = ArrayUtils.add(outIndexs, outputIndex);
				}
				unspentOutputs.put(transactionId, outIndexs);
				if (accumulated >= amount) {
					break;
				}
			}
		}
		return new SpendableOutputResult(accumulated, unspentOutputs);
	}


	/**
	 * 根据交易id查询交易信息
	 *
	 * @param transactionId
	 * @return
	 */
	private Transaction findTransaction(byte[] transactionId) throws Exception {
		for (BlockchainIterator iterator = this.getBlockchainIterator(); iterator.hashNext(); ) {
			Block block = iterator.next();
			for (Transaction transaction : block.getTransactions()) {
				if (Arrays.equals(transaction.getTransactionId(), transactionId)) {
					return transaction;
				}
			}
		}
		throw new Exception("ERROR: Can not found tx by txId ! ");
	}


	/**
	 * 进行交易签名
	 *
	 * @param transaction 交易数据
	 * @param privateKey  私钥
	 * @throws Exception
	 */
	public void signTransaction(Transaction transaction, BCECPrivateKey privateKey) throws Exception {
		//先找到这笔新的交易中，交易输入所引用的前面的多笔交易的数据
		Map<String, Transaction> prevTransactions = new HashMap<>();
		for (TransactionInput input : transaction.getInputs()) {
			Transaction prevTransaction = this.findTransaction(input.getTransactionId());
			prevTransactions.put(Hex.encodeHexString(input.getTransactionId()), prevTransaction);
		}
		transaction.sign(privateKey, prevTransactions);
	}

	/**
	 * 验证交易
	 *
	 * @param transaction 交易数据
	 * @return
	 * @throws Exception
	 */
	public boolean verifyTransaction(Transaction transaction) throws Exception {
		Map<String, Transaction> prevTransaction = new HashMap<>();
		for (TransactionInput txInput : transaction.getInputs()) {
			Transaction tempTransaction = this.findTransaction(txInput.getTransactionId());
			prevTransaction.put(Hex.encodeHexString(txInput.getTransactionId()), transaction);
		}
		return transaction.verify(prevTransaction);
	}

	/**
	 * 查找所有的未交易输出
	 *
	 * @return
	 */
	public Map<String, TransactionOutput[]> findAllUTXOs() throws Exception {
		Map<String, int[]> spentTXOs = this.findAllSpentTxOutputs();
		Map<String, TransactionOutput[]> allUTXOs = Maps.newHashMap();

		for (BlockchainIterator iterator = this.getBlockchainIterator(); iterator.hashNext(); ) {
			Block block = iterator.next();

			for (Transaction transaction : block.getTransactions()) {
				String transactionId = Hex.encodeHexString(transaction.getTransactionId());
				int[] spentOutIndexArray = spentTXOs.get(transactionId);

				TransactionOutput[] outputs = transaction.getOutputs();
				for (int index = 0; index < outputs.length; index++) {
					if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray, index)) {
						continue;
					}

					TransactionOutput[] UTXOArray = allUTXOs.get(transactionId);
					if (UTXOArray == null) {
						UTXOArray = new TransactionOutput[]{outputs[index]};
					} else {
						UTXOArray = ArrayUtils.add(UTXOArray, outputs[index]);
					}
					allUTXOs.put(transactionId, UTXOArray);
				}
			}
		}
		return allUTXOs;
	}

	/**
	 * 查询所有交易输出
	 *
	 * @return
	 * @throws Exception
	 */
	private Map<String, int[]> findAllSpentTxOutputs() throws Exception {
		Map<String, int[]> result = Maps.newHashMap();
		for (BlockchainIterator iterator = this.getBlockchainIterator(); iterator.hashNext(); ) {
			Block block = iterator.next();

			for (Transaction transaction : block.getTransactions()) {
				//coinbase交易直接跳过
				if (transaction.isCoinBase()) {
					continue;
				}

				for (TransactionInput input : transaction.getInputs()) {
					String inputTxId = Hex.encodeHexString(input.getTransactionId());
					int[] spentOutIndexArray = result.get(inputTxId);
					if (spentOutIndexArray == null) {
						spentOutIndexArray = new int[input.getTransactionOutputIndex()];
					} else {
						spentOutIndexArray = ArrayUtils.add(spentOutIndexArray, input.getTransactionOutputIndex());
					}
					result.put(inputTxId, spentOutIndexArray);
				}
			}
		}
		return result;
	}
}
