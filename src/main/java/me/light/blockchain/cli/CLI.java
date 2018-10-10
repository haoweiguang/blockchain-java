package me.light.blockchain.cli;


import me.light.blockchain.core.*;
import me.light.blockchain.util.Base58Check;
import me.light.blockchain.util.RocksDBUtils;
import me.light.blockchain.util.WalletUtils;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;

/**
 * 程序命令行工具入口
 *
 * @author light.hao
 * @date 2018/03/08
 */
public class CLI {

	private String[] args;
	private Options options = new Options();

	public CLI(String[] args) {
		this.args = args;

		Option helpCmd = Option.builder("h").desc("show help").build();
		options.addOption(helpCmd);

		Option address = Option.builder("address").hasArg(true).desc("Source wallet address").build();
		Option sendFrom = Option.builder("from").hasArg(true).desc("Source wallet address").build();
		Option sendTo = Option.builder("to").hasArg(true).desc("Destination wallet address").build();
		Option sendAmount = Option.builder("amount").hasArg(true).desc("Amount to send").build();

		options.addOption(address);
		options.addOption(sendFrom);
		options.addOption(sendTo);
		options.addOption(sendAmount);
	}

	/**
	 * 命令行解析入口
	 */
	public void parse() {
		this.validateArgs(args);
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			switch (args[0]) {
				case "createblockchain":
					String createblockchainAddress = cmd.getOptionValue("address");
					if (StringUtils.isBlank(createblockchainAddress)) {
						help();
					}
					this.createBlockchain(createblockchainAddress);
					break;
				case "getbalance":
					String getBalanceAddress = cmd.getOptionValue("address");
					if (StringUtils.isBlank(getBalanceAddress)) {
						help();
					}
					this.getBalance(getBalanceAddress);
					break;
				case "send":
					String sendFrom = cmd.getOptionValue("from");
					String sendTo = cmd.getOptionValue("to");
					String sendAmount = cmd.getOptionValue("amount");
					if (StringUtils.isBlank(sendFrom) ||
							StringUtils.isBlank(sendTo) ||
							!NumberUtils.isDigits(sendAmount)) {
						help();
					}
					this.send(sendFrom, sendTo, Integer.valueOf(sendAmount));
					break;
				case "createwallet":
					this.createWallet();
				case "printchain":
					this.printChain();
					break;
				case "h":
					this.help();
					break;
				default:
					this.help();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			RocksDBUtils.getInstance().closeDB();
		}
	}

	/**
	 * 验证入参
	 *
	 * @param args
	 */
	private void validateArgs(String[] args) {
		if (args == null || args.length < 1) {
			help();
		}
	}

	/**
	 * 创建区块链
	 *
	 * @param address
	 */
	private void createBlockchain(String address) throws Exception {
		Blockchain blockchain = Blockchain.newBlockChain(address);
		UTXOSet utxoSet = new UTXOSet(blockchain);
		utxoSet.reIndex();
		System.out.println("Done ! ");
	}

	/**
	 * 查询钱包余额
	 *
	 * @param address 钱包地址
	 */
	private void getBalance(String address) throws Exception {
		Blockchain blockchain = Blockchain.newBlockChain(address);
		// 得到公钥Hash值
		byte[] versionedPayload = Base58Check.base58ToBytes(address);
		byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
		TransactionOutput[] txOutputs = blockchain.findUTXO(pubKeyHash);
		int balance = 0;
		if (txOutputs != null && txOutputs.length > 0) {
			for (TransactionOutput txOutput : txOutputs) {
				balance += txOutput.getValue();
			}
		}
		System.out.printf("Balance of '%s': %d\n", address, balance);
	}

	/**
	 * 转账
	 *
	 * @param from
	 * @param to
	 * @param amount
	 */
	private void send(String from, String to, int amount) throws Exception {
		Blockchain blockchain = Blockchain.newBlockChain(from);
		//新交易
		Transaction transaction = Transaction.newTransaction(from, to, amount, blockchain);
		//奖励
		Transaction rewardTx = Transaction.newCoinbaseTransaction(from, "");
		Block newBlock = blockchain.mineBlock(new Transaction[]{transaction, rewardTx});
		new UTXOSet(blockchain).update(newBlock);
		RocksDBUtils.getInstance().closeDB();
		System.out.println("Success!");
	}

	/**
	 * 打印帮助信息
	 */
	private void help() {
		System.out.println("Usage:");
		System.out.println("  getbalance -address ADDRESS - Get balance of ADDRESS");
		System.out.println("  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
		System.out.println("  printchain - Print all the blocks of the blockchain");
		System.out.println("  send -from FROM -to TO -amount AMOUNT - Send AMOUNT of coins from FROM address to TO");
		System.exit(0);
	}

	/**
	 * 打印出区块链中的所有区块
	 */
	private void printChain() throws Exception {
		Blockchain blockchain = Blockchain.initBlockchainFromDB();
		for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
			Block block = iterator.next();
			if (block != null) {
				boolean validate = ProofOfWork.newProofOfWork(block).validate();
				System.out.println(block.toString() + ", validate = " + validate);
			}
		}
	}

	/**
	 * 创建钱包
	 *
	 * @throws Exception
	 */
	private void createWallet() throws Exception {
		Wallet wallet = WalletUtils.getInstance().createWallet();
		System.out.println("wallet address : " + wallet.getAddress());
	}

}

