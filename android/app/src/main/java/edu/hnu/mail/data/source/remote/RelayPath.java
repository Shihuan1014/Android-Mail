package edu.hnu.mail.data.source.remote;

import java.util.Enumeration;
import java.util.Vector;

/**
 * 用来封装多个发送者sender
 */
public final class RelayPath
{
    Vector<String> _path;
    String _emailAddress;

    /***
     * Create a relay path with the specified email address as the ultimate
     * destination.
     * <p>
     * @param emailAddress The destination email address.
     ***/
    public RelayPath(String emailAddress)
    {
        _path = new Vector<String>();
        _emailAddress = emailAddress;
    }

    /***
     * Add a mail relay host to the relay path.  Hosts are added left to
     * right.  For example, the following will create the path
     * <code><b> &lt @bar.com,@foo.com:foobar@foo.com &gt </b></code>
     * <pre>
     * path = new RelayPath("foobar@foo.com");
     * path.addRelay("bar.com");
     * path.addRelay("foo.com");
     * </pre>
     * <p>
     * @param hostname The host to add to the relay path.
     ***/
    public void addRelay(String hostname)
    {
        _path.addElement(hostname);
    }

    /***
     * Return the properly formatted string representation of the relay path.
     * <p>
     * @return The properly formatted string representation of the relay path.
     ***/
    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        Enumeration<String> hosts;

        buffer.append('<');

        hosts = _path.elements();

        if (hosts.hasMoreElements())
        {
            buffer.append('@');
            buffer.append(hosts.nextElement());

            while (hosts.hasMoreElements())
            {
                buffer.append(",@");
                buffer.append(hosts.nextElement());
            }
            buffer.append(':');
        }

        buffer.append(_emailAddress);
        buffer.append('>');

        return buffer.toString();
    }

}