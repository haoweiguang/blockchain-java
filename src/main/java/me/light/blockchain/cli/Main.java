package me.light.blockchain.cli;

public class Main {

	public static void main(String[] args) {
		args = new String[]{"createwallet"};
		CLI cli = new CLI(args);
		cli.parse();
	}
}
