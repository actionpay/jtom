package net.actionpay.jtom;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Temp on 15.05.2015.
 */
public class AnnotationScanner {
    static List<Class> classes = new ArrayList<Class>();
    static {
        try {
            Enumeration<URL> enumeration = Thread.currentThread().getContextClassLoader().getResources("./");
            List<File> dirs = new ArrayList<File>();
            while (enumeration.hasMoreElements()) {
                URL resource = enumeration.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            classes.clear();
            for (File directory : dirs) {
                classes.addAll(findClasses(directory));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static private List<Class> findClasses(File directory) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file));
            } else if (file.getName().endsWith(".class")) {
                try {
                    classes.add(Class.forName(file.getName().substring(0, file.getName().length() - 6)));
                }catch (Exception ex){}
            }
        }
        return classes;
    }

    public static Stream<Class> find(Class<? extends Annotation> annotation) {

        return classes.stream().filter(cl->cl.isAnnotationPresent(annotation));
    }
}
