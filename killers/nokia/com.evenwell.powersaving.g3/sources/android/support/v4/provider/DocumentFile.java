package android.support.v4.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import java.io.File;

public abstract class DocumentFile {
    static final String TAG = "DocumentFile";
    private final DocumentFile mParent;

    public abstract boolean canRead();

    public abstract boolean canWrite();

    public abstract DocumentFile createDirectory(String str);

    public abstract DocumentFile createFile(String str, String str2);

    public abstract boolean delete();

    public abstract boolean exists();

    public abstract String getName();

    public abstract String getType();

    public abstract Uri getUri();

    public abstract boolean isDirectory();

    public abstract boolean isFile();

    public abstract long lastModified();

    public abstract long length();

    public abstract DocumentFile[] listFiles();

    public abstract boolean renameTo(String str);

    DocumentFile(DocumentFile parent) {
        this.mParent = parent;
    }

    public static DocumentFile fromFile(File file) {
        return new RawDocumentFile(null, file);
    }

    public static DocumentFile fromSingleUri(Context context, Uri singleUri) {
        if (VERSION.SDK_INT >= 19) {
            return new SingleDocumentFile(null, context, singleUri);
        }
        return null;
    }

    public static DocumentFile fromTreeUri(Context context, Uri treeUri) {
        if (VERSION.SDK_INT >= 21) {
            return new TreeDocumentFile(null, context, DocumentsContractApi21.prepareTreeUri(treeUri));
        }
        return null;
    }

    public static boolean isDocumentUri(Context context, Uri uri) {
        if (VERSION.SDK_INT >= 19) {
            return DocumentsContractApi19.isDocumentUri(context, uri);
        }
        return false;
    }

    public DocumentFile getParentFile() {
        return this.mParent;
    }

    public DocumentFile findFile(String displayName) {
        for (DocumentFile doc : listFiles()) {
            if (displayName.equals(doc.getName())) {
                return doc;
            }
        }
        return null;
    }
}
