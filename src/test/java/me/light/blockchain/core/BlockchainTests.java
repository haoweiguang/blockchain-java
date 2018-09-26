package me.light.blockchain.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 区块链的测试
 *
 * @author light.hao
 * @create 2018-08-08-14:30
 */
public class BlockchainTests {

	public static void main(String[] args) throws Exception {
		String address = "light";
		Blockchain blockchain = Blockchain.newBlockChain(address);
		TransactionOutput[] outputs = blockchain.findUTXO(address);
		int balance = 0;
		if (outputs != null && outputs.length > 0) {
			for (TransactionOutput output : outputs) {
				balance += output.getValue();
			}
		}
		System.out.printf("Balance of '%s': %d\n", address, balance);
	}

}
