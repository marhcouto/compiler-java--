package pt.up.fe.comp.semantic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Constants {
    private Constants() {}

    //Chosen because types starting with . are invalid
    public static final String ANY_TYPE = ".Any";
    public static final Set<String> primitives = new HashSet<>(Arrays.asList("int", "bool", "String"));
}