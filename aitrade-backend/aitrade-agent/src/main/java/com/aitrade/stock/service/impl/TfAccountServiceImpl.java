package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfAccountMapper;
import com.aitrade.stock.domain.TfAccount;
import com.aitrade.stock.service.ITfAccountService;

/**
 * 回測虛擬賬戶Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@Service
public class TfAccountServiceImpl implements ITfAccountService 
{
    @Autowired
    private TfAccountMapper tfAccountMapper;

    /**
     * 查询回測虛擬賬戶
     * 
     * @param id 回測虛擬賬戶主键
     * @return 回測虛擬賬戶
     */
    @Override
    public TfAccount selectTfAccountById(Long id)
    {
        return tfAccountMapper.selectTfAccountById(id);
    }

    /**
     * 查询回測虛擬賬戶列表
     * 
     * @param tfAccount 回測虛擬賬戶
     * @return 回測虛擬賬戶
     */
    @Override
    public List<TfAccount> selectTfAccountList(TfAccount tfAccount)
    {
        return tfAccountMapper.selectTfAccountList(tfAccount);
    }

    /**
     * 新增回測虛擬賬戶
     * 
     * @param tfAccount 回測虛擬賬戶
     * @return 结果
     */
    @Override
    public int insertTfAccount(TfAccount tfAccount)
    {
        tfAccount.setCreateTime(DateUtils.getNowDate());
        return tfAccountMapper.insertTfAccount(tfAccount);
    }

    /**
     * 修改回測虛擬賬戶
     * 
     * @param tfAccount 回測虛擬賬戶
     * @return 结果
     */
    @Override
    public int updateTfAccount(TfAccount tfAccount)
    {
        tfAccount.setUpdateTime(DateUtils.getNowDate());
        return tfAccountMapper.updateTfAccount(tfAccount);
    }

    /**
     * 批量删除回測虛擬賬戶
     * 
     * @param ids 需要删除的回測虛擬賬戶主键
     * @return 结果
     */
    @Override
    public int deleteTfAccountByIds(Long[] ids)
    {
        return tfAccountMapper.deleteTfAccountByIds(ids);
    }

    /**
     * 删除回測虛擬賬戶信息
     * 
     * @param id 回測虛擬賬戶主键
     * @return 结果
     */
    @Override
    public int deleteTfAccountById(Long id)
    {
        return tfAccountMapper.deleteTfAccountById(id);
    }
}
