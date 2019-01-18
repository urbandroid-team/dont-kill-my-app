package org.apache.commons.io.input;

public interface TailerListener {
    void fileNotFound();

    void fileRotated();

    void handle(Exception exception);

    void handle(String str);

    void init(Tailer tailer);
}
