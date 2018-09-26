package me.light.blockchain.core;

import me.light.blockchain.util.RocksDBUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
	 * 生成区块链
	 *
	 * @return
	 */
	public static Blockchain newBlockChain() {
		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		if (StringUtils.isBlank(lastBlockHash)) {
			Block genesisBlock = Block.newGenesisBlock();
			lastBlockHash = genesisBlock.getHash();
			RocksDBUtils.getInstance().putBlock(genesisBlock);
			RocksDBUtils.getInstance().putLastBlockHash(lastBlockHash);
		}
		return new Blockchain(lastBlockHash);
	}

	/**
	 * 添加新的区块
	 *
	 * @param data
	 */
	public void addBlock(String data) throws Exception {
		/**
		 * 每次挖矿完成后，我们也需要将最新的区块信息保存下来，并且更新最新区块链Hash值
		 */

		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		if (StringUtils.isBlank(lastBlockHash)) {
			throw new Exception("Fail to add block into blockchain ! ");
		}
		this.addBlock(Block.newBlock(lastBlockHash, data));
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

}
