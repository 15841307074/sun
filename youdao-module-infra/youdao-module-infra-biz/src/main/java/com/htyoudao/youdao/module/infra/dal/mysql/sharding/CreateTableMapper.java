package com.htyoudao.youdao.module.infra.dal.mysql.sharding;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

/**
 * details
 *
 * @author liuzhaowang
 */
@Mapper
public interface CreateTableMapper {

    /**
     * 创建分片表
     *
     * @param tableName 表名称
     * @param suffix    后缀
     */
    @Update("CREATE TABLE IF NOT EXISTS ${tableName}_${suffix} LIKE ${tableName};")
    void createShardingTable(@Param("tableName") String tableName, @Param("suffix") String suffix);

    /**
     * 查询表名称
     *
     * @param tableName 表名称
     * @return {@link List }<{@link String }>
     */
    @Select("SHOW TABLES LIKE '${tableName}';")
    Set<String> selectTables(@Param("tableName") String tableName);
}
