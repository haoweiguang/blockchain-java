package me.light.blockchain.util;

import com.google.common.collect.Maps;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import sun.plugin2.os.windows.FLASHWINFO;

import javax.management.RuntimeErrorException;
import java.util.Map;

/**
 * 存储工具类
 *
 * @author light.hao
 * @create 2018-09-20-14:22
 */
public class RocksDBUtils {

	/**
	 * 区块链数据文件
	 */
	private final String DB_FILE = "blockchain.db";

	/**
	 * 区块桶前缀
	 */
	private static final String BLOCKS_BUCKET_KEY = "blocks";
	/**
	 * 最新一个区块
	 */
	private static final String LAST_BLOCK_KEY = "l";

	private volatile static RocksDBUtils instance;

	public static RocksDBUtils getInstance() {
		if (instance == null) {
			synchronized (RocksDBUtils.class) {
				if (instance == null) {
					instance = new RocksDBUtils();
				}
			}
		}
		return instance;
	}

	private RocksDB db;

	/**
	 * block buckets
	 */
	private Map<String, byte[]> blocksBucket;


	private RocksDBUtils() {
		openDB();
	}

	/**
	 * 打开数据库
	 */
	private void openDB() {
		try {
			db = RocksDB.open(DB_FILE);
		} catch (RocksDBException e) {
			throw new RuntimeException("Fail to open db ! ", e);
		}
	}

	/**
	 * 初始化blocks数据桶
	 */
	private void initBlockBucket() {
		try {

			byte[] blockBucketKey = SerializeUtils.serialize(BLOCKS_BUCKET_KEY);
			byte[] blockBucketBytes = db.get(blockBucketKey);
			if (blockBucketBytes == null) {
				blocksBucket = Maps.newHashMap();
				db.put(blockBucketKey, SerializeUtils.serialize(blocksBucket));
			} else {
				blocksBucket = (Map) SerializeUtils.deserialize(blockBucketBytes);
			}

		} catch (RocksDBException e) {
			throw new RuntimeException("Fail to init block bucket ! ", e);
		}
	}

	/**
	 * 保存最新一个区块的hash值
	 *
	 * @param tipBlockHash
	 */
	public void putLastBlockHash(String tipBlockHash) {
		blocksBucket.put(LAST_BLOCK_KEY, SerializeUtils.serialize(tipBlockHash));
		try {
			db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
		} catch (RocksDBException e) {
			throw new RuntimeException("Fail to put last block hash ! ", e);
		}
	}

	/**
	 * 查询最新一个区块的hash值
	 *
	 * @return
	 */
	public String getLastBlockHash() {
		byte[] lastBlockHashBytes = blocksBucket.get(LAST_BLOCK_KEY);
		if (lastBlockHashBytes != null) {
			return (String) SerializeUtils.deserialize(lastBlockHashBytes);
		}
		return "";
	}



}
