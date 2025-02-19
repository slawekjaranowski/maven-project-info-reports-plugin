/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.report.projectinfo.avatars;

import javax.inject.Named;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.codehaus.plexus.util.IOUtil;

/**
 * Provider for user avatar from gravatar.com
 * <p>
 * <a href="https://docs.gravatar.com/api/avatars/images/">Gravatar API</a>
 */
@Named("gravatar")
class GravatarProvider implements AvatarsProvider {

    private static final String AVATAR_SIZE = "s=60";

    private static final String AVATAR_DIRECTORY = "avatars";

    private String baseUrl = "https://www.gravatar.com/avatar/";

    private Path outputDirectory;

    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    @Override
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory.toPath();
    }

    public String getExternalAvatarUrl(String email) {

        if (email == null || email.isEmpty()) {
            return getSpacerGravatarUrl();
        }

        try {
            email = email.trim().toLowerCase(Locale.ROOT);
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            md.update(email.getBytes());
            byte[] byteData = md.digest();
            StringBuilder sb = new StringBuilder();
            final int lowerEightBitsOnly = 0xff;
            for (byte aByteData : byteData) {
                sb.append(Integer.toString((aByteData & lowerEightBitsOnly) + 0x100, 16)
                        .substring(1));
            }
            return baseUrl + sb + ".jpg?d=mp&" + AVATAR_SIZE;
        } catch (NoSuchAlgorithmException e) {
            return getSpacerGravatarUrl();
        }
    }

    @Override
    public String getLocalAvatarPath(String email) throws IOException {
        String externalAvatarUrl = getExternalAvatarUrl(email);
        try {
            URL url = new URI(externalAvatarUrl).toURL();
            Path name = Paths.get(url.getPath()).getFileName();
            Path outputPath = outputDirectory.resolve(AVATAR_DIRECTORY).resolve(name);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath.getParent());
                try (InputStream in = url.openStream();
                        OutputStream out = Files.newOutputStream(outputPath)) {
                    IOUtil.copy(in, out);
                }
            }
            return AVATAR_DIRECTORY + "/" + name;
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private String getSpacerGravatarUrl() {
        return baseUrl + "00000000000000000000000000000000.jpg?d=blank&f=y&" + AVATAR_SIZE;
    }
}
