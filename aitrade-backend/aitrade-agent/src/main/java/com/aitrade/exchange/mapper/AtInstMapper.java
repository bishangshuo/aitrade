package com.aitrade.exchange.mapper;

import java.util.List;
import com.aitrade.exchange.domain.AtInst;
import org.apache.ibatis.annotations.Param;

/**
 * 币种管理Mapper接口
 * 
 * @author aitrade
 * @date 2026-05-14
 */
public interface AtInstMapper 
{
    /**
     * 查询币种管理
     * 
     * @param id 币种管理主键
     * @return 币种管理
     */
    public AtInst selectAtInstById(Long id);

    /**
     * 查询币种管理列表
     * 
     * @param atInst 币种管理
     * @return 币种管理集合
     */
    public List<AtInst> selectAtInstList(AtInst atInst);

    /**
     * 新增币种管理
     * 
     * @param atInst 币种管理
     * @return 结果
     */
    public int insertAtInst(AtInst atInst);

    /**
     * 修改币种管理
     * 
     * @param atInst 币种管理
     * @return 结果
     */
    public int updateAtInst(AtInst atInst);

    /**
     * 删除币种管理
     * 
     * @param id 币种管理主键
     * @return 结果
     */
    public int deleteAtInstById(Long id);

    /**
     * 批量删除币种管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAtInstByIds(Long[] ids);

    AtInst findByInstId(@Param("instId") String instId);
}
