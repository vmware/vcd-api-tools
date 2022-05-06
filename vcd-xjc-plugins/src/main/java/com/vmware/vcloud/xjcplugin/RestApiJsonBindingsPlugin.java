/*-
 * #%L
 * vcd-xjc-plugins :: Custom plugins for XML to Java Compilation
 * %%
 * Copyright (C) 2022 VMware, Inc.
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

package com.vmware.vcloud.xjcplugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBElement;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.util.ClassNameComparator;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.vmware.vcloud.api.annotation.Supported;

import org.apache.commons.lang3.StringUtils;
import org.jvnet.jaxb2_commons.util.FieldAccessorUtils;

/**
 * <code>RestApiJsonBindingsPlugin</code> is a plug-in for the JAXB's XJC code generator to
 * incorporate necessary annotations for json serialization/de-serialization
 * <p>
 * To activate it, add the following parameters to the XJC command line:<br>
 *
 * <pre>
 * -enable -Xrest-json-api
 * </pre>
 * <p>
 * Unlike {@link RestApiVersionsPlugin}, this plugin is not a customization processing plugin,
 * rather it processes all classes while producing annotations for necessary circumstances.
 */
public class RestApiJsonBindingsPlugin extends Plugin {

    private static final String VCLOUD_EXTENSIBLE_TYPE = "VCloudExtensibleType";
    private static final String PLUGIN_OPTION = "Xrest-json-api";

    private static final String DESER_PACKAGE_NAME = "com.vmware.vcloud.api.rest.deser";
    private static final String DESERIALIZE_METHOD = "deserialize";
    private static final String JSON_PARSER_ARG = "jsonParser";
    private static final String DESERIALIZATION_CONTEXT_ARG = "deserializationContext";

    private MultivaluedMap<String, JDefinedClass> VALUES_MAP = new MultivaluedHashMap<>();
    private Map<String, JAnnotationArrayMember> ANNOTATION_ARRAY_MAP = new HashMap<>();

    private Outline outline;

    private JClass jaxbElementClass;
    private JClass stdDeserializerClass;
    private JCodeModel codeModel;
    private ErrorReceiver errorReceiver;

    @Override
    public String getOptionName() {
        return PLUGIN_OPTION;
    }

    @Override
    public String getUsage() {
        return "  -" + PLUGIN_OPTION
                + ": enables the plugin and generates necessary json relevant annotations\n";
    }

    @Override
    public boolean run(Outline outline, Options options, ErrorHandler errorHandler) {
        this.outline = outline;
        codeModel = outline.getCodeModel();
        jaxbElementClass = codeModel.ref(JAXBElement.class);
        stdDeserializerClass = codeModel.ref(StdDeserializer.class);
        errorReceiver = outline.getErrorReceiver();

        for (ClassOutline classOutline : outline.getClasses()) {
            processClassOutline(classOutline, errorHandler);
        }

        /*
         * As noted in the javadoc for #processClassOutline(), this loop adds necessary sub-type information
         * to super classes that have empty @JsonSubTypes annotation added.
         */
        for (final Entry<String, JAnnotationArrayMember> entry : ANNOTATION_ARRAY_MAP.entrySet()) {
            final String annotatedClass = entry.getKey();
            final JAnnotationArrayMember annotationArray = entry.getValue();
            final List<JDefinedClass> classList = VALUES_MAP.get(annotatedClass);
            if (classList == null) {
                continue;
            }

            for (final JDefinedClass clz : classList) {
                annotationArray.annotate(JsonSubTypes.Type.class).param("name", clz.name()).param("value", clz);
            }
        }

        return true;
    }

    /**
     * Annotates {@link ClassOutline} with necessary json-relevant annotations
     *
     * @param classOutline
     * @param errorHandler
     */
    private void processClassOutline(ClassOutline classOutline, ErrorHandler errorHandler) {
        addJsonInheritenceInformation(classOutline);
        final JAnnotationUse enableFieldFiltering = classOutline.implClass.annotate(JsonFilter.class);
        enableFieldFiltering.param("value", Supported.VCLOUD_LEGACY_FILTER_PARAM);

        for (FieldOutline fieldOutline : classOutline.getDeclaredFields()) {
            addJsonPropertyOutline(fieldOutline);
        }
    }

