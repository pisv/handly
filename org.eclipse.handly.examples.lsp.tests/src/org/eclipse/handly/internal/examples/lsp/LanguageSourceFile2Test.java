/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.lsp;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

/**
 * <code>LanguageSourceFile</code> tests using out-of-process language server.
 */
public class LanguageSourceFile2Test
    extends LanguageSourceFileTestBase
{
    @Override
    ServerManager getServerManager()
    {
        return ModelManager.INSTANCE.getServerManager();
    }

    public static class ServerConnectionProvider
        extends ProcessStreamConnectionProvider
    {
        public ServerConnectionProvider()
        {
            List<String> commands = new ArrayList<>();
            commands.add("java");
            //commands.add("-Xdebug");
            //commands.add("-Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=y,quiet=y");
            String className = LanguageSourceFile2TestServer.class.getName();
            commands.add("-classpath");
            StringBuilder classPath = new StringBuilder();
            String pathSeparator = System.getProperty("path.separator");
            String classFileName = className.replace('.', '/') + ".class";
            URL classFileUrl = Activator.getDefault().getBundle().getResource(
                classFileName);
            String path = getFilePath(classFileUrl);
            int index = path.indexOf(classFileName);
            if (index >= 0)
                path = path.substring(0, index);
            classPath.append(path);
            String[] requiredBundles = new String[] { "org.eclipse.lsp4j",
                "org.eclipse.lsp4j.jsonrpc", "org.eclipse.xtext.xbase.lib",
                "com.google.gson" };
            for (String bundle : requiredBundles)
            {
                classPath.append(pathSeparator);
                classPath.append(getFilePath(Platform.getBundle(
                    bundle).getResource("")));
            }
            commands.add(classPath.toString());
            commands.add(className);
            setCommands(commands);
            setWorkingDirectory(Platform.getLocation().toOSString());
        }

        private static String getFilePath(URL resource)
        {
            try
            {
                URL url = FileLocator.resolve(resource);
                if ("jar".equals(url.getProtocol()))
                {
                    JarURLConnection connection =
                        (JarURLConnection)url.openConnection();
                    url = connection.getJarFileURL();
                }
                return new File(url.toURI()).getAbsolutePath();
            }
            catch (URISyntaxException | IOException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }
}

class LanguageSourceFile2TestServer
{
    public static void main(String[] args)
    {
        TestLanguageServer server = new TestLanguageServer();
        server.setTextDocumentService(
            new LanguageSourceFile2Test.TestTextDocumentService());
        server.start();
    }
}
