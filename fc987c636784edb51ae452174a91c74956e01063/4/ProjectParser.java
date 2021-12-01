package com.synaptix.toast.runtime.parse;

import com.synaptix.toast.dao.domain.impl.report.Campaign;
import com.synaptix.toast.dao.domain.impl.report.Project;
import com.synaptix.toast.dao.domain.impl.test.block.*;
import com.synaptix.toast.dao.domain.impl.test.block.line.CampaignLine;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectParser extends AbstractParser {

	private static final Logger LOG = LogManager.getLogger(ProjectParser.class);

	public ProjectParser() {
		LOG.info("Parser initializing..");
		blockParserProvider = new BlockParserProvider();
	}

	public IProject parse(String path) throws IOException, IllegalArgumentException {
		path = cleanPath(path);
		Path p = Paths.get(path);
		List<String> list;
		try (Stream<String> lines = Files.lines(p)) {
			list = lines.collect(Collectors.toList());
		}

		if (list.isEmpty()) {
			throw new IllegalArgumentException("File empty at path: " + path);
		}
		removeBom(list);
		return buildProject(list, p.getFileName().toString(), path);
	}

	private IProject buildProject(List<String> lines, String pageName, String filePath) throws IllegalArgumentException, IOException {
		LOG.info("Starting project parsing: {}", pageName);
		Project project = new Project();
		project.setName(pageName);
		project.setCampaigns(new ArrayList<>());
		while (CollectionUtils.isNotEmpty(lines)) {
			IBlock block = readBlock(lines, filePath);

			if (block instanceof CampaignBlock) {
				project.getCampaigns().add(readCampaignBlock((CampaignBlock) block));
			}

			int numberOfLines = TestParserHelper.getNumberOfBlockLines(block);
			int numberOfLineIncludingHeaderSize = numberOfLines + block.getHeaderSize();
			lines = lines.subList(numberOfLineIncludingHeaderSize, lines.size()); //FIXME index offset needs to be revised, check test case 5
		}

		return project;
	}

	private ICampaign readCampaignBlock(CampaignBlock block) {
		Campaign campaign = new Campaign();
		campaign.setTestCases(new ArrayList<>());
		campaign.setName(block.getCampaignName());
		List<CampaignLine> testCases = block.getTestCases();
		for (CampaignLine testCase : testCases) {
			testCase.getFile().setName(testCase.getName());
			campaign.getTestCases().add(testCase.getFile());
		}
		return campaign;
	}

}