    /**
     * Annotates selected {@code abstract} classes with necessary inheritance information
     * <P>
     * Abstract classes that inherit from {@code VCloudExtensibleType} or {@link Object} are
     * targeted for annotating with {@link JsonSubTypes} and {@link JsonTypeInfo} annotations to
     * assist in json deserialization
     * <P>
     * {@link JsonSubTypes} needs to augmented with type information about sub-types. However, since
     * this version of the plugin is not the latest, it does not have access to
     * {@link JAnnotatable}'s {code annotations()} method, which would be critical to append type
     * information to an existing annotation. As a result, sub-types are noted in a map and the
     * calling method will post-process and add necessary information to the super-class's
     * annotation. Also, since classes are processed in no-fixed order, it is not possible to
     * guarantee that the super-class is always processed before the sub-class, hence it is best to
     * process things later.
     *
     * @param classOutline
     */
    private void addJsonInheritenceInformation(final ClassOutline classOutline) {
        final JDefinedClass implClass = classOutline.implClass;
        if (isAbstractClassToAnnotate(classOutline)) {
            final JAnnotationUse typeInfoAnnotation = implClass.annotate(JsonTypeInfo.class);
            typeInfoAnnotation.param("use", JsonTypeInfo.Id.NAME);
            typeInfoAnnotation.param("include", JsonTypeInfo.As.PROPERTY);
            typeInfoAnnotation.param("property", "_type");

            final JAnnotationArrayMember annotationValueArray =
                    implClass.annotate(JsonSubTypes.class).paramArray("value");
            ANNOTATION_ARRAY_MAP.put(implClass.fullName(), annotationValueArray);
            return;
        }

        ClassOutline parentClass = classOutline;
        while ((parentClass = parentClass.getSuperClass()) != null) {
            if (isAbstractClassToAnnotate(parentClass)) {
                VALUES_MAP.add(parentClass.implClass.fullName(), implClass);
            }
        }
    }

    private boolean isAbstractClassToAnnotate(ClassOutline classOutline) {
        return classOutline != null &&
                !classOutline.implClass.name().contains(VCLOUD_EXTENSIBLE_TYPE) &&
                classOutline.implClass.isAbstract() &&
                (classOutline.getSuperClass() == null ||
                        classOutline.getSuperClass().implClass.name().contains(VCLOUD_EXTENSIBLE_TYPE));
    }

    private void addJsonPropertyOutline(FieldOutline fieldOutline) {
        final JFieldVar field = FieldAccessorUtils.field(fieldOutline);

        addressJsonNamingIssue(fieldOutline, field);
        generateDeserializationClass(fieldOutline, field);
    }

    /**
     * Adds {@link JsonProperty} annotation for fields under special circumstances
     * <P>
     * If fields have 2nd letter capitalized, the corresponding getters and setters have 2 letters
     * following {@code get}, {@code set} or {@code is} capitalized. {@link ObjectMapper} has a bug
     * where it lower-cases not just the first but all capital letters until first lower-cased
     * letter is encountered.
     *
     * @param fieldOutline
     */
    private void addressJsonNamingIssue(FieldOutline fieldOutline, final JFieldVar field) {
        final String varName = field.name();

        final JMethod getter = FieldAccessorUtils.getter(fieldOutline);
        final JMethod setter = FieldAccessorUtils.setter(fieldOutline);

        if (varName.length() > 1 && Character.isLowerCase(varName.charAt(1)) &&
                isGetterNameSafeForJackson(getter.name())) {
            return;
        }

        if (getter != null) {
            final JAnnotationUse getterAnnotation = getter.annotate(JsonProperty.class);
            getterAnnotation.param("value", varName);
        }

        if (setter != null) {
            final JAnnotationUse setterAnnotation = setter.annotate(JsonProperty.class);
            setterAnnotation.param("value", varName);
        }
    }

