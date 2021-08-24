package org.bytedeco.cuda.cpp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
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
    private static final class Cpp14DefinitionListener extends CPP14ParserBaseListener
    {
        private final FunctionDefinition.Builder builder = new FunctionDefinition.Builder();

        @Override
        public void exitNamespaceDefinition(NamespaceDefinitionContext ctx)
        {
            TerminalNode id = ctx.Identifier();
            this.builder.namespace(id.getText());
        }

        @Override
        public void exitClassHeadName(ClassHeadNameContext ctx)
        {
            this.builder.struct(ctx.getText());
        }
    }

    private static final class Cpp14Listener extends CPP14ParserBaseListener
    {
        private List<FunctionDefinition> functions = new ArrayList<>();
        private Set<String> definitions = new HashSet<>();

        private final FunctionDefinition.Builder base;
        private final FunctionDefinition.Builder builder;

        public Cpp14Listener(Cpp14DefinitionListener structAndNamespaces)
        {
            this.base = structAndNamespaces.builder;
            this.builder = new FunctionDefinition.Builder().mergeFrom(this.base);
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

            FunctionDefinition function = this.builder.build();

            if (this.definitions.add(function.toDefinition()))
            {
                this.functions.add(function);
            }

            this.builder.clear()
                        .mergeFrom(this.base);

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
            CubTemplates templates = new CubTemplates();

            for (FunctionDefinition functionObj : this.functions)
            {
                final String definition = functionObj.toDefinition();

                System.out.println();
                System.out.format("    // %s", definition);
                System.out.println();

                templates.walk(functionObj)
                         .forEach(result ->
                         {
                             System.out.format("    %s", result);
                             System.out.println();
                         });
            }
        }
    }

    public static void main(String[] args) throws IOException
    {
        Path includeDir = Paths.get("C:", "Program Files", "NVIDIA GPU Computing Toolkit", "CUDA", "v11.2", "include");
        Path path = includeDir.resolve("cub")
                              .resolve("device");

        // final String suffix = ".cuh";
        final String suffix = "device_spmv.cuh";

        List<Path> allFiles = Files.walk(path, 1)
                                   .filter(p ->
                                   {
                                       String name = p.toString();
                                       return name.endsWith(suffix);
                                   })
                                   .collect(Collectors.toList());

        Map<Path, Cpp14Listener> collectors = new LinkedHashMap<>();

        for (Path deviceCuh : allFiles)
        {
            Cpp14DefinitionListener structAndNamespace = parse(deviceCuh, new Cpp14DefinitionListener());
            Cpp14Listener listener = parse(deviceCuh, new Cpp14Listener(structAndNamespace));

            collectors.put(deviceCuh, listener);
        }

        collectors.forEach((cuh, collector) ->
        {
            Path relativePath = includeDir.relativize(cuh);
            String headerDef = relativePath.toString()
                                           .replace(File.separatorChar, '/');

            System.out.println();
            System.out.println("\"<" + headerDef + ">\"");
            System.out.println();
            
            String name = cuh.getFileName()
                             .toString();
            String method = name.substring(0, name.lastIndexOf('.'));

            System.out.format("private static void %s(InfoMap infoMap)", method)
                      .println();
            System.out.print("{");
            collector.print();
            System.out.println("}");
        });
    }

    private static final <T extends ParseTreeListener> T parse(Path path, T listener) throws IOException
    {
        CharStream stream = CharStreams.fromPath(path);
        Lexer lexer = new CPP14Lexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);

        parser.addParseListener(listener);
        parser.translationUnit();

        return listener;
    }
}
