package com.paymentgateway.commons.util;

import java.io.*;
import java.nio.*;

public abstract class CharacterDecoder
{
    protected abstract int bytesPerAtom();
    
    protected abstract int bytesPerLine();
    
    protected void decodeBufferPrefix(final PushbackInputStream aStream, final OutputStream bStream) throws IOException {
    }
    
    protected void decodeBufferSuffix(final PushbackInputStream aStream, final OutputStream bStream) throws IOException {
    }
    
    protected int decodeLinePrefix(final PushbackInputStream aStream, final OutputStream bStream) throws IOException {
        return this.bytesPerLine();
    }
    
    protected void decodeLineSuffix(final PushbackInputStream aStream, final OutputStream bStream) throws IOException {
    }
    
    protected void decodeAtom(final PushbackInputStream aStream, final OutputStream bStream, final int l) throws IOException {
        throw new CEStreamExhausted();
    }
    
    protected int readFully(final InputStream in, final byte[] buffer, final int offset, final int len) throws IOException {
        for (int i = 0; i < len; ++i) {
            final int q = in.read();
            if (q == -1) {
                return (i == 0) ? -1 : i;
            }
            buffer[i + offset] = (byte)q;
        }
        return len;
    }
    
    public void decodeBuffer(final InputStream aStream, final OutputStream bStream) throws IOException {
        int totalBytes = 0;
        final PushbackInputStream ps = new PushbackInputStream(aStream);
        this.decodeBufferPrefix(ps, bStream);
        try {
            while (true) {
                int length;
                int i;
                for (length = this.decodeLinePrefix(ps, bStream), i = 0; i + this.bytesPerAtom() < length; i += this.bytesPerAtom()) {
                    this.decodeAtom(ps, bStream, this.bytesPerAtom());
                    totalBytes += this.bytesPerAtom();
                }
                if (i + this.bytesPerAtom() == length) {
                    this.decodeAtom(ps, bStream, this.bytesPerAtom());
                    totalBytes += this.bytesPerAtom();
                }
                else {
                    this.decodeAtom(ps, bStream, length - i);
                    totalBytes += length - i;
                }
                this.decodeLineSuffix(ps, bStream);
            }
        }
        catch (CEStreamExhausted ceStreamExhausted) {
            this.decodeBufferSuffix(ps, bStream);
        }
    }
    
    public byte[] decodeBuffer(final String inputString) throws IOException {
        final byte[] inputBuffer = new byte[inputString.length()];
        inputString.getBytes(0, inputString.length(), inputBuffer, 0);
        final ByteArrayInputStream inStream = new ByteArrayInputStream(inputBuffer);
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        this.decodeBuffer(inStream, outStream);
        return outStream.toByteArray();
    }
    
    public byte[] decodeBuffer(final InputStream in) throws IOException {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        this.decodeBuffer(in, outStream);
        return outStream.toByteArray();
    }
    
    public ByteBuffer decodeBufferToByteBuffer(final String inputString) throws IOException {
        return ByteBuffer.wrap(this.decodeBuffer(inputString));
    }
    
    public ByteBuffer decodeBufferToByteBuffer(final InputStream in) throws IOException {
        return ByteBuffer.wrap(this.decodeBuffer(in));
    }
}