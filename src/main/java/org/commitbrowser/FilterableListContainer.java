package org.commitbrowser;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.viritin.ListContainer;

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.UnsupportedFilterException;

public class FilterableListContainer<T> extends ListContainer<T> implements Container.SimpleFilterable{

	List<SimpleStringFilter> filters = new LinkedList<SimpleStringFilter>();
	
	
	public FilterableListContainer(Collection<T> backingList) {
		super(backingList);
	}


	@Override
	public void removeAllContainerFilters() {
		filters.clear();
	}



	@Override
	public void addContainerFilter(Object propertyId, String filterString,
			boolean ignoreCase, boolean onlyMatchPrefix) {
		SimpleStringFilter filter = new SimpleStringFilter(propertyId, filterString, ignoreCase, onlyMatchPrefix);
		filters.add(filter);
	}

	/**@TODO Add actual filtering logic */
	

	@Override
	public void removeContainerFilters(Object propertyId) {
		for (SimpleStringFilter filter : filters){
			if (filter.getPropertyId().equals(propertyId)){
				filters.remove(filter);
			}
		}
		
	}

}
