/*
 * Copyright (c) Rye Client 2024-2025.
 *
 * This file belongs to Rye Client,
 * an open-source Fabric injection client.
 * Rye GitHub: https://github.com/RyeClient/rye-v1.git
 *
 * THIS PROJECT DOES NOT HAVE A WARRANTY.
 *
 * Rye (and subsequently, its files) are all licensed under the MIT License.
 * Rye should have come with a copy of the MIT License.
 * If it did not, you may obtain a copy here:
 * MIT License: https://opensource.org/license/mit
 *
 */

package dev.thoq.event;

import dev.thoq.event.pool.EventSubscriberPool;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class EventBus {
    private List<IEventListener<?>> listeners = new ArrayList<>();

    public <T> void register(IEventListener<T> listener) {
        listeners.add(listener);
    }

    public <T> void dispatch(T event) {
        for(IEventListener<?> listener : listeners) {
            if(listener != null) {
                ((IEventListener<T>) listener).invoke(event);
            }
        }
    }

    private final EventSubscriberPool eventSubscriberPool;

    public EventBus() {
        this.eventSubscriberPool = new EventSubscriberPool();
    }

    public void subscribe(final Object subscriber) {
        eventSubscriberPool.subscribe(subscriber);
    }

    public void unsubscribe(final Object subscriber) {
        eventSubscriberPool.unsubscribe(subscriber);
    }

    public void dispatch(final Event event) {
        eventSubscriberPool.dispatch(event);
    }
}
