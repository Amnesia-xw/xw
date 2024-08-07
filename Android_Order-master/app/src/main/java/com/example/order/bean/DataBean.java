package com.example.order.bean;


import com.example.order.R;

import java.util.ArrayList;
import java.util.List;

public class DataBean {
    public Integer imageRes;

    public DataBean(Integer imageRes) {
        this.imageRes = imageRes;
    }

    public static List<DataBean> getTestData() {
        List<DataBean> list = new ArrayList<>();
        list.add(new DataBean(R.drawable.banner_1));
        list.add(new DataBean(R.drawable.banner_2));
        list.add(new DataBean(R.drawable.banner_3));
        return list;
    }

}

