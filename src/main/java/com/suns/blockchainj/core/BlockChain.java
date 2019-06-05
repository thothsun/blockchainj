package com.suns.blockchainj.core;

import com.suns.blockchainj.db.DBAccess;
import com.suns.blockchainj.encrypt.HashUtils;
import com.suns.blockchainj.encrypt.SignUtils;
import com.suns.blockchainj.event.MineBlockEvent;
import com.suns.blockchainj.event.SendTransactionEvent;
import com.suns.blockchainj.mine.Miner;
import com.suns.blockchainj.net.ApplicationContextProvider;
import com.suns.blockchainj.net.base.Node;
import com.suns.blockchainj.net.client.AppClient;
import com.suns.blockchainj.wallet.Account;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 区块链主类
 * @author yangjian
 * @since 18-4-6
 */
@Component
public class BlockChain {

	private static Logger logger = LoggerFactory.getLogger(BlockChain.class);

	@Autowired
	private DBAccess dbAccess;

	@Autowired
	private AppClient appClient;

	@Autowired
	private Miner miner;

	@Autowired
	private TransactionPool transactionPool;

	@Autowired
	private TransactionExecutor executor;
	/**
	 * 挖取一个区块
	 * @return
	 */
	public Block mining() throws Exception {

		Optional<Block> lastBlock = getLastBlock();
		Block block = miner.newBlock(lastBlock);
		transactionPool.getTransactions().forEach(e -> block.getBody().addTransaction(e));
		executor.run(block);
		//清空交易池
		transactionPool.clearTransactions();
		//存储区块
		dbAccess.putLastBlockIndex(block.getHeader().getIndex());
		dbAccess.putBlock(block);
		logger.info("Find a New Block, {}", block);

		//触发挖矿事件，并等待其他节点确认区块
		ApplicationContextProvider.publishEvent(new MineBlockEvent(block));
		return block;
	}

	/**
	 * 发送交易
	 * @param transaction
	 * @param privateKey 付款人私钥，用来签名交易
	 */
	public Transaction sendTransaction(Transaction transaction, String privateKey) throws Exception {

		//从数据库查询到用户的公钥
		Optional<Account> sender = dbAccess.getAccount(transaction.getSender());
		Optional<Account> recipient = dbAccess.getAccount(transaction.getRecipient());
		if (!sender.isPresent()) {
			throw new RuntimeException("付款人地址不存在");
		}
		if (!recipient.isPresent()) {
			throw new RuntimeException("收款人地址不存在");
		}
		transaction.setPublicKey(sender.get().getPublicKey());
		transaction.setTxHash(HashUtils.sha256Hex(transaction.toString()));
		//签名
		String sign = SignUtils.sign(privateKey, transaction.toString());
		transaction.setSign(sign);

		//先验证私钥是否正确
		if (!SignUtils.verify(sender.get().getPublicKey(), sign, transaction.toString())) {
			throw new RuntimeException("私钥签名验证失败，非法的私钥");
		}

		//打包数据到交易池
		transactionPool.addTransaction(transaction);

		//触发交易事件，向全网广播交易，并等待确认
		ApplicationContextProvider.publishEvent(new SendTransactionEvent(transaction));
		return transaction;
	}

	/**
	 * 获取最后一个区块
	 * @return
	 */
	public Optional<Block> getLastBlock() {
		return dbAccess.getLastBlock();
	}

	/**
	 * 添加一个节点
	 * @param ip
	 * @param port
	 * @return
	 */
	public void addNode(String ip, int port) throws Exception {

		appClient.addNode(ip, port);
		Node node = new Node(ip, port);
		dbAccess.addNode(node);
	}
}
