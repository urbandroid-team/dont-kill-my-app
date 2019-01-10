package net2.lingala.zip4j.progress;

import net2.lingala.zip4j.exception.ZipException;

public class ProgressMonitor {
    public static final int OPERATION_ADD = 0;
    public static final int OPERATION_CALC_CRC = 3;
    public static final int OPERATION_EXTRACT = 1;
    public static final int OPERATION_MERGE = 4;
    public static final int OPERATION_NONE = -1;
    public static final int OPERATION_REMOVE = 2;
    public static final int RESULT_CANCELLED = 3;
    public static final int RESULT_ERROR = 2;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_WORKING = 1;
    public static final int STATE_BUSY = 1;
    public static final int STATE_READY = 0;
    private boolean cancelAllTasks;
    private int currentOperation;
    private Throwable exception;
    private String fileName;
    private boolean pause;
    private int percentDone = 0;
    private int result;
    private int state;
    private long totalWork;
    private long workCompleted;

    public ProgressMonitor() {
        reset();
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getTotalWork() {
        return this.totalWork;
    }

    public void setTotalWork(long totalWork) {
        this.totalWork = totalWork;
    }

    public long getWorkCompleted() {
        return this.workCompleted;
    }

    public void updateWorkCompleted(long workCompleted) {
        this.workCompleted += workCompleted;
        this.percentDone = (int) ((this.workCompleted * 100) / this.totalWork);
        if (this.percentDone > 100) {
            this.percentDone = 100;
        }
        while (this.pause) {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
            }
        }
    }

    public int getPercentDone() {
        return this.percentDone;
    }

    public void setPercentDone(int percentDone) {
        this.percentDone = percentDone;
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getCurrentOperation() {
        return this.currentOperation;
    }

    public void setCurrentOperation(int currentOperation) {
        this.currentOperation = currentOperation;
    }

    public Throwable getException() {
        return this.exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public void endProgressMonitorSuccess() throws ZipException {
        reset();
        this.result = 0;
    }

    public void endProgressMonitorError(Throwable e) throws ZipException {
        reset();
        this.result = 2;
        this.exception = e;
    }

    public void reset() {
        this.currentOperation = -1;
        this.state = 0;
        this.fileName = null;
        this.totalWork = 0;
        this.workCompleted = 0;
    }

    public void fullReset() {
        reset();
        this.exception = null;
        this.result = 0;
        this.percentDone = 0;
    }

    public boolean isCancelAllTasks() {
        return this.cancelAllTasks;
    }

    public void cancelAllTasks() {
        this.cancelAllTasks = true;
    }

    public boolean isPause() {
        return this.pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }
}
