package net.actionpay.jtom;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

/**
 * Simple directory class checker for annotations
 * todo: add *.jar read or replace by solid solution
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public class AnnotationScanner {
	static List<Class> classes = new ArrayList<>();

	static {
		try {
			Enumeration<URL> enumeration = Thread.currentThread().getContextClassLoader().getResources("./");
			List<File> dirs = new ArrayList<>();
			while (enumeration.hasMoreElements()) {
				URL resource = enumeration.nextElement();
				dirs.add(new File(resource.getFile()));
			}
			classes.clear();
			dirs.stream().forEach(directory -> classes.addAll(findClasses(directory)));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Search Classes at directory
	 *
	 * @param directory File
	 * @return list of classes
	 */
	static private List<Class> findClasses(File directory) {
		List<Class> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		Arrays.stream(directory.listFiles()).forEach(file -> {
			if (file.isDirectory()) {
				classes.addAll(findClasses(file));
			} else if (file.getName().endsWith(".class")) {
				try {
					classes.add(Class.forName(file.getName().substring(0, file.getName().length() - 6)));
				} catch (Exception ignored) {
				}
			}
		});
		return classes;
	}

	public static Stream<Class> find(Class<? extends Annotation> annotation) {

		return classes.stream().filter(cl -> cl.isAnnotationPresent(annotation));
	}
}