    private boolean isGetterNameSafeForJackson(String getter) {
        final String strippedName = (getter.startsWith("get")) ? StringUtils.removeFirst(getter, "get") :
                StringUtils.removeFirst(getter, "is");
        return strippedName.length() > 1 ? Character.isLowerCase(strippedName.charAt(1)) : true;
    }

    /**
     * Produce custom de-serializer for fields whose objects will be of {@link JAXBElement} type
     * <p>
     * Such elements will be annotated with {@link JsonDeserialize} annotation and a corresponding
     * custom de-serializer is generated once or reused.<br/>
     * As noted in the documentation of {@link JsonDeserialize} annotation, we need to supply the
     * de-serializer using attributes as applicable by nature of the field, which is summarized below:
     * <ul>
     *   <li><b>Single Element</b>: A de-serializer is specified using {@link JsonDeserialize#using()
     *       using attribute}</li>
     *   <li><b>Collection/Array of {@code JAXBElement}</b>: A de-serializer is specified using
     * {@link JsonDeserialize#contentUsing() contentUsing attribute}<li>
     * </ul>
     * <p>
     * <b>Note</b>: Following use-cases are currently not addressed. If necessary, the code can be expanded
     * to cover these cases:
     * <ul>
     *   <li><b>Nested Collection/Array of {@link JAXBElement}s</b></li>
     *   <li><b>Map with either key, value or both containing {@link JAXBElement}s</b>: Incorporate using
     *       {@link JsonDeserialize#keyUsing() keyUsing attribute} and/or {@link JsonDeserialize#contentUsing()
     *       contentUsing attribute}</li>
     * </ul>
     *
     * @param fieldOutline
     *            {@link FieldOutline} object representing the field being processed
     * @param field
     *            {@link JFieldVar} object representing the field being processed
     */
    private void generateDeserializationClass(FieldOutline fieldOutline, final JFieldVar field) {
        final JType type = field.type();

        final Locator locator = fieldOutline.getPropertyInfo().locator;
        if (isJAXBElementType(type)) {
            final JDefinedClass deserializerClass = generateDeserializer(type, fieldOutline, locator);
            final JAnnotationUse annotationUse = field.annotate(JsonDeserialize.class);
            annotationUse.param("using", deserializerClass);
        }

        final JType jaxbElementCollection = getElementTypeIfJAXBElementCollection(type);
        if (jaxbElementCollection != null) {
            final JDefinedClass deserializerClass = generateDeserializer(jaxbElementCollection, fieldOutline, locator);
            final JAnnotationUse annotationUse = field.annotate(JsonDeserialize.class);
            annotationUse.param("contentUsing", deserializerClass);
        }
    }

    /**
     * Determines whether the specified type is a {@link JAXBElement}
     *
     * {@link JType} and its sub-classes don't provide an {@code equals} method, so we are stuck
     * with name comparison. For reference, notice the technique used in for
     * {@link ClassNameComparator}.
     * <P>
     * However, as we <i>are</i> dealing with a genericized type, the name includes the generic's
     * argument information. Hence to only compare whether the type is {@code  JAXBElement} while
     * ignoring its type argument, we need to first erase that information. This is accomplished
     * using {@link JType#erasure()}
     *
     * @param type
     *            the type to check
     * @return <code>true</code> if the specified type is {@link JAXBElement}
     */
    private boolean isJAXBElementType(final JType type) {
        return (!type.isPrimitive()) && type.erasure().name().equals(jaxbElementClass.name());
    }

    private JType getElementTypeIfJAXBElementCollection(final JType type) {
        if (type.isPrimitive()) {
            return null;
        }
        if (type.isArray()) {
            final JType elementType = type.elementType();
            return isJAXBElementType(elementType) ? elementType : null;
        }

        // seriously, how about implementing 'equals'
        if (List.class.getName().equals(type.erasure().fullName())) {
            final JClass listParamType = ((JClass)type).getTypeParameters().get(0);
            return isJAXBElementType(listParamType) ? listParamType : null;
        }
        return null;
    }

