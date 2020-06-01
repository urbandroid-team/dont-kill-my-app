package okhttp3.internal.tls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

public abstract class TrustRootIndex {

    static final class AndroidTrustRootIndex extends TrustRootIndex {
        private final Method findByIssuerAndSignatureMethod;
        private final X509TrustManager trustManager;

        AndroidTrustRootIndex(X509TrustManager trustManager, Method findByIssuerAndSignatureMethod) {
            this.findByIssuerAndSignatureMethod = findByIssuerAndSignatureMethod;
            this.trustManager = trustManager;
        }

        public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
            X509Certificate x509Certificate = null;
            try {
                TrustAnchor trustAnchor = (TrustAnchor) this.findByIssuerAndSignatureMethod.invoke(this.trustManager, new Object[]{cert});
                if (trustAnchor != null) {
                    x509Certificate = trustAnchor.getTrustedCert();
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            } catch (InvocationTargetException e2) {
            }
            return x509Certificate;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AndroidTrustRootIndex)) {
                return false;
            }
            AndroidTrustRootIndex that = (AndroidTrustRootIndex) obj;
            if (this.trustManager.equals(that.trustManager) && this.findByIssuerAndSignatureMethod.equals(that.findByIssuerAndSignatureMethod)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.trustManager.hashCode() + (this.findByIssuerAndSignatureMethod.hashCode() * 31);
        }
    }

    static final class BasicTrustRootIndex extends TrustRootIndex {
        private final Map<X500Principal, Set<X509Certificate>> subjectToCaCerts = new LinkedHashMap();

        public BasicTrustRootIndex(X509Certificate... caCerts) {
            for (X509Certificate caCert : caCerts) {
                X500Principal subject = caCert.getSubjectX500Principal();
                Set<X509Certificate> subjectCaCerts = (Set) this.subjectToCaCerts.get(subject);
                if (subjectCaCerts == null) {
                    subjectCaCerts = new LinkedHashSet(1);
                    this.subjectToCaCerts.put(subject, subjectCaCerts);
                }
                subjectCaCerts.add(caCert);
            }
        }

        public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
            Set<X509Certificate> subjectCaCerts = (Set) this.subjectToCaCerts.get(cert.getIssuerX500Principal());
            if (subjectCaCerts == null) {
                return null;
            }
            for (X509Certificate caCert : subjectCaCerts) {
                try {
                    cert.verify(caCert.getPublicKey());
                    return caCert;
                } catch (Exception e) {
                }
            }
            return null;
        }

        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if ((other instanceof BasicTrustRootIndex) && ((BasicTrustRootIndex) other).subjectToCaCerts.equals(this.subjectToCaCerts)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.subjectToCaCerts.hashCode();
        }
    }

    public abstract X509Certificate findByIssuerAndSignature(X509Certificate x509Certificate);

    public static TrustRootIndex get(X509TrustManager trustManager) {
        try {
            Method method = trustManager.getClass().getDeclaredMethod("findTrustAnchorByIssuerAndSignature", new Class[]{X509Certificate.class});
            method.setAccessible(true);
            return new AndroidTrustRootIndex(trustManager, method);
        } catch (NoSuchMethodException e) {
            return get(trustManager.getAcceptedIssuers());
        }
    }

    public static TrustRootIndex get(X509Certificate... caCerts) {
        return new BasicTrustRootIndex(caCerts);
    }
}
