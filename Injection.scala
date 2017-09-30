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

case class Hook(obf: String, tmethod: Method, mult: String){
	val Array(obfClass, obfField) = obf.split('.')
	val interface = tmethod.getDeclaringClass.getName
	val method = tmethod.getName
	val multiplier = mult.toInt
}

object Injection {

	def apply(hooks: List[Hook]){
		val jar = new JarFile("gamepack.jar")
		val nodes = loadClasses(jar)
		jar.close()

		hooks.foreach(h => addHook(nodes, h))

		writeJar("injected.jar", nodes)
	}
	private def addHook(nodes: List[ClassNode], hook: Hook){
		/*val cn = nodes.find(_.name == hook.obfClass).get
		if(!cn.interfaces.contains(hook.interface))
			cn.interfaces.add(hook.interface)

		val mn = new MethodNode(ACC_PUBLIC, m.name, "()" + m.cast, null, null)
		val ins = mn.instructions

		if(Modifier.isStatic(cn.fields.find(_.name == m.id).get.access)){
			ins.add(new FieldInsnNode(GETSTATIC, m.clazz, m.id, m.actual))
		} else {
			ins.add(new VarInsnNode(ALOAD, 0))
			ins.add(new FieldInsnNode(GETFIELD, m.clazz, m.id, m.actual))
		}
		if(m.actual != m.cast)
			ins.add(new TypeInsnNode(CHECKCAST, m.cast))

		ins.add(new InsnNode(
			m.cast.charAt(0) match {
				case 'J' => LRETURN
				case 'F' => FRETURN
				case 'D' => DRETURN
				case 'L'|'[' => ARETURN
				case 'I'|'Z'|'B'|'C'|'S' => IRETURN
			}))
		cn.methods.add(mn)*/
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
		nodes.foreach(n => {
			out.putNextEntry(new JarEntry(n.name + ".class"))
			val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
			n.accept(cw)
			out.write(cw.toByteArray)
		})
		out.close()
	}
}
