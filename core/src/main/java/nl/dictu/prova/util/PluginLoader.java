/**
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * <p>
 * http://ec.europa.eu/idabc/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * <p>
 * Date:      18-12-2016
 * Author(s): Sjoerd Boerhout, Robert Bralts & Coos van der Galiën
 * <p>
 */
package nl.dictu.prova.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Load a Jar file from a given URL
 * 
 * @author Sjoerd Boerhout
 * @since 2016-04-22
 */
public class PluginLoader extends URLClassLoader {
    private final static URL urls[] = {};

    final static Logger LOGGER = LogManager.getLogger();

    /**
     * Constructor
     * 
     * @param urls
     */
    public PluginLoader() {
        super(urls);
    }

    /**
     * Constructor with url(s)
     * 
     * @param urls
     */
    public PluginLoader(URL[] urls) {
        super(urls);
    }

    /**
     * Add the given file to the Java class path
     * 
     * @param path
     * @throws MalformedURLException
     */
    public void addFile(String path) throws Exception, MalformedURLException {
        try {
            LOGGER.trace("Try to add file to plugin loader: {}", path);

            super.addURL(new File(path).toURI().toURL());
            LOGGER.trace("Added '{}' to classpath", new File(path).toURI().toURL());
        } catch (MalformedURLException eX) {
            LOGGER.trace("Malformed URL: {}", eX.getMessage());
            throw eX;
        } catch (Exception eX) {
            LOGGER.trace("Unhandled exception: {}", eX.getMessage());
            throw eX;
        }
    }

    /**
     * Add all files from the given directory with correct file extension to the Java class path
     * 
     * @param dirName
     * @param fileExt
     */
    public void addFiles(String dirName, String fileExt) {
        try {
            LOGGER.debug("Try to load all plugins from '{}' with ext '{}' to plugin loader.", () -> dirName,
                    () -> fileExt);

            File dir = new File(dirName);

            if (dir.exists() && dir.canRead())
                addAllFiles(dir, fileExt);
            else
                throw new Exception("Directory name for plugins can't be read.(" + dirName + ")");
        } catch (Exception eX) {
            LOGGER.warn(eX.getMessage());
        }
    }

    /**
     * Add all file's with given file extension to the class path.
     * 
     * @param rootDir
     * @param fileExt
     */
    private void addAllFiles(File rootDir, String fileExt) {
        for (File file : rootDir.listFiles()) {
            try {
                if (file.isFile() && file.canRead()) {
                    if (file.getAbsolutePath().toLowerCase().endsWith(fileExt.toLowerCase())) {
                        super.addURL(file.toURI().toURL());
                        LOGGER.trace("Added '{}' to classpath", file.toURI().toURL());
                    }
                } else if (file.isDirectory() && file.canRead()) {
                    addAllFiles(file, fileExt);
                } else {
                    LOGGER.warn("Unable to search '{}' for plugins!", () -> file.getAbsolutePath());
                }
            } catch (Exception eX) {
                LOGGER.warn(eX.getMessage());
            }
        }
    }

    /**
     * Get an instance of the given class name and type
     * 
     * @param className
     * @param classType
     * @return
     * @throws Exception
     */
    public <T> T getInstanceOf(final String className, final Class<T> classType) throws Exception {
        try {
            LOGGER.trace("Try to load class '{}' of type '{}'", () -> className, () -> classType.getName());

            return classType.cast(this.loadClass(className).newInstance());
        } catch (Exception eX) {
            LOGGER.trace("Exception: {}", eX.getMessage());
            throw eX;
        }
    }
}
