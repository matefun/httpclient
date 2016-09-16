/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author d6788
 */
public class InputStreamCloner {
    private static final Logger LOG = LoggerFactory.getLogger(InputStreamCloner.class);

    private final InputStream _is;
    private final ByteArrayOutputStream _clone = new ByteArrayOutputStream();

    public InputStreamCloner(InputStream is) {
        _is = is;
        try {
            createClone();
        } catch (IOException ex) {
            LOG.error("Error creating clone ", ex);
        }
    }

    private int createClone() throws IOException {
        int read = 0;
        int chunk = 0;
        byte[] data = new byte[256];

        while (-1 != (chunk = _is.read(data))) {
            read += data.length;
            _clone.write(data, 0, chunk);
        }

        return read;
    }

    public InputStream getClone() {
        return (InputStream) new ByteArrayInputStream(_clone.toByteArray());
    }
}