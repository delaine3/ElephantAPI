package org.example;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DataTable {
        private int draw;
        private int start;
        private int length; // Number of recrds per page
        private Search search;
        private List<Column> columns;
        private List<Order> order;

        public String getSearchValue() {
                return search != null ? search.getValue() : ""; // Empty if no search string
        }

        public String getOrderColumn() {
                if (order != null && !order.isEmpty() && columns != null && !columns.isEmpty()) { // Columns is an array of objects (the cols). If there are columns and an order value, order the db results
                        int orderIndex = order.get(0).getColumn(); // Column to order by
                        return columns.get(orderIndex).getData();
                }
                return "Name"; // If we don't have a column to sort by
        }

        public String getOrderDir() { // Order is a list with 1 object.
                return order != null && !order.isEmpty() ? order.get(0).getDir() : "ASC";
        }
}