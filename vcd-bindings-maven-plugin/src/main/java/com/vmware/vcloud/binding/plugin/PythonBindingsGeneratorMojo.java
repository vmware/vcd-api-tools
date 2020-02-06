package com.vmware.vcloud.binding.plugin;

/*-
 * #%L
 * vcd-bindings-maven-plugin :: Maven plugin wrapper for bindings generation utility
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

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.vmware.vcloud.bindings.generator.OutputType;
import com.vmware.vcloud.bindings.generator.OverwriteType;
import com.vmware.vcloud.bindings.generator.PythonBindingsGenerator;

@Mojo(name = "generate-python")
public class PythonBindingsGeneratorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.directory}/generated-python-bindings")
    private File outputDirectory;

    @Parameter(required = true)
    private List<String> packages;

    @Parameter(defaultValue = "None")
    private OverwriteType overwrite;

    @Parameter(defaultValue = "Class")
    private OutputType outputType;

    public void execute() throws MojoExecutionException {
        getLog().info("Generating bindings into " + outputDirectory.getAbsolutePath());
        new PythonBindingsGenerator(packages)
            .outputDir(outputDirectory)
            .outputType(outputType)
            .overwrite(overwrite)
            .generate();
    }
}
