package com.ec.survey.model.survey;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents a section element in a survey
 */
@Entity
@DiscriminatorValue("SECTION")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Section extends Element {
	
	public static final String TABTITLE = "TABTITLE";
	private static final long serialVersionUID = 1L;
	private int level = 1;
	private String tabTitle;
	
	public Section(String ptitle, String shortname, String uid) {
		setTitle(ptitle);
		setShortname(shortname);
		setUniqueId(uid);
	}
	
	public Section() {
	}	
	
	@Column(name = "TABTITLE")
	public String getTabTitle() {
		return tabTitle;
	}
	public void setTabTitle(String tabTitle) {
		this.tabTitle = tabTitle;
	}
	
	@Column(name = "HLEVEL")
	public Integer getLevel() {
		return level;
	}	
	public void setLevel(Integer level) {
		this.level = level;
	}

	public Section copy(String fileDir)
	{
		Section copy = new Section();
		copy.setUniqueId(getUniqueId());
		copy.setShortname(this.getShortname());
		copy.tabTitle = tabTitle;
		copy.setLevel(level);
		copy.setSourceId(this.getId());
		copy.setTitle(this.getTitle());
		copy.setPosition(this.getPosition());
		
		return copy;
	}
	
	@Override
	public boolean differsFrom(Element element) {
		if (basicDiffersFrom(element)) return true;
		
		if (!(element instanceof Section)) return true;
		
		Section section = (Section)element;

		return getLevel() != null && !getLevel().equals(section.getLevel());

	}
}
