package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.io.IOError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private final JasminUtils jasminUtils;

    public OllirToJasmin(ClassUnit classUnit)
    {
        this.classUnit = classUnit;
        this.jasminUtils = new JasminUtils(this.classUnit);

    }

    public String getCode()
    {
        var code = new StringBuilder();

        JasminInstruction.reset();

        code.append(createJasminHeader());
        code.append(createJasminFields());
        code.append(createJasminMethods());

        return code.toString();
    }


    public String createAccessSpecsStr(String classType, Boolean isStatic, Boolean isFinal)
    {
        var code = new StringBuilder();

        if (!classType.equals("DEFAULT"))
            code.append(classType.toLowerCase() + " ");
        if (isStatic)
            code.append("static ");
        if (isFinal)
            code.append("final ");

        return code.toString();
    }

    public String createClassDirective()
    {

        String classType = classUnit.getClassAccessModifier().name();
        String accessSpecsStr = createAccessSpecsStr(classType, classUnit.isStaticClass(), classUnit.isFinalClass());
        String className = classUnit.getClassName();
        String packageName = classUnit.getPackage();

        if (packageName != null) {
            className = packageName + "/" + className;
        }

        return ".class " + accessSpecsStr + className + '\n';
    }

    public String createSuperDirective()
    {
        String superClassName = classUnit.getSuperClass();
        String qualifiedSuperClassName = jasminUtils.getFullyQualifiedName(superClassName);

        return ".super " + qualifiedSuperClassName + '\n';
    }

    public String createJasminHeader()
    {
        String classDirective = createClassDirective();
        String superDirective = createSuperDirective();

        return classDirective + superDirective + "\n";
    }

    public String createJasminFields()
    {
        ArrayList<Field> fields = classUnit.getFields();
        var code = new StringBuilder();

        for (Field field : fields) {
            code.append(createField(field) + '\n');
        }

        code.append("\n");
        return code.toString();
    }

    public String createField(Field field)
    {
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

    public String createConstructMethod(String superClassName)
    {
        return String.format(".method public <init>()V\n" +
                "   aload_0\n" +
                "   invokenonvirtual %s/<init>()V\n" +
                "   return\n" +
                ".end method\n", superClassName);
    }

    public String createJasminMethods()
    {

        var code = new StringBuilder();
        ArrayList<Method> methods = classUnit.getMethods();

        for (Method method : methods) {
            code.append(getCode(method));
        }

        return code.toString();
    }


    public String createMethodBody(Method method)
    {
        var code = new StringBuilder();
        String methodBody = "";
        int instruction_stack = 0;
        int limit_stack = 0;

        String accessSpecs = createAccessSpecsStr(method.getMethodAccessModifier().name(), method.isStaticMethod(), method.isFinalMethod());
        code.append(accessSpecs + method.getMethodName() + '(');
        method.buildVarTable();

        var paramsTypes = method.getParams().stream()
                .map(element -> jasminUtils.getJasminType(element.getType(), true))
                .collect(Collectors.joining());
        code.append(paramsTypes).append(")").append(jasminUtils.getJasminType(method.getReturnType(), true) + '\n');

        int limitLocals =  method.getVarTable().size() +
                (method.getVarTable().containsKey("this") || method.isStaticMethod() ? 0 : 1);

        this.jasminUtils.resetStack();

        for (int i = 0; i < method.getInstructions().size(); i++) {

            if(i<method.getInstructions().size()-1) method.getInstr(i).addSucc(method.getInstr(i+1));

            JasminInstruction jasminInstruction = new JasminInstruction(classUnit, method);

            methodBody += jasminInstruction.getCode(method.getInstr(i));
            instruction_stack = jasminInstruction.getStackLimit();
            limit_stack = Math.max(instruction_stack, limit_stack);

        }

        code.append("\t.limit stack "+ limit_stack +"\n");
        code.append("\t.limit locals " + limitLocals + "\n");
        code.append(methodBody);



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
