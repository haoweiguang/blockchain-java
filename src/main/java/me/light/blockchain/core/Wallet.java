package me.light.blockchain.core;

import me.light.blockchain.util.Base58Check;
import me.light.blockchain.util.BitcoinAddressUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.io.ByteArrayOutputStream;
import java.security.*;

/**
 * 钱包
 *
 * @author light.hao
 * @create 2018-09-28-22:23
 */
public class Wallet {

	//校检码长度
	private static final int ADDRESS_CHECKSUM_LEN = 4;

	/**
	 * 私钥
	 */
	private BCECPrivateKey privateKey;

	/**
	 * 公钥
	 */
	private byte[] publicKey;

	public BCECPrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(BCECPrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	public Wallet() {
		initWallet();
	}

	public Wallet(BCECPrivateKey privateKey, byte[] publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

	/**
	 * 初始化钱包
	 */
	public void initWallet() {
		try {
			KeyPair keyPair = newECKeyPair();
			keyPair = newECKeyPair();
			BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
			BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

			byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);

			this.setPrivateKey(privateKey);
			this.setPublicKey(publicKeyBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建新的秘钥对
	 *
	 * @return
	 */
	public KeyPair newECKeyPair() throws Exception {

		//注册BC Provider
		Security.addProvider(new BouncyCastleProvider());
		//创建椭圆曲线算法的密钥对生成器，算法为 ECDSA
		KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
		// 椭圆曲线（EC）域参数设定
		// bitcoin 为什么会选择 secp256k1，详见：https://bitcointalk.org/index.php?topic=151120.0
		ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
		generator.initialize(ecSpec, new SecureRandom());
		return generator.generateKeyPair();
	}

	/**
	 * 获取钱包地址
	 *
	 * @return
	 */
	public String getAddress() throws Exception {
		// 1. 获取 ripemdHashedKey
		byte[] ripemdHashedKey = BitcoinAddressUtils.ripeMD160Hash(this.getPublicKey());

		// 2. 添加版本 0x00
		ByteArrayOutputStream addrStream = new ByteArrayOutputStream();
		addrStream.write((byte) 0);
		addrStream.write(ripemdHashedKey);
		byte[] versionedPayload = addrStream.toByteArray();

		// 3. 计算校验码
		byte[] checksum = BitcoinAddressUtils.checksum(versionedPayload);

		// 4. 得到 version + paylod + checksum 的组合
		addrStream.write(checksum);
		byte[] binaryAddress = addrStream.toByteArray();

		// 5. 执行Base58转换处理
		return Base58Check.rawBytesToBase58(binaryAddress);
	}
}
