package net2.lingala.zip4j.model;

public class Zip64EndCentralDirLocator {
    private int noOfDiskStartOfZip64EndOfCentralDirRec;
    private long offsetZip64EndOfCentralDirRec;
    private long signature;
    private int totNumberOfDiscs;

    public long getSignature() {
        return this.signature;
    }

    public void setSignature(long signature) {
        this.signature = signature;
    }

    public int getNoOfDiskStartOfZip64EndOfCentralDirRec() {
        return this.noOfDiskStartOfZip64EndOfCentralDirRec;
    }

    public void setNoOfDiskStartOfZip64EndOfCentralDirRec(int noOfDiskStartOfZip64EndOfCentralDirRec) {
        this.noOfDiskStartOfZip64EndOfCentralDirRec = noOfDiskStartOfZip64EndOfCentralDirRec;
    }

    public long getOffsetZip64EndOfCentralDirRec() {
        return this.offsetZip64EndOfCentralDirRec;
    }

    public void setOffsetZip64EndOfCentralDirRec(long offsetZip64EndOfCentralDirRec) {
        this.offsetZip64EndOfCentralDirRec = offsetZip64EndOfCentralDirRec;
    }

    public int getTotNumberOfDiscs() {
        return this.totNumberOfDiscs;
    }

    public void setTotNumberOfDiscs(int totNumberOfDiscs) {
        this.totNumberOfDiscs = totNumberOfDiscs;
    }
}
