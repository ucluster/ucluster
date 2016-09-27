package com.github.ucluster.common.junit;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;

public class ResourceReader {
    public static String read(String classpath) {
        try {
            return Resources.toString(Resources.getResource(classpath), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
