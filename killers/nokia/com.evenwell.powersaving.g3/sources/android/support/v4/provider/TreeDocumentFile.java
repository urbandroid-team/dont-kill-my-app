package android.support.v4.provider;

import android.content.Context;
import android.net.Uri;

class TreeDocumentFile extends DocumentFile {
    private Context mContext;
    private Uri mUri;

    TreeDocumentFile(DocumentFile parent, Context context, Uri uri) {
        super(parent);
        this.mContext = context;
        this.mUri = uri;
    }

    public DocumentFile createFile(String mimeType, String displayName) {
        Uri result = DocumentsContractApi21.createFile(this.mContext, this.mUri, mimeType, displayName);
        return result != null ? new TreeDocumentFile(this, this.mContext, result) : null;
    }

    public DocumentFile createDirectory(String displayName) {
        Uri result = DocumentsContractApi21.createDirectory(this.mContext, this.mUri, displayName);
        return result != null ? new TreeDocumentFile(this, this.mContext, result) : null;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public String getName() {
        return DocumentsContractApi19.getName(this.mContext, this.mUri);
    }

    public String getType() {
        return DocumentsContractApi19.getType(this.mContext, this.mUri);
    }

    public boolean isDirectory() {
        return DocumentsContractApi19.isDirectory(this.mContext, this.mUri);
    }

    public boolean isFile() {
        return DocumentsContractApi19.isFile(this.mContext, this.mUri);
    }

    public long lastModified() {
        return DocumentsContractApi19.lastModified(this.mContext, this.mUri);
    }

    public long length() {
        return DocumentsContractApi19.length(this.mContext, this.mUri);
    }

    public boolean canRead() {
        return DocumentsContractApi19.canRead(this.mContext, this.mUri);
    }

    public boolean canWrite() {
        return DocumentsContractApi19.canWrite(this.mContext, this.mUri);
    }

    public boolean delete() {
        return DocumentsContractApi19.delete(this.mContext, this.mUri);
    }

    public boolean exists() {
        return DocumentsContractApi19.exists(this.mContext, this.mUri);
    }

    public DocumentFile[] listFiles() {
        Uri[] result = DocumentsContractApi21.listFiles(this.mContext, this.mUri);
        DocumentFile[] resultFiles = new DocumentFile[result.length];
        for (int i = 0; i < result.length; i++) {
            resultFiles[i] = new TreeDocumentFile(this, this.mContext, result[i]);
        }
        return resultFiles;
    }

    public boolean renameTo(String displayName) {
        Uri result = DocumentsContractApi21.renameTo(this.mContext, this.mUri, displayName);
        if (result == null) {
            return false;
        }
        this.mUri = result;
        return true;
    }
}
