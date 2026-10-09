package com.htyoudao.youdao.module.system.api.store.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class StoreStatusLogDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3065792229106688268L;

    private Long id;
    private Long storeId;
    private Integer storeStatus; // 0/1/2
    private LocalDateTime createTime;
    private Boolean deleted;
}
