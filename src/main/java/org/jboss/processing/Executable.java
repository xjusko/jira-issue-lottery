package org.jboss.processing;

import org.jboss.config.LotteryConfig;

/**
 * Even if it is basically the same as {@code java.lang.Runnable} interface,
 * due to interference we need a separate interface mimicking it.
 */
public interface Executable {
    void execute(LotteryConfig lotteryConfig) throws Exception;
}
