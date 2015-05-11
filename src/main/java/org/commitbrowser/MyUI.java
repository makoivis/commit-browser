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
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.DetailsGenerator;
import com.vaadin.ui.Grid.RowReference;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
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
    private TextArea fullMessage;
    private Grid grid = new Grid();

    @Override
    protected void init(VaadinRequest vaadinRequest) {
    	
    	Layout layout = new VerticalLayout();
    	fullMessage = new TextArea();
    	fullMessage.setWidth("1400px");
        

        LazyList<Commit> lazyList = new LazyList<>(
                firstRow -> gitRepositoryService.find(firstRow,
                        LazyList.DEFAULT_PAGE_SIZE), gitRepositoryService::count);
        grid.setContainerDataSource(new ListContainer(lazyList));
        List<Column> columns = grid.getColumns();
        
        //print out column names for debugging purposes
        for(Column c : columns){
        	System.out.println("c.getPropertyId() = "+c.getPropertyId());
        }
        
        //remove size
        grid.removeColumn(columns.get(1).getPropertyId());
        //remove full message
        grid.removeColumn(columns.get(1).getPropertyId());
        
        grid.setSizeFull();
        //grid.setWidth("1400px");

        layout.addComponent(grid);
        layout.addComponent(fullMessage);
        
        
        grid.addItemClickListener(new ItemClickEvent.ItemClickListener(){

			@Override
			public void itemClick(ItemClickEvent event) {
				Commit commit = (Commit)event.getItemId();
				fullMessage.setValue(commit.getFullMessage());
			}
        	
        }
        );
        
        
        
        
        final CheckBox checkbox = new CheckBox("Details generator");
        checkbox.addValueChangeListener(new ValueChangeListener() {
            @Override
            @SuppressWarnings("boxing")
            public void valueChange(ValueChangeEvent event) {
                if (checkbox.getValue()) {
                    grid.setDetailsGenerator(detailsGenerator);
                } else {
                    grid.setDetailsGenerator(DetailsGenerator.NULL);
                }
            }
        });
        layout.addComponent(checkbox);
        
        numberTextField = new TextField("Row");
        numberTextField.setImmediate(true);
        layout.addComponent(numberTextField);

        layout.addComponent(new Button("Toggle and scroll",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        toggle();
                        scrollTo();
                    }
                }));
        layout.addComponent(new Button("Scroll and toggle",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        scrollTo();
                        toggle();
                    }
                }));
        
        setContent(layout);

    }
    
    private final DetailsGenerator detailsGenerator = new DetailsGenerator() {
        @Override
        public Component getDetails(RowReference rowReference) {
            Commit commit = (Commit) rowReference.getItemId();
            TextArea textArea = new TextArea();
            textArea.setValue(commit.getFullMessage());
            return textArea;
            // currently the decorator row doesn't change its height when the
            // content height is different.
           // label.setHeight("30px");
            //return label;
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
