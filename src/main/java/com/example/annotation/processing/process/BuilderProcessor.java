package com.example.annotation.processing.process;

import com.example.annotation.processing.annotation.Builder;
import com.google.common.collect.ImmutableSet;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import static java.lang.annotation.ElementType.FIELD;
import static java.util.stream.Collectors.joining;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
// @AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;

    public BuilderProcessor() {
        super();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.messager = processingEnvironment.getMessager();
        this.filer = processingEnvironment.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Builder.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Executing");

        annotations.forEach(
                annotation -> roundEnv.getElementsAnnotatedWith(Builder.class)
                        .forEach(this::generateBuilderFile));
        return true;
    }

    private void generateBuilderFile(Element element) {
        String className = element.getSimpleName().toString();
        String packageName = element.getEnclosingElement().toString();
        String builderName = className + "Builder";
        String builderFullName = packageName + "." + builderName;
        List<? extends Element> fields = element.getEnclosedElements()
                .stream().filter(e -> FIELD.name().equals(e.getKind().name())).toList();

        try (PrintWriter writer = new PrintWriter(
                filer.createSourceFile(builderFullName).openWriter())) {
            writer.println("""
                    package %s;

                    public class %s {
                    """
                    .formatted(packageName, builderName));

            fields.forEach(field -> writer.print("""
                        private %s %s;
                    """.formatted(field.asType().toString(), field.getSimpleName())));

            writer.println();
            fields.forEach(field -> writer.println("""
                        public %s %s(%s value) {
                            %s = value;
                            return this;
                        }
                    """.formatted(builderName, "set" + makeCamelCase(field.getSimpleName().toString()),
                    field.asType().toString(), field.getSimpleName())));

            writer.println("""
                        public %s build() {
                            return new %s(%s);
                        }
                    """.formatted(className, className,
                    fields.stream().map(Element::getSimpleName).collect(joining(", "))));
            writer.println("}");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String makeCamelCase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
