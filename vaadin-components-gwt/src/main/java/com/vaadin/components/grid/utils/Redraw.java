package com.vaadin.components.grid.utils;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.plugins.Resize;
import com.google.gwt.user.client.Timer;
import com.vaadin.client.widgets.Grid;
import com.vaadin.components.grid.GridComponent;
import com.vaadin.components.grid.data.GridDataSource;

/**
 * Right now we need to notify the grid for size changed.
 */
public class Redraw extends Timer {
    private final Grid<?> grid;
    private GQuery container;
    boolean heightByRows = false;
    boolean heightAuto = false;
    private int defaultRows, numberRows, width, height;

    public Redraw(GridComponent gridComponent) {
        grid = gridComponent.getGrid();
    }

    public void setContainer(Element containerElement) {
        container = $(containerElement);
        // Use gQuery resize plugin to observe resize changes in the container.
        container.as(Resize.Resize).resize(new Function(){
            public void f() {
                redraw(false);
            }
        });
    }

    public void redraw(boolean force) {
        if (force) {
            width = height = 0;
        }
        schedule(50);
    }

    public void run() {
        if (defaultRows == 0) {
            defaultRows = (int) grid.getHeightByRows();
            if (numberRows == 0) {
                numberRows = defaultRows;
            }
        }

        int w = container.width();
        if (w != width) {
            // Setting grid to 100% makes it fit to our v-grid container.
            // We could set this in CSS, but we should be sure that it is set
            // with selectors able override inline sizes, because width is
            // always set by the grid.
            grid.setWidth("100%");
            grid.recalculateColumnWidths();
            width = w;
        }

        // If height is set using the 'rows' attribute, we always use it.
        if (heightByRows) {
            if (numberRows != defaultRows) {
                numberRows = defaultRows;
                setHeightByRows(numberRows);
            }
        } else {
            int h = container.height();
            if (h != height) {
                // Let see whether our container has a height set in CCS or it's
                // auto. The only way to know it is comparing container and grid
                // sizes.
                heightAuto = h == $(grid).innerHeight();
                if (!heightAuto) {
                    // Container has a fixed height, so setting it to 100% makes
                    // the grid expand to fill all the space. It also makes the
                    // grid to recompute rows, so we use it as a refresh
                    // mechanism. We cannot set this in CSS because we should
                    // override inline sizes making setHeightByRows fail.
                    grid.setHeight("100%");
                    height = h;
                } else if (height == 0) {
                    // There is no height set for our container, if the number
                    // of rows of the data source is smaller then the default
                    // size we reduce it.
                    GridDataSource ds = (GridDataSource) grid.getDataSource();
                    int nsize = Math.min(ds == null ? 0 : ds.size(),
                            defaultRows);
                    if (nsize != numberRows) {
                        numberRows = nsize;
                        setHeightByRows(numberRows);
                    }
                }
                grid.resetSizesFromDom();
            }
        }
    }

    // TODO: This method is here because original grid.setHeightByRows seems
    // not working and it always sets a value of 10 rows. It also used to
    // have performance problems in FF.
    void setHeightByRows(int rows) {
        // grid.setHeightByRows(rows);
        int h = $(grid).find("tr td").height() + 1;
        rows += grid.getHeaderRowCount() + grid.getFooterRowCount();
        height = h * rows;
        grid.setHeight(height + "px");
    }

    public void setSize(int size) {
        if (!heightByRows || size != defaultRows) {
            heightByRows = size > 0;
            defaultRows = size;
            redraw(true);
        }
    }

    public int getSize() {
        return defaultRows;
    }
}