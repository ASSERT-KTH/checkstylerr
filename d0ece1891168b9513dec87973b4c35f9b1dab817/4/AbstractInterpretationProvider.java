package com.synaptix.toast.action.interpret.web;

import java.util.ArrayList;
import java.util.List;

import com.synaptix.toast.core.agent.interpret.WebEventRecord;
import com.synaptix.toast.dao.domain.impl.repository.ElementImpl;
import com.synaptix.toast.dao.domain.impl.repository.RepositoryImpl;
import com.synaptix.toast.swing.agent.interpret.MongoRepositoryCacheWrapper;

public abstract class AbstractInterpretationProvider implements IActionInterpret {

	private MongoRepositoryCacheWrapper mongoRepoManager;
	private List<ElementImpl> elements;

	public AbstractInterpretationProvider(MongoRepositoryCacheWrapper mongoRepoManager){
		this.mongoRepoManager = mongoRepoManager;
		this.elements = new ArrayList<>();
	}
	
	public String getLabel(WebEventRecord eventRecord) {
		RepositoryImpl container = mongoRepoManager.findContainer(eventRecord.parent, "web page");
		ElementImpl element = mongoRepoManager.find(container, eventRecord);
		elements.add(element);
		return container.name + "." + getElementLabel(element);
	}
	
	@Override
	public List<ElementImpl> getElements(){
		return elements;
	}
	
	@Override
	public void clearElements(){
		elements.clear();
	}
	
	public String getElementLabel(ElementImpl element){
		return "".equals(element.name) || element.name == null ? element.locator : element.name;
	}

	public abstract String convertToKnowType(String type);

	
}
