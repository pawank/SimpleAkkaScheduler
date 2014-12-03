package in.efoundry.utils

import akka.actor._
import akka.actor.Actor

import org.apache.commons.mail.{HtmlEmail, DefaultAuthenticator, Email, SimpleEmail}
import java.util.Properties

object EfoundryEmail { 
  def send(msg:HtmlEmailMessage) = {
	val to = msg.to
	val from = msg.from
	val sub = msg.subject
	val body = msg.body
	val cc = msg.cc
	val bcc = msg.bcc

	val email:HtmlEmail = new HtmlEmail()
    email.setHostName(notifications.EfoundryConfig.getValue("smtp.host"))
    email.setSmtpPort(notifications.EfoundryConfig.config.getInt("smtp.port"))
    email.setAuthenticator(new DefaultAuthenticator(notifications.EfoundryConfig.getValue("smtp.user"), notifications.EfoundryConfig.getValue("smtp.password")))
    email.setTLS(true)
    println(s"Sending email to - $to")
    to.foreach(t =>     email.addTo(t))
    if (from.size > 0)
        email.setFrom(from)
    else
      email.setFrom(notifications.EfoundryConfig.getValue("smtp.from"), notifications.EfoundryConfig.getValue("smtp.sign"))
    email.setSubject(sub)
    email.setHtmlMsg(body)
    email.send()
    bcc.foreach(t => {
      val email:HtmlEmail = new HtmlEmail()
      email.setHostName(notifications.EfoundryConfig.getValue("smtp.host"))
      email.setSmtpPort(notifications.EfoundryConfig.getValue("smtp.port").toInt)
      email.setAuthenticator(new DefaultAuthenticator(notifications.EfoundryConfig.getValue("smtp.user"), notifications.EfoundryConfig.getValue("smtp.password")))
      email.setTLS(true)
      email.addTo(t)
      if (from.size > 0)
        email.setFrom(from)
      else
        email.setFrom(notifications.EfoundryConfig.getValue("smtp.from"), notifications.EfoundryConfig.getValue("smtp.sign"))
      email.setSubject(sub)
      email.setHtmlMsg(body)
      email.send()
      }
    )
	}
}

sealed trait EmailBaseMessage
case class EmailMessage(to:Seq[String],subject:String,body:String) extends EmailBaseMessage{
}
case class HtmlEmailMessage(to:Seq[String],subject:String,body:String, from:String, cc:Seq[String] = Seq.empty, bcc:Seq[String] = Seq.empty) extends EmailBaseMessage




