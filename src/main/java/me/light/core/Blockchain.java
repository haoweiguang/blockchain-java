package me.light.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 区块链
 *
 * @author light.hao
 * @create 2018-08-07-17:12
 */
public class Blockchain {
	List<Block> blockchain;

	public List<Block> getBlockchain() {
		return blockchain;
	}

	public Blockchain(List<Block> blocks) {
		this.blockchain = blocks;
	}

	public void setBlockchain(List<Block> blockchain) {
		this.blockchain = blockchain;
	}

	/**
	 * 生成区块链
	 *
	 * @return
	 */
	public static Blockchain newBlockChain() {
		List<Block> blocks = new ArrayList<>();
		blocks.add(Block.newGenesisBlock());
		return new Blockchain(blocks);
	}

	/**
	 * 添加新的区块
	 *
	 * @param data
	 */
	public void addBlock(String data) {
		/**
		 * 1、获取前一个区块的hash值
		 * 2、生成新的区块
		 */

		Block prevBlock = blockchain.get(blockchain.size() - 1);
		this.addBlock(Block.newBlock(prevBlock.getHash(), data));
	}

	/**
	 * 添加新的区块
	 *
	 * @param block
	 */
	public void addBlock(Block block) {
		this.blockchain.add(block);
	}
}
