package net2.lingala.zip4j.model;

public class UnzipParameters {
    private boolean ignoreAllFileAttributes;
    private boolean ignoreArchiveFileAttribute;
    private boolean ignoreDateTimeAttributes;
    private boolean ignoreHiddenFileAttribute;
    private boolean ignoreReadOnlyFileAttribute;
    private boolean ignoreSystemFileAttribute;

    public boolean isIgnoreReadOnlyFileAttribute() {
        return this.ignoreReadOnlyFileAttribute;
    }

    public void setIgnoreReadOnlyFileAttribute(boolean ignoreReadOnlyFileAttribute) {
        this.ignoreReadOnlyFileAttribute = ignoreReadOnlyFileAttribute;
    }

    public boolean isIgnoreHiddenFileAttribute() {
        return this.ignoreHiddenFileAttribute;
    }

    public void setIgnoreHiddenFileAttribute(boolean ignoreHiddenFileAttribute) {
        this.ignoreHiddenFileAttribute = ignoreHiddenFileAttribute;
    }

    public boolean isIgnoreArchiveFileAttribute() {
        return this.ignoreArchiveFileAttribute;
    }

    public void setIgnoreArchiveFileAttribute(boolean ignoreArchiveFileAttribute) {
        this.ignoreArchiveFileAttribute = ignoreArchiveFileAttribute;
    }

    public boolean isIgnoreSystemFileAttribute() {
        return this.ignoreSystemFileAttribute;
    }

    public void setIgnoreSystemFileAttribute(boolean ignoreSystemFileAttribute) {
        this.ignoreSystemFileAttribute = ignoreSystemFileAttribute;
    }

    public boolean isIgnoreAllFileAttributes() {
        return this.ignoreAllFileAttributes;
    }

    public void setIgnoreAllFileAttributes(boolean ignoreAllFileAttributes) {
        this.ignoreAllFileAttributes = ignoreAllFileAttributes;
    }

    public boolean isIgnoreDateTimeAttributes() {
        return this.ignoreDateTimeAttributes;
    }

    public void setIgnoreDateTimeAttributes(boolean ignoreDateTimeAttributes) {
        this.ignoreDateTimeAttributes = ignoreDateTimeAttributes;
    }
}
