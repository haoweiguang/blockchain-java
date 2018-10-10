package me.light.blockchain.core;

import com.google.common.collect.Lists;
import me.light.blockchain.util.ByteUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;

/**
 * 默克尔树
 *
 * @author light.hao
 * @create 2018-10-10-16:29
 */
public class MerkleTree {

	/**
	 * 根节点
	 */
	private Node root;

	/**
	 * 叶子节点hash
	 */
	private byte[][] leafHashes;


	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public byte[][] getLeafHashes() {
		return leafHashes;
	}

	public void setLeafHashes(byte[][] leafHashes) {
		this.leafHashes = leafHashes;
	}

	public MerkleTree(byte[][] leafHashes) {
		constructTree(leafHashes);
	}

	/**
	 * 从底部叶子节点开始往上构建整个Merkle Tree
	 *
	 * @param leafHashes
	 */
	private void constructTree(byte[][] leafHashes) {
		if (leafHashes == null && leafHashes.length == 0) {
			throw new RuntimeException("ERROR:Fail to construct merkle tree ! leafHashes data invalid ! ");
		}
		this.leafHashes = leafHashes;

	}

	/**
	 * 底部节点构建
	 *
	 * @param hashes
	 * @return
	 */
	private List<Node> bottomLevel(byte[][] hashes) {
		List<Node> parents = Lists.newArrayListWithCapacity(hashes.length / 2);

		for (int i = 0; i < hashes.length - 1; i += 2) {
			Node leaf1 = constructLeafNode(hashes[i]);
			Node leaf2 = constructLeafNode(hashes[i + 1]);

			Node parent = constructInternalNode(leaf1, leaf2);
			parents.add(parent);
		}

		if (hashes.length % 2 != 0) {
			Node leaf = constructLeafNode(hashes[hashes.length - 1]);
			// 奇数个节点的情况，复制最后一个节点
			Node parent = constructInternalNode(leaf, leaf);
			parents.add(parent);
		}

		return parents;
	}

	/**
	 * 构建叶子节点
	 *
	 * @param hash
	 * @return
	 */
	private static Node constructLeafNode(byte[] hash) {
		Node leaf = new Node();
		leaf.hash = hash;
		return leaf;
	}

	/**
	 * 构建内部节点
	 *
	 * @param leftChild
	 * @param rightChild
	 * @return
	 */
	private Node constructInternalNode(Node leftChild, Node rightChild) {
		Node parent = new Node();
		if (rightChild == null) {
			parent.hash = leftChild.hash;
		} else {
			parent.hash = internalHash(leftChild.hash, rightChild.hash);
		}
		parent.left = leftChild;
		parent.right = rightChild;
		return parent;
	}

	/**
	 * 计算内部节点Hash
	 *
	 * @param leftChildHash
	 * @param rightChildHash
	 * @return
	 */
	private byte[] internalHash(byte[] leftChildHash, byte[] rightChildHash) {
		byte[] mergedBytes = ByteUtils.merge(leftChildHash, rightChildHash);
		return DigestUtils.sha256(mergedBytes);
	}


	public static class Node {
		private byte[] hash;
		private Node left;
		private Node right;

		public byte[] getHash() {
			return hash;
		}

		public void setHash(byte[] hash) {
			this.hash = hash;
		}

		public Node getLeft() {
			return left;
		}

		public void setLeft(Node left) {
			this.left = left;
		}

		public Node getRight() {
			return right;
		}

		public void setRight(Node right) {
			this.right = right;
		}
	}
}
