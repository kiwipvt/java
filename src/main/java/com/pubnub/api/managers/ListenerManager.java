package com.pubnub.api.managers;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class ListenerManager {

    private final List<SubscribeCallback> listeners;
    private final PubNub pubnub;

    public ListenerManager(PubNub pubnubInstance) {
        this.listeners = new ArrayList<>();
        this.pubnub = pubnubInstance;
    }

    public void addListener(SubscribeCallback listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(SubscribeCallback listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private List<SubscribeCallback> getListeners() {
        List<SubscribeCallback> tempCallbackList = new ArrayList<>();
        synchronized (listeners) {
            tempCallbackList.addAll(listeners);
        }
        return tempCallbackList;
    }

    /**
     * announce a PNStatus to listeners.
     *
     * @param status PNStatus which will be broadcast to listeners.
     */
    public void announce(final PNStatus status) {
        for (final SubscribeCallback subscribeCallback : getListeners()) {
            Executor executorToRunOn = getExecutorToRunOn(subscribeCallback);
            if (executorToRunOn != null) {
                executorToRunOn.execute(new Runnable() {
                    @Override
                    public void run() {
                        subscribeCallback.status(pubnub, status);
                    }
                });
            }
            else {
                subscribeCallback.status(this.pubnub, status);
            }
        }
    }

    public void announce(final PNMessageResult message) {
        for (final SubscribeCallback subscribeCallback : getListeners()) {
            Executor executorToRunOn = getExecutorToRunOn(subscribeCallback);
            if (executorToRunOn != null) {
                executorToRunOn.execute(new Runnable() {
                    @Override
                    public void run() {
                        subscribeCallback.message(pubnub, message);
                    }
                });
            }
            else {
                subscribeCallback.message(this.pubnub, message);
            }
        }
    }

    public void announce(final PNPresenceEventResult presence) {
        for (final SubscribeCallback subscribeCallback : getListeners()) {
            Executor executorToRunOn = getExecutorToRunOn(subscribeCallback);
            if (executorToRunOn != null) {
                executorToRunOn.execute(new Runnable() {
                    @Override
                    public void run() {
                        subscribeCallback.presence(pubnub, presence);
                    }
                });
            }
            else {
                subscribeCallback.presence(this.pubnub, presence);
            }
        }
    }

    private Executor getExecutorToRunOn(SubscribeCallback subscribeCallback) {
        Executor responseCallBackExecutor = pubnub.getConfiguration().getResponseCallbackExecutor();
        boolean isPubnubInternalCallback = subscribeCallback.getClass().getPackage().getName().startsWith("com.pubnub.api");
        return (responseCallBackExecutor != null && !isPubnubInternalCallback) ? responseCallBackExecutor : null;
    }

}
