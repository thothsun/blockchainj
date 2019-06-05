package com.suns.blockchainj.listener;

import com.suns.blockchainj.event.NewAccountEvent;
import com.suns.blockchainj.net.base.MessagePacket;
import com.suns.blockchainj.net.base.MessagePacketType;
import com.suns.blockchainj.net.client.AppClient;
import com.suns.blockchainj.utils.SerializeUtils;
import com.suns.blockchainj.wallet.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 账户事件监听器
 * @author suns
 * @since 2018-04-21 下午12:02.
 */
@Component
public class AccountEventListener {

	private static Logger logger = LoggerFactory.getLogger(AccountEventListener.class);

	@Autowired
	private AppClient appClient;

	@EventListener(NewAccountEvent.class)
	public void newAccount(NewAccountEvent event) {

		Account account = (Account) event.getSource();
		logger.info("准备发起账户同步请求， {}", account);
		MessagePacket messagePacket = new MessagePacket();
		messagePacket.setType(MessagePacketType.REQ_NEW_ACCOUNT);
		messagePacket.setBody(SerializeUtils.serialize(account));
		appClient.sendGroup(messagePacket);
	}
}
