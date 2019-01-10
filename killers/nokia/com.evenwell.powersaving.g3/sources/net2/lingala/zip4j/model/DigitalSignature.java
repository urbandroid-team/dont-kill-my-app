package net2.lingala.zip4j.model;

public class DigitalSignature {
    private int headerSignature;
    private String signatureData;
    private int sizeOfData;

    public int getHeaderSignature() {
        return this.headerSignature;
    }

    public void setHeaderSignature(int headerSignature) {
        this.headerSignature = headerSignature;
    }

    public int getSizeOfData() {
        return this.sizeOfData;
    }

    public void setSizeOfData(int sizeOfData) {
        this.sizeOfData = sizeOfData;
    }

    public String getSignatureData() {
        return this.signatureData;
    }

    public void setSignatureData(String signatureData) {
        this.signatureData = signatureData;
    }
}
