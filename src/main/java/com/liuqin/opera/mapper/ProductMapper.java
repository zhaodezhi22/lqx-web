package com.liuqin.opera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liuqin.opera.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}

