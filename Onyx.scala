package onyx

import scala.language.postfixOps
import scala.sys.process._
import javax.swing.{ JFrame, WindowConstants }
import java.applet.{ Applet, AppletStub }
import java.net.{ URL, URLClassLoader }
import java.awt.Dimension

object Onyx extends App {
	System.setProperty("user.home", "/home/boar/.jagex")

	private val gamepack = new Gamepack("gamepack.jar")
	gamepack.inject(
		List(new ClassHook("Client", "client")),
		List(new FieldHook("canvas", "aj", "an"))
	)
	gamepack.write("injected.jar")

	private val frame = new JFrame
	private val dim = new Dimension(815, 736)
	frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
	frame.getContentPane.setPreferredSize(dim)
	frame.setLocation(25, 25)
	frame.setResizable(false)

	private var client = load()
	start()

	def reload(){
		frame.remove(client)
		client.destroy()
		client = load()
		start()
	}
	private def start(){
		frame.add(client)
		frame.pack()
		frame.setVisible(true)
		client.init()
		client.start()
	}
	private def load() = {
		val jar = Array(new URL("file:injected.jar"))
		val loader = new URLClassLoader(jar, Thread.currentThread.getContextClassLoader)
		val client = loader.loadClass("client").newInstance.asInstanceOf[Client]
		client.setStub(new Stub)
		client.setSize(dim)
		client
	}
}

class Stub extends AppletStub {
	val url = "http://oldschool30.runescape.com"
	val base = new URL(url + "/gamepack.jar")
	val src = io.Source.fromURL(url).mkString
	val params = """<param name="([^"]*)" value="([^"]*)">""".r
		.findAllIn(src).matchData.map(n => n.group(1) -> n.group(2)).toMap

	def getCodeBase() = base
	def getDocumentBase() = base
	def getParameter(s: String) = params(s)

	def appletResize(x: Int, y: Int){}
	def getAppletContext() = null
	def isActive() = false
}
