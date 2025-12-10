package ru.mirakyan.mymarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagingDto {
    private int pageSize;
    private int pageNumber;
    private boolean hasPrevious;
    private boolean hasNext;
}
