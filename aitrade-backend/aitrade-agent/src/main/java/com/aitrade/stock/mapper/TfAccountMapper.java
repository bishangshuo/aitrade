package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TfAccount;

/**
 * 回測虛擬賬戶Mapper接口
 * 
 * @author aitrade
 * @date 2026-10-25
 */
public interface TfAccountMapper 
{
    /**
     * 查询回測虛擬賬戶
     * 
     * @param id 回測虛擬賬戶主键
     * @return 回測虛擬賬戶
     */
    public TfAccount selectTfAccountById(Long id);

    /**
     * 查询回測虛擬賬戶列表
     * 
     * @param tfAccount 回測虛擬賬戶
     * @return 回測虛擬賬戶集合
     */
    public List<TfAccount> selectTfAccountList(TfAccount tfAccount);

    /**
     * 新增回測虛擬賬戶
     * 
     * @param tfAccount 回測虛擬賬戶
     * @return 结果
     */
    public int insertTfAccount(TfAccount tfAccount);

    /**
     * 修改回測虛擬賬戶
     * 
     * @param tfAccount 回測虛擬賬戶
     * @return 结果
     */
    public int updateTfAccount(TfAccount tfAccount);

    /**
     * 删除回測虛擬賬戶
     * 
     * @param id 回測虛擬賬戶主键
     * @return 结果
     */
    public int deleteTfAccountById(Long id);

    /**
     * 批量删除回測虛擬賬戶
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfAccountByIds(Long[] ids);
}
