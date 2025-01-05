package pl.jakubkonkol.tasteitserver.model;

public class UserActivityInfo {
    private int actionCount;
    private boolean updateInProgress;

    public UserActivityInfo() {
        this.actionCount = 0;
        this.updateInProgress = false;
    }

    public synchronized void incrementCount() {
        this.actionCount++;
    }

    public synchronized boolean shouldTriggerUpdate(int threshold) {
        return !updateInProgress && actionCount >= threshold;
    }

    public synchronized void markUpdateInProgress() {
        this.updateInProgress = true;
    }

    public synchronized void reset() {
        this.actionCount = 0;
        this.updateInProgress = false;
    }

    public synchronized int getActionCount() {
        return actionCount;
    }

    public synchronized boolean isUpdateInProgress() {
        return updateInProgress;
    }
}
