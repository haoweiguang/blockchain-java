package me.light.core;

import me.light.util.ByteUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.time.Instant;

/**
 * 区块(超简化版的)
 *
 * @author light.hao
 * @create 2018-08-07-16:42
 */
public class Block {

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
	 * 区块中的数据（现在是做的简化）
	 */
	private String data;

	/**
	 * 区块创建的时间
	 */
	private long timeStamp;

	public Block() {

	}

	public Block(String hash, String previousHash, String data, long timeStamp) {
		this();
		this.hash = hash;
		this.previousHash = previousHash;
		this.data = data;
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}


	/**
	 * 创建区块
	 *
	 * @param previousHash
	 * @param data
	 * @return
	 */
	public static Block newBlock(String previousHash, String data) {

		Block block = new Block(ZERO_HASH, previousHash, data, Instant.now().getEpochSecond());
		block.setHash();
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
				this.getData().getBytes(),
				ByteUtils.toBytes(this.getTimeStamp()));

		this.setHash(DigestUtils.sha256Hex(headers));
	}


}
