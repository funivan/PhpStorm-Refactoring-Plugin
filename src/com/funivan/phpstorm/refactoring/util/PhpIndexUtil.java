package com.funivan.phpstorm.refactoring.util;

import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Ivan Shcherbak <alotofall@gmail.com>
 */
public class PhpIndexUtil {

    @Nullable
    public static Method getClassMethod(PhpClass phpClass, String methodName) {
        Collection<Method> classMethods = phpClass.getMethods();
        for (Method m : classMethods) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }

        return null;
    }

}
