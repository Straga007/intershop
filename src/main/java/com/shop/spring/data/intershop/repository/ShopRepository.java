package com.shop.spring.data.intershop.repository;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ShopRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShopRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Item> itemRowMapper = (rs, rowNum) -> new Item(
            String.valueOf(rs.getLong("id")),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("image"),
            rs.getInt("count"),
            rs.getDouble("price")
    );

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        String orderId = String.valueOf(rs.getLong("id"));
        List<Item> items = getOrderItems(orderId);
        return new Order(orderId, items);
    };

    public List<Item> findAllItems() {
        String sql = "SELECT * FROM items";
        return jdbcTemplate.query(sql, itemRowMapper);
    }

    public Item findItemById(String id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, itemRowMapper, Long.parseLong(id));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Item> findItemsBySearch(String search) {
        String sql = "SELECT * FROM items WHERE title LIKE ? OR description LIKE ?";
        String searchTerm = "%" + search + "%";
        return jdbcTemplate.query(sql, itemRowMapper, searchTerm, searchTerm);
    }

    public List<Order> findAllOrders() {
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        return jdbcTemplate.query(sql, orderRowMapper);
    }

    public Order findOrderById(String id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, orderRowMapper, Long.parseLong(id));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private List<Item> getOrderItems(String orderId) {
        String sql = "SELECT i.*, oi.quantity FROM items i " +
                "JOIN order_items oi ON i.id = oi.item_id " +
                "WHERE oi.order_id = ?";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new Item(
                    String.valueOf(rs.getLong("id")),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("image"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
            ), Long.parseLong(orderId));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String createOrder(List<Item> items) {
        String orderId = String.valueOf(System.currentTimeMillis());

        String orderSql = "INSERT INTO orders (id, order_date) VALUES (?, ?)";
        jdbcTemplate.update(orderSql, Long.parseLong(orderId), LocalDateTime.now());

        for (Item item : items) {
            String itemSql = "INSERT INTO order_items (order_id, item_id, quantity) VALUES (?, ?, ?)";
            jdbcTemplate.update(itemSql, Long.parseLong(orderId), Long.parseLong(item.getId()), item.getCount());
        }

        return orderId;
    }

    public void updateItemCount(String itemId, int newCount) {
        String sql = "UPDATE items SET count = ? WHERE id = ?";
        jdbcTemplate.update(sql, newCount, Long.parseLong(itemId));
    }
}
