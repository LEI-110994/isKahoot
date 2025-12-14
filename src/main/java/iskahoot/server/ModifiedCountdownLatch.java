package iskahoot.server;

public class ModifiedCountdownLatch {
    private final int bonusFactor;
    private final int bonusCount;
    private final long waitPeriodMillis;
    private int count;
    private int currentBonusCount;

    public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitPeriod, int count) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitPeriodMillis = waitPeriod * 1000L;
        this.count = count;
        this.currentBonusCount = 0;
    }

    public synchronized int countdown() {
        if (count > 0) {
            count--;
            if (count == 0) {
                notifyAll();
            }
        }

        if (currentBonusCount < bonusCount) {
            currentBonusCount++;
            return bonusFactor;
        }
        return 1;
    }

    public synchronized void await() throws InterruptedException {
        long start = System.currentTimeMillis();
        long remaining = waitPeriodMillis;

        while (count > 0 && remaining > 0) {
            wait(remaining);
            remaining = waitPeriodMillis - (System.currentTimeMillis() - start);
        }
    }
}
