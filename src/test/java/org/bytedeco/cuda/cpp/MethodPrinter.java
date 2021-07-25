package org.bytedeco.cuda.cpp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.immersed.gaffe.CPP14Lexer;
import org.immersed.gaffe.CPP14Parser;
import org.immersed.gaffe.CPP14Parser.ClassHeadNameContext;
import org.immersed.gaffe.CPP14Parser.DeclaratorContext;
import org.immersed.gaffe.CPP14Parser.FunctionDefinitionContext;
import org.immersed.gaffe.CPP14Parser.NamespaceDefinitionContext;
import org.immersed.gaffe.CPP14Parser.ParameterDeclarationContext;
import org.immersed.gaffe.CPP14Parser.TemplateParameterContext;
import org.immersed.gaffe.CPP14Parser.TemplateparameterListContext;
import org.immersed.gaffe.CPP14Parser.TypeParameterContext;
import org.immersed.gaffe.CPP14ParserBaseListener;

public class MethodPrinter
{
    private static final class Cpp14Listener extends CPP14ParserBaseListener
    {
        private String struct;
        private String namespace;

        private FunctionDefinition.Builder builder = new FunctionDefinition.Builder();
        private List<FunctionDefinition> functions = new ArrayList<>();

        @Override
        public void exitNamespaceDefinition(NamespaceDefinitionContext ctx)
        {
            TerminalNode id = ctx.Identifier();
            this.namespace = id.getText();
        }

        @Override
        public void exitClassHeadName(ClassHeadNameContext ctx)
        {
            this.struct = ctx.getText();
        }

        @Override
        public void exitFunctionDefinition(FunctionDefinitionContext ctx)
        {
            DeclaratorContext declarator = ctx.declarator();

            ParseTree methodName = declarator.getChild(0);

            while (methodName.getChildCount() > 0)
            {
                methodName = methodName.getChild(0);
            }

            this.builder.name(methodName.getText());
            this.functions.add(this.builder.build());

            this.builder.clear();

        }

        @Override
        public void exitTemplateparameterList(TemplateparameterListContext ctx)
        {
            for (TemplateParameterContext param : ctx.templateParameter())
            {
                TypeParameterContext tpc = param.typeParameter();
                ParameterDeclarationContext pdc = param.parameterDeclaration();

                ParseTree type = null;
                ParseTree name = null;

                if (tpc != null)
                {
                    type = tpc.Typename_();
                    name = tpc.Identifier();
                }

                if (pdc != null)
                {
                    type = pdc.declSpecifierSeq();
                    name = pdc.declarator();
                }

                this.builder.addTemplateTypes(type.getText());
                this.builder.addTemplates(name.getText());
            }
        }

        private void print()
        {
            Map<String, String> uniqueDefinitions = new LinkedHashMap<>();

            for (FunctionDefinition function : this.functions)
            {
                StringBuilder builder = new StringBuilder();

                if (namespace != null)
                {
                    builder.append(namespace)
                           .append("::");
                }

                if (struct != null)
                {
                    builder.append(struct)
                           .append("::");
                }

                builder.append(function.name());
                builder.append(function.templates()
                                       .stream()
                                       .reduce((a, b) -> a + "," + b)
                                       .map(s -> "<" + s + ">")
                                       .orElse(""));

                uniqueDefinitions.put(builder.toString(), function.name());
            }

            CubTemplates templates = new CubTemplates();

            uniqueDefinitions.forEach((definition, function) ->
            {
                System.out.println();
                System.out.println("// " + definition);

                templates.walk(definition, function)
                         .forEach(System.out::println);
            });
        }
    }

    public static void main(String[] args) throws IOException
    {
        Path includeDir = Paths.get("C:", "Program Files", "NVIDIA GPU Computing Toolkit", "CUDA", "v11.2", "include");
        Path path = includeDir.resolve("cub")
                              .resolve("device");

        List<Path> allFiles = Files.walk(path, 1)
                                   .filter(p -> p.toString()
                                                 .endsWith(".cuh"))
                                   .collect(Collectors.toList());

        Map<Path, Cpp14Listener> collectors = new LinkedHashMap<>();

        for (Path deviceCuh : allFiles)
        {
            CharStream stream = CharStreams.fromPath(deviceCuh);
            Lexer lexer = new CPP14Lexer(stream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CPP14Parser parser = new CPP14Parser(tokens);
            Cpp14Listener listener = new Cpp14Listener();
            parser.addParseListener(listener);
            parser.translationUnit();

            collectors.put(deviceCuh, listener);
        }

        collectors.forEach((cuh, collector) ->
        {
            Path relativePath = includeDir.relativize(cuh);
            String headerDef = relativePath.toString()
                                           .replace(File.separatorChar, '/');

            System.out.println();
            System.out.println("\"<" + headerDef + ">\"");
            collector.print();
        });
    }
}
