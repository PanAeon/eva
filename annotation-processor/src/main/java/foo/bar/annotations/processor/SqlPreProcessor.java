package foo.bar.annotations.processor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

// @AutoService(Processor.class)
public class SqlPreProcessor extends AbstractProcessor {
	
	private Types typeUtils;
	private Elements elementUtils;
	private Filer filer;
	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment env){
		super.init(processingEnv);
		typeUtils = processingEnv.getTypeUtils();
	    elementUtils = processingEnv.getElementUtils();
	    filer = processingEnv.getFiler();
	    messager = processingEnv.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		messager.printMessage(Kind.WARNING, "Hello, I'm here");
//		 for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(a) {
//		  		...
//		    }
		 
		return false;
	}
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
	   return Stream.of("a", "b").collect(Collectors.toSet());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() { // Java 8
		return SourceVersion.latestSupported();
	}

}
