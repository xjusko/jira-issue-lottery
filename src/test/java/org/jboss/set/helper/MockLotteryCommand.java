package org.jboss.set.helper;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.jboss.set.LotteryCommand;

@ApplicationScoped
@Alternative
@Priority(1)
public class MockLotteryCommand extends LotteryCommand {
}
