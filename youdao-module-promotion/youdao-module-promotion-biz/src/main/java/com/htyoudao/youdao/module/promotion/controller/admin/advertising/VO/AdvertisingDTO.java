package com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO;

import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.AdvertisingDO;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//广告返回
@Component
@Data
public class AdvertisingDTO {
    Map<Integer ,List<AdvertisingRespVO>>integerListMap = new HashMap<>();
}
