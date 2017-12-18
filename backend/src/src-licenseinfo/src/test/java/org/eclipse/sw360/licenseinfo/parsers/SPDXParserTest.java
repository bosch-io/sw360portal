/*
 * Copyright Bosch Software Innovations GmbH, 2016-2017.
 * Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.licenseinfo.parsers;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.eclipse.sw360.datahandler.common.SW360Constants;
import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.eclipse.sw360.datahandler.thrift.Visibility;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentType;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseNameWithText;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.model.SpdxDocument;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.sw360.licenseinfo.TestHelper.*;
import static org.eclipse.sw360.licenseinfo.parsers.SPDXParser.FILETYPE_SPDX_INTERNAL;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author maximilian.huber@tngtech.com
 */
@RunWith(DataProviderRunner.class)
public class SPDXParserTest {

    private User dummyUser = new User().setEmail("dummy@some.domain");

    private SPDXParser parser;

    private AttachmentContentStore attachmentContentStore;

    @Mock
    private AttachmentConnector connector;

    private static final String spdxExampleFile = "SPDXRdfExample-v2.0.rdf";
    private static final String spdx11ExampleFile = "SPDXRdfExample-v1.1.rdf";
    private static final String spdx12ExampleFile = "SPDXRdfExample-v1.2.rdf";

    @DataProvider
    public static Object[][] dataProviderAdd() {
        // @formatter:off
        return new Object[][] {
                { spdxExampleFile,
                        Arrays.asList("Apache License 2.0", "GNU Library General Public License v2 only", "1", "GNU General Public License v2.0 only", "CyberNeko License", "2"),
                        4,
                        "Copyright 2008-2010 John Smith" },
                { spdx11ExampleFile,
                        Arrays.asList("4", "1", "Apache License 2.0", "2", "Apache License 1.0", "Mozilla Public License 1.1", "CyberNeko License"),
                        2,
                        "Hewlett-Packard Development Company, LP" },
                { spdx12ExampleFile,
                        Arrays.asList("4", "1", "Apache License 2.0", "2", "Apache License 1.0", "Mozilla Public License 1.1", "CyberNeko License"),
                        3,
                        "Hewlett-Packard Development Company, LP" },
        };
        // @formatter:on
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        attachmentContentStore = new AttachmentContentStore(connector);

        parser = new SPDXParser(connector, attachmentContentStore.getAttachmentContentProvider());

        attachmentContentStore.put(spdxExampleFile);
        attachmentContentStore.put(spdx11ExampleFile);
        attachmentContentStore.put(spdx12ExampleFile);
    }

    private void assertIsResultOfExample(LicenseInfo result, String exampleFile, List<String> expectedLicenses, int numberOfCoyprights, String exampleCopyright){
        assertLicenseInfo(result);

        assertThat(result.getFilenames().size(), is(1));
        assertThat(result.getFilenames().get(0), is(exampleFile));

        assertThat(result.getLicenseNamesWithTextsSize(), is(expectedLicenses.size()));

        List<String> actualLicenses =
                result.getLicenseNamesWithTexts().stream()
                        .map(LicenseNameWithText::getLicenseName)
                        .collect(Collectors.toList());

        assertThat(actualLicenses, containsInAnyOrder(expectedLicenses.toArray()));

        List<String> licenseTexts = result.getLicenseNamesWithTexts().stream()
                .map(LicenseNameWithText::getLicenseText).collect(Collectors.toList());

        assertThat(licenseTexts, hasItem(containsString("The CyberNeko Software License, Version 1.0")));

        assertThat(result.getCopyrightsSize(), is(numberOfCoyprights));
        assertThat(result.getCopyrights(), hasItem(containsString(exampleCopyright)));
    }

    @Test
    @UseDataProvider("dataProviderAdd")
    public void testAddSPDXContentToCLI(String exampleFile, List<String> expectedLicenses, int numberOfCoyprights, String exampleCopyright) throws Exception {
        AttachmentContent attachmentContent = new AttachmentContent()
                .setFilename(exampleFile);

        InputStream input = makeAttachmentContentStream(exampleFile);
        SpdxDocument spdxDocument = SPDXDocumentFactory.createSpdxDocument(input,
                parser.getUriOfAttachment(attachmentContentStore.get(exampleFile)),
                FILETYPE_SPDX_INTERNAL);

        LicenseInfoParsingResult result = SPDXParserTools.getLicenseInfoFromSpdx(attachmentContent, spdxDocument);
        assertIsResultOfExample(result.getLicenseInfo(), exampleFile, expectedLicenses, numberOfCoyprights, exampleCopyright);
    }

    @Test
    @UseDataProvider("dataProviderAdd")
    public void testGetLicenseInfo(String exampleFile, List<String> expectedLicenses, int numberOfCoyprights, String exampleCopyright) throws Exception {

        Attachment attachment = makeAttachment(exampleFile,
                Arrays.stream(AttachmentType.values())
                        .filter(SW360Constants.LICENSE_INFO_ATTACHMENT_TYPES::contains)
                        .findAny()
                        .get());

        LicenseInfoParsingResult result = parser.getLicenseInfos(attachment, dummyUser,
                                            new Project()
                                                    .setVisbility(Visibility.ME_AND_MODERATORS)
                                                    .setCreatedBy(dummyUser.getEmail())
                                                    .setAttachments(Collections.singleton(new Attachment().setAttachmentContentId(attachment.getAttachmentContentId()))))
                .stream()
                .findFirst()
                .orElseThrow(()->new RuntimeException("Parser returned empty LicenseInfoParsingResult list"));

        assertLicenseInfoParsingResult(result);
        assertIsResultOfExample(result.getLicenseInfo(), exampleFile, expectedLicenses, numberOfCoyprights, exampleCopyright);
    }
}
