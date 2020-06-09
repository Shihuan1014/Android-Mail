package edu.hnu.mail.data.source.remote;

import android.content.Context;
import android.renderscript.ScriptIntrinsicYuvToRGB;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import edu.hnu.mail.data.entity.Attachment;

/**
 * 设计思路：
 * 架构：因为客户端对性能要求较低，故不需要过多考虑多线程和并发，直接用Socket就行
 */
public class POP3Client extends POP3 {

    //单例模式
    public static String TAG = "POP3Client ";

    private Context context;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    //暂时仅使用本系统的用户
    private String separator = System.getProperty("line.separator");
    private boolean endOneMail = false;
    private String tmpBoundary = "";

    //连接信息
    private String username;

    //封掉构造函数
    private POP3Client(){

    }

    //封掉构造函数
    public POP3Client(Context context){
        this.context = context;
    }


    private static POP3MessageInfo parseStatus(String line)
    {
        int num, size;
        StringTokenizer tokenizer;

        tokenizer = new StringTokenizer(line);

        if (!tokenizer.hasMoreElements())
            return null;

        num = size = 0;

        try
        {
            num = Integer.parseInt(tokenizer.nextToken());

            if (!tokenizer.hasMoreElements())
                return null;

            size = Integer.parseInt(tokenizer.nextToken());
        }
        catch (NumberFormatException e)
        {
            return null;
        }

        return new POP3MessageInfo(num, size);
    }

    private static POP3MessageInfo parseUID(String line)
    {
        int num;
        StringTokenizer tokenizer;

        tokenizer = new StringTokenizer(line);

        if (!tokenizer.hasMoreElements())
            return null;

        num = 0;

        try
        {
            num = Integer.parseInt(tokenizer.nextToken());

            if (!tokenizer.hasMoreElements())
                return null;

            line = tokenizer.nextToken();
        }
        catch (NumberFormatException e)
        {
            return null;
        }

        return new POP3MessageInfo(num, line);
    }

    /**
     * 发送登录
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public boolean login(String username, String password) throws IOException
    {
        if (getState() != AUTHORIZATION_STATE)
            return false;

        if (sendCommand(POP3Command.USER, username) != POP3Reply.OK)
            return false;

        if (sendCommand(POP3Command.PASS, password) != POP3Reply.OK)
            return false;

        this.username = username;

        setState(TRANSACTION_STATE);

        return true;
    }

    public String getUsername(){
        return username;
    }

    public boolean logout() throws IOException
    {
        if (getState() == TRANSACTION_STATE)
            setState(UPDATE_STATE);
        sendCommand(POP3Command.QUIT);
        return (replyCode == POP3Reply.OK);
    }

    public boolean noop() throws IOException
    {
        if (getState() == TRANSACTION_STATE)
            return (sendCommand(POP3Command.NOOP) == POP3Reply.OK);
        return false;
    }

    public boolean deleteMessage(int messageId) throws IOException
    {
        if (getState() == TRANSACTION_STATE)
            return (sendCommand(POP3Command.DELE, Integer.toString(messageId))
                    == POP3Reply.OK);
        return false;
    }

    public POP3MessageInfo listMessage(int messageId) throws IOException
    {
        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.LIST, Integer.toString(messageId))
                != POP3Reply.OK)
            return null;
        return parseStatus(lastReplyLine.substring(3));
    }

    public POP3MessageInfo[] listMessages() throws IOException
    {
        POP3MessageInfo[] messages;
        Enumeration<String> en;
        int line;

        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.LIST) != POP3Reply.OK)
            return null;
        getAdditionalReply();


        messages = new POP3MessageInfo[replyLines.size() - 2];
        en = replyLines.elements();

        en.nextElement();

        for (line = 0; line < messages.length; line++)
            messages[line] = parseStatus(en.nextElement());

        return messages;
    }

    public POP3MessageInfo listUniqueIdentifier(int messageId)
            throws IOException
    {
        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.UIDL, Integer.toString(messageId))
                != POP3Reply.OK)
            return null;
        return parseUID(lastReplyLine.substring(3));
    }

    public POP3MessageInfo[] listUniqueIdentifiers() throws IOException
    {
        POP3MessageInfo[] messages;
        Enumeration<String> en;
        int line;

        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.UIDL) != POP3Reply.OK)
            return null;
        getAdditionalReply();

        messages = new POP3MessageInfo[replyLines.size() - 2];
        en = replyLines.elements();

        en.nextElement();

        for (line = 0; line < messages.length; line++)
            messages[line] = parseUID(en.nextElement());

        return messages;
    }

    public Reader retrieveMessage(int messageId) throws IOException
    {
        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.RETR, Integer.toString(messageId))
                != POP3Reply.OK)
            return null;
//        return new DotTerminatedMessageReader(reader);
        return reader;
    }

    public Reader retrieveMessageTop(int messageId, int numLines)
            throws IOException
    {
        if (numLines < 0 || getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.TOP, Integer.toString(messageId) + " " +
                Integer.toString(numLines)) != POP3Reply.OK)
            return null;

        return new DotTerminatedMessageReader(reader);
    }



    /** 从这里开始，和业务相关**/
    /**
     * 提供mailNo,获取邮件，分为几步
     * @param p
     * @param mailNo
     * @return 返回的是 mailFormat，包含headers,attachments, multipart,uid等
     * @throws Exception
     */
    public Mail retrMail(POP3Client p, int mailNo) throws Exception{
        Reader reader11 = p.retrieveMessage(mailNo);
        BufferedReader reader111 = new BufferedReader(reader11);
        String text;
        Mail mail = parseMail(p,reader111);
        return mail;
    }

