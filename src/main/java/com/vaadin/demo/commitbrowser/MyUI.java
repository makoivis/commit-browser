package com.vaadin.demo.commitbrowser;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

import javax.inject.Inject;

import com.vaadin.event.SelectionEvent;
import com.vaadin.ui.renderers.HtmlRenderer;
import elemental.json.JsonValue;
import org.vaadin.viritin.LazyList;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.DetailsGenerator;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Grid.RowReference;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ProgressBarRenderer;
import com.vaadin.ui.themes.ValoTheme;

/**
 *
 */
@Theme("tests-valo")
@CDIUI("")
public class MyUI extends UI {

    @Inject
    GitRepositoryService gitRepositoryService;
    private Grid grid = new Grid();

    private static LinkedHashMap<String, String> themeVariants = new LinkedHashMap<String, String>();

    private static class MinimalSizeHtmlRenderer extends HtmlRenderer {
        @Override
        public JsonValue encode(String value) {
            return super.encode("<div style=';pointer-events: none'><div style='width:10px;overflow-x:visible;pointer-events: none'>" +value + "</div></div>");
        }
    }

    static {
        themeVariants.put("tests-valo", "Default");
        themeVariants.put("tests-valo-blueprint", "Blueprint");
        themeVariants.put("tests-valo-dark", "Dark");
        themeVariants.put("tests-valo-facebook", "Facebook");
        themeVariants.put("tests-valo-flatdark", "Flat dark");
        themeVariants.put("tests-valo-flat", "Flat");
        themeVariants.put("tests-valo-light", "Light");
        themeVariants.put("tests-valo-metro", "Metro");
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        boolean isEmbedded = "embedded".equals(vaadinRequest.getParameter("type"));
        VerticalLayout layout = new VerticalLayout();
        layout.setId("rootLayout");
        layout.setMargin(!isEmbedded);
        layout.setSpacing(!isEmbedded);

        // Add theme selector
        if(!isEmbedded) {
            Component themeSelector = buildThemeSelector();
            layout.addComponent(themeSelector);
            layout.setComponentAlignment(themeSelector, Alignment.TOP_RIGHT);
        }
        LazyList<Commit> lazyList = new LazyList<>(
                firstRow -> gitRepositoryService.find(firstRow,
                        LazyList.DEFAULT_PAGE_SIZE),
                gitRepositoryService::count);

        BeanItemContainer<Commit> container = new BeanItemContainer<Commit>(
                Commit.class, lazyList);

        grid.setContainerDataSource(container);

        List<Column> columns = grid.getColumns();

        // print out column names for debugging purposes
        for (Column c : columns) {
            System.out.println("c.getPropertyId() = " + c.getPropertyId());
        }

        grid.setColumns("fullName","fullTopic","size","timestamp");

        grid.getColumn("fullName").setExpandRatio(1);
        grid.getColumn("fullName").setRenderer(new MinimalSizeHtmlRenderer());
//        grid.getColumn("fullName").setWidth(185);

        grid.getColumn("fullTopic").setExpandRatio(2);
        grid.getColumn("fullTopic").setRenderer(new MinimalSizeHtmlRenderer());
//        grid.getColumn("fullTopic").setWidth(372);
        // Allow column hiding for all columns
        grid.getColumns().forEach(column -> column.setHidable(true));

        // render size as progressbar
        grid.getColumn("size").setRenderer(new ProgressBarRenderer());

        // Allow column reordering
        grid.setColumnReorderingAllowed(true);

        // Create a header row to hold column filters
        HeaderRow filterRow = grid.appendHeaderRow();

        // Set up a filter for author, topic, date and email

        // set up dateFilters
        startFilter = new DateFilter(null, true);
        endFilter = new DateFilter(null, false);

        for (Object pid : grid.getContainerDataSource()
                .getContainerPropertyIds()) {

            // if we are not in one of the tree columns, move on
            if (!(pid.equals("fullName") || pid.equals("fullTopic") || pid.equals("timestamp"))) {
                continue;
            }

            HeaderCell cell = filterRow.getCell(pid);

            // if we are dealing with a text field, add a simple string filter.

            if (pid.equals("fullName") || pid.equals("fullTopic")) {

                // Have an input field to use for filter
                TextField filterField = new TextField();
                filterField.setWidth(100, Unit.PERCENTAGE);
                filterField.addStyleName(ValoTheme.TEXTFIELD_SMALL);

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

            // if we are dealing with a date field, add a date range filter.
            if (pid.equals("timestamp")) {
                // we need a start date and an end date
                HorizontalLayout hl = new HorizontalLayout();
                hl.setSpacing(true);

                DateField startDate = new DateField();
                DateField endDate = new DateField();
                startDate.addStyleName(ValoTheme.DATEFIELD_SMALL);
                endDate.addStyleName(ValoTheme.DATEFIELD_SMALL);
                startDate.setWidth(120, Unit.PIXELS);
                endDate.setWidth(120, Unit.PIXELS);

                Label dash = new Label("-");
                dash.setSizeUndefined();

                hl.addComponent(startDate);
                hl.addComponent(dash);
                hl.addComponent(endDate);

                hl.setComponentAlignment(dash, Alignment.MIDDLE_CENTER);

                startDate.addValueChangeListener(new ValueChangeListener() {

                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        Date start = (Date) event.getProperty().getValue();
                        // remove filter
                        startFilter = new DateFilter(start, true);
                        container.removeContainerFilters("timestamp");
                        container.addContainerFilter(startFilter);
                        container.addContainerFilter(endFilter);
                    }
                });

                endDate.addValueChangeListener(new ValueChangeListener() {

                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        Date end = (Date) event.getProperty().getValue();
                        // remove filter
                        endFilter = new DateFilter(end, false);
                        container.removeContainerFilters("timestamp");
                        container.addContainerFilter(startFilter);
                        container.addContainerFilter(endFilter);
                    }
                });

                cell.setComponent(hl);

            }

        }

