package com.linkknown.iwork.core.node.mail;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.*;

@AutoRegistry
public class SendMailNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "mailConn_user", "发送邮箱服务器账号信息"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "mailConn_pass", "发送邮箱服务器密码或者授权码"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "mailConn_host", "发送邮箱服务器"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "mailConn_port", "发送端口"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "from_label?", "发送用户别名"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "mail_to", "被发送用户,多个用逗号分隔"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "subject", "邮件主题"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "body", "邮件正文"));
        return this.buildParamInputSchema(paramMetaList);
    }

    @Override
    public Param.ParamOutputSchema getDefaultParamOutputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta("flag", ""));
        paramMetaList.add(new ParamMeta("errorMsg", ""));
        return this.buildParamOutputSchema(paramMetaList);
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        // 需要存储的中间数据
        Map<String, Object> paramMap = new HashMap<>();

        String mailConn_user = (String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "mailConn_user");
        String mailConn_pass = (String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "mailConn_pass");
        String mailConn_host = (String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "mailConn_host");
        String mailConn_port = (String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "mailConn_port");
        String from_label = (String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "from_label?");
        String mail_to = (String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "mail_to");
        String subject = (String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "subject");
        String body = (String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "body");

        try {
            sendMail(mailConn_host, mailConn_user, mailConn_pass, from_label, subject,  mail_to, body);

            paramMap.put("flag", "SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();

            paramMap.put("flag", "FAILED");
            paramMap.put("errorMsg", e.getMessage());
        }
        // 将数据数据存储到数据中心
        this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), paramMap);
    }

    public static void sendMail(String mailHost, String mailUser, String mailPasswd, String from_label, String title, String mailTo, String content) throws MessagingException, UnsupportedEncodingException {

        Properties prop = new Properties();
        prop.put("mail.host", mailHost);
        prop.put("mail.transport.protocol", "smtp");
        prop.put("mail.smtp.auth", "true");
        //如果不加下面的这行代码 windows下正常，linux环境下发送失败，
        // 解决：http://www.cnblogs.com/Harold-Hua/p/7029117.html
        prop.setProperty("mail.smtp.ssl.enable", "true");

        //使用java发送邮件5步骤
        //1.创建sesssion
        Session session = Session.getInstance(prop);
        //开启session的调试模式，可以查看当前邮件发送状态
        //session.setDebug(true);

        //2.通过session获取Transport对象（发送邮件的核心API）
        Transport ts = session.getTransport();
        //3.通过邮件用户名密码链接，阿里云默认是开启个人邮箱pop3、smtp协议的，所以无需在阿里云邮箱里设置
        ts.connect(mailUser, mailPasswd);

        //4.创建邮件
        //创建邮件对象
        MimeMessage mm = new MimeMessage(session);
        //设置发件人
        mm.setFrom(new InternetAddress(mailUser, from_label, "UTF-8"));
        //设置收件人
        mm.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
        //设置抄送人
        //mm.setRecipient(Message.RecipientType.CC, new InternetAddress("XXXX@qq.com"));

        //mm.setSubject("吸引力注册邮件");
        mm.setSubject(title);

        //mm.setContent("您的注册验证码为:<b style=\"color:blue;\">0123</b>", "text/html;charset=utf-8");
        mm.setContent(content, "text/html;charset=utf-8");

        // true表示开始附件模式 -----------------------------------------------------------------------
        /*MimeMessageHelper messageHelper = new MimeMessageHelper(mm, true, "utf-8");
        // 设置收件人，寄件人
        messageHelper.setTo(email);
        messageHelper.setFrom(EMAIL_OWNER_ADDR);
        messageHelper.setSubject(title);
        // true 表示启动HTML格式的邮件
        messageHelper.setText(content, true);

        FileSystemResource file1 = new FileSystemResource(new File("d:/rongke.log"));
        FileSystemResource file2 = new FileSystemResource(new File("d:/新建文本文档.txt"));
        // 添加2个附件
        messageHelper.addAttachment("rongke.log", file1);
        try {
            //附件名有中文可能出现乱码
            messageHelper.addAttachment(MimeUtility.encodeWord("新建文本文档.txt"), file2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new MessagingException();
        }*/
        //-------------------------------------------------------------------------------------------

        //5.发送电子邮件
        ts.sendMessage(mm, mm.getAllRecipients());
    }
}