    /**
     * Generate a deserializer class that implements {@link JsonDeserializer} to deserialize the
     * {@link JAXBElement} typed field
     *
     * @param fieldType
     *            The {@link JType} representing the {@link JAXBElement} wrapped type of the field
     * @param fieldOutline
     *            {@link FieldOutline} representation of the field whose type is {@code JAXBElement}
     *            wrapped
     * @param locator
     *            {@link Locator} object representing the location of the field in the .xsd files
     * @return {@link JDefinedClass} of the deserializer
     */
    @SuppressWarnings("deprecation")
    private JDefinedClass generateDeserializer(JType fieldType, FieldOutline fieldOutline, Locator locator) {
        errorReceiver.debug(String.format("Preparing to generate Json Deserializer for JAXBElement field %s.%s",
                fieldOutline.parent().implClass.fullName(), fieldOutline.getPropertyInfo().getName()));

        final JClass jaxbElementClass = (JClass)fieldType;
        errorReceiver.debug(String.format("Processing sub-type %s of %s: ",
                jaxbElementClass.getTypeParameters(), fieldType.name()));
        final JClass realDeserializingType = jaxbElementClass.getTypeParameters().get(0);

        final String deserClassFullName = generateClassName(realDeserializingType);

        final JDefinedClass deserClass;
        try {
            deserClass = codeModel._class(deserClassFullName);
        } catch (JClassAlreadyExistsException e1) {
            errorReceiver.debug(String.format("Re-using %s as deserializer", deserClassFullName));
            return e1.getExistingClass();
        }

        final JClass narrowedStdDeserializer = stdDeserializerClass.narrow(jaxbElementClass);
        deserClass._extends(narrowedStdDeserializer);

        generateSerializationField(deserClass);
        generateConstructor(deserClass, realDeserializingType);
        generateDeserializationMethod(deserClass, jaxbElementClass, fieldOutline, realDeserializingType, locator);

        return deserClass;
    }

