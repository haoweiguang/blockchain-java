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

	public static void main(String[] args) {
		try {
			Blockchain blockchain = Blockchain.newBlockChain();

			blockchain.addBlock("Send 1.0 BTC to light");
			blockchain.addBlock("Send 2.5 more BTC to light");
			blockchain.addBlock("Send 3.5 more BTC to light");

			for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
				Block block = iterator.next();

				if (block != null) {
					boolean validate = ProofOfWork.newProofOfWork(block).validate();
					System.out.println(block.toString() + ", validate = " + validate);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
