package edu.hnu.mail.data.source.remote;

import java.io.IOException;
import java.io.Writer;

public class DotTerminatedMessageWriter extends Writer {
    private static final int __NOTHING_SPECIAL_STATE = 0;
    private static final int __LAST_WAS_CR_STATE = 1;
    private static final int __LAST_WAS_NL_STATE = 2;

    private int __state;
    private Writer __output;


    /***
     * Creates a DotTerminatedMessageWriter that wraps an existing Writer
     * output destination.
     * <p>
     * @param output  The Writer output destination to write the message.
     ***/
    public DotTerminatedMessageWriter(Writer output)
    {
        super(output);
        __output = output;
        __state = __NOTHING_SPECIAL_STATE;
    }


    /***
     * Writes a character to the output.  Note that a call to this method
     * may result in multiple writes to the underling Writer in order to
     * convert naked linefeeds to NETASCII line separators and to double
     * line-leading periods.  This is transparent to the programmer and
     * is only mentioned for completeness.
     * <p>
     * @param ch  The character to write.
     * @exception IOException  If an error occurs while writing to the
     *            underlying output.
     ***/
    @Override
    public void write(int ch) throws IOException
    {
        synchronized (lock)
        {
            switch (ch)
            {
                case '\r':
                    __state = __LAST_WAS_CR_STATE;
                    __output.write('\r');
                    System.out.println("识别到\\r");
                    return ;
                case '\n':
                    if (__state != __LAST_WAS_CR_STATE)
                        __output.write('\r');
                    __output.write('\n');
                    System.out.println("识别到\\n");
                    __state = __LAST_WAS_NL_STATE;
                    return ;
                case '.':
                    // Double the dot at the beginning of a line
                    if (__state == __LAST_WAS_NL_STATE)
                        __output.write('.');
                    System.out.println("识别到 .");
                    // Fall through
                default:
                    __state = __NOTHING_SPECIAL_STATE;
                    __output.write(ch);
                    return ;
            }
        }
    }


    /***
     * Writes a number of characters from a character array to the output
     * starting from a given offset.
     * <p>
     * @param buffer  The character array to write.
     * @param offset  The offset into the array at which to start copying data.
     * @param length  The number of characters to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    @Override
    public void write(char[] buffer, int offset, int length) throws IOException
    {
        synchronized (lock)
        {
            while (length-- > 0)
                write(buffer[offset++]);
        }
    }


    /***
     * Writes a character array to the output.
     * <p>
     * @param buffer  The character array to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    @Override
    public void write(char[] buffer) throws IOException
    {
        write(buffer, 0, buffer.length);
    }


    /***
     * Writes a String to the output.
     * <p>
     * @param string  The String to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    @Override
    public void write(String string) throws IOException
    {
        write(string.toCharArray());
    }


    /***
     * Writes part of a String to the output starting from a given offset.
     * <p>
     * @param string  The String to write.
     * @param offset  The offset into the String at which to start copying data.
     * @param length  The number of characters to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    @Override
    public void write(String string, int offset, int length) throws IOException
    {
        write(string.toCharArray(), offset, length);
    }


    /***
     * Flushes the underlying output, writing all buffered output.
     * <p>
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    @Override
    public void flush() throws IOException
    {
        synchronized (lock)
        {
            __output.flush();
        }
    }


    /***
     * Flushes the underlying output, writing all buffered output, but doesn't
     * actually close the underlying stream.  The underlying stream may still
     * be used for communicating with the server and therefore is not closed.
     * <p>
     * @exception IOException If an error occurs while writing to the underlying
     *            output or closing the Writer.
     ***/
    @Override
    public void close() throws IOException
    {
        synchronized (lock)
        {
            if (__output == null)
                return ;

            if (__state == __LAST_WAS_CR_STATE)
                __output.write('\n');
            else if (__state != __LAST_WAS_NL_STATE)
                __output.write("\r\n");

            __output.write(".\r\n");

            __output.flush();
            __output = null;
        }
    }

}
