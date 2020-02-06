package com.vmware.vcloud.bindings.generator;

/*-
 * #%L
 * vcd-bindings-generator :: Bindings generation utility
 * %%
 * Copyright (C) 2020 VMware, Inc.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.EnumConverter;
import com.vmware.vcloud.bindings.generator.python.PythonEnum;
import com.vmware.vcloud.bindings.generator.python.PythonFile;

public class PythonBindingsGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PythonBindingsGenerator.class);
    public Map<String, String> fileToClassName;

    public static class OutputTypeConverter extends EnumConverter<OutputType> {
        public OutputTypeConverter(final String optionName, final Class<OutputType> clazz) {
            super(optionName, clazz);
        }
    }

    public static class OverwriteTypeConverter extends EnumConverter<OverwriteType> {
        public OverwriteTypeConverter(final String optionName, final Class<OverwriteType> clazz) {
            super(optionName, clazz);
        }
    }

    private static final Set<AnnotationTypeFilter> FILTERS = new HashSet<>();
    static {
        FILTERS.add(new AnnotationTypeFilter(XmlType.class));
        FILTERS.add(new AnnotationTypeFilter(XmlEnum.class));
    };

    public static void main(String... args) {
        final PythonBindingsGenerator generator = new PythonBindingsGenerator();
        final JCommander commander = JCommander.newBuilder().programName(PythonBindingsGenerator.class.getSimpleName())
                .addObject(generator).build();
        commander.parse(args);
        if (generator.help) {
            commander.usage();
            return;
        }
        generator.generate();
    }

    @Parameter(names = {"-p", "--packages"}, description = "One or more packages to scan for schema classes.", variableArity = true, required = true)
    private List<String> packages;

    @Parameter(names = {"-h", "--help"}, description = "Prints this help message", help = true)
    private boolean help;

    @Parameter(names = {"-o", "--outputDir"}, description = "A directory to output custom files to.  If none is specified, the output is streamed to standard out.")
    private File outputDir;

    @Parameter(names = {"-x", "--overwrite"}, description = "Determines what to do if the outputDir is not empty.  None halts generation.  Full deletes the content "
        + "of the outputDir.  Merge attempts to create new files that coexists with the state of outputDir", converter = OverwriteTypeConverter.class)
    private OverwriteType overwrite = OverwriteType.None;

    @Parameter(names = {"-t", "--outputType"}, description = "Indicates whether to generate classes or interfaces for bindings", converter = OutputTypeConverter.class)
    private OutputType outputType = OutputType.Class;

    private ClassPathScanningCandidateComponentProvider provider;

    private Template classTemplate;
    private Template enumTemplate;
    private Template indexTemplate;

    private PythonBindingsGenerator() {
        provider = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                return metadata.isIndependent() && !metadata.isInterface();
            }
        };

        LOGGER.trace("Scanning for classes with annotations {}", FILTERS);
        FILTERS.forEach(provider::addIncludeFilter);
        provider.setResourcePattern("*.class");
    }

    public PythonBindingsGenerator(final List<String> packages) {
        this();
        this.packages = packages;
        this.fileToClassName = new HashMap<>();
    }

    public PythonBindingsGenerator outputDir(final File outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public PythonBindingsGenerator outputType(final OutputType outputType) {
        this.outputType = outputType;
        return this;
    }

    public PythonBindingsGenerator overwrite(OverwriteType overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    public void generate() {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        engine.init();

        this.classTemplate = engine.getTemplate("/python/class.py.vm");
        this.enumTemplate = engine.getTemplate("/python/enum.py.vm");
        this.indexTemplate = engine.getTemplate("/python/__init__.py.vm");

        long start = System.currentTimeMillis();
        validateOutputState();
        this.packages.forEach(this::createBindingsForPackage);
        createBarrels();

        LOGGER.info("Complete in {} ms", System.currentTimeMillis() - start);
    }

    private void validateOutputState() {
        if (outputDir == null || !outputDir.exists()) {
            return;
        }

        boolean empty;
        try (Stream<Path> files = Files.list(outputDir.toPath())) {
            empty = !files.iterator().hasNext();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (empty) {
            return;
        }

        if (!empty && overwrite == OverwriteType.None) {
            LOGGER.error("Directory {} is not empty.  Aborting code generation.  To overwrite the directory, specify --overwrite option.", outputDir.getAbsolutePath());
            throw new IllegalStateException("outputDir not empty and overwrite flag not specified.");
        } else if (!empty && overwrite == OverwriteType.Full) {
            LOGGER.info("--overwrite flag specified.  Deleting current content of {}", outputDir.getAbsolutePath());
            try {
                Files.walk(outputDir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void createBindingsForPackage(final String basePackage) {
        LOGGER.info("Creating Python bindings for package {}", basePackage);
        List<Class<?>> classes = this.provider.findCandidateComponents(basePackage).stream()
                .map(bd -> ClassUtils.resolveClassName(bd.getBeanClassName(), null))
                .collect(Collectors.toList());

        if (classes.isEmpty()) {
            LOGGER.warn("Package {} contains no bindable classes", basePackage);
            return;
        }

        for (Class<?> clazz : classes) {
            PythonFile file;
            if (clazz.isEnum()) {
                file = PythonFile.createEnum(clazz);
            } else {
                file = PythonFile.createClass(clazz);
            }

            LOGGER.debug("Adding {} to package {}", file, basePackage);
            writeFile(file, basePackage);
        }
    }

    private void createBarrels() {
        if (outputDir == null) {
            LOGGER.debug("No output directory specified.  Skipping barrel creation.");
            return;
        }

        Map<Path, List<String>> barrels = new HashMap<>();
        try (Stream<Path> files = Files.walk(outputDir.toPath())) {
            files.filter(f -> !"__init__.py".equals(f.getFileName().toString())).forEach(f -> {
                if (Files.isDirectory(f)) {
                    barrels.putIfAbsent(outputDir.toPath().relativize(f), new ArrayList<>());
                }

                if (f.equals(outputDir.toPath())) {
                    return;
                }

                String value;
                if (Files.isDirectory(f)) {
                    value = f.getFileName().toString().concat("/");
                } else {
                    value = f.getFileName().toString();
                    value = value.substring(0, value.lastIndexOf("."));
                }

                barrels.get(outputDir.toPath().relativize(f.getParent())).add(value);
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        barrels.entrySet().forEach(barrel -> {
            VelocityContext context = new VelocityContext();
            context.put("_formatter", StringUtils.class);
            context.put("year", LocalDate.now().getYear());
            final List<String> values = barrel.getValue();
            final List<Map<String, String>> strippedValue = new ArrayList<>();
            values.forEach(value -> {
                if (value.endsWith("/")) {
                    return;
                }
                Map<String, String> importData = new HashMap<>();
                importData.putIfAbsent(value, fileToClassName.get(value));
                strippedValue.add(importData);
            });
            context.put("imports", strippedValue);
            try (Writer writer = getWriter("__init__.py", barrel.getKey().toString())) {
                indexTemplate.merge(context, writer);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void writeFile(final PythonFile tsFile, final String basePackage) {
        VelocityContext context = new VelocityContext();
        context.put("_formatter", StringUtils.class);
        context.put("year", LocalDate.now().getYear());
        context.put("class", tsFile);
        final String finalName = PythonFile.underscore(tsFile.getName());
        fileToClassName.put(finalName, tsFile.getName());

        Template template = (tsFile instanceof PythonEnum)  ? enumTemplate : classTemplate;
        try (Writer writer = getWriter(finalName + ".py", basePackage)) {
            template.merge(context, writer);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Writer getWriter(final String name, final String basePackage) {
        if (outputDir == null) {
            return new BufferedWriter(new PrintWriter(System.out));
        }

        try {
            File scopedOutputDir = new File(outputDir, getRelativePackage(basePackage).replace('.', File.separatorChar) + File.separatorChar);
            scopedOutputDir.mkdirs();
            return new BufferedWriter(new FileWriter(new File(scopedOutputDir, name)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getRelativePackage(final String basePackage) {
        return basePackage.replace("com.vmware.", "");
    }
}
