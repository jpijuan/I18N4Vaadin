/*
 * Copyright (c) 2012 Petter Holmström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.peholmst.i18n4vaadin.ap;

import com.github.peholmst.i18n4vaadin.annotations.Message;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.PackageElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Class for generating the bundle ({@code messages.properties}) files.
 * <p><b>This class is internal and should never be used by clients.</b>
 *
 * @author Petter Holmström
 */
class BundleFileGenerator extends AbstractFileGenerator {

    BundleFileGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    void process(final PackageMap packageMap) {
        for (final PackageInfo pkg : packageMap.getAllPackages()) {
            processPackage(pkg);
        }
    }

    void processPackage(final PackageInfo packageInfo) {
        for (final Locale locale : packageInfo.getLocales()) {
            final Properties props = new Properties();
            for (final Message message : packageInfo.getMessages(locale)) {
                props.put(message.key(), message.value());
            }
            try {
                final OutputStream out = getBundleForWriting(packageInfo.getElement(), locale).openOutputStream();
                try {
                    props.store(out, "Auto-generated by " + getClass().getName());
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not save bundle: " + e.getMessage(), packageInfo.getElement());
            }
        }
    }

    FileObject getBundleForWriting(final PackageElement pkg, final Locale locale) throws IOException {
        final Filer filer = processingEnv.getFiler();
        final String packageName = pkg.getQualifiedName().toString();
        final String fileName = "messages" + getBundleSuffix(locale) + ".properties";
        final FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, packageName, fileName);
        return fo;
    }

    private static String getBundleSuffix(final Locale locale) {
        if (locale == null) {
            return "";
        } else {
            return "_" + locale.toString();
        }
    }
}
