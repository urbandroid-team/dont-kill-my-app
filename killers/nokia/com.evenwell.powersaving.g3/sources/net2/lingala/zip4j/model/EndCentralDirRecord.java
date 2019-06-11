package net2.lingala.zip4j.model;

public class EndCentralDirRecord {
    private String comment;
    private byte[] commentBytes;
    private int commentLength;
    private int noOfThisDisk;
    private int noOfThisDiskStartOfCentralDir;
    private long offsetOfStartOfCentralDir;
    private long signature;
    private int sizeOfCentralDir;
    private int totNoOfEntriesInCentralDir;
    private int totNoOfEntriesInCentralDirOnThisDisk;

    public long getSignature() {
        return this.signature;
    }

    public void setSignature(long signature) {
        this.signature = signature;
    }

    public int getNoOfThisDisk() {
        return this.noOfThisDisk;
    }

    public void setNoOfThisDisk(int noOfThisDisk) {
        this.noOfThisDisk = noOfThisDisk;
    }

    public int getNoOfThisDiskStartOfCentralDir() {
        return this.noOfThisDiskStartOfCentralDir;
    }

    public void setNoOfThisDiskStartOfCentralDir(int noOfThisDiskStartOfCentralDir) {
        this.noOfThisDiskStartOfCentralDir = noOfThisDiskStartOfCentralDir;
    }

    public int getTotNoOfEntriesInCentralDirOnThisDisk() {
        return this.totNoOfEntriesInCentralDirOnThisDisk;
    }

    public void setTotNoOfEntriesInCentralDirOnThisDisk(int totNoOfEntriesInCentralDirOnThisDisk) {
        this.totNoOfEntriesInCentralDirOnThisDisk = totNoOfEntriesInCentralDirOnThisDisk;
    }

    public int getTotNoOfEntriesInCentralDir() {
        return this.totNoOfEntriesInCentralDir;
    }

    public void setTotNoOfEntriesInCentralDir(int totNoOfEntrisInCentralDir) {
        this.totNoOfEntriesInCentralDir = totNoOfEntrisInCentralDir;
    }

    public int getSizeOfCentralDir() {
        return this.sizeOfCentralDir;
    }

    public void setSizeOfCentralDir(int sizeOfCentralDir) {
        this.sizeOfCentralDir = sizeOfCentralDir;
    }

    public long getOffsetOfStartOfCentralDir() {
        return this.offsetOfStartOfCentralDir;
    }

    public void setOffsetOfStartOfCentralDir(long offSetOfStartOfCentralDir) {
        this.offsetOfStartOfCentralDir = offSetOfStartOfCentralDir;
    }

    public int getCommentLength() {
        return this.commentLength;
    }

    public void setCommentLength(int commentLength) {
        this.commentLength = commentLength;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public byte[] getCommentBytes() {
        return this.commentBytes;
    }

    public void setCommentBytes(byte[] commentBytes) {
        this.commentBytes = commentBytes;
    }
}
