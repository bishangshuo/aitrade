package com.aitrade.exchange.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import com.aitrade.exchange.component.SymbolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.exchange.mapper.AtInstMapper;
import com.aitrade.exchange.domain.AtInst;
import com.aitrade.exchange.service.IAtInstService;

/**
 * 币种管理Service业务层处理
 * 
 * @author aitrade
 * @date 2026-05-14
 */
@Service
public class AtInstServiceImpl implements IAtInstService 
{
    @Autowired
    private AtInstMapper atInstMapper;

    @Autowired
    private SymbolManager symbolManager;

    /**
     * 查询币种管理
     * 
     * @param id 币种管理主键
     * @return 币种管理
     */
    @Override
    public AtInst selectAtInstById(Long id)
    {
        return atInstMapper.selectAtInstById(id);
    }

    /**
     * 查询币种管理列表
     * 
     * @param atInst 币种管理
     * @return 币种管理
     */
    @Override
    public List<AtInst> selectAtInstList(AtInst atInst)
    {
        return atInstMapper.selectAtInstList(atInst);
    }

    /**
     * 新增币种管理
     * 
     * @param atInst 币种管理
     * @return 结果
     */
    @Override
    public int insertAtInst(AtInst atInst)
    {
        atInst.setCreateTime(DateUtils.getNowDate());
        return atInstMapper.insertAtInst(atInst);
    }

    /**
     * 修改币种管理
     * 
     * @param atInst 币种管理
     * @return 结果
     */
    @Override
    public int updateAtInst(AtInst atInst)
    {
        AtInst old = atInstMapper.selectAtInstById(atInst.getId());
        atInst.setUpdateTime(DateUtils.getNowDate());
        int res = atInstMapper.updateAtInst(atInst);

        //添加监控
        if(old.getWatch().equals(0) && atInst.getWatch().equals(1)) {
            symbolManager.addSymbol(atInst.getInstId());
        }
        //删除监控
        else if(old.getWatch().equals(1) && atInst.getWatch().equals(0)) {
            symbolManager.removeSymbol(atInst.getInstId());
        }
        return res;
    }

    /**
     * 批量删除币种管理
     * 
     * @param ids 需要删除的币种管理主键
     * @return 结果
     */
    @Override
    public int deleteAtInstByIds(Long[] ids)
    {
        return atInstMapper.deleteAtInstByIds(ids);
    }

    /**
     * 删除币种管理信息
     * 
     * @param id 币种管理主键
     * @return 结果
     */
    @Override
    public int deleteAtInstById(Long id)
    {
        return atInstMapper.deleteAtInstById(id);
    }
}
