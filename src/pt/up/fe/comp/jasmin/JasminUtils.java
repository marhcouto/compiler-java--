package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JasminUtils {
    ClassUnit classUnit;

    JasminUtils(ClassUnit classUnit)
    {
        this.classUnit = classUnit;
    }

    public String getFullyQualifiedName(String className){

        if(className == null) return "java/lang/Object";

        for(var importString : this.classUnit.getImports()){
            var splittedImport = importString.split("\\.");
            String lastName;

            if(splittedImport.length == 0){
                lastName = importString;
            }
            else{
                lastName = splittedImport[splittedImport.length-1];

            }
            if(lastName.equals(className)){
                return importString.replace('.', '/');
            }
        }

        if(this.classUnit.getClassName().equals(className)) return className;

        else throw new RuntimeException("Could not find import for class " + className);
    }

    public String getIload(int paramId){

        var code = new StringBuilder();

        code.append("iload");

        if(paramId < 4)
            code.append("_" + paramId);
        else
            code.append(" " + paramId);

        return code.toString();
    }

    public String getIStore(int paramId){

        var code = new StringBuilder();

        code.append("istore");

        if(paramId < 4)
            code.append("_" + paramId);
        else
            code.append(" " + paramId);

        return code.toString();
    }

    public String getIConst(String _const){

        var code = new StringBuilder();

        if(Integer.parseInt(_const) < 6)
            code.append("iconst_" + _const);
        else
            code.append("ldc " + _const);

        return code.toString();
    }

    public String storeElement(Element element, HashMap<String, Descriptor> varTable)
    {
        String instrStr ="";
        if(element.isLiteral()){
            LiteralElement lit = (LiteralElement) element;

            switch (lit.getType().getTypeOfElement())
            {
                case INT32:
                    instrStr += getIStore(Integer.parseInt(lit.getLiteral()));
                    break;
                default:
                    instrStr += "store " + lit.getLiteral();
                    break;
            }

        } else {
            Operand operand = (Operand) element;
            ElementType elemType = operand.getType().getTypeOfElement();
            if (operand.isParameter()) {
                switch (elemType) {
                    case INT32:
                        instrStr += getIStore(operand.getParamId());
                        break;
                    case BOOLEAN:
                        instrStr += getIStore(operand.getParamId());
                        break;
                    case ARRAYREF:
                        instrStr += "aastore " + operand.getParamId();
                        break;
                    case OBJECTREF:
                        instrStr += "astore " + operand.getParamId();
                        break;
                    case CLASS:
                        instrStr += "astore " + operand.getParamId();
                        break;
                    case THIS:
                        instrStr += "astore_0";
                        break;
                    case STRING:
                        instrStr += "astore " + operand.getParamId();
                        break;
                    case VOID:
                        break;
                }
            } else {
                int virtualReg = varTable.get(operand.getName()).getVirtualReg();

                if (virtualReg != -1) {
                    switch (elemType) {
                        case INT32:
                            instrStr += getIStore(virtualReg);
                            break;
                        case BOOLEAN:
                            instrStr += getIStore(virtualReg);
                            break;
                        case ARRAYREF:
                            instrStr += this.loadElement(getArrayIndex(operand), varTable) + "\n\t";
                            instrStr += "iastore";
                            break;
                        case OBJECTREF:
                            instrStr += "astore " + virtualReg;
                            break;
                        case CLASS:
                            instrStr += "astore " + virtualReg;
                            break;
                        case THIS:
                            instrStr += "astore_0";
                            break;
                        case STRING:
                            instrStr += "astore " + virtualReg;
                            break;
                        case VOID:
                            break;
                    }
                } else {
                    instrStr += "putfield " + getJasminType(elemType) + "/" + operand.getName() + " " + getJasminType(elemType);
                }

            }
        }
        instrStr += "\n";

        return instrStr;
    }

    public String getJasminReturnType(ElementType type){
        switch (type)
        {
            case INT32:
                return "i";
            case BOOLEAN:
                return "i";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "";
            default:
                throw new NotImplementedException(type);
        }
    }

    public String loadElement(Element element, HashMap<String, Descriptor> varTable){
        String instrStr ="";
        if(element.isLiteral()){
            LiteralElement lit = (LiteralElement) element;

            switch (lit.getType().getTypeOfElement())
            {
                case INT32:
                case BOOLEAN:
                    instrStr = getIConst(lit.getLiteral());
                    break;
                default:
                    instrStr = "ldc " + lit.getLiteral();
                    break;
            }

        } else{
            Operand operand = (Operand) element;
            ElementType elemType = operand.getType().getTypeOfElement();
            if(operand.isParameter()) {
                switch (elemType) {
                    case INT32:
                        instrStr = getIload(operand.getParamId());
                        break;
                    case BOOLEAN:
                        instrStr = getIload(operand.getParamId());
                        break;
                    case ARRAYREF:
                        instrStr = "aload " + operand.getParamId();
                        break;
                    case OBJECTREF:
                        instrStr = "aload " + operand.getParamId();
                        break;
                    case CLASS:
                        instrStr = "aload " + operand.getParamId();
                        break;
                    case THIS:
                        instrStr = "aload_0";
                        break;
                    case STRING:
                        instrStr = "aload " + operand.getParamId();
                        break;
                    case VOID:
                        break;
                }
            }
            else{
                int virtualReg = varTable.get(operand.getName()).getVirtualReg();
                if(virtualReg != -1)
                {
                    switch (elemType) {
                        case INT32:
                            instrStr = getIload(virtualReg);
                            break;
                        case BOOLEAN:
                            instrStr = getIload(virtualReg);
                            break;
                        case ARRAYREF:
                            //TODO
                            instrStr = "aload_1";
                            break;
                        case OBJECTREF:
                            instrStr = "aload " + virtualReg;
                            break;
                        case CLASS:
                            instrStr = "aload " + virtualReg;
                            break;
                        case THIS:
                            instrStr = "aload_0";
                            break;
                        case STRING:
                            instrStr = "aload " + virtualReg;
                            break;
                        case VOID:
                            break;
                    }
                }
                else
                {
                    instrStr += "getfield " + getJasminType(elemType)+"/"+operand.getName() + " ";
                }

            }

        }
        instrStr += "\n";
        return instrStr;
    }

    public String getJasminType(Type type)
    {
        if(type instanceof ArrayType)
        {
            return "[" + getJasminType(((ArrayType) type).getArrayType());
        }
        else if(type instanceof ClassType)
            return ((ClassType) type).getName();
        else if(type instanceof Type)
            return getJasminType(type.getTypeOfElement());
        else{
            throw new NotImplementedException(type.getTypeOfElement());
        }

    }

    public String getJasminType(ElementType type)
    {

        switch (type)
        {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            /*TODO
            case ARRAYREF:
                //return "[" + ((ArrayType) typeOllir).getTypeOfElement();
                return "[";
            case OBJECTREF:
                //return "L" + getFullyQualifiedName(classUnit.getSuperClass()) + ";";
                return "";
            case CLASS:
                return "[" + classUnit.getClassName() + ";";
            case THIS:
                return getFullyQualifiedName(classUnit.getSuperClass());*/
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            default:
                throw new NotImplementedException(type);
        }

    }

    public String getFieldSpecs(Operand firstArg, String secondArg) {
        var code = new StringBuilder();

        switch (firstArg.getType().getTypeOfElement()) {
            case THIS:
                code.append(this.classUnit.getClassName() + "/");
                code.append(secondArg);
                break;
            default:
                code.append(getFullyQualifiedName(firstArg.getName()) + "/" + secondArg);
                break;
        }

        return code.toString();
    }

    /**
     * Return operand
     * @param operand
     * @return
     */
    private Element getArrayIndex(Operand operand){
        if(operand instanceof ArrayOperand) {
            ArrayOperand arrOperand = (ArrayOperand)  operand;
            ArrayList<Element> indexes = arrOperand.getIndexOperands();
            if (!indexes.isEmpty()) {

                return  indexes.get(0);
            }
        }
        return null;
    }
}
