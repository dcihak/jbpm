/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.persistence.scripts;

import org.assertj.core.api.Assertions;
import org.assertj.core.internal.cglib.transform.impl.AccessFieldTransformer;
import org.flywaydb.core.Flyway;
import org.jbpm.persistence.scripts.util.TestsUtil;
import org.jbpm.persistence.util.PersistenceUtil;
import org.jbpm.services.task.impl.model.AttachmentImpl;
import org.jbpm.services.task.impl.model.ContentImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.test.util.PoolingDataSource;
import org.junit.*;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Content;
import org.kie.internal.task.api.model.AccessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import static org.jbpm.persistence.scripts.PersistenceUnit.DB_TESTING_UPDATE;
import static org.jbpm.persistence.util.PersistenceUtil.JBPM_PERSISTENCE_UNIT_NAME;

/**
 * Contains tests that test DDL scripts.
 */
public class DDLScriptsTest {
    private static final Logger logger = LoggerFactory.getLogger(DDLScriptsTest.class);
    private Flyway flyway = new Flyway();

    @BeforeClass
    public static void printHibernateVersion() {
        logger.info("Running with Hibernate " + org.hibernate.Version.getVersionString());
    }

    @Before
    public void clearSchema() {
        TestsUtil.clearSchema();
    }

    private static final String DB_DDL_SCRIPTS_RESOURCE_PATH = "/db/ddl-scripts";

    /**
     * Tests that DB schema is created properly using DDL scripts.
     */
    @Test
    public void createSchemaUsingDDLs() throws Exception {
        final TestPersistenceContext scriptRunnerContext = createAndInitPersistenceContext(PersistenceUnit.SCRIPT_RUNNER);
        PoolingDataSource srPds = scriptRunnerContext.getPds();
        Assert.assertNotNull(srPds);

//        AttachmentImpl attachment = new AttachmentImpl();
//        attachment.setAccessType(AccessType.Inline);
//        attachment.setAttachedAt(new Date());
//        attachment.setName("NAME");
//        ContentImpl content = new ContentImpl();
//        attachment.setContent(content);
//        attachment.setContentType("CONTENT TYPE");
//        attachment.setSize(10);

//        String dbScriptsLocation = TestsUtil.getDDLScriptDirByDatabaseType("db/ddl-scripts", scriptRunnerContext.getDatabaseType());
//
//        flyway.setDataSource(srPds);
//        flyway.setLocations(dbScriptsLocation);
//        flyway.baseline();
//        flyway.migrate();
        scriptRunnerContext.clean();

//        try {
//            scriptRunnerContext.executeScripts(new File(getClass().getResource(DB_DDL_SCRIPTS_RESOURCE_PATH).getFile()));
//        } finally {
//            scriptRunnerContext.clean();
//        }

        final TestPersistenceContext dbTestingContext = createAndInitPersistenceContext(PersistenceUnit.DB_TESTING_VALIDATE);
        dbTestingContext.startAndPersistSomeProcess("minimalProcess");
        Assert.assertTrue(dbTestingContext.getStoredProcessesCount() == 1);
        dbTestingContext.clean();

//        try {
//            dbTestingContext.startAndPersistSomeProcess("minimalProcess");
//            Assert.assertTrue(dbTestingContext.getStoredProcessesCount() == 1);
//        } finally {
//            dbTestingContext.clean();
//        }
    }

    /**
     * Simulates the default config for kie-server/kie-wb when deploying the apps for the first time (and without running the DDL scripts first)
     */
    @Test
    @Ignore
    public void runHibernateUpdateOnEmptyDB() throws Exception {
        final TestPersistenceContext dbTestingContext = createAndInitPersistenceContext(DB_TESTING_UPDATE);
        dbTestingContext.clean();
    }

    /**
     * Simulates the case when user executes DDL scripts before deploying the kie-server/kie-wb and leaves the hibernate
     * config untouched (thus using the default 'update')
     */
    @Test
    @Ignore
    public void createSchemaWithDDLsAndRunHibernateUpdate() throws Exception {
        final TestPersistenceContext scriptRunnerContext = createAndInitPersistenceContext(PersistenceUnit.SCRIPT_RUNNER);
        PoolingDataSource srPds = scriptRunnerContext.getPds();
        Assert.assertNotNull(srPds);
            //scriptRunnerContext.executeScripts(new File(getClass().getResource("/db/ddl-scripts").getFile()));

        flyway.setDataSource(srPds);
        flyway.baseline();
        flyway.migrate();
        scriptRunnerContext.clean();

        final TestPersistenceContext dbTestingContext = createAndInitPersistenceContext(DB_TESTING_UPDATE);
        dbTestingContext.clean();
    }

    private TestPersistenceContext createAndInitPersistenceContext(PersistenceUnit persistenceUnit) {
        TestPersistenceContext testPersistenceContext = new TestPersistenceContext();
        testPersistenceContext.init(persistenceUnit);
        return testPersistenceContext;
    }
}
