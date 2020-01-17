package com.tangyb.ReactNativeBluetooth;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BufferQueue {
    private Queue<byte[]> queue = new LinkedList<>();
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Lock rlock = rwlock.readLock();
    private final Lock wlock = rwlock.writeLock();
    private boolean canParseData = false;
    private boolean canParseCommand = false;


    public synchronized void addTask(byte[] s) {
        this.queue.add(s);
        this.notifyAll();
    }

    public synchronized byte[] getTask() throws InterruptedException {
        while (queue.isEmpty()) {
            this.wait();
        }
        return queue.remove();
    }

    public boolean getCanParseData() {
        rlock.lock(); // 加读锁
        try {
            return canParseData;
        } finally {
            rlock.unlock(); // 释放读锁
        }
    }

    public boolean getCanParseCommand() {
        rlock.lock(); // 加读锁
        try {
            return canParseCommand;
        } finally {
            rlock.unlock(); // 释放读锁
        }
    }

    public void setCanParseData(boolean state) {
        wlock.lock(); // 加写锁
        try {
            canParseData = state;
        } finally {
            wlock.unlock(); // 释放写锁
        }
    }


    public void setCanParseCommand(boolean state) {
        wlock.lock(); // 加写锁
        try {
            canParseCommand = state;
        } finally {
            wlock.unlock(); // 释放写锁
        }
    }
}
