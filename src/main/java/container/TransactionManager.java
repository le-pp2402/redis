package container;

public class TransactionManager {
    public boolean calledMulti = false;

    public void setCalledMulti(boolean calledMulti) {
        this.calledMulti = calledMulti;
    }

    public boolean isCalledMulti() {
        return calledMulti;
    }
}
