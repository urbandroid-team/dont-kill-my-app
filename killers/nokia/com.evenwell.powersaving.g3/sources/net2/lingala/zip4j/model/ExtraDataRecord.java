package net2.lingala.zip4j.model;

public class ExtraDataRecord {
    private byte[] data;
    private long header;
    private int sizeOfData;

    public long getHeader() {
        return this.header;
    }

    public void setHeader(long header) {
        this.header = header;
    }

    public int getSizeOfData() {
        return this.sizeOfData;
    }

    public void setSizeOfData(int sizeOfData) {
        this.sizeOfData = sizeOfData;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
