package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;
import org.apache.commons.io.TaggedIOException;

public class TaggedOutputStream extends ProxyOutputStream {
    private final Serializable tag = UUID.randomUUID();

    public TaggedOutputStream(OutputStream proxy) {
        super(proxy);
    }

    public boolean isCauseOf(Exception exception) {
        return TaggedIOException.isTaggedWith(exception, this.tag);
    }

    public void throwIfCauseOf(Exception exception) throws IOException {
        TaggedIOException.throwCauseIfTaggedWith(exception, this.tag);
    }

    protected void handleIOException(IOException e) throws IOException {
        throw new TaggedIOException(e, this.tag);
    }
}