        layout.setSizeFull();
        grid.setSizeFull();

        layout.addComponent(grid);
        layout.setExpandRatio(grid, 1);
        
        

        grid.addItemClickListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                Object itemId = event.getItemId();
                boolean isVisible = grid.isDetailsVisible(itemId);
                grid.setDetailsVisible(itemId, !isVisible);
            }

        });
        grid.setDetailsGenerator(detailsGenerator);
        grid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent selectionEvent) {
                for (Object id : selectionEvent.getAdded()) {
                    grid.setDetailsVisible(id, true);
                }
                if(selectionEvent.getSelected().isEmpty()) {
                    for (Object id : selectionEvent.getRemoved()) {
                        grid.setDetailsVisible(id, false);
                    }
                }
            }
        });
        layout.addShortcutListener(new ShortcutListener("Close details row",
                KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                try {
                      grid.setDetailsVisible(grid.getSelectedRow(), false);
                } catch (Exception ignore) {
                }
            }
        });

        setContent(layout);

    }

    @SuppressWarnings("unchecked")
    private Component buildThemeSelector() {
        final NativeSelect ns = new NativeSelect();
        ns.setNullSelectionAllowed(false);
        ns.setId("themeSelect");
        ns.addContainerProperty("caption", String.class, "");
        ns.setItemCaptionPropertyId("caption");
        for (final String identifier : themeVariants.keySet()) {
            ns.addItem(identifier).getItemProperty("caption")
                    .setValue(themeVariants.get(identifier));
        }

        ns.setValue("tests-valo");
        ns.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                setTheme((String) ns.getValue());
            }
        });
        return ns;
    }

    private final DetailsGenerator detailsGenerator = new DetailsGenerator() {
        @Override
        public Component getDetails(RowReference rowReference) {
            FormLayout layout = new FormLayout();
            layout.setMargin(true);

            Commit commit = (Commit) rowReference.getItemId();

            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                    DateFormat.MEDIUM, getLocale());

            Label commitDate = new Label();
            commitDate.setCaption("Commit Timestamp");
            commitDate.setValue(df.format(commit.getCommitTime()));
            layout.addComponent(commitDate);

            Label authorDate = new Label();
            authorDate.setCaption("Author Timestamp");
            authorDate.setValue(df.format(commit.getTimestamp()));
            authorDate.setReadOnly(true);
            layout.addComponent(authorDate);

            Label msg = new Label();
            msg.setCaption("Commit Message");
            msg.setWidth("100%");
            msg.setContentMode(ContentMode.HTML);
            msg.setStyleName("align-value-top");
            layout.addComponent(msg);
            String text = commit.getFullMessage().replaceAll("(\\n\\r)|(\\n\\r)","\\n");
            text = text.replaceAll("\\r","\\n");
            text = text.replaceAll("\\n","<br/>");
            msg.setValue(text);

            return layout;
        }
    };
    private DateFilter startFilter;
    private DateFilter endFilter;

}
