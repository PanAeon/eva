package foo.bar

import foo.bar.annotations._
import scala.collection.JavaConverters._
import net.bytebuddy.description.`type`.TypeDescription
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.ByteBuddy

object MainApp extends App {
  val myType = getTypeInfo(classOf[Genesys])
  val instance : Genesys = myType.newInstance()
  System.out.println(instance.userWithOrgQ(1).result());
  
  def getTypeInfo[T]( _class: Class[T]): Class[T] = {
    val description = new TypeDescription.ForLoadedType(_class)
    val declaredMethodsList = description.getDeclaredMethods()
    
    val sqlMethodsList = declaredMethodsList
          .filter(ElementMatchers.isAnnotatedWith(classOf[sql]))
    val sqlMethods = sqlMethodsList.asScala.toList;
          
    val stub : DynamicType.Builder[Object] = new ByteBuddy()
      .subclass(classOf[Object])
      .implement(_class);
    
    sqlMethods.foreach { m =>
      val methodAnnotations = m.getDeclaredAnnotations.asScala
      val sqlAnnotation = methodAnnotations
        .filter(_.getAnnotationType.getName == "foo.bar.annotations.sql")
        .apply(0)
      val valueMethod = sqlAnnotation
        .getAnnotationType()
        .getDeclaredMethods().filter(ElementMatchers.named("value"))
				.get(0);
      val annotationValues = sqlAnnotation
        .getValue(valueMethod).asInstanceOf[Array[String]]
      if ( annotationValues.isEmpty ) {
        sys.error("empty sql annotation is not implemented")
      }
      val sqlQuery = annotationValues.mkString(",")
      val declaredParametersList = m.getParameters()
    }
    sys.error("not implemented")
  }
  
}