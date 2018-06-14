//>>scalac Onyx.scala Injection.scala; scala onyx.Onyx
package onyx

import collection.JavaConverters._
import jdk.internal.org.objectweb.asm.{ ClassReader, ClassWriter }
import jdk.internal.org.objectweb.asm.Opcodes._
import jdk.internal.org.objectweb.asm.tree._
import java.util.jar.{ JarOutputStream, JarFile, JarEntry }
import java.lang.reflect.{ Method, Modifier }
import java.applet.{ Applet, AppletStub }
import java.io.{ FileOutputStream, File }
import java.net.{ URL, URLClassLoader }

/* new Hook("Character", "localX", "bc", "k") */
class Hook(
	val interface: String,
	val name: String,
	val clazz: String,
	val field: String){

	def inject(client: ClassNode, nodes: List[ClassNode]){
		val cn = nodes.find(_.name == clazz).get
		val fn = cn.fields.asScala.find(_.name == field).get
		val mn = new MethodNode(ACC_PUBLIC, name, "()" + fn.desc, null, null)
		val ins = mn.instructions

		if(Modifier.isStatic(fn.access)){
			client.methods.add(mn)
			ins.add(new FieldInsnNode(GETSTATIC, clazz, field, fn.desc))
		} else {
			cn.methods.add(mn)
			ins.add(new VarInsnNode(ALOAD, 0))
			ins.add(new FieldInsnNode(GETFIELD, clazz, field, fn.desc))
		}

		ins.add(new InsnNode(
			fn.desc.charAt(0) match {
				case 'J' => LRETURN
				case 'F' => FRETURN
				case 'D' => DRETURN
				case 'L'|'[' => ARETURN
				case 'I'|'Z'|'B'|'C'|'S' => IRETURN
			})
		)
	}
}

object Injection {

	def apply(hooks: List[Hook]){
		val jar = new JarFile("gamepack.jar")
		val nodes = loadClasses(jar)
		jar.close()

		val client = nodes.find(_.name == "client").get
		hooks.foreach(_.inject(client, nodes))

		writeJar("injected.jar", nodes)
	}
	private def loadClasses(jar: JarFile) = {
		jar.entries.asScala.filter(_.getName.endsWith(".class")).map(e => {
			val reader = new ClassReader(jar.getInputStream(e))
			val node = new ClassNode
			reader.accept(node, 0)
			node
		}).toList
	}
	private def writeJar(file: String, nodes: List[ClassNode]){
		val out = new JarOutputStream(new FileOutputStream(new File(file)))
		nodes.foreach(node => {
			out.putNextEntry(new JarEntry(node.name + ".class"))
			val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
			node.accept(cw)
			out.write(cw.toByteArray)
		})
		out.close()
	}
}
