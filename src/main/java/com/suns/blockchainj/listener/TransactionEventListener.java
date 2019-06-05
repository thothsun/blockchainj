package com.suns.blockchainj.listener;

import com.suns.blockchainj.core.Transaction;
import com.suns.blockchainj.event.SendTransactionEvent;
import com.suns.blockchainj.net.base.MessagePacket;
import com.suns.blockchainj.net.base.MessagePacketType;
import com.suns.blockchainj.net.client.AppClient;
import com.suns.blockchainj.utils.SerializeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 发送交易事件监听器
 * @author suns
 * @since 18-4-19
 */
@Component
public class TransactionEventListener {

	@Autowired
	private AppClient appClient;

	/**
	 * 向所有客户端广播交易
	 * @param event
	 */
	@EventListener(SendTransactionEvent.class)
	public void sendTransaction(SendTransactionEvent event) {

		Transaction transaction = (Transaction) event.getSource();
		MessagePacket messagePacket = new MessagePacket();
		messagePacket.setType(MessagePacketType.REQ_CONFIRM_TRANSACTION);
		messagePacket.setBody(SerializeUtils.serialize(transaction));
		appClient.sendGroup(messagePacket);
	}

}
