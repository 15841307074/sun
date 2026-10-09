package com.htyoudao.youdao.module.bpm.service.definition;


import com.htyoudao.youdao.module.bpm.BpmServerApplication;
import java.util.List;
import org.flowable.engine.repository.Model;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = BpmServerApplication.class)
class BpmModelServiceImplTest {


    @Autowired
    private BpmModelService bpmModelService;

    @Test
    void getModelList() {
        List<Model> modelList = bpmModelService.getModelList(null);
        for (Model model : modelList) {
            System.out.println(model.getCreateTime());
        }
    }
}