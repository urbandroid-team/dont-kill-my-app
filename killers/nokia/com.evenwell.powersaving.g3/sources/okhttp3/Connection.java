package okhttp3;

import java.net.Socket;

public interface Connection {
    Handshake handshake();

    Protocol protocol();

    Route route();

    Socket socket();
}
