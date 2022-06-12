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

    public String loadArrayRefAndIndex(ArrayOperand operand, HashMap<String, Descriptor> varTable){
        StringBuilder code = new StringBuilder();
        if(operand.isParameter()){
            code.append("\taload ");
            code.append(operand.getParamId());
            code.append("\n\t");
        } else {
            var virtualReg = -1;

            if(varTable.get(operand.getName()) != null)
                virtualReg = varTable.get(operand.getName()).getVirtualReg();
            code.append("\taload ");
            code.append(virtualReg);
            code.append("\n\t");
        }
        code.append(this.loadArrayIndexes(operand, varTable));
        code.append("\n");
        return code.toString();
    }

    public String storeElement(Element element, HashMap<String, Descriptor> varTable)
    {
        System.out.println("INSIDE STORE");
        String instrStr ="";
        if(element.isLiteral()){
            System.out.println("INSIDE LITERAL");
            LiteralElement lit = (LiteralElement) element;

            switch (lit.getType().getTypeOfElement())
            {
                case INT32:
                    instrStr +="\t";
                    instrStr += getIStore(Integer.parseInt(lit.getLiteral()));
                    break;
                default:
                    instrStr +="\t";
                    instrStr += "store " + lit.getLiteral();
                    break;
            }

        } else {
            System.out.println("INSIDE ELSE");
            Operand operand = (Operand) element;
            boolean isArrayOperand = false;
            if(operand instanceof ArrayOperand){
                System.out.println("found arrayoperand " + operand.getType().getTypeOfElement());
                isArrayOperand = true;
            }
            ElementType elemType = operand.getType().getTypeOfElement();
            System.out.println("elemtype = " + elemType);
            if (operand.isParameter()) {
                switch (elemType) {
                    case INT32:
                        instrStr +="\t";
                        if(isArrayOperand){
                            instrStr += "iastore";
                        } else {
                            instrStr += getIStore(operand.getParamId());
                        }
                        break;
                    case BOOLEAN:
                        instrStr +="\t";
                        instrStr += getIStore(operand.getParamId());
                        break;
                    case ARRAYREF:
                        instrStr +="\t";
                        instrStr += "aastore " + operand.getParamId();
                        break;
                    case OBJECTREF:
                        instrStr +="\t";
                        instrStr += "astore " + operand.getParamId();
                        break;
                    case CLASS:
                        instrStr +="\t";
                        instrStr += "astore " + operand.getParamId();
                        break;
                    case THIS:
                        instrStr +="\t";
                        instrStr += "astore_0";
                        break;
                    case STRING:
                        instrStr +="\t";
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
                            instrStr +="\t";
                            if(isArrayOperand){
                                instrStr += "iastore";
                            } else {
                                instrStr += getIStore(virtualReg);
                            }

                            instrStr += "\n";
                            break;
                        case BOOLEAN:
                            instrStr +="\t";
                            instrStr += getIStore(virtualReg);
                            instrStr += "\n";
                            break;
                        case ARRAYREF:
                            instrStr +="\t";
                            instrStr += "astore " + virtualReg + "\n";
                            break;
                        case OBJECTREF:
                            instrStr +="\t";
                            instrStr += "astore " + virtualReg;
                            instrStr += "\n";

                            break;
                        case CLASS:
                            instrStr +="\t";
                            instrStr += "astore " + virtualReg;
                            instrStr += "\n";
                            break;
                        case THIS:
                            instrStr +="\t";
                            instrStr += "astore_0\n";
                            break;
                        case STRING:
                            instrStr +="\t";
                            instrStr += "astore " + virtualReg;
                            instrStr += "\n";
                            break;
                        case VOID:
                            break;
                    }
                } else {
                    instrStr += "\tputfield " + getJasminType(elemType) + "/" + operand.getName() + " " + getJasminType(elemType);
                }

            }
        }

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
            case ARRAYREF:
                return "a";
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
                    instrStr = "\tldc " + lit.getLiteral();
                    break;
            }

        } else{

            Operand operand = (Operand) element;
            boolean isArrayOperand = false;
            if(operand instanceof ArrayOperand){
                System.out.println("found arrayoperand ");
                operand.show();
                isArrayOperand = true;

            }

            ElementType elemType = operand.getType().getTypeOfElement();
            if(operand.isParameter()) {
                switch (elemType) {
                    case INT32:
                        if(isArrayOperand) {
                            instrStr += "aload " + operand.getParamId() + "\n\t";
                            instrStr += loadArrayIndexes((ArrayOperand) operand, varTable);
                            instrStr += "iaload";
                        } else {
                            instrStr = getIload(operand.getParamId());
                        }

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

                var operandName = operand.getName();
                System.out.println(operandName);
                var virtualReg = -1;

                if(varTable.get(operandName) != null)
                     virtualReg = varTable.get(operandName).getVirtualReg();

                if(virtualReg != -1)
                {

                    switch (elemType) {
                        case INT32:
                            if(isArrayOperand) {
                                instrStr += "aload " + virtualReg + "\n\t";
                                instrStr += loadArrayIndexes((ArrayOperand) operand, varTable);
                                instrStr += "iaload";
                            }else {
                                instrStr = getIload(virtualReg);
                            }
                            break;
                        case BOOLEAN:
                            instrStr = getIload(virtualReg);
                            break;
                        case ARRAYREF:
                            instrStr = "aload " + virtualReg;
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
                        default:
                            throw new NotImplementedException(element);
                    }
                }
                else
                {


                    instrStr += "\tgetfield " + getJasminType(element.getType())+"/"+operand.getName() + " " ;
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
        else if (type instanceof ClassType) {
            //return getJasminType(type.getTypeOfElement());
            if(((ClassType) type).getName().equals("boolean")) return "Z";
            return ((ClassType) type).getName();
        }
        else if (type instanceof Type)
            return getJasminType(type.getTypeOfElement());
        else{
            throw new NotImplementedException(type);
        }

    }

    public String getJasminType(ElementType type)
    {
        System.out.println(type);
        switch (type)
        {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            /*TODO
            case ARRAYREF:
                //return "[" + ((ArrayType) typeOllir).getTypeOfElement();
                return "[";*/
            case OBJECTREF:
                return "";
            /*case CLASS:
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

    private String loadArrayIndexes(ArrayOperand operand, HashMap<String, Descriptor> varTable){
        StringBuilder code = new StringBuilder();

        ArrayList<Element> list = operand.getIndexOperands();
        if(list.isEmpty()){
            return "";
        }

        code.append(this.loadElement(list.get(0), varTable)+ "\n\t") ;
        return code.toString();
    }
}
