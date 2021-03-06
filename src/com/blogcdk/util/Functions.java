package com.blogcdk.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.startup.HomesUserDatabase;
import org.omg.CORBA.PUBLIC_MEMBER;

public class Functions {
	
	/**
	 * 获取md5
	 * @param message
	 * @return
	 */
	public static String getMd5(String message){
		String temp = "";
		try{
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			byte[] encodeMd5Digest = md5Digest.digest(message.getBytes());
			temp = convertByteToHexString(encodeMd5Digest);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}
	/**
	 * 转换md5工具
	 * @param bytes
	 * @return
	 */
	public static String convertByteToHexString(byte[] bytes){
		String result = "";
		for(int i=0;i<bytes.length;i++){
			int temp = bytes[i]&0xff;
			String tempHex = Integer.toHexString(temp);
			if(tempHex.length()<2){
				result +="0"+tempHex;
			}else{
				result +=tempHex;
			}
		}
		return result;
	}
	
	/*
	 * 根据email获取gravatar头像
	 */
    public static String getGravatar(String email) {
        String emailMd5 = getMd5(email);
        //设置图片大小32px
        String avatar = "https://s.gravatar.com/avatar/"+emailMd5;
        return avatar;
    }
    /**
     * 发送邮件工具..
     * @param email
     * @param code
     */
    public static void sendMail(String email,String code){
		//收信人的email
		String to = email;
		//邮件发送的email
		String from = "m15070398311@163.com";
		//主机名
		String host = "smtp.163.com";
		//创建连接对象，连接到邮箱服务器
		Properties properties = System.getProperties();
		//设置邮件服务器主机名
		properties.setProperty("mail.smtp.host", host);
		// 发送邮件协议名称
		properties.setProperty("mail.transport.protocol", "smtp");
		//发送服务器需要身份验证
		properties.put("mail.smtp.auth", "true");
		
		Session session  = Session.getDefaultInstance(properties,new  Authenticator() {
			
			public PasswordAuthentication getPasswordAuthentication()
	        {
				//发件人邮件用户名、密码
				return new PasswordAuthentication("m15070398311@163.com", "9533674599");
	        }
		});
		
		try {
			//创建邮件对象
			MimeMessage message = new MimeMessage(session);
			//设置自定义发件人昵称  
	        String mailName="CDK";  
	        try {  
	        	mailName=javax.mail.internet.MimeUtility.encodeText("Smile博客的投递员");  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
			//设置发件者
			message.setFrom(new InternetAddress(mailName+"<"+from+">"));
			//设置收件者
			message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));
			//设置邮件主题
			message.setSubject("欢迎您注册Smile博客!");
			//设置邮件的正文
//			message.setText("This is actual message");
			message.setContent("<h1>请点击<a href='http://127.0.0.1/Blog_cdk/activate?code="+code+"'>此链接</a>以激活账号</h1>", "text/html;charset=utf-8");
			//发送邮件
			Transport.send(message);
	        System.out.println("Sent message successfully....");
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    /**
     * 获取uuid
     * @return
     */
    public static String getCode(){
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}
    /**
     * 获取现在的时间..
     * @return
     */
    public static String getNowTime(){
    	//获取当前日期
		Date currentTime = new Date();
		//将日期转化为指定格式
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String getNowTimeString = formatter.format(currentTime);
    	return getNowTimeString;
    }
    /**
     * 获取用户ip地址
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request){
    	String ipAddress = null;
    	try {
			ipAddress = request.getHeader("x-forwarded-for");
			if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)){
				ipAddress = request.getHeader("Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
			// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                                                                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
		} catch (Exception e) {
			// TODO: handle exception
			ipAddress="";
		}
    	return ipAddress;
    }
    
    /**
     * 为了能更客观的统计一篇博文的阅读量而设计的防止重复刷新刷浏览量工具类
     * 即规定一个时间，在DAY天内，同一ip或同一账号对同一篇博客的浏览次数记为1
     * 具体实现：在数据库建立用户访问博客表主要有博文id 用户id 用户访问之ip地址 访问时间等
     * 每次运行本方法时先删除DAY天之前的数据（一方面是为了防止产生大量数据加重数据库负担，另一方面则便于统计），
     * 再遍历表查找是否重复出现访问ip或用户id，若出现返回true表示ip/id重复  反之为false
     * @param ip 用户的IP地址
     * @param userId 用户id
     * @return
     */
    public static boolean repeatIpOrUserId(String ip,int userId){
		//定义的间隔天数
    	final int DAY = 1;
    	return false;
    }
}