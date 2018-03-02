package it.unica.blockchain.bitcoin.parser.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Sergio Serusi on 16/10/2017.
 */
public class SendValues {
    private ArrayList<Long> arrayListBTCSent;
    private ArrayList<Double> arrayListUSDSent;
    private HashMap<Integer, Integer> sendFreq;
    private HashMap<Integer, Long> dailySentBTC;
    private HashMap<String, Long> sendToAddress;
    private Set<Long> timestampTransaction = new TreeSet<>();
    private double totalUSDSend;
    private int paid;

    public ArrayList<Long> getArrayListBTCSent() {
        return arrayListBTCSent;
    }

    public void setArrayListBTCSent(ArrayList<Long> arrayListBTCSent) {
        this.arrayListBTCSent = arrayListBTCSent;
    }

    public ArrayList<Double> getArrayListUSDSent() {
        return arrayListUSDSent;
    }

    public void setArrayListUSDSent(ArrayList<Double> arrayListUSDSent) {
        this.arrayListUSDSent = arrayListUSDSent;
    }

    public HashMap<Integer, Integer> getSendFreq() {
        return sendFreq;
    }

    public void setSendFreq(HashMap<Integer, Integer> sendFreq) {
        this.sendFreq = sendFreq;
    }

    public HashMap<Integer, Long> getDailySentBTC() {
        return dailySentBTC;
    }

    public void setDailySentBTC(HashMap<Integer, Long> dailySentBTC) {
        this.dailySentBTC = dailySentBTC;
    }

    public int getPaid() {
        return paid;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public double getTotalUSDSend() {
        return totalUSDSend;
    }

    public void setTotalUSDSend(double totalUSDSend) {
        this.totalUSDSend = totalUSDSend;
    }

    public HashMap<String, Long> getSendToAddress() {
        return sendToAddress;
    }

    public void setSendToAddress(HashMap<String, Long> sendToAddress) {
        this.sendToAddress = sendToAddress;
    }

    public Set<Long> getTimestampTransaction() {
        return timestampTransaction;
    }

    public void setTimestampTransaction(Set<Long> timestampTransaction) {
        this.timestampTransaction = timestampTransaction;
    }
}
