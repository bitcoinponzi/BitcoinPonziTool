package it.unica.blockchain.bitcoin.parser.Model;

/**
 * Created by Ponzi on 25/07/2017.
 */
public class Triple<F, S, Z> {
    private F first; //first member of pair
    private S second; //second member of pair
    private Z third;

    public Triple(F first, S second, Z third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst() {
        return first;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public Z getThird() {
        return third;
    }

    public void setThird(Z third) {
        this.third = third;
    }
}
