package org.commitbrowser;

import java.util.List;

import javax.inject.Inject;

import org.vaadin.viritin.LazyList;

//import com.vaadin.ui.Grid.DetailsGenerator;
import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.DetailsGenerator;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Grid.RowReference;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ProgressBarRenderer;

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
                        LazyList.DEFAULT_PAGE_SIZE),
                gitRepositoryService::count);
        // IndexedContainer container = new IndexedContainer(lazyList);
        FilterableListContainer container = new FilterableListContainer<Commit>(
                lazyList);
        grid.setContainerDataSource(container);

        List<Column> columns = grid.getColumns();

        // print out column names for debugging purposes
        for (Column c : columns) {
            System.out.println("c.getPropertyId() = " + c.getPropertyId());
        }

        // render size as progressbar
        columns.get(1).setRenderer(new ProgressBarRenderer());

        // remove commit time
        grid.removeColumn(columns.get(2).getPropertyId());

        // remove full message
        grid.removeColumn(columns.get(3).getPropertyId());

        // Allow column reordering
        grid.setColumnReorderingAllowed(true);

        // Allow column hiding for all columns
        // FIXME: breaks the grid
        // grid.getColumns().forEach(column -> column.setHidable(true));

        // Create a header row to hold column filters
        HeaderRow filterRow = grid.appendHeaderRow();

        // Set up a filter for all columns
        for (Object pid : grid.getContainerDataSource()
                .getContainerPropertyIds()) {
            HeaderCell cell = filterRow.getCell(pid);
            if (cell == null) {
                continue;
            }

            // Have an input field to use for filter
            TextField filterField = new TextField();
            filterField.setColumns(8);

            // Update filter When the filter input is changed
            filterField.addTextChangeListener(change -> {
                // Can't modify filters so need to replace
                    System.err.println("Got text change event");
                    container.removeContainerFilters(pid);

                    boolean ignoreCase = true;
                    boolean onlyMatchPrefix = false;

                    // (Re)create the filter if necessary
                    if (!change.getText().isEmpty()) {
                        System.err.println("Adding filter");
                        container.addContainerFilter(pid, change.getText(),
                                ignoreCase, onlyMatchPrefix);
                    }
                });
            cell.setComponent(filterField);
        }

        layout.setSizeFull();
        grid.setSizeFull();

        layout.addComponent(grid);

        grid.addItemClickListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                Object itemId = event.getItemId();
                boolean isVisible = grid.isDetailsVisible(itemId);
                grid.setDetailsVisible(itemId, !isVisible);
            }

        });
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
