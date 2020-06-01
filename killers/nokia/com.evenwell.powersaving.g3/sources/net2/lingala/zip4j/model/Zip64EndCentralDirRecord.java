package net2.lingala.zip4j.model;

public class Zip64EndCentralDirRecord {
    private byte[] extensibleDataSector;
    private int noOfThisDisk;
    private int noOfThisDiskStartOfCentralDir;
    private long offsetStartCenDirWRTStartDiskNo;
    private long signature;
    private long sizeOfCentralDir;
    private long sizeOfZip64EndCentralDirRec;
    private long totNoOfEntriesInCentralDir;
    private long totNoOfEntriesInCentralDirOnThisDisk;
    private int versionMadeBy;
    private int versionNeededToExtract;

    public long getSignature() {
        return this.signature;
    }

    public void setSignature(long signature) {
        this.signature = signature;
    }

    public long getSizeOfZip64EndCentralDirRec() {
        return this.sizeOfZip64EndCentralDirRec;
    }

    public void setSizeOfZip64EndCentralDirRec(long sizeOfZip64EndCentralDirRec) {
        this.sizeOfZip64EndCentralDirRec = sizeOfZip64EndCentralDirRec;
    }

    public int getVersionMadeBy() {
        return this.versionMadeBy;
    }

    public void setVersionMadeBy(int versionMadeBy) {
        this.versionMadeBy = versionMadeBy;
    }

    public int getVersionNeededToExtract() {
        return this.versionNeededToExtract;
    }

    public void setVersionNeededToExtract(int versionNeededToExtract) {
        this.versionNeededToExtract = versionNeededToExtract;
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

    public long getTotNoOfEntriesInCentralDirOnThisDisk() {
        return this.totNoOfEntriesInCentralDirOnThisDisk;
    }

    public void setTotNoOfEntriesInCentralDirOnThisDisk(long totNoOfEntriesInCentralDirOnThisDisk) {
        this.totNoOfEntriesInCentralDirOnThisDisk = totNoOfEntriesInCentralDirOnThisDisk;
    }

    public long getTotNoOfEntriesInCentralDir() {
        return this.totNoOfEntriesInCentralDir;
    }

    public void setTotNoOfEntriesInCentralDir(long totNoOfEntriesInCentralDir) {
        this.totNoOfEntriesInCentralDir = totNoOfEntriesInCentralDir;
    }

    public long getSizeOfCentralDir() {
        return this.sizeOfCentralDir;
    }

    public void setSizeOfCentralDir(long sizeOfCentralDir) {
        this.sizeOfCentralDir = sizeOfCentralDir;
    }

    public long getOffsetStartCenDirWRTStartDiskNo() {
        return this.offsetStartCenDirWRTStartDiskNo;
    }

    public void setOffsetStartCenDirWRTStartDiskNo(long offsetStartCenDirWRTStartDiskNo) {
        this.offsetStartCenDirWRTStartDiskNo = offsetStartCenDirWRTStartDiskNo;
    }

    public byte[] getExtensibleDataSector() {
        return this.extensibleDataSector;
    }

    public void setExtensibleDataSector(byte[] extensibleDataSector) {
        this.extensibleDataSector = extensibleDataSector;
    }
}