    private void generateSerializationField(JDefinedClass deserClass) {
        deserClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, codeModel.LONG, "serialVersionUID", JExpr.lit(1L));
    }

    private String generateClassName(JClass jaxbTypeParameter) {
        jaxbTypeParameter = sanitizeIfNecessary(jaxbTypeParameter);

        final String deserClassName = jaxbTypeParameter.name() + "JaxbDeserializer";
        final String typePackage = jaxbTypeParameter._package().name();
        final String commonPrefix = StringUtils.getCommonPrefix(typePackage, DESER_PACKAGE_NAME);
        String subPackage = StringUtils.removeStart(typePackage, commonPrefix);

        final String deserClassFullName = String.format("%s.%s.%s", DESER_PACKAGE_NAME, subPackage, deserClassName);
        return deserClassFullName;
    }

    private JClass sanitizeIfNecessary(JClass jaxbTypeParameter) {
        if (jaxbTypeParameter.name().contains("?")) {
            jaxbTypeParameter = jaxbTypeParameter._extends();
        }
        return jaxbTypeParameter;
    }

    private void generateConstructor(JDefinedClass deserClass, JClass realDeserializingType) {
        final JMethod defaultConstructor = deserClass.constructor(JMod.PUBLIC);

        /*
         * Due to genericized class needing to be communicated as a type, TypeFactory
         * is the only way to communicate the Generic class and its type argument properly
         */
        final JClass typeFactory = codeModel.ref(TypeFactory.class);
        final JInvocation typeFactoryInstance = typeFactory.staticInvoke("defaultInstance");
        final JInvocation typeCreator =
                typeFactoryInstance.invoke("constructParametricType")
                        .arg(JExpr.dotclass(jaxbElementClass))
                        .arg(JExpr.dotclass(sanitizeIfNecessary(realDeserializingType)));

        final JInvocation superInvocation = JExpr.invoke("super").arg(typeCreator);

        final JBlock body = defaultConstructor.body();
        body.add(superInvocation);
    }

    /**
     * Generate the deserialization method
     * <p>
     * The generated method will look as:
     * <pre>
     * {@code
     *      @Override
     *      public JAXBElement<Type> deserialize(JsonParser p, DeserializationContext ctxt)
     *                                                throws IOException, JsonProcessingException {
     *          final Type value = p.readValueAs(Type.class);
     *          return new ObjectFactory().createSelectorExtensionTypePhases(value);
     *      }
     * }
     * </pre>
     *
     * @param deserClass
     *            The newly generated de-serialzer class to generate the implementation of
     *            {@link JsonDeserializer#deserialize(JsonParser, DeserializationContext)} method in
     * @param jaxbDeserializingType
     *            type representing the {@link JAXBElement} wrapped type narrowed to
     *            {@code realDeserializingType}
     * @param fieldOutline
     *            {@link FieldOutline} representation of the field for whom the deserializer is
     *            being generated
     * @param realDeserializingType
     *            the type of the object that the payload can be parsed to and subsequently wrapped
     *            within a {@link JAXBElement} type
     * @param locator
     *            {@link Locator} object representing the location of the field in the .xsd files
     */
    private void generateDeserializationMethod(JDefinedClass deserClass, JClass jaxbDeserializingType,
                                               FieldOutline fieldOutline, JClass realDeserializingType,
                                               Locator locator) {

        final JMethod deserMethod = deserClass.method(JMod.PUBLIC, jaxbDeserializingType, DESERIALIZE_METHOD);
        final JVar parserParam = deserMethod.param(JsonParser.class, JSON_PARSER_ARG);
        deserMethod.param(DeserializationContext.class, DESERIALIZATION_CONTEXT_ARG);

        deserMethod._throws(IOException.class);
        deserMethod._throws(JsonProcessingException.class);

        deserMethod.annotate(Override.class);

        final JBlock body = deserMethod.body();

        final JInvocation parserInvocation =
                JExpr.invoke(parserParam, "readValueAs")
                        .arg(sanitizeIfNecessary(realDeserializingType).dotclass());

        final JVar parsedValue =
                body.decl(JMod.FINAL, sanitizeIfNecessary(realDeserializingType), "value", parserInvocation);

        final JInvocation conversionInvocation = getJaxbConversionInvocation(realDeserializingType, fieldOutline);
        if (conversionInvocation == null) {
            errorReceiver.error(locator,
                    String.format("Unable to find an ObjectFactory to convert %s to JAXBElement<%s>",
                            realDeserializingType.fullName(), realDeserializingType.name()));
            body._return(JExpr._null());
            return;
        }

        conversionInvocation.arg(parsedValue);
        body._return(conversionInvocation);
    }

    /**
     * Locates the appropriate {@code DataType} -> {@code JAXBElement<DataType>} conversion method
     * with an {@code ObjectFactory}
     * <p>
     * First the appropriate {@code ObjectFactory} is located as based on {@code DataType} follows:
     * <ul>
     *   <li>For the generated {@code DataType} from the .xsd schema files, we choose the {@code ObjectFactory} in the
     *       same package as the {@code DataType}</li>
     *   <li>For a java language type, we choose the {@code ObjectFactory} in the same package as the generated class
     *       containing the field whose type is {@code DataType}</li>
     * </ul>
     *
     * @param realDeserializingType
     *            The {@code DataType} which is wrapped in {@link JAXBElement} and to the object of
     *            which type, the json is deserialized to
     * @param fieldOutline
     *            {@link FieldOutline} representing the field to be de-serialized
     * @return
     */
    private JInvocation getJaxbConversionInvocation(JClass realDeserializingType, FieldOutline fieldOutline) {
        final JClass deserializingType = sanitizeIfNecessary(realDeserializingType);
        final JPackage typePackage = deserializingType._package();
        final JDefinedClass objectFactory;
        if (typePackage.name().startsWith("java")) {
            objectFactory = fieldOutline.parent()._package().objectFactory();
        } else {
            objectFactory = outline.getPackageContext(typePackage).objectFactory();
        }

        for (final JMethod method : objectFactory.methods()) {
            final boolean argMatch = method.hasSignature(new JType[]{deserializingType});
            final boolean returnTypeMatch = isJAXBElementType(method.type());
            if (argMatch && returnTypeMatch) {
                return JExpr.invoke(JExpr._new(objectFactory), method);
            }
        }

        return null;
    }
}

