package org.commitbrowser;

import java.util.List;

import javax.servlet.annotation.WebServlet;








//import com.vaadin.ui.Grid.DetailsGenerator;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.cdi.CDIUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.DetailsGenerator;
import com.vaadin.ui.Grid.RowReference;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ProgressBarRenderer;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;

import javax.inject.Inject;

import org.vaadin.viritin.LazyList;
import org.vaadin.viritin.ListContainer;

/**
 *
 */
@Theme("valo")
@CDIUI("")
public class MyUI extends UI {
    
    @Inject
    GitRepositoryService gitRepositoryService;
    private TextField numberTextField;
    private Grid grid = new Grid();

    @Override
    protected void init(VaadinRequest vaadinRequest) {
    	
    	Layout layout = new VerticalLayout();

        

        LazyList<Commit> lazyList = new LazyList<>(
                firstRow -> gitRepositoryService.find(firstRow,
                        LazyList.DEFAULT_PAGE_SIZE), gitRepositoryService::count);
        grid.setContainerDataSource(new ListContainer(lazyList));
        List<Column> columns = grid.getColumns();
        
        //print out column names for debugging purposes
        for(Column c : columns){
        	System.out.println("c.getPropertyId() = "+c.getPropertyId());
        }

          
        //render size as progressbar
        columns.get(1).setRenderer(new ProgressBarRenderer());
        
     
        //remove commit time
        grid.removeColumn(columns.get(2).getPropertyId());
        
        //remove full message
        grid.removeColumn(columns.get(3).getPropertyId());
        
        layout.setSizeFull();
        grid.setSizeFull();
        
        layout.addComponent(grid);
        
        grid.addItemClickListener(new ItemClickEvent.ItemClickListener(){

			@Override
			public void itemClick(ItemClickEvent event) {
				 Object itemId = event.getItemId();
			     boolean isVisible = grid.isDetailsVisible(itemId);
			     grid.setDetailsVisible(itemId, !isVisible);
			}
        	
        }
        );
        grid.setDetailsGenerator(detailsGenerator);
        
        
        setContent(layout);

    }
    
    private final DetailsGenerator detailsGenerator = new DetailsGenerator() {
        @Override
        public Component getDetails(RowReference rowReference) {
        	FormLayout layout = new FormLayout();
        	        	
            Commit commit = (Commit) rowReference.getItemId();
            
            DateField commitDate = new DateField("Commit Timestamp");
            commitDate.setValue(commit.getCommitTime());
            layout.addComponent(commitDate);
            
            DateField authorDate = new DateField("Author Timestamp");
            authorDate.setValue(commit.getTimestamp());
            layout.addComponent(authorDate);
            
            TextArea textArea = new TextArea("Commit Message");
            textArea.setValue(commit.getFullMessage());
            textArea.setWidth("100%");
            layout.addComponent(textArea);
            
            return layout;
        }
    };
    
    private void toggle() {
        Object itemId = getItemId();
        boolean isVisible = grid.isDetailsVisible(itemId);
        grid.setDetailsVisible(itemId, !isVisible);
    }
    
    private void scrollTo() {
        grid.scrollTo(getItemId());
    }

    
    private Object getItemId() {
        int row = Integer.parseInt(numberTextField.getValue());
        Object itemId = grid.getContainerDataSource().getIdByIndex(row);
        return itemId;
    }
}
