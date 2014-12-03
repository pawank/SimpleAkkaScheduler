package notifications

import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.util.Timeout
import akka.pattern.ask
import akka.actor._
import com.typesafe.config._

import scala.concurrent.ExecutionContext.Implicits.global

object EfoundryConfig {
  val config:Config = ConfigFactory.load()
  def getValue(input:String) = config.getString(input)
}

object RunningEngine {
  def apply(systemNotificationActors: ActorRef) {
    implicit val timeout = Timeout(1 second)
    // Make the RunningEngine Inform every 30 seconds
  }
}

object SystemNotificationActors {
  implicit val timeout = Timeout(1 second)

  lazy val default = {
    val system = ActorSystem("SimpleSchedulerSystem")
    val alertsEnvActor = system.actorOf(Props[SystemNotificationActors], name = "AppActors")
    println("Started TestScheduler...")
    system.scheduler.schedule(
      0 milliseconds,
      5 seconds,
      alertsEnvActor,
      Inform("system", ""))
  }
}

class SystemNotificationActors extends Actor {
  def killRUNNING_PID = {
    val RUNNING_PID = EfoundryConfig.getValue("app.running.pid") match {
      case "" => "C:\\Test\\bin\\RUNNING_PID"
      case _ => EfoundryConfig.getValue("app.running.pid")
    }
    try {
    val f = new java.io.File(RUNNING_PID)
    if(f.exists() && !f.isDirectory()) { 
      println(s"Found RUNNING_PID at location - $RUNNING_PID")
      f.delete()
      println(s"Deleted RUNNING_PID at location - $RUNNING_PID")
    }
    true
    }catch {
      case e:Exception =>
        e.printStackTrace
        false
    }
  }


   def callUrlAndGetResult = {
import org.apache.commons.httpclient._
import org.apache.commons.httpclient.methods._
import org.apache.commons.httpclient.params.HttpMethodParams
import java.io._

	val url = EfoundryConfig.getValue("app.url")
     println(s"Calling url - $url")

// Create an instance of HttpClient.
    val client:HttpClient = new HttpClient()

    // Create a method instance.
    val method:GetMethod = new GetMethod(url)
    
    // Provide custom retry handler is necessary
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
    		new DefaultHttpMethodRetryHandler(3, false))

    try {
      // Execute the method.
      val statusCode = client.executeMethod(method)
      println(s"Found status code as - $statusCode for url - $url")

      if (statusCode != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + method.getStatusLine())
      }

      // Read the response body.
      val responseBody:Array[Byte] = method.getResponseBody()

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      val result = new String(responseBody)
      System.out.println(result)
      result.toLowerCase.contains("ok")
    } catch {
      case e:HttpException => 
      System.err.println("Fatal protocol violation: " + e.getMessage())
      e.printStackTrace()
	false
      case e:IOException =>
      System.err.println("Fatal transport error: " + e.getMessage())
      e.printStackTrace()
	false
      case _:Throwable => 	
      	System.err.println("Fatal Http Error")
        false
    } finally {
      // Release the connection.
      method.releaseConnection()
    }  
	
}


	def sendEmail = {
	val messageAsString = "Error in Starting Test SAP engine"
		try {
      val subject = s"InternalServerError Test from SAP Engine"
			val users = EfoundryConfig.getValue("app.admin.emails").split(",").toSeq
			in.efoundry.utils.EfoundryEmail.send(in.efoundry.utils.HtmlEmailMessage(to = users, subject = subject, body = messageAsString, from = users(0)))
			} catch {
				case e:Exception =>
				e.printStackTrace
				println(s"Test SAP Engine: SendEmail InternalServerError: ${e.getMessage} and full exception - $messageAsString")
			}
		}

  def receive = {
    case Inform(username, text) => {
      println("Sending message:" + text + " for the user:" + username)
      val checkAppRunning = callUrlAndGetResult
      checkAppRunning match {
	case true =>
       		println(s"Test application is running on main server..")
	 case false => 
		 killRUNNING_PID match {
        case true => 
           val base_folder = EfoundryConfig.getValue("base.folder")
	  val file_path = base_folder  + java.io.File.separator + "test.bat"
	  val inv_path = base_folder  + java.io.File.separator + "invisible.vbs"
          val cmd = s"""wscript.exe $inv_path $file_path"""
          println(cmd)
          Runtime.getRuntime().exec(cmd)
        case _ => println(s"ERROR while finding and removing old RUNNING_PID\n")
      		sendEmail
      }
      }
     
    }
  }
}

case class Inform(username: String, text: String)
