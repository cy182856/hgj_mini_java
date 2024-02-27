package com.ej.hgj.entity.bill;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BillYear {

    private String id;

    private Boolean checked;

    private Boolean isHidden;

    private Boolean bindAll;

    private String year;

    private String title;

    private BigDecimal priRev;

    private List<BillItem> children;
}
