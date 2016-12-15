import java.util.*;

class SinghalToken implements java.io.Serializable {
    Integer[] TN;
    SinghalState[] TS;
    int size;
    boolean valid = true;

    SinghalToken(int size) {
        this.TN = new Integer[size];
        this.TS = new SinghalState[size];
        this.size = size;

        for (int i=0; i<size; i++) {
            this.TN[i] = 0;
            this.TS[i] = SinghalState.O;
        }
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return this.valid;
    }

    public void copy(SinghalToken copy) {
        Integer[] copyTN = copy.getTN();
        SinghalState[] copyTS = copy.getTS();
        if (this.size != copyTN.length) {
            System.out.println("Error: Wrong size!");
        }
        for (int i=0; i<this.size; i++) {
            this.TN[i] = copyTN[i];
            this.TS[i] = copyTS[i];
        }
    }

    public Integer[] getTN() {
        return this.TN;
    }

    public SinghalState[] getTS() {
        return this.TS;
    }
}
