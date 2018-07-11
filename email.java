package com.GCH.mail.scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.GCH.mail.util.AppProperties;

public class SendMail {
	
	Logger log = Logger.getLogger(SendMail.class.getName());
	
	public boolean sendMail(DataSource fds, String message){
		boolean flag = false;
		
		log.debug("send mail - entry ");
		
		log.debug("SenderEmailAddress :"+AppProperties.getmailProperties().getSenderEmailAddress());
		log.debug("ReceiverEmailAddress :"+AppProperties.getmailProperties().getReceiverEmailAddress());
		log.debug("Subject :"+AppProperties.getmailProperties().getSubjectLine());
		log.debug("host :"+AppProperties.getmailProperties().getHost());
		log.debug("attachmentName :"+AppProperties.getmailProperties().getFileName());
		
		String senderEmaillAddress = AppProperties.getmailProperties().getSenderEmailAddress();
		String receiverEmailAddress = AppProperties.getmailProperties().getReceiverEmailAddress();
		String subjectLine = AppProperties.getmailProperties().getSubjectLine();
		String host = AppProperties.getmailProperties().getHost();
		String fileName = AppProperties.getmailProperties().getFileName();
		
		
		Properties mailProp = new Properties();
		mailProp.put("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(mailProp, null);
		try {
			Transport transport = session.getTransport("smtp");
			transport.connect();
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(senderEmaillAddress));
			List<String> receiverList = new ArrayList<String>();
			String[] rcp = receiverEmailAddress.split(",");
			receiverList = Arrays.asList(rcp);
			InternetAddress[] TheAddresses = multipleRecipients(receiverList);
			message.setRecipients(Message.RecipientType.TO, TheAddresses); 
			message.setSubject(subjectLine);
			BodyPart messageBodyPart = new MimeBodyPart();
			
			MimeMultipart multipart = new MimeMultipart();
			BodyPart attachPart = new MimeBodyPart();
			if(message != null && message != ""){
				
				attachPart.setDataHandler(new DataHandler(fds));
				attachPart.setFileName(fileName);
				multipart.addBodyPart(attachPart);
			}
			message.setContent(multipart);
			
			String bodyMessage = prepareBody(message);
			messageBodyPart.setContent(bodyMessage, "text/plain");
			
			multipart.addBodyPart(messageBodyPart);
			
			Transport.send(message);
			flag = true;
			log.debug("SendMail ------> Mail sent successfully");
		} catch (NoSuchProviderException e) {
			flag = false;
			log.error("NoSuchProviderException: "+e.getMessage());
			log.debug("SendMail ------> SMTP provider not found");
		} catch (MessagingException e) {
			flag = false;
			log.error("MessagingException: "+e.getMessage());
			log.debug("SendMail ------> InternetAddress parsing failed");
		} catch(Exception e){
			flag = false;
			log.error("Job failed. Sending mail failed.");
		} 
				
		return flag;
	}
	
	private static InternetAddress[] multipleRecipients(List<String> e_mails) throws AddressException {
			Iterator<String> e_mails_iter = e_mails.iterator();
			int counter = 0;
			InternetAddress[] int_address = new InternetAddress[e_mails.size()];
			while (e_mails_iter.hasNext()) {
				int_address[counter] = new InternetAddress((String) e_mails_iter.next());
				counter++;
			}
		return int_address;
	}
	
	private String prepareBody(String message){
		
		Date date= new Date();  
		SimpleDateFormat dtFrmt = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' hh:mm:ss");
		TimeZone estTime = TimeZone.getTimeZone("US/Eastern");
		dtFrmt.setTimeZone(estTime);
		String submitdate = dtFrmt.format(date);
		String bodytext="";
		if(message != null && message != ""){
			bodytext += "Attached is the message notifiction \n\n";
		}
				
		bodytext += "Report ran on Date: "+submitdate+ "\n";
		bodytext += "---------------------------------------------------------------------------------------------------- "+"\n";
		return bodytext;
	}

}
