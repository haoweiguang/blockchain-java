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
@RunWith(SpringRunner.class)
@SpringBootTest
public class BlockchainTests {

	@Test
	public void addBlock() {
		Blockchain blockchain = Blockchain.newBlockChain();
		blockchain.addBlock("alice send 0.5btc to jack");
		blockchain.addBlock("jack send 0.1btc to lucy");
	}

	@Test
	public void POWTest() {
		Blockchain blockchain = Blockchain.newBlockChain();

		blockchain.addBlock("Send 1 BTC to Ivan");
		blockchain.addBlock("Send 2 more BTC to Ivan");

		for (Block block : blockchain.getBlockchain()) {
			System.out.println("Prev.hash: " + block.getPreviousHash());
			System.out.println("Data: " + block.getData());
			System.out.println("Hash: " + block.getHash());
			System.out.println("Nonce: " + block.getNonce());

			ProofOfWork pow = ProofOfWork.newProofOfWork(block);
			System.out.println("Pow valid: " + pow.validate() + "\n");
		}

	}

}
