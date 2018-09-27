package me.light.blockchain.core;

import java.util.Map;

/**
 * 查询结果
 *
 * @author light.hao
 * @create 2018-09-27-16:45
 */
public class SpendableOutputResult {

	/**
	 * 交易时的支付金额
	 */
	private int accumulated;

	/**
	 * 未花费的交易
	 */
	private Map<String, int[]> unspentOutputs;

	public int getAccumulated() {
		return accumulated;
	}

	public void setAccumulated(int accumulated) {
		this.accumulated = accumulated;
	}

	public Map<String, int[]> getUnspentOutputs() {
		return unspentOutputs;
	}

	public void setUnspentOutputs(Map<String, int[]> unspentOutputs) {
		this.unspentOutputs = unspentOutputs;
	}

	public SpendableOutputResult() {
	}

	public SpendableOutputResult(int accumulated, Map<String, int[]> unspentOutputs) {
		this.accumulated = accumulated;
		this.unspentOutputs = unspentOutputs;
	}
}
