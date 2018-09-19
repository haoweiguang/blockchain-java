package me.light;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.*;
import java.math.BigInteger;

/**
 * 工具类测试
 *
 * @author light.hao
 * @create 2018-08-10-15:38
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UtilTests {

	@Test
	public void testBigIntegerShiftLeft() {
		BigInteger targetValue = BigInteger.ONE.shiftLeft((256 - 26));
		System.out.println(BigInteger.ONE);
		System.out.println(targetValue.toString());
	}

}
