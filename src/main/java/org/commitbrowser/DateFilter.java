package org.commitbrowser;

import java.util.Date;

import com.vaadin.data.Item;
import com.vaadin.data.Container.Filter;

final class DateFilter implements Filter {
	/**
	 * 
	 */
	private Date comparisonDate;
	private boolean startDate;

	/**
	 * @param myUI
	 */
	DateFilter(Date date, boolean startDate){
		this.comparisonDate = date;
		this.startDate = startDate;
	}

	@Override
	public boolean passesFilter(Object itemId, Item item)
			throws UnsupportedOperationException {
		
		if(comparisonDate == null){
			return true;
		}
		
		Date current = ((Commit)itemId).getTimestamp();
		boolean after; 
		if (current.after(comparisonDate)) {
			after = true;
		} else {
			after = false;
		}
		if(after && startDate){
			return true;
		}
		if(!after && !startDate){
			return true;
		}
		return false;
		
		
	}

	@Override
	public boolean appliesToProperty(Object propertyId) {
		if (propertyId.equals("timestamp")) {
			return true;
		} else {
			return false;
		}
	}
}