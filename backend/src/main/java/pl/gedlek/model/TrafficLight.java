package pl.gedlek.model;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TrafficLight {
    private boolean isGreen;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition greenCondition = lock.newCondition();

    public TrafficLight(boolean startGreen) {
        this.isGreen = startGreen;
    }

    public void waitForGreen() {
        lock.lock();
        try {
            while (!isGreen) {
                greenCondition.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void toggleLight(boolean green) {
        lock.lock();
        try {
            this.isGreen = green;
            if (this.isGreen) {
                greenCondition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isGreen() {
        return isGreen;
    }
}
