package org.ektorp.http;

import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class InputStreamBulkEntity extends AbstractHttpEntity {

    private static final byte[] BULK_HEADER_TRUE = "{\"all_or_nothing\":true,\"docs\":".getBytes(Charset.forName("UTF-8"));
    private static final byte[] BULK_HEADER_FALSE = "{\"all_or_nothing\":false,\"docs\":".getBytes(Charset.forName("UTF-8"));
    private static final byte[] BULK_FOOTER = "}".getBytes(Charset.forName("UTF-8"));

    private final static int DEFAULT_BUFFER_SIZE = 2048;

    private final InputStream inputStream;

    private final boolean allOrNothing;

    public InputStreamBulkEntity(InputStream inputStream, boolean allOrNothing) {
        this.inputStream = inputStream;
        this.allOrNothing = allOrNothing;
        setContentType("application/json");
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        if (allOrNothing) {
            outputStream.write(BULK_HEADER_TRUE);
        } else {
            outputStream.write(BULK_HEADER_FALSE);
        }

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int l;
        while ((l = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, l);
        }

        outputStream.write(BULK_FOOTER);
    }

    @Override
    public boolean isStreaming() {
        return true;
    }
}
