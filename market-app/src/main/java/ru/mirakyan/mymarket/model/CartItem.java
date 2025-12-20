package ru.mirakyan.mymarket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("item_id")
    private Long itemId;

    @Transient
    private Item item;

    @Column("count")
    private Integer count;

    public CartItem(Long userId, Long itemId, Integer count) {
        this.userId = userId;
        this.itemId = itemId;
        this.count = count;
    }
}
