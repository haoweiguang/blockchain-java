package me.light.blockchain.core;

import me.light.blockchain.util.ByteUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

/**
 * 工作量证明类
 *
 * @author light.hao
 * @create 2018-08-08-16:35
 */
public class ProofOfWork {

	/**
	 * 难度目标位
	 */
	private static final int TAGET_BITS = 20;

	/**
	 * 区块
	 */
	private Block block;

	/**
	 * 难度目标值
	 */
	private BigInteger target;

	public ProofOfWork(Block block, BigInteger target) {
		this.block = block;
		this.target = target;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public BigInteger getTarget() {
		return target;
	}

	public void setTarget(BigInteger target) {
		this.target = target;
	}

	/**
	 * 创建工作量证明
	 *
	 * @param block
	 * @return
	 */
	public static ProofOfWork newProofOfWork(Block block) {
		BigInteger targetValue = BigInteger.ONE.shiftLeft((256 - TAGET_BITS));
		return new ProofOfWork(block, targetValue);

	}

	/**
	 * 运行工作量证明
	 *
	 * @return
	 */
	public PowResult run() {
		long nonce = 0;
		String shaHex = "";
		System.out.printf("Mining the block containing：%s \n", this.getBlock().getTransactions());
		long startTime = System.currentTimeMillis();
		while (nonce < Long.MAX_VALUE) {
			byte[] data = prepare(nonce);
			shaHex = DigestUtils.sha256Hex(data);
			if (new BigInteger(shaHex, 16).compareTo(this.target) == -1) {
				//小于目标值
				System.out.printf("Elapsed Time: %s seconds \n", (float) (System.currentTimeMillis() - startTime) / 1000);
				System.out.printf("correct hash Hex: %s \n\n", shaHex);
				break;
			} else {
				nonce++;
			}
		}
		return new PowResult(nonce, shaHex);
	}

	/**
	 * 准备数据
	 *
	 * @param nonce
	 * @return
	 */
	private byte[] prepare(long nonce) {
		byte[] prevBlockHashBytes = {};
		if (StringUtils.isNoneBlank(this.getBlock().getPreviousHash())) {
			prevBlockHashBytes = new BigInteger(this.getBlock().getPreviousHash(), 16).toByteArray();
		}

		return ByteUtils.merge(
				prevBlockHashBytes,
				this.getBlock().hashTransaction(),
				ByteUtils.toBytes(this.getBlock().getTimeStamp()),
				ByteUtils.toBytes(TAGET_BITS),
				ByteUtils.toBytes(nonce)
		);
	}

	/**
	 * 验证区块
	 *
	 * @return
	 */
	public boolean validate() {
		byte[] data = this.prepare(this.block.getNonce());
		return new BigInteger(DigestUtils.sha256Hex(data), 16).compareTo(this.target) == -1;
	}

}
