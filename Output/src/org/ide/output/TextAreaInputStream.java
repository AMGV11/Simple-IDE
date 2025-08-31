package org.ide.output;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Clase encargada del stream de input que van a consola.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class TextAreaInputStream extends InputStream {
    private final Queue<Integer> buffer = new LinkedList<>();
    private volatile boolean closed = false;

    public synchronized void appendText(String text) {
        if (closed) return;
        
        String fullText = text + "\n";
        for (char c : fullText.toCharArray()) {
            buffer.add((int) c);
        }
        notifyAll();
    }

    @Override
    public synchronized int read() throws IOException {
        while (buffer.isEmpty() && !closed) {
            try {
                wait(100); // Timeout para evitar esperas infinitas
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted", e);
            }
        }
        
        if (buffer.isEmpty() && closed) {
            return -1;
        }
        
        Integer result = buffer.poll();
        return result != null ? result : -1;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int bytesRead = 0;
        while (bytesRead < len) {
            int nextByte = read();
            if (nextByte == -1) {
                return bytesRead == 0 ? -1 : bytesRead;
            }
            b[off + bytesRead] = (byte) nextByte;
            bytesRead++;
            
            if (buffer.isEmpty()) {
                break;
            }
        }
        
        return bytesRead;
    }

    @Override
    public int available() throws IOException {
        return buffer.size();
    }

    @Override
    public synchronized void close() throws IOException {
        closed = true;
        notifyAll();
        super.close();
    }

    public synchronized void clearBuffer() {
        buffer.clear();
        notifyAll();
    }
}