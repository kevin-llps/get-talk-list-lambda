package fr.kevin.llps.lambda.utils;

import java.util.Optional;

public class Utils {

    public static String getEnvironmentVarValue(String environmentVarName) {
        return Optional.ofNullable(System.getenv(environmentVarName))
                .orElseThrow(() -> new IllegalArgumentException("Environment variable " + environmentVarName + " does not exist"));
    }

}
