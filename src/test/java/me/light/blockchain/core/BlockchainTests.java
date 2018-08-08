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

}
