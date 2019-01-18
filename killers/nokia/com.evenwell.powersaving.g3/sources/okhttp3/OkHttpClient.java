package okhttp3;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call.Factory;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.cache.InternalCache;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.connection.RouteDatabase;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.platform.Platform;
import okhttp3.internal.tls.CertificateChainCleaner;
import okhttp3.internal.tls.OkHostnameVerifier;
import okhttp3.internal.ws.RealWebSocket;

public class OkHttpClient implements Cloneable, Factory, WebSocket.Factory {
    static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Util.immutableList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT);
    static final List<Protocol> DEFAULT_PROTOCOLS = Util.immutableList(Protocol.HTTP_2, Protocol.HTTP_1_1);
    final Authenticator authenticator;
    final Cache cache;
    final CertificateChainCleaner certificateChainCleaner;
    final CertificatePinner certificatePinner;
    final int connectTimeout;
    final ConnectionPool connectionPool;
    final List<ConnectionSpec> connectionSpecs;
    final CookieJar cookieJar;
    final Dispatcher dispatcher;
    final Dns dns;
    final EventListener.Factory eventListenerFactory;
    final boolean followRedirects;
    final boolean followSslRedirects;
    final HostnameVerifier hostnameVerifier;
    final List<Interceptor> interceptors;
    final InternalCache internalCache;
    final List<Interceptor> networkInterceptors;
    final int pingInterval;
    final List<Protocol> protocols;
    final Proxy proxy;
    final Authenticator proxyAuthenticator;
    final ProxySelector proxySelector;
    final int readTimeout;
    final boolean retryOnConnectionFailure;
    final SocketFactory socketFactory;
    final SSLSocketFactory sslSocketFactory;
    final int writeTimeout;

    /* renamed from: okhttp3.OkHttpClient$1 */
    class C00681 extends Internal {
        C00681() {
        }

        public void addLenient(okhttp3.Headers.Builder builder, String line) {
            builder.addLenient(line);
        }

        public void addLenient(okhttp3.Headers.Builder builder, String name, String value) {
            builder.addLenient(name, value);
        }

        public void setCache(Builder builder, InternalCache internalCache) {
            builder.setInternalCache(internalCache);
        }

        public boolean connectionBecameIdle(ConnectionPool pool, RealConnection connection) {
            return pool.connectionBecameIdle(connection);
        }

        public RealConnection get(ConnectionPool pool, Address address, StreamAllocation streamAllocation, Route route) {
            return pool.get(address, streamAllocation, route);
        }

        public boolean equalsNonHost(Address a, Address b) {
            return a.equalsNonHost(b);
        }

        public Socket deduplicate(ConnectionPool pool, Address address, StreamAllocation streamAllocation) {
            return pool.deduplicate(address, streamAllocation);
        }

        public void put(ConnectionPool pool, RealConnection connection) {
            pool.put(connection);
        }

        public RouteDatabase routeDatabase(ConnectionPool connectionPool) {
            return connectionPool.routeDatabase;
        }

        public int code(okhttp3.Response.Builder responseBuilder) {
            return responseBuilder.code;
        }

        public void apply(ConnectionSpec tlsConfiguration, SSLSocket sslSocket, boolean isFallback) {
            tlsConfiguration.apply(sslSocket, isFallback);
        }

        public HttpUrl getHttpUrlChecked(String url) throws MalformedURLException, UnknownHostException {
            return HttpUrl.getChecked(url);
        }

        public StreamAllocation streamAllocation(Call call) {
            return ((RealCall) call).streamAllocation();
        }

        public Call newWebSocketCall(OkHttpClient client, Request originalRequest) {
            return new RealCall(client, originalRequest, true);
        }
    }

    public static final class Builder {
        Authenticator authenticator;
        Cache cache;
        CertificateChainCleaner certificateChainCleaner;
        CertificatePinner certificatePinner;
        int connectTimeout;
        ConnectionPool connectionPool;
        List<ConnectionSpec> connectionSpecs;
        CookieJar cookieJar;
        Dispatcher dispatcher;
        Dns dns;
        EventListener.Factory eventListenerFactory;
        boolean followRedirects;
        boolean followSslRedirects;
        HostnameVerifier hostnameVerifier;
        final List<Interceptor> interceptors;
        InternalCache internalCache;
        final List<Interceptor> networkInterceptors;
        int pingInterval;
        List<Protocol> protocols;
        Proxy proxy;
        Authenticator proxyAuthenticator;
        ProxySelector proxySelector;
        int readTimeout;
        boolean retryOnConnectionFailure;
        SocketFactory socketFactory;
        SSLSocketFactory sslSocketFactory;
        int writeTimeout;

        public Builder() {
            this.interceptors = new ArrayList();
            this.networkInterceptors = new ArrayList();
            this.dispatcher = new Dispatcher();
            this.protocols = OkHttpClient.DEFAULT_PROTOCOLS;
            this.connectionSpecs = OkHttpClient.DEFAULT_CONNECTION_SPECS;
            this.eventListenerFactory = EventListener.factory(EventListener.NONE);
            this.proxySelector = ProxySelector.getDefault();
            this.cookieJar = CookieJar.NO_COOKIES;
            this.socketFactory = SocketFactory.getDefault();
            this.hostnameVerifier = OkHostnameVerifier.INSTANCE;
            this.certificatePinner = CertificatePinner.DEFAULT;
            this.proxyAuthenticator = Authenticator.NONE;
            this.authenticator = Authenticator.NONE;
            this.connectionPool = new ConnectionPool();
            this.dns = Dns.SYSTEM;
            this.followSslRedirects = true;
            this.followRedirects = true;
            this.retryOnConnectionFailure = true;
            this.connectTimeout = 10000;
            this.readTimeout = 10000;
            this.writeTimeout = 10000;
            this.pingInterval = 0;
        }

        Builder(OkHttpClient okHttpClient) {
            this.interceptors = new ArrayList();
            this.networkInterceptors = new ArrayList();
            this.dispatcher = okHttpClient.dispatcher;
            this.proxy = okHttpClient.proxy;
            this.protocols = okHttpClient.protocols;
            this.connectionSpecs = okHttpClient.connectionSpecs;
            this.interceptors.addAll(okHttpClient.interceptors);
            this.networkInterceptors.addAll(okHttpClient.networkInterceptors);
            this.eventListenerFactory = okHttpClient.eventListenerFactory;
            this.proxySelector = okHttpClient.proxySelector;
            this.cookieJar = okHttpClient.cookieJar;
            this.internalCache = okHttpClient.internalCache;
            this.cache = okHttpClient.cache;
            this.socketFactory = okHttpClient.socketFactory;
            this.sslSocketFactory = okHttpClient.sslSocketFactory;
            this.certificateChainCleaner = okHttpClient.certificateChainCleaner;
            this.hostnameVerifier = okHttpClient.hostnameVerifier;
            this.certificatePinner = okHttpClient.certificatePinner;
            this.proxyAuthenticator = okHttpClient.proxyAuthenticator;
            this.authenticator = okHttpClient.authenticator;
            this.connectionPool = okHttpClient.connectionPool;
            this.dns = okHttpClient.dns;
            this.followSslRedirects = okHttpClient.followSslRedirects;
            this.followRedirects = okHttpClient.followRedirects;
            this.retryOnConnectionFailure = okHttpClient.retryOnConnectionFailure;
            this.connectTimeout = okHttpClient.connectTimeout;
            this.readTimeout = okHttpClient.readTimeout;
            this.writeTimeout = okHttpClient.writeTimeout;
            this.pingInterval = okHttpClient.pingInterval;
        }

        public Builder connectTimeout(long timeout, TimeUnit unit) {
            this.connectTimeout = checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder readTimeout(long timeout, TimeUnit unit) {
            this.readTimeout = checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder writeTimeout(long timeout, TimeUnit unit) {
            this.writeTimeout = checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder pingInterval(long interval, TimeUnit unit) {
            this.pingInterval = checkDuration("interval", interval, unit);
            return this;
        }

        private static int checkDuration(String name, long duration, TimeUnit unit) {
            if (duration < 0) {
                throw new IllegalArgumentException(name + " < 0");
            } else if (unit == null) {
                throw new NullPointerException("unit == null");
            } else {
                long millis = unit.toMillis(duration);
                if (millis > 2147483647L) {
                    throw new IllegalArgumentException(name + " too large.");
                } else if (millis != 0 || duration <= 0) {
                    return (int) millis;
                } else {
                    throw new IllegalArgumentException(name + " too small.");
                }
            }
        }

        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder proxySelector(ProxySelector proxySelector) {
            this.proxySelector = proxySelector;
            return this;
        }

        public Builder cookieJar(CookieJar cookieJar) {
            if (cookieJar == null) {
                throw new NullPointerException("cookieJar == null");
            }
            this.cookieJar = cookieJar;
            return this;
        }

        void setInternalCache(InternalCache internalCache) {
            this.internalCache = internalCache;
            this.cache = null;
        }

        public Builder cache(Cache cache) {
            this.cache = cache;
            this.internalCache = null;
            return this;
        }

        public Builder dns(Dns dns) {
            if (dns == null) {
                throw new NullPointerException("dns == null");
            }
            this.dns = dns;
            return this;
        }

        public Builder socketFactory(SocketFactory socketFactory) {
            if (socketFactory == null) {
                throw new NullPointerException("socketFactory == null");
            }
            this.socketFactory = socketFactory;
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            if (sslSocketFactory == null) {
                throw new NullPointerException("sslSocketFactory == null");
            }
            X509TrustManager trustManager = Platform.get().trustManager(sslSocketFactory);
            if (trustManager == null) {
                throw new IllegalStateException("Unable to extract the trust manager on " + Platform.get() + ", sslSocketFactory is " + sslSocketFactory.getClass());
            }
            this.sslSocketFactory = sslSocketFactory;
            this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
            if (sslSocketFactory == null) {
                throw new NullPointerException("sslSocketFactory == null");
            } else if (trustManager == null) {
                throw new NullPointerException("trustManager == null");
            } else {
                this.sslSocketFactory = sslSocketFactory;
                this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
                return this;
            }
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            if (hostnameVerifier == null) {
                throw new NullPointerException("hostnameVerifier == null");
            }
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder certificatePinner(CertificatePinner certificatePinner) {
            if (certificatePinner == null) {
                throw new NullPointerException("certificatePinner == null");
            }
            this.certificatePinner = certificatePinner;
            return this;
        }

        public Builder authenticator(Authenticator authenticator) {
            if (authenticator == null) {
                throw new NullPointerException("authenticator == null");
            }
            this.authenticator = authenticator;
            return this;
        }

        public Builder proxyAuthenticator(Authenticator proxyAuthenticator) {
            if (proxyAuthenticator == null) {
                throw new NullPointerException("proxyAuthenticator == null");
            }
            this.proxyAuthenticator = proxyAuthenticator;
            return this;
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            if (connectionPool == null) {
                throw new NullPointerException("connectionPool == null");
            }
            this.connectionPool = connectionPool;
            return this;
        }

        public Builder followSslRedirects(boolean followProtocolRedirects) {
            this.followSslRedirects = followProtocolRedirects;
            return this;
        }

        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        public Builder dispatcher(Dispatcher dispatcher) {
            if (dispatcher == null) {
                throw new IllegalArgumentException("dispatcher == null");
            }
            this.dispatcher = dispatcher;
            return this;
        }

        public Builder protocols(List<Protocol> protocols) {
            List<Protocol> protocols2 = new ArrayList(protocols);
            if (!protocols2.contains(Protocol.HTTP_1_1)) {
                throw new IllegalArgumentException("protocols doesn't contain http/1.1: " + protocols2);
            } else if (protocols2.contains(Protocol.HTTP_1_0)) {
                throw new IllegalArgumentException("protocols must not contain http/1.0: " + protocols2);
            } else if (protocols2.contains(null)) {
                throw new IllegalArgumentException("protocols must not contain null");
            } else {
                if (protocols2.contains(Protocol.SPDY_3)) {
                    protocols2.remove(Protocol.SPDY_3);
                }
                this.protocols = Collections.unmodifiableList(protocols2);
                return this;
            }
        }

        public Builder connectionSpecs(List<ConnectionSpec> connectionSpecs) {
            this.connectionSpecs = Util.immutableList((List) connectionSpecs);
            return this;
        }

        public List<Interceptor> interceptors() {
            return this.interceptors;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public List<Interceptor> networkInterceptors() {
            return this.networkInterceptors;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            this.networkInterceptors.add(interceptor);
            return this;
        }

        Builder eventListener(EventListener eventListener) {
            if (eventListener == null) {
                throw new NullPointerException("eventListener == null");
            }
            this.eventListenerFactory = EventListener.factory(eventListener);
            return this;
        }

        Builder eventListenerFactory(EventListener.Factory eventListenerFactory) {
            if (eventListenerFactory == null) {
                throw new NullPointerException("eventListenerFactory == null");
            }
            this.eventListenerFactory = eventListenerFactory;
            return this;
        }

        public OkHttpClient build() {
            return new OkHttpClient(this);
        }
    }

    static {
        Internal.instance = new C00681();
    }

    public OkHttpClient() {
        this(new Builder());
    }

    OkHttpClient(Builder builder) {
        this.dispatcher = builder.dispatcher;
        this.proxy = builder.proxy;
        this.protocols = builder.protocols;
        this.connectionSpecs = builder.connectionSpecs;
        this.interceptors = Util.immutableList(builder.interceptors);
        this.networkInterceptors = Util.immutableList(builder.networkInterceptors);
        this.eventListenerFactory = builder.eventListenerFactory;
        this.proxySelector = builder.proxySelector;
        this.cookieJar = builder.cookieJar;
        this.cache = builder.cache;
        this.internalCache = builder.internalCache;
        this.socketFactory = builder.socketFactory;
        boolean isTLS = false;
        for (ConnectionSpec spec : this.connectionSpecs) {
            isTLS = isTLS || spec.isTls();
        }
        if (builder.sslSocketFactory == null && isTLS) {
            X509TrustManager trustManager = systemDefaultTrustManager();
            this.sslSocketFactory = systemDefaultSslSocketFactory(trustManager);
            this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
        } else {
            this.sslSocketFactory = builder.sslSocketFactory;
            this.certificateChainCleaner = builder.certificateChainCleaner;
        }
        this.hostnameVerifier = builder.hostnameVerifier;
        this.certificatePinner = builder.certificatePinner.withCertificateChainCleaner(this.certificateChainCleaner);
        this.proxyAuthenticator = builder.proxyAuthenticator;
        this.authenticator = builder.authenticator;
        this.connectionPool = builder.connectionPool;
        this.dns = builder.dns;
        this.followSslRedirects = builder.followSslRedirects;
        this.followRedirects = builder.followRedirects;
        this.retryOnConnectionFailure = builder.retryOnConnectionFailure;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.pingInterval = builder.pingInterval;
    }

    private X509TrustManager systemDefaultTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length == 1 && (trustManagers[0] instanceof X509TrustManager)) {
                return (X509TrustManager) trustManagers[0];
            }
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        } catch (GeneralSecurityException e) {
            throw new AssertionError();
        }
    }

    private SSLSocketFactory systemDefaultSslSocketFactory(X509TrustManager trustManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new AssertionError();
        }
    }

    public int connectTimeoutMillis() {
        return this.connectTimeout;
    }

    public int readTimeoutMillis() {
        return this.readTimeout;
    }

    public int writeTimeoutMillis() {
        return this.writeTimeout;
    }

    public int pingIntervalMillis() {
        return this.pingInterval;
    }

    public Proxy proxy() {
        return this.proxy;
    }

    public ProxySelector proxySelector() {
        return this.proxySelector;
    }

    public CookieJar cookieJar() {
        return this.cookieJar;
    }

    public Cache cache() {
        return this.cache;
    }

    InternalCache internalCache() {
        return this.cache != null ? this.cache.internalCache : this.internalCache;
    }

    public Dns dns() {
        return this.dns;
    }

    public SocketFactory socketFactory() {
        return this.socketFactory;
    }

    public SSLSocketFactory sslSocketFactory() {
        return this.sslSocketFactory;
    }

    public HostnameVerifier hostnameVerifier() {
        return this.hostnameVerifier;
    }

    public CertificatePinner certificatePinner() {
        return this.certificatePinner;
    }

    public Authenticator authenticator() {
        return this.authenticator;
    }

    public Authenticator proxyAuthenticator() {
        return this.proxyAuthenticator;
    }

    public ConnectionPool connectionPool() {
        return this.connectionPool;
    }

    public boolean followSslRedirects() {
        return this.followSslRedirects;
    }

    public boolean followRedirects() {
        return this.followRedirects;
    }

    public boolean retryOnConnectionFailure() {
        return this.retryOnConnectionFailure;
    }

    public Dispatcher dispatcher() {
        return this.dispatcher;
    }

    public List<Protocol> protocols() {
        return this.protocols;
    }

    public List<ConnectionSpec> connectionSpecs() {
        return this.connectionSpecs;
    }

    public List<Interceptor> interceptors() {
        return this.interceptors;
    }

    public List<Interceptor> networkInterceptors() {
        return this.networkInterceptors;
    }

    EventListener.Factory eventListenerFactory() {
        return this.eventListenerFactory;
    }

    public Call newCall(Request request) {
        return new RealCall(this, request, false);
    }

    public WebSocket newWebSocket(Request request, WebSocketListener listener) {
        RealWebSocket webSocket = new RealWebSocket(request, listener, new Random());
        webSocket.connect(this);
        return webSocket;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }
}
