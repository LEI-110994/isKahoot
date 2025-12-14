package iskahoot.server;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomBarrier {
    private final int parties;
    private final Runnable barrierAction;
    private int count;
    private final Lock lock = new ReentrantLock();
    private final Condition trip = lock.newCondition();
    private boolean broken = false;

    public CustomBarrier(int parties, Runnable barrierAction) {
        this.parties = parties;
        this.count = parties;
        this.barrierAction = barrierAction;
    }

    public void await(long timeoutMillis) throws InterruptedException {
        lock.lock();
        try {
            if (broken) {
                // If already broken (timeout happened), just return
                return;
            }

            count--;
            if (count == 0) {
                // All parties arrived
                if (barrierAction != null) {
                    barrierAction.run();
                }
                trip.signalAll();
            } else {
                long nanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
                while (count > 0 && !broken) {
                    if (nanos <= 0) {
                        // Timeout
                        breakTimeout(); 
                        break;
                    }
                    nanos = trip.awaitNanos(nanos);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void breakTimeout() {
        broken = true;
        // Timeout happened, execute action anyway if needed?
        // Requirement says: "Se este tempo de facto expirar, todos as chamadas aos métodos await devem ser desbloqueadas, e o cálculo das pontuações deve ser feito através da funcionalidade barrierAction."
        if (barrierAction != null) {
            barrierAction.run();
        }
        trip.signalAll();
    }
    
    public void reset() {
        lock.lock();
        try {
            broken = false;
            count = parties;
        } finally {
            lock.unlock();
        }
    }
}
