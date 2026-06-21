package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.entity.ClaimRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClaimRecordMapper extends BaseMapper<ClaimRecord> {

    /**
     * 统计累计归还数
     */
    @Select("SELECT COUNT(*) FROM claim_record")
    long countTotalReturned();

    /**
     * 统计当前未领取数
     */
    @Select("SELECT COUNT(*) FROM lost_item WHERE status = 'UNCLAIMED'")
    long countCurrentUnclaimed();
}