    /**
     * 1、解析邮件
     * @param bufferedReader
     * @return
     * @throws Exception
     */
    public Mail parseMail(POP3Client p,BufferedReader bufferedReader) throws Exception {
        // Read header lines until a blank line. It is valid
        // to have BodyParts with no header lines.
        System.out.println(TAG+"开始parseMail");
        Mail mail = new Mail();
        Map<String,String> headers = new HashMap<String, String>();
        String line;
        String prevline = null;
        StringBuilder lineBuffer = new StringBuilder();
        String separator = System.getProperty("line.separator");
        //获得headers
        try {
            //while ((line = lis.readLine()) != null) {
            do {
                line = bufferedReader.readLine();
                if (line != null &&
                        (line.startsWith(" ") || line.startsWith("\t"))) {
                    // continuation of header
                    if (prevline != null) {
                        lineBuffer.append(prevline);
                        prevline = null;
                    }
                    lineBuffer.append(separator);
                    lineBuffer.append(line);
                } else {
                    // new header
                    if (prevline != null)
                        addHeaderLine(headers,prevline);
                    else if (lineBuffer.length() > 0) {
                        // store previous header first
                        addHeaderLine(headers,lineBuffer.toString());
                        lineBuffer.setLength(0);
                    }
                    prevline = line;
                }
            } while (line != null && !isEmpty(line));
        } catch (IOException ioex) {
            throw new MessagingException("Error in input stream", ioex);
        }
        mail.setHeader(headers);
        System.out.println(TAG+"解析到的headers: " + headers);
        String s = headers.get("Content-Type");
        // 没有Content-Type, 那就是纯文本，而且没加密，准备收工
        if(s==null){
            System.out.println("纯文本邮件");
            StringBuilder bodyBuilder = new StringBuilder();
            while ((line=bufferedReader.readLine()) != null){
                if(line.equalsIgnoreCase(".")){
                    break;
                }
                bodyBuilder.append(line);
                bodyBuilder.append(separator);
            }
            mail.setText(bodyBuilder.toString());
            return mail;
        }
        String contentTypeTmp = headers.get("Content-Type");
        int boundaryIndex = contentTypeTmp.indexOf("boundary");
        if (boundaryIndex > 0){
            //递归参数1
            String boundary = contentTypeTmp.substring(boundaryIndex+9);
            boundary = boundary.replace("\"","");
            System.out.println(boundary);
            //递归参数2
            MultiPart multiPart = new MultiPart();
            try {
                // 解析MIME格式的邮件, boundary为边界
                parsePart(boundary,bufferedReader,multiPart);
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("完成一封邮件读取");
            endOneMail = false;
            //1、解析附件
            mail.setAttachments(getAttachment(multiPart));
            //2、解析正文
            String content = null;
            String textContent = null;
            System.out.println(TAG+"开始解析正文：");
//            System.out.println(multiPart);
            for(MultiPart multiPart1 : multiPart.getParts()){
                String contentType = multiPart1.getContentType();
                System.out.println(contentType);
                System.out.println(multiPart1.getCharset());
                // 先看格式，是不是multipart
                if(contentType.startsWith("multipart")){
                    if(contentType.contains("related") || contentType.contains("mixed")){
                        String[] contents = parseRelatedMultipart(multiPart1);
                        content = contents[0];
                        textContent = contents[1];
                    }else if(contentType.contains("alternative")){
                        System.out.println(TAG+"没有related part, 故最上层就是alternative 加 内嵌资源");
                        String[] contents = parseRelatedMultipart(multiPart);
                        content = contents[0];
                        textContent = contents[1];
                    }
                }else if(contentType.startsWith("text")){
                    content = multiPart1.getContent();
                    System.out.println(TAG+"解码前：" + content);
                    String charset = multiPart1.getCharset().replace("\"","");
                    String encoding = multiPart1.getEncoding();
                    if (charset==null){
                        charset = "UTF-8";
                    }
                    System.out.println("POP3charset: " + charset);
                    System.out.println("POP3encoding: " + encoding);
                    if(encoding.contains("quoted")){
                        content = qpDecoding(content,charset);
                    }else if(encoding.contains("base64")){
                        Base64 base64 = new Base64();
                        content = new String(base64.decode(content.getBytes(charset)),charset);
                    }else{
                        content = new String((content.getBytes(charset)),charset);
                    }
                    textContent = content;
                    System.out.println(TAG+"解码后：" + content);
                }
            }
            multiPart.setContent(content);
            multiPart.setTextContent(textContent);
            mail.setMultiPart(multiPart);
        }else{
            //不是MIME类型邮件，直接解析文本即可
            StringBuilder bodyBuilder = new StringBuilder();
            while ((line=bufferedReader.readLine()) != null && !line.equalsIgnoreCase(".")){
                System.out.println("读取内容：" + line);
                bodyBuilder.append(line);
                bodyBuilder.append(separator);
            }
            System.out.println("读取普通文本完毕");
            String charset = null;
            // 解码
            Matcher matcher2 = Pattern.compile("charset=\"(.*)\"").matcher(contentTypeTmp);
            if (matcher2.find()){
                charset = matcher2.group(1);
            }else{
                matcher2 = Pattern.compile("charset=(.*)").matcher(s);
                if (matcher2.find()){
                    charset = matcher2.group(1);
                }
            }
            String encoding = headers.get("Content-Transfer-Encoding");
            String content = bodyBuilder.toString();
            if(encoding!=null){
                if(encoding.contains("quoted")){
                    content = qpDecoding(content,charset);
                }else if(encoding.contains("base64")){
                    Base64 base64 = new Base64();
                    content = new String(base64.decode(content.getBytes(charset)),charset);
                }else{
                    content = new String((content.getBytes(charset)),charset);
                }
            }
            mail.setText(content);
        }
        return mail;
    }

    /**
     * 1.1 解析header
     * @param reader
     * @return
     * @throws Exception
     */
    private List<String> getPartHeaders(BufferedReader reader) throws Exception{
        List<String> list = new ArrayList<String>();
        String line;
        StringBuilder lineBuffer = new StringBuilder();
        String prevline = null;
        do {
            //首先读一行
            line = reader.readLine();
            if (line.equalsIgnoreCase(".")){
                return null;
            }
            //这情况就是上一行没结束
            if (line != null &&
                    (line.startsWith(" ") || line.startsWith("\t"))) {
                // continuation of header
                if (prevline != null) {
                    lineBuffer.append(prevline);
                    prevline = null;
                }
                lineBuffer.append(separator);
                lineBuffer.append(line);
            } else {
                // new header
                //意味着新行的到来，那么上一行就结束了
                if (prevline != null){
                    list.add(prevline);
                }
                else if (lineBuffer.length() > 0) {
                    // store previous header first
                    list.add(lineBuffer.toString());
                    lineBuffer.setLength(0);
                }
                //把当前行存起来先
                prevline = line;
            }
        } while (line != null && !isEmpty(line));
        return list;
    }

    /**
     * 1.1.1 工具，增加header; 实际上就是把一行拆成key - value
     * @param headers
     * @param l
     */
    private void addHeaderLine(Map<String,String> headers,String l){
        int i = l.indexOf(':');
        if(i < 0){

        }else{
            headers.put(l.substring(0,i),l.substring(i+2));
        }
    }

    /**
     * 1.2 解析附件
     * @param multiPart
     * @return
     */
    private List<Attachment> getAttachment(MultiPart multiPart) throws IOException {
        List<Attachment> list = new ArrayList<Attachment>();
        // multipart提取完毕
        // 1、是否有附件
        if(multiPart.getHasAttachment() == 1){
            for(MultiPart tmp : multiPart.getParts()){
                System.out.println(tmp.getName());
                if(tmp.getIsAttachment() == 1){
                    if (tmp.getEncoding().contains("base64")){
//                           if(tmp.getContentType().contains("image"))
//                                System.out.println("附件内容是图片: image/jpeg;base64,"+tmp.getContent());
                        if(tmp.getContentType().contains("application/octet-stream")){
//                               System.out.println("附件内容是文件:"+tmp.getContent());
                            System.out.println("文件名: "+tmp.getName());
                            String path = tmp.getName();
                            decryptByBase64(tmp.getContent(),path);
                            Attachment attachment = new Attachment();
                            attachment.setPath(path);
                            attachment.setName(tmp.getName());
                            attachment.setSize(tmp.getContent().length());
                            list.add(attachment);
                        }
                    }
                }
            }
        }
        return list;
    }

    //1.3解析正文
    private MultiPart parsePart(String boundary,BufferedReader reader,MultiPart parent) throws Exception{
        System.out.println("当前part的boundary: " + boundary);
        MultiPart currentPart = new MultiPart();
        String line;
        if (endOneMail){
            return parent;
        }

        //如果tmpBoundary不等于当前boudary,表示还需要循环读行去找到这个boundary
        if(!tmpBoundary.startsWith("--" + boundary)) {
            //过滤掉无关的行，直到  --boundary
            while ((line = reader.readLine()) != null && !line.startsWith(("--" + boundary))) {
                if (line.equalsIgnoreCase(".")) {
                    System.out.println("整个邮件结束");
                    tmpBoundary = "";
                    return parent;
                }
            }
            if (line.equalsIgnoreCase("--" + boundary + "--")) {
                System.out.println(boundary + "结束");
                tmpBoundary = "";
                return parent;
            }
        }else if(tmpBoundary.equalsIgnoreCase("--"+boundary+"--")){
            tmpBoundary = "";
            return parent;
        }

        tmpBoundary = "";
        //找multipart头部
        List<String> headers = getPartHeaders(reader);
        boolean hasChild = false;
        String myBoundary = null;
        //检查头部
        for(String s : headers){
            System.out.println("Content-Type: " + s);
            if(s.startsWith("Content-Type")){
                Matcher matcher = Pattern.compile("Content-Type:\\s+(.*);").matcher(s);
                String contentType = "text/plain";
                if(matcher.find()){
                    contentType = matcher.group(1);
                    currentPart.setContentType(contentType);
                }
                System.out.println(contentType);
                if(contentType.startsWith("multipart")){
                    Matcher matcher2 = Pattern.compile("boundary=\"(.*)\"").matcher(s);
                    if (matcher2.find()){
                        hasChild = true;
                        myBoundary = matcher2.group(1);
                    }else{
                        System.out.println("没找到boundary");
                    }
                }else if(contentType.startsWith("text")){
                    Matcher matcher2 = Pattern.compile("charset=\"(.*)\"").matcher(s);
                    if (matcher2.find()){
                        currentPart.setCharset(matcher2.group(1));
                    }else{
                        matcher2 = Pattern.compile("charset=(.*)").matcher(s);
                        if (matcher2.find()){
                            currentPart.setCharset(matcher2.group(1));
                        }
                    }
                }
            }
            else if(s.startsWith("Content-Transfer")){
                int i = s.indexOf(":");
                currentPart.setEncoding(s.substring(i+1));
            }else if(s.startsWith("Content-Disposition")){
                if(s.contains("attachment")){
                    System.out.println("发现文件");
                    Matcher matcher = Pattern.compile("filename=\"=\\?(.*)\\?(\\S)\\?(.*)\\?=\"").matcher(s);
                    parent.setHasAttachment(1);
                    currentPart.setIsAttachment(1);
                    if(matcher.find()){
                        String charset = matcher.group(1);
                        String encoding = matcher.group(2);
                        String content = matcher.group(3);
                        Base64 base64 = new Base64();
                        System.out.println(charset + " "+encoding+" " + content);
                        if(encoding.equalsIgnoreCase("B")){
                            currentPart.setName(new String(base64.decode(content.getBytes(charset)),charset));
                        }
                    }else{
                        Matcher matcher2 = Pattern.compile("filename=\"(.*)\"").matcher(s);
                        if(matcher2.find()){
                            currentPart.setName(matcher2.group(1));
                        }
                    }
                }
            }else if(s.startsWith("Content-ID")){
                Matcher matcher2 = Pattern.compile("<(.*)>").matcher(s);
                if (matcher2.find()){
                    currentPart.setContentId(matcher2.group(1));
                }
            }
        }
        //如果没孩子，则读内容体
        if(!hasChild) {
            StringBuilder bodyBuilder = new StringBuilder();
            String s = System.getProperty("line.separator");
            boolean start = false;
            while ((line = reader.readLine()) != null) {
//                System.out.println("读取内容：" + line);
                if (line.equalsIgnoreCase(".")) {
                    //直接宣布终止该邮件，因为这是在一个multipart里面，碰到了 . 结束符，没什么好说的
                    endOneMail = true;
                    return parent;
                }
                //遇到boundary标识结束
                if (line.startsWith(("--" + boundary))) {
                    tmpBoundary = line;
                    break;
                }else{
                    bodyBuilder.append(line);
                    bodyBuilder.append(s);
                }
            }
            System.out.println("内容块读取完毕");
            currentPart.setContent(bodyBuilder.toString());
            parent.addPart(currentPart);
            currentPart.setParent(parent);
            parsePart(boundary,reader,parent);
        }else{
            //有子part，读子part
            System.out.println("读取子part: "+myBoundary);
            parsePart(myBoundary,reader,currentPart);
            parent.addPart(currentPart);
            currentPart.setParent(parent);
            //读完子part出来，应该继续读取当前part
            parsePart(boundary,reader,parent);
        }
        return parent;
    }

    /**
     * 1.3.1 解析 related类型的multipart，返回两个字符串textContent和htmlContent
     * @param related
     * @return
     */
    private String[] parseRelatedMultipart(MultiPart related) throws DecoderException, MessagingException, IOException {
        String[] contents = new String[2];
        Map<String,String> resources = new HashMap<String,String>();
        MultiPart alternative = null;
        for(MultiPart multiPart : related.getParts()){
            String contentType = multiPart.getContentType();
            // 图片内嵌资源
            if(contentType.equalsIgnoreCase("multipart/alternative")){
                alternative = multiPart;
            }else if(contentType.contains("image")){
                if(multiPart.getEncoding().contains("base64")){
                    String s = "data:image/jpeg;base64,"+multiPart.getContent();
                    resources.put(multiPart.getContentId(),s);
                }
            }
        }
        if(alternative!=null){
            related = alternative;
        }
        for(MultiPart multiPart : related.getParts()){
            String contentType = multiPart.getContentType();
            String content = multiPart.getContent();
            String charset = multiPart.getCharset().replace("\"","");
            if(charset == null){
                charset = "UTF-8" ;
            }
            System.out.println("POP3charset: " + charset);
            if(contentType.contains("html")){
                String encoding = multiPart.getEncoding();
                if(encoding!=null) {
                    if (encoding.contains("quoted")) {
                        content = qpDecoding(content, charset);
                    } else if (encoding.contains("base64")) {
                        Base64 base64 = new Base64();
                        content = new String(base64.decode(content.getBytes(charset)), charset);
                    } else {
                        content = new String((content.getBytes(charset)), charset);
                    }
                }
                System.out.println("替换前"+content);
                Iterator<Map.Entry<String, String>> entries = resources.entrySet().iterator();
                while(entries.hasNext()){
                    Map.Entry<String, String> entry = entries.next();
                    String key = "cid:"+entry.getKey();
                    if(charset.contains("quoted"))
                        key = qpDecoding(key,charset);
                    String value = entry.getValue();
                    System.out.println("resources Id :" + key);
                    content = content.replace(key,value);
                }
               System.out.println("替换后的html: "+ content);
                contents[0] = content;
            }else if(contentType.contains("plain")){
                String encoding = multiPart.getEncoding();
                if(encoding!=null) {
                    if (encoding.contains("quoted")) {
                        content = qpDecoding(content, charset);
                    } else if (encoding.contains("base64")) {
                        Base64 base64 = new Base64();
                        content = new String(base64.decode(content.getBytes(charset)), charset);
                    } else {
                        content = new String((content.getBytes(charset)), charset);
                    }
                }
                System.out.println("plain text: "+ contents[1]);
            }
        }
        return contents;
    }

    /**
     * 工具，解码quoted-printable
     * @param str
     * @return
     */
    public final String qpDecoding(String str,String charset) {
        if (str == null)
        {
            return "";
        }
        try
        {
            str = str.replaceAll("=\n", "");
            byte[] bytes = str.getBytes(charset);
//            for (int i = 0; i < bytes.length; i++)
//            {
//                byte b = bytes[i];
//                if (b != 95)
//                {
//                    bytes[i] = b;
//                }
//                else
//                {
//                    bytes[i] = 32;
//                }
//            }
            if (bytes == null)
            {
                return "";
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (int i = 0; i < bytes.length; i++)
            {
                int b = bytes[i];
                if (b == '=')
                {
                    try
                    {
                        int u = Character.digit((char) bytes[++i], 16);
                        int l = Character.digit((char) bytes[++i], 16);
                        if (u == -1 || l == -1)
                        {
                            continue;
                        }
                        buffer.write((char) ((u << 4) + l));
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    buffer.write(b);
                }
            }
            return new String(buffer.toByteArray(), charset);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 工具：解码base64的字符串，比如 =?GB2313?B?xxxxxxxx
     * @param mailText
     * @return
     * @throws UnsupportedEncodingException
     */
    public String decodeMailBase64(String mailText) throws UnsupportedEncodingException {
        Matcher matcher = Pattern.compile("=\\?(.*)\\?(\\S)\\?(.*)\\?=").matcher(mailText);
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()){
//            stringBuilder.append(matcher.group(1));
            String charset = matcher.group(1);
            String encoding = matcher.group(2);
            String content = matcher.group(3);
            Base64 base64 = new Base64();
            if(encoding.equalsIgnoreCase("B")){
                stringBuilder.append(new String(base64.decode(content.getBytes(charset)),charset));
            }else if(encoding.equalsIgnoreCase("Q")){
                stringBuilder.append(qpDecoding(content,charset));
            }
//            stringBuilder.append(matcher.group(5));
        }
        if(stringBuilder.toString().length() > 0){
            return stringBuilder.toString();
        }else {
            return mailText;
        }
    }

    /**
     * 工具：解码附件，并存储
     * @param base64
     * @param filePath
     * @return
     */
    private Boolean decryptByBase64(String base64, String filePath) throws IOException {
        if (base64 == null && filePath == null) {
            return Boolean.FALSE;
        }
        return save2File(filePath, android.util.Base64.decode(base64.getBytes(), android.util.Base64.DEFAULT));
    }

    /**
     * 工具：存储附件
     * @param fname
     * @param msg
     * @return
     */
    private boolean save2File(String fname, byte[] msg) throws IOException {
        File createFiles = new File(context.getFilesDir(), fname);
        createFiles.createNewFile();
        FileOutputStream fos = context.openFileOutput( fname, Context.MODE_PRIVATE );
        try{
            fos.write(msg);
            fos.flush();
            return true;
        }catch (FileNotFoundException e){
            return false;
        }catch (IOException e){
            return false;
        }
        finally{
            if (fos != null) {
                try{
                    fos.close();
                }catch (IOException e) {}
            }
        }
    }

    /**
     * 工具：检查是否为空行
     * @param line
     * @return
     */
    private static final boolean isEmpty(String line) {
        return line.length() == 0 ||
                ( line.trim().length() == 0);
    }
}
