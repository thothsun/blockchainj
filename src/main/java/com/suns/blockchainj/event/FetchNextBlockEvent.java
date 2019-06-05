package com.suns.blockchainj.event;

import org.springframework.context.ApplicationEvent;

/**
 * 当发起同步下一个区块请求的时候将会触发该事件
 * @author suns
 */
public class FetchNextBlockEvent extends ApplicationEvent {

    /**
     * @param blockIndex 区块高度
     */
    public FetchNextBlockEvent(Integer blockIndex) {
        super(blockIndex);
    }
}
