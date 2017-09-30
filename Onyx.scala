package onyx

import scala.language.postfixOps
import scala.sys.process._
import javax.swing.{ JFrame, WindowConstants }
import java.applet.{ Applet, AppletStub }
import java.net.{ URL, URLClassLoader }
import java.awt.Dimension

object Onyx extends App {
	System.setProperty("user.home", "/home/boar/.jagex")

	private val frame = new JFrame
	private val dim = new Dimension(815, 736)
	frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
	frame.getContentPane.setPreferredSize(dim)
	frame.setLocation(25, 25)
	frame.setVisible(true)

	private var applet = load()
	start()

	def reload(){
		frame.remove(applet)
		applet.destroy()
		applet = load()
		start()
	}
	private def start(){
		frame.add(applet)
		frame.pack()
		applet.init()
		applet.start()
	}
	private def load() = {
		val loader = new URLClassLoader(Array(new URL("file:gamepack.jar")))
		val applet = loader.loadClass("client").newInstance.asInstanceOf[Applet]
		applet.setStub(new Stub)
		applet.setSize(dim)
		applet
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

	def getAppletContext() = null
	def isActive() = false
	def appletResize(x: Int, y: Int){}
}
