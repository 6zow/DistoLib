package ru.shizow.proxy.distributed;

import java.util.Collection;

/**
 * @author Max Gorbunov
 */
public interface DistributedAggregator<T> {
    T aggregate(Collection<T> collection);
}
