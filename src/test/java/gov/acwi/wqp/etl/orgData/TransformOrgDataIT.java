package gov.acwi.wqp.etl.orgData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import gov.acwi.wqp.etl.NwisBaseFlowIT;

public class TransformOrgDataIT extends NwisBaseFlowIT {

	@Autowired
	@Qualifier("orgDataFlow")
	private Flow orgDataFlow;

	@Test
	@DatabaseSetup(value = "classpath:/testResult/wqp/orgData/empty.xml")
	@DatabaseSetup(connection = "nwis", value = "classpath:/testData/nwis/nwisDistrictCdsByHost/nwisDistrictCdsByHost.xml")
	@ExpectedDatabase(value = "classpath:/testResult/wqp/orgData/orgData.xml", assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void transformNwisOrgDataStepTest() {
		try {
			JobExecution jobExecution = jobLauncherTestUtils.launchStep("transformOrgDataStep", testJobParameters);
			assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test
	@DatabaseSetup(value = "classpath:/testResult/wqp/orgData/empty.xml")
	@DatabaseSetup(value = "classpath:/testData/wqp/orgData/orgDataOld.xml")
	@DatabaseSetup(connection = "nwis", value = "classpath:/testData/nwis/nwisDistrictCdsByHost/nwisDistrictCdsByHost.xml")
	@ExpectedDatabase(value = "classpath:/testResult/wqp/orgData/indexes/all.xml",
		assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED, 
		table = EXPECTED_DATABASE_TABLE_CHECK_INDEX,
		query=BASE_EXPECTED_DATABASE_QUERY_CHECK_INDEX + "'org_data_swap_nwis'")
	@ExpectedDatabase(connection = "pg", 
		value = "classpath:/testResult/wqp/orgData/create.xml", 
		assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED, 
		table = EXPECTED_DATABASE_TABLE_CHECK_TABLE, 
		query = BASE_EXPECTED_DATABASE_QUERY_CHECK_TABLE + "'org_data_swap_nwis'")
	@ExpectedDatabase(value = "classpath:/testResult/wqp/orgData/orgData.xml", assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void orgDataFlowTest() {
		Job orgDataFlowTest = jobBuilderFactory.get("orgDataFlowTest")
				.start(orgDataFlow)
				.build()
				.build();
	jobLauncherTestUtils.setJob(orgDataFlowTest);
		try {
			JobExecution jobExecution = jobLauncherTestUtils.launchJob(testJobParameters);
			assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
}