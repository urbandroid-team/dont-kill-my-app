package net2.lingala.zip4j.model;

public class DataDescriptor {
    private int compressedSize;
    private String crc32;
    private int uncompressedSize;

    public String getCrc32() {
        return this.crc32;
    }

    public void setCrc32(String crc32) {
        this.crc32 = crc32;
    }

    public int getCompressedSize() {
        return this.compressedSize;
    }

    public void setCompressedSize(int compressedSize) {
        this.compressedSize = compressedSize;
    }

    public int getUncompressedSize() {
        return this.uncompressedSize;
    }

    public void setUncompressedSize(int uncompressedSize) {
        this.uncompressedSize = uncompressedSize;
    }
}
