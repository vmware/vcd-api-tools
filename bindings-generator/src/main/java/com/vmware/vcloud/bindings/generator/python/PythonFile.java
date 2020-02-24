package com.vmware.vcloud.bindings.generator.python;

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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class PythonFile {
    private Class<?> clazz;

    private Set<Import> imports = new HashSet<>();
    private List<Field> fields = new ArrayList<>();

    public Set<Import> getImports() {
        return Collections.unmodifiableSet(imports);
    }

    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public static PythonClass createClass(final Class<?> clazz) {
        Objects.requireNonNull(clazz);
        if (clazz.isEnum()) {
            throw new IllegalArgumentException("Can't create Python class for enum " + clazz.getSimpleName() + ".  Try creating Python enum instead.");
        }

        final PythonClass tc = new PythonClass(clazz);
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> tc.processField(field, clazz));

        return tc;
    }

    public static PythonEnum createEnum(final Class<?> clazz) {
        Objects.requireNonNull(clazz);
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException("Can't create Python enum for class " + clazz.getSimpleName() + ".  Try creating Python class instead.");
        }

        PythonEnum te = new PythonEnum(clazz);
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> te.processField(field, clazz));

        return te;
    }

    public String getName() {
        return clazz.getSimpleName();
    }

    protected Class<?> getClazz() {
        return clazz;
    }

    protected void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    protected void addField(final String name, final String type) {
        Field field = new Field();
        field.name = name;
        field.type = type;
        field.displayName = underscore(name);
        fields.add(field);
    }

    protected void addField(final String name) {
        Field field = new Field();
        field.name = name;
        field.displayName = underscore(name);
        this.fields.add(field);
    }

    protected void addImport(final Class<?> usingClass, final Class<?> referencedClass) {
        if (referencedClass == null || referencedClass.getName().startsWith("java")) {
            return;
        }

        Path usingClassPath = Paths.get(usingClass.getPackage().getName().replace(".", "/")).normalize();
        Path referencedClassPath = Paths.get(referencedClass.getName().replace(".", "/")).normalize();
        String clasName = referencedClass.getSimpleName();
        String fileName;
        final String[] references = referencedClassPath.toString().split("/");
        if (references.length == 2 && references[0].isEmpty()) {
        	fileName = underscore(usingClassPath.relativize(referencedClassPath).toString().replace("\\", ".").replace("/", "."));
        } else if (references.length > 1) {
            String relativePath = usingClassPath.relativize(referencedClassPath).toString();
            relativePath = relativePath.replaceFirst("/", "");
            fileName =  underscore(relativePath.replace("/", ".").replace("\\", "."));
            if (!fileName.startsWith(".")) {
            	fileName = '.' + fileName;
            }
        } else {
            fileName = '.' + underscore(usingClassPath.relativize(referencedClassPath).toString().replace("\\", ".").replace("/", "."));
        }
        this.imports.add(new Import(clasName, fileName));
    }

    /**
     * Underscore the given word.
     * Copied from Twitter elephant bird
     * https://github.com/twitter/elephant-bird/blob/master/core/src/main/java/com/twitter/elephantbird/util/Strings.java
     *
     * @param word The word
     * @return The underscored version of the word
     */
    public static String underscore(String word) {
        String firstPattern = "([A-Z]+)([A-Z][a-z])";
        String secondPattern = "([a-z\\d])([A-Z])";
        String replacementPattern = "$1_$2";
        // Replace package separator with slash.
        //word = word.replaceAll("\\.", "/"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.
        // Replace $ with two underscores for inner classes.
        word = word.replaceAll("\\$", "__");
        // Replace capital letter with _ plus lowercase letter.
        word = word.replaceAll(firstPattern, replacementPattern);
        word = word.replaceAll(secondPattern, replacementPattern);
        word = word.replace('-', '_');
        word = word.toLowerCase();
        return word;
    }

    protected abstract void processField(final java.lang.reflect.Field field, final Class<?> clazz);

    public static class Import {
        private String definition;
        private String module;

        public Import(final String definition, final String module) {
            this.definition = definition;
            this.module = module;
        }

        public String getDefinition() {
            return definition;
        }

        public String getModule() {
            return module;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Import)) {
                return false;
            }

            Import other = (Import) obj;
            return Objects.equals(this.definition, other.definition) && Objects.equals(this.module, other.module);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.definition, this.module);
        }
    }

    public static class Field {
        private String name;
        private String type;
        private String displayName;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        private boolean required;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public boolean isRequired() {
            return required;
        }

        @Override
        public String toString() {
            ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).append(getName());
            if (type != null) {
                builder.append(": ").append(type);
            }

            return builder.toString();
        }
    }
}
