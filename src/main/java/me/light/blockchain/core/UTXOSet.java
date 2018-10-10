package me.light.blockchain.core;

import com.google.common.collect.Maps;
import lombok.Synchronized;
import me.light.blockchain.util.RocksDBUtils;
import me.light.blockchain.util.SerializeUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 未被花费的交易输出池
 *
 * @author light.hao
 * @create 2018-10-10-10:50
 */
public class UTXOSet {

	private final Logger logger = LoggerFactory.getLogger(UTXOSet.class);

	private Blockchain blockchain;

	public Blockchain getBlockchain() {
		return blockchain;
	}

	public void setBlockchain(Blockchain blockchain) {
		this.blockchain = blockchain;
	}

	public UTXOSet(Blockchain blockchain) {
		this.blockchain = blockchain;
	}

	public UTXOSet() {
	}

	/**
	 * 重建UTXO池索引
	 */
	@Synchronized
	public void reIndex() throws Exception {
		logger.info("start to reIndex UTXO set");
		//先清空
		RocksDBUtils.getInstance().cleanChainStateBucket();
		Map<String, TransactionOutput[]> allUTXOs = blockchain.findAllUTXOs();
		for (Map.Entry<String, TransactionOutput[]> entry : allUTXOs.entrySet()) {
			RocksDBUtils.getInstance().putUTXOs(entry.getKey(), entry.getValue());
		}
		logger.info("ReIndex UTXO set finished ! ");
	}


	/**
	 * 寻找能够交易的输出
	 *
	 * @param publicKeyHash 钱包的公钥hash
	 * @param amount        花费金额
	 * @return
	 */
	public SpendableOutputResult findSpendableOutputs(byte[] publicKeyHash, int amount) {
		Map<String, int[]> unspentOuts = Maps.newHashMap();
		int accumulated = 0;
		Map<String, byte[]> chainstateBucket = RocksDBUtils.getInstance().getChainstateBucket();
		for (Map.Entry<String, byte[]> entry : chainstateBucket.entrySet()) {
			String transactionId = entry.getKey();
			TransactionOutput[] outputs = (TransactionOutput[]) SerializeUtils.deserialize(entry.getValue());

			for (int index = 0; index < outputs.length; index++) {
				TransactionOutput output = outputs[index];
				if (output.isLockedWithKey(publicKeyHash) && accumulated < amount) {
					accumulated += output.getValue();
				}

				int[] outputIds = unspentOuts.get(transactionId);
				if (outputIds == null) {
					outputIds = new int[index];
				} else {
					outputIds = ArrayUtils.add(outputIds, index);
				}
				unspentOuts.put(transactionId, outputIds);
				if (accumulated >= amount) {
					break;
				}
			}
		}
		return new SpendableOutputResult(accumulated, unspentOuts);
	}

	/**
	 * 查找钱包地址对应的所有UTXO
	 *
	 * @param publicKeyHash
	 * @return
	 */
	public TransactionOutput[] findUTXOs(byte[] publicKeyHash) {
		TransactionOutput[] utxos = {};
		Map<String, byte[]> chainstateBucket = RocksDBUtils.getInstance().getChainstateBucket();

		if (!chainstateBucket.isEmpty()) {
			for (byte[] value : chainstateBucket.values()) {
				TransactionOutput[] outputs = (TransactionOutput[]) SerializeUtils.deserialize(value);
				for (TransactionOutput output : outputs) {
					if (output.isLockedWithKey(publicKeyHash)) {
						utxos = ArrayUtils.add(utxos, output);
					}
				}
			}
		}
		return utxos;
	}

	/**
	 * 更新UTXO池
	 * <p>
	 * 当一个新的区块产生时，需要做两个事情
	 * 1.从UTXO池中移除花费掉了的交易输出；
	 * 2.保存新的未花费交易输出；
	 *
	 * @param block 最新的区块
	 */
	@Synchronized
	public void update(Block block) {
		if (block == null) {
			logger.error("Fail to update UTXO set ! tipBlock is null !");
			throw new RuntimeException("Fail to update UTXO set ! ");
		}

		for (Transaction transaction : block.getTransactions()) {

			//根据交易输入排查未被使用的交易输出
			if (!transaction.isCoinBase()) {
				for (TransactionInput input : transaction.getInputs()) {
					//未被使用的交易输出
					TransactionOutput[] unSpendOutputs = {};

					String transactionId = Hex.encodeHexString(input.getTransactionId());
					TransactionOutput[] outputs = RocksDBUtils.getInstance().getUTXOs(transactionId);

					if (outputs == null) {
						continue;
					}

					for (int i = 0; i < outputs.length; i++) {
						if (i != input.getTransactionOutputIndex()) {
							unSpendOutputs = ArrayUtils.add(unSpendOutputs, outputs[i]);
						}
					}

					//如果没有剩余则删除，否则更新
					if (unSpendOutputs.length == 0) {
						RocksDBUtils.getInstance().deleteUTXOs(transactionId);
					} else {
						RocksDBUtils.getInstance().putUTXOs(transactionId, unSpendOutputs);
					}
				}
			}

			//新的交易输出保存到DB中
			TransactionOutput[] outputs = transaction.getOutputs();
			String transactionId = Hex.encodeHexString(transaction.getTransactionId());
			RocksDBUtils.getInstance().putUTXOs(transactionId, outputs);
		}
	}
}
