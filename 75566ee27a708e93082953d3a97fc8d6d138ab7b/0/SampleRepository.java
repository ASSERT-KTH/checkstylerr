package ca.corefacility.bioinformatics.irida.repositories.sample;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectSampleJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.repositories.IridaJpaRepository;
import ca.corefacility.bioinformatics.irida.ria.web.admin.dto.statistics.GenericStatModel;

/**
 * A repository for storing Sample objects
 * 
 */
public interface SampleRepository extends IridaJpaRepository<Sample, Long>, SampleRepositoryCustom {
	/**
	 * Get a {@link Sample} with the given string sample identifier from a
	 * specific project.
	 * 
	 * @param p
	 *            The {@link Project} that the {@link Sample} belongs to.
	 * @param sampleName
	 *            The string sample name for a sample
	 * @return The {@link Sample} for this identifier
	 * @throws EntityNotFoundException
	 *             if a sample with this identifier doesn't exist
	 */
	@Query("select j.sample from ProjectSampleJoin j where j.project = ?1 and j.sample.sampleName = ?2")
	public Sample getSampleBySampleName(Project p, String sampleName) throws EntityNotFoundException;

	/**
	 * Get a {@link Page} of {@link Sample}s based on a list of {@link Sample}
	 * names
	 *
	 * @param project
	 *            The {@link Project} that the {@link Sample} belongs to.
	 * @param sampleNames
	 *            A {@link List} of {@link String} sample names
	 * @param pageable
	 *            {@link Pageable} information about which {@link Sample}s to
	 *            retrun
	 *
	 * @return The {@link Page} of {@link ProjectSampleJoin}
	 */
	@Query("select j from ProjectSampleJoin j where j.project = ?1 and j.sample.sampleName in ?2")
	public Page<ProjectSampleJoin> findSampleByNameInProject(Project project, List<String> sampleNames,
			Pageable pageable);

	/**
	 * Get the {@link Sample}s associated with a given
	 * {@link AnalysisSubmission}
	 * 
	 * @param analysisSubmission
	 *            the {@link AnalysisSubmission}.
	 * @return the set of associated {@link Sample}s
	 */
	@Query("SELECT j.sample FROM SampleSequencingObjectJoin j WHERE ?1 in elements(j.sequencingObject.analysisSubmissions)")
	public Set<Sample> findSamplesForAnalysisSubmission(AnalysisSubmission analysisSubmission);

	/**
	 * Get a count of all {@link Sample}s created within time period
	 *
	 * @return a count of {@link Sample}s
	 */
	@Query("select count(s.id) from Sample s where s.createdDate >= ?1")
	public Long countSamplesCreatedInTimePeriod(Date createdDate);

	/**
	 * Get a list of {@link GenericStatModel}s for samples created in the last day and grouped by hour
	 *
	 * @param createdDate The minimum created date for samples
	 * @return A list of {@link GenericStatModel}s
	 */
	@Query("select new ca.corefacility.bioinformatics.irida.ria.web.admin.dto.statistics.GenericStatModel(function('date_format', s.createdDate, '%H:00'), count(s.id))"
			+ "from Sample s where s.createdDate >= ?1 group by function('date_format', s.createdDate, '%H')")
	public List<GenericStatModel> countSamplesCreatedHourly(Date createdDate);

	/**
	 * Get a list of {@link GenericStatModel}s for samples created in the past 30 days and grouped by month and day
	 *
	 * @param createdDate The minimum created date for samples
	 * @return A list of {@link GenericStatModel}s
	 */
	@Query("select new ca.corefacility.bioinformatics.irida.ria.web.admin.dto.statistics.GenericStatModel(function('date_format', s.createdDate, '%m/%d'), count(s.id))"
			+ "from Sample s where s.createdDate >= ?1 group by function('date_format', s.createdDate, '%m/%d')")
	public List<GenericStatModel> countSamplesCreatedDaily(Date createdDate);

	/**
	 * Get a list of {@link GenericStatModel}s for samples created in the past 365 days and grouped by month and year
	 *
	 * @param createdDate The minimum created date for samples
	 * @return A list of {@link GenericStatModel}s
	 */
	@Query("select new ca.corefacility.bioinformatics.irida.ria.web.admin.dto.statistics.GenericStatModel(function('date_format', s.createdDate, '%m/%y'), count(s.id))"
			+ "from Sample s where s.createdDate >= ?1 group by function('date_format', s.createdDate, '%m/%y')")
	public List<GenericStatModel> countSamplesCreatedMonthly(Date createdDate);

	/**
	 * Get a list of {@link GenericStatModel}s for samples created in the past 2,5 and 10 years and grouped by year
	 *
	 * @param createdDate The minimum created date for samples
	 * @return A list of {@link GenericStatModel}s
	 */
	@Query("select new ca.corefacility.bioinformatics.irida.ria.web.admin.dto.statistics.GenericStatModel(function('date_format', s.createdDate, '%Y'), count(s.id))"
			+ "from Sample s where s.createdDate >= ?1 group by function('date_format', s.createdDate, '%Y')")
	public List<GenericStatModel> countSamplesCreatedYearly(Date createdDate);
}
