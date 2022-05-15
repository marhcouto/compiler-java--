package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.io.IOError;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private final JasminUtils jasminUtils;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.jasminUtils = new JasminUtils(this.classUnit);
    }

    public String getCode() {
        var code = new StringBuilder();

        code.append(createJasminHeader());
        code.append(createJasminFields());
        code.append(createMethods());

        return code.toString();
    }


    public String createAccessSpecsStr(String classType, Boolean isStatic, Boolean isFinal) {
        String accessSpecsStr = "";

        if (classType != "DEFAULT")
            accessSpecsStr += classType.toLowerCase() + " ";
        if (isStatic)
            accessSpecsStr += "static ";
        if (isFinal)
            accessSpecsStr += "final ";

        return accessSpecsStr;
    }

    public String createClassDirective() {

        String classType = classUnit.getClassAccessModifier().name();
        String accessSpecsStr = createAccessSpecsStr(classType, classUnit.isStaticClass(), classUnit.isFinalClass());
        String className = classUnit.getClassName();
        String packageName = classUnit.getPackage();

        if (packageName != null) {
            className = packageName + "/" + className;
        }

        return ".class " + accessSpecsStr + className + '\n';
    }

    public String createSuperDirective() {

        String superClassName = classUnit.getSuperClass();
        String qualifiedSuperClassName = jasminUtils.getFullyQualifiedName(superClassName);

        return ".super " + qualifiedSuperClassName + '\n';
    }

    public String createJasminHeader() {
        String classDirective = createClassDirective();
        String superDirective = createSuperDirective();

        return classDirective + superDirective + "\n";
    }

    public String createJasminFields() {

        ArrayList<Field> fields = classUnit.getFields();

        var code = new StringBuilder();

        for (Field field : fields) {
            code.append(createField(field) + '\n');
        }

        code.append("\n");
        return code.toString();
    }

    public String createField(Field field) {
        var code = new StringBuilder();

        code.append(".field ");

        String accModifiers = field.getFieldAccessModifier().name();
        String accessModifiers = createAccessSpecsStr(accModifiers, field.isStaticField(), field.isFinalField());

        code.append(accessModifiers + field.getFieldName() + " ");
        String fieldType = jasminUtils.getJasminType(field.getFieldType());

        if (field.isInitialized())
            code.append("=" + field.getInitialValue());

        code.append(fieldType);

        return code.toString();
    }

    public String createConstructMethod(String superClassName) {

        String constructStr = SpecsIo.getResource("fixtures/public/jasmin/jasminConstructor.template").replace("$<SUPERCLASS_NAME>", superClassName);

        return constructStr;

    }

    public String createMethods() {

        String methodsStr = "";
        ArrayList<Method> methods = classUnit.getMethods();

        for (Method method : methods) {
            String fieldStr = getCode(method);
            methodsStr += fieldStr;

        }

        return methodsStr;
    }


    public String createMethodBody(Method method) {
        method.show();
        var code = new StringBuilder();
        //int varId = method.getParams().size() + 1;

        String accessSpecs = createAccessSpecsStr(method.getMethodAccessModifier().name(), method.isStaticMethod(), method.isFinalMethod());
        code.append(accessSpecs + method.getMethodName() + '(');

        var paramsTypes = method.getParams().stream()
                .map(element -> jasminUtils.getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(paramsTypes).append(")").append(jasminUtils.getJasminType(method.getReturnType()) + '\n');

        code.append("\t.limit stack 99\n");
        code.append("\t.limit locals 99\n");
        for (var instruction : method.getInstructions()) {
            //varId = method.addToVartable(instruction, varId);
            var jasminInstruction = new JasminInstruction(classUnit, method);
            code.append(jasminInstruction.getCode(instruction));
        }

        return code.toString();
    }

    public String createReturnStatment(Type type)
    {
        switch(type.getTypeOfElement())
        {
            case VOID:
                return "\treturn\n";
            default:
                return "";
        }
    }

    public String createNonConstructMethod(Method method) {

        var code = new StringBuilder();

        code.append(".method ");
        code.append(createMethodBody(method));
        code.append(createReturnStatment(method.getReturnType()));
        code.append(".end method\n");

        return code.toString();
    }

    public String getCode(Method method) {

        var code = new StringBuilder();

        if (method.isConstructMethod()) {
            code.append(createConstructMethod(jasminUtils.getFullyQualifiedName(classUnit.getSuperClass())));
        } else {
            code.append(createNonConstructMethod(method));
        }

        return code.toString();

    }



}
